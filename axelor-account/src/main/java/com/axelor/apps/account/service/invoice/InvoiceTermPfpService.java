package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PfpPartialReason;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.auth.db.User;
import java.math.BigDecimal;
import java.util.List;

public interface InvoiceTermPfpService {
  void validatePfp(InvoiceTerm invoiceTerm, User currenctUser);

  Integer massValidatePfp(List<Long> invoiceTermIds);

  Integer massRefusePfp(
      List<Long> invoiceTermIds, CancelReason reasonOfRefusalToPay, String reasonOfRefusalToPayStr);

  void refusalToPay(
      InvoiceTerm invoiceTerm, CancelReason reasonOfRefusalToPay, String reasonOfRefusalToPayStr);

  void generateInvoiceTerm(
      InvoiceTerm originalInvoiceTerm,
      BigDecimal invoiceAmount,
      BigDecimal grantedAmount,
      PfpPartialReason partialReason);
}
