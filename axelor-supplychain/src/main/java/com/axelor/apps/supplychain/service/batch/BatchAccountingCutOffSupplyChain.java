/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.service.AccountingCutOffService;
import com.axelor.apps.account.service.batch.BatchAccountingCutOff;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.AccountingCutOffSupplyChainService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.ExceptionOriginRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchAccountingCutOffSupplyChain extends BatchAccountingCutOff {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected StockMoveRepository stockMoveRepository;
  protected StockMoveLineRepository stockMoveLineRepository;
  protected AccountingCutOffSupplyChainService cutOffSupplyChainService;
  public List<Long> recordIdList;

  @Inject
  public BatchAccountingCutOffSupplyChain(
      AccountingCutOffService cutOffService,
      MoveLineRepository moveLineRepository,
      AccountingBatchRepository accountingBatchRepository,
      StockMoveRepository stockMoveRepository,
      StockMoveLineRepository stockMoveLineRepository,
      AccountingCutOffSupplyChainService cutOffSupplyChainService) {
    super(cutOffService, moveLineRepository, accountingBatchRepository);
    this.stockMoveRepository = stockMoveRepository;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.cutOffSupplyChainService = cutOffSupplyChainService;
  }

  @Override
  protected void process() {
    AccountingBatch accountingBatch = batch.getAccountingBatch();

    LocalDate moveDate = accountingBatch.getMoveDate();
    int accountingCutOffTypeSelect = accountingBatch.getAccountingCutOffTypeSelect();
    updateBatch(moveDate, accountingCutOffTypeSelect);

    if (accountingCutOffTypeSelect
        < AccountingBatchRepository.ACCOUNTING_CUT_OFF_TYPE_PREPAID_EXPENSES) {
      if (this.recordIdList == null) {
        this._processStockMovesByQuery(accountingBatch);
      } else {
        this._processStockMovesByIds(accountingBatch);
      }
    } else {
      if (this.recordIdList == null) {
        this._processMovesByQuery(accountingBatch);
      } else {
        this._processMovesByIds(accountingBatch);
      }
    }
  }

  protected void _processStockMovesByQuery(AccountingBatch accountingBatch) {
    Company company = accountingBatch.getCompany();
    LocalDate moveDate = accountingBatch.getMoveDate();
    int accountingCutOffTypeSelect = accountingBatch.getAccountingCutOffTypeSelect();

    int offset = 0;
    List<StockMove> stockMoveList;
    Query<StockMove> stockMoveQuery =
        cutOffSupplyChainService.getStockMoves(company, accountingCutOffTypeSelect, moveDate);

    while (!(stockMoveList = stockMoveQuery.fetch(AbstractBatch.FETCH_LIMIT, offset)).isEmpty()) {

      findBatch();
      accountingBatchRepository.find(accountingBatch.getId());

      for (StockMove stockMove : stockMoveList) {
        ++offset;

        if (this._processStockMove(stockMove, accountingBatch)) {
          break;
        }
      }

      JPA.clear();
    }
  }

  protected void _processStockMovesByIds(AccountingBatch accountingBatch) {
    List<StockMove> stockMoveList =
        recordIdList.stream()
            .map(it -> stockMoveLineRepository.find(it))
            .filter(Objects::nonNull)
            .map(StockMoveLine::getStockMove)
            .distinct()
            .collect(Collectors.toList());

    for (StockMove stockMove : stockMoveList) {
      this._processStockMove(stockMove, accountingBatch);
    }
  }

  @Transactional
  protected boolean _processStockMove(StockMove stockMove, AccountingBatch accountingBatch) {
    try {
      Journal miscOpeJournal = accountingBatch.getMiscOpeJournal();
      LocalDate reverseMoveDate = accountingBatch.getReverseMoveDate();
      LocalDate moveDate = accountingBatch.getMoveDate();
      String moveDescription = accountingBatch.getMoveDescription();
      String reverseMoveDescription = accountingBatch.getReverseMoveDescription();
      int accountingCutOffTypeSelect = accountingBatch.getAccountingCutOffTypeSelect();
      int cutOffMoveStatusSelect = accountingBatch.getCutOffMoveStatusSelect();
      boolean recoveredTax = accountingBatch.getRecoveredTax();
      boolean ati = accountingBatch.getAti();
      boolean includeNotStockManagedProduct = accountingBatch.getIncludeNotStockManagedProduct();
      boolean automaticReverse = accountingBatch.getAutomaticReverse();
      boolean automaticReconcile = accountingBatch.getAutomaticReconcile();
      Account forecastedInvCustAccount = accountingBatch.getForecastedInvCustAccount();
      Account forecastedInvSuppAccount = accountingBatch.getForecastedInvSuppAccount();

      List<Move> moveList =
          cutOffSupplyChainService.generateCutOffMovesFromStockMove(
              stockMove,
              miscOpeJournal,
              moveDate,
              reverseMoveDate,
              moveDescription,
              reverseMoveDescription,
              accountingCutOffTypeSelect,
              cutOffMoveStatusSelect,
              recoveredTax,
              ati,
              includeNotStockManagedProduct,
              automaticReverse,
              automaticReconcile,
              forecastedInvCustAccount,
              forecastedInvSuppAccount);

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

  protected void updateStockMove(StockMove stockMove) {

    stockMove.addBatchSetItem(Beans.get(BatchRepository.class).find(batch.getId()));

    incrementDone();
  }

  @Override
  protected String getProcessedMessage() {
    return I18n.get(SupplychainExceptionMessage.ACCOUNTING_CUT_OFF_STOCK_MOVE_PROCESSED);
  }
}
