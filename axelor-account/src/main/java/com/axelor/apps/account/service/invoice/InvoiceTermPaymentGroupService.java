package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.InvoiceTermPayment;
import java.util.Map;

public interface InvoiceTermPaymentGroupService {

  Map<String, Map<String, Object>> getOnLoadAttrsMap(InvoiceTermPayment invoiceTermPayment);
}
