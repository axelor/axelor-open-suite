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
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.StringUtils;
import com.axelor.common.csv.CSVFile;
import com.axelor.data.Listener;
import com.axelor.data.csv.CSVImporter;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataBackupRestoreService {

  protected static final char SEPARATOR = ',';

  /* Restore the Data using provided zip File and prepare Log File and Return it*/
  public File restore(MetaFile zipedBackupFile, boolean isTemplateWithDescription)
      throws AxelorException {
    Logger LOG = LoggerFactory.getLogger(getClass());
    File tempDir = Files.createTempDir();
    String dirPath = tempDir.getAbsolutePath();
    StringBuilder sb = new StringBuilder();
    try {
      unZip(zipedBackupFile, dirPath, isTemplateWithDescription);
      String configFName =
          tempDir.getAbsolutePath() + File.separator + DataBackupServiceImpl.CONFIG_FILE_NAME;

      CSVImporter csvImporter = new CSVImporter(configFName, tempDir.getAbsolutePath());
      csvImporter.addListener(
          new Listener() {
            String modelName;
            StringBuilder sb1 = new StringBuilder();

            @Override
            public void handle(Model bean, Exception e) {
              if (e.getMessage() != null && !e.getMessage().equals("null")) {
                if (bean != null) {
                  sb1.append(bean.getClass().getSimpleName() + " : \n" + e.getMessage() + "\n\n");
                } else {
                  sb1.append(e.getMessage() + "\n\n");
                }
              }
            }

            @Override
            public void imported(Model model) {
              modelName = model.getClass().getSimpleName();
            }

            @Override
            public void imported(Integer total, Integer count) {
              String str = "", strError = "";
              if (!StringUtils.isBlank(sb1)) {
                strError = "Errors : \n" + sb1.toString();
              }
              str = "Total Records :  {" + total + "} - Success Records :  {" + count + "}  \n";
              sb.append(modelName + " : \n");
              sb.append(strError).append(str + "-----------------------------------------\n");
              sb1.setLength(0);
            }
          });
      csvImporter.run();
      LOG.info("Data Restore Completed");
      FileUtils.cleanDirectory(new File(tempDir.getAbsolutePath()));
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmSS");
      String logFileName = "DataBackupLog_" + LocalDateTime.now().format(formatter) + ".log";

      File file = new File(tempDir.getAbsolutePath(), logFileName);
      PrintWriter pw = new PrintWriter(file);
      pw.write(sb.toString());
      pw.close();
      return file;
    } catch (IOException e) {
      TraceBackService.trace(e);
      return null;
    }
  }

  protected boolean unZip(
      MetaFile zipMetaFile, String destinationDirectoryPath, boolean isTemplateWithDescription)
      throws IOException, AxelorException {
    File zipFile = MetaFiles.getPath(zipMetaFile).toFile();
    try (ZipInputStream zis =
        new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
      ZipEntry ze;
      byte[] buffer = new byte[1024];
      int count;
      while ((ze = zis.getNextEntry()) != null) {
        File file = new File(destinationDirectoryPath, ze.getName());
        if (!file.toPath()
            .normalize()
            .startsWith(new File(destinationDirectoryPath).toPath().normalize())) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              String.format(BaseExceptionMessage.DATABACKUP_ERROR_1, ze.getName()));
        }

        if (!file.getParentFile().exists()) {
          file.getParentFile().mkdirs();
        }

        try (FileOutputStream fout = new FileOutputStream(file)) {
          while ((count = zis.read(buffer)) != -1) {
            fout.write(buffer, 0, count);
          }
        }
        if (isTemplateWithDescription && ze.getName().toLowerCase().endsWith(".csv")) {
          processCsv(file);
        }
        zis.closeEntry();
      }
      return true;
    }
  }

  public boolean sequencesOrMrpLineTypesExist() {
    long total = (long) JPA.em().createQuery("SELECT count(*) FROM Sequence").getSingleResult();
    long total1 = (long) JPA.em().createQuery("SELECT count(*) FROM MrpLineType").getSingleResult();
    return total > 0 || total1 > 0 ? true : false;
  }

  public Object importObjectWithByteArray(Object bean, Map<String, Object> values)
      throws IOException {
    assert bean instanceof Model;
    final Path path = (Path) values.get("__path__");

    Mapper mapper = Mapper.of(bean.getClass());
    for (String fieldName : values.keySet()) {
      if (fieldName.startsWith("byte_")) {
        String fileName = (String) values.get(fieldName);
        if (Strings.isNullOrEmpty((fileName))) {
          return bean;
        }
        try {
          final File image = path.resolve(fileName).toFile();
          byte[] bytes = new byte[(int) image.length()];
          bytes = java.nio.file.Files.readAllBytes(image.toPath());
          mapper.set(bean, fieldName.substring(5), bytes);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    return bean;
  }

  protected void processCsv(File file) throws IOException {
    CSVFile csvFormat = CSVFile.EXCEL.withDelimiter(SEPARATOR).withQuoteAll();
    List<CSVRecord> records;
    try (CSVParser parser = csvFormat.parse(file, StandardCharsets.UTF_8)) {
      records = parser.getRecords();
      if (CollectionUtils.isEmpty(records)) {
        return;
      }
      if (records.size() > 4) {
        records = records.subList(0, records.size() - 4);
      }
    }
    try (CSVPrinter printer = csvFormat.write(file)) {
      for (CSVRecord record : records) {
        printer.printRecord(record);
      }
    }
  }
}
