package com.axelor.apps.account.service;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;

public interface BankDetailsDomainServiceAccount {
  String createDomainForBankDetails(Partner partner, PaymentMode paymentMode, Company company);
}
