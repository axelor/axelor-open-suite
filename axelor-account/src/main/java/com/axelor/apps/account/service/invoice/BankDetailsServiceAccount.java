package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;

public interface BankDetailsServiceAccount {
  BankDetails getDefaultBankDetails(Partner partner, Company company, PaymentMode paymentMode);
}
