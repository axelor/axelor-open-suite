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
package com.axelor.apps.base.service.printing.template;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.PrintingTemplateLine;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.apps.base.service.printing.template.model.TemplatePrint;
import com.axelor.apps.base.utils.PdfHelper;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.utils.ThrowConsumer;
import com.axelor.utils.helpers.ModelHelper;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public class PrintingTemplatePrintServiceImpl implements PrintingTemplatePrintService {

  protected AppBaseService appBaseService;

  @Inject
  public PrintingTemplatePrintServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public String getPrintLink(PrintingTemplate template, PrintingGenFactoryContext context)
      throws AxelorException {
    return getPrintLink(template, context, getOutputFileName(template));
  }

  @Override
  public String getPrintLink(
      PrintingTemplate template, PrintingGenFactoryContext context, String outputFileName)
      throws AxelorException {
    List<TemplatePrint> prints = getPrintList(template, context);
    return getPrintFileLink(prints, outputFileName, isPdfFormat(prints));
  }

  @Override
  public File getPrintFile(PrintingTemplate template, PrintingGenFactoryContext context)
      throws AxelorException {
    return getPrintFile(template, context, getOutputFileName(template));
  }

  @Override
  public File getPrintFile(
      PrintingTemplate template, PrintingGenFactoryContext context, String outputFileName)
      throws AxelorException {
    List<TemplatePrint> prints = getPrintList(template, context);
    return getPrintFile(prints, outputFileName, isPdfFormat(prints));
  }

  @Override
  public <T extends Model> String getPrintLinkForList(
      List<Integer> idList, Class<T> contextClass, PrintingTemplate template)
      throws IOException, AxelorException {
    List<File> printedRecords = new ArrayList<>();
    AtomicBoolean isPDf = new AtomicBoolean(true);
    int errorCount =
        ModelHelper.apply(
            contextClass,
            idList,
            new ThrowConsumer<T, Exception>() {
              @Override
              public void accept(T item) throws Exception {
                try {
                  File printFile = getPrintFile(template, new PrintingGenFactoryContext(item));
                  if (!ReportSettings.FORMAT_PDF.equals(
                      FilenameUtils.getExtension(printFile.getName()))) {
                    isPDf.set(false);
                  }
                  printedRecords.add(printFile);
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
    String fileName = getOutputFileName(template);
    return mergeToFileLink(printedRecords, isPDf.get(), fileName);
  }

  protected List<TemplatePrint> getPrintList(
      PrintingTemplate printingTemplate, PrintingGenFactoryContext context) throws AxelorException {
    List<TemplatePrint> prints = new ArrayList<>();
    for (PrintingTemplateLine templateLine : printingTemplate.getPrintingTemplateLineList()) {
      PrintingGeneratorFactory factory = PrintingGeneratorFactory.getFactory(templateLine);
      TemplatePrint print = factory.generate(templateLine, context);
      prints.add(print);
    }
    return prints;
  }

  protected String getPrintFileLink(
      List<TemplatePrint> prints, String outputFileName, boolean isPdfOutputFormat)
      throws AxelorException {
    List<File> printFiles = getPrintFilesList(prints);
    return mergeToFileLink(printFiles, isPdfOutputFormat, outputFileName);
  }

  protected File getPrintFile(
      List<TemplatePrint> prints, String outputFileName, boolean isPdfOutputFormat)
      throws AxelorException {
    List<File> printFiles = getPrintFilesList(prints);
    return mergeToFile(printFiles, isPdfOutputFormat, outputFileName);
  }

  protected List<File> getPrintFilesList(List<TemplatePrint> prints) throws AxelorException {
    if (CollectionUtils.isEmpty(prints)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FILE_COULD_NOT_BE_GENERATED));
    }

    return prints.stream()
        .map(TemplatePrint::getPrint)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  protected String mergeToFileLink(
      List<File> printFiles, boolean isPdfOutputFormat, String fileName) throws AxelorException {
    if (CollectionUtils.isEmpty(printFiles)) {
      return null;
    }
    File file = mergeToFile(printFiles, isPdfOutputFormat, fileName);
    return PdfHelper.getFileLinkFromPdfFile(
        file, fileName + "." + FilenameUtils.getExtension(file.getName()));
  }

  protected File mergeToFile(
      List<File> printFiles, boolean isPdfOutputFormat, String outputFileName)
      throws AxelorException {
    Path file = null;
    try {
      if (CollectionUtils.isEmpty(printFiles)) {
        return null;
      }

      if (printFiles.size() == 1) {
        return printFiles.listIterator().next();
      }

      if (isPdfOutputFormat) {
        Path output = PdfHelper.mergePdf(printFiles).toPath();
        file =
            Files.move(
                output,
                output.resolveSibling(outputFileName + "." + ReportSettings.FORMAT_PDF),
                StandardCopyOption.REPLACE_EXISTING);
      } else {
        file = createZip(outputFileName, printFiles);
      }
    } catch (IOException e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FILE_COULD_NOT_BE_GENERATED));
    }
    return file.toFile();
  }

  protected boolean isPdfFormat(List<TemplatePrint> prints) {
    return prints.stream()
        .map(TemplatePrint::getOutputFormat)
        .allMatch(ReportSettings.FORMAT_PDF::equals);
  }

  protected Path createZip(String zipFileName, List<File> fileList) throws IOException {
    if (CollectionUtils.isEmpty(fileList)) {
      return null;
    }
    Path zipFile = MetaFiles.createTempFile(zipFileName, ".zip");

    try (ZipOutputStream zout = new ZipOutputStream(Files.newOutputStream(zipFile))) {
      for (File file : fileList) {
        zout.putNextEntry(new ZipEntry(file.getName()));
        zout.write(IOUtils.toByteArray(Files.newInputStream(file.toPath())));
      }
    }
    return zipFile;
  }

  protected String getOutputFileName(PrintingTemplate template) {
    return I18n.get(template.getName())
        + " - "
        + appBaseService
            .getTodayDate(
                Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null))
            .format(DateTimeFormatter.BASIC_ISO_DATE);
  }
}
