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
package com.axelor.apps.account.service.invoice.print;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Localization;
import com.axelor.apps.base.db.PfxCertificate;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.birt.template.BirtTemplateService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.pdf.PdfSignatureService;
import com.axelor.apps.base.service.printing.template.PrintingTemplateHelper;
import com.axelor.apps.base.service.printing.template.PrintingTemplatePrintService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.EntityHelper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.utils.ThrowConsumer;
import com.axelor.utils.helpers.ModelHelper;
import com.axelor.utils.helpers.file.PdfHelper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.io.FilenameUtils;

/** Implementation of the service printing invoices. */
@Singleton
public class InvoicePrintServiceImpl implements InvoicePrintService {

  protected InvoiceRepository invoiceRepo;
  protected AccountConfigRepository accountConfigRepo;
  protected AppBaseService appBaseService;
  protected AccountConfigService accountConfigService;
  protected BirtTemplateService birtTemplateService;
  protected PdfSignatureService pdfSignatureService;
  protected PrintingTemplatePrintService printingTemplatePrintService;

  @Inject
  public InvoicePrintServiceImpl(
      InvoiceRepository invoiceRepo,
      AccountConfigRepository accountConfigRepo,
      AccountConfigService accountConfigService,
      AppBaseService appBaseService,
      BirtTemplateService birtTemplateService,
      PdfSignatureService pdfSignatureService,
      PrintingTemplatePrintService printingTemplatePrintService) {
    this.invoiceRepo = invoiceRepo;
    this.accountConfigRepo = accountConfigRepo;
    this.appBaseService = appBaseService;
    this.accountConfigService = accountConfigService;
    this.birtTemplateService = birtTemplateService;
    this.pdfSignatureService = pdfSignatureService;
    this.printingTemplatePrintService = printingTemplatePrintService;
  }

  @Override
  public String printInvoice(
      Invoice invoice,
      boolean forceRefresh,
      PrintingTemplate invoicePrintTemplate,
      Integer reportType,
      String locale)
      throws AxelorException, IOException {
    return PrintingTemplateHelper.getFileLink(
        printCopiesToFile(invoice, forceRefresh, reportType, invoicePrintTemplate, locale));
  }

  @Override
  public File printCopiesToFile(
      Invoice invoice,
      boolean forceRefresh,
      Integer reportType,
      PrintingTemplate invoicePrintTemplate,
      String locale)
      throws AxelorException, IOException {
    File file = getPrintedInvoice(invoice, forceRefresh, reportType, invoicePrintTemplate, locale);
    int copyNumber = invoice.getInvoicesCopySelect();
    copyNumber = copyNumber == 0 ? 1 : copyNumber;
    File fileCopies = file;
    String fileName = file.getName();
    if (ReportSettings.FORMAT_PDF.equals(FilenameUtils.getExtension(fileName))) {
      Path path = PdfHelper.printCopiesToFile(file, copyNumber).toPath();
      fileCopies =
          Files.move(path, path.resolveSibling(fileName), StandardCopyOption.REPLACE_EXISTING)
              .toFile();
    }
    return fileCopies;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public File getPrintedInvoice(
      Invoice invoice,
      boolean forceRefresh,
      Integer reportType,
      PrintingTemplate invoicePrintTemplate,
      String locale)
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
            ? print(invoice, reportType, invoicePrintTemplate, locale)
            : printAndSave(invoice, reportType, invoicePrintTemplate, locale);
      }
    } else {
      // invoice is not ventilated (or validated for advance payment invoices) --> generate and
      // don't save
      return print(invoice, reportType, invoicePrintTemplate, locale);
    }
  }

  @Override
  public File print(
      Invoice invoice, Integer reportType, PrintingTemplate invoicePrintTemplate, String locale)
      throws AxelorException {
    if (invoice.getPrintingSettings() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          String.format(
              I18n.get(AccountExceptionMessage.INVOICE_MISSING_PRINTING_SETTINGS),
              invoice.getInvoiceId()),
          invoice);
    }

    AccountConfig accountConfig = accountConfigRepo.findByCompany(invoice.getCompany());
    if (Strings.isNullOrEmpty(locale)) {
      String userLocalizationCode =
          Optional.ofNullable(AuthUtils.getUser())
              .map(User::getLocalization)
              .map(Localization::getCode)
              .orElse(null);
      String companyLocalizationCode =
          invoice.getCompany().getLocalization() != null
              ? invoice.getCompany().getLocalization().getCode()
              : userLocalizationCode;
      String partnerLocalizationCode =
          invoice.getPartner().getLocalization() != null
              ? invoice.getPartner().getLocalization().getCode()
              : userLocalizationCode;
      locale =
          accountConfig.getIsPrintInvoicesInCompanyLanguage()
              ? companyLocalizationCode
              : partnerLocalizationCode;
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

    PrintingGenFactoryContext factoryContext =
        new PrintingGenFactoryContext(EntityHelper.getEntity(invoice));
    factoryContext.setContext(paramMap);
    return printingTemplatePrintService.getPrintFile(invoicePrintTemplate, factoryContext);
  }

  public File printAndSave(
      Invoice invoice, Integer reportType, PrintingTemplate invoicePrintTemplate, String locale)
      throws AxelorException {

    File file = print(invoice, reportType, invoicePrintTemplate, locale);
    MetaFile metaFile;

    try {
      MetaFiles metaFiles = Beans.get(MetaFiles.class);
      metaFile = metaFiles.upload(file);
      MetaFile signedMetaFile =
          ReportSettings.FORMAT_PDF.equals(FilenameUtils.getExtension(file.getName()))
              ? getSignedPdf(metaFile)
              : metaFile;
      metaFiles.attach(signedMetaFile, signedMetaFile.getFileName(), invoice);
      invoice.setPrintedPDF(signedMetaFile);
      return MetaFiles.getPath(metaFile).toFile();
    } catch (IOException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.INVOICE_PRINTING_IO_ERROR)
              + " "
              + e.getLocalizedMessage());
    }
  }

  protected MetaFile getSignedPdf(MetaFile metaFile) throws AxelorException {
    PfxCertificate pfxCertificate = appBaseService.getAppBase().getPfxCertificate();
    if (pfxCertificate == null) {
      return metaFile;
    }
    return pdfSignatureService.digitallySignPdf(metaFile, pfxCertificate, "Invoice");
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
        ModelHelper.apply(
            Invoice.class,
            ids,
            new ThrowConsumer<Invoice, Exception>() {
              @Override
              public void accept(Invoice invoice) throws Exception {
                try {
                  printedInvoices.add(
                      printCopiesToFile(
                          invoice,
                          false,
                          InvoiceRepository.REPORT_TYPE_ORIGINAL_INVOICE,
                          accountConfigService.getInvoicePrintTemplate(invoice.getCompany()),
                          null));
                } catch (Exception e) {
                  TraceBackService.trace(e);
                  throw e;
                }
              }
            });
    if (errorCount > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FILE_COULD_NOT_BE_GENERATED));
    }

    String fileName =
        I18n.get("Invoices")
            + " - "
            + appBaseService
                .getTodayDate(
                    Optional.ofNullable(AuthUtils.getUser())
                        .map(User::getActiveCompany)
                        .orElse(null))
                .format(DateTimeFormatter.BASIC_ISO_DATE);
    return PrintingTemplateHelper.mergeToFileLink(printedInvoices, fileName);
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
}
