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
package com.axelor.apps.base.utils;

import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.utils.exception.UtilsExceptionMessage;
import com.axelor.utils.helpers.ExceptionHelper;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

// This class is a copy a PdfHelper from axelor-utils to prevent dependencies issues.
public final class PdfHelper {

  private PdfHelper() {}

  /**
   * Merge pdf, then return the webservice url to the created file.
   *
   * @param fileList a list of files to merge
   * @param fileName the name of the created file
   * @return the link to the file
   * @throws IOException
   */
  public static String mergePdfToFileLink(List<File> fileList, String fileName) throws IOException {
    return getFileLinkFromPdfFile(mergePdf(fileList), fileName);
  }

  /**
   * Append multiple PDF files into one PDF.
   *
   * @param fileList a list of path of PDF files to merge.
   * @return The link to access the generated PDF.
   */
  public static File mergePdf(List<File> fileList) throws IOException {
    PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
    for (File file : fileList) {
      pdfMergerUtility.addSource(file);
    }
    Path tmpFile = MetaFiles.createTempFile(null, "");
    FileOutputStream stream = new FileOutputStream(tmpFile.toFile());
    pdfMergerUtility.setDestinationStream(stream);
    pdfMergerUtility.mergeDocuments(null);
    return tmpFile.toFile();
  }

  /**
   * Return a webservice url to get a printed pdf with a defined name.
   *
   * @param file the printed report
   * @param fileName the file name
   * @return the url
   */
  public static String getFileLinkFromPdfFile(File file, String fileName) {

    String fileLink = "ws/files/report/" + file.getName();
    try {
      fileLink += "?name=" + URLEncoder.encode(fileName, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      ExceptionHelper.trace(e);
    }
    return fileLink;
  }

  /**
   * Allows to get a PDF with multiple copies.
   *
   * @param file the PDF to copy
   * @param copyNumber the number of copies
   * @return a new file with the number of asked copies.
   * @throws IllegalArgumentException if copy number is inferior or equal to 0.
   * @throws IOException if mergePdf fails to write the new file.
   */
  public static File printCopiesToFile(File file, int copyNumber) throws IOException {
    Preconditions.checkArgument(
        copyNumber > 0, I18n.get(UtilsExceptionMessage.BAD_COPY_NUMBER_ARGUMENT));
    List<File> invoicePrintingToMerge = Collections.nCopies(copyNumber, file);
    return mergePdf(invoicePrintingToMerge);
  }
}
