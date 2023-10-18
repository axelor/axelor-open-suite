/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.advanced.imports;

import com.axelor.apps.base.db.FileField;
import com.axelor.apps.base.db.FileTab;
import com.axelor.apps.base.db.repo.FileFieldRepository;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class FileTabServiceImpl implements FileTabService {

  @Inject MetaFieldRepository metaFieldRepo;

  @Inject FileFieldService fileFieldService;

  @Override
  public FileTab updateFields(FileTab fileTab) throws ClassNotFoundException {

    MetaModel model = fileTab.getMetaModel();

    if (model == null || CollectionUtils.isEmpty(fileTab.getFileFieldList())) {
      return fileTab;
    }

    Beans.get(ValidatorService.class).sortFileFieldList(fileTab.getFileFieldList());

    for (FileField fileField : fileTab.getFileFieldList()) {

      MetaField importField =
          metaFieldRepo
              .all()
              .filter(
                  "self.label = ?1 AND self.metaModel.id = ?2",
                  fileField.getColumnTitle(),
                  model.getId())
              .fetchOne();

      if (importField != null) {
        String relationship = importField.getRelationship();
        if (!Strings.isNullOrEmpty(relationship) && relationship.equals("OneToMany")) {
          continue;
        }

        fileField.setImportField(importField);
        if (!Strings.isNullOrEmpty(relationship)) {
          String subImportField = this.getSubImportField(importField);
          fileField.setSubImportField(subImportField);
        }
        fileField = fileFieldService.fillType(fileField);

        if (!Strings.isNullOrEmpty(relationship) && !fileField.getTargetType().equals("MetaFile")) {
          fileField.setImportType(FileFieldRepository.IMPORT_TYPE_FIND);
        } else {
          if (!Strings.isNullOrEmpty(fileField.getTargetType())
              && fileField.getTargetType().equals("MetaFile")) {
            fileField.setImportType(FileFieldRepository.IMPORT_TYPE_NEW);
          }
        }
        fileField.setFullName(fileFieldService.computeFullName(fileField));
      } else {
        fileField.setImportField(null);
        fileField.setSubImportField(null);
      }
    }
    return fileTab;
  }

  protected String getSubImportField(MetaField importField) throws ClassNotFoundException {
    String modelName = importField.getTypeName();
    MetaModel metaModel = Beans.get(MetaModelRepository.class).findByName(modelName);

    AdvancedImportService advancedImportService = Beans.get(AdvancedImportService.class);
    Mapper mapper = advancedImportService.getMapper(metaModel.getFullName());

    return (mapper != null && mapper.getNameField() != null)
        ? mapper.getNameField().getName()
        : null;
  }

  @Override
  public FileTab compute(FileTab fileTab) {
    if (CollectionUtils.isEmpty(fileTab.getFileFieldList())) {
      return fileTab;
    }

    for (FileField fileField : fileTab.getFileFieldList()) {
      fileField.setFullName(fileFieldService.computeFullName(fileField));
    }
    return fileTab;
  }

  @Override
  public Map<String, Object> getImportedRecordMap(String recordString) throws JSONException {
    if (Strings.isNullOrEmpty(recordString)) {
      return null;
    }
    Map<String, Object> dataMap = new HashMap<String, Object>();
    List<Object> recordList = new ArrayList<>();

    JSONObject jsonObject = new JSONObject(recordString);
    for (Object key : jsonObject.keySet()) {
      Map<String, Object> recordMap = new HashMap<>();
      recordMap.put("model", key);
      recordMap.put("ids", jsonObject.get(key));
      recordList.add(recordMap);
    }

    dataMap.put("$recordMap", recordList);
    return dataMap;
  }
}
