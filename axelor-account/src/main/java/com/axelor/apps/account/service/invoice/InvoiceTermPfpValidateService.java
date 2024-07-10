package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PfpPartialReason;
import com.axelor.auth.db.User;
import java.math.BigDecimal;
import java.util.List;

public interface InvoiceTermPfpValidateService {

  Integer massValidatePfp(List<Long> invoiceTermIds);

  void validatePfp(InvoiceTerm invoiceTerm, User currenctUser);

  void initPftPartialValidation(
      InvoiceTerm originalInvoiceTerm, BigDecimal grantedAmount, PfpPartialReason partialReason);
}
