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
import com.google.inject.Inject;
import java.math.BigDecimal;

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
}
