package com.axelor.apps.bankpayment.service.accountingsituation;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationAttrsServiceImpl;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.common.ObjectUtils;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.List;

public class AccountingSituationAttrsServiceBankPaymentImpl
    extends AccountingSituationAttrsServiceImpl {

  private final BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public AccountingSituationAttrsServiceBankPaymentImpl(
      AppAccountService appAccountService,
      AccountConfigService accountConfigService,
      PaymentModeService paymentModeService,
      BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    super(appAccountService, accountConfigService, paymentModeService);
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  protected String createDomainForBankDetails(
      AccountingSituation accountingSituation, PaymentMode paymentMode) {
    String domain = "self.id = 0";
    List<BankDetails> authorizedBankDetailsList;
    if (paymentMode != null) {
      authorizedBankDetailsList =
          bankDetailsBankPaymentService.getBankDetailsLinkedToActiveUmr(
              paymentMode, accountingSituation.getPartner(), accountingSituation.getCompany());
      if (authorizedBankDetailsList != null && authorizedBankDetailsList.isEmpty()) {
        authorizedBankDetailsList =
            paymentModeService.getCompatibleBankDetailsList(
                paymentMode, accountingSituation.getCompany());
        if (!ObjectUtils.isEmpty(authorizedBankDetailsList)) {
          domain =
              String.format(
                  "self.id IN (%s) AND self.active = true",
                  StringHelper.getIdListString(authorizedBankDetailsList));
        }
      }
    }
    return domain;
  }
}
