/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.productionorder;

import static com.axelor.apps.production.exceptions.ProductionExceptionMessage.YOUR_SCHEDULING_CONFIGURATION_IS_AT_THE_LATEST_YOU_NEED_TO_FILL_THE_ESTIMATED_SHIPPING_DATE;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.db.repo.ProductionOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.production.service.manuforder.ManufOrderService.ManufOrderOriginTypeProduction;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductionOrderSaleOrderServiceImpl implements ProductionOrderSaleOrderService {

  protected UnitConversionService unitConversionService;
  protected ProductionOrderService productionOrderService;
  protected ProductionOrderRepository productionOrderRepo;
  protected AppProductionService appProductionService;
  protected BillOfMaterialService billOfMaterialService;
  protected ProductionConfigService productionConfigService;

  @Inject
  public ProductionOrderSaleOrderServiceImpl(
      UnitConversionService unitConversionService,
      ProductionOrderService productionOrderService,
      ProductionOrderRepository productionOrderRepo,
      AppProductionService appProductionService,
      BillOfMaterialService billOfMaterialService,
      ProductionConfigService productionConfigService) {

    this.unitConversionService = unitConversionService;
    this.productionOrderService = productionOrderService;
    this.productionOrderRepo = productionOrderRepo;
    this.appProductionService = appProductionService;
    this.billOfMaterialService = billOfMaterialService;
    this.productionConfigService = productionConfigService;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class})
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
            I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_SALES_ORDER_NO_BOM),
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
          productionOrder,
          billOfMaterial,
          qty,
          LocalDateTime.now(),
          saleOrderLine.getSaleOrder(),
          saleOrderLine);
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
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine)
      throws AxelorException {

    List<BillOfMaterial> childBomList = new ArrayList<>();
    childBomList.add(billOfMaterial);

    Map<BillOfMaterial, ManufOrder> subBomManufOrderParentMap = new HashMap<>();
    // prevent infinite loop
    int depth = 0;
    while (!childBomList.isEmpty()) {
      if (depth >= 100) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(ProductionExceptionMessage.CHILD_BOM_TOO_MANY_ITERATION));
      }
      ProductionConfig productionConfig =
          productionConfigService.getProductionConfig(saleOrder.getCompany());

      LocalDateTime endDate = null;
      if (productionConfig.getScheduling()
          != ProductionConfigRepository.AS_SOON_AS_POSSIBLE_SCHEDULING) {
        if (saleOrderLine.getEstimatedShippingDate() == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(
                  YOUR_SCHEDULING_CONFIGURATION_IS_AT_THE_LATEST_YOU_NEED_TO_FILL_THE_ESTIMATED_SHIPPING_DATE),
              saleOrderLine.getSequence());
        }
        endDate = saleOrderLine.getEstimatedShippingDate().atStartOfDay();
        // Start date will be filled at plan
        startDate = null;
      }

      List<BillOfMaterial> tempChildBomList = new ArrayList<>();

      // Map for future manufOrder and its manufOrder Parent

      for (BillOfMaterial childBom : childBomList) {

        ManufOrder manufOrder =
            productionOrderService.generateManufOrder(
                childBom.getProduct(),
                childBom,
                qtyRequested.multiply(childBom.getQty()),
                startDate,
                endDate,
                saleOrder,
                saleOrderLine,
                ManufOrderOriginTypeProduction.ORIGIN_TYPE_SALE_ORDER,
                subBomManufOrderParentMap.get(childBom));

        productionOrderService.addManufOrder(productionOrder, manufOrder);

        List<BillOfMaterial> subBomList = billOfMaterialService.getSubBillOfMaterial(childBom);
        subBomList.forEach(
            bom -> {
              subBomManufOrderParentMap.putIfAbsent(bom, manufOrder);
            });

        tempChildBomList.addAll(subBomList);
      }
      childBomList.clear();
      childBomList.addAll(tempChildBomList);
      tempChildBomList.clear();
      depth++;
    }
    return productionOrder;
  }
}
