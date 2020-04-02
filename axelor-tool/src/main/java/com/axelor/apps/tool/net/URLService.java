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
package com.axelor.apps.tool.net;

import com.axelor.apps.tool.exception.IExceptionMessage;
import com.axelor.apps.tool.file.FileTool;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.net.URLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class URLService {

  static final int size = 1024;

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Test la validité d'une url.
   *
   * @param url L'URL à tester.
   * @return
   */
  public static String notExist(String url) {

    if (Strings.isNullOrEmpty(url)) {
      return I18n.get(IExceptionMessage.URL_SERVICE_1);
    }

    try {
      URL fileURL = new URL(url);
      fileURL.openConnection().connect();
      return null;
    } catch (java.net.MalformedURLException ex) {
      ex.printStackTrace();
      return String.format(I18n.get(IExceptionMessage.URL_SERVICE_2), url);
    } catch (java.io.IOException ex) {
      ex.printStackTrace();
      return String.format(I18n.get(IExceptionMessage.URL_SERVICE_3), url);
    }
  }

  public static void fileUrl(
      File file, String fAddress, String localFileName, String destinationDir) throws IOException {
    int ByteRead, ByteWritten = 0;
    byte[] buf = new byte[size];

    URL Url = new URL(fAddress);
    OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
    URLConnection urlConnection = Url.openConnection();
    InputStream inputStream = urlConnection.getInputStream();

    while ((ByteRead = inputStream.read(buf)) != -1) {
      outputStream.write(buf, 0, ByteRead);
      ByteWritten += ByteRead;
    }

    LOG.info("Downloaded Successfully.");
    LOG.debug("No of bytes :" + ByteWritten);

    if (inputStream != null) {
      inputStream.close();
    }
    if (outputStream != null) {
      outputStream.close();
    }
  }

  public static File fileDownload(String fAddress, String destinationDir, String fileName)
      throws IOException {

    int slashIndex = fAddress.lastIndexOf('/');
    int periodIndex = fAddress.lastIndexOf('.');

    if (periodIndex >= 1 && slashIndex >= 0 && slashIndex < fAddress.length() - 1) {
      LOG.debug("Downloading file {} from {} to {}", fileName, fAddress, destinationDir);

      File file = FileTool.create(destinationDir, fileName);

      fileUrl(file, fAddress, fileName, destinationDir);

      return file;

    } else {
      LOG.error("Destination path or filename is not well formatted.");
      return null;
    }
  }

  public static void fileDownload(
      File file, String fAddress, String destinationDir, String fileName) throws IOException {

    int slashIndex = fAddress.lastIndexOf('/');
    int periodIndex = fAddress.lastIndexOf('.');

    if (periodIndex >= 1 && slashIndex >= 0 && slashIndex < fAddress.length() - 1) {
      LOG.debug("Downloading file {} from {} to {}", fileName, fAddress, destinationDir);
      fileUrl(file, fAddress, fileName, destinationDir);
    } else {
      LOG.error("Destination path or filename is not well formatted.");
    }
  }
}
