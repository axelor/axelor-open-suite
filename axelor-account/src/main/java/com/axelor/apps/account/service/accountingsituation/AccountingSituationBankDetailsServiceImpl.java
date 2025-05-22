package com.axelor.apps.account.service.accountingsituation;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.google.inject.Inject;
import java.util.List;

public class AccountingSituationBankDetailsServiceImpl
    implements AccountingSituationBankDetailsService {

  protected final PaymentModeService paymentModeService;

  @Inject
  public AccountingSituationBankDetailsServiceImpl(PaymentModeService paymentModeService) {
    this.paymentModeService = paymentModeService;
  }

  @Override
  public void setAccountingSituationBankDetails(
      AccountingSituation accountingSituation, Partner partner, Company company) {
    PaymentMode inPaymentMode = partner.getInPaymentMode();
    PaymentMode outPaymentMode = partner.getOutPaymentMode();
    BankDetails defaultBankDetails = company.getDefaultBankDetails();

    if (inPaymentMode != null) {
      List<BankDetails> authorizedInBankDetails =
          paymentModeService.getCompatibleBankDetailsList(inPaymentMode, company);
      if (authorizedInBankDetails.contains(defaultBankDetails)) {
        accountingSituation.setCompanyInBankDetails(defaultBankDetails);
      }
    }

    if (outPaymentMode != null) {
      List<BankDetails> authorizedOutBankDetails =
          paymentModeService.getCompatibleBankDetailsList(outPaymentMode, company);
      if (authorizedOutBankDetails.contains(defaultBankDetails)) {
        accountingSituation.setCompanyOutBankDetails(defaultBankDetails);
      }
    }
  }
}
