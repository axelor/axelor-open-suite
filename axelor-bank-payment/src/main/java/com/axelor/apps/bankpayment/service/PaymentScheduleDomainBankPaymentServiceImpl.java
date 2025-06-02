package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.PaymentScheduleDomainServiceImpl;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.List;

public class PaymentScheduleDomainBankPaymentServiceImpl extends PaymentScheduleDomainServiceImpl {
  protected BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public PaymentScheduleDomainBankPaymentServiceImpl(
      BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  public String createDomainForBankDetails(PaymentSchedule paymentSchedule) {
    Partner partner = paymentSchedule.getPartner();
    String domain = super.createDomainForBankDetails(paymentSchedule);

    PaymentMode paymentMode = paymentSchedule.getPaymentMode();
    Company company = paymentSchedule.getCompany();

    if (partner != null && !partner.getBankDetailsList().isEmpty()) {
      List<BankDetails> bankDetailsList =
          bankDetailsBankPaymentService.getBankDetailsLinkedToActiveUmr(
              paymentMode, partner, company);
      if (paymentMode.getTypeSelect() == PaymentModeRepository.TYPE_DD) {
        domain = "self.id IN (" + StringHelper.getIdListString(bankDetailsList) + ")";
      }
    }
    return domain;
  }
}
