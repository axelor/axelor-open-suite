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

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportType;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.db.repo.AccountingReportTypeRepository;
import com.axelor.apps.account.service.AccountingReportService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class BatchPrintAccountingReportServiceImpl implements BatchPrintAccountingReportService {

  protected AppAccountService appAccountService;
  protected AccountingReportService accountingReportService;
  protected AccountingReportRepository accountingReportRepo;
  protected AccountingReportTypeRepository accountingReportTypeRepo;
  protected AccountConfigService accountConfigService;
  protected AccountRepository accountRepository;

  @Inject
  public BatchPrintAccountingReportServiceImpl(
      AppAccountService appAccountService,
      AccountingReportService accountingReportService,
      AccountingReportRepository accountingReportRepo,
      AccountingReportTypeRepository accountingReportTypeRepo,
      AccountConfigService accountConfigService,
      AccountRepository accountRepository) {
    this.appAccountService = appAccountService;
    this.accountingReportService = accountingReportService;
    this.accountingReportRepo = accountingReportRepo;
    this.accountingReportTypeRepo = accountingReportTypeRepo;
    this.accountConfigService = accountConfigService;
    this.accountRepository = accountRepository;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public AccountingReport createAccountingReportFromBatch(AccountingBatch accountingBatch)
      throws AxelorException {
    AccountingReport accountingReport = new AccountingReport();
    accountingReport.setCompany(accountingBatch.getCompany());
    if (accountingReport.getCompany() != null) {
      accountingReport.setCurrency(accountingReport.getCompany().getCurrency());
      AccountConfig accountConfig =
          accountConfigService.getAccountConfig(accountingReport.getCompany());
      if (accountConfig != null) {
        accountingReport.setJournal(accountConfig.getReportedBalanceJournal());
      }
    }
    if (accountingBatch.getYear() != null) {
      accountingReport.setDateFrom(accountingBatch.getYear().getReportedBalanceDate());
      accountingReport.setDate(accountingBatch.getYear().getReportedBalanceDate());
    }
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

    accountingReport.setDisplayClosingAccountingMoves(accountingBatch.getCloseYear());
    accountingReport.setDisplayOpeningAccountingMoves(accountingBatch.getOpenYear());

    accountingReport.setExcludeViewAccount(true);
    accountingReport.setExcludeCommitmentSpecialAccount(
        !accountingBatch.getIncludeSpecialAccounts());

    accountingReportService.buildQuery(accountingReport);

    BigDecimal debitBalance = accountingReportService.getDebitBalance();
    BigDecimal creditBalance = accountingReportService.getCreditBalance();

    accountingReport.setTotalDebit(debitBalance);
    accountingReport.setTotalCredit(creditBalance);
    accountingReport.setBalance(debitBalance.subtract(creditBalance));
    return accountingReportRepo.save(accountingReport);
  }
}
