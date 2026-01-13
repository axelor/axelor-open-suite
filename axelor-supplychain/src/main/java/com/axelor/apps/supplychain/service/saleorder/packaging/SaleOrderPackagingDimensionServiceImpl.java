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
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.UnitRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SaleOrderPackagingDimensionServiceImpl implements SaleOrderPackagingDimensionService {

  protected static final BigDecimal THICKNESS = BigDecimal.valueOf(5); // (in mm)
  protected static final String UNIT_MILLIMETER = "mm";
  protected static final String UNIT_KG = "kg";

  protected UnitConversionService unitConversionService;
  protected UnitRepository unitRepository;

  @Inject
  public SaleOrderPackagingDimensionServiceImpl(
      UnitConversionService unitConversionService, UnitRepository unitRepository) {
    this.unitConversionService = unitConversionService;
    this.unitRepository = unitRepository;
  }

  @Override
  public List<Product> getProductsOrderedByVolume(Set<Product> products) throws AxelorException {
    Map<Product, BigDecimal> map = new HashMap<>();
    for (Product product : products) {
      BigDecimal volume =
          getConvertedDimension(product.getLength(), product)
              .multiply(getConvertedDimension(product.getWidth(), product))
              .multiply(getConvertedDimension(product.getHeight(), product));
      map.put(product, volume);
    }
    return products.stream()
        .sorted((p1, p2) -> map.get(p2).compareTo(map.get(p1)))
        .collect(Collectors.toList());
  }

  @Override
  public BigDecimal getSpaceVolume(BigDecimal[] space) {
    return space[0].multiply(space[1]).multiply(space[2]);
  }

  @Override
  public BigDecimal getBoxInnerVolume(Product box) {
    return box.getInnerLength().multiply(box.getInnerWidth()).multiply(box.getInnerHeight());
  }

  @Override
  public BigDecimal[] getDimensions(Product product, boolean isBox) throws AxelorException {
    if (isBox) {
      return new BigDecimal[] {
        product.getInnerLength(), product.getInnerWidth(), product.getInnerHeight()
      };
    } else {
      return getProductDimensions(product);
    }
  }

  @Override
  public BigDecimal[] getProductDimensions(Product product) throws AxelorException {
    return new BigDecimal[] {
      getEffectiveDimension(product.getLength(), product),
      getEffectiveDimension(product.getWidth(), product),
      getEffectiveDimension(product.getHeight(), product)
    };
  }

  protected BigDecimal getEffectiveDimension(BigDecimal value, Product product)
      throws AxelorException {
    return getConvertedDimension(value, product).add(THICKNESS.multiply(BigDecimal.valueOf(2)));
  }

  protected BigDecimal getConvertedDimension(BigDecimal value, Product product)
      throws AxelorException {
    Unit unit = product.getLengthUnit();
    Unit targetUnit = getMillimeterUnit();
    if (unit != null && !unit.equals(targetUnit)) {
      value =
          unitConversionService.convert(
              unit, targetUnit, value, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, product);
    }
    return value;
  }

  protected Unit getMillimeterUnit() {
    return unitRepository
        .all()
        .filter("self.labelToPrinting = :labelToPrinting")
        .bind("labelToPrinting", UNIT_MILLIMETER)
        .fetchOne();
  }

  @Override
  public BigDecimal getConvertedWeight(BigDecimal value, Product product) throws AxelorException {
    Unit unit = product.getMassUnit();
    Unit targetUnit = getKilogramUnit();
    if (unit != null && !unit.equals(targetUnit)) {
      value =
          unitConversionService.convertWithAutoFlushFalse(
              unit, targetUnit, value, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, product);
    }
    return value;
  }

  protected Unit getKilogramUnit() {
    return unitRepository
        .all()
        .autoFlush(false)
        .filter("self.labelToPrinting = :labelToPrinting")
        .bind("labelToPrinting", UNIT_KG)
        .fetchOne();
  }
}
