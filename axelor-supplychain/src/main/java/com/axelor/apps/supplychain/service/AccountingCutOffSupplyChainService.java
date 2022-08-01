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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.service.AccountingCutOffService;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.meta.CallMethod;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.List;

public interface AccountingCutOffSupplyChainService extends AccountingCutOffService {

  public Query<StockMove> getStockMoves(
      Company company, int accountingCutOffTypeSelect, LocalDate moveDate);

  @Transactional(rollbackOn = {Exception.class})
  public List<Move> generateCutOffMovesFromStockMove(
      StockMove stockMove,
      Journal miscOpeJournal,
      LocalDate moveDate,
      LocalDate reverseMoveDate,
      String moveDescription,
      String reverseMoveDescription,
      int accountingCutOffTypeSelect,
      int cutOffMoveStatusSelect,
      boolean recoveredTax,
      boolean ati,
      boolean includeNotStockManagedProduct,
      boolean automaticReverse,
      boolean automaticReconcile)
      throws AxelorException;

  public Move generateCutOffMoveFromStockMove(
      StockMove stockMove,
      List<StockMoveLine> sortedStockMoveLine,
      Journal miscOpeJournal,
      LocalDate moveDate,
      LocalDate originDate,
      String moveDescription,
      int cutOffMoveStatusSelect,
      boolean isPurchase,
      boolean recoveredTax,
      boolean ati,
      boolean includeNotStockManagedProduct,
      boolean isReverse)
      throws AxelorException;

  @CallMethod
  List<Long> getStockMoveLines(Batch batch);
}
