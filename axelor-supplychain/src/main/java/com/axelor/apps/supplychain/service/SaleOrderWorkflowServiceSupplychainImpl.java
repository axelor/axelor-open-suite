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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.base.db.AppSupplychain;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.BlockedSaleOrderException;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderWorkflowServiceImpl;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderWorkflowServiceSupplychainImpl extends SaleOrderWorkflowServiceImpl {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected SaleOrderStockService saleOrderStockService;
  protected SaleOrderPurchaseService saleOrderPurchaseService;
  protected AppSupplychain appSupplychain;
  protected AccountingSituationSupplychainService accountingSituationSupplychainService;

  @Inject
  public SaleOrderWorkflowServiceSupplychainImpl(
      SequenceService sequenceService,
      PartnerRepository partnerRepo,
      SaleOrderRepository saleOrderRepo,
      AppSaleService appSaleService,
      UserService userService,
      SaleOrderStockService saleOrderStockService,
      SaleOrderPurchaseService saleOrderPurchaseService,
      AppSupplychainService appSupplychainService,
      AccountingSituationSupplychainService accountingSituationSupplychainService) {

    super(sequenceService, partnerRepo, saleOrderRepo, appSaleService, userService);

    this.saleOrderStockService = saleOrderStockService;
    this.saleOrderPurchaseService = saleOrderPurchaseService;
    this.appSupplychain = appSupplychainService.getAppSupplychain();
    this.accountingSituationSupplychainService = accountingSituationSupplychainService;
  }

  @Inject StockMoveService stockMoveService;
  @Inject TeamTaskRepository teamTaskRepo;
  @Inject InvoiceService invoiceService;

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void confirmSaleOrder(SaleOrder saleOrder) throws AxelorException {

    super.confirmSaleOrder(saleOrder);

    if (appSupplychain.getPurchaseOrderGenerationAuto()) {
      saleOrderPurchaseService.createPurchaseOrders(saleOrder);
    }
    if (appSupplychain.getCustomerStockMoveGenerationAuto()) {
      saleOrderStockService.createStocksMovesFromSaleOrder(saleOrder);
    }
    int intercoSaleCreatingStatus =
        Beans.get(AppSupplychainService.class)
            .getAppSupplychain()
            .getIntercoSaleCreatingStatusSelect();
    if (saleOrder.getInterco()
        && intercoSaleCreatingStatus == SaleOrderRepository.STATUS_ORDER_CONFIRMED) {
      Beans.get(IntercoService.class).generateIntercoPurchaseFromSale(saleOrder);
    }
  }

  @Override
  @Transactional
  public void cancelSaleOrder(
      SaleOrder saleOrder, CancelReason cancelReason, String cancelReasonStr) {

    try {
      accountingSituationSupplychainService.updateUsedCredit(saleOrder.getClientPartner());

      // Cancel the associated customer deliveries
      this.cancelAssociatedStockMoves(StockMoveRepository.ORIGIN_SALE_ORDER, saleOrder.getId());

      // Cancel the associated purchase orders and its stock moves
      List<PurchaseOrder> purchaseOrderList =
          Beans.get(PurchaseOrderRepository.class)
              .all()
              .filter(
                  "self.generatedSaleOrderId = ?1 and self.statusSelect != ?2",
                  saleOrder.getId(),
                  PurchaseOrderRepository.STATUS_CANCELED)
              .fetch();

      for (PurchaseOrder purchaseOrder : purchaseOrderList) {
        this.cancelAssociatedStockMoves(
            StockMoveRepository.ORIGIN_PURCHASE_ORDER, purchaseOrder.getId());
        purchaseOrder.setStatusSelect(PurchaseOrderRepository.STATUS_CANCELED);
      }

      // Cancel the associated team tasks
      List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();

      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        List<TeamTask> teamTaskList =
            teamTaskRepo
                .all()
                .filter(
                    "self.saleOrderLine = ?1 and self.status != ?2",
                    saleOrderLine.getId(),
                    TeamTaskRepository.STATUS_CANCELED)
                .fetch();

        for (TeamTask teamTask : teamTaskList) {
          teamTask.setStatus(TeamTaskRepository.STATUS_CANCELED);
        }
      }

      // Cancel the associated invoices
      List<Invoice> invoiceList =
          Beans.get(InvoiceRepository.class)
              .all()
              .filter(
                  "self.saleOrder = ?1 and self.statusSelect != ?2",
                  saleOrder.getId(),
                  InvoiceRepository.STATUS_CANCELED)
              .fetch();

      for (Invoice invoice : invoiceList) {
        invoiceService.cancel(invoice);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    super.cancelSaleOrder(saleOrder, cancelReason, cancelReasonStr);
  }

  public void cancelAssociatedStockMoves(String originType, Long originId) throws AxelorException {

    // Cancel the associated stock moves
    List<StockMove> stockMoveList =
        Beans.get(StockMoveRepository.class)
            .all()
            .filter(
                "self.originTypeSelect = ?1 and self.originId = ?2 and self.statusSelect != ?3",
                originType,
                originId,
                StockMoveRepository.STATUS_CANCELED)
            .fetch();

    for (StockMove stockMove : stockMoveList) {
      stockMoveService.cancel(stockMove);
    }
  }

  public String cancelWarningAssociatedElements(SaleOrder saleOrder) {
    String message = null;
    String initialMessage = I18n.get("The following associated elements will be cancelled") + " : ";

    List<StockMove> stockMoveList =
        Beans.get(StockMoveRepository.class)
            .all()
            .filter(
                "self.originTypeSelect = ?1 and self.originId = ?2 and self.statusSelect != ?3",
                StockMoveRepository.ORIGIN_SALE_ORDER,
                saleOrder.getId(),
                StockMoveRepository.STATUS_CANCELED)
            .fetch();

    if (stockMoveList != null && !stockMoveList.isEmpty()) {
      String stockMoveMessage = I18n.get("Stock move(s)") + " - ";
      message =
          message == null
              ? message = initialMessage + stockMoveMessage
              : message + ", " + stockMoveMessage;

      for (StockMove stockMove : stockMoveList) {
        message = message + " " + stockMove.getStockMoveSeq();
      }
    }

    // Associated purchase orders
    List<PurchaseOrder> purchaseOrderList =
        Beans.get(PurchaseOrderRepository.class)
            .all()
            .filter(
                "self.generatedSaleOrderId = ?1 and self.statusSelect != ?2",
                saleOrder.getId(),
                PurchaseOrderRepository.STATUS_CANCELED)
            .fetch();

    if (purchaseOrderList != null && !purchaseOrderList.isEmpty()) {
      String purchaseOrderMessage = I18n.get("Purchase order(s)") + " - ";
      message =
          message == null
              ? message = initialMessage + purchaseOrderMessage
              : message + ", " + purchaseOrderMessage;

      for (PurchaseOrder purchaseOrder : purchaseOrderList) {
        message = message + " " + purchaseOrder.getPurchaseOrderSeq();
      }
    }

    // Associated team tasks
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();

    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      List<TeamTask> teamTaskList =
          teamTaskRepo
              .all()
              .filter(
                  "self.saleOrderLine = ?1 and self.status != ?2",
                  saleOrderLine.getId(),
                  TeamTaskRepository.STATUS_CANCELED)
              .fetch();

      if (teamTaskList != null && !teamTaskList.isEmpty()) {
        String teamTaskMessage = I18n.get("Project task(s)") + " - ";
        message =
            message == null
                ? message = initialMessage + teamTaskMessage
                : message + ", " + teamTaskMessage;

        for (TeamTask teamTask : teamTaskList) {
          message = message + " " + teamTask.getName();
        }
      }
    }

    // Associated invoices
    List<Invoice> invoiceList =
        Beans.get(InvoiceRepository.class)
            .all()
            .filter(
                "self.saleOrder = ?1 and self.statusSelect != ?2",
                saleOrder.getId(),
                InvoiceRepository.STATUS_CANCELED)
            .fetch();

    if (invoiceList != null && !invoiceList.isEmpty()) {
      String invoiceMessage = I18n.get("Invoice(s)") + " - ";
      message =
          message == null
              ? message = initialMessage + invoiceMessage
              : message + ", " + invoiceMessage;

      for (Invoice invoice : invoiceList) {
        message = message + " " + invoice.getInvoiceId();
      }
    }

    return message;
  }

  @Override
  @Transactional(
    rollbackOn = {AxelorException.class, RuntimeException.class},
    ignore = {BlockedSaleOrderException.class}
  )
  public void finalizeQuotation(SaleOrder saleOrder) throws AxelorException {
    accountingSituationSupplychainService.updateCustomerCreditFromSaleOrder(saleOrder);
    super.finalizeQuotation(saleOrder);
    int intercoSaleCreatingStatus =
        Beans.get(AppSupplychainService.class)
            .getAppSupplychain()
            .getIntercoSaleCreatingStatusSelect();
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
