package com.axelor.apps.bankpayment.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.JournalService;
import com.axelor.apps.account.service.PartnerAccountService;
import com.axelor.apps.account.service.PaymentConditionService;
import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpToolService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.record.MoveRecordSetServiceImpl;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;

public class MoveRecordSetBankPaymentServiceImpl extends MoveRecordSetServiceImpl {
  protected final BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public MoveRecordSetBankPaymentServiceImpl(
      PartnerRepository partnerRepository,
      BankDetailsService bankDetailsService,
      PeriodService periodService,
      PaymentConditionService paymentConditionService,
      AppBaseService appBaseService,
      PartnerAccountService partnerAccountService,
      JournalService journalService,
      PfpService pfpService,
      MoveToolService moveToolService,
      InvoiceTermPfpToolService invoiceTermPfpToolService,
      BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    super(
        partnerRepository,
        bankDetailsService,
        periodService,
        paymentConditionService,
        appBaseService,
        partnerAccountService,
        journalService,
        pfpService,
        moveToolService,
        invoiceTermPfpToolService);
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  public void setPartnerBankDetails(Move move) {
    super.setPartnerBankDetails(move);
    PaymentMode paymentMode = move.getPaymentMode();
    Partner partner = move.getPartner();
    Company company = move.getCompany();

    if (paymentMode != null && partner != null && company != null) {
      bankDetailsBankPaymentService
          .getBankDetailsLinkedToActiveUmr(paymentMode, partner, company)
          .stream()
          .findAny()
          .ifPresent(move::setPartnerBankDetails);
    }
  }
}
