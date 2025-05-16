package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.event.SaleOrderCopy;
import com.axelor.event.Event;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class SaleOrderCopyServiceImpl implements SaleOrderCopyService {

  protected final Event<SaleOrderCopy> saleOrderCopyEvent;

  protected final SaleOrderDateService saleOrderDateService;
  protected final AppBaseService appBaseService;

  @Inject
  public SaleOrderCopyServiceImpl(
      Event<SaleOrderCopy> saleOrderCopyEvent,
      SaleOrderDateService saleOrderDateService,
      AppBaseService appBaseService) {
    this.saleOrderCopyEvent = saleOrderCopyEvent;
    this.saleOrderDateService = saleOrderDateService;
    this.appBaseService = appBaseService;
  }

  @Override
  public void copySaleOrder(SaleOrder saleOrder) {
    SaleOrderCopy saleOrderCopy = new SaleOrderCopy(saleOrder);
    saleOrderCopyEvent.fire(saleOrderCopy);
  }

  @Override
  public void copySaleOrderProcess(SaleOrder copy) {

    copy.setStatusSelect(SaleOrderRepository.STATUS_DRAFT_QUOTATION);
    copy.setSaleOrderSeq(null);
    copy.clearBatchSet();
    copy.setImportId(null);
    copy.setCreationDate(appBaseService.getTodayDate(copy.getCompany()));
    copy.setConfirmationDateTime(null);
    copy.setConfirmedByUser(null);
    copy.setOrderDate(null);
    copy.setOrderNumber(null);
    copy.setVersionNumber(1);
    copy.setTotalCostPrice(null);
    copy.setTotalGrossMargin(null);
    copy.setMarginRate(null);
    copy.setEstimatedShippingDate(null);
    copy.setOrderBeingEdited(false);
    copy.setManualUnblock(false);
    copy.setBlockedOnCustCreditExceed(false);
    copy.setOrderingStatus(null);
    if (copy.getAdvancePaymentAmountNeeded().compareTo(copy.getAdvanceTotal()) <= 0) {
      copy.setAdvancePaymentAmountNeeded(BigDecimal.ZERO);
      copy.setAdvancePaymentNeeded(false);
      copy.clearAdvancePaymentList();
      copy.setAdvanceTotal(BigDecimal.ZERO);
    }

    if (copy.getSaleOrderLineList() != null) {
      for (SaleOrderLine saleOrderLine : copy.getSaleOrderLineList()) {
        saleOrderLine.setDesiredDeliveryDate(null);
        saleOrderLine.setEstimatedShippingDate(null);
        saleOrderLine.setDiscountDerogation(null);
        saleOrderLine.setOrderedQty(BigDecimal.ZERO);
      }
    }
    saleOrderDateService.computeEndOfValidityDate(copy);
  }
}
