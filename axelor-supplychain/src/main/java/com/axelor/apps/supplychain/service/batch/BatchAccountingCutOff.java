/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service.batch;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.db.SupplychainBatch;
import com.axelor.apps.supplychain.db.repo.SupplychainBatchRepository;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.AccountingCutOffService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.ExceptionOriginRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchAccountingCutOff extends BatchStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected static final int FETCH_LIMIT = 1;

  protected AccountingCutOffService cutOffService;
  protected StockMoveRepository stockMoveRepository;
  protected StockMoveLineRepository stockMoveLineRepository;
  protected MoveLineRepository moveLineRepository;
  public List<Long> recordIdList;

  @Inject
  public BatchAccountingCutOff(
      AccountingCutOffService cutOffService,
      StockMoveRepository stockMoveRepository,
      StockMoveLineRepository stockMoveLineRepository,
      MoveLineRepository moveLineRepository) {
    super();
    this.cutOffService = cutOffService;
    this.stockMoveRepository = stockMoveRepository;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.moveLineRepository = moveLineRepository;
  }

  @Override
  protected void process() {
    SupplychainBatch supplychainBatch = batch.getSupplychainBatch();

    LocalDate moveDate = supplychainBatch.getMoveDate();
    int accountingCutOffTypeSelect = supplychainBatch.getAccountingCutOffTypeSelect();
    updateBatch(moveDate, accountingCutOffTypeSelect);

    if (accountingCutOffTypeSelect
        < SupplychainBatchRepository.ACCOUNTING_CUT_OFF_TYPE_PREPAID_EXPENSES) {
      if (this.recordIdList == null) {
        this._processStockMovesByQuery(supplychainBatch);
      } else {
        this._processStockMovesByIds(supplychainBatch);
      }
    } else {
      if (this.recordIdList == null) {
        this._processMovesByQuery(supplychainBatch);
      } else {
        this._processMovesByIds(supplychainBatch);
      }
    }
  }

  protected void _processStockMovesByQuery(SupplychainBatch supplychainBatch) {
    Company company = supplychainBatch.getCompany();
    LocalDate moveDate = supplychainBatch.getMoveDate();
    int accountingCutOffTypeSelect = supplychainBatch.getAccountingCutOffTypeSelect();

    int offset = 0;
    List<StockMove> stockMoveList;
    Query<StockMove> stockMoveQuery =
        cutOffService.getStockMoves(company, accountingCutOffTypeSelect, moveDate);

    while (!(stockMoveList = stockMoveQuery.fetch(AbstractBatch.FETCH_LIMIT, offset)).isEmpty()) {

      findBatch();

      for (StockMove stockMove : stockMoveList) {
        ++offset;

        if (this._processStockMove(stockMove, supplychainBatch)) {
          break;
        }
      }

      JPA.clear();
    }
  }

  protected void _processMovesByQuery(SupplychainBatch supplychainBatch) {
    Company company = supplychainBatch.getCompany();
    LocalDate moveDate = supplychainBatch.getMoveDate();
    Journal researchJournal = supplychainBatch.getResearchJournal();
    int accountingCutOffTypeSelect = supplychainBatch.getAccountingCutOffTypeSelect();

    int offset = 0;
    List<Move> moveList;
    Query<Move> moveQuery =
        cutOffService.getMoves(company, researchJournal, moveDate, accountingCutOffTypeSelect);

    while (!(moveList = moveQuery.fetch(AbstractBatch.FETCH_LIMIT, offset)).isEmpty()) {

      findBatch();

      for (Move move : moveList) {
        ++offset;

        if (this._processMove(move, supplychainBatch)) {
          break;
        }
      }

      JPA.clear();
    }
  }

  protected void _processStockMovesByIds(SupplychainBatch supplychainBatch) {
    List<StockMove> stockMoveList =
        recordIdList.stream()
            .map(it -> stockMoveLineRepository.find(it))
            .filter(Objects::nonNull)
            .map(StockMoveLine::getStockMove)
            .distinct()
            .collect(Collectors.toList());

    for (StockMove stockMove : stockMoveList) {
      this._processStockMove(stockMove, supplychainBatch);
    }
  }

  protected void _processMovesByIds(SupplychainBatch supplychainBatch) {
    List<Move> moveList =
        recordIdList.stream()
            .map(it -> moveLineRepository.find(it))
            .filter(Objects::nonNull)
            .map(MoveLine::getMove)
            .distinct()
            .collect(Collectors.toList());

    for (Move move : moveList) {
      this._processMove(move, supplychainBatch);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected boolean _processStockMove(StockMove stockMove, SupplychainBatch supplychainBatch) {
    try {
      Journal miscOpeJournal = supplychainBatch.getMiscOpeJournal();
      LocalDate reverseMoveDate = supplychainBatch.getReverseMoveDate();
      LocalDate moveDate = supplychainBatch.getMoveDate();
      String moveDescription = supplychainBatch.getMoveDescription();
      int accountingCutOffTypeSelect = supplychainBatch.getAccountingCutOffTypeSelect();
      int cutOffMoveStatusSelect = supplychainBatch.getCutOffMoveStatusSelect();
      boolean recoveredTax = supplychainBatch.getRecoveredTax();
      boolean ati = supplychainBatch.getAti();
      boolean includeNotStockManagedProduct = supplychainBatch.getIncludeNotStockManagedProduct();
      boolean automaticReverse = supplychainBatch.getAutomaticReverse();
      boolean automaticReconcile = supplychainBatch.getAutomaticReconcile();

      List<Move> moveList =
          cutOffService.generateCutOffMovesFromStockMove(
              stockMove,
              miscOpeJournal,
              moveDate,
              reverseMoveDate,
              moveDescription,
              accountingCutOffTypeSelect,
              cutOffMoveStatusSelect,
              recoveredTax,
              ati,
              includeNotStockManagedProduct,
              automaticReverse,
              automaticReconcile);

      if (moveList != null && !moveList.isEmpty()) {
        updateStockMove(stockMove);

        for (Move move : moveList) {
          updateAccountMove(move, false);
        }
      }

    } catch (AxelorException e) {
      TraceBackService.trace(
          new AxelorException(
              e, e.getCategory(), I18n.get("StockMove") + " %s", stockMove.getStockMoveSeq()),
          ExceptionOriginRepository.INVOICE_ORIGIN,
          batch.getId());
      incrementAnomaly();
      return true;
    } catch (Exception e) {
      TraceBackService.trace(
          new Exception(
              String.format(I18n.get("StockMove") + " %s", stockMove.getStockMoveSeq()), e),
          ExceptionOriginRepository.INVOICE_ORIGIN,
          batch.getId());
      incrementAnomaly();
      LOG.error("Anomaly generated for the stock move {}", stockMove.getStockMoveSeq());
      return true;
    }

    return false;
  }

  protected boolean _processMove(Move move, SupplychainBatch supplychainBatch) {
    try {
      Journal miscOpeJournal = supplychainBatch.getMiscOpeJournal();
      LocalDate reverseMoveDate = supplychainBatch.getReverseMoveDate();
      LocalDate moveDate = supplychainBatch.getMoveDate();
      String moveDescription = supplychainBatch.getMoveDescription();
      int accountingCutOffTypeSelect = supplychainBatch.getAccountingCutOffTypeSelect();
      int cutOffMoveStatusSelect = supplychainBatch.getCutOffMoveStatusSelect();
      boolean automaticReverse = supplychainBatch.getAutomaticReverse();
      boolean automaticReconcile = supplychainBatch.getAutomaticReconcile();

      List<Move> moveList =
          cutOffService.generateCutOffMovesFromMove(
              move,
              miscOpeJournal,
              moveDate,
              reverseMoveDate,
              moveDescription,
              accountingCutOffTypeSelect,
              cutOffMoveStatusSelect,
              automaticReverse,
              automaticReconcile);

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
                I18n.get(IExceptionMessage.ACCOUNTING_CUT_OFF_GENERATION_REPORT), batch.getDone()));

    if (this.batch.getAccountingCutOffTypeSelect()
            == SupplychainBatchRepository.ACCOUNTING_CUT_OFF_TYPE_SUPPLIER_INVOICES
        || this.batch.getAccountingCutOffTypeSelect()
            == SupplychainBatchRepository.ACCOUNTING_CUT_OFF_TYPE_CUSTOMER_INVOICES) {
      comment.append(I18n.get(IExceptionMessage.ACCOUNTING_CUT_OFF_STOCK_MOVE_PROCESSED));
    } else {
      comment.append(I18n.get(IExceptionMessage.ACCOUNTING_CUT_OFF_MOVE_PROCESSED));
    }

    comment.append(
        String.format(
            "\n\t"
                + I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4),
            batch.getAnomaly()));

    super.stop();
    addComment(comment.toString());
  }
}
