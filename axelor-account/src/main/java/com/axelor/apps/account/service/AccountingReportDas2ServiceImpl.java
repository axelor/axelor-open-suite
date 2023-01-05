/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.tool.StringHTMLListBuilder;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import javax.persistence.Query;
import org.apache.commons.collections.CollectionUtils;

public class AccountingReportDas2ServiceImpl implements AccountingReportDas2Service {

  protected AccountingReportRepository accountingReportRepo;
  protected AccountConfigService accountConfigService;
  protected AccountingReportMoveLineService accountingReportMoveLineService;
  protected AccountingReportDas2CheckService accountingReportDas2CheckService;
  protected AccountingReportPrintService accountingReportPrintService;

  @Inject
  public AccountingReportDas2ServiceImpl(
      AccountingReportRepository accountingReportRepo,
      AccountConfigService accountConfigService,
      AccountingReportMoveLineService accountingReportMoveLineService,
      AccountingReportDas2CheckService accountingReportDas2CheckService,
      AccountingReportPrintService accountingReportPrintService) {
    this.accountingReportRepo = accountingReportRepo;
    this.accountConfigService = accountConfigService;
    this.accountingReportMoveLineService = accountingReportMoveLineService;
    this.accountingReportDas2CheckService = accountingReportDas2CheckService;
    this.accountingReportPrintService = accountingReportPrintService;
  }

  @Override
  public String printPreparatoryProcessDeclaration(AccountingReport accountingReport)
      throws AxelorException {

    processAccountingReportMoveLines(accountingReport);

    return accountingReportPrintService.print(accountingReport);
  }

  @Override
  public MetaFile exportN4DSFile(AccountingReport accountingReport)
      throws AxelorException, IOException {
    // check mandatory datas
    List<String> errorList =
        accountingReportDas2CheckService.checkMandatoryDataForDas2Export(accountingReport);

    if (!CollectionUtils.isEmpty(errorList)) {
      StringHTMLListBuilder errorMessageBuilder = new StringHTMLListBuilder();
      errorList.forEach(errorMessageBuilder::append);
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, errorMessageBuilder.toString());
    }

