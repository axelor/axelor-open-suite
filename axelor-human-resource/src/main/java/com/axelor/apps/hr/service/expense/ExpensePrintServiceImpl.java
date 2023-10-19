package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PrintFromBirtTemplateService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.pdf.PdfService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.dms.db.DMSFile;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.utils.helpers.file.PdfHelper;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ExpensePrintServiceImpl implements ExpensePrintService {

  protected static final String DATE_FORMAT_YYYYMMDDHHMM = "YYYYMMddHHmm";

  protected MetaFiles metaFiles;
  protected AppBaseService appBaseService;
  protected HRConfigService hrConfigService;
  protected PrintFromBirtTemplateService printFromBirtTemplateService;
  protected PdfService pdfService;

  @Inject
  public ExpensePrintServiceImpl(
      MetaFiles metaFiles,
      AppBaseService appBaseService,
      HRConfigService hrConfigService,
      PrintFromBirtTemplateService printFromBirtTemplateService,
      PdfService pdfService) {
    this.metaFiles = metaFiles;
    this.appBaseService = appBaseService;
    this.hrConfigService = hrConfigService;
    this.printFromBirtTemplateService = printFromBirtTemplateService;
    this.pdfService = pdfService;
  }

  @Override
  public DMSFile uploadExpenseReport(Expense expense) throws IOException, AxelorException {
    String title = getExpenseReportTitle();
    MetaFile metaFile = metaFiles.upload(printAll(expense));
    metaFile.setFileName(title + ".pdf");
    return metaFiles.attach(metaFile, null, expense);
  }

  @Override
  public String getExpenseReportTitle() {
    return I18n.get("Expense")
        + " - "
        + I18n.get("Report")
        + " - "
        + appBaseService
            .getTodayDateTime()
            .format(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYYMMDDHHMM));
  }

  protected File printAll(Expense expense) throws AxelorException, IOException {
    List<File> fileList = new ArrayList<>();
    File reportFile = getReportFile(expense);
    fileList.add(reportFile);
    List<MetaFile> pdfMetaFileList = getExpenseLinePdfJustificationFiles(expense);
    List<MetaFile> imageConvertedMetaFileList =
        pdfService.convertImageToPdf(getExpenseLineImageJustificationFiles(expense));

    fileList.addAll(convertMetaFileToFile(imageConvertedMetaFileList));
    fileList.addAll(convertMetaFileToFile(pdfMetaFileList));

    return PdfHelper.mergePdf(fileList);
  }

  protected File getReportFile(Expense expense) throws AxelorException, IOException {
    BirtTemplate birtTemplate = getBirtTemplate(expense);
    return printFromBirtTemplateService.generateBirtTemplate(birtTemplate, expense);
  }

  protected BirtTemplate getBirtTemplate(Expense expense) throws AxelorException {
    BirtTemplate birtTemplate =
        hrConfigService.getHRConfig(expense.getCompany()).getExpenseReportBirtTemplate();
    if (birtTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_BIRT_TEMPLATE_MISSING));
    }

    return birtTemplate;
  }

  protected List<MetaFile> getExpenseLinePdfJustificationFiles(Expense expense) {
    return expense.getGeneralExpenseLineList().stream()
        .map(ExpenseLine::getJustificationMetaFile)
        .filter(Objects::nonNull)
        .filter(file -> "application/pdf".equals(file.getFileType()))
        .collect(Collectors.toList());
  }

  protected List<MetaFile> getExpenseLineImageJustificationFiles(Expense expense) {
    return expense.getGeneralExpenseLineList().stream()
        .map(ExpenseLine::getJustificationMetaFile)
        .filter(Objects::nonNull)
        .filter(file -> file.getFileType().startsWith("image"))
        .collect(Collectors.toList());
  }

  protected List<File> convertMetaFileToFile(List<MetaFile> metaFileList) {
    List<File> fileList = new ArrayList<>();
    for (MetaFile metaFile : metaFileList) {
      Path path = MetaFiles.getPath(metaFile);
      fileList.add(path.toFile());
    }
    return fileList;
  }
}
