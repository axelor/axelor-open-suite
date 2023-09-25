/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.invoice.print;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.birt.template.BirtTemplateService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.EntityHelper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.utils.ModelTool;
import com.axelor.utils.ThrowConsumer;
import com.axelor.utils.file.PdfTool;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.map.HashedMap;

/** Implementation of the service printing invoices. */
@Singleton
public class InvoicePrintServiceImpl implements InvoicePrintService {

  protected InvoiceRepository invoiceRepo;
  protected AccountConfigRepository accountConfigRepo;
  protected AppBaseService appBaseService;
  protected AccountConfigService accountConfigService;
  protected BirtTemplateService birtTemplateService;

  @Inject
  public InvoicePrintServiceImpl(
      InvoiceRepository invoiceRepo,
      AccountConfigRepository accountConfigRepo,
      AccountConfigService accountConfigService,
      AppBaseService appBaseService,
      BirtTemplateService birtTemplateService) {
    this.invoiceRepo = invoiceRepo;
    this.accountConfigRepo = accountConfigRepo;
    this.appBaseService = appBaseService;
    this.accountConfigService = accountConfigService;
    this.birtTemplateService = birtTemplateService;
  }

  @Override
  public String printInvoice(
      Invoice invoice, boolean forceRefresh, String format, Integer reportType, String locale)
      throws AxelorException, IOException {
    String fileName =
        I18n.get(InvoiceToolService.isRefund(invoice) ? "Refund" : "Invoice")
            + "-"
            + invoice.getInvoiceId()
            + "."
            + format;
    return PdfTool.getFileLinkFromPdfFile(
        printCopiesToFile(invoice, forceRefresh, reportType, format, locale), fileName);
  }

