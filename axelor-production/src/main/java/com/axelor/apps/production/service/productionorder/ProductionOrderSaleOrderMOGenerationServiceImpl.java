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
package com.axelor.apps.production.service.productionorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.productionorder.manuforder.ManufOrderGenerationService;
import com.axelor.apps.production.service.productionorder.manuforder.SaleOrderLineMOGenerationService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductionOrderSaleOrderMOGenerationServiceImpl
    implements ProductionOrderSaleOrderMOGenerationService {

  protected UnitConversionService unitConversionService;
  protected ProductionConfigService productionConfigService;
  protected BillOfMaterialService billOfMaterialService;
  protected ManufOrderService manufOrderService;
  protected ProductionOrderUpdateService productionOrderUpdateService;
  protected AppBaseService appBaseService;
  protected final SaleOrderLineMOGenerationService saleOrderLineMOGenerationService;
  protected final ManufOrderGenerationService manufOrderGenerationService;

  @Inject
  public ProductionOrderSaleOrderMOGenerationServiceImpl(
      UnitConversionService unitConversionService,
      ProductionConfigService productionConfigService,
      BillOfMaterialService billOfMaterialService,
      ManufOrderService manufOrderService,
      ProductionOrderUpdateService productionOrderUpdateService,
      AppBaseService appBaseService,
      SaleOrderLineMOGenerationService saleOrderLineMOGenerationService,
      ManufOrderGenerationService manufOrderGenerationService) {
    this.unitConversionService = unitConversionService;
    this.productionConfigService = productionConfigService;
    this.billOfMaterialService = billOfMaterialService;
    this.manufOrderService = manufOrderService;
    this.productionOrderUpdateService = productionOrderUpdateService;
    this.appBaseService = appBaseService;
    this.saleOrderLineMOGenerationService = saleOrderLineMOGenerationService;
    this.manufOrderGenerationService = manufOrderGenerationService;
  }

  @Override
  public ProductionOrder generateManufOrders(
      ProductionOrder productionOrder,
      SaleOrderLine saleOrderLine,
      Product product,
      BigDecimal qtyToProduce)
      throws AxelorException {
    BillOfMaterial billOfMaterial = getBillOfMaterial(saleOrderLine, product);
    if (billOfMaterial.getProdProcess() == null) {
      return null;
    }

    BigDecimal qty = convertToProductUnit(product, saleOrderLine.getUnit(), qtyToProduce);

    return saleOrderLineMOGenerationService.generateManufOrders(
        saleOrderLine, saleOrderLine.getSaleOrder(), productionOrder, qty);
  }

  protected BigDecimal convertToProductUnit(Product product, Unit saleOrderLineUnit, BigDecimal qty)
      throws AxelorException {
    Unit productUnit = product.getUnit();
    if (productUnit != null && !productUnit.equals(saleOrderLineUnit)) {
      qty =
          unitConversionService.convert(saleOrderLineUnit, productUnit, qty, qty.scale(), product);
    }
    return qty;
  }

  protected BillOfMaterial getBillOfMaterial(SaleOrderLine saleOrderLine, Product product)
      throws AxelorException {
    BillOfMaterial billOfMaterial = saleOrderLine.getBillOfMaterial();

    if (billOfMaterial == null) {
      // May call billOfMaterialService here
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
    return billOfMaterial;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public ProductionOrder addManufOrder(
      ProductionOrder productionOrder,
      Product product,
      BillOfMaterial billOfMaterial,
      BigDecimal qtyRequested,
      LocalDateTime startDate,
      LocalDateTime endDate,
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      ManufOrderService.ManufOrderOriginType manufOrderOriginType)
      throws AxelorException {

    ManufOrder manufOrder =
        manufOrderGenerationService.generateManufOrder(
            product,
            billOfMaterial,
            qtyRequested,
            startDate,
            endDate,
            saleOrder,
            saleOrderLine,
            manufOrderOriginType,
            null);

    return productionOrderUpdateService.addManufOrder(productionOrder, manufOrder);
  }
}
