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
package com.axelor.csv.script;

import com.axelor.apps.base.db.FileTab;
import com.axelor.apps.base.db.repo.FileTabRepository;
import com.axelor.apps.base.service.advanced.imports.ActionService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptBindings;
import com.axelor.script.ScriptHelper;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class ImportAdvancedImport {

  @Inject protected MetaFiles metaFiles;

  @Inject private FileTabRepository fileTabRepo;

  @Inject protected ActionService actionService;

  @SuppressWarnings("unchecked")
  public Object importGeneral(Object bean, Map<String, Object> values)
      throws ClassNotFoundException, JSONException {
    if (bean == null) {
      return bean;
    }

    FileTab fileTab = fileTabRepo.find(Long.valueOf(values.get("fileTabId").toString()));

    ScriptHelper scriptHelper = new GroovyScriptHelper(new ScriptBindings(values));

    List<String> exprs = (List<String>) values.get("ifConditions" + fileTab.getId());
    if (!CollectionUtils.isEmpty(exprs)) {
      if ((boolean) scriptHelper.eval(String.join(" || ", exprs))) {
        return null;
      }
    }

    if (((Model) bean).getId() == null) {
      List<Property> propList = this.getProperties(bean);
      JPA.save((Model) bean);
      this.addImportedRecordIds(bean, fileTab, fileTab.getMetaModel().getName(), values);

      for (Property prop : propList) {

        this.addImportedRecordIds(
            prop.get(bean),
            fileTab,
            StringUtils.substringAfterLast(prop.getTarget().getName(), "."),
            values);
      }
    }

    final String ACTIONS_TO_APPLY = "actionsToApply" + fileTab.getId();
    if (!ObjectUtils.isEmpty(values.get(ACTIONS_TO_APPLY))) {
      bean = actionService.apply(values.get(ACTIONS_TO_APPLY).toString(), bean);
    }
    return bean;
  }

  private List<Property> getProperties(Object bean) {

    List<Property> propList = new ArrayList<Property>();

    for (Property prop : Mapper.of(bean.getClass()).getProperties()) {
      if (prop.getTarget() != null
          && !prop.isCollection()
          && ((Model) prop.get(bean) != null)
          && ((Model) prop.get(bean)).getId() == null) {
        propList.add(prop);
      }
    }
    return propList;
  }

  private void addImportedRecordIds(
      Object bean, FileTab fileTab, String modelName, Map<String, Object> values)
      throws JSONException {
    List<String> recordList = new ArrayList<>();
    JSONObject jsonObject = new JSONObject();
    String recordId = ((Model) bean).getId().toString();
    if (!Strings.isNullOrEmpty(fileTab.getImportedRecordIds())) {
      jsonObject = new JSONObject(fileTab.getImportedRecordIds());
    }

    if (!jsonObject.isEmpty()) {
      if (jsonObject.containsKey(modelName)) {
        String ids = (String) jsonObject.get(modelName);
        recordList = new ArrayList<>(Arrays.asList(ids.split("\\,")));
      }
    }

    recordList.add(recordId);
    String recordIds = Joiner.on(",").join(recordList);
    jsonObject.put(modelName, recordIds);
    fileTab.setImportedRecordIds(jsonObject.toString());
  }

  public Object importPicture(String value, String pathVal) throws IOException {
    if (Strings.isNullOrEmpty(value)) {
      return null;
    }

    Path path = Paths.get(pathVal);
    if (Strings.isNullOrEmpty(value)) {
      return null;
    }

    File image = path.resolve(value).toFile();
    if (!image.exists() || image.isDirectory()) {
      return null;
    }

    MetaFile metaFile = metaFiles.upload(image);
    return metaFile;
  }
}
