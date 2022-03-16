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
package com.axelor.apps.base.service.wordreport.config;

import com.axelor.apps.base.db.ReportQueryBuilder;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.script.ScriptException;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;

public interface WordReportQueryBuilderService {

  public Map<String, List<Object>> getAllReportQueryBuilderResult(
      Set<ReportQueryBuilder> reportQueryBuilderList, Object bean);

  public void setReportQueryTextValue(
      Text text,
      List<Object> collection,
      String value,
      String operationString,
      ResourceBundle resourceBundle)
      throws ClassNotFoundException, ScriptException;

  public List<String> getReportQueryColumnData(
      Tbl table,
      String value,
      List<Object> collection,
      String operationString,
      ResourceBundle resourceBundle)
      throws ClassNotFoundException, ScriptException;
}
