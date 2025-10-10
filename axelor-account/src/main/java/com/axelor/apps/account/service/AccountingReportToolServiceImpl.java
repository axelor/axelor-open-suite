/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.AccountingReportType;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.db.repo.AccountingReportTypeRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.db.Query;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;

public class AccountingReportToolServiceImpl implements AccountingReportToolService {

  protected AccountingReportRepository accountingReportRepository;
  protected AccountingReportTypeRepository accountingReportTypeRepository;

  @Inject
  public AccountingReportToolServiceImpl(
      AccountingReportRepository accountingReportRepository,
      AccountingReportTypeRepository accountingReportTypeRepository) {
    this.accountingReportRepository = accountingReportRepository;
    this.accountingReportTypeRepository = accountingReportTypeRepository;
  }

  @Override
  public boolean isThereAlreadyDraftReportInPeriod(AccountingReport accountingReport) {
    return accountingReportRepository
            .all()
            .filter(
                "self.id != ?1 AND self.reportType = ?2 "
                    + "AND self.dateFrom <= ?3 AND self.dateTo >= ?4 AND self.statusSelect = 1",
                accountingReport.getId(),
                accountingReport.getReportType(),
                accountingReport.getDateFrom(),
                accountingReport.getDateTo())
            .count()
        > 0;
  }

  @Override
  public String getAccountingReportTypeIds(AccountingReport accountingReport, boolean isCustom) {
    String queryStr =
        String.format(
            "self.reportExportTypeSelect = :reportType AND self.typeSelect %s :typeCustom",
            isCustom ? "=" : "<>");

    Query<AccountingReportType> query =
        accountingReportTypeRepository
            .all()
            .filter(queryStr)
            .bind("reportType", AccountingReportTypeRepository.REPORT)
            .bind("typeCustom", AccountingReportRepository.REPORT_CUSTOM_STATE);

    Stream<AccountingReportType> accountingReportTypeStream = query.fetch().stream();

    Company company = accountingReport.getCompany();
    if (!isCustom && company != null) {
      accountingReportTypeStream =
          accountingReportTypeStream.filter(it -> company.equals(it.getCompany()));
    }

    Set<Company> companySet = accountingReport.getCompanySet();
    if (isCustom && CollectionUtils.isNotEmpty(companySet)) {
      accountingReportTypeStream =
          accountingReportTypeStream.filter(
              it ->
                  CollectionUtils.isNotEmpty(it.getCompanySet())
                      && it.getCompanySet().stream().anyMatch(companySet::contains));
    }

    return accountingReportTypeStream
        .map(AccountingReportType::getId)
        .map(Objects::toString)
        .collect(Collectors.joining(","));
  }
}
