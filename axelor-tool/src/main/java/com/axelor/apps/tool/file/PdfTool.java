/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.List;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.meta.MetaFiles;

public final class PdfTool {

    private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    private PdfTool() {
    }

    /**
     * Append multiple PDF files into one PDF.
     *
     * @param fileList a list of path of PDF files to merge.
     * @param fileName the output file name.
     * @return The link to access the generated PDF.
     */
    public static String mergePdf(List<File> fileList, String fileName) throws IOException {
        PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        for (File file : fileList) {
            pdfMergerUtility.addSource(file);
        }
        Path tmpFile = MetaFiles.createTempFile(null, "");
        FileOutputStream stream = new FileOutputStream(tmpFile.toFile());
        pdfMergerUtility.setDestinationStream(stream);
        pdfMergerUtility.mergeDocuments(null);

        String fileLink = "ws/files/report/" + tmpFile.toFile().getName();
        try {
            fileLink += "?name=" + URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getLocalizedMessage());
        }
        return fileLink;
    }
}
