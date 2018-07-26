/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.InterbankCodeLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.account.db.PaymentScheduleLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InterbankCodeLineRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.PaymentScheduleLineServiceImpl;
import com.axelor.apps.account.service.PaymentScheduleService;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PaymentScheduleLineBankPaymentServiceImpl extends PaymentScheduleLineServiceImpl
    implements PaymentScheduleLineBankPaymentService {

  protected static final int RESULT_LIMIT = 100;
  protected InvoicePaymentCancelService invoicePaymentCancelService;
  protected InterbankCodeLineRepository interbankCodeLineRepo;
  protected ReconcileRepository reconcileRepo;
  protected InvoicePaymentRepository invoicePaymentRepo;

  @Inject
  public PaymentScheduleLineBankPaymentServiceImpl(
      AppBaseService appBaseService,
      PaymentScheduleService paymentScheduleService,
      MoveService moveService,
      PaymentModeService paymentModeService,
      SequenceService sequenceService,
      AccountingSituationService accountingSituationService,
      MoveToolService moveToolService,
      PaymentService paymentService,
      InvoicePaymentCancelService invoicePaymentCancelService,
      InterbankCodeLineRepository interbankCodeLineRepo,
      MoveLineRepository moveLineRepo,
      PaymentScheduleLineRepository paymentScheduleLineRepo,
      ReconcileRepository reconcileRepo,
      InvoicePaymentRepository invoicePaymentRepo) {
    super(
        appBaseService,
        paymentScheduleService,
        moveService,
        paymentModeService,
        sequenceService,
        accountingSituationService,
        moveToolService,
        paymentService,
        moveLineRepo,
        paymentScheduleLineRepo);
    this.invoicePaymentCancelService = invoicePaymentCancelService;
    this.interbankCodeLineRepo = interbankCodeLineRepo;
    this.reconcileRepo = reconcileRepo;
    this.invoicePaymentRepo = invoicePaymentRepo;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void reject(PaymentScheduleLine paymentScheduleLine, boolean represent)
      throws AxelorException {
    Preconditions.checkNotNull(
        paymentScheduleLine, I18n.get("Payment schedule line cannot be null."));
    PaymentSchedule paymentSchedule = paymentScheduleLine.getPaymentSchedule();
    Preconditions.checkNotNull(
        paymentSchedule, I18n.get("Parent payment schedule cannot be null."));

    if (paymentScheduleLine.getStatusSelect() != PaymentScheduleLineRepository.STATUS_VALIDATED) {
      throw new AxelorException(
          paymentScheduleLine,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get("Only validated payment schedule lines can be rejected."));
    }

    createRejectionMove(paymentScheduleLine);

    if (paymentSchedule.getTypeSelect() == PaymentScheduleRepository.TYPE_TERMS) {
      cancelInvoicePayments(paymentScheduleLine);
    }

    if (represent) {
      representPaymentScheduleLine(paymentScheduleLine);
    }
  }

  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  protected Move createRejectionMove(PaymentScheduleLine paymentScheduleLine)
      throws AxelorException {
    MoveValidateService moveValidateService = moveService.getMoveValidateService();
    MoveLineService moveLineService = moveService.getMoveLineService();

    Move advanceOrPaymentMove = paymentScheduleLine.getAdvanceOrPaymentMove();
    Move rejectionMove = moveService.generateReverse(advanceOrPaymentMove);
    rejectionMove.setRejectOk(true);
    moveValidateService.validateMove(rejectionMove);

    InterbankCodeLine rejectCause = interbankCodeLineRepo.findByCode("A3");
    paymentScheduleLine.setInterbankCodeLine(rejectCause);
    paymentScheduleLine.setRejectMoveLine(moveLineService.getDebitCustomerMoveLine(rejectionMove));
    paymentScheduleLine.setRejectDate(rejectionMove.getDate());
    paymentScheduleLine.setAmountRejected(paymentScheduleLine.getRejectMoveLine().getDebit());
    paymentScheduleLine.setRejectedOk(true);

    List<MoveLine> moveLineList = new ArrayList<>();
    moveLineList.addAll(advanceOrPaymentMove.getMoveLineList());
    moveLineList.addAll(rejectionMove.getMoveLineList());
    moveLineService.reconcileMoveLines(moveLineList);

    return rejectionMove;
  }

  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  protected void cancelInvoicePayments(PaymentScheduleLine paymentScheduleLine)
      throws AxelorException {
    MoveLineService moveLineService = moveService.getMoveLineService();
    PaymentSchedule paymentSchedule = paymentScheduleLine.getPaymentSchedule();
    MoveLine creditMoveLine = paymentScheduleLine.getAdvanceMoveLine();
    Set<Invoice> invoiceSet =
        MoreObjects.firstNonNull(paymentSchedule.getInvoiceSet(), Collections.emptySet());

    for (Invoice invoice : invoiceSet) {
      MoveLine debitMoveLine = moveLineService.getDebitCustomerMoveLine(invoice);
      Reconcile reconcile = reconcileRepo.findByMoveLines(debitMoveLine, creditMoveLine);

      if (reconcile == null) {
        continue;
      }

      for (InvoicePayment invoicePayment : invoicePaymentRepo.findByReconcile(reconcile).fetch()) {
        invoicePaymentCancelService.cancel(invoicePayment);
      }
    }
  }

  @Transactional
  protected PaymentScheduleLine representPaymentScheduleLine(
      PaymentScheduleLine paymentScheduleLine) {
    PaymentSchedule paymentSchedule = paymentScheduleLine.getPaymentSchedule();
    BigDecimal inTaxAmount = paymentScheduleLine.getInTaxAmount();
    int scheduleLineSeq = paymentScheduleLine.getScheduleLineSeq();
    LocalDate scheduleDate = paymentScheduleLine.getScheduleDate();
    PaymentScheduleLine representedPaymentScheduleLine =
        createPaymentScheduleLine(paymentSchedule, inTaxAmount, scheduleLineSeq, scheduleDate);
    representedPaymentScheduleLine.setFromReject(true);
    representedPaymentScheduleLine.setStatusSelect(
        PaymentScheduleLineRepository.STATUS_IN_PROGRESS);
    return representedPaymentScheduleLine;
  }

  @Override
  public void reject(PaymentScheduleLine paymentScheduleLine) throws AxelorException {
    reject(paymentScheduleLine, true);
  }

  @Override
  public Collection<Exception> reject(
      List<PaymentScheduleLine> paymentScheduleLines, boolean represent) {
    paymentScheduleLines = MoreObjects.firstNonNull(paymentScheduleLines, Collections.emptyList());
    List<Exception> exceptions = new ArrayList<>();

    for (int i = 0; i < paymentScheduleLines.size(); ++i) {
      PaymentScheduleLine paymentScheduleLine = paymentScheduleLines.get(i);

      try {
        reject(paymentScheduleLine, represent);
      } catch (Exception e) {
        exceptions.add(e);
        refindPaymentScheduleLines(paymentScheduleLines, i + 1);
      }
    }

    return exceptions;
  }

  protected void refindPaymentScheduleLines(
      List<PaymentScheduleLine> paymentScheduleLines, int index) {
    List<Long> idList =
        paymentScheduleLines
            .subList(Math.max(index, paymentScheduleLines.size()), paymentScheduleLines.size())
            .stream()
            .map(PaymentScheduleLine::getId)
            .collect(Collectors.toList());

    if (!idList.isEmpty()) {
      List<PaymentScheduleLine> foundPaymentScheduleLines =
          paymentScheduleLineRepo.findByIdList(idList).fetch();

      if (foundPaymentScheduleLines.size() != idList.size()) {
        throw new IllegalStateException(
            String.format(
                "Expected size: %d, got: %d", idList.size(), foundPaymentScheduleLines.size()));
      }

      for (int i = 0; i < foundPaymentScheduleLines.size(); ++i) {
        paymentScheduleLines.set(index + i, foundPaymentScheduleLines.get(i));
      }
    }
  }

  @Override
  public Collection<Exception> reject(List<PaymentScheduleLine> paymentScheduleLines) {
    return reject(paymentScheduleLines, true);
  }

  @Override
  public void reject(long id, boolean represent) throws AxelorException {
    PaymentScheduleLine paymentScheduleLine = paymentScheduleLineRepo.find(id);
    reject(paymentScheduleLine, represent);
  }

  @Override
  public void reject(long id) throws AxelorException {
    reject(id, true);
  }

  @Override
  public int rejectFromIdList(List<Long> idList, boolean represent) {
    idList = MoreObjects.firstNonNull(idList, Collections.emptyList());
    int errorCount = 0;

    for (List<Long> subIdList : Lists.partition(idList, RESULT_LIMIT)) {
      List<PaymentScheduleLine> paymentScheduleLines =
          paymentScheduleLineRepo.findByIdList(subIdList).fetch();
      Collection<Exception> exceptions = reject(paymentScheduleLines);
      exceptions.forEach(TraceBackService::trace);
      errorCount += exceptions.size();
    }

    return errorCount;
  }

  @Override
  public int rejectFromIdList(List<Long> idList) {
    return rejectFromIdList(idList, true);
  }
}
