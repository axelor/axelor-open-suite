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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.crm.service.app.AppCrmService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.BlockedSaleOrderException;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderWorkflowServiceImpl;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.analytic.AnalyticToolSupplychainService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppSupplychain;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class SaleOrderWorkflowServiceSupplychainImpl extends SaleOrderWorkflowServiceImpl {

  protected SaleOrderStockService saleOrderStockService;
  protected SaleOrderPurchaseService saleOrderPurchaseService;
  protected AppSupplychainService appSupplychainService;
  protected AccountingSituationSupplychainService accountingSituationSupplychainService;
  protected PartnerSupplychainService partnerSupplychainService;
  protected SaleConfigService saleConfigService;
  protected AnalyticToolSupplychainService analyticToolSupplychainService;

  @Inject
  public SaleOrderWorkflowServiceSupplychainImpl(
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
      AnalyticToolSupplychainService analyticToolSupplychainService) {
    super(
        sequenceService,
        partnerRepo,
        saleOrderRepo,
        appSaleService,
        appCrmService,
        userService,
        saleOrderLineService);
    this.saleOrderStockService = saleOrderStockService;
    this.saleOrderPurchaseService = saleOrderPurchaseService;
    this.appSupplychainService = appSupplychainService;
    this.accountingSituationSupplychainService = accountingSituationSupplychainService;
    this.partnerSupplychainService = partnerSupplychainService;
    this.saleConfigService = saleConfigService;
    this.analyticToolSupplychainService = analyticToolSupplychainService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void confirmSaleOrder(SaleOrder saleOrder) throws AxelorException {

    if (!appSupplychainService.isApp("supplychain")) {
      super.confirmSaleOrder(saleOrder);
      return;
    }

    analyticToolSupplychainService.checkSaleOrderLinesAnalyticDistribution(saleOrder);

    if (partnerSupplychainService.isBlockedPartnerOrParent(saleOrder.getClientPartner())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.CUSTOMER_HAS_BLOCKED_ACCOUNT));
    }

    super.confirmSaleOrder(saleOrder);

    AppSupplychain appSupplychain = appSupplychainService.getAppSupplychain();

    if (appSupplychain.getPurchaseOrderGenerationAuto()) {
      saleOrderPurchaseService.createPurchaseOrders(saleOrder);
    }
    if (appSupplychain.getCustomerStockMoveGenerationAuto()) {
      saleOrderStockService.createStocksMovesFromSaleOrder(saleOrder);
    }
    int intercoSaleCreatingStatus = appSupplychain.getIntercoSaleCreatingStatusSelect();
    if (saleOrder.getInterco()
        && intercoSaleCreatingStatus == SaleOrderRepository.STATUS_ORDER_CONFIRMED) {
      Beans.get(IntercoService.class).generateIntercoPurchaseFromSale(saleOrder);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancelSaleOrder(
      SaleOrder saleOrder, CancelReason cancelReason, String cancelReasonStr)
      throws AxelorException {
    super.cancelSaleOrder(saleOrder, cancelReason, cancelReasonStr);

    if (!appSupplychainService.isApp("supplychain")) {
      return;
    }
    try {
      accountingSituationSupplychainService.updateUsedCredit(saleOrder.getClientPartner());
    } catch (Exception e) {
      e.printStackTrace();
    }
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

    accountingSituationSupplychainService.updateCustomerCreditFromSaleOrder(saleOrder);
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

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void completeSaleOrder(SaleOrder saleOrder) throws AxelorException {

    if (!appSupplychainService.isApp("supplychain")) {
      super.completeSaleOrder(saleOrder);
      return;
    }

    List<StockMove> stockMoves =
        Beans.get(StockMoveRepository.class)
            .all()
            .filter(
                "self.originId = ? AND self.originTypeSelect = ?",
                saleOrder.getId(),
                "com.axelor.apps.sale.db.SaleOrder")
            .fetch();
    if (!stockMoves.isEmpty()) {
      for (StockMove stockMove : stockMoves) {
        Integer statusSelect = stockMove.getStatusSelect();
        if (statusSelect == StockMoveRepository.STATUS_DRAFT
            || statusSelect == StockMoveRepository.STATUS_PLANNED) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(SupplychainExceptionMessage.SALE_ORDER_COMPLETE_MANUALLY));
        }
      }
    }
    super.completeSaleOrder(saleOrder);
    accountingSituationSupplychainService.updateUsedCredit(saleOrder.getClientPartner());
  }
}
