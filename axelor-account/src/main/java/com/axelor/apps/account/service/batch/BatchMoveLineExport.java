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
package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.MoveLineExportService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchMoveLineExport extends BatchStrategy {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected boolean end = false;

  protected long moveLineDone = 0;
  protected long moveDone = 0;
  protected BigDecimal debit = BigDecimal.ZERO;
  protected BigDecimal credit = BigDecimal.ZERO;
  protected BigDecimal balance = BigDecimal.ZERO;

  protected AccountingReportRepository accountingReportRepository;

  @Inject
  public BatchMoveLineExport(
      MoveLineExportService moveLineExportService,
      AccountingReportRepository accountingReportRepository) {

    super(moveLineExportService);

    this.accountingReportRepository = accountingReportRepository;
  }

  @Override
  protected void start() throws IllegalAccessException {

    super.start();

    try {

      this.testAccountingBatchField();

    } catch (AxelorException e) {

      TraceBackService.trace(
          new AxelorException(e, e.getCategory(), ""),
          ExceptionOriginRepository.MOVE_LINE_EXPORT_ORIGIN,
          batch.getId());
      incrementAnomaly();
      end = true;
    }

    checkPoint();
  }

  @Override
  protected void process() {

    if (!end) {
      try {
        Company company = batch.getAccountingBatch().getCompany();
        LocalDate startDate = batch.getAccountingBatch().getStartDate();
        LocalDate endDate = batch.getAccountingBatch().getEndDate();
        int exportTypeSelect = batch.getAccountingBatch().getMoveLineExportTypeSelect();

        AccountingReport accountingReport =
            moveLineExportService.createAccountingReport(
                company, exportTypeSelect, startDate, endDate);
        moveLineExportService.exportMoveLine(accountingReport);

        JPA.clear();

        accountingReport = accountingReportRepository.find(accountingReport.getId());

        moveLineDone =
            moveLineRepo.all().filter("self.move.accountingReport = ?1", accountingReport).count();
        moveDone = moveRepo.all().filter("self.accountingReport = ?1", accountingReport).count();
        debit = accountingReport.getTotalDebit();
        credit = accountingReport.getTotalCredit();
        balance = accountingReport.getBalance();

        updateAccountingReport(accountingReport);

      } catch (AxelorException e) {

        TraceBackService.trace(
            new AxelorException(e, e.getCategory(), String.format("%s", e)),
            ExceptionOriginRepository.MOVE_LINE_EXPORT_ORIGIN,
            batch.getId());
        incrementAnomaly();

      } catch (Exception e) {

        TraceBackService.trace(
            new Exception(String.format("%s", e), e),
            ExceptionOriginRepository.MOVE_LINE_EXPORT_ORIGIN,
            batch.getId());

        incrementAnomaly();

        log.error("Bug(Anomalie) généré(e) pour le batch {}", batch.getId());
      }
    }
  }

  public void testAccountingBatchField() throws AxelorException {
    AccountingBatch accountingBatch = batch.getAccountingBatch();
    if (accountingBatch.getCompany() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.BATCH_MOVELINE_EXPORT_1),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          accountingBatch.getCode());
    }
    if (accountingBatch.getEndDate() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.BATCH_MOVELINE_EXPORT_2),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          accountingBatch.getCode());
    }
    if (accountingBatch.getMoveLineExportTypeSelect() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.BATCH_MOVELINE_EXPORT_3),
          I18n.get(BaseExceptionMessage.EXCEPTION),
          accountingBatch.getCode());
    }
  }

  /**
   * As {@code batch} entity can be detached from the session, call {@code Batch.find()} get the
   * entity in the persistant context. Warning : {@code batch} entity have to be saved before.
   */
  @Override
  protected void stop() {

    String comment = I18n.get(AccountExceptionMessage.BATCH_MOVELINE_EXPORT_4) + "\n";
    comment +=
        String.format(
            "\t* %s (%s)" + I18n.get(AccountExceptionMessage.BATCH_MOVELINE_EXPORT_5) + "\n",
            moveLineDone,
            moveDone);
    comment += String.format("\t* " + I18n.get("Debit") + " : %s\n", debit);
    comment += String.format("\t* " + I18n.get("Credit") + " : %s\n", credit);
    comment += String.format("\t* " + I18n.get("Balance") + " : %s\n", balance);
    comment +=
        String.format(
            "\t" + I18n.get(BaseExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());

    super.stop();
    addComment(comment);
  }
}
