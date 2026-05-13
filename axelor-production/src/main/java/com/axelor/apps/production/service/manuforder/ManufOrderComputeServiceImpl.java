/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.supplychain.service.ProductStockLocationService;
import com.google.inject.servlet.RequestScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

@RequestScoped
public class ManufOrderComputeServiceImpl implements ManufOrderComputeService {

  protected ProductStockLocationService productStockLocationService;
  protected UnitConversionService unitConversionService;
  protected AppBaseService appBaseService;

  @Inject
  public ManufOrderComputeServiceImpl(
      ProductStockLocationService productStockLocationService,
      UnitConversionService unitConversionService,
      AppBaseService appBaseService) {
    this.productStockLocationService = productStockLocationService;
    this.unitConversionService = unitConversionService;
    this.appBaseService = appBaseService;
  }

  @Override
  public BigDecimal computeProducibleQty(ManufOrder manufOrder) throws AxelorException {
    Company company = manufOrder.getCompany();
    BillOfMaterial billOfMaterial = manufOrder.getBillOfMaterial();

    if (company == null
        || billOfMaterial == null
        || billOfMaterial.getQty().compareTo(BigDecimal.ZERO) <= 0
        || CollectionUtils.isEmpty(billOfMaterial.getBillOfMaterialLineList())) {
      return BigDecimal.ZERO;
    }

    BigDecimal producibleQty = null;
    BigDecimal bomQty = billOfMaterial.getQty();

    for (BillOfMaterialLine billOfMaterialLine : billOfMaterial.getBillOfMaterialLineList()) {
      if (billOfMaterialLine.getHasNoManageStock()) {
        continue;
      }
      Product product = billOfMaterialLine.getProduct();
      BigDecimal availableQty = productStockLocationService.getAvailableQty(product, company, null);
      BigDecimal qtyNeeded = billOfMaterialLine.getQty();
      Unit bomLineUnit = billOfMaterialLine.getUnit();
      Unit productUnit = product.getUnit();
      if (productUnit != null && bomLineUnit != null && !bomLineUnit.equals(productUnit)) {
        availableQty =
            unitConversionService.convert(
                productUnit,
                bomLineUnit,
                availableQty,
                appBaseService.getNbDecimalDigitForQty(),
                product);
      }
      if (availableQty.compareTo(BigDecimal.ZERO) >= 0
          && qtyNeeded.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal qtyToUse = availableQty.divideToIntegralValue(qtyNeeded);
        producibleQty = producibleQty == null ? qtyToUse : producibleQty.min(qtyToUse);
      }
    }

    producibleQty =
        producibleQty == null
            ? BigDecimal.ZERO
            : producibleQty
                .multiply(bomQty)
                .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP);
    return producibleQty;
  }

  @Override
  public Map<Product, BigDecimal> getMissingComponents(ManufOrder manufOrder)
      throws AxelorException {
    Map<Product, BigDecimal> missingProductsMap = new HashMap<>();
    Company company = manufOrder.getCompany();
    BillOfMaterial billOfMaterial = manufOrder.getBillOfMaterial();

    if (company == null
        || billOfMaterial == null
        || billOfMaterial.getQty().compareTo(BigDecimal.ZERO) <= 0
        || CollectionUtils.isEmpty(billOfMaterial.getBillOfMaterialLineList())) {
      return missingProductsMap;
    }

    BigDecimal bomQty = billOfMaterial.getQty();
    BigDecimal qty = manufOrder.getQty();

    Map<Product, Pair<BigDecimal, Unit>> bomLineMap =
        billOfMaterial.getBillOfMaterialLineList().stream()
            .filter(bomLine -> !bomLine.getHasNoManageStock())
            .collect(
                Collectors.toMap(
                    BillOfMaterialLine::getProduct,
                    line -> Pair.of(line.getQty(), line.getUnit()),
                    (x, y) -> Pair.of(x.getLeft().add(y.getLeft()), x.getRight())));

    for (Entry<Product, Pair<BigDecimal, Unit>> billOfMaterialLine : bomLineMap.entrySet()) {
      Product product = billOfMaterialLine.getKey();
      BigDecimal bomLineQty = billOfMaterialLine.getValue().getLeft();
      Unit bomLineUnit = billOfMaterialLine.getValue().getRight();
      BigDecimal availableQty = productStockLocationService.getAvailableQty(product, company, null);
      availableQty =
          unitConversionService.convert(
              product.getUnit(), bomLineUnit, availableQty, availableQty.scale(), product);
      BigDecimal qtyNeeded =
          qty.multiply(bomLineQty)
              .divide(bomQty, appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP);

      if (availableQty.compareTo(BigDecimal.ZERO) >= 0
          && qtyNeeded.compareTo(BigDecimal.ZERO) > 0
          && qtyNeeded.compareTo(availableQty) > 0) {
        BigDecimal missingQty =
            qtyNeeded
                .subtract(availableQty)
                .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP);
        missingProductsMap.put(product, missingQty);
      }
    }
    return missingProductsMap;
  }

  @Override
  public boolean areLinesOutsourced(ManufOrder manufOrder) {
    List<OperationOrder> operationOrderList = manufOrder.getOperationOrderList();
    if (manufOrder.getOutsourcing() || CollectionUtils.isEmpty(operationOrderList)) {
      return false;
    }
    return operationOrderList.stream().anyMatch(OperationOrder::getOutsourcing);
  }
}
