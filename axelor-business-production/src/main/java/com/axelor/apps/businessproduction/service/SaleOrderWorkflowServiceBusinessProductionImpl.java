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
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.apps.production.service.SaleOrderWorkflowServiceProductionImpl;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.productionorder.ProductionOrderSaleOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainService;
import com.axelor.apps.supplychain.service.PartnerSupplychainService;
import com.axelor.apps.supplychain.service.SaleOrderPurchaseService;
import com.axelor.apps.supplychain.service.SaleOrderStockService;
import com.axelor.apps.supplychain.service.analytic.AnalyticToolSupplychainService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderWorkflowServiceBusinessProductionImpl
    extends SaleOrderWorkflowServiceProductionImpl {

  protected final AnalyticMoveLineRepository analyticMoveLineRepository;

  @Inject
  public SaleOrderWorkflowServiceBusinessProductionImpl(
      SequenceService sequenceService,
      PartnerRepository partnerRepo,
      SaleOrderRepository saleOrderRepo,
      AppSaleService appSaleService,
      AppCrmService appCrmService,
      UserService userService,
      SaleOrderLineService saleOrderLineService,
      SaleOrderStockService saleOrderStockService,
      SaleOrderPurchaseService saleOrderPurchaseService,
      AppSupplychainService appSupplychainService,
      AccountingSituationSupplychainService accountingSituationSupplychainService,
      PartnerSupplychainService partnerSupplychainService,
      SaleConfigService saleConfigService,
      AnalyticToolSupplychainService analyticToolSupplychainService,
      ProductionOrderSaleOrderService productionOrderSaleOrderService,
      AppProductionService appProductionService,
      AnalyticMoveLineRepository analyticMoveLineRepository) {
    super(
        sequenceService,
        partnerRepo,
        saleOrderRepo,
        appSaleService,
        appCrmService,
        userService,
        saleOrderLineService,
        saleOrderStockService,
        saleOrderPurchaseService,
        appSupplychainService,
        accountingSituationSupplychainService,
        partnerSupplychainService,
        saleConfigService,
        analyticToolSupplychainService,
        productionOrderSaleOrderService,
        appProductionService);
    this.analyticMoveLineRepository = analyticMoveLineRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void cancelSaleOrder(
      SaleOrder saleOrder, CancelReason cancelReason, String cancelReasonStr)
      throws AxelorException {
    super.cancelSaleOrder(saleOrder, cancelReason, cancelReasonStr);
    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      for (AnalyticMoveLine analyticMoveLine : saleOrderLine.getAnalyticMoveLineList()) {
        analyticMoveLine.setProject(null);
        analyticMoveLineRepository.save(analyticMoveLine);
      }
    }
  }
}
