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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.DuplicateObjectsService;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
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
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DuplicateObjectsController {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private DuplicateObjectsService duplicateObjectService;

  public void removeDuplicate(ActionRequest request, ActionResponse response)
      throws ClassNotFoundException {

    Long originalId =
        Long.parseLong(
            ((Map) request.getContext().get("originalObject")).get("recordId").toString());

    List<Map<String, Object>> duplicateObjects =
        (List<Map<String, Object>>) request.getContext().get("duplicateObjects");

    List<Long> duplicateIds = new ArrayList<>();

    for (Map map : duplicateObjects) {
      if (!map.get("recordId").toString().equals(originalId.toString())) {
        duplicateIds.add(Long.parseLong(map.get("recordId").toString()));
      }
    }

    String model = request.getContext().get("_modelName").toString();
    duplicateObjectService.removeDuplicate(originalId, duplicateIds, Class.forName(model));
    response.setCanClose(true);
  }

  public void defaultObjects(ActionRequest request, ActionResponse response)
      throws NoSuchFieldException, SecurityException, ClassNotFoundException {

    List<Long> selectedObjectIds = new ArrayList<>();
    for (Integer id : (List<Integer>) request.getContext().get("_ids")) {
      selectedObjectIds.add(new Long(id));
    }

    Class<?> klass = Class.forName(request.getContext().get("_modelName").toString());
    String nameColumn = Mapper.of(klass).getNameField().getName();

    response.setAttr(
        "$duplicateObjects", "value", this.getWizards(selectedObjectIds, klass, nameColumn));
  }

  public void addOriginal(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    List<Map<String, Object>> duplicateObjects =
        (List<Map<String, Object>>) context.get("duplicateObjects");
    Object currentOriginalObject = null;
    for (Map map : duplicateObjects) {
      if ((boolean) map.get("selected")) {
        response.setAttr("$originalObject", "value", map);
        currentOriginalObject = map;
        break;
      }
    }

    if (currentOriginalObject == null) {
      response.setAlert(I18n.get(IExceptionMessage.GENERAL_11));
    }
    duplicateObjects.remove(currentOriginalObject);

    Object previousOriginalObject = context.get("originalObject");
    if (previousOriginalObject != null) {
      duplicateObjects.add((Map<String, Object>) previousOriginalObject);
    }

    response.setAttr("$duplicateObjects", "value", duplicateObjects);
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

      String filter = findDuplicated(context, fields, modelClass);

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

  private String findDuplicated(Context context, Set<String> fields, Class<?> modelClass)
      throws AxelorException {

    LOG.debug("Duplicate finder fields: {}", fields);

    List<Long> contextIds = (List<Long>) context.get("_ids");

    String filter =
        (contextIds != null && !contextIds.isEmpty())
            ? "self.id in (" + Joiner.on(",").join(contextIds) + ")"
            : (String) context.get("_domain_");

    List<?> duplicateIds =
        duplicateObjectService.findDuplicatedRecordIds(fields, modelClass, filter);

    if (duplicateIds.isEmpty()) {
      return null;
    }

    return "self.id in (" + Joiner.on(",").join(duplicateIds) + ")";
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

        fieldsSet.forEach(it -> fields.add((String) it.get("name")));
      }
    }

    return JPA.model(model);
  }

  private List<Wizard> getWizards(
      List<Long> selectedObjectIds, Class<?> modelName, String nameColumn) {
    StringBuilder queryString = new StringBuilder("SELECT NEW Map(self.id as id");
    if (nameColumn != null) {
      queryString.append(" ,self." + nameColumn + " as " + nameColumn);
    }
    queryString.append(") FROM ");
    queryString.append(modelName.getSimpleName());
    queryString.append(" self WHERE self.id IN (:ids)");

    Query query = JPA.em().createQuery(queryString.toString(), Map.class);
    query.setParameter("ids", selectedObjectIds);

    List<Wizard> wizardList = new ArrayList<>();

    for (Object obj : query.getResultList()) {
      Map<String, Object> map = (Map<String, Object>) obj;
      Wizard wizard = new Wizard();
      wizard.setRecordId(map.get("id").toString());
      if (map.containsKey(nameColumn)) {
        wizard.setName(map.get(nameColumn).toString());
        wizard.setRecordName(map.get(nameColumn).toString());
      } else {
        wizard.setName(map.get("id").toString());
      }
      wizardList.add(wizard);
    }

    return wizardList;
  }
}
