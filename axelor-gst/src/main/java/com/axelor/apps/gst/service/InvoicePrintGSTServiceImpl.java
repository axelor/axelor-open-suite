package com.axelor.apps.gst.service;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.invoice.print.InvoicePrintServiceImpl;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.gst.report.IReport;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.time.format.DateTimeFormatter;

public class InvoicePrintGSTServiceImpl extends InvoicePrintServiceImpl {

  @Override
  public ReportSettings prepareReportSettings(Invoice invoice) throws AxelorException {

    if (invoice.getPrintingSettings() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          String.format(
              I18n.get(IExceptionMessage.INVOICE_MISSING_PRINTING_SETTINGS),
              invoice.getInvoiceId()),
          invoice);
    }
    String locale = ReportSettings.getPrintingLocale(invoice.getPartner());

    String title = I18n.get("InvoiceGST");
    if (invoice.getInvoiceId() != null) {
      title += " " + invoice.getInvoiceId();
    }

    ReportSettings reportSetting =
        ReportFactory.createReport(IReport.INVOICE_GST, title + " - ${date}");

    return reportSetting.addParam("InvoiceId", invoice.getId()).addParam("Locale", locale);
  }

  /**
   * Return the name for the printed invoice.
   *
   * @param plural if there is one or multiple invoices.
   */
  protected String getInvoiceFilesName(boolean plural) {

    return I18n.get(plural ? "InvoicesGST" : "InvoiceGST")
        + " - "
        + Beans.get(AppBaseService.class).getTodayDate().format(DateTimeFormatter.BASIC_ISO_DATE)
        + ".pdf";
  }
}
