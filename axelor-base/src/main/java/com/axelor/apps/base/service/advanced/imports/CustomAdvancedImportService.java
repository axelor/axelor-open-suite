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
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.rpc.JsonContext;
import java.util.Arrays;
import java.util.List;

public interface CustomAdvancedImportService {

  public static final List<String> relationTypeList =
      Arrays.asList(
          "many-to-many",
          "many-to-one",
          "one-to-many",
          "one-to-one",
          "json-one-to-many",
          "json-many-to-many",
          "json-one-to-one",
          "json-many-to-one");

  public boolean checkJsonField(FileTab fileTab, String importField, String subImportField)
      throws AxelorException, ClassNotFoundException;

  public boolean checkAttrsSubField(
      String[] subFields, int index, String uniqueModel, String importField, String model)
      throws AxelorException, ClassNotFoundException;

  public void setJsonFields(
      FileTab fileTab, FileField fileField, String importField, String subImportField);

  public void removeJsonRecords(List<FileTab> fileTabList) throws ClassNotFoundException;

  public void removeJsonSubRecords(
      Class<? extends Model> klass, List<MetaJsonField> jsonFields, JsonContext jsonContext)
      throws ClassNotFoundException;

  public MetaJsonField getJsonField(
      String fieldName, String model, String uniqueModel, MetaJsonModel jsonModel);
}
