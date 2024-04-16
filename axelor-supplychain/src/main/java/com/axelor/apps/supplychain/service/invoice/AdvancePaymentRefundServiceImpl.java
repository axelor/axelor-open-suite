package com.axelor.apps.supplychain.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvancePaymentRefundServiceImpl implements AdvancePaymentRefundService {

  protected InvoiceRepository invoiceRepository;

  @Inject
  public AdvancePaymentRefundServiceImpl(InvoiceRepository invoiceRepository) {
    this.invoiceRepository = invoiceRepository;
  }

  @Override
  public BigDecimal getRefundPaidAmount(Invoice advancePayment) {
    List<Invoice> refundList = getRefundList(advancePayment);

    if (!ObjectUtils.isEmpty(refundList)) {
      return refundList.stream()
          .map(Invoice::getAmountPaid)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    return BigDecimal.ZERO;
  }

  protected List<Invoice> getRefundList(Invoice advancePayment) {
    List<Invoice> refundList = new ArrayList<>();

    if (advancePayment == null) {
      return refundList;
    }

    String filter =
        "self.operationSubTypeSelect = :operationSubTypeSelect AND (self.originalInvoice = :originalInvoice OR :advancePaymentInvoice MEMBER OF self.advancePaymentInvoiceSet) AND self.amountPaid > 0 AND self.statusSelect = :statusSelect";
    Map<String, Object> params = new HashMap<>();
    params.put(
        "operationSubTypeSelect", InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE_PAYMENT_REFUND);
    params.put("originalInvoice", advancePayment);
    params.put("advancePaymentInvoice", advancePayment.getInvoiceId());
    params.put("statusSelect", InvoiceRepository.STATUS_VALIDATED);
    refundList = invoiceRepository.all().filter(filter).bind(params).fetch();

    return refundList;
  }
}
