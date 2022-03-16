/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.wordreport;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.Print;
import com.axelor.apps.base.service.Html2PdfConvertor;
import com.axelor.apps.base.service.wordreport.html.Word2HtmlConvertor;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.script.ScriptException;
import javax.xml.parsers.ParserConfigurationException;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.xml.sax.SAXException;

public class WordReportServiceImpl implements WordReportService {

  @Inject WordReportProcessService wordReportProcessService;
  @Inject Html2PdfConvertor html2PdfConvertor;

  public File createReport(Long objectId, Print print)
      throws ClassNotFoundException, Docx4JException, AxelorException, ScriptException, IOException,
          ParserConfigurationException, SAXException {
    File processedWordFile = null;
    File outputFile = null;

    List<Model> modelList =
        this.getModelData(print.getMetaModel().getFullName(), Arrays.asList(objectId));
    processedWordFile = wordReportProcessService.processWordFile(print, modelList);

    if (print.getFormatSelect().equalsIgnoreCase("WORD")) {
      outputFile = processedWordFile;
    } else if (print.getFormatSelect().equalsIgnoreCase("PDF")) {
      Word2HtmlConvertor convertor = new Word2HtmlConvertor();
      File htmlFile = convertor.convertToHtml(processedWordFile, print);
      String attachmentPath = AppSettings.get().getPath("file.upload.dir", "");
      File pdfFile = html2PdfConvertor.toPdf(htmlFile, attachmentPath, print);
      outputFile = pdfFile;
    }

    return outputFile;
  }

  @SuppressWarnings("unchecked")
  protected <T extends Model> List<T> getModelData(String modelFullName, List<Long> ids)
      throws ClassNotFoundException {
    Class<T> modelClass = (Class<T>) Class.forName(modelFullName);
    return JpaRepository.of(modelClass).all().filter("id in :ids").bind("ids", ids).fetch();
  }
}
