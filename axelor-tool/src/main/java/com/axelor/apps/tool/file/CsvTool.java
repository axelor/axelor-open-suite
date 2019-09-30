/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.tool.file;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public final class CsvTool {

  private CsvTool() {}

  /**
   * Méthode permettant de lire le contenu d'un fichier
   *
   * @param fileName Le nom du fichier
   * @return Une liste de tableau de valeur contenant l'ensemble des lignes
   * @throws IOException
   * @throws AxelorException
   */
  public static List<String[]> cSVFileReader(String fileName, char separator) throws IOException {

    CSVReader reader;
    List<String[]> myEntries;

    reader = new CSVReader(new FileReader(fileName), separator);
    myEntries = reader.readAll();
    reader.close();

    return myEntries;
  }

  /*
   * Format Windows, sans double quote et CR/LF à chaque fin de ligne
   */
  public static CSVWriter setCsvFile(final String filePath, final String fileName, char separator)
      throws IOException {

    java.io.Writer w = new FileWriter(filePath + File.separator + fileName);
    return new CSVWriter(w, separator, CSVWriter.NO_QUOTE_CHARACTER, "\r\n");
  }

  public static CSVWriter setCsvFile(
      final String filePath, final String fileName, char separator, char quoteChar)
      throws IOException {

    java.io.Writer w = new FileWriter(filePath + File.separator + fileName);
    return new CSVWriter(w, separator, quoteChar, "\r\n");
  }

  public static void csvWriter(
      String filePath, String fileName, char separator, String[] headers, List<String[]> dataList)
      throws IOException {
    CSVWriter reconWriter = setCsvFile(filePath, fileName, separator);
    if (headers != null) {
      reconWriter.writeNext(headers);
    }
    reconWriter.writeAll(dataList);
    reconWriter.flush();
    try {
      reconWriter.close();
    } catch (IOException e) {

      reconWriter = null;
    }
  }

  public static void csvWriter(
      String filePath,
      String fileName,
      char separator,
      char quoteChar,
      String[] headers,
      List<String[]> dataList)
      throws IOException {
    CSVWriter reconWriter = setCsvFile(filePath, fileName, separator, quoteChar);
    if (headers != null) {
      reconWriter.writeNext(headers);
    }
    reconWriter.writeAll(dataList);
    reconWriter.flush();
    try {
      reconWriter.close();
    } catch (IOException e) {

      reconWriter = null;
    }
  }
}
