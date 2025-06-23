package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;

public class InvoiceBankDetailsServiceImpl implements InvoiceBankDetailsService {

  @Override
  public BankDetails getDefaultBankDetails(
      Partner partner, Company company, PaymentMode paymentMode) {
    if (partner != null) {
      return partner.getBankDetailsList().stream()
          .filter(bankDetails -> bankDetails.getIsDefault() && bankDetails.getActive())
          .findFirst()
          .orElse(null);
    }
    return null;
  }
}
