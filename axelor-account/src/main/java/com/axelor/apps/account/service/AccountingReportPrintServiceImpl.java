package com.axelor.apps.account.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountingReportPrintServiceImpl implements AccountingReportPrintService {

  protected AppBaseService appBaseService;
  protected AccountingReportRepository accountingReportRepository;

  @Inject
  public AccountingReportPrintServiceImpl(
      AppBaseService appBaseService, AccountingReportRepository accountingReportRepository) {
    this.appBaseService = appBaseService;
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
      file =
          String.format(
              IReport.ACCOUNTING_REPORT_TYPE, accountingReport.getReportType().getTypeSelect());
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
}
