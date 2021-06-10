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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface AccountingReportMoveLineService {

  public List<MoveLine> getInTaxMoveLines(Move move);

  public BigDecimal getReconcileAmountInPeriod(
      MoveLine moveLine, LocalDate fromDate, LocalDate toDate);

  public List<MoveLine> getMoveLinesToReport(
      Move move,
      BigDecimal reconcileAmount,
      BigDecimal inTaxTotal,
      AccountingReport accountingReport);

  public void processMoveLinesToDisplay(
      List<MoveLine> moveLinesToProcess,
      boolean toReport,
      BigDecimal reconcileAmount,
      BigDecimal inTaxTotal,
      AccountingReport accountingReport);

  public AccountingReportMoveLine createAccountingReportMoveLine(
      MoveLine moveLine, AccountingReport accountingReport);

  public AccountingReport processExportMoveLine(
      AccountingReportMoveLine reportMoveLine, AccountingReport accountingReport);

  public BigDecimal computeAmountToReport(
      MoveLine moveLine, BigDecimal reconcileAmount, BigDecimal inTaxTotal);
}
