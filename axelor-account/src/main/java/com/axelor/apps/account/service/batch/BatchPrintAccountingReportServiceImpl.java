package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportType;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.db.repo.AccountingReportTypeRepository;
import com.axelor.apps.account.service.AccountingReportService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class BatchPrintAccountingReportServiceImpl implements BatchPrintAccountingReportService {

  protected AppAccountService appAccountService;
  protected AccountingReportService accountingReportService;
  protected AccountingReportRepository accountingReportRepo;
  protected AccountingReportTypeRepository accountingReportTypeRepo;

  @Inject
  public BatchPrintAccountingReportServiceImpl(
      AppAccountService appAccountService,
      AccountingReportService accountingReportService,
      AccountingReportRepository accountingReportRepo,
      AccountingReportTypeRepository accountingReportTypeRepo) {
    this.appAccountService = appAccountService;
    this.accountingReportService = accountingReportService;
    this.accountingReportRepo = accountingReportRepo;
    this.accountingReportTypeRepo = accountingReportTypeRepo;
  }

  @Transactional
  @Override
  public AccountingReport createAccountingReportFromBatch(AccountingBatch accountingBatch)
      throws AxelorException {
    AccountingReport accountingReport = new AccountingReport();
    accountingReport.setCompany(accountingBatch.getCompany());
    if (accountingReport.getCompany() != null) {
      accountingReport.setCurrency(accountingReport.getCompany().getCurrency());
    }
    if (accountingBatch.getYear() != null) {
      accountingReport.setDateFrom(accountingBatch.getYear().getReportedBalanceDate());
    }
    accountingReport.setDate(appAccountService.getTodayDateTime().toLocalDate());
    accountingReport.setDateTo(accountingReport.getDate());
    accountingReport.setPeriod(accountingBatch.getPeriod());
    AccountingReportType accountingReportType = new AccountingReportType();
    accountingReportType.setTypeSelect(AccountingReportRepository.REPORT_GENERAL_BALANCE);
    accountingReport.setReportType(
        accountingReportTypeRepo.findByTypeSelect(
            AccountingReportRepository.REPORT_GENERAL_BALANCE));
    accountingReport.setExportTypeSelect("pdf");
    accountingReport.setRef(accountingReportService.getSequence(accountingReport));
    accountingReport.setStatusSelect(AccountingReportRepository.STATUS_DRAFT);
    accountingReportService.buildQuery(accountingReport);

    BigDecimal debitBalance = accountingReportService.getDebitBalance();
    BigDecimal creditBalance = accountingReportService.getCreditBalance();

    accountingReport.setTotalDebit(debitBalance);
    accountingReport.setTotalCredit(creditBalance);
    accountingReport.setBalance(debitBalance.subtract(creditBalance));
    accountingReportRepo.save(accountingReport);
    return accountingReport;
  }
}
