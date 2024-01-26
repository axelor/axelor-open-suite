/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;

public class ProductConversionServiceImpl implements ProductConversionService {

  protected ProductCompanyService productCompanyService;
  protected UnitConversionService unitConversionService;

  @Inject
  public ProductConversionServiceImpl(
      ProductCompanyService productCompanyService, UnitConversionService unitConversionService) {

    this.productCompanyService = productCompanyService;
    this.unitConversionService = unitConversionService;
  }

  @Override
  public BigDecimal convertFromPurchaseToStockUnitPrice(
      Product product, BigDecimal lastPurchasePrice) throws AxelorException {

    Unit purchaseUnit =
        Optional.ofNullable(productCompanyService.get(product, "purchasesUnit", null))
            .map(o -> (Unit) o)
            .orElse(null);
    Unit stockUnit =
        Optional.ofNullable(productCompanyService.get(product, "unit", null))
            .map(o -> (Unit) o)
            .orElse(null);

    if (purchaseUnit == null || stockUnit == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(
              String.format(
                  BaseExceptionMessage.PRODUCT_MISSING_UNITS_TO_CONVERT, product.getName())));
    }

    return unitConversionService.convert(
        stockUnit, purchaseUnit, lastPurchasePrice, lastPurchasePrice.scale(), null);
  }
}
