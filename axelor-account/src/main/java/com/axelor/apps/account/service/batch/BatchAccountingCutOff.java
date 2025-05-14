/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountingCutOffService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchAccountingCutOff extends PreviewBatch {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AccountingCutOffService cutOffService;
  protected AccountingBatchRepository accountingBatchRepository;

  @Inject
  public BatchAccountingCutOff(
      AccountingCutOffService cutOffService, AccountingBatchRepository accountingBatchRepository) {
    super();
    this.accountingBatchRepository = accountingBatchRepository;
    this.cutOffService = cutOffService;
  }

  @Override
  protected void process() {
    try {
      AccountingBatch accountingBatch = batch.getAccountingBatch();

      LocalDate moveDate = accountingBatch.getMoveDate();
      int accountingCutOffTypeSelect = accountingBatch.getAccountingCutOffTypeSelect();
      this.updateBatch(moveDate, accountingCutOffTypeSelect);

      super.process();
    } catch (Exception e) {
      TraceBackService.trace(e, null, batch.getId());
      incrementAnomaly();
    }
  }

  @Override
  protected void _processByQuery(AccountingBatch accountingBatch) {
    Company company = accountingBatch.getCompany();
    LocalDate moveDate = accountingBatch.getMoveDate();
    Set<Journal> journalSet = accountingBatch.getJournalSet();
    int accountingCutOffTypeSelect = accountingBatch.getAccountingCutOffTypeSelect();

    int offset = 0;
    List<Move> moveList;
    Query<Move> moveQuery =
        cutOffService.getMoves(company, journalSet, moveDate, accountingCutOffTypeSelect);

    while (!(moveList = moveQuery.fetch(getFetchLimit(), offset)).isEmpty()) {

      accountingBatch = accountingBatchRepository.find(accountingBatch.getId());
      company = accountingBatch.getCompany();
      journalSet = accountingBatch.getJournalSet();

      for (Move move : moveList) {
        ++offset;

        if (this._processMove(
            moveRepo.find(move.getId()), accountingBatchRepository.find(accountingBatch.getId()))) {
          break;
        }
      }

      JPA.clear();
      findBatch();
    }
  }

  protected void _processByIds(AccountingBatch accountingBatch) {
    List<Move> moveList =
        recordIdList.stream()
            .map(it -> moveLineRepo.find(it))
            .filter(Objects::nonNull)
            .map(MoveLine::getMove)
            .distinct()
            .collect(Collectors.toList());

    for (Move move : moveList) {
      this._processMove(
          moveRepo.find(move.getId()), accountingBatchRepository.find(accountingBatch.getId()));
    }
  }

  protected boolean _processMove(Move move, AccountingBatch accountingBatch) {
    try {
      Journal miscOpeJournal = accountingBatch.getMiscOpeJournal();
      LocalDate reverseMoveDate = accountingBatch.getReverseMoveDate();
      LocalDate moveDate = accountingBatch.getMoveDate();
      String moveDescription = accountingBatch.getMoveDescription();
      String reverseMoveDescription = accountingBatch.getReverseMoveDescription();
      int accountingCutOffTypeSelect = accountingBatch.getAccountingCutOffTypeSelect();
      int cutOffMoveStatusSelect = accountingBatch.getGeneratedMoveStatusSelect();
      boolean automaticReverse = accountingBatch.getAutomaticReverse();
      boolean automaticReconcile = accountingBatch.getAutomaticReconcile();
      String prefixOrigin =
          accountingBatch.getPrefixOrigin() != null
              ? accountingBatch.getPrefixOrigin()
              : miscOpeJournal.getPrefixOrigin() != null ? miscOpeJournal.getPrefixOrigin() : "";

      List<Move> moveList =
          cutOffService.generateCutOffMovesFromMove(
              move,
              miscOpeJournal,
              moveDate,
              reverseMoveDate,
              moveDescription,
              reverseMoveDescription,
              accountingCutOffTypeSelect,
              cutOffMoveStatusSelect,
              automaticReverse,
              automaticReconcile,
              prefixOrigin);

      if (moveList != null && !moveList.isEmpty()) {
        updateAccountMove(move, true);

        for (Move cutOffMove : moveList) {
          updateAccountMove(cutOffMove, false);
        }
      }

    } catch (AxelorException e) {
      TraceBackService.trace(
          new AxelorException(e, e.getCategory(), I18n.get("Move") + " %s", move.getReference()),
          ExceptionOriginRepository.INVOICE_ORIGIN,
          batch.getId());
      incrementAnomaly();
      return true;
    } catch (Exception e) {
      TraceBackService.trace(
          new Exception(String.format(I18n.get("Move") + " %s", move.getReference()), e),
          ExceptionOriginRepository.INVOICE_ORIGIN,
          batch.getId());
      incrementAnomaly();
      LOG.error("Anomaly generated for the move {}", move.getReference());
      return true;
    }

    return false;
  }

  @Transactional
  public void updateBatch(LocalDate moveDate, int accountingCutOffTypeSelect) {
    batch.setMoveDate(moveDate);
    batch.setAccountingCutOffTypeSelect(accountingCutOffTypeSelect);
    batchRepo.save(batch);
  }

  /**
   * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the
   * entity in the persistant context. Warning : {@code batch} entity have to be saved before.
   */
  @Override
  protected void stop() {
    StringBuilder comment =
        new StringBuilder(
            String.format(
                "%s\n\t* %s ",
                I18n.get(AccountExceptionMessage.ACCOUNTING_CUT_OFF_GENERATION_REPORT),
                batch.getDone()));

    comment.append(getProcessedMessage());

    comment.append(
        String.format(
            "\n\t" + I18n.get(com.axelor.apps.base.exceptions.BaseExceptionMessage.BASE_BATCH_3),
            batch.getAnomaly()));

    super.stop();
    addComment(comment.toString());
  }

  protected String getProcessedMessage() {
    return I18n.get(AccountExceptionMessage.ACCOUNTING_CUT_OFF_MOVE_PROCESSED);
  }
}
