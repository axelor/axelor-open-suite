/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import jakarta.inject.Inject;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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
  public String getAccountingReportTypeIds(AccountingReport accountingReport) {
    Set<Company> companySet = accountingReport.getCompanySet();
    boolean isMultiCompany = CollectionUtils.isNotEmpty(companySet) && companySet.size() > 1;
    Company company = accountingReport.getCompany();

    String queryStr = "self.reportExportTypeSelect = :reportType";
    if (isMultiCompany) {
      queryStr +=
          " AND self.typeSelect = :typeCustom"
              + " AND EXISTS (SELECT c FROM Company c WHERE c MEMBER OF self.companySet AND c IN :companySet)";
    } else if (company != null) {
      queryStr += " AND (self.company = :company OR :company MEMBER OF self.companySet)";
    }

    Query<AccountingReportType> query =
        accountingReportTypeRepository
            .all()
            .filter(queryStr)
            .bind("reportType", AccountingReportTypeRepository.REPORT);
    if (isMultiCompany) {
      query =
          query
              .bind("typeCustom", AccountingReportRepository.REPORT_CUSTOM_STATE)
              .bind("companySet", companySet);
    } else if (company != null) {
      query = query.bind("company", company);
    }

    return query.fetch().stream()
        .map(AccountingReportType::getId)
        .map(Objects::toString)
        .collect(Collectors.joining(","));
  }
}
