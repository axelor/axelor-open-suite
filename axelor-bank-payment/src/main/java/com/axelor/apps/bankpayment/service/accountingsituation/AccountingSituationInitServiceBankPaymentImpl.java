package com.axelor.apps.bankpayment.service.accountingsituation;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationInitServiceImpl;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.SequenceService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class AccountingSituationInitServiceBankPaymentImpl
    extends AccountingSituationInitServiceImpl {
  private final BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public AccountingSituationInitServiceBankPaymentImpl(
      SequenceService sequenceService,
      AccountingSituationRepository accountingSituationRepository,
      PaymentModeService paymentModeService,
      AccountConfigService accountConfigService,
      BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    super(sequenceService, accountingSituationRepository, paymentModeService, accountConfigService);
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public AccountingSituation createAccountingSituation(Partner partner, Company company)
      throws AxelorException {
    AccountingSituation accountingSituation = new AccountingSituation();
    accountingSituation.setVatSystemSelect(AccountingSituationRepository.VAT_COMMON_SYSTEM);
    accountingSituation.setCompany(company);
    partner.addCompanySetItem(company);

    PaymentMode inPaymentMode = partner.getInPaymentMode();
    PaymentMode outPaymentMode = partner.getOutPaymentMode();
    BankDetails defaultBankDetails = company.getDefaultBankDetails();

    if (inPaymentMode != null) {
      List<BankDetails> authorizedInBankDetails =
          bankDetailsBankPaymentService.getBankDetailsLinkedToActiveUmr(
              inPaymentMode, partner, company);
      if (authorizedInBankDetails != null && authorizedInBankDetails.isEmpty()) {
        authorizedInBankDetails =
            paymentModeService.getCompatibleBankDetailsList(inPaymentMode, company);
        if (authorizedInBankDetails.contains(defaultBankDetails)) {
          accountingSituation.setCompanyInBankDetails(defaultBankDetails);
        }
      }
    }

    if (outPaymentMode != null) {
      List<BankDetails> authorizedInBankDetails =
          bankDetailsBankPaymentService.getBankDetailsLinkedToActiveUmr(
              outPaymentMode, partner, company);
      if (authorizedInBankDetails != null && authorizedInBankDetails.isEmpty()) {
        List<BankDetails> authorizedOutBankDetails =
            paymentModeService.getCompatibleBankDetailsList(outPaymentMode, company);
        if (authorizedOutBankDetails.contains(defaultBankDetails)) {
          accountingSituation.setCompanyOutBankDetails(defaultBankDetails);
        }
      }
    }

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    accountingSituation.setInvoiceAutomaticMail(accountConfig.getInvoiceAutomaticMail());
    accountingSituation.setInvoiceMessageTemplate(accountConfig.getInvoiceMessageTemplate());

    partner.addAccountingSituationListItem(accountingSituation);
    return accountingSituationRepository.save(accountingSituation);
  }
}
