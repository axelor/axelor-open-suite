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
package com.axelor.apps.base.service.advanced.imports;

import com.axelor.apps.base.db.AdvancedImport;
import com.axelor.apps.base.db.FileField;
import com.axelor.apps.base.db.FileTab;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.data.csv.CSVBind;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DataImportService {

  public ImportHistory importData(AdvancedImport advanceImport)
      throws IOException, AxelorException, ClassNotFoundException;

  public Map<String, Object> createJsonContext(FileTab fileTab);

  public CSVBind createCSVBind(
      String column,
      String field,
      String search,
      String expression,
      String adapter,
      Boolean update);

  public void writeSelectionData(
      String selection, String dataCell, int forSelectUse, List<String> dataList);

  public void createBindForNotMatchWithFile(
      String column,
      int importType,
      CSVBind dummyBind,
      String expression,
      String adapter,
      CSVBind parentBind,
      Property childProp);

  public void createBindForMatchWithFile(
      String column,
      int importType,
      String expression,
      String adapter,
      String relationship,
      CSVBind parentBind,
      Property childProp);

  public String getAdapter(String type, String dateFormat);

  public String setExpression(String column, FileField fileField, String selection);

  public void checkSubFieldAndWriteData(
      String[] subFields,
      int index,
      Property parentProp,
      String dataCell,
      int forSelectUse,
      List<String> dataList)
      throws ClassNotFoundException;

  public void setImportIf(Boolean isRequired, CSVBind bind, String column);

  public void setSearch(
      String column,
      String field,
      Property prop,
      FileField fileField,
      CSVBind bind,
      int index,
      boolean isSameParentExist)
      throws ClassNotFoundException;
}
