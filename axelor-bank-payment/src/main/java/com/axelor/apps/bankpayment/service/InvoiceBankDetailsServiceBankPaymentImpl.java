package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.invoice.InvoiceBankDetailsServiceImpl;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.google.inject.Inject;

public class InvoiceBankDetailsServiceBankPaymentImpl extends InvoiceBankDetailsServiceImpl {

  protected BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public InvoiceBankDetailsServiceBankPaymentImpl(
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
