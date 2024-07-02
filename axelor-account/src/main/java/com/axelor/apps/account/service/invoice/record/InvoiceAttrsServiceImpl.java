package com.axelor.apps.account.service.invoice.record;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceAttrsServiceImpl implements InvoiceAttrsService {

  @Inject
  public InvoiceAttrsServiceImpl() {}

  protected void addAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {
    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }

  @Override
  public void hideCancelButton(Invoice invoice, Map<String, Map<String, Object>> attrsMap) {
    boolean isHidden =
        List.of(InvoiceRepository.STATUS_VENTILATED, InvoiceRepository.STATUS_CANCELED)
            .contains(invoice.getStatusSelect());

    if (InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE == invoice.getOperationSubTypeSelect()
        && InvoiceRepository.STATUS_VALIDATED == invoice.getStatusSelect()) {
      if (ObjectUtils.isEmpty(invoice.getInvoicePaymentList())) {
        isHidden = false;
      } else {
        isHidden =
            invoice.getInvoicePaymentList().stream()
                .noneMatch(
                    payment ->
                        !List.of(
                                InvoicePaymentRepository.STATUS_VALIDATED,
                                InvoicePaymentRepository.STATUS_PENDING)
                            .contains(payment.getStatusSelect()));
      }
    }

    this.addAttr("cancelBtn", "hidden", isHidden, attrsMap);
  }
}
