/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.service.productionorder;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.ProductionOrderRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductionOrderSaleOrderServiceImpl implements ProductionOrderSaleOrderService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected UnitConversionService unitConversionService;
  protected ProductionOrderService productionOrderService;
  protected ProductionOrderRepository productionOrderRepo;
  protected AppProductionService appProductionService;

  @Inject
  public ProductionOrderSaleOrderServiceImpl(
      UnitConversionService unitConversionService,
      ProductionOrderService productionOrderService,
      ProductionOrderRepository productionOrderRepo,
      AppProductionService appProductionService) {

    this.unitConversionService = unitConversionService;
    this.productionOrderService = productionOrderService;
    this.productionOrderRepo = productionOrderRepo;
    this.appProductionService = appProductionService;
  }

  @Override
  public List<Long> generateProductionOrder(SaleOrder saleOrder) throws AxelorException {

    boolean oneProdOrderPerSO = appProductionService.getAppProduction().getOneProdOrderPerSO();

    List<Long> productionOrderIdList = new ArrayList<>();
    if (saleOrder.getSaleOrderLineList() == null) {
      return productionOrderIdList;
    }

    ProductionOrder productionOrder = null;
    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {

      if (productionOrder == null || !oneProdOrderPerSO) {
        productionOrder = this.createProductionOrder(saleOrder);
      }

      productionOrder = this.generateManufOrder(productionOrder, saleOrderLine);

      if (productionOrder != null && !productionOrderIdList.contains(productionOrder.getId())) {
        productionOrderIdList.add(productionOrder.getId());
      }
    }

    return productionOrderIdList;
  }

  protected ProductionOrder createProductionOrder(SaleOrder saleOrder) throws AxelorException {

    return productionOrderService.createProductionOrder(saleOrder);
  }

  @Override
  public ProductionOrder generateManufOrder(
      ProductionOrder productionOrder, SaleOrderLine saleOrderLine) throws AxelorException {

    Product product = saleOrderLine.getProduct();

    if (saleOrderLine.getSaleSupplySelect() == ProductRepository.SALE_SUPPLY_PRODUCE
        && product != null
        && product.getProductTypeSelect().equals(ProductRepository.PRODUCT_TYPE_STORABLE)) {

      BillOfMaterial billOfMaterial = saleOrderLine.getBillOfMaterial();

      if (billOfMaterial == null) {
        billOfMaterial = product.getDefaultBillOfMaterial();
      }

      if (billOfMaterial == null && product.getParentProduct() != null) {
        billOfMaterial = product.getParentProduct().getDefaultBillOfMaterial();
      }

      if (billOfMaterial == null) {
        throw new AxelorException(
            saleOrderLine,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.PRODUCTION_ORDER_SALES_ORDER_NO_BOM),
            product.getName(),
            product.getCode());
      }

      if (billOfMaterial.getProdProcess() == null) {
        return null;
      }

      Unit unit = saleOrderLine.getProduct().getUnit();
      BigDecimal qty = saleOrderLine.getQty();
      if (unit != null && !unit.equals(saleOrderLine.getUnit())) {
        qty =
            unitConversionService.convert(
                saleOrderLine.getUnit(), unit, qty, qty.scale(), saleOrderLine.getProduct());
      }

      return productionOrderService.addManufOrder(
          productionOrder,
          product,
          billOfMaterial,
          qty,
          LocalDateTime.now(),
          saleOrderLine.getSaleOrder(),
          ManufOrderService.ORIGIN_TYPE_SALE_ORDER);
    }

    return null;
  }
}
