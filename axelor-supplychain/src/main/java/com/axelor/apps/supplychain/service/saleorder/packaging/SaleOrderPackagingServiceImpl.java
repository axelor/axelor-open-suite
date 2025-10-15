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
package com.axelor.apps.supplychain.service.saleorder.packaging;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.db.repo.PackagingRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

public class SaleOrderPackagingServiceImpl implements SaleOrderPackagingService {

  protected ProductRepository productRepository;
  protected SaleOrderPackagingPlanService saleOrderPackagingPlanService;
  protected SaleOrderPackagingDimensionService saleOrderPackagingDimensionService;
  protected SaleOrderPackagingCreateService saleOrderPackagingCreateService;
  protected SaleOrderProductPackagingService saleOrderProductPackagingService;
  protected SaleOrderPackagingOrientationService saleOrderPackagingOrientationService;
  protected PackagingRepository packagingRepository;

  @Inject
  public SaleOrderPackagingServiceImpl(
      ProductRepository productRepository,
      SaleOrderPackagingPlanService saleOrderPackagingPlanService,
      SaleOrderPackagingDimensionService saleOrderPackagingDimensionService,
      SaleOrderPackagingCreateService saleOrderPackagingCreateService,
      SaleOrderProductPackagingService saleOrderProductPackagingService,
      SaleOrderPackagingOrientationService saleOrderPackagingOrientationService,
      PackagingRepository packagingRepository) {
    this.productRepository = productRepository;
    this.saleOrderPackagingPlanService = saleOrderPackagingPlanService;
    this.saleOrderPackagingDimensionService = saleOrderPackagingDimensionService;
    this.saleOrderPackagingCreateService = saleOrderPackagingCreateService;
    this.saleOrderProductPackagingService = saleOrderProductPackagingService;
    this.saleOrderPackagingOrientationService = saleOrderPackagingOrientationService;
    this.packagingRepository = packagingRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void estimatePackaging(SaleOrder saleOrder) throws AxelorException {
    List<SaleOrderLine> saleOrderLines = saleOrder.getSaleOrderLineList();
    Map<Product, Pair<SaleOrderLine, BigDecimal>> productQtyMap =
        saleOrderLines.stream()
            .filter(
                line ->
                    line.getProduct()
                            .getProductTypeSelect()
                            .equals(ProductRepository.PRODUCT_TYPE_STORABLE)
                        && line.getProduct().getDtype().equals("Product")
                        && line.getQty().compareTo(BigDecimal.ZERO) > 0)
            .collect(
                Collectors.toMap(SaleOrderLine::getProduct, line -> Pair.of(line, line.getQty())));

    saleOrderProductPackagingService.checkMultipleQty(productQtyMap);

    List<Product> packagingOptions =
        productRepository.all().filter("self.isPackaging = true").fetch();

    saleOrderPackagingOrientationService.validateProductsForPackaging(
        productQtyMap.keySet(), packagingOptions);

    removePackagings(saleOrder);

    packByLevel(ProductRepository.PACKAGING_LEVEL_BOX, packagingOptions, productQtyMap, saleOrder);
  }

  protected void packByLevel(
      int level,
      List<Product> packagingOptions,
      Map<Product, Pair<SaleOrderLine, BigDecimal>> productQtyMap,
      SaleOrder saleOrder)
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
        saleOrderPackagingDimensionService.getProductsOrderedByVolume(productQtyMap.keySet());
    List<Product> packedThisLevel = new ArrayList<>();

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
          productQtyMap, productsWithPackaging, packedThisLevel, saleOrder);

      processPackaging(
          productQtyMap, currentLevelBoxes, productsWithoutPackaging, packedThisLevel, saleOrder);
    } else {
      processPackaging(productQtyMap, currentLevelBoxes, products, packedThisLevel, saleOrder);
    }

    int nextLevel = level + 1;
    if (nextLevel <= ProductRepository.PACKAGING_LEVEL_CONTAINER
        && !CollectionUtils.isEmpty(packedThisLevel)) {
      Map<Product, Pair<SaleOrderLine, BigDecimal>> nextLevelMap = new HashMap<>();
      for (Product box : packedThisLevel) {
        nextLevelMap.put(box, Pair.of(null, BigDecimal.ONE));
      }
      packByLevel(nextLevel, packagingOptions, nextLevelMap, saleOrder);
    }
  }

  protected void processPackaging(
      Map<Product, Pair<SaleOrderLine, BigDecimal>> productQtyMap,
      List<Product> currentLevelBoxes,
      List<Product> products,
      List<Product> packedThisLevel,
      SaleOrder saleOrder)
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
              .filter(p -> productQtyMap.get(p).getRight().compareTo(BigDecimal.ZERO) > 0)
              .findFirst()
              .orElse(null);
      if (nextProduct == null) {
        break;
      }
      Map<Product, Pair<SaleOrderLine, BigDecimal>> boxContents = new HashMap<>();
      Product selectedBox =
          saleOrderPackagingPlanService.chooseBestBox(
              nextProduct, currentLevelBoxes, products, productQtyMap, boxContents);
      if (selectedBox == null) {
        break;
      }
      saleOrderPackagingCreateService.createPackaging(
          selectedBox, boxContents, productQtyMap, saleOrder);
      packedThisLevel.add(selectedBox);
    }
  }

  protected void removePackagings(SaleOrder saleOrder) {
    List<Packaging> packagingList =
        packagingRepository
            .all()
            .filter("self.saleOrder = :saleOrder")
            .bind("saleOrder", saleOrder)
            .fetch();
    if (CollectionUtils.isEmpty(packagingList)) {
      return;
    }
    for (Packaging packaging : packagingList) {
      Packaging pack = packagingRepository.find(packaging.getId());
      if (pack != null) {
        if (CollectionUtils.isNotEmpty(pack.getPackagingLineList())) {
          pack.getPackagingLineList().clear();
        }
        packagingRepository.remove(pack);
      }
    }
  }
}
