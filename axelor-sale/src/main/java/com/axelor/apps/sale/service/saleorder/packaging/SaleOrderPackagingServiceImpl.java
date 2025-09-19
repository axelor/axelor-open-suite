/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.sale.service.saleorder.packaging;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderPackagingServiceImpl implements SaleOrderPackagingService {

  protected ProductRepository productRepository;
  protected SaleOrderPackagingPlanService saleOrderPackagingPlanService;
  protected SaleOrderPackagingDimensionService saleOrderPackagingDimensionService;
  protected SaleOrderPackagingMessageService saleOrderPackagingMessageService;
  protected SaleOrderProductPackagingService saleOrderProductPackagingService;

  @Inject
  public SaleOrderPackagingServiceImpl(
      ProductRepository productRepository,
      SaleOrderPackagingPlanService saleOrderPackagingPlanService,
      SaleOrderPackagingDimensionService saleOrderPackagingDimensionService,
      SaleOrderPackagingMessageService saleOrderPackagingMessageService,
      SaleOrderProductPackagingService saleOrderProductPackagingService) {
    this.productRepository = productRepository;
    this.saleOrderPackagingPlanService = saleOrderPackagingPlanService;
    this.saleOrderPackagingDimensionService = saleOrderPackagingDimensionService;
    this.saleOrderPackagingMessageService = saleOrderPackagingMessageService;
    this.saleOrderProductPackagingService = saleOrderProductPackagingService;
  }

  @Override
  public String estimatePackaging(SaleOrder saleOrder) throws AxelorException {
    List<SaleOrderLine> saleOrderLines = saleOrder.getSaleOrderLineList();
    Map<Product, BigDecimal> productQtyMap =
        saleOrderLines.stream()
            .filter(line -> line.getQty().compareTo(BigDecimal.ZERO) > 0)
            .collect(
                Collectors.toMap(
                    SaleOrderLine::getProduct, SaleOrderLine::getQty, BigDecimal::add));

    saleOrderProductPackagingService.checkMultipleQty(productQtyMap);

    List<String> messages = new ArrayList<>();
    List<Product> packagingOptions =
        productRepository.all().filter("self.isPackaging = true").fetch();

    Map<Integer, Map<Product, String>> levelDescriptions = new HashMap<>();
    Map<Integer, Map<Product, BigDecimal[]>> levelWeights = new HashMap<>();

    packByLevel(
        ProductRepository.PACKAGING_LEVEL_BOX,
        packagingOptions,
        productQtyMap,
        messages,
        levelDescriptions,
        levelWeights);

    return saleOrderPackagingMessageService.formatPackagingMessage(
        saleOrder.getFullName(), messages);
  }

  protected void packByLevel(
      int level,
      List<Product> packagingOptions,
      Map<Product, BigDecimal> productQtyMap,
      List<String> messages,
      Map<Integer, Map<Product, String>> levelDescriptions,
      Map<Integer, Map<Product, BigDecimal[]>> levelWeights)
      throws AxelorException {

    List<Product> currentLevelBoxes =
        packagingOptions.stream()
            .filter(p -> p.getPackagingLevelSelect() == level)
            .sorted(Comparator.comparing(saleOrderPackagingDimensionService::getBoxInnerVolume))
            .collect(Collectors.toList());

    if (CollectionUtils.isEmpty(currentLevelBoxes)) {
      return;
    }
    List<Product> products =
        saleOrderPackagingDimensionService.getProductsOrderedByVolume(productQtyMap);
    List<Product> packedThisLevel = new ArrayList<>();

    Map<Product, String> descMap = levelDescriptions.computeIfAbsent(level, l -> new HashMap<>());
    Map<Product, BigDecimal[]> weightMap =
        levelWeights.computeIfAbsent(level, l -> new HashMap<>());

    if (level == ProductRepository.PACKAGING_LEVEL_BOX) {
      List<Product> productsWithPackaging = new ArrayList<>();
      List<Product> productsWithoutPackaging = new ArrayList<>();

      for (Product product : products) {
        if (CollectionUtils.isNotEmpty(
            saleOrderProductPackagingService.getProductPackagings(product))) {
          productsWithPackaging.add(product);
        } else {
          productsWithoutPackaging.add(product);
        }
      }
      saleOrderProductPackagingService.packWithProductPackaging(
          productQtyMap, messages, productsWithPackaging, packedThisLevel, descMap, weightMap);

      processPackaging(
          productQtyMap,
          messages,
          currentLevelBoxes,
          productsWithoutPackaging,
          packedThisLevel,
          descMap,
          weightMap);
    } else {
      processPackaging(
          productQtyMap,
          messages,
          currentLevelBoxes,
          products,
          packedThisLevel,
          descMap,
          weightMap);
    }

    int nextLevel = level + 1;
    if (nextLevel <= ProductRepository.PACKAGING_LEVEL_CONTAINER
        && !CollectionUtils.isEmpty(packedThisLevel)) {
      Map<Product, BigDecimal> nextLevelMap = new HashMap<>();
      for (Product box : packedThisLevel) {
        nextLevelMap.merge(box, BigDecimal.ONE, BigDecimal::add);
      }
      packByLevel(
          nextLevel, packagingOptions, nextLevelMap, messages, levelDescriptions, levelWeights);
    }
  }

  protected void processPackaging(
      Map<Product, BigDecimal> productQtyMap,
      List<String> messages,
      List<Product> currentLevelBoxes,
      List<Product> products,
      List<Product> packedThisLevel,
      Map<Product, String> descMap,
      Map<Product, BigDecimal[]> weightMap)
      throws AxelorException {

    final int MAX_ITERATION = 1000;
    int counter = 0;

    while (saleOrderPackagingPlanService.hasQtyRemaining(productQtyMap)) {
      if (++counter > MAX_ITERATION) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SaleExceptionMessage.PACKAGING_TOO_MANY_ITERATIONS));
      }
      Product nextProduct =
          products.stream()
              .filter(p -> productQtyMap.get(p).compareTo(BigDecimal.ZERO) > 0)
              .findFirst()
              .orElse(null);
      if (nextProduct == null) {
        break;
      }
      Map<Product, BigDecimal> boxContents = new HashMap<>();
      Product selectedBox =
          saleOrderPackagingPlanService.chooseBestBox(
              nextProduct, currentLevelBoxes, products, productQtyMap, boxContents);
      if (selectedBox == null) {
        break;
      }
      saleOrderPackagingMessageService.updatePackagingMessage(
          selectedBox, boxContents, productQtyMap, messages, descMap, weightMap);
      packedThisLevel.add(selectedBox);
    }
  }
}
