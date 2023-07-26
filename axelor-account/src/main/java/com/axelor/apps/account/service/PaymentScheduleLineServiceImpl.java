/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleRepository;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineToolService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentScheduleLineServiceImpl implements PaymentScheduleLineService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppBaseService appBaseService;
  protected PaymentScheduleService paymentScheduleService;
  protected MoveValidateService moveValidateService;
  protected MoveCreateService moveCreateService;
  protected PaymentModeService paymentModeService;
  protected SequenceService sequenceService;
  protected AccountingSituationService accountingSituationService;
  protected MoveToolService moveToolService;
  protected PaymentService paymentService;
  protected MoveLineRepository moveLineRepo;
  protected PaymentScheduleLineRepository paymentScheduleLineRepo;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveLineToolService moveLineToolService;

  @Inject
  public PaymentScheduleLineServiceImpl(
      AppBaseService appBaseService,
      PaymentScheduleService paymentScheduleService,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      PaymentModeService paymentModeService,
      SequenceService sequenceService,
      AccountingSituationService accountingSituationService,
      MoveToolService moveToolService,
      PaymentService paymentService,
      MoveLineRepository moveLineRepo,
      PaymentScheduleLineRepository paymentScheduleLineRepo,
      MoveLineCreateService moveLineCreateService,
      MoveLineToolService moveLineToolService) {
    this.appBaseService = appBaseService;
    this.paymentScheduleService = paymentScheduleService;
    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.paymentModeService = paymentModeService;
    this.sequenceService = sequenceService;
    this.accountingSituationService = accountingSituationService;
    this.moveToolService = moveToolService;
    this.paymentService = paymentService;
    this.moveLineRepo = moveLineRepo;
    this.paymentScheduleLineRepo = paymentScheduleLineRepo;
    this.moveLineCreateService = moveLineCreateService;
    this.moveLineToolService = moveLineToolService;
  }

  /**
   * Création d'une ligne d'échéancier
   *
   * @param paymentSchedule L'échéancié attaché.
   * @param inTaxAmount Le montant TTC.
   * @param scheduleLineSeq Le numéro d'échéance.
   * @param scheduleDate La date d'échéance.
   * @return
   */
  @Override
  public PaymentScheduleLine createPaymentScheduleLine(
      PaymentSchedule paymentSchedule,
      BigDecimal inTaxAmount,
      int scheduleLineSeq,
      LocalDate scheduleDate) {

    PaymentScheduleLine paymentScheduleLine = new PaymentScheduleLine();
    paymentScheduleLine.setScheduleLineSeq(scheduleLineSeq);
    paymentScheduleLine.setScheduleDate(scheduleDate);
    paymentScheduleLine.setInTaxAmount(inTaxAmount);
    paymentScheduleLine.setStatusSelect(PaymentScheduleLineRepository.STATUS_DRAFT);

    if (paymentSchedule != null) {
      paymentSchedule.addPaymentScheduleLineListItem(paymentScheduleLine);
    }

    log.debug(
        "Creation of payment schedule line number {} for the date {} and amount {}",
        paymentScheduleLine.getScheduleLineSeq(),
        paymentScheduleLine.getScheduleDate(),
        paymentScheduleLine.getInTaxAmount());

    return paymentScheduleLine;
  }

  /**
   * En fonction des infos d'entête d'un échéancier, crée les lignes d'échéances
   *
   * @param paymentSchedule
   */
  @Override
  public List<PaymentScheduleLine> createPaymentScheduleLines(PaymentSchedule paymentSchedule) {

    List<PaymentScheduleLine> paymentScheduleLines = new ArrayList<PaymentScheduleLine>();

    int nbrTerm = paymentSchedule.getNbrTerm();

    BigDecimal inTaxAmount = paymentSchedule.getInTaxAmount();

    log.debug(
        "Creation of lines for the payment schedule number {} (number of schedule : {}, amount : {})",
        new Object[] {paymentSchedule.getPaymentScheduleSeq(), nbrTerm, inTaxAmount});

    if (nbrTerm > 0 && inTaxAmount.compareTo(BigDecimal.ZERO) == 1) {

      BigDecimal termAmount = inTaxAmount.divide(new BigDecimal(nbrTerm), 2, RoundingMode.HALF_UP);
      BigDecimal cumul = BigDecimal.ZERO;

      for (int i = 1; i < nbrTerm + 1; i++) {

        if (i == nbrTerm) {
          termAmount = inTaxAmount.subtract(cumul);
        } else {
          cumul = cumul.add(termAmount);
        }

        paymentScheduleLines.add(
            this.createPaymentScheduleLine(
                paymentSchedule, termAmount, i, paymentSchedule.getStartDate().plusMonths(i - 1)));
      }
    }

    return paymentScheduleLines;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Move createPaymentMove(
      PaymentScheduleLine paymentScheduleLine,
      BankDetails companyBankDetails,
      PaymentMode paymentMode)
      throws AxelorException {

    Preconditions.checkNotNull(paymentScheduleLine);
    Preconditions.checkNotNull(companyBankDetails);

    PaymentSchedule paymentSchedule = paymentScheduleLine.getPaymentSchedule();
    Company company = paymentSchedule.getCompany();
    Partner partner = paymentSchedule.getPartner();
    Journal journal =
        paymentModeService.getPaymentModeJournal(paymentMode, company, companyBankDetails);
    BigDecimal amount = paymentScheduleLine.getInTaxAmount();
    String origin = paymentScheduleLine.getName();
    LocalDate todayDate = appBaseService.getTodayDate(company);
    Account account = accountingSituationService.getCustomerAccount(partner, company);

    Move move =
        moveCreateService.createMove(
            journal,
            company,
            null,
            partner,
            paymentMode,
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            origin,
            null,
            companyBankDetails);

    MoveLine creditMoveLine =
        moveLineCreateService.createMoveLine(
            move, partner, account, amount, false, todayDate, 1, origin, null);
    move.addMoveLineListItem(creditMoveLine);
    creditMoveLine = moveLineRepo.save(creditMoveLine);

    Account paymentModeAccount =
        paymentModeService.getPaymentModeAccount(paymentMode, company, companyBankDetails);
    MoveLine debitMoveLine =
        moveLineCreateService.createMoveLine(
            move, partner, paymentModeAccount, amount, true, todayDate, 2, origin, null);
    move.addMoveLineListItem(debitMoveLine);
    debitMoveLine = moveLineRepo.save(debitMoveLine);

    moveValidateService.accounting(move);

    // Reconcile
    if (paymentSchedule.getTypeSelect() == PaymentScheduleRepository.TYPE_TERMS
        && paymentSchedule.getInvoiceSet() != null) {
      List<MoveLine> debitMoveLineList =
          paymentSchedule.getInvoiceSet().stream()
              .sorted(Comparator.comparing(Invoice::getDueDate))
              .map(invoice -> moveLineToolService.getDebitCustomerMoveLine(invoice))
              .collect(Collectors.toList());

      if (moveToolService.isSameAccount(debitMoveLineList, account)) {
        List<MoveLine> creditMoveLineList = Lists.newArrayList(creditMoveLine);
        paymentService.useExcessPaymentOnMoveLines(debitMoveLineList, creditMoveLineList);
      }
    }

    paymentScheduleLine.setDirectDebitAmount(amount);
    paymentScheduleLine.setInTaxAmountPaid(amount);
    paymentScheduleLine.setAdvanceOrPaymentMove(move);
    paymentScheduleLine.setAdvanceMoveLine(creditMoveLine);
    paymentScheduleLine.setStatusSelect(PaymentScheduleLineRepository.STATUS_VALIDATED);

    paymentScheduleService.closePaymentScheduleIfAllPaid(paymentSchedule);

    return move;
  }
}
