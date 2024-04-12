package com.axelor.apps.supplychain.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RefundServiceImpl implements RefundService {

  protected InvoiceRepository invoiceRepository;

  @Inject
  public RefundServiceImpl(InvoiceRepository invoiceRepository) {
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
        "self.operationSubTypeSelect = ?1 AND (self.originalInvoice = ?2 OR ?3 MEMBER OF self.advancePaymentInvoiceSet) AND self.amountPaid > 0 AND self.statusSelect = ?4";

    refundList =
        invoiceRepository
            .all()
            .filter(
                filter,
                InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE_PAYMENT_REFUND,
                advancePayment,
                advancePayment.getId(),
                InvoiceRepository.STATUS_VALIDATED)
            .fetch();

    return refundList;
  }
}
