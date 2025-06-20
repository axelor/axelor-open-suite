package com.axelor.apps.bankpayment.service.bankdetails;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.BankDetailsDomainServiceAccountImpl;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.List;

public class BankDetailsDomainServiceBankPaymentImpl extends BankDetailsDomainServiceAccountImpl {

  protected BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public BankDetailsDomainServiceBankPaymentImpl(
      BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  public String createDomainForBankDetails(
      Partner partner, PaymentMode paymentMode, Company company) {
    String domain = super.createDomainForBankDetails(partner, paymentMode, company);

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
