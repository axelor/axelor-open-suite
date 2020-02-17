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
package com.axelor.csv.script;

import com.axelor.apps.account.db.AccountChart;
import com.axelor.common.FileUtils;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.io.Files;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportAccountChart {

  private final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private MetaFiles metaFiles;

  private File getDataFile(AccountChart accountChart) throws IOException {

    File tempDir = new File(Files.createTempDir(), accountChart.getCode());
    if (!tempDir.exists()) tempDir.mkdir();

    String chartPath =
        "/l10n/l10n_" + accountChart.getCountryCode() + "/" + accountChart.getCode() + "/";

    String[] files =
        new String[] {
          "account_account.csv",
          "account_accountEquiv.csv",
          "account_accountType.csv",
          "account_fiscalPosition.csv",
          "account_tax.csv",
          "account_taxAccount.csv",
          "account_taxEquiv.csv",
          "account_taxLine.csv"
        };

    for (String fileName : Arrays.asList(files)) {

      File resourceFile = new File(tempDir, fileName);
      String resource = chartPath + fileName;

      InputStream inputStream = this.getClass().getResourceAsStream(resource);
      if (inputStream == null) {
        continue;
      }

      try (FileOutputStream outputStream = new FileOutputStream(resourceFile)) {
        int read = 0;
        byte[] bytes = new byte[1024];
        while ((read = inputStream.read(bytes)) != -1) {
          outputStream.write(bytes, 0, read);
        }
        outputStream.close();
      }
    }
    return tempDir;
  }

  private File getZipFile(AccountChart accountChart) throws IOException {

    File directory = this.getDataFile(accountChart);
    String fileName = accountChart.getCode() + "_" + accountChart.getCountryCode() + ".zip";

    File zipFile = new File(directory.getParent(), fileName);

    this.zipIt(directory, zipFile);

    return zipFile;
  }

  private void zipIt(File directory, File zipFile) throws IOException {

    byte[] buffer = new byte[1024];

    try (FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(fos)) {

      for (File file : directory.listFiles()) {

        ZipEntry ze = new ZipEntry(file.getName());
        zos.putNextEntry(ze);

        try (FileInputStream in = new FileInputStream(file)) {

          int len;
          while ((len = in.read(buffer)) > 0) {
            zos.write(buffer, 0, len);
          }
          in.close();
        }
        zos.closeEntry();
      }
      zos.close();
      fos.close();
    }
  }

  public Object importAccountChart(Object bean, Map<String, Object> values) throws IOException {

    assert bean instanceof AccountChart;
    AccountChart accountChart = (AccountChart) bean;

    File zipFile = this.getZipFile(accountChart);

    try {
      final MetaFile metaFile = metaFiles.upload(zipFile);
      accountChart.setMetaFile(metaFile);

    } catch (Exception e) {
      e.printStackTrace();
      LOG.warn("Can't load file {} for accountChart {}", zipFile.getName(), accountChart.getName());
    }

    FileUtils.deleteDirectory(zipFile.getParentFile());

    return accountChart;
  }
}
