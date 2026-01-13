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
package com.axelor.apps.base.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipExtractionTool {

  private ZipExtractionTool() {}

  public static void extractZipFile(File zipFile, File destDir) throws IOException {

    String canonicalDestDirPath = destDir.getCanonicalPath();

    try (ZipInputStream zis =
        new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
      ZipEntry ze;
      byte[] buffer = new byte[1024];
      int count;

      while ((ze = zis.getNextEntry()) != null) {
        File file = new File(destDir, ze.getName());
        String canonicalFilePath = file.getCanonicalPath();

        // Path traversal protection
        if (!canonicalFilePath.startsWith(canonicalDestDirPath + File.separator)) {
          throw new IOException("Entry is outside of the target dir: " + ze.getName());
        }

        if (ze.isDirectory()) {
          if (!file.exists() && !file.mkdirs()) {
            throw new IOException("Failed to create directory: " + file);
          }
        } else {
          File parent = file.getParentFile();
          if (!parent.exists() && !parent.mkdirs()) {
            throw new IOException("Failed to create directory: " + parent);
          }

          try (FileOutputStream fout = new FileOutputStream(file)) {
            while ((count = zis.read(buffer)) != -1) {
              fout.write(buffer, 0, count);
            }
          }
        }

        zis.closeEntry();
      }
    }
  }
}
