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

import com.axelor.apps.base.db.Print;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.wordreport.config.ReportValueService;
import com.axelor.apps.base.service.wordreport.config.WordReportHelperService;
import com.axelor.apps.base.service.wordreport.config.WordReportQueryBuilderService;
import com.axelor.apps.base.service.wordreport.config.WordReportTableService;
import com.axelor.apps.base.service.wordreport.config.WordReportTranslationService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.script.ScriptException;
import org.apache.commons.io.FilenameUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;

public class WordReportProcessService {

  @Inject WordReportHelperService helperService;
  @Inject ReportValueService valueService;
  @Inject WordReportTableService tableService;
  @Inject WordReportTranslationService translationService;
  @Inject WordReportQueryBuilderService queryBuilderService;

  public File processWordFile(Print print, List<Model> modelList)
      throws Docx4JException, AxelorException, ClassNotFoundException, ScriptException,
          IOException {
    WordprocessingMLPackage originWMLPackage =
        getWMLPackage(MetaFiles.getPath(print.getWordTemplate()).toFile());
    WordprocessingMLPackage newWMLPackage = cloneWMLPackage(originWMLPackage);
    this.traverse(newWMLPackage, modelList.get(0), print);

    File processedWordFile =
        MetaFiles.createTempFile(I18n.get(print.getMetaModel().getName()), ".docx").toFile();
    newWMLPackage.save(processedWordFile);

    return processedWordFile;
  }

  private void traverse(WordprocessingMLPackage wmlPackage, Object object, Print print)
      throws ClassNotFoundException, AxelorException, ScriptException, IOException {
    Mapper mapper = helperService.getMapper(print.getMetaModel().getFullName());
    List<Object> textObjectList =
        helperService.getAllElementFromObject(wmlPackage.getMainDocumentPart(), Text.class);
    List<Object> tableObjectList =
        helperService.getAllElementFromObject(wmlPackage.getMainDocumentPart(), Tbl.class);
    ResourceBundle resourceBundle = translationService.getResourceBundle(print);
    Map<String, List<Object>> reportQueryBuilderResultMap =
        queryBuilderService.getAllReportQueryBuilderResult(
            print.getReportQueryBuilderList(), object);
    // process text objects
    for (Object ob : textObjectList) {
      Text text = (Text) ob;
      valueService.setTextValue(mapper, text, object, resourceBundle, reportQueryBuilderResultMap);
    }
    // process table objects
    for (Object ob : tableObjectList) {
      Tbl table = (Tbl) ob;
      tableService.setTable(table, mapper, object, resourceBundle, reportQueryBuilderResultMap);
    }
  }

  private WordprocessingMLPackage getWMLPackage(File wordFile)
      throws Docx4JException, AxelorException {

    WordprocessingMLPackage wordprocessingMLPackage = null;
    try {
      if (FilenameUtils.getExtension(wordFile.getName()).equalsIgnoreCase("DOCX")) {
        wordprocessingMLPackage = WordprocessingMLPackage.load(wordFile);
      }
    } catch (Exception e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.WML_PACKAGE_RETRIEVAL_FAILED));
    }
    return wordprocessingMLPackage;
  }

  private WordprocessingMLPackage cloneWMLPackage(WordprocessingMLPackage originalPackage) {
    WordprocessingMLPackage clonedPackage = null;
    if (ObjectUtils.notEmpty(originalPackage)) {
      clonedPackage = (WordprocessingMLPackage) originalPackage.clone();
    }
    return clonedPackage;
  }
}
