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
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.AccountingReportMoveLineRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class AccountingReportMoveLineServiceImpl implements AccountingReportMoveLineService {

  protected AccountingReportMoveLineRepository accountingReportMoveLineRepo;

  protected ReconcileService reconcileService;

  @Inject
  public AccountingReportMoveLineServiceImpl(
      AccountingReportMoveLineRepository accountingReportMoveLineRepo,
      ReconcileService reconcileService) {
    this.accountingReportMoveLineRepo = accountingReportMoveLineRepo;
    this.reconcileService = reconcileService;
  }

  @Override
  public List<MoveLine> getInTaxMoveLines(Move move) {

    List<MoveLine> moveLines = Lists.newArrayList();
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLine.getAccount().getReconcileOk()) {
        moveLines.add(moveLine);
      }
    }
    return moveLines;
  }

  @Override
  public BigDecimal getReconcileAmountInPeriod(
      MoveLine moveLine, LocalDate fromDate, LocalDate toDate) {

    BigDecimal reconcileAmount = BigDecimal.ZERO;
    for (Reconcile reconcile : reconcileService.getReconciles(moveLine)) {
      if ((reconcile.getStatusSelect().equals(ReconcileRepository.STATUS_CONFIRMED)
              && reconcile.getReconciliationDate().isAfter(fromDate)
              && reconcile.getReconciliationDate().isBefore(toDate))
          || (reconcile.getStatusSelect().equals(ReconcileRepository.STATUS_CANCELED)
              && reconcile.getReconciliationCancelDate().isAfter(fromDate)
              && reconcile.getReconciliationCancelDate().isBefore(toDate))) {
        reconcileAmount = reconcileAmount.add(reconcile.getAmount());
      }
    }
    return reconcileAmount;
  }

  @Override
  public List<MoveLine> getMoveLinesToReport(
      Move move,
      BigDecimal reconcileAmount,
      BigDecimal inTaxTotal,
      AccountingReport accountingReport) {

    List<MoveLine> moveLines = Lists.newArrayList();
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (!moveLine.getAccount().getReconcileOk()
          && moveLine.getServiceType() != null
          && moveLine.getDas2Activity() != null
          && moveLine.getServiceType().getIsDas2Declarable()) {

        BigDecimal amountToReport = computeAmountToReport(moveLine, reconcileAmount, inTaxTotal);

        if (accountingReportMoveLineRepo
                .all()
                .filter(
                    "self.moveLine = ?1 AND self.accountingReport.reportType.typeSelect = ?2 AND self.excludeFromDas2Report is not true "
                        + "AND (self.partiallyReconciliated is not true OR "
                        + "(self.partiallyReconciliated is true "
                        + "AND self.amountToReport = ?3 "
                        + "AND self.accountingReport.dateFrom >= ?4 "
                        + "AND self.accountingReport.dateTo <= ?5))",
                    moveLine,
                    accountingReport.getReportType().getTypeSelect(),
                    amountToReport,
                    accountingReport.getDateFrom(),
                    accountingReport.getDateTo())
                .count()
            > 0) {
          continue;
        }
        moveLines.add(moveLine);
      }
    }
    return moveLines;
  }

  @Override
  @Transactional
  public void processMoveLinesToDisplay(
      List<MoveLine> moveLinesToProcess,
      boolean toReport,
      BigDecimal reconcileAmount,
      BigDecimal inTaxTotal,
      AccountingReport accountingReport) {

    for (MoveLine moveLine : moveLinesToProcess) {
      AccountingReportMoveLine accountingReportMoveLine =
          createAccountingReportMoveLine(moveLine, accountingReport);

      if (toReport) {
        accountingReportMoveLine.setAmountToReport(
            computeAmountToReport(moveLine, reconcileAmount, inTaxTotal));
        if (reconcileAmount.compareTo(inTaxTotal) != 0) {
          accountingReportMoveLine.setPartiallyReconciliated(true);
        }
      }

      accountingReportMoveLineRepo.save(accountingReportMoveLine);
    }
  }

  @Override
  public AccountingReportMoveLine createAccountingReportMoveLine(
      MoveLine moveLine, AccountingReport accountingReport) {

    AccountingReportMoveLine accountingReportMoveLine =
        new AccountingReportMoveLine(moveLine.getPartner(), moveLine);
    accountingReportMoveLine.setAccountingReport(accountingReport);

    return accountingReportMoveLine;
  }

  @Override
  @Transactional
  public AccountingReport processExportMoveLine(
      AccountingReportMoveLine reportMoveLine, AccountingReport accountingReport) {

    AccountingReportMoveLine exportMoveLine =
        createAccountingReportMoveLine(reportMoveLine.getMoveLine(), accountingReport);
    exportMoveLine.setAmountToReport(reportMoveLine.getAmountToReport());
    exportMoveLine.setPartiallyReconciliated(reportMoveLine.getPartiallyReconciliated());
    accountingReportMoveLineRepo.save(exportMoveLine);
    return accountingReport;
  }

  @Override
  public BigDecimal computeAmountToReport(
      MoveLine moveLine, BigDecimal reconcileAmount, BigDecimal inTaxTotal) {

    return moveLine
        .getCredit()
        .add(moveLine.getDebit())
        .multiply(reconcileAmount)
        .divide(inTaxTotal, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
  }
}
