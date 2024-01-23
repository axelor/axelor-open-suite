/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.extract.ExtractContextMoveService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelService;
import com.axelor.apps.base.AxelorException;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveReverseServiceImpl implements MoveReverseService {
  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MoveCreateService moveCreateService;
  protected ReconcileService reconcileService;
  protected MoveValidateService moveValidateService;
  protected MoveRepository moveRepository;
  protected MoveLineCreateService moveLineCreateService;
  protected ExtractContextMoveService extractContextMoveService;
  protected InvoicePaymentRepository invoicePaymentRepository;
  protected InvoicePaymentCancelService invoicePaymentCancelService;

  @Inject
  public MoveReverseServiceImpl(
      MoveCreateService moveCreateService,
      ReconcileService reconcileService,
      MoveValidateService moveValidateService,
      MoveRepository moveRepository,
      MoveLineCreateService moveLineCreateService,
      ExtractContextMoveService extractContextMoveService,
      InvoicePaymentRepository invoicePaymentRepository,
      InvoicePaymentCancelService invoicePaymentCancelService) {
    this.moveCreateService = moveCreateService;
    this.reconcileService = reconcileService;
    this.moveValidateService = moveValidateService;
    this.moveRepository = moveRepository;
    this.moveLineCreateService = moveLineCreateService;
    this.extractContextMoveService = extractContextMoveService;
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.invoicePaymentCancelService = invoicePaymentCancelService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public Move generateReverse(
      Move move,
      boolean isAutomaticReconcile,
      boolean isAutomaticAccounting,
      boolean isUnreconcileOriginalMove,
      LocalDate dateOfReversion)
      throws AxelorException {

    String origin = move.getOrigin();
    if (move.getJournal().getHasDuplicateDetectionOnOrigin()
        && move.getJournal().getPrefixOrigin() != null) {
      origin = move.getJournal().getPrefixOrigin() + origin;
    }

    Move newMove =
        moveCreateService.createMove(
            move.getJournal(),
            move.getCompany(),
            move.getCurrency(),
            move.getPartner(),
            dateOfReversion,
            move.getPaymentMode(),
            move.getFiscalPosition(),
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            move.getFunctionalOriginSelect(),
            move.getIgnoreInDebtRecoveryOk(),
            move.getIgnoreInAccountingOk(),
            move.getAutoYearClosureMove(),
            origin,
            move.getDescription(),
            null,
            null,
            move.getCompanyBankDetails());

    boolean validatedMove =
        move.getStatusSelect() == MoveRepository.STATUS_DAYBOOK
            || move.getStatusSelect() == MoveRepository.STATUS_ACCOUNTED;

    for (MoveLine moveLine : move.getMoveLineList()) {
      log.debug("Moveline {}", moveLine);
      boolean isDebit = moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0;

      MoveLine newMoveLine = generateReverseMoveLine(newMove, moveLine, dateOfReversion, isDebit);
      AnalyticMoveLineRepository analyticMoveLineRepository =
          Beans.get(AnalyticMoveLineRepository.class);
      newMoveLine.setAnalyticDistributionTemplate(moveLine.getAnalyticDistributionTemplate());
      List<AnalyticMoveLine> analyticMoveLineList = Lists.newArrayList();
      if (!CollectionUtils.isEmpty(moveLine.getAnalyticMoveLineList())) {
        for (AnalyticMoveLine analyticMoveLine : moveLine.getAnalyticMoveLineList()) {
          AnalyticMoveLine newAnalyticMoveLine =
              analyticMoveLineRepository.copy(analyticMoveLine, true);
          newAnalyticMoveLine.setDate(newMoveLine.getDate());
          analyticMoveLineList.add(newAnalyticMoveLine);
        }
      } else if (moveLine.getAnalyticDistributionTemplate() != null) {
        analyticMoveLineList =
            Beans.get(AnalyticMoveLineService.class)
                .generateLines(
                    newMoveLine.getAnalyticDistributionTemplate(),
                    newMoveLine.getDebit().add(newMoveLine.getCredit()),
                    AnalyticMoveLineRepository.STATUS_REAL_ACCOUNTING,
                    dateOfReversion);
      }
      if (CollectionUtils.isNotEmpty(analyticMoveLineList)) {
        newMoveLine.clearAnalyticMoveLineList();
        analyticMoveLineList.forEach(newMoveLine::addAnalyticMoveLineListItem);
      }

      newMove.addMoveLineListItem(newMoveLine);

      if (isUnreconcileOriginalMove) {
        List<Reconcile> reconcileList =
            Beans.get(ReconcileRepository.class)
                .all()
                .filter(
                    "self.statusSelect != ?1 AND (self.debitMoveLine = ?2 OR self.creditMoveLine = ?2)",
                    ReconcileRepository.STATUS_CANCELED,
                    moveLine)
                .fetch();
        for (Reconcile reconcile : reconcileList) {
          reconcileService.unreconcile(reconcile);
        }
      } else {
        cancelInvoicePayment(move);
      }

      if (validatedMove && isAutomaticReconcile) {
        if (isDebit) {
          reconcileService.reconcile(moveLine, newMoveLine, false, true);
        } else {
          reconcileService.reconcile(newMoveLine, moveLine, false, true);
        }
      }
    }

    if (validatedMove && isAutomaticAccounting) {
      moveValidateService.accounting(newMove);
    }

    return moveRepository.save(newMove);
  }

  protected void cancelInvoicePayment(Move move) throws AxelorException {
    InvoicePayment invoicePayment =
        invoicePaymentRepository
            .all()
            .filter("self.move.id = :moveId AND self.statusSelect != :statusSelect")
            .bind("moveId", move.getId())
            .bind("statusSelect", InvoicePaymentRepository.STATUS_CANCELED)
            .fetchOne();

    if (invoicePayment != null) {
      invoicePaymentCancelService.updateCancelStatus(invoicePayment);
    }
  }

  @Override
  public Move generateReverse(Move move, Map<String, Object> assistantMap) throws AxelorException {
    move =
        generateReverse(
            move,
            (boolean) assistantMap.get("isAutomaticReconcile"),
            (boolean) assistantMap.get("isAutomaticAccounting"),
            (boolean) assistantMap.get("isUnreconcileOriginalMove"),
            (LocalDate) assistantMap.get("dateOfReversion"));
    return move;
  }

  protected MoveLine generateReverseMoveLine(
      Move reverseMove, MoveLine originMoveLine, LocalDate dateOfReversion, boolean isDebit)
      throws AxelorException {
    MoveLine reverseMoveLine =
        moveLineCreateService.createMoveLine(
            reverseMove,
            originMoveLine.getPartner(),
            originMoveLine.getAccount(),
            originMoveLine.getCurrencyAmount(),
            originMoveLine.getTaxLine(),
            originMoveLine.getDebit().add(originMoveLine.getCredit()),
            originMoveLine.getCurrencyRate(),
            !isDebit,
            dateOfReversion,
            dateOfReversion,
            dateOfReversion,
            originMoveLine.getCounter(),
            originMoveLine.getName(),
            null,
            originMoveLine.getCutOffStartDate(),
            originMoveLine.getCutOffEndDate());
    reverseMoveLine.setVatSystemSelect(originMoveLine.getVatSystemSelect());

    return reverseMoveLine;
  }

  public List<Move> massReverse(List<Move> moveList, Map<String, Object> assistantMap)
      throws AxelorException {
    boolean isAutomaticReconcile = (boolean) assistantMap.get("isAutomaticReconcile");
    boolean isAutomaticAccounting = (boolean) assistantMap.get("isAutomaticAccounting");
    boolean isUnreconcileOriginalMove = (boolean) assistantMap.get("isUnreconcileOriginalMove");
    int dateOfReversionSelect = (int) assistantMap.get("dateOfReversionSelect");

    boolean isChooseDate = dateOfReversionSelect == MoveRepository.DATE_OF_REVERSION_CHOOSE_DATE;
    LocalDate dateOfReversion =
        isChooseDate ? (LocalDate) assistantMap.get("dateOfReversion") : null;
    List<Move> reverseMoveList = new ArrayList<>();

    for (Move move : moveList) {
      if (!isChooseDate) {
        dateOfReversion =
            extractContextMoveService.getDateOfReversion(null, move, dateOfReversionSelect);
      }

      reverseMoveList.add(
          this.generateReverse(
              move,
              isAutomaticReconcile,
              isAutomaticAccounting,
              isUnreconcileOriginalMove,
              dateOfReversion));
    }

    return reverseMoveList;
  }
}
