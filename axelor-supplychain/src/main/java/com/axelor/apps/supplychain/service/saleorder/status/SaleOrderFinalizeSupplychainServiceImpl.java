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
      AppSupplychainService appSupplychainService,
      AccountingSituationSupplychainService accountingSituationSupplychainService) {
    super(
        saleOrderRepository,
        sequenceService,
        saleOrderService,
        saleOrderPrintService,
        saleConfigService,
        appSaleService,
        appCrmService);
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
}
