/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.app;

import com.axelor.common.StringUtils;
import com.axelor.data.Listener;
import com.axelor.data.csv.CSVImporter;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.io.Files;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataBackupRestoreService {

  /* Restore the Data using provided zip File and prepare Log File and Return it*/
  public File restore(MetaFile zipedBackupFile) {
    Logger LOG = LoggerFactory.getLogger(getClass());
    File tempDir = Files.createTempDir();
    String dirPath = tempDir.getAbsolutePath();
    StringBuilder sb = new StringBuilder();
    try {
      unZip(zipedBackupFile, dirPath);
      String configFName =
          tempDir.getAbsolutePath() + File.separator + DataBackupServiceImpl.configFileName;

      CSVImporter csvImporter = new CSVImporter(configFName, tempDir.getAbsolutePath());
      csvImporter.addListener(
          new Listener() {
            String modelName;
            StringBuilder sb1 = new StringBuilder();

            @Override
            public void handle(Model bean, Exception e) {
              if (bean != null) {
                sb1.append(bean.getClass().getSimpleName() + " : \n" + e.getMessage() + "\n\n");
              } else {
                sb1.append(e.getMessage() + "\n\n");
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
              if (total != 0 && count != 0) {
                sb.append(modelName + " : \n");
              }
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

  private boolean unZip(MetaFile zipMetaFile, String destinationDirectoryPath) throws IOException {
    File zipFile = MetaFiles.getPath(zipMetaFile).toFile();
    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
    ZipEntry ze;
    byte[] buffer = new byte[1024];
    int count;
    while ((ze = zis.getNextEntry()) != null) {
      FileOutputStream fout =
          new FileOutputStream(new File(destinationDirectoryPath, ze.getName()));
      while ((count = zis.read(buffer)) != -1) {
        fout.write(buffer, 0, count);
      }
      fout.close();
      zis.closeEntry();
    }
    zis.close();
    return true;
  }

  public boolean SeuencesExist() {
    long total = (long) JPA.em().createQuery("SELECT count(*) FROM Sequence").getSingleResult();
    long total1 = (long) JPA.em().createQuery("SELECT count(*) FROM MrpLineType").getSingleResult();
    return total > 0 || total1 > 0 ? true : false;
  }
}
