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
package com.axelor.apps.account.service.batch;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportType;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.db.repo.AccountingReportTypeRepository;
import com.axelor.apps.account.service.AccountingReportService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

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

  @Transactional
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

    Set<Account> accountSet =
        initializeAccountSet(
            accountingReport.getCompany(), accountingBatch.getIncludeSpecialAccounts());
    if (!CollectionUtils.isEmpty(accountSet)) {
      accountingReport.setAccountSet(accountSet);
    }

    accountingReportService.buildQuery(accountingReport);

    BigDecimal debitBalance = accountingReportService.getDebitBalance();
    BigDecimal creditBalance = accountingReportService.getCreditBalance();

    accountingReport.setTotalDebit(debitBalance);
    accountingReport.setTotalCredit(creditBalance);
    accountingReport.setBalance(debitBalance.subtract(creditBalance));
    return accountingReportRepo.save(accountingReport);
  }

  @Override
  public Set<Account> initializeAccountSet(Company company, boolean includeSpecialAccounts) {
    List<String> technicalTypeToExclude = new ArrayList();
    technicalTypeToExclude.add(AccountTypeRepository.TYPE_VIEW);
    if (!includeSpecialAccounts) {
      technicalTypeToExclude.add(AccountTypeRepository.TYPE_COMMITMENT);
      technicalTypeToExclude.add(AccountTypeRepository.TYPE_SPECIAL);
    }

    return new HashSet<>(
        accountRepository
            .all()
            .filter(
                "self.statusSelect = :statusActive AND self.company.id = :companyId AND self.accountType.technicalTypeSelect NOT IN :technicalTypes")
            .bind("statusActive", AccountRepository.STATUS_ACTIVE)
            .bind("companyId", company != null && company.getId() != null ? company.getId() : 0)
            .bind("technicalTypes", technicalTypeToExclude)
            .fetch());
  }
}
