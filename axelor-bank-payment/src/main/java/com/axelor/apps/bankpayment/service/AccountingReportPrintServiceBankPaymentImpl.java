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
package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.AccountingReport;
import com.axelor.apps.account.db.repo.AccountingReportRepository;
import com.axelor.apps.account.service.AccountingReportPrintServiceImpl;
import com.axelor.apps.account.service.custom.AccountingReportValueService;
import com.axelor.apps.bankpayment.service.config.BankPaymentConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.printing.template.PrintingTemplatePrintService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class AccountingReportPrintServiceBankPaymentImpl extends AccountingReportPrintServiceImpl {

  protected BankPaymentConfigService bankPaymentConfigService;
  protected PrintingTemplatePrintService printingTemplatePrintService;

  @Inject
  public AccountingReportPrintServiceBankPaymentImpl(
      AppBaseService appBaseService,
      AccountingReportValueService accountingReportValueService,
      AccountingReportRepository accountingReportRepository,
      BankPaymentConfigService bankPaymentConfigService,
      PrintingTemplatePrintService printingTemplatePrintService) {
    super(appBaseService, accountingReportValueService, accountingReportRepository);
    this.bankPaymentConfigService = bankPaymentConfigService;
    this.printingTemplatePrintService = printingTemplatePrintService;
  }

  @Override
  public String getReportFileLink(AccountingReport accountingReport, String name)
      throws AxelorException {
    if (accountingReport.getReportType().getTypeSelect()
        == AccountingReportRepository.REPORT_BANK_RECONCILIATION_STATEMENT) {

      PrintingTemplate bankReconciliationStatementPrintTemplate =
          bankPaymentConfigService
              .getBankPaymentConfig(accountingReport.getCompany())
              .getBankReconciliationStatementPrintTemplate();
      if (ObjectUtils.isEmpty(bankReconciliationStatementPrintTemplate)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.TEMPLATE_CONFIG_NOT_FOUND));
      }
      return printingTemplatePrintService.getPrintLink(
          bankReconciliationStatementPrintTemplate,
          new PrintingGenFactoryContext(accountingReport),
          name + "-${date}");
    } else {
      return super.getReportFileLink(accountingReport, name);
    }
  }
}
