/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
import java.util.stream.Collectors;
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

      productionOrder = this.generateManufOrders(productionOrder, saleOrderLine);

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
  public ProductionOrder generateManufOrders(
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

      return generateManufOrders(
          productionOrder, billOfMaterial, qty, LocalDateTime.now(), saleOrderLine.getSaleOrder());
    }

    return null;
  }

  /**
   * Loop through bill of materials components to generate manufacturing order for given sale order
   * line and all of its sub manuf order needed to get components for parent manufacturing order.
   *
   * @param productionOrder Initialized production order with no manufacturing order.
   * @param billOfMaterial the bill of material of the parent manufacturing order
   * @param qtyRequested the quantity requested of the parent manufacturing order.
   * @param startDate startDate of creation
   * @param saleOrder a sale order
   * @return the updated production order with all generated manufacturing orders.
   * @throws AxelorException
   */
  protected ProductionOrder generateManufOrders(
      ProductionOrder productionOrder,
      BillOfMaterial billOfMaterial,
      BigDecimal qtyRequested,
      LocalDateTime startDate,
      SaleOrder saleOrder)
      throws AxelorException {

    List<BillOfMaterial> childBomList = new ArrayList<>();
    childBomList.add(billOfMaterial);
    // prevent infinite loop
    int depth = 0;
    while (!childBomList.isEmpty()) {
      if (depth >= 100) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.CHILD_BOM_TOO_MANY_ITERATION));
      }
      List<BillOfMaterial> tempChildBomList = new ArrayList<>();
      for (BillOfMaterial childBom : childBomList) {
        productionOrder =
            productionOrderService.addManufOrder(
                productionOrder,
                childBom.getProduct(),
                childBom,
                qtyRequested.multiply(childBom.getQty()),
                startDate,
                null,
                saleOrder,
                ManufOrderService.ORIGIN_TYPE_SALE_ORDER);
        tempChildBomList.addAll(
            childBom.getBillOfMaterialSet().stream()
                .filter(BillOfMaterial::getDefineSubBillOfMaterial)
                .collect(Collectors.toList()));
      }
      childBomList.clear();
      childBomList.addAll(tempChildBomList);
      tempChildBomList.clear();
      depth++;
    }
    return productionOrder;
  }
}
