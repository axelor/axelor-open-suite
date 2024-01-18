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

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.account.service.custom.AccountingReportValueService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.studio.app.service.AppService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountingReportPrintServiceImpl implements AccountingReportPrintService {

  protected AppBaseService appBaseService;
  protected AccountingReportValueService accountingReportValueService;
  protected AccountingReportRepository accountingReportRepository;

  @Inject
  public AccountingReportPrintServiceImpl(
      AppBaseService appBaseService,
      AccountingReportValueService accountingReportValueService,
      AccountingReportRepository accountingReportRepository) {
    this.appBaseService = appBaseService;
    this.accountingReportValueService = accountingReportValueService;
    this.accountingReportRepository = accountingReportRepository;
  }

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public String print(AccountingReport accountingReport) throws AxelorException {

    setPublicationDateTime(accountingReport);

    String name = computeName(accountingReport);

    String fileLink = getReportFileLink(accountingReport, name);

    logger.debug("Printing {}", name);

    return fileLink;
  }

  @Override
  public String computeName(AccountingReport accountingReport) {
    return accountingReport.getReportType().getName() + " " + accountingReport.getRef();
  }

  @Transactional
  protected void setPublicationDateTime(AccountingReport accountingReport) {
    accountingReport.setPublicationDateTime(appBaseService.getTodayDateTime());
    accountingReportRepository.save(accountingReport);
  }

  protected String getReportFileLink(AccountingReport accountingReport, String name)
      throws AxelorException {
    String file = "";
    if (accountingReport.getReportType().getTemplate() != null) {
      file =
          String.format(
              "%s/%s",
              AppService.getFileUploadDir(),
              accountingReport.getReportType().getTemplate().getFilePath());

    } else {
      final Integer typeSelect = accountingReport.getReportType().getTypeSelect();
      String typeSelectStr =
          typeSelect.equals(AccountingReportRepository.REPORT_GENERAL_BALANCE)
                  && accountingReport.getIsComparativeBalance()
              ? typeSelect + "_1"
              : typeSelect.toString();
      file = String.format(IReport.ACCOUNTING_REPORT_TYPE, typeSelectStr);
    }
    return ReportFactory.createReport(file, name + "-${date}")
        .addParam("AccountingReportId", accountingReport.getId())
        .addParam("Locale", ReportSettings.getPrintingLocale(null))
        .addParam(
            "Timezone",
            accountingReport.getCompany() != null
                ? accountingReport.getCompany().getTimezone()
                : null)
        .addFormat(accountingReport.getExportTypeSelect())
        .toAttach(accountingReport)
        .generate()
        .getFileLink();
  }

  @Override
  public String printCustomReport(AccountingReport accountingReport) throws AxelorException {
    String fileLink;
    accountingReportValueService.clearReportValues(accountingReport);

    try {
      accountingReportValueService.computeReportValues(accountingReport);
      accountingReport = accountingReportRepository.find(accountingReport.getId());

      fileLink = this.print(accountingReport);
    } catch (Exception e) {
      accountingReport = accountingReportRepository.find(accountingReport.getId());
      accountingReportValueService.clearReportValues(accountingReport);

      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }

    accountingReportValueService.clearReportValues(accountingReport);
    accountingReport = accountingReportRepository.find(accountingReport.getId());

    return fileLink;
  }
}
