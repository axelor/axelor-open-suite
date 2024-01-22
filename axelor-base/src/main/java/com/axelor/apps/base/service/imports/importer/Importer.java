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
package com.axelor.apps.base.service.imports.importer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.imports.listener.ImporterListener;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Importer {

  private static final File DEFAULT_WORKSPACE = createDefaultWorkspace();

  protected Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private ImportConfiguration configuration;
  private File workspace;

  @Inject ExcelToCSV excelToCSV;

  @Inject MetaFiles metaFiles;

  public void setConfiguration(ImportConfiguration configuration) {
    this.configuration = configuration;
  }

  public void setWorkspace(File workspace) {
    Preconditions.checkArgument(workspace.exists() && workspace.isDirectory());
    this.workspace = workspace;
  }

  public ImportConfiguration getConfiguration() {
    return this.configuration;
  }

  public File getWorkspace() {
    return this.workspace;
  }

  public Importer init(ImportConfiguration configuration) {
    return init(configuration, DEFAULT_WORKSPACE);
  }

  public Importer init(ImportConfiguration configuration, File workspace) {
    setConfiguration(configuration);
    setWorkspace(workspace);
    log.debug("Initialization of the import for the configuration {}", configuration.getName());
    return this;
  }

  public ImportHistory run(Map<String, Object> importContext) throws AxelorException, IOException {

    File bind = MetaFiles.getPath(configuration.getBindMetaFile()).toFile();
    File data = MetaFiles.getPath(configuration.getDataMetaFile()).toFile();

    if (!bind.exists()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.IMPORTER_1));
    }
    if (!data.exists()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.IMPORTER_2));
    }

    File fileWorkspace = createFinalWorkspace(configuration.getDataMetaFile());
    ImportHistory importHistory =
        process(bind.getAbsolutePath(), fileWorkspace.getAbsolutePath(), importContext);
    deleteFinalWorkspace(fileWorkspace);

    return importHistory;
  }

  public ImportHistory run() throws AxelorException, IOException {
    return run(null);
  }

  protected abstract ImportHistory process(
      String bind, String data, Map<String, Object> importContext) throws IOException;

  protected abstract ImportHistory process(String bind, String data) throws IOException;

  protected void deleteFinalWorkspace(File workspace) throws IOException {

    if (workspace.isDirectory()) {
      FileUtils.deleteDirectory(workspace);
    } else {
      java.nio.file.Files.delete(workspace.toPath());
    }
  }

  protected File createFinalWorkspace(MetaFile metaFile) throws IOException {

    File data = MetaFiles.getPath(metaFile).toFile();
    File finalWorkspace = new File(workspace, computeFinalWorkspaceName(data));
    finalWorkspace.mkdir();

    if (isZip(data)) {
      unZip(data, finalWorkspace);
    } else {
      FileUtils.copyFile(data, new File(finalWorkspace, metaFile.getFileName()));
    }

    if (Files.getFileExtension(data.getName()).equals("xlsx"))
      importExcel(new File(finalWorkspace, metaFile.getFileName()));

    return finalWorkspace;
  }

  protected String computeFinalWorkspaceName(File data) {
    return String.format(
        "%s-%s",
        Files.getNameWithoutExtension(data.getName()),
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
  }

  protected boolean isZip(File file) {
    return Files.getFileExtension(file.getName()).equals("zip");
  }

  protected void unZip(File file, File directory) throws IOException {

    File extractFile = null;
    try (ZipFile zipFile = new ZipFile(file); ) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        try (InputStream entryInputStream = zipFile.getInputStream(entry)) {
          byte[] buffer = new byte[1024];
          int bytesRead = 0;

          extractFile = new File(directory, entry.getName());
          if (entry.isDirectory()) {
            extractFile.mkdirs();
            continue;
          } else {
            extractFile.getParentFile().mkdirs();
            extractFile.createNewFile();
          }

          try (FileOutputStream fileOutputStream = new FileOutputStream(extractFile)) {
            while ((bytesRead = entryInputStream.read(buffer)) != -1) {
              fileOutputStream.write(buffer, 0, bytesRead);
            }
          }
          if (Files.getFileExtension(extractFile.getName()).equals("xlsx")) {
            importExcel(extractFile);
          }
        } catch (IOException ioException) {
          log.error(ioException.getMessage());
        }
      }
    }
  }

  /**
   * Adds a new log in the history table for data configuration.
   *
   * @param listener
   * @return
   * @throws IOException
   */
  protected ImportHistory addHistory(ImporterListener listener) throws IOException {

    ImportHistory importHistory =
        new ImportHistory(AuthUtils.getUser(), configuration.getDataMetaFile());
    File logFile = File.createTempFile("importLog", ".log");

    try (FileWriter writer = new FileWriter(logFile)) {
      writer.write(listener.getImportLog());
    }

    MetaFile logMetaFile =
        metaFiles.upload(
            new FileInputStream(logFile),
            "importLog-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".log");
    importHistory.setLogMetaFile(logMetaFile);
    importHistory.setImportConfiguration(configuration);

    return importHistory;
  }

  private static File createDefaultWorkspace() {

    File file = Files.createTempDir();
    file.deleteOnExit();
    return file;
  }

  public void importExcel(File excelFile) throws IOException {
    List<Map> sheetList = excelToCSV.generateExcelSheets(excelFile);
    FileInputStream inputStream = new FileInputStream(excelFile);
    Workbook workBook = new XSSFWorkbook(inputStream);

    try {
      for (int i = 0; i < sheetList.size(); i++) {
        Sheet sheet = workBook.getSheet(sheetList.get(i).get("name").toString());
        File sheetFile =
            new File(
                excelFile.getParent() + "/" + sheetList.get(i).get("name").toString() + ".csv");
        excelToCSV.writeTOCSV(sheetFile, sheet, 0, 0);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
