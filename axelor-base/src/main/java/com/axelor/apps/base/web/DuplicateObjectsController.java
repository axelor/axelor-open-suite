/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.DuplicateObjectsService;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DuplicateObjectsController {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private DuplicateObjectsService duplicateObjectService;

  public void removeDuplicate(ActionRequest request, ActionResponse response) {
    List<Long> selectedIds = new ArrayList<>();
    String originalId =
        ((Map) request.getContext().get("originalObject")).get("recordId").toString();
    selectedIds.add(Long.parseLong(originalId));
    List<Map<String, Object>> duplicateObjects =
        (List<Map<String, Object>>) request.getContext().get("duplicateObjects");
    for (Map map : duplicateObjects) {
      if (!map.get("recordId").toString().equals(originalId)) {
        selectedIds.add(Long.parseLong(map.get("recordId").toString()));
      }
    }
    String model = request.getContext().get("_modelName").toString();
    String modelName = model.substring(model.lastIndexOf(".") + 1, model.length());
    duplicateObjectService.removeDuplicate(selectedIds, modelName);
    response.setCanClose(true);
  }

  public void defaultObjects(ActionRequest request, ActionResponse response)
      throws NoSuchFieldException, SecurityException {
    List<Long> selectedIds = new ArrayList<>();
    List<Object[]> duplicateObjects = new ArrayList<>();
    List<Wizard> wizardDataList = new ArrayList<>();
    for (Integer id : (List<Integer>) request.getContext().get("_ids")) {
      selectedIds.add(Long.parseLong("" + id));
    }
    String modelName = request.getContext().get("_modelName").toString();
    List<Object> duplicateObj = duplicateObjectService.getAllSelectedObject(selectedIds, modelName);

    for (Object object : duplicateObj) {
      Long id = (Long) Mapper.of(object.getClass()).get(object, "id");
      Property propertyNameColumn = Mapper.of(object.getClass()).getNameField();
      String nameColumn =
          propertyNameColumn == null ? null : propertyNameColumn.getName().toString();
      Property propertyCode = Mapper.of(object.getClass()).getProperty("code");
      String code = propertyCode == null ? null : propertyCode.getName().toString();
      String noColumn = null;
      if (nameColumn != null) {
        duplicateObjects.add(
            (Object[]) duplicateObjectService.getWizardValue(id, modelName, nameColumn));
      } else if (code != null) {
        duplicateObjects.add((Object[]) duplicateObjectService.getWizardValue(id, modelName, code));
      } else {
        Object obj = duplicateObjectService.getWizardValue(id, modelName, noColumn);
        Wizard wizard = new Wizard();
        wizard.setRecordId(obj.toString());
        wizard.setName(obj.toString());
        wizardDataList.add(wizard);
      }
    }
    for (Object[] obj : duplicateObjects) {
      String recordName = obj[1].toString();
      String recordId = obj[0].toString();
      Wizard wizard = new Wizard();
      wizard.setRecordId(recordId);
      wizard.setRecordName(recordName);
      wizard.setName(recordName);
      wizardDataList.add(wizard);
    }

    response.setAttr("$duplicateObjects", "value", wizardDataList);
  }

  public void addOriginal(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    List<Map<String, Object>> duplicateObj =
        (List<Map<String, Object>>) context.get("duplicateObjects");
    Object originalObj = null;
    Object original = "";
    boolean flag = false;
    for (Map map : duplicateObj) {
      if ((boolean) map.get("selected")) {
        originalObj = context.get("originalObject");
        response.setAttr("$originalObject", "value", map);
        original = map;
        flag = true;
      }
    }
    if (!flag) {
      response.setAlert(I18n.get(IExceptionMessage.GENERAL_11));
    }
    duplicateObj.remove(original);
    if (originalObj != null) {
      duplicateObj.add((Map<String, Object>) originalObj);
    }

    response.setAttr("$duplicateObjects", "value", duplicateObj);
  }

  /**
   * Find duplicated records by using DuplicateObjectsService and open it.
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void showDuplicate(ActionRequest request, ActionResponse response) throws AxelorException {

    Context context = request.getContext();
    Set<String> fields = new HashSet<String>();
    Class<?> modelClass = extractModel(request, fields);

    LOG.debug("Duplicate record model: {}", modelClass.getName());

    if (fields.size() > 0) {

      String filter = findDuplicated(request, fields, modelClass);

      if (filter == null) {
        response.setFlash(I18n.get(IExceptionMessage.GENERAL_1));
      } else {
        response.setView(
            ActionView.define(I18n.get(IExceptionMessage.GENERAL_2))
                .model(modelClass.getName())
                .add("grid")
                .add("form")
                .domain(filter)
                .context("_domain", filter)
                .context("_duplicateFinderFields", context.get("_duplicateFinderFields"))
                .context("_ids", context.get("_ids"))
                .map());

        if (context.get("_contextModel") != null) {
          response.setCanClose(true);
        }
      }
    } else if (context.get("_contextModel") == null) {
      response.setFlash(I18n.get(IExceptionMessage.GENERAL_10));
    } else {
      response.setFlash(I18n.get(IExceptionMessage.GENERAL_3));
    }
  }

  private String findDuplicated(ActionRequest request, Set<String> fields, Class<?> modelClass)
      throws AxelorException {

    Context context = request.getContext();
    LOG.debug("Duplicate finder fields: {}", fields);

    List<Long> contextIds = (List<Long>) context.get("_ids");

    String filter = null;
    if (contextIds != null && !contextIds.isEmpty()) {
      filter = "self.id in (" + Joiner.on(",").join(contextIds) + ")";
    } else {
      filter = (String) context.get("_domain_");
    }

    List<?> ids = duplicateObjectService.findDuplicatedRecordIds(fields, modelClass, filter);

    if (ids.isEmpty()) {
      return null;
    }

    return "self.id in (" + Joiner.on(",").join(ids) + ")";
  }

  private Class<?> extractModel(ActionRequest request, Set<String> fields) {

    Context context = request.getContext();
    String model = (String) context.get("_contextModel");

    if (model == null) {
      model = request.getModel();
      String duplicateFinderFields = (String) context.get("_duplicateFinderFields");
      if (duplicateFinderFields != null) {
        fields.addAll(Arrays.asList(duplicateFinderFields.split(";")));
      }
    } else {

      if (context.get("fieldsSet") != null) {
        List<HashMap<String, Object>> fieldsSet =
            (List<HashMap<String, Object>>) context.get("fieldsSet");
        for (HashMap<String, Object> field : fieldsSet) {
          fields.add((String) field.get("name"));
        }
      }
    }

    return JPA.model(model);
  }
}
