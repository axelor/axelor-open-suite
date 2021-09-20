/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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

import com.axelor.exception.AxelorException;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

public final class CsvTool {

  private CsvTool() {}

  /**
   * Create new {@link CSVReader} with the given reader, separator, quote and escape char.
   *
   * @param reader the reader
   * @param separator separator char
   * @param quoteChar quote char
   * @param escapeChar escape char
   * @return new instance of {@link CSVReader}
   */
  public static CSVReader newCSVReader(
      Reader reader, char separator, char quoteChar, char escapeChar) {
    return new CSVReaderBuilder(reader)
        .withCSVParser(
            new CSVParserBuilder()
                .withSeparator(separator)
                .withQuoteChar(quoteChar)
                .withEscapeChar(escapeChar)
                .build())
        .build();
  }

  /**
   * Create new {@link CSVReader} with the given reader, separator, quote char.
   *
   * @param reader the reader
   * @param separator separator char
   * @param quoteChar quote char
   * @return new instance of {@link CSVReader}
   */
  public static CSVReader newCSVReader(Reader reader, char separator, char quoteChar) {
    return newCSVReader(reader, separator, quoteChar, CSVParser.DEFAULT_ESCAPE_CHARACTER);
  }

  /**
   * Create new {@link CSVReader} with the given reader, separator.
   *
   * @param reader the reader
   * @param separator separator char
   * @return new instance of {@link CSVReader}
   */
  public static CSVReader newCSVReader(Reader reader, char separator) {
    return newCSVReader(reader, separator, CSVParser.DEFAULT_QUOTE_CHARACTER);
  }

  /**
   * Create new {@link CSVReader} with the given reader.
   *
   * @param reader the reader
   * @return new instance of {@link CSVReader}
   */
  public static CSVReader newCSVReader(Reader reader) {
    return newCSVReader(reader, CSVParser.DEFAULT_SEPARATOR);
  }

  /**
   * Create new {@link CSVWriter} with the given writer, separator, quote, escape char and line
   * ending.
   *
   * @param writer the writer
   * @param separator separator char
   * @param quoteChar quote char
   * @param escapeChar escape char
   * @param lineEnd line ending
   * @return new instance of {@link CSVWriter}
   */
  public static CSVWriter newCSVWriter(
      Writer writer, char separator, char quoteChar, char escapeChar, String lineEnd) {
    return new CSVWriter(writer, separator, quoteChar, escapeChar, lineEnd);
  }

  /**
   * Create new {@link CSVWriter} with the given writer, separator, quote char and line ending.
   *
   * @param writer the writer
   * @param separator separator char
   * @param quoteChar quote char
   * @param lineEnd line ending
   * @return new instance of {@link CSVWriter}
   */
  public static CSVWriter newCSVWriter(
      Writer writer, char separator, char quoteChar, String lineEnd) {
    return newCSVWriter(writer, separator, quoteChar, ICSVWriter.DEFAULT_ESCAPE_CHARACTER, lineEnd);
  }

  /**
   * Create new {@link CSVWriter} with the given writer, separator, quote char.
   *
   * @param writer the writer
   * @param separator separator char
   * @param quoteChar quote char
   * @return new instance of {@link CSVWriter}
   */
  public static CSVWriter newCSVWriter(Writer writer, char separator, char quoteChar) {
    return newCSVWriter(writer, separator, quoteChar, ICSVWriter.DEFAULT_LINE_END);
  }

  /**
   * Create new {@link CSVWriter} with the given writer, separator.
   *
   * @param writer the writer
   * @param separator separator char
   * @return new instance of {@link CSVWriter}
   */
  public static CSVWriter newCSVWriter(Writer writer, char separator) {
    return newCSVWriter(writer, separator, ICSVWriter.DEFAULT_QUOTE_CHARACTER);
  }

  /**
   * Create new {@link CSVWriter} with the given writer, separator.
   *
   * @param writer the writer
   * @param separator separator char
   * @return new instance of {@link CSVWriter}
   */
  public static CSVWriter newCSVWriter(Writer writer) {
    return newCSVWriter(writer, ICSVWriter.DEFAULT_SEPARATOR);
  }

  /**
   * Méthode permettant de lire le contenu d'un fichier
   *
   * @param fileName Le nom du fichier
   * @return Une liste de tableau de valeur contenant l'ensemble des lignes
   * @throws IOException
   * @throws AxelorException
   */
  public static List<String[]> cSVFileReader(String fileName, char separator)
      throws IOException, CsvException {

    CSVReader reader;
    List<String[]> myEntries;

    reader = newCSVReader(new FileReader(fileName), separator);
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
    return newCSVWriter(w, separator, CSVWriter.NO_QUOTE_CHARACTER, "\r\n");
  }

  public static CSVWriter setCsvFile(
      final String filePath, final String fileName, char separator, char quoteChar)
      throws IOException {

    java.io.Writer w = new FileWriter(filePath + File.separator + fileName);
    return newCSVWriter(w, separator, quoteChar, "\r\n");
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
