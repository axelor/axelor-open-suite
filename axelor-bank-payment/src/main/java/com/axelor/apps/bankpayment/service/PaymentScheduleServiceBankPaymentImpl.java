package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleRepository;
import com.axelor.apps.account.service.PaymentScheduleLineService;
import com.axelor.apps.account.service.PaymentScheduleServiceImpl;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.google.inject.Inject;
import java.time.LocalDate;

public class PaymentScheduleServiceBankPaymentImpl extends PaymentScheduleServiceImpl {
  protected BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public PaymentScheduleServiceBankPaymentImpl(
      AppAccountService appAccountService,
      PaymentScheduleLineService paymentScheduleLineService,
      PaymentScheduleLineRepository paymentScheduleLineRepo,
      SequenceService sequenceService,
      PaymentScheduleRepository paymentScheduleRepo,
      PartnerRepository partnerRepository,
      PartnerService partnerService,
      BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    super(
        appAccountService,
        paymentScheduleLineService,
        paymentScheduleLineRepo,
        sequenceService,
        paymentScheduleRepo,
        partnerRepository,
        partnerService);
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  public PaymentSchedule createPaymentSchedule(
      Partner partner,
      Invoice invoice,
      Company company,
      LocalDate date,
      LocalDate startDate,
      int nbrTerm,
      BankDetails bankDetails,
      PaymentMode paymentMode)
      throws AxelorException {

    PaymentSchedule paymentSchedule =
        super.createPaymentSchedule(
            partner, invoice, company, date, startDate, nbrTerm, bankDetails, paymentMode);

    bankDetailsBankPaymentService
        .getBankDetailsLinkedToActiveUmr(paymentMode, partner, company)
        .stream()
        .findAny()
        .ifPresent(paymentSchedule::setBankDetails);

    return paymentSchedule;
  }
}
