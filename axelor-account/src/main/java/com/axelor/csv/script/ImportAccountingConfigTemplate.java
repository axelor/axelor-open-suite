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
package com.axelor.csv.script;

import com.axelor.apps.account.db.AccountingConfigTemplate;
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

public class ImportAccountingConfigTemplate {

  private final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private MetaFiles metaFiles;

  protected File getDataFile(AccountingConfigTemplate accountingConfigTemplate) throws IOException {

    File tempDir = new File(Files.createTempDir(), accountingConfigTemplate.getCode());
    if (!tempDir.exists()) tempDir.mkdir();

    String chartPath =
        "/l10n/l10n_"
            + accountingConfigTemplate.getCountryCode()
            + "/"
            + accountingConfigTemplate.getCode()
            + "/";

    String[] files =
        new String[] {
          "account_account.csv",
          "account_fiscalPosition.csv",
          "account_accountEquiv.csv",
          "account_accountType.csv",
          "account_tax.csv",
          "account_accountManagement.csv",
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

  protected File getZipFile(AccountingConfigTemplate accountingConfigTemplate) throws IOException {

    File directory = this.getDataFile(accountingConfigTemplate);
    String fileName = accountingConfigTemplate.getCode() + ".zip";

    File zipFile = new File(directory.getParent(), fileName);

    this.zipIt(directory, zipFile);

    return zipFile;
  }

  protected void zipIt(File directory, File zipFile) throws IOException {

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

  public Object importAccountingConfigTemplate(Object bean, Map<String, Object> values)
      throws IOException {

    assert bean instanceof AccountingConfigTemplate;
    AccountingConfigTemplate accountConfigTemplate = (AccountingConfigTemplate) bean;

    File zipFile = this.getZipFile(accountConfigTemplate);

    try {
      final MetaFile metaFile = metaFiles.upload(zipFile);
      accountConfigTemplate.setMetaFile(metaFile);

    } catch (Exception e) {
      e.printStackTrace();
      LOG.warn(
          "Can't load file {} for accountConfigTemplate {}",
          zipFile.getName(),
          accountConfigTemplate.getName());
    }

    FileUtils.deleteDirectory(zipFile.getParentFile());

    return accountConfigTemplate;
  }
}
