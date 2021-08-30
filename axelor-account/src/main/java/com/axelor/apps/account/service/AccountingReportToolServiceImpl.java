package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.google.inject.Inject;

public class AccountingReportToolServiceImpl implements AccountingReportToolService {

  protected AccountingReportRepository accountingReportRepository;

  @Inject
  public AccountingReportToolServiceImpl(AccountingReportRepository accountingReportRepository) {
    this.accountingReportRepository = accountingReportRepository;
  }

  @Override
  public boolean isThereAlreadyDraftReportInPeriod(AccountingReport accountingReport) {
    return accountingReportRepository
            .all()
            .filter(
                "self.id != ?1 AND self.reportType.typeSelect = ?2 "
                    + "AND self.dateFrom <= ?3 AND self.dateTo >= ?4 AND self.statusSelect = 1",
                accountingReport.getId(),
                accountingReport.getReportType().getTypeSelect(),
                accountingReport.getDateFrom(),
                accountingReport.getDateTo())
            .count()
        > 0;
  }
}