  @Override
  public File printCopiesToFile(
      Invoice invoice, boolean forceRefresh, Integer reportType, String format, String locale)
      throws AxelorException, IOException {
    File file = getPrintedInvoice(invoice, forceRefresh, reportType, format, locale);
    int copyNumber = invoice.getInvoicesCopySelect();
    copyNumber = copyNumber == 0 ? 1 : copyNumber;
    return format.equals(ReportSettings.FORMAT_PDF)
        ? PdfTool.printCopiesToFile(file, copyNumber)
        : file;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public File getPrintedInvoice(
      Invoice invoice, boolean forceRefresh, Integer reportType, String format, String locale)
      throws AxelorException {

    // if invoice is ventilated (or just validated for advance payment invoices)
    if (invoice.getStatusSelect() == InvoiceRepository.STATUS_VENTILATED
        || (invoice.getOperationSubTypeSelect() == InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE
            && invoice.getStatusSelect() == InvoiceRepository.STATUS_VALIDATED)) {

      // return a previously generated printing if possible
      if (!forceRefresh
          && invoice.getPrintedPDF() != null
          && reportType != null
          && reportType != InvoiceRepository.REPORT_TYPE_INVOICE_WITH_PAYMENTS_DETAILS) {

        Path path = MetaFiles.getPath(invoice.getPrintedPDF().getFilePath());
        return path.toFile();
      } else {

        // generate a new printing
        return reportType != null
                && reportType == InvoiceRepository.REPORT_TYPE_INVOICE_WITH_PAYMENTS_DETAILS
            ? print(invoice, reportType, format, locale)
            : printAndSave(invoice, reportType, format, locale);
      }
    } else {
      // invoice is not ventilated (or validated for advance payment invoices) --> generate and
      // don't save
      return print(invoice, reportType, format, locale);
    }
  }

  public File print(Invoice invoice, Integer reportType, String format, String locale)
      throws AxelorException {
    ReportSettings reportSettings = prepareReportSettings(invoice, reportType, format, locale);
    return reportSettings.generate().getFile();
  }

  public File printAndSave(Invoice invoice, Integer reportType, String format, String locale)
      throws AxelorException {

    ReportSettings reportSettings = prepareReportSettings(invoice, reportType, format, locale);
    MetaFile metaFile;

    reportSettings.toAttach(invoice);
    File file = reportSettings.generate().getFile();

    try {
      MetaFiles metaFiles = Beans.get(MetaFiles.class);
      metaFile = metaFiles.upload(file);
      metaFile.setFileName(String.format("%s.%s", reportSettings.getOutputName(), format));
      invoice.setPrintedPDF(metaFile);
      return MetaFiles.getPath(metaFile).toFile();
    } catch (IOException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.INVOICE_PRINTING_IO_ERROR)
              + " "
              + e.getLocalizedMessage());
    }
  }

  @Override
  public String printInvoices(List<Long> ids) throws IOException, AxelorException {
    List<File> printedInvoices = new ArrayList<>();
    List<String> invalidPrintSettingsInvoiceIds = checkInvalidPrintSettingsInvoices(ids);

    if (invalidPrintSettingsInvoiceIds.size() > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.INVOICES_MISSING_PRINTING_SETTINGS),
          invalidPrintSettingsInvoiceIds.toString());
    }

    int errorCount =
        ModelTool.apply(
            Invoice.class,
            ids,
            new ThrowConsumer<Invoice, Exception>() {
              @Override
              public void accept(Invoice invoice) throws Exception {
                try {
                  printedInvoices.add(
                      printCopiesToFile(invoice, false, null, ReportSettings.FORMAT_PDF, null));
                } catch (Exception e) {
                  TraceBackService.trace(e);
                  throw e;
                }
              }
            });
    if (errorCount > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("The file could not be generated"));
    }

    String fileName =
        I18n.get("Invoices")
            + " - "
            + appBaseService
                .getTodayDate(
                    Optional.ofNullable(AuthUtils.getUser())
                        .map(User::getActiveCompany)
                        .orElse(null))
                .format(DateTimeFormatter.BASIC_ISO_DATE)
            + ".pdf";
    return PdfTool.mergePdfToFileLink(printedInvoices, fileName);
  }

  public List<String> checkInvalidPrintSettingsInvoices(List<Long> ids) {

    List<String> invalidPrintSettingsInvoiceIds = new ArrayList<>();

    for (Long id : ids) {
      Invoice invoice = invoiceRepo.find(id);
      if (invoice.getPrintingSettings() == null) {
        invalidPrintSettingsInvoiceIds.add(invoice.getInvoiceId());
      }
    }
    return invalidPrintSettingsInvoiceIds;
  }

  @Override
  public ReportSettings prepareReportSettings(
      Invoice invoice, Integer reportType, String format, String locale) throws AxelorException {
    if (invoice.getPrintingSettings() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          String.format(
              I18n.get(AccountExceptionMessage.INVOICE_MISSING_PRINTING_SETTINGS),
              invoice.getInvoiceId()),
          invoice);
    }
    BirtTemplate invoiceBirtTemplate =
        accountConfigService.getAccountConfig(invoice.getCompany()).getInvoiceBirtTemplate();
    if (invoiceBirtTemplate == null || invoiceBirtTemplate.getTemplateMetaFile() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.BIRT_TEMPLATE_CONFIG_NOT_FOUND));
    }

    String title = I18n.get(InvoiceToolService.isRefund(invoice) ? "Refund" : "Invoice");
    if (invoice.getInvoiceId() != null) {
      title += " " + invoice.getInvoiceId();
    }

    AccountConfig accountConfig = accountConfigRepo.findByCompany(invoice.getCompany());
    if (Strings.isNullOrEmpty(locale)) {
      String userLanguageCode =
          Optional.ofNullable(AuthUtils.getUser()).map(User::getLanguage).orElse(null);
      String companyLanguageCode =
          invoice.getCompany().getLanguage() != null
              ? invoice.getCompany().getLanguage().getCode()
              : userLanguageCode;
      String partnerLanguageCode =
          invoice.getPartner().getLanguage() != null
              ? invoice.getPartner().getLanguage().getCode()
              : userLanguageCode;
      locale =
          accountConfig.getIsPrintInvoicesInCompanyLanguage()
              ? companyLanguageCode
              : partnerLanguageCode;
    }
    String watermark = null;
    MetaFile invoiceWatermark = accountConfig.getInvoiceWatermark();
    if (invoiceWatermark != null) {
      watermark = MetaFiles.getPath(invoiceWatermark).toString();
    }
    Map<String, Object> paramMap = new HashedMap<>();
    paramMap.put("ReportType", reportType == null ? 0 : reportType);
    paramMap.put("locale", locale);
    paramMap.put("Watermark", watermark);
    return birtTemplateService.generate(
        invoiceBirtTemplate,
        EntityHelper.getEntity(invoice),
        paramMap,
        title + " - ${date}",
        false,
        format);
  }
}
