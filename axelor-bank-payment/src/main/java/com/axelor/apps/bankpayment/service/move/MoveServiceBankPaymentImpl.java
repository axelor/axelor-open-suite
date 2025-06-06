package com.axelor.apps.bankpayment.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.google.inject.Inject;

public class MoveServiceBankPaymentImpl implements MoveServiceBankPayment {

  protected BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public MoveServiceBankPaymentImpl(BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  public BankDetails checkMovePartnerBankDetails(Move move) {
    BankDetails partnerBankDetails = move.getPartnerBankDetails();
    Company company = move.getCompany();
    PaymentMode paymentMode = move.getPaymentMode();

    if (bankDetailsBankPaymentService.isBankDetailsNotLinkedToActiveUmr(
        paymentMode, company, partnerBankDetails)) {
      partnerBankDetails = null;
    }
    return partnerBankDetails;
  }
}
