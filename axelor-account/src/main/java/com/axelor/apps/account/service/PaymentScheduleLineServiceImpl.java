/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.DirectDebitManagement;
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
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
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
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentScheduleLineServiceImpl implements PaymentScheduleLineService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppBaseService appBaseService;
  protected PaymentScheduleService paymentScheduleService;
  protected MoveService moveService;
  protected PaymentModeService paymentModeService;
  protected SequenceService sequenceService;
  protected AccountingSituationService accountingSituationService;
  protected MoveToolService moveToolService;
  protected PaymentService paymentService;
  protected MoveLineRepository moveLineRepo;
  protected PaymentScheduleLineRepository paymentScheduleLineRepo;

  @Inject
  public PaymentScheduleLineServiceImpl(
      AppBaseService appBaseService,
      PaymentScheduleService paymentScheduleService,
      MoveService moveService,
      PaymentModeService paymentModeService,
      SequenceService sequenceService,
      AccountingSituationService accountingSituationService,
      MoveToolService moveToolService,
      PaymentService paymentService,
      MoveLineRepository moveLineRepo,
      PaymentScheduleLineRepository paymentScheduleLineRepo) {
    this.appBaseService = appBaseService;
    this.paymentScheduleService = paymentScheduleService;
    this.moveService = moveService;
    this.paymentModeService = paymentModeService;
    this.sequenceService = sequenceService;
    this.accountingSituationService = accountingSituationService;
    this.moveToolService = moveToolService;
    this.paymentService = paymentService;
    this.moveLineRepo = moveLineRepo;
    this.paymentScheduleLineRepo = paymentScheduleLineRepo;
  }

  /**
   * Création d'une ligne d'échéancier
   *
   * @param paymentSchedule L'échéancié attaché.
   * @param invoiceTerm La facture d'échéance.
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
        "Création de la ligne de l'échéancier numéro {} pour la date du {} et la somme de {}",
        new Object[] {
          paymentScheduleLine.getScheduleLineSeq(),
          paymentScheduleLine.getScheduleDate(),
          paymentScheduleLine.getInTaxAmount()
        });

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
        "Création de lignes pour l'échéancier numéro {} (nombre d'échéance : {}, montant : {})",
        new Object[] {paymentSchedule.getPaymentScheduleSeq(), nbrTerm, inTaxAmount});

    if (nbrTerm > 0 && inTaxAmount.compareTo(BigDecimal.ZERO) == 1) {

      BigDecimal termAmount =
          inTaxAmount.divide(new BigDecimal(nbrTerm), 2, RoundingMode.HALF_EVEN);
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
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
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
    String name = paymentScheduleLine.getName();
    LocalDate todayDate = appBaseService.getTodayDate();
    Account account = accountingSituationService.getCustomerAccount(partner, company);

    Move move =
        moveService
            .getMoveCreateService()
            .createMove(
                journal,
                company,
                null,
                partner,
                paymentMode,
                MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC);

    MoveLine creditMoveLine =
        moveService
            .getMoveLineService()
            .createMoveLine(move, partner, account, amount, false, todayDate, 1, name, null);
    move.addMoveLineListItem(creditMoveLine);
    creditMoveLine = moveLineRepo.save(creditMoveLine);

    Account paymentModeAccount =
        paymentModeService.getPaymentModeAccount(paymentMode, company, companyBankDetails);
    MoveLine debitMoveLine =
        moveService
            .getMoveLineService()
            .createMoveLine(
                move, partner, paymentModeAccount, amount, true, todayDate, 2, name, null);
    move.addMoveLineListItem(debitMoveLine);
    debitMoveLine = moveLineRepo.save(debitMoveLine);

    moveService.getMoveValidateService().validate(move);

    // Reconcile
    if (paymentSchedule.getTypeSelect() == PaymentScheduleRepository.TYPE_TERMS
        && paymentSchedule.getInvoiceSet() != null) {
      List<MoveLine> debitMoveLineList =
          paymentSchedule
              .getInvoiceSet()
              .stream()
              .sorted(Comparator.comparing(Invoice::getDueDate))
              .map(invoice -> moveService.getMoveLineService().getDebitCustomerMoveLine(invoice))
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

  /**
   * Procédure permettant d'assigner un numéro de prélèvement à l'échéance à prélever Si plusieurs
   * échéance d'un même échéancier sont à prélever, alors on utilise un objet de gestion de
   * prélèvement encadrant l'ensemble des échéances en question Sinon on assigne simplement un
   * numéro de prélèvement à l'échéance
   *
   * @param paymentScheduleLineList Une liste d'échéance à prélever
   * @param paymentScheduleLine L'échéance traité
   * @param company Une société
   * @param paymentMode TODO
   * @param journal Un journal (prélèvement mensu masse ou grand compte)
   * @throws AxelorException
   */
  protected void setDebitNumber(
      List<PaymentScheduleLine> paymentScheduleLineList,
      PaymentScheduleLine paymentScheduleLine,
      Company company,
      PaymentMode paymentMode)
      throws AxelorException {

    if (hasOtherPaymentScheduleLine(paymentScheduleLineList, paymentScheduleLine)) {
      DirectDebitManagement directDebitManagement =
          getDirectDebitManagement(paymentScheduleLineList, paymentScheduleLine);
      if (directDebitManagement == null) {
        directDebitManagement =
            createDirectDebitManagement(getDirectDebitSequence(company, paymentMode), company);
      }
      paymentScheduleLine.setDirectDebitManagement(directDebitManagement);
      directDebitManagement.getPaymentScheduleLineList().add(paymentScheduleLine);
    } else {
      paymentScheduleLine.setDebitNumber(getDirectDebitSequence(company, paymentMode));
    }
  }

  protected void setDebitNumber(PaymentScheduleLine paymentScheduleLine, PaymentMode paymentMode)
      throws AxelorException {
    PaymentSchedule paymentSchedule = paymentScheduleLine.getPaymentSchedule();
    List<PaymentScheduleLine> paymentScheduleLineList =
        paymentSchedule.getPaymentScheduleLineList();
    Company company = paymentSchedule.getCompany();
    setDebitNumber(paymentScheduleLineList, paymentScheduleLine, company, paymentMode);
  }

  /**
   * Y a-t-il d'autres échéance a exporter pour le même payeur ?
   *
   * @param pslList : une liste d'échéance
   * @param psl
   * @return
   */
  protected boolean hasOtherPaymentScheduleLine(
      List<PaymentScheduleLine> pslList, PaymentScheduleLine psl) {
    int i = 0;
    for (PaymentScheduleLine paymentScheduleLine : pslList) {
      paymentScheduleLine = paymentScheduleLineRepo.find(paymentScheduleLine.getId());
      if (psl.getPaymentSchedule().equals(paymentScheduleLine.getPaymentSchedule())) {
        i++;
      }
    }
    return i > 1;
  }

  /**
   * Procédure permettant de récupérer l'objet de gestion déjà créé lors du prélèvement d'une autre
   * échéance
   *
   * @param pslList La liste d'échéance à prélever
   * @param psl L'échéance à prélever
   * @return L'objet de gestion trouvé
   */
  protected DirectDebitManagement getDirectDebitManagement(
      List<PaymentScheduleLine> pslList, PaymentScheduleLine psl) {
    for (PaymentScheduleLine paymentScheduleLine : pslList) {
      paymentScheduleLine = paymentScheduleLineRepo.find(paymentScheduleLine.getId());
      if (psl.getPaymentSchedule().equals(paymentScheduleLine.getPaymentSchedule())) {
        if (paymentScheduleLine.getDirectDebitManagement() != null) {
          return paymentScheduleLine.getDirectDebitManagement();
        }
      }
    }
    return null;
  }

  /**
   * Procédure permettant de créer un objet de gestion de prélèvement
   *
   * @param sequence La séquence de prélèvement à utiliser
   * @param company Une société
   * @return
   */
  protected DirectDebitManagement createDirectDebitManagement(String sequence, Company company) {
    DirectDebitManagement directDebitManagement = new DirectDebitManagement();
    directDebitManagement.setDebitNumber(sequence);
    directDebitManagement.setInvoiceSet(new HashSet<Invoice>());
    directDebitManagement.setPaymentScheduleLineList(new ArrayList<PaymentScheduleLine>());
    directDebitManagement.setCompany(company);
    return directDebitManagement;
  }

  protected String getDirectDebitSequence(Company company, PaymentMode paymentMode)
      throws AxelorException {

    // TODO manage multi bank

    return sequenceService.getSequenceNumber(
        paymentModeService.getPaymentModeSequence(paymentMode, company, null));
  }
}
