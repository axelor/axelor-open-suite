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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.factory.CancelFactory;
import com.axelor.apps.account.service.invoice.factory.ValidateFactory;
import com.axelor.apps.account.service.invoice.factory.VentilateFactory;
import com.axelor.apps.account.service.invoice.print.InvoiceProductStatementService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.businessproject.report.IReport;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.service.IntercoService;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychainImpl;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class InvoiceServiceProjectImpl extends InvoiceServiceSupplychainImpl
    implements InvoiceServiceProject {

  @Inject
  public InvoiceServiceProjectImpl(
      ValidateFactory validateFactory,
      VentilateFactory ventilateFactory,
      CancelFactory cancelFactory,
      AlarmEngineService<Invoice> alarmEngineService,
      InvoiceRepository invoiceRepo,
      AppAccountService appAccountService,
      PartnerService partnerService,
      InvoiceLineService invoiceLineService,
      AccountConfigService accountConfigService,
      MoveToolService moveToolService,
      InvoiceTermService invoiceTermService,
      InvoiceTermPfpService invoiceTermPfpService,
      AppBaseService appBaseService,
      TaxService taxService,
      InvoiceProductStatementService invoiceProductStatementService,
      TemplateMessageService templateMessageService,
      InvoiceLineRepository invoiceLineRepo,
      IntercoService intercoService,
      StockMoveRepository stockMoveRepository) {
    super(
        validateFactory,
        ventilateFactory,
        cancelFactory,
        alarmEngineService,
        invoiceRepo,
        appAccountService,
        partnerService,
        invoiceLineService,
        accountConfigService,
        moveToolService,
        invoiceTermService,
        invoiceTermPfpService,
        appBaseService,
        taxService,
        invoiceProductStatementService,
        templateMessageService,
        invoiceLineRepo,
        intercoService,
        stockMoveRepository);
  }

  public List<String> editInvoiceAnnex(Invoice invoice, String invoiceIds, boolean toAttach)
      throws AxelorException {

    if (invoice.getPrintingSettings() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          String.format(
              I18n.get(AccountExceptionMessage.INVOICE_MISSING_PRINTING_SETTINGS),
              invoice.getInvoiceId()),
          invoice);
    }

    if (Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null) != null
        && !AuthUtils.getUser()
            .getActiveCompany()
            .getAccountConfig()
            .getDisplayTimesheetOnPrinting()
        && !AuthUtils.getUser()
            .getActiveCompany()
            .getAccountConfig()
            .getDisplayExpenseOnPrinting()) {
      return null;
    }

    String language = ReportSettings.getPrintingLocale(invoice.getPartner());

    String title = I18n.get("Invoice");
    if (invoice.getInvoiceId() != null) {
      title += invoice.getInvoiceId();
    }

    Integer invoicesCopy = invoice.getInvoicesCopySelect();
    ReportSettings rS =
        ReportFactory.createReport(
            IReport.INVOICE_ANNEX, title + "-" + I18n.get("Annex") + "-${date}");

    if (toAttach) {
      rS.toAttach(invoice);
    }

    String fileLink =
        rS.addParam("InvoiceId", invoiceIds)
            .addParam("Locale", language)
            .addParam(
                "Timezone",
                invoice.getCompany() != null ? invoice.getCompany().getTimezone() : null)
            .addParam("InvoicesCopy", invoicesCopy)
            .addParam("HeaderHeight", invoice.getPrintingSettings().getPdfHeaderHeight())
            .addParam("FooterHeight", invoice.getPrintingSettings().getPdfFooterHeight())
            .generate()
            .getFileLink();

    List<String> res = Arrays.asList(title, fileLink);

    return res;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void cancel(Invoice invoice) throws AxelorException {
    super.cancel(invoice);
    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      for (AnalyticMoveLine analyticMoveLine : invoiceLine.getAnalyticMoveLineList()) {
        analyticMoveLine.setProject(null);
      }
    }
  }

  @Transactional
  public Invoice updateLines(Invoice invoice) {
    AnalyticMoveLineRepository analyticMoveLineRepository =
        Beans.get(AnalyticMoveLineRepository.class);
    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      invoiceLine.setProject(invoice.getProject());
      for (AnalyticMoveLine analyticMoveLine : invoiceLine.getAnalyticMoveLineList()) {
        analyticMoveLine.setProject(invoice.getProject());
        analyticMoveLineRepository.save(analyticMoveLine);
      }
    }
    return invoice;
  }
}
