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
package com.axelor.apps.base.service;

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
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.schema.actions.ActionExport;
import com.axelor.utils.ThrowConsumer;
import com.axelor.utils.helpers.ModelHelper;
import com.axelor.utils.helpers.file.PdfHelper;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;

public class PrintFromBirtTemplateServiceImpl implements PrintFromBirtTemplateService {

  protected BirtTemplateService birtTemplateService;
  protected AppBaseService appBaseService;

  @Inject
  public PrintFromBirtTemplateServiceImpl(
      BirtTemplateService birtTemplateService, AppBaseService appBaseService) {
    this.birtTemplateService = birtTemplateService;
    this.appBaseService = appBaseService;
  }

  @Override
  public String print(BirtTemplate template, Model model) throws AxelorException {

    String outputName = getOutputFileName(template);

    return birtTemplateService.generateBirtTemplateLink(template, model, outputName);
  }

  @Override
  public <T extends Model> String getPrintFileLink(
      List<Integer> idList, Class<T> contextClass, BirtTemplate birtTemplate)
      throws IOException, AxelorException {
    List<File> printedRecords = new ArrayList<>();

    int errorCount =
        ModelHelper.apply(
            contextClass,
            idList,
            new ThrowConsumer<T, Exception>() {
              @Override
              public void accept(T item) throws Exception {
                try {
                  printedRecords.add(generateBirtTemplate(birtTemplate, item));
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
    String fileName = getOutputFileName(birtTemplate);

    String fileLink = "";
    if (ReportSettings.FORMAT_PDF.equals(birtTemplate.getFormat())) {
      fileLink =
          PdfHelper.mergePdfToFileLink(printedRecords, fileName + "." + birtTemplate.getFormat());
    } else {
      fileLink = getZipFileLink(fileName, printedRecords);
    }
    return fileLink;
  }

  @Override
  public <T extends Model> File generateBirtTemplate(BirtTemplate birtTemplate, T model)
      throws AxelorException, IOException {
    String name = birtTemplate.getName();
    String format = birtTemplate.getFormat();
    Path src =
        birtTemplateService
            .generateBirtTemplateFile(birtTemplate, model, name, false, format)
            .toPath();
    String outFileName = String.format("%s-%s.%s", name, model.getId(), format);
    Path dest =
        Files.move(src, src.resolveSibling(outFileName), StandardCopyOption.REPLACE_EXISTING);
    return dest.toFile();
  }

  protected String getOutputFileName(BirtTemplate birtTemplate) {
    return I18n.get(birtTemplate.getName())
        + " - "
        + appBaseService
            .getTodayDate(
                Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null))
            .format(DateTimeFormatter.BASIC_ISO_DATE);
  }

  protected String getZipFileLink(String zipFileName, List<File> fileList) throws IOException {
    String outFileName = String.format("%s.%s", zipFileName, "zip");
    Path zip = createZip(zipFileName, fileList);
    moveToExportDir(outFileName, zip);
    return outFileName;
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

  protected void moveToExportDir(String fileName, Path filePath) throws IOException {
    Path exportDirPath = Paths.get(ActionExport.getExportPath().getAbsolutePath(), fileName);
    Files.move(filePath, exportDirPath, StandardCopyOption.REPLACE_EXISTING);
  }
}