    // If all controls ok, export file
    return launchN4DSExport(accountingReport);
  }

  protected void processAccountingReportMoveLines(AccountingReport accountingReport) {

    accountingReport.clearAccountingReportMoveLineList();

    List<BigInteger> paymentMoveLineDistributioneIds =
        getAccountingReportDas2Pieces(accountingReport);
    accountingReportMoveLineService.createAccountingReportMoveLines(
        paymentMoveLineDistributioneIds, accountingReport);
  }

  @Override
  public boolean isThereAlreadyDas2ExportInPeriod(AccountingReport accountingReport) {

    return accountingReportRepo
            .all()
            .filter(
                "self.reportType.typeSelect = ?1 "
                    + "AND self.dateFrom <= ?2 AND self.dateTo >= ?3 AND self.statusSelect = 2 AND self.exported = ?4",
                AccountingReportRepository.EXPORT_N4DS,
                accountingReport.getDateFrom(),
                accountingReport.getDateTo(),
                true)
            .count()
        > 0;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<BigInteger> getAccountingReportDas2Pieces(AccountingReport accountingReport) {

    String queryStr =
        "WITH PARTNERS AS (SELECT PARTNER.ID AS ID "
            + "FROM ACCOUNT_PAYMENT_MOVE_LINE_DISTRIBUTION PMVLD "
            + "LEFT OUTER JOIN ACCOUNT_RECONCILE RECONCILE ON PMVLD.RECONCILE = RECONCILE.ID "
            + "LEFT OUTER JOIN ACCOUNT_MOVE_LINE MOVELINE ON PMVLD.MOVE_LINE = MOVELINE.ID "
            + "LEFT OUTER JOIN ACCOUNT_MOVE MOVE ON PMVLD.MOVE = MOVE.ID "
            + "LEFT OUTER JOIN ACCOUNT_JOURNAL JOURNAL ON MOVE.JOURNAL = JOURNAL.ID "
            + "LEFT OUTER JOIN ACCOUNT_JOURNAL_TYPE JOURNAL_TYPE ON JOURNAL.JOURNAL_TYPE = JOURNAL_TYPE.ID "
            + "LEFT OUTER JOIN BASE_PARTNER PARTNER ON MOVE.PARTNER = PARTNER.ID "
            + "LEFT OUTER JOIN BASE_COMPANY COMPANY ON MOVE.COMPANY = COMPANY.ID "
            + "LEFT OUTER JOIN BASE_CURRENCY CURRENCY ON MOVE.COMPANY_CURRENCY = CURRENCY.ID "
            + "LEFT OUTER JOIN ACCOUNT_MOVE_LINE DMOVE_LINE ON RECONCILE.DEBIT_MOVE_LINE = DMOVE_LINE.ID "
            + "LEFT OUTER JOIN ACCOUNT_MOVE DMOVE ON DMOVE_LINE.MOVE = DMOVE.ID "
            + "WHERE RECONCILE.STATUS_SELECT IN (2,3)  "
            + "AND DMOVE.STATUS_SELECT = 3 "
            + "AND MOVE.STATUS_SELECT = 3 "
            + "AND PMVLD.OPERATION_DATE >= '"
            + accountingReport.getDateFrom()
            + "' "
            + "AND PMVLD.OPERATION_DATE <= '"
            + accountingReport.getDateTo()
            + "' "
            + "AND MOVELINE.SERVICE_TYPE IS NOT NULL  "
            + "AND MOVELINE.DAS2ACTIVITY IS NOT NULL  "
            + "AND JOURNAL_TYPE.CODE = 'ACH' "
            + "AND COMPANY.ID = "
            + accountingReport.getCompany().getId()
            + " AND CURRENCY.ID =  "
            + accountingReport.getCurrency().getId()
            + " AND MOVE.IGNORE_IN_ACCOUNTING_OK != true "
            + "AND PMVLD.ID NOT IN (SELECT PMVLD.ID "
            + "FROM ACCOUNT_ACCOUNTING_REPORT_MOVE_LINE HISTORY "
            + "LEFT OUTER JOIN ACCOUNT_PAYMENT_MOVE_LINE_DISTRIBUTION PMVLD ON HISTORY.PAYMENT_MOVE_LINE_DISTRIBUTION = PMVLD.ID "
            + "LEFT OUTER JOIN ACCOUNT_ACCOUNTING_REPORT REPORT ON HISTORY.ACCOUNTING_REPORT = REPORT.ID "
            + "LEFT OUTER JOIN ACCOUNT_ACCOUNTING_REPORT_TYPE REPORT_TYPE ON REPORT.REPORT_TYPE = REPORT_TYPE.ID "
            + "WHERE  "
            + "ACCOUNTING_REPORT != "
            + accountingReport.getId()
            + " AND REPORT_TYPE.TYPE_SELECT = "
            + accountingReport.getReportType().getTypeSelect()
            + " AND (HISTORY.EXCLUDE_FROM_DAS2REPORT != true OR HISTORY.EXPORTED != true) ) "
            + "GROUP BY PARTNER.ID "
            + "HAVING SUM(PMVLD.IN_TAX_PRORATED_AMOUNT) >= "
            + accountingReport.getMinAmountExcl()
            + " ) "
            + "SELECT PMVLD.ID AS PAYMENTMVLD "
            + "FROM ACCOUNT_PAYMENT_MOVE_LINE_DISTRIBUTION PMVLD "
            + "LEFT OUTER JOIN ACCOUNT_RECONCILE RECONCILE ON PMVLD.RECONCILE = RECONCILE.ID "
            + "LEFT OUTER JOIN ACCOUNT_MOVE_LINE MOVELINE ON PMVLD.MOVE_LINE = MOVELINE.ID "
            + "LEFT OUTER JOIN ACCOUNT_MOVE MOVE ON PMVLD.MOVE = MOVE.ID "
            + "LEFT OUTER JOIN ACCOUNT_JOURNAL JOURNAL ON MOVE.JOURNAL = JOURNAL.ID "
            + "LEFT OUTER JOIN ACCOUNT_JOURNAL_TYPE JOURNAL_TYPE ON JOURNAL.JOURNAL_TYPE = JOURNAL_TYPE.ID "
            + "LEFT OUTER JOIN BASE_PARTNER PARTNER ON MOVE.PARTNER = PARTNER.ID "
            + "LEFT OUTER JOIN BASE_COMPANY COMPANY ON MOVE.COMPANY = COMPANY.ID "
            + "LEFT OUTER JOIN BASE_CURRENCY CURRENCY ON MOVE.COMPANY_CURRENCY = CURRENCY.ID "
            + "LEFT OUTER JOIN ACCOUNT_MOVE_LINE DMOVE_LINE ON RECONCILE.DEBIT_MOVE_LINE = DMOVE_LINE.ID "
            + "LEFT OUTER JOIN ACCOUNT_MOVE DMOVE ON DMOVE_LINE.MOVE = DMOVE.ID "
            + "WHERE RECONCILE.STATUS_SELECT IN (2,3)  "
            + "AND DMOVE.STATUS_SELECT = 3 "
            + "AND MOVE.STATUS_SELECT = 3 "
            + "AND PMVLD.OPERATION_DATE >= '"
            + accountingReport.getDateFrom()
            + "' "
            + "AND PMVLD.OPERATION_DATE <= '"
            + accountingReport.getDateTo()
            + "' "
            + "AND MOVELINE.SERVICE_TYPE IS NOT NULL  "
            + "AND MOVELINE.DAS2ACTIVITY IS NOT NULL  "
            + "AND JOURNAL_TYPE.CODE = 'ACH' "
            + "AND COMPANY.ID = "
            + accountingReport.getCompany().getId()
            + " AND CURRENCY.ID =  "
            + accountingReport.getCurrency().getId()
            + " AND MOVE.IGNORE_IN_ACCOUNTING_OK != true "
            + "AND PMVLD.ID NOT IN (SELECT PMVLD.ID "
            + "FROM ACCOUNT_ACCOUNTING_REPORT_MOVE_LINE HISTORY "
            + "LEFT OUTER JOIN ACCOUNT_PAYMENT_MOVE_LINE_DISTRIBUTION PMVLD ON HISTORY.PAYMENT_MOVE_LINE_DISTRIBUTION = PMVLD.ID "
            + "LEFT OUTER JOIN ACCOUNT_ACCOUNTING_REPORT REPORT ON HISTORY.ACCOUNTING_REPORT = REPORT.ID "
            + "LEFT OUTER JOIN ACCOUNT_ACCOUNTING_REPORT_TYPE REPORT_TYPE ON REPORT.REPORT_TYPE = REPORT_TYPE.ID "
            + "WHERE  "
            + "ACCOUNTING_REPORT != "
            + accountingReport.getId()
            + " AND REPORT_TYPE.TYPE_SELECT = "
            + accountingReport.getReportType().getTypeSelect()
            + " AND (HISTORY.EXCLUDE_FROM_DAS2REPORT != true OR HISTORY.EXPORTED != true) ) "
            + "AND PARTNER.ID IN (SELECT ID FROM PARTNERS) ";

    Query query = JPA.em().createNativeQuery(queryStr);

    return query.getResultList();
  }

  protected MetaFile launchN4DSExport(AccountingReport accountingExport)
      throws AxelorException, IOException {

    // TODO manage sequence in fileNaming
    String fileName = "N4DS_" + accountingExport.getCompany().getCode() + ".txt";
    MetaFile metaFile =
        accountingReportMoveLineService.generateN4DSFile(accountingExport, fileName);
    setExported(accountingExport);
    accountingReportMoveLineService.updateN4DSExportStatus(accountingExport);

    return metaFile;
  }

  @Transactional
  protected void setExported(AccountingReport accountingReport) {
    accountingReport.setExported(true);
    accountingReportRepo.save(accountingReport);
  }

  public AccountingReport getAssociatedDas2Export(AccountingReport accountingReport) {

    if (CollectionUtils.isEmpty(accountingReport.getAccountingReportMoveLineList())) {
      return null;
    }
    return accountingReport.getAccountingReportMoveLineList().get(0).getAccountingExport();
  }
}
