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
import com.axelor.common.FileUtils;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.utils.ThrowConsumer;
import com.axelor.utils.helpers.ModelHelper;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public class PrintingTemplatePrintServiceImpl implements PrintingTemplatePrintService {

  protected AppBaseService appBaseService;
  protected MetaFiles metaFiles;

  @Inject
  public PrintingTemplatePrintServiceImpl(AppBaseService appBaseService, MetaFiles metaFiles) {
    this.appBaseService = appBaseService;
    this.metaFiles = metaFiles;
  }

  @Override
  public String getPrintLink(PrintingTemplate template, PrintingGenFactoryContext context)
      throws AxelorException {
    return getPrintLink(template, context, template.getName());
  }

  @Override
  public String getPrintLink(
      PrintingTemplate template, PrintingGenFactoryContext context, String outputFileName)
      throws AxelorException {
    return getPrintLink(template, context, outputFileName, template.getToAttach());
  }

  @Override
  public String getPrintLink(
      PrintingTemplate template, PrintingGenFactoryContext context, boolean toAttach)
      throws AxelorException {
    return getPrintLink(template, context, template.getName(), toAttach);
  }

  @Override
  public String getPrintLink(
      PrintingTemplate template,
      PrintingGenFactoryContext context,
      String outputFileName,
      boolean toAttach)
      throws AxelorException {
    File file = getPrintFile(template, context, outputFileName, toAttach);
    return getFileLink(file);
  }

  @Override
  public File getPrintFile(PrintingTemplate template, PrintingGenFactoryContext context)
      throws AxelorException {
    return getPrintFile(template, context, template.getName());
  }

  @Override
  public File getPrintFile(
      PrintingTemplate template, PrintingGenFactoryContext context, String outputFileName)
      throws AxelorException {
    return getPrintFile(template, context, outputFileName, template.getToAttach());
  }

  @Override
  public File getPrintFile(
      PrintingTemplate template,
      PrintingGenFactoryContext context,
      String outputFileName,
      Boolean toAttach)
      throws AxelorException {
    List<TemplatePrint> prints = getPrintList(template, context);
    return getPrintFile(prints, outputFileName, isPdfFormat(prints), context, toAttach);
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
                  File printFile =
                      getPrintFile(
                          template,
                          new PrintingGenFactoryContext(EntityHelper.getEntity(item)),
                          template.getName() + "-" + item.getId());
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
    if (errorCount > 0 || CollectionUtils.isEmpty(printedRecords)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FILE_COULD_NOT_BE_GENERATED));
    }
    File file = mergeToFile(printedRecords, isPDf.get(), template.getName());
    return getFileLink(file);
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

  protected File getPrintFile(
      List<TemplatePrint> prints,
      String outputFileName,
      boolean isPdfOutputFormat,
      PrintingGenFactoryContext context,
      boolean toAttach)
      throws AxelorException {
    List<File> printFiles = getPrintFilesList(prints);
    File file = mergeToFile(printFiles, isPdfOutputFormat, outputFileName);
    if (toAttach && context != null && context.getModel() != null) {
      try {
        metaFiles.attach(new FileInputStream(file), file.getName(), context.getModel());
      } catch (IOException e) {
        throw new AxelorException(
            e,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.FILE_COULD_NOT_BE_GENERATED));
      }
    }
    return file;
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

  protected File mergeToFile(
      List<File> printFiles, boolean isPdfOutputFormat, String outputFileName)
      throws AxelorException {
    Path file = null;
    try {
      if (CollectionUtils.isEmpty(printFiles)) {
        return null;
      }

      outputFileName = formatOutputName(outputFileName);

      if (printFiles.size() == 1) {
        String outputName =
            outputFileName
                + "."
                + FilenameUtils.getExtension(
                    printFiles.listIterator().next().toPath().getFileName().toString());
        return renameFile(outputName, printFiles.listIterator().next().toPath()).toFile();
      }

      if (isPdfOutputFormat) {
        Path output = PdfHelper.mergePdf(printFiles).toPath();
        file = renameFile(outputFileName + "." + ReportSettings.FORMAT_PDF, output);
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

  protected String getFileLink(File file) throws AxelorException {
    String originalName = file.getName();
    String safeName = FileUtils.safeFileName(originalName);

    try {
      if (!originalName.equals(safeName)) {
        file = renameFile(safeName, file.toPath()).toFile();
      }
    } catch (IOException e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FILE_COULD_NOT_BE_GENERATED));
    }

    return PdfHelper.getFileLinkFromPdfFile(file, originalName);
  }

  protected Path renameFile(String newName, Path path) throws IOException {
    return Files.move(path, path.resolveSibling(newName), StandardCopyOption.REPLACE_EXISTING);
  }

  protected String formatOutputName(String outputFileName) {
    ZonedDateTime todayDateTime = appBaseService.getTodayDateTime();
    outputFileName =
        outputFileName
            .replace("${date}", todayDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
            .replace("${time}", todayDateTime.format(DateTimeFormatter.ofPattern("HHmmss")));
    return outputFileName;
  }
}
