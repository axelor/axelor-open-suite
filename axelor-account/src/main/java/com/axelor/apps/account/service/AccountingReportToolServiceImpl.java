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
                "self.id != ?1 AND self.reportType = ?2 "
                    + "AND self.dateFrom <= ?3 AND self.dateTo >= ?4 AND self.statusSelect = 1",
                accountingReport.getId(),
                accountingReport.getReportType(),
                accountingReport.getDateFrom(),
                accountingReport.getDateTo())
            .count()
        > 0;
  }
}
