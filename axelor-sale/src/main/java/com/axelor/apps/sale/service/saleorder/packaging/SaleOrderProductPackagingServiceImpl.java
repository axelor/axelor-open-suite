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
import com.axelor.apps.base.db.ProductPackaging;
import com.axelor.apps.base.db.repo.ProductPackagingRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.StringHtmlListBuilder;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderProductPackagingServiceImpl implements SaleOrderProductPackagingService {

  protected SaleOrderPackagingMessageService saleOrderPackagingMessageService;
  protected ProductPackagingRepository productPackagingRepository;

  @Inject
  public SaleOrderProductPackagingServiceImpl(
      SaleOrderPackagingMessageService saleOrderPackagingMessageService,
      ProductPackagingRepository productPackagingRepository) {
    this.saleOrderPackagingMessageService = saleOrderPackagingMessageService;
    this.productPackagingRepository = productPackagingRepository;
  }

  @Override
  public void checkMultipleQty(Map<Product, BigDecimal> productQtyMap) throws AxelorException {
    List<String> invalidProducts = new ArrayList<>();

    for (Map.Entry<Product, BigDecimal> entry : productQtyMap.entrySet()) {
      Product product = entry.getKey();
      BigDecimal qty = entry.getValue();

      List<ProductPackaging> packagingList = getProductPackagings(product);

      if (CollectionUtils.isEmpty(packagingList)) {
        continue;
      }
      boolean isValidPackaging = false;

      for (ProductPackaging productPackaging : packagingList) {
        BigDecimal packagingQty = BigDecimal.valueOf(productPackaging.getQty());
        if (packagingQty.compareTo(BigDecimal.ZERO) > 0
            && qty.remainder(packagingQty).compareTo(BigDecimal.ZERO) == 0) {
          isValidPackaging = true;
          break;
        }
      }
      if (!isValidPackaging) {
        invalidProducts.add(I18n.get(product.getFullName()));
      }
    }
    if (CollectionUtils.isNotEmpty(invalidProducts)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          StringHtmlListBuilder.formatMessage(
              I18n.get(SaleExceptionMessage.SALE_ORDER_INVALID_PACKAGING_QTY), invalidProducts));
    }
  }

  @Override
  public List<ProductPackaging> getProductPackagings(Product product) {
    return productPackagingRepository
        .all()
        .filter("self.productToPackage = :product")
        .bind("product", product)
        .fetch();
  }

  @Override
  public void packWithProductPackaging(
      Map<Product, BigDecimal> productQtyMap,
      List<String> messages,
      List<Product> productsWithPackaging,
      List<Product> packedThisLevel,
      Map<Product, String> descMap,
      Map<Product, BigDecimal[]> weightMap) {

    for (Product nextProduct : productsWithPackaging) {
      while (productQtyMap.getOrDefault(nextProduct, BigDecimal.ZERO).signum() > 0) {
        Map<Product, BigDecimal> boxContents = new HashMap<>();
        Product selectedBox = chooseBestPackaging(nextProduct, productQtyMap, boxContents);
        if (selectedBox == null) {
          break;
        }
        saleOrderPackagingMessageService.updatePackagingMessage(
            selectedBox, boxContents, productQtyMap, messages, descMap, weightMap);

        packedThisLevel.add(selectedBox);
      }
    }
  }

  protected Product chooseBestPackaging(
      Product product,
      Map<Product, BigDecimal> productQtyMap,
      Map<Product, BigDecimal> boxContents) {

    Map<Product, BigDecimal> qtyMap = new HashMap<>(productQtyMap);
    List<ProductPackaging> productPackagings =
        getProductPackagings(product).stream()
            .sorted(Comparator.comparing(ProductPackaging::getQty))
            .collect(Collectors.toList());

    if (CollectionUtils.isEmpty(productPackagings)) {
      return null;
    }
    BigDecimal qtyLeft = qtyMap.getOrDefault(product, BigDecimal.ZERO);
    if (qtyLeft.compareTo(BigDecimal.ZERO) <= 0) {
      return null;
    }
    ProductPackaging bestPackaging = null;

    for (ProductPackaging productPackaging : productPackagings) {
      BigDecimal packagingQty = BigDecimal.valueOf(productPackaging.getQty());
      if (packagingQty.compareTo(BigDecimal.ZERO) <= 0 || qtyLeft.compareTo(packagingQty) < 0) {
        continue;
      }
      bestPackaging = productPackaging;
    }
    if (bestPackaging != null) {
      BigDecimal packagingQty = BigDecimal.valueOf(bestPackaging.getQty());
      qtyMap.put(product, qtyLeft.subtract(packagingQty));
      boxContents.merge(product, packagingQty, BigDecimal::add);
      return bestPackaging.getPackaging();
    }
    return null;
  }
}
