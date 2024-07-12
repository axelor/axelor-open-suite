package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.auth.db.User;
import java.util.List;

public interface InvoiceTermPfpToolService {

  int getPfpValidateStatusSelect(InvoiceTerm invoiceTerm);

  List<Integer> getAlreadyValidatedStatusList();

  boolean canUpdateInvoiceTerm(InvoiceTerm invoiceTerm, User currentUser);

  boolean checkPfpValidatorUser(InvoiceTerm invoiceTerm);

  User getPfpValidatorUser(Partner partner, Company company);

  boolean isPfpValidatorUser(InvoiceTerm invoiceTerm, User user);
}
