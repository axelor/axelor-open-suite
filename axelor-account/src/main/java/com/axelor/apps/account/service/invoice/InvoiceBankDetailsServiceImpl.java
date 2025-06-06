package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;

public class InvoiceBankDetailsServiceImpl implements InvoiceBankDetailsService {

  @Override
  public BankDetails getDefaultBankDetails(Invoice invoice) {
    Partner partner = invoice.getPartner();
    if (partner != null) {
      return partner.getBankDetailsList().stream()
          .filter(bankDetails -> bankDetails.getIsDefault() && bankDetails.getActive())
          .findFirst()
          .orElse(null);
    }
    return null;
  }
}
