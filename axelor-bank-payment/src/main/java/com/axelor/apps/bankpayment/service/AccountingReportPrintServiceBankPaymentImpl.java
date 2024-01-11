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
package com.axelor.apps.bankpayment.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.service.AccountingReportPrintServiceImpl;
import com.axelor.apps.account.service.custom.AccountingReportValueService;
import com.axelor.apps.bankpayment.report.IReport;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.report.engine.ReportSettings;
import com.google.inject.Inject;

public class AccountingReportPrintServiceBankPaymentImpl extends AccountingReportPrintServiceImpl {

  @Inject
  public AccountingReportPrintServiceBankPaymentImpl(
      AppBaseService appBaseService,
      AccountingReportValueService accountingReportValueService,
      AccountingReportRepository accountingReportRepository) {
    super(appBaseService, accountingReportValueService, accountingReportRepository);
  }

  @Override
  public String getReportFileLink(AccountingReport accountingReport, String name)
      throws AxelorException {
    if (accountingReport.getReportType().getTypeSelect()
        == AccountingReportRepository.REPORT_BANK_RECONCILIATION_STATEMENT) {
      return ReportFactory.createReport(IReport.BANK_PAYMENT_REPORT_TYPE, name + "-${date}")
          .addParam("AccountingReportId", accountingReport.getId())
          .addParam("Locale", ReportSettings.getPrintingLocale(null))
          .addFormat(accountingReport.getExportTypeSelect())
          .toAttach(accountingReport)
          .generate()
          .getFileLink();
    } else {
      return super.getReportFileLink(accountingReport, name);
    }
  }
}
