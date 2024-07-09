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
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.utils.PdfHelper;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.common.FileUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.utils.helpers.StringHelper;
import com.axelor.utils.service.TranslationBaseService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public class PrintingTemplateHelper {

  private PrintingTemplateHelper() {}

  public static String mergeToFileLink(List<File> printFiles, String outputFileName)
      throws AxelorException {
    File file = mergeToFile(printFiles, outputFileName);
    String fileLink = null;
    if (file != null) {
      fileLink = getFileLink(file);
    }
    return fileLink;
  }

  public static File mergeToFile(List<File> printFiles, String outputFileName)
      throws AxelorException {
    Path file = null;
    try {
      if (CollectionUtils.isEmpty(printFiles)) {
        return null;
      }

      outputFileName = Beans.get(TranslationBaseService.class).getValueTranslation(outputFileName);
      outputFileName = formatOutputName(outputFileName);

      if (printFiles.size() == 1) {
        Path first = printFiles.listIterator().next().toPath();
        String outputName =
            outputFileName + "." + FilenameUtils.getExtension(first.getFileName().toString());
        return renameFile(outputName, first).toFile();
      }
      if (isPdf(printFiles)) {
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
    return Optional.ofNullable(file).map(Path::toFile).orElse(null);
  }

  public static String getFileLink(File file) throws AxelorException {
    String originalName = file.getName();
    originalName = translateFileName(originalName);
    String safeName = FileUtils.safeFileName(originalName).replaceAll("[^\\p{ASCII}]", "");

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

  protected static String translateFileName(String originalName) {
    return String.format(
        "%s.%s",
        Beans.get(TranslationBaseService.class)
            .getValueTranslation(FileUtils.stripExtension(originalName)),
        FileUtils.getExtension(originalName));
  }

  private static Path createZip(String zipFileName, List<File> fileList) throws IOException {
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
    String outFileName = String.format("%s.%s", zipFileName, "zip");
    return renameFile(outFileName, zipFile);
  }

  private static Path renameFile(String newName, Path path) throws IOException {
    return Files.move(path, path.resolveSibling(newName), StandardCopyOption.REPLACE_EXISTING);
  }

  private static String formatOutputName(String outputFileName) {
    ZonedDateTime todayDateTime = Beans.get(AppBaseService.class).getTodayDateTime();
    outputFileName =
        outputFileName
            .replace("${date}", todayDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
            .replace("${time}", todayDateTime.format(DateTimeFormatter.ofPattern("HHmmss")));
    outputFileName = StringHelper.getFilename(outputFileName);
    return outputFileName;
  }

  private static boolean isPdf(List<File> files) {
    return files.stream()
        .map(File::getName)
        .map(FilenameUtils::getExtension)
        .allMatch(ReportSettings.FORMAT_PDF::equals);
  }
}
