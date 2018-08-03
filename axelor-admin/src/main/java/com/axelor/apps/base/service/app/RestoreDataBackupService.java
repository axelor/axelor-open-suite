/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.IDataBackup;
import com.axelor.data.Listener;
import com.axelor.data.csv.CSVImporter;
import com.axelor.db.Model;
import com.axelor.meta.db.MetaFile;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestoreDataBackupService {

  private String dirPath = "";

  public File restore(MetaFile zipedBackupFile) {
    Logger LOG = LoggerFactory.getLogger(getClass());
    File dir = new File("temp");
    if (!dir.exists()) dir.mkdir();
    dirPath = dir.getAbsolutePath();
    CSVImporter csvImporter = null;
    StringBuilder sb = new StringBuilder();
    StringBuilder sb1 = new StringBuilder();
    File tempDir = new File(dirPath + IDataBackup.TEMP_FOLDER_NAME);
    if (!tempDir.exists()) tempDir.mkdir();
    try {
      unZip(dirPath + File.pathSeparator, zipedBackupFile.getFileName());
      for (int i = 1; i <= 2; i++) {
        csvImporter =
            new CSVImporter(
                tempDir.getAbsolutePath()
                    + File.pathSeparator
                    + IDataBackup.CONFIG_PREFIX
                    + i
                    + ".xml",
                tempDir.getAbsolutePath());
        csvImporter.addListener(
            new Listener() {
              String modelName;

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
                if (sb1.length() > 0) {
                  strError = "Errors : \n" + sb1.toString();
                } else {
                  strError = sb1.toString();
                }
                if (total != 0) {
                  str =
                      IDataBackup.TOTAL_IMPORT
                          + " {"
                          + total
                          + "} - "
                          + IDataBackup.SUCCESS_IMPORT
                          + " {"
                          + count
                          + "}  \n";
                  sb.append(modelName + " : \n")
                      .append(strError)
                      .append(str + IDataBackup.CONGIF_FILE_TABLE_LINE_SEPERATOR + "\n");
                  sb1.setLength(0);
                }
              }
            });
        csvImporter.run();
      }
      LOG.info(IDataBackup.IMPORT_COMPLETE);
      FileUtils.deleteDirectory(new File(tempDir.getAbsolutePath()));
      File file =
          new File(
              dir.getAbsolutePath()
                  + File.pathSeparator
                  + new Date().getTime()
                  + IDataBackup.LOG_FILE_NAME);
      PrintWriter pw = null;
      pw = new PrintWriter(file);
      pw.write(sb.toString());
      pw.close();
      return file;
    } catch (IOException e) {
      LOG.error(e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  private boolean unZip(String path, String zipname) throws IOException {
    File tempDir = new File(dirPath + IDataBackup.TEMP_FOLDER_NAME);
    if (!tempDir.exists()) tempDir.mkdir();
    InputStream is;
    ZipInputStream zis;
    String filename;
    is = new FileInputStream(path + zipname);
    zis = new ZipInputStream(new BufferedInputStream(is));
    ZipEntry ze;
    byte[] buffer = new byte[1024];
    int count;
    while ((ze = zis.getNextEntry()) != null) {
      filename = ze.getName();
      if (ze.isDirectory()) {
        File fmd = new File(path + filename);
        fmd.mkdirs();
        continue;
      }
      FileOutputStream fout =
          new FileOutputStream(tempDir.getAbsolutePath() + File.pathSeparator + filename);
      while ((count = zis.read(buffer)) != -1) {
        fout.write(buffer, 0, count);
      }
      fout.close();
      zis.closeEntry();
    }
    zis.close();
    return true;
  }
}
