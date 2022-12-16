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

import com.axelor.apps.base.db.FileField;
import com.axelor.apps.base.db.FileTab;
import com.axelor.data.csv.CSVBind;
import com.axelor.data.csv.CSVInputJson;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.db.MetaJsonField;
import java.util.List;
import java.util.Map;

public interface CustomDataImportService {

  public CSVInputJson createCSVInputJson(
      FileTab fileTab, String fileName, String inputCallable, char csvSepartor);

  public void checkAndWriteJsonData(String dataCell, FileField fileField, List<String> dataList)
      throws ClassNotFoundException;

  public void checkJsonSubFieldAndWriteData(
      String[] subFields,
      int index,
      MetaJsonField parentField,
      String dataCell,
      int forSelectUse,
      List<String> dataList)
      throws ClassNotFoundException;

  public List<CSVBind> createNameFieldBinding(String fieldname, List<CSVBind> bindList);

  public CSVBind createAttrsCSVBinding(
      String[] subFields,
      int index,
      String column,
      String modelName,
      FileField fileField,
      CSVBind parentBind,
      CSVBind dummyBind,
      boolean isSameParentExist,
      List<CSVBind> allBindings,
      Map<String, CSVBind> parentBindMap,
      String fullFieldName)
      throws ClassNotFoundException;

  public void createCustomCSVSubBinding(
      String[] subFields,
      int index,
      String column,
      MetaJsonField parentField,
      FileField fileField,
      CSVBind parentBind,
      CSVBind dummyBind,
      boolean isSameParentExist,
      Map<String, CSVBind> subBindMap,
      String fullFieldName)
      throws ClassNotFoundException;

  public void createChildCSVSubBinding(
      FileField fileField,
      Property parentProp,
      CSVBind parentBind,
      CSVBind dummyBind,
      String fieldName,
      String column,
      String relationship,
      int importType,
      int index,
      boolean isSameParentExist)
      throws ClassNotFoundException;

  public CSVBind checkAvailableSubBinding(
      Map<String, CSVBind> subBindMap,
      CSVBind parentBind,
      CSVBind subBind,
      String fieldName,
      String key,
      int importType);

  public void addJsonModelAndSearch(
      List<CSVBind> allBindings,
      Map<Object, Object> searchMap,
      CSVInputJson jsonCsvInput,
      String model)
      throws ClassNotFoundException;

  public void addCustomObjectNameField(FileField fileField, String column);

  public String appendSearchFieldFilter(FileField fileField, String model, String column)
      throws ClassNotFoundException;

  public void appendSearchForJsonField(
      StringBuilder preparedFilter,
      MetaJsonField jsonField,
      FileField fileField,
      String column,
      int index)
      throws ClassNotFoundException;

  public void appendSearchForAttrsField(
      StringBuilder preparedFilter,
      FileField fileField,
      Property parentProp,
      String column,
      int index)
      throws ClassNotFoundException;

  public void setSearch(
      String column,
      String field,
      FileField fileField,
      MetaJsonField jsonField,
      CSVBind bind,
      int index,
      boolean isSameParentExist)
      throws ClassNotFoundException;

  public void checkAndAppendAttrsSearch(
      StringBuilder searchString,
      String[] subFields,
      FileField fileField,
      Mapper mapper,
      String column,
      int index,
      boolean isJson,
      boolean isReal,
      boolean isAppend)
      throws ClassNotFoundException;
}
