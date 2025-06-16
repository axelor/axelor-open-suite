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
package com.axelor.apps.supplychain.service.saleorder.status;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.BlockedSaleOrderException;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.saleorder.SaleOrderSequenceService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderPrintService;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderFinalizeServiceImpl;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainService;
import com.axelor.apps.supplychain.service.IntercoService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderFinalizeSupplychainServiceImpl extends SaleOrderFinalizeServiceImpl {

  protected AppSupplychainService appSupplychainService;
  protected AccountingSituationSupplychainService accountingSituationSupplychainService;

  @Inject
  public SaleOrderFinalizeSupplychainServiceImpl(
      SaleOrderRepository saleOrderRepository,
      SequenceService sequenceService,
      SaleOrderService saleOrderService,
      SaleOrderPrintService saleOrderPrintService,
      SaleConfigService saleConfigService,
      AppSaleService appSaleService,
      AppCrmService appCrmService,
      SaleOrderSequenceService saleOrderSequenceService,
      AppSupplychainService appSupplychainService,
      AccountingSituationSupplychainService accountingSituationSupplychainService) {
    super(
        saleOrderRepository,
        sequenceService,
        saleOrderService,
        saleOrderPrintService,
        saleConfigService,
        appSaleService,
        appCrmService,
        saleOrderSequenceService);
    this.appSupplychainService = appSupplychainService;
    this.accountingSituationSupplychainService = accountingSituationSupplychainService;
  }

  @Override
  @Transactional(
      rollbackOn = {AxelorException.class, RuntimeException.class},
      ignore = {BlockedSaleOrderException.class})
  public void finalizeQuotation(SaleOrder saleOrder) throws AxelorException {

    if (!appSupplychainService.isApp("supplychain")) {
      super.finalizeQuotation(saleOrder);
      return;
    }

    accountingSituationSupplychainService.checkExceededUsedCredit(saleOrder);
    super.finalizeQuotation(saleOrder);
    int intercoSaleCreatingStatus =
        appSupplychainService.getAppSupplychain().getIntercoSaleCreatingStatusSelect();
    if (saleOrder.getInterco()
        && intercoSaleCreatingStatus == SaleOrderRepository.STATUS_FINALIZED_QUOTATION) {
      Beans.get(IntercoService.class).generateIntercoPurchaseFromSale(saleOrder);
    }
    if (saleOrder.getCreatedByInterco()) {
      fillIntercompanyPurchaseOrderCounterpart(saleOrder);
    }
  }

  /**
   * Fill interco purchase order counterpart is the sale order exist.
   *
   * @param saleOrder
   */
  protected void fillIntercompanyPurchaseOrderCounterpart(SaleOrder saleOrder) {
    PurchaseOrder purchaseOrder =
        Beans.get(PurchaseOrderRepository.class)
            .all()
            .filter("self.purchaseOrderSeq = :purchaseOrderSeq")
            .bind("purchaseOrderSeq", saleOrder.getExternalReference())
            .fetchOne();
    if (purchaseOrder != null) {
      purchaseOrder.setExternalReference(saleOrder.getSaleOrderSeq());
    }
  }
}
