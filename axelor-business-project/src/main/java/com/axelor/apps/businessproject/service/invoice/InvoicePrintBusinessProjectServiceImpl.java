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
package com.axelor.apps.businessproject.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.print.InvoicePrintServiceImpl;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.birt.template.BirtTemplateService;
import com.axelor.apps.base.service.pdf.PdfService;
import com.axelor.apps.base.service.pdf.PdfSignatureService;
import com.axelor.apps.base.service.printing.template.PrintingTemplateHelper;
import com.axelor.apps.base.service.printing.template.PrintingTemplatePrintService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.apps.businessproject.report.ITranslation;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.io.Files;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

public class InvoicePrintBusinessProjectServiceImpl extends InvoicePrintServiceImpl
    implements InvoicePrintBusinessProjectService {

  protected AppBusinessProjectService appBusinessProjectService;
  protected PdfService pdfService;
  protected MetaFiles metaFiles;

  @Inject
  public InvoicePrintBusinessProjectServiceImpl(
      InvoiceRepository invoiceRepo,
      AccountConfigRepository accountConfigRepo,
      AccountConfigService accountConfigService,
      AppBaseService appBaseService,
      BirtTemplateService birtTemplateService,
      PdfSignatureService pdfSignatureService,
      PrintingTemplatePrintService printingTemplatePrintService,
      AppBusinessProjectService appBusinessProjectService,
      PdfService pdfService,
      MetaFiles metaFiles) {
    super(
        invoiceRepo,
        accountConfigRepo,
        accountConfigService,
        appBaseService,
        birtTemplateService,
        pdfSignatureService,
        printingTemplatePrintService);
    this.appBusinessProjectService = appBusinessProjectService;
    this.pdfService = pdfService;
    this.metaFiles = metaFiles;
  }

  @Override
  public File printAndSave(
      Invoice invoice, Integer reportType, PrintingTemplate invoicePrintTemplate, String locale)
      throws AxelorException {
    try {
      File file = super.printAndSave(invoice, reportType, invoicePrintTemplate, locale);
      File printExpenses = printExpenses(invoice, locale);
      if (printExpenses != null) {
        MetaFile expenseMetaFile = metaFiles.upload(printExpenses);
        metaFiles.attach(expenseMetaFile, expenseMetaFile.getFileName(), invoice);
      }
      return file;
    } catch (IOException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getLocalizedMessage());
    }
  }

  @Override
  public File printExpenses(Invoice invoice, String locale) throws AxelorException {
    if (!invoice.getDisplayExpenseOnPrinting()
        || invoice.getInvoiceLineList().stream().noneMatch(il -> il.getExpenseLine() != null)) {
      return null;
    }

    Map<String, List<ExpenseLine>> map = getExpenseLinesByGroupingPeriod(invoice);
    List<File> fileList = new ArrayList<>();
    PrintingTemplate invoiceExpensePrintTemplate = getInvoiceExpensePrintTemplate();
    int noOfPeriods = 0;
    String fileName = getReportFileName(invoice);
    String fileNamePerPeriod = fileName;
    for (Entry<String, List<ExpenseLine>> entry : map.entrySet()) {
      String groupingPeriod = entry.getKey();
      List<ExpenseLine> expenseLines = entry.getValue();
      expenseLines.sort(Comparator.comparing(ExpenseLine::getExpenseDate));

      Map<String, Object> paramMap = new HashMap<>();
      paramMap.put("GroupingPeriod", groupingPeriod);
      paramMap.put(
          "ExpenseLineIds",
          expenseLines.stream()
              .map(ExpenseLine::getId)
              .map(Objects::toString)
              .collect(Collectors.joining(",")));
      paramMap.put("locale", locale);
      paramMap.put("InvoiceId", invoice.getId());

      if (groupingPeriod != null) {
        fileNamePerPeriod = getFileNamePerPeriod(invoice, groupingPeriod);
      }
      fileList.add(
          printingTemplatePrintService.getPrintFile(
              invoiceExpensePrintTemplate,
              new PrintingGenFactoryContext(paramMap),
              fileNamePerPeriod));

      addJustificationFiles(fileList, expenseLines);
      noOfPeriods++;
    }
    if (noOfPeriods == 1) {
      fileName = Files.getNameWithoutExtension(fileList.get(0).getName());
    }
    return PrintingTemplateHelper.mergeToFile(fileList, fileName);
  }

  protected void addJustificationFiles(List<File> fileList, List<ExpenseLine> expenseLines)
      throws AxelorException {
    for (ExpenseLine expenseLine : expenseLines) {
      MetaFile justificationMetaFile = expenseLine.getJustificationMetaFile();
      if (justificationMetaFile == null) {
        continue;
      }
      if (justificationMetaFile.getFileType().startsWith("image")) {
        justificationMetaFile = pdfService.convertImageToPdf(justificationMetaFile);
      }
      fileList.add(MetaFiles.getPath(justificationMetaFile).toFile());
    }
  }

  protected Map<String, List<ExpenseLine>> getExpenseLinesByGroupingPeriod(Invoice invoice) {
    List<ExpenseLine> expenseLineList =
        invoice.getInvoiceLineList().stream()
            .map(InvoiceLine::getExpenseLine)
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(ExpenseLine::getExpenseDate))
            .collect(Collectors.toList());

    Map<String, List<ExpenseLine>> map = new HashMap<>();
    if (invoice.getIsExpenseLineOnInvoiceGrouped() && invoice.getGroupingPeriodSelect() != null) {
      if (invoice.getGroupingPeriodSelect().equals(InvoiceRepository.GROUPING_PERIOD_MONTH)) {
        map = groupExpenseLinesByMonth(expenseLineList);
      } else {
        map = groupExpenseLinesByWeek(expenseLineList);
      }
    } else {
      map.put(null, expenseLineList);
    }
    return map;
  }

  protected Map<String, List<ExpenseLine>> groupExpenseLinesByMonth(
      List<ExpenseLine> expenseLines) {
    return expenseLines.stream()
        .collect(
            Collectors.groupingBy(
                expenseLine ->
                    expenseLine.getExpenseDate().format(DateTimeFormatter.ofPattern("MM/yyyy")),
                LinkedHashMap::new,
                Collectors.toList()));
  }

  protected Map<String, List<ExpenseLine>> groupExpenseLinesByWeek(List<ExpenseLine> expenseLines) {
    WeekFields weekFields = WeekFields.ISO;
    return expenseLines.stream()
        .collect(
            Collectors.groupingBy(
                expenseLine ->
                    String.format(
                        "%02d/%d",
                        expenseLine.getExpenseDate().get(weekFields.weekOfWeekBasedYear()),
                        expenseLine.getExpenseDate().getYear()),
                LinkedHashMap::new,
                Collectors.toList()));
  }

  protected PrintingTemplate getInvoiceExpensePrintTemplate() throws AxelorException {
    PrintingTemplate invoiceExpensePrintTemplate =
        appBusinessProjectService.getAppBusinessProject().getInvoiceExpensePrintTemplate();
    if (invoiceExpensePrintTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.TEMPLATE_CONFIG_NOT_FOUND));
    }
    return invoiceExpensePrintTemplate;
  }

  protected String getFileNamePerPeriod(Invoice invoice, String groupingPeriod) {
    String[] periodParts = groupingPeriod.split("/");
    return String.format(
        "%s %s %s",
        I18n.get(ITranslation.INVOICE_EXPENSE_JUSTIFICATION),
        I18n.get(invoice.getGroupingPeriodSelect()),
        String.format("%s%s", periodParts[1], periodParts[0]));
  }

  protected String getReportFileName(Invoice invoice) {
    return String.format(
        "%s %s", I18n.get(ITranslation.INVOICE_EXPENSE_ON_INVOICE), invoice.getInvoiceId());
  }
}
