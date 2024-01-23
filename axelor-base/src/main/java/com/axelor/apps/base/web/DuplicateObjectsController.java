/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.DuplicateObjectsService;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.db.Wizard;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DuplicateObjectsController {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
    Beans.get(DuplicateObjectsService.class).removeDuplicate(selectedIds, modelName);
    response.setCanClose(true);
  }

  public void defaultObjects(ActionRequest request, ActionResponse response)
      throws SecurityException {
    List<Long> selectedIds = new ArrayList<>();
    List<Object[]> duplicateObjects = new ArrayList<>();
    List<Wizard> wizardDataList = new ArrayList<>();
    DuplicateObjectsService duplicateObjectsService = Beans.get(DuplicateObjectsService.class);
    for (Integer id : (List<Integer>) request.getContext().get("_ids")) {
      selectedIds.add(Long.parseLong("" + id));
    }
    String modelName = request.getContext().get("_modelName").toString();
    List<Object> duplicateObj =
        duplicateObjectsService.getAllSelectedObject(selectedIds, modelName);

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
            (Object[]) duplicateObjectsService.getWizardValue(id, modelName, nameColumn));
      } else if (code != null) {
        duplicateObjects.add(
            (Object[]) duplicateObjectsService.getWizardValue(id, modelName, code));
      } else {
        Object obj = duplicateObjectsService.getWizardValue(id, modelName, noColumn);
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
      response.setAlert(I18n.get(BaseExceptionMessage.GENERAL_11));
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
    Class<? extends Model> modelClass = extractModel(request, fields);

    LOG.debug("Duplicate record model: {}", modelClass.getName());

    if (fields.size() > 0) {

      String filter = findDuplicated(request, fields, modelClass);

      if (filter == null) {
        response.setInfo(I18n.get(BaseExceptionMessage.GENERAL_1));
      } else {
        String modelNameKebabCase =
            CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, modelClass.getSimpleName());
        response.setView(
            ActionView.define(I18n.get(BaseExceptionMessage.GENERAL_2))
                .model(modelClass.getName())
                .add("grid", modelNameKebabCase + "-grid")
                .add("form", modelNameKebabCase + "-form")
                .domain(filter)
                .context("_domain", filter)
                .map());

        if (context.get("_contextModel") != null) {
          response.setCanClose(true);
        }
      }
    } else if (context.get("_contextModel") == null) {
      response.setInfo(I18n.get(BaseExceptionMessage.GENERAL_10));
    } else {
      response.setInfo(I18n.get(BaseExceptionMessage.GENERAL_3));
    }
  }

  protected String findDuplicated(
      ActionRequest request, Set<String> fields, Class<? extends Model> modelClass)
      throws AxelorException {

    LOG.debug("Duplicate finder fields: {}", fields);

    List<?> ids =
        Beans.get(DuplicateObjectsService.class)
            .findDuplicatedRecordIds(fields, modelClass, getCriteria(request, modelClass));

    if (ids.isEmpty()) {
      return null;
    }

    return "self.id in (" + Joiner.on(",").join(ids) + ")";
  }

  private Class<? extends Model> extractModel(ActionRequest request, Set<String> fields) {

    Context context = request.getContext();
    String model = (String) context.get("_contextModel");
    MetaFieldRepository metaFieldRepository = Beans.get(MetaFieldRepository.class);

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
          MetaField metaField = metaFieldRepository.find(((Integer) field.get("id")).longValue());
          if (metaField != null) {
            fields.add(metaField.getName());
          }
        }
      }
    }

    return (Class<? extends Model>) JPA.model(model);
  }
  /**
   * call check duplicate wizard
   *
   * @param request
   * @param response
   */
  @SuppressWarnings("unchecked")
  public void callCheckDuplicateWizard(ActionRequest request, ActionResponse response) {

    LOG.debug("Call check duplicate wizard for model : {} ", request.getModel());

    String criteria = getCriteria(request, (Class<? extends Model>) JPA.model(request.getModel()));

    response.setView(
        ActionView.define(I18n.get("Check duplicate"))
            .model(Wizard.class.getName())
            .add("form", "wizard-check-duplicate-form")
            .param("popup", "true")
            .param("show-toolbar", "false")
            .param("width", "500")
            .param("popup-save", "false")
            .context("_contextModel", request.getModel())
            .context("_criteria", criteria)
            .map());
  }

  protected String getCriteria(ActionRequest request, Class<? extends Model> modelClass) {

    String criteria = (String) (request.getContext().get("_criteria"));
    if (criteria != null) {
      return criteria;
    }

    List<?> contextIds = (List<?>) request.getContext().get("_ids");
    if (contextIds != null && !contextIds.isEmpty()) {
      return "self.id in (" + Joiner.on(",").join(contextIds) + ")";
    } else {
      List<Map> listObj = request.getCriteria().createQuery(modelClass).select("id").fetch(0, 0);
      if (listObj != null) {
        List<?> ids = listObj.stream().map(it -> it.get("id")).collect(Collectors.toList());
        LOG.debug("Total criteria ids: {}", ids.size());
        return "self.id in (" + Joiner.on(",").join(ids) + ")";
      }
    }

    return null;
  }
}
