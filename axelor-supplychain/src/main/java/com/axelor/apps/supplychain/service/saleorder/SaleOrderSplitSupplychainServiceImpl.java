package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderOrderingStatusService;
import com.axelor.apps.sale.service.saleorder.SaleOrderSplitServiceImpl;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderConfirmService;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderFinalizeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineOnChangeService;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class SaleOrderSplitSupplychainServiceImpl extends SaleOrderSplitServiceImpl {

  protected final InvoiceRepository invoiceRepository;
  protected final AppSupplychainService appSupplychainService;
  protected final SaleOrderAdvancePaymentFetchService saleOrderAdvancePaymentFetchService;

  @Inject
  public SaleOrderSplitSupplychainServiceImpl(
      SaleOrderRepository saleOrderRepository,
      SaleOrderLineRepository saleOrderLineRepository,
      SaleOrderLineOnChangeService saleOrderLineOnChangeService,
      SaleOrderFinalizeService saleOrderFinalizeService,
      SaleOrderConfirmService saleOrderConfirmService,
      SaleOrderComputeService saleOrderComputeService,
      AppBaseService appBaseService,
      SaleOrderOrderingStatusService saleOrderOrderingStatusService,
      InvoiceRepository invoiceRepository,
      AppSupplychainService appSupplychainService,
      SaleOrderAdvancePaymentFetchService saleOrderAdvancePaymentFetchService) {
    super(
        saleOrderRepository,
        saleOrderLineRepository,
        saleOrderLineOnChangeService,
        saleOrderFinalizeService,
        saleOrderConfirmService,
        saleOrderComputeService,
        appBaseService,
        saleOrderOrderingStatusService);
    this.invoiceRepository = invoiceRepository;
    this.appSupplychainService = appSupplychainService;
    this.saleOrderAdvancePaymentFetchService = saleOrderAdvancePaymentFetchService;
  }

  @Override
  protected SaleOrder getConfirmedSaleOrder(SaleOrder saleOrder) {
    SaleOrder confirmedSaleOrder = super.getConfirmedSaleOrder(saleOrder);
    if (!appSupplychainService.isApp("supplychain")) {
      return confirmedSaleOrder;
    }

    if (saleOrder.getAdvancePaymentNeeded()) {
      confirmedSaleOrder.setAdvancePaymentNeeded(true);
      confirmedSaleOrder.setAdvancePaymentAmountNeeded(saleOrder.getAdvancePaymentAmountNeeded());
      saleOrder.setAdvancePaymentNeeded(false);
      saleOrder.setAdvancePaymentAmountNeeded(BigDecimal.ZERO);
      List<Invoice> advancePaymentInvoiceList =
          saleOrderAdvancePaymentFetchService.getAdvancePayments(saleOrder);
      for (Invoice invoice : advancePaymentInvoiceList) {
        invoice.setSaleOrder(confirmedSaleOrder);
      }
    }
    return confirmedSaleOrder;
  }

  @Override
  protected void checkBeforeConfirm(SaleOrder saleOrder, Map<Long, BigDecimal> qtyToOrderMap)
      throws AxelorException {
    super.checkBeforeConfirm(saleOrder, qtyToOrderMap);
    checkAdvancePayment(saleOrder, qtyToOrderMap);
  }

  protected void checkAdvancePayment(SaleOrder saleOrder, Map<Long, BigDecimal> qtyToOrderMap)
      throws AxelorException {
    BigDecimal totalAmount = BigDecimal.ZERO;
    for (Map.Entry<Long, BigDecimal> entry : qtyToOrderMap.entrySet()) {
      Long lineId = entry.getKey();
      BigDecimal qtyToOrder = entry.getValue();
      if (qtyToOrderMap.get(lineId) != null
          && qtyToOrderMap.get(lineId).compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      SaleOrderLine saleOrderLine = saleOrderLineRepository.find(lineId);
      totalAmount = totalAmount.add(saleOrderLine.getInTaxPrice().multiply(qtyToOrder));
    }
    if (totalAmount.compareTo(saleOrder.getAdvanceTotal()) < 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.SALE_ORDER_SPLIT_ADVANCE_PAYMENT_AMOUNT_ERROR));
    }
  }
}
