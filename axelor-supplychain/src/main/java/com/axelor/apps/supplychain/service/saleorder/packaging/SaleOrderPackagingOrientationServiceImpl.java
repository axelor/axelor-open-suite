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
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.StringHtmlListBuilder;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderPackagingOrientationServiceImpl
    implements SaleOrderPackagingOrientationService {

  protected SaleOrderPackagingDimensionService saleOrderPackagingDimensionService;

  @Inject
  public SaleOrderPackagingOrientationServiceImpl(
      SaleOrderPackagingDimensionService saleOrderPackagingDimensionService) {
    this.saleOrderPackagingDimensionService = saleOrderPackagingDimensionService;
  }

  @Override
  public boolean canFit(Product product, Product box) throws AxelorException {
    BigDecimal[] productDims = saleOrderPackagingDimensionService.getDimensions(product, false);
    BigDecimal[] boxDims = saleOrderPackagingDimensionService.getDimensions(box, true);

    for (BigDecimal[] orientation : getOrientations(productDims)) {
      if (orientation[0].compareTo(boxDims[0]) <= 0
          && orientation[1].compareTo(boxDims[1]) <= 0
          && orientation[2].compareTo(boxDims[2]) <= 0) {
        return true;
      }
    }
    return false;
  }

  @Override
  public BigDecimal[][] getOrientations(BigDecimal[] dimensions) {
    return new BigDecimal[][] {
      {dimensions[0], dimensions[1], dimensions[2]},
      {dimensions[0], dimensions[2], dimensions[1]},
      {dimensions[1], dimensions[0], dimensions[2]},
      {dimensions[1], dimensions[2], dimensions[0]},
      {dimensions[2], dimensions[0], dimensions[1]},
      {dimensions[2], dimensions[1], dimensions[0]}
    };
  }

  @Override
  public void validateProductsForPackaging(Set<Product> products, List<Product> packagings)
      throws AxelorException {
    Product biggestBox =
        packagings.stream()
            .filter(p -> p.getPackagingLevelSelect() == ProductRepository.PACKAGING_LEVEL_BOX)
            .max(
                (b1, b2) ->
                    saleOrderPackagingDimensionService
                        .getBoxInnerVolume(b1)
                        .compareTo(saleOrderPackagingDimensionService.getBoxInnerVolume(b2)))
            .orElse(null);

    if (biggestBox == null) {
      return;
    }
    List<String> oversizedProducts = new ArrayList<>();
    List<String> overweightProducts = new ArrayList<>();

    for (Product product : products) {
      if (!canFit(product, biggestBox)) {
        oversizedProducts.add(I18n.get(product.getName()));
      }
      if (isOverweight(product, biggestBox)) {
        overweightProducts.add(I18n.get(product.getName()));
      }
    }
    if (CollectionUtils.isNotEmpty(oversizedProducts)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          StringHtmlListBuilder.formatMessage(
              I18n.get(SaleExceptionMessage.SALE_ORDER_OVERSIZED_ITEMS), oversizedProducts));
    }
    if (CollectionUtils.isNotEmpty(overweightProducts)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          StringHtmlListBuilder.formatMessage(
              I18n.get(SaleExceptionMessage.SALE_ORDER_OVERWEIGHT_ITEMS), overweightProducts));
    }
  }

  protected boolean isOverweight(Product product, Product box) throws AxelorException {
    BigDecimal productWeight =
        saleOrderPackagingDimensionService.getConvertedWeight(product.getGrossMass(), product);
    return productWeight.compareTo(box.getMaxWeight()) > 0;
  }
}
