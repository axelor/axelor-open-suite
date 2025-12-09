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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductPackaging;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.UnitRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import jakarta.inject.Inject;
import java.math.BigDecimal;

public class ProductPackagingServiceImpl implements ProductPackagingService {

  protected static final String UNIT_KG = "kg";

  protected final UnitConversionService unitConversionService;
  protected final UnitRepository unitRepository;

  @Inject
  public ProductPackagingServiceImpl(
      UnitConversionService unitConversionService, UnitRepository unitRepository) {
    this.unitConversionService = unitConversionService;
    this.unitRepository = unitRepository;
  }

  @Override
  public BigDecimal computePackagingMaxTotalWeight(ProductPackaging productPackaging)
      throws AxelorException {
    BigDecimal packagingWeight = getConvertedGrossMass(productPackaging.getPackaging());
    BigDecimal productTotalWeight = getProductTotalWeight(productPackaging);
    return packagingWeight.add(productTotalWeight);
  }

  @Override
  public boolean isTotalWeightNotValid(ProductPackaging productPackaging) throws AxelorException {
    Product packaging = productPackaging.getPackaging();
    if (packaging == null) {
      return false;
    }
    BigDecimal productTotalWeight = getProductTotalWeight(productPackaging);
    return productTotalWeight.compareTo(packaging.getMaxWeight()) > 0;
  }

  protected BigDecimal getConvertedGrossMass(Product product) throws AxelorException {
    if (product == null) {
      return BigDecimal.ZERO;
    }
    BigDecimal grossMass = product.getGrossMass();
    Unit unit = product.getMassUnit();
    Unit targetUnit = getKilogramUnit();
    if (unit != null && targetUnit != null && !unit.equals(targetUnit)) {
      return unitConversionService.convert(
          unit, targetUnit, grossMass, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, product);
    }
    return grossMass;
  }

  protected Unit getKilogramUnit() {
    return unitRepository
        .all()
        .filter("self.labelToPrinting = :labelToPrinting")
        .bind("labelToPrinting", UNIT_KG)
        .fetchOne();
  }

  protected BigDecimal getProductTotalWeight(ProductPackaging productPackaging)
      throws AxelorException {
    return getConvertedGrossMass(productPackaging.getProductToPackage())
        .multiply(BigDecimal.valueOf(productPackaging.getQty()));
  }
}
