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
package com.axelor.apps.production.service.productionorder.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.repo.AppSaleRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class SaleOrderLineMOGenerationServiceImpl implements SaleOrderLineMOGenerationService {

  protected final BillOfMaterialService billOfMaterialService;
  protected final AppBaseService appBaseService;
  protected final AppSaleService appSaleService;
  protected final SaleOrderLineMOGenerationSingleLineService
      saleOrderLineMOGenerationSingleLineService;
  protected final SaleOrderLineMOGenerationMultiLineService
      saleOrderLineMOGenerationMultiLineService;

  @Inject
  public SaleOrderLineMOGenerationServiceImpl(
      BillOfMaterialService billOfMaterialService,
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      SaleOrderLineMOGenerationSingleLineService saleOrderLineMOGenerationSingleLineService,
      SaleOrderLineMOGenerationMultiLineService saleOrderLineMOGenerationMultiLineService) {
    this.billOfMaterialService = billOfMaterialService;
    this.appBaseService = appBaseService;
    this.appSaleService = appSaleService;
    this.saleOrderLineMOGenerationSingleLineService = saleOrderLineMOGenerationSingleLineService;
    this.saleOrderLineMOGenerationMultiLineService = saleOrderLineMOGenerationMultiLineService;
  }

  @Override
  public ProductionOrder generateManufOrders(
      SaleOrderLine saleOrderLine,
      SaleOrder saleOrder,
      ProductionOrder productionOrder,
      BigDecimal qtyRequested)
      throws AxelorException {

    Objects.requireNonNull(saleOrderLine);
    Objects.requireNonNull(saleOrder);
    Objects.requireNonNull(productionOrder);

    LocalDateTime startDateTime = appBaseService.getTodayDateTime().toLocalDateTime();

    switch (appSaleService.getAppSale().getListDisplayTypeSelect()) {
      case AppSaleRepository.APP_SALE_LINE_DISPLAY_TYPE_CLASSIC:
      case AppSaleRepository.APP_SALE_LINE_DISPLAY_TYPE_EDITABLE:
        // Generation through BillOfMaterial
        saleOrderLineMOGenerationSingleLineService.generateManufOrders(
            productionOrder,
            getBillOfMaterial(saleOrderLine, saleOrderLine.getProduct(), saleOrder.getCompany()),
            qtyRequested,
            startDateTime,
            saleOrder,
            saleOrderLine);
        break;
      case AppSaleRepository.APP_SALE_LINE_DISPLAY_TYPE_MULTI:
        // Generation through SaleOrderLines
        saleOrderLineMOGenerationMultiLineService.generateManufOrders(
            productionOrder, qtyRequested, startDateTime, saleOrder, saleOrderLine);
        break;
      default:
        throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, "");
    }

    return productionOrder;
  }

  protected BillOfMaterial getBillOfMaterial(
      SaleOrderLine saleOrderLine, Product product, Company company) throws AxelorException {
    BillOfMaterial billOfMaterial = saleOrderLine.getBillOfMaterial();

    if (billOfMaterial == null) {
      // May call billOfMaterialService here
      billOfMaterial = billOfMaterialService.getDefaultBOM(product, company);
    }

    if (billOfMaterial == null && product.getParentProduct() != null) {
      billOfMaterial = billOfMaterialService.getDefaultBOM(product.getParentProduct(), company);
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
}
