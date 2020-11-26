/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.csv.script;

import com.axelor.apps.base.db.FileTab;
import com.axelor.apps.base.db.repo.FileTabRepository;
import com.axelor.apps.base.service.advanced.imports.ActionService;
import com.axelor.apps.base.service.advanced.imports.ValidatorService;
import com.axelor.common.Inflector;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.Context;
import com.axelor.rpc.JsonContext;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptBindings;
import com.axelor.script.ScriptHelper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class ImportAdvancedImport {

  @Inject protected MetaFiles metaFiles;

  @Inject private FileTabRepository fileTabRepo;

  @Inject private ValidatorService validatorService;

  @Inject protected ActionService actionService;

  @SuppressWarnings("unchecked")
  public Object importGeneral(Object bean, Map<String, Object> values)
      throws ClassNotFoundException {
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
      this.addJsonObjectRecord(bean, fileTab, fileTab.getMetaModel().getName(), values);

      int fieldSeq = 2;
      int btnSeq = 3;
      for (Property prop : propList) {
        validatorService.createCustomObjectSet(
            fileTab.getClass().getName(), prop.getTarget().getName(), fieldSeq);
        validatorService.createCustomButton(
            fileTab.getClass().getName(), prop.getTarget().getName(), btnSeq);

        this.addJsonObjectRecord(
            prop.get(bean),
            fileTab,
            StringUtils.substringAfterLast(prop.getTarget().getName(), "."),
            values);
        fieldSeq++;
        btnSeq++;
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

  @SuppressWarnings("unchecked")
  private void addJsonObjectRecord(
      Object bean, FileTab fileTab, String fieldName, Map<String, Object> values) {

    String field = Inflector.getInstance().camelize(fieldName, true) + "Set";
    List<Object> recordList;

    Map<String, Object> recordMap = new HashMap<String, Object>();
    recordMap.put("id", ((Model) bean).getId());

    Map<String, Object> jsonContextValues =
        (Map<String, Object>) values.get("jsonContextValues" + fileTab.getId());

    JsonContext jsonContext = (JsonContext) jsonContextValues.get("jsonContext");
    Context context = (Context) jsonContextValues.get("context");

    if (!jsonContext.containsKey(field)) {
      recordList = new ArrayList<Object>();
    } else {
      recordList =
          ((List<Object>) jsonContext.get(field))
              .stream()
                  .map(
                      obj -> {
                        if (Mapper.toMap(EntityHelper.getEntity(obj)).get("id") != null) {
                          Map<String, Object> idMap = new HashMap<String, Object>();
                          idMap.put("id", Mapper.toMap(EntityHelper.getEntity(obj)).get("id"));
                          return idMap;
                        }
                        return obj;
                      })
                  .collect(Collectors.toList());
    }
    recordList.add(recordMap);
    jsonContext.put(field, recordList);

    fileTab.setAttrs(context.get("attrs").toString());
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
