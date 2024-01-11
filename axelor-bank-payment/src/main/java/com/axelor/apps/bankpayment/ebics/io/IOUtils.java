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
package com.axelor.apps.bankpayment.ebics.io;

/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */

import com.axelor.apps.bankpayment.ebics.interfaces.ContentFactory;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Some IO utilities for EBICS files management. EBICS server
 *
 * @author hachani
 */
public class IOUtils {

  /**
   * Creates a directory from a root one
   *
   * @param parent the parent directory
   * @param child the directory name
   * @return The created directory
   */
  public static File createDirectory(File parent, String child) {
    File directory;

    directory = new File(parent, child);
    directory.mkdir();

    return directory;
  }

  /**
   * Creates a directory from a root one
   *
   * @param parent the parent directory
   * @param child the directory name
   * @return The created directory
   */
  public static File createDirectory(String parent, String child) {
    File directory;

    directory = new File(parent, child);
    directory.mkdir();

    return directory;
  }

  /**
   * Creates a directory from a directory name
   *
   * @param name the absolute directory name
   * @return The created directory
   */
  public static File createDirectory(String name) {
    File directory;

    directory = new File(name);
    directory.mkdir();

    return directory;
  }

  /**
   * Creates many directories from a given full path. Path should use default separator like '/' for
   * UNIX systems
   *
   * @param fullName the full absolute path of the directories
   * @return The created directory
   */
  public static File createDirectories(String fullName) {
    File directory;

    directory = new File(fullName);
    directory.mkdirs();

    return directory;
  }

  /**
   * Creates a new <code>java.io.File</code> from a given root.
   *
   * @param parent the parent of the file.
   * @param name the file name.
   * @return the created file.
   */
  public static File createFile(String parent, String name) {
    File file;

    file = new File(parent, name);

    return file;
  }

  /**
   * Creates a new <code>java.io.File</code> from a given root.
   *
   * @param parent the parent of the file.
   * @param name the file name.
   * @return the created file.
   */
  public static File createFile(File parent, String name) {
    File file;

    file = new File(parent, name);

    return file;
  }

  /**
   * Creates a file from its name. The name can be absolute if only the directory tree is created
   *
   * @param name the file name
   * @return the created file
   */
  public static File createFile(String name) {
    File file;

    file = new File(name);

    return file;
  }

  /**
   * Returns the content of a file as byte array.
   *
   * @param path the file path
   * @return the byte array content of the file
   * @throws EbicsException
   */
  public static byte[] getFileContent(String path) throws AxelorException {
    try (InputStream input = new FileInputStream(path)) {
      byte[] content;

      content = new byte[input.available()];
      input.read(content);
      return content;
    } catch (IOException e) {
      throw new AxelorException(
          e.getCause(), TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
  }

  /**
   * Returns the content of a <code>ContentFactory</code> as a byte array
   *
   * @param content
   * @return
   * @throws EbicsException
   */
  public static byte[] getFactoryContent(ContentFactory content) throws AxelorException {
    try {
      byte[] buffer;
      ByteArrayOutputStream out;
      InputStream in;
      int len = -1;

      out = new ByteArrayOutputStream();
      in = content.getContent();
      buffer = new byte[1024];
      while ((len = in.read(buffer)) != -1) {
        out.write(buffer, 0, len);
      }
      in.close();
      out.close();
      return out.toByteArray();
    } catch (IOException e) {
      throw new AxelorException(
          e.getCause(), TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
  }
}
