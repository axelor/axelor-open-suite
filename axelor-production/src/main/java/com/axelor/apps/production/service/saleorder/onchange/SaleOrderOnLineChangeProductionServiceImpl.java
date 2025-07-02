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
package com.axelor.apps.production.service.saleorder.onchange;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.service.SaleOrderProductionSyncService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComplementaryProductService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderGlobalDiscountService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.pack.SaleOrderLinePackService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderShipmentService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderSupplychainService;
import com.axelor.apps.supplychain.service.saleorder.onchange.SaleOrderOnLineChangeSupplyChainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineAnalyticService;
import com.axelor.studio.db.repo.AppSaleRepository;
import com.google.inject.Inject;

public class SaleOrderOnLineChangeProductionServiceImpl
    extends SaleOrderOnLineChangeSupplyChainServiceImpl {

  protected final AppProductionService appProductionService;
  protected final SaleOrderProductionSyncService saleOrderProductionSyncService;

  @Inject
  public SaleOrderOnLineChangeProductionServiceImpl(
      AppSaleService appSaleService,
      SaleOrderService saleOrderService,
      SaleOrderMarginService saleOrderMarginService,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderLineRepository saleOrderLineRepository,
      SaleOrderLineComputeService saleOrderLineComputeService,
      SaleOrderLinePackService saleOrderLinePackService,
      SaleOrderComplementaryProductService saleOrderComplementaryProductService,
      SaleOrderSupplychainService saleOrderSupplychainService,
      SaleOrderShipmentService saleOrderShipmentService,
      SaleOrderGlobalDiscountService saleOrderGlobalDiscountService,
      SaleOrderLineAnalyticService saleOrderLineAnalyticService,
      AppProductionService appProductionService,
      SaleOrderProductionSyncService saleOrderProductionSyncService) {
    super(
        appSaleService,
        saleOrderService,
        saleOrderMarginService,
        saleOrderComputeService,
        saleOrderLineRepository,
        saleOrderLineComputeService,
        saleOrderLinePackService,
        saleOrderComplementaryProductService,
        saleOrderGlobalDiscountService,
        saleOrderSupplychainService,
        saleOrderShipmentService,
        saleOrderLineAnalyticService);
    this.appProductionService = appProductionService;
    this.saleOrderProductionSyncService = saleOrderProductionSyncService;
  }

  @Override
  public String onLineChange(SaleOrder saleOrder) throws AxelorException {
    String message = super.onLineChange(saleOrder);

    if (appProductionService.isApp("production")
        && appSaleService.getAppSale().getListDisplayTypeSelect()
            == AppSaleRepository.APP_SALE_LINE_DISPLAY_TYPE_MULTI
        && saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_DRAFT_QUOTATION) {
      saleOrderProductionSyncService.syncSaleOrderLineList(
          saleOrder, saleOrder.getSaleOrderLineList());
    }
    return message;
  }
}
