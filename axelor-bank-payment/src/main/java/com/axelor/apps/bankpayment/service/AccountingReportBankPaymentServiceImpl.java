/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountingReportServiceImpl;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.bankpayment.report.IReport;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
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

  @Override
  public String getSequence(AccountingReport accountingReport) throws AxelorException {
    SequenceService sequenceService = Beans.get(SequenceService.class);
    int accountingReportTypeSelect = accountingReport.getTypeSelect();

    if (accountingReportTypeSelect >= 3000) {
      String seq =
          sequenceService.getSequenceNumber(
              SequenceRepository.ACCOUNTING_REPORT, accountingReport.getCompany());
      if (seq == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.ACCOUNTING_REPORT_1),
            I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.EXCEPTION),
            accountingReport.getCompany().getName());
      }
      return seq;
    } else {
      return super.getSequence(accountingReport);
    }
  }
}
