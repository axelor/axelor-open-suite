package com.axelor.apps.bankpayment.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.service.AccountingReportServiceImpl;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.bankpayment.report.IReport;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class AccountingReportBankPaymentServiceImpl extends AccountingReportServiceImpl
    implements AccountingReportBankPaymentService {

  @Inject
  public AccountingReportBankPaymentServiceImpl(
      AppAccountService appBaseService,
      AccountingReportRepository accountingReportRepo,
      AccountRepository accountRepo) {
    super(appBaseService, accountingReportRepo, accountRepo);
  }

  @Override
  public String createDomainForBankDetails(AccountingReport accountingReport) {
    return Beans.get(BankDetailsService.class)
        .getActiveCompanyBankDetails(accountingReport.getCompany(), accountingReport.getCurrency());
  }

  @Override
  public String getReportFileLink(AccountingReport accountingReport, String name)
      throws AxelorException {
    if (accountingReport.getTypeSelect()
        >= AccountingReportRepository.REPORT_BANK_RECONCILIATION_STATEMENT) {
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
