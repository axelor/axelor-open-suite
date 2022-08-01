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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.tool.StringHTMLListBuilder;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.ArrayList;
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

    List<Long> paymentMoveLineDistributioneIds = getAccountingReportDas2Pieces(accountingReport);
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
  public List<Long> getAccountingReportDas2Pieces(AccountingReport accountingReport) {

    AccountConfig accountConfig =
        Beans.get(AccountConfigRepository.class).findByCompany(accountingReport.getCompany());
    JournalType journalType = accountConfig.getDasReportJournalType();

    List<Long> partnerIds = new ArrayList<Long>();
    String sameQuery =
        "FROM PaymentMoveLineDistribution pmvld "
            + "LEFT OUTER JOIN pmvld.reconcile reconcile "
            + "LEFT OUTER JOIN pmvld.moveLine moveLine "
            + "LEFT OUTER JOIN pmvld.move move "
            + "LEFT OUTER JOIN move.journal journal "
            + "LEFT OUTER JOIN journal.journalType journalType "
            + "LEFT OUTER JOIN move.partner partner "
            + "LEFT OUTER JOIN move.company company "
            + "LEFT OUTER JOIN move.companyCurrency currency "
            + "LEFT OUTER JOIN reconcile.debitMoveLine dMoveLine "
            + "LEFT OUTER JOIN dMoveLine.move dMove "
            + "LEFT OUTER JOIN moveLine.account account "
            + "WHERE reconcile.statusSelect IN (?1, ?2)  " // (STATUS_CONFIRMED, STATUS_CANCELED)
            + "AND dMove.statusSelect = ?3 " // STATUS_VALIDATED
            + "AND move.statusSelect = ?3 " // STATUS_VALIDATED
            + "AND pmvld.operationDate >= ?4 " //  accountingReport.getDateFrom()
            + "AND pmvld.operationDate <= ?5 " // accountingReport.getDateTo()
            + "AND account.serviceType IS NOT NULL  "
            + "AND partner.das2Activity IS NOT NULL  "
            + "AND account.serviceType.isDas2Declarable IS TRUE "
            + "AND journalType = ?6 " // ACH
            + "AND company = ?7 " // accountingReport.getCompany()
            + "AND currency = ?8 " // accountingReport.getCurrency()
            + "AND move.ignoreInAccountingOk != true "
            + "AND pmvld NOT IN (SELECT pmvld "
            + "FROM AccountingReportMoveLine history "
            + "LEFT OUTER JOIN history.paymentMoveLineDistribution pmvld "
            + "LEFT OUTER JOIN history.accountingReport report "
            + "LEFT OUTER JOIN report.reportType reportType "
            + "WHERE report != ?9 " // accountingReport
            + "AND reportType.typeSelect = ?10 " // accountingReport.getReportType().getTypeSelect()
            + "AND (history.excludeFromDas2Report != true OR history.exported != true) ) ";

    String partnerQueryStr =
        "SELECT DISTINCT partner.id AS id "
            + sameQuery
            + "GROUP BY partner.id "
            + "HAVING SUM(pmvld.inTaxProratedAmount) >= ?11 "; // accountingReport.getMinAmountExcl()

    Query partnerQuery =
        JPA.em()
            .createQuery(partnerQueryStr)
            .setParameter(1, ReconcileRepository.STATUS_CONFIRMED)
            .setParameter(2, ReconcileRepository.STATUS_CANCELED)
            .setParameter(3, MoveRepository.STATUS_VALIDATED)
            .setParameter(4, accountingReport.getDateFrom())
            .setParameter(5, accountingReport.getDateTo())
            .setParameter(6, journalType)
            .setParameter(7, accountingReport.getCompany())
            .setParameter(8, accountingReport.getCurrency())
            .setParameter(9, accountingReport)
            .setParameter(10, accountingReport.getReportType().getTypeSelect())
            .setParameter(11, accountingReport.getMinAmountExcl());
    partnerIds = partnerQuery.getResultList();
    if (partnerIds == null || partnerIds.isEmpty()) {
      return new ArrayList<Long>();
    }

    // Check if one error persist
    String exclusionQuery =
        "AND NOT EXISTS (SELECT pmvld.id "
            + "FROM PaymentMoveLineDistribution pmvld "
            + "LEFT OUTER JOIN pmvld.reconcile reconcile "
            + "LEFT OUTER JOIN pmvld.moveLine moveLine "
            + "LEFT OUTER JOIN pmvld.move move1 "
            + "LEFT OUTER JOIN move1.journal journal "
            + "LEFT OUTER JOIN journal.journalType journalType "
            + "LEFT OUTER JOIN move1.partner partner "
            + "LEFT OUTER JOIN move1.company company "
            + "LEFT OUTER JOIN move1.companyCurrency currency "
            + "LEFT OUTER JOIN reconcile.debitMoveLine dMoveLine "
            + "LEFT OUTER JOIN dMoveLine.move dMove "
            + "LEFT OUTER JOIN moveLine.account account "
            + "WHERE reconcile.statusSelect IN (?1, ?2)  " // (STATUS_CONFIRMED, STATUS_CANCELED)
            + "AND dMove.statusSelect = ?3 " // STATUS_VALIDATED
            + "AND move1.statusSelect = ?3 " // STATUS_VALIDATED
            + "AND pmvld.operationDate >= ?4 " //  accountingReport.getDateFrom()
            + "AND pmvld.operationDate <= ?5 " // accountingReport.getDateTo()
            + "AND (account.serviceType IS NULL OR partner.das2Activity IS NULL) "
            + "AND journalType = ?6 " // ACH
            + "AND company = ?7 " // accountingReport.getCompany()
            + "AND currency = ?8 " // accountingReport.getCurrency()
            + "AND move1.ignoreInAccountingOk != true "
            + "AND move = move1 " // Check on the same move
            + "AND pmvld NOT IN (SELECT pmvld "
            + "FROM AccountingReportMoveLine history "
            + "LEFT OUTER JOIN history.paymentMoveLineDistribution pmvld "
            + "LEFT OUTER JOIN history.accountingReport report "
            + "LEFT OUTER JOIN report.reportType reportType "
            + "WHERE report != ?9 " // accountingReport
            + "AND reportType.typeSelect = ?10 " // accountingReport.getReportType().getTypeSelect()
            + "AND (history.excludeFromDas2Report != true OR history.exported != true) ) ) ";

    String pmvldQueryStr =
        "SELECT pmvld.id AS paymentMvld " + sameQuery + "AND partner.id IN ?11 " + exclusionQuery;

    Query query =
        JPA.em()
            .createQuery(pmvldQueryStr)
            .setParameter(1, ReconcileRepository.STATUS_CONFIRMED)
            .setParameter(2, ReconcileRepository.STATUS_CANCELED)
            .setParameter(3, MoveRepository.STATUS_VALIDATED)
            .setParameter(4, accountingReport.getDateFrom())
            .setParameter(5, accountingReport.getDateTo())
            .setParameter(6, journalType)
            .setParameter(7, accountingReport.getCompany())
            .setParameter(8, accountingReport.getCurrency())
            .setParameter(9, accountingReport)
            .setParameter(10, accountingReport.getReportType().getTypeSelect())
            .setParameter(11, partnerIds);
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
