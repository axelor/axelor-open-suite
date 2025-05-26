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
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.util.List;

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
    Partner partner = move.getPartner();
    PaymentMode paymentMode = move.getPaymentMode();
    Company company = move.getCompany();

    if (partner == null) {
      move.setPartnerBankDetails(null);
      return;
    }

    List<BankDetails> bankDetailsList =
        bankDetailsBankPaymentService.getBankDetailsLinkedToActiveUmr(
            paymentMode, partner, company);

    BankDetails selectedBankDetails =
        (bankDetailsList != null && !bankDetailsList.isEmpty())
            ? bankDetailsList.get(0)
            : partner.getBankDetailsList().stream()
                .filter(
                    bankDetails ->
                        Boolean.TRUE.equals(bankDetails.getIsDefault())
                            && Boolean.TRUE.equals(bankDetails.getActive()))
                .findFirst()
                .orElse(null);

    move.setPartnerBankDetails(selectedBankDetails);
  }
}
