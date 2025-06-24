package com.axelor.apps.bankpayment.service.bankdetails;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.BankDetailsServiceAccountImpl;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.google.inject.Inject;

public class BankDetailsServiceBankPaymentImpl extends BankDetailsServiceAccountImpl {

  protected BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public BankDetailsServiceBankPaymentImpl(
      BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  public BankDetails getDefaultBankDetails(
      Partner partner, Company company, PaymentMode paymentMode) {
    BankDetails defaultBankDetails = super.getDefaultBankDetails(partner, company, paymentMode);

    if (paymentMode != null && paymentMode.getTypeSelect() == PaymentModeRepository.TYPE_DD) {
      defaultBankDetails =
          bankDetailsBankPaymentService
              .getBankDetailsLinkedToActiveUmr(paymentMode, partner, company)
              .stream()
              .findFirst()
              .orElse(null);
    }
    return defaultBankDetails;
  }
}
