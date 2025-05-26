package com.axelor.apps.bankpayment.service.accountingsituation;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationBankDetailsServiceImpl;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class AccountingSituationBankDetailsServiceBankPaymentImpl
    extends AccountingSituationBankDetailsServiceImpl {
  private final BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public AccountingSituationBankDetailsServiceBankPaymentImpl(
      PaymentModeService paymentModeService,
      BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    super(paymentModeService);
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void setAccountingSituationBankDetails(
      AccountingSituation accountingSituation, Partner partner, Company company) {

    super.setAccountingSituationBankDetails(accountingSituation, partner, company);

    PaymentMode inPaymentMode = partner.getInPaymentMode();
    PaymentMode outPaymentMode = partner.getOutPaymentMode();
    BankDetails defaultBankDetails = company.getDefaultBankDetails();

    if (inPaymentMode != null) {
      List<BankDetails> authorizedInBankDetails =
          bankDetailsBankPaymentService.getBankDetailsLinkedToActiveUmr(
              inPaymentMode, partner, company);
      if (authorizedInBankDetails.contains(defaultBankDetails)) {
        accountingSituation.setCompanyInBankDetails(defaultBankDetails);
      }
    }

    if (outPaymentMode != null) {
      List<BankDetails> authorizedOutBankDetails =
          bankDetailsBankPaymentService.getBankDetailsLinkedToActiveUmr(
              outPaymentMode, partner, company);
      if (authorizedOutBankDetails.contains(defaultBankDetails)) {
        accountingSituation.setCompanyOutBankDetails(defaultBankDetails);
      }
    }
  }
}
