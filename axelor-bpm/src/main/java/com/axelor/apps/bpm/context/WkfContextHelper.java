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
package com.axelor.apps.bpm.context;

import com.axelor.apps.bpm.service.WkfCommonService;
import com.axelor.apps.bpm.service.init.ProcessEngineService;
import com.axelor.apps.tool.context.FullContext;
import com.axelor.apps.tool.context.FullContextHelper;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats;
import org.camunda.bpm.engine.variable.value.ObjectValue;

public class WkfContextHelper {

  @SuppressWarnings("unchecked")
  public static FullContext create(String modelName, Map<String, Object> values) {
    return FullContextHelper.create(modelName, values);
  }

  public static FullContext create(Model model) {
    return FullContextHelper.create(model);
  }

  public static FullContext filterOne(String modelName, String queryStr, Object... params)
      throws AxelorException {

    return FullContextHelper.filterOne(modelName, queryStr, params);
  }

  public static FullContext filterOne(String modelName, String queryStr) throws AxelorException {

    return FullContextHelper.filterOne(modelName, queryStr);
  }

  public static FullContext filterOne(
      String modelName, String queryStr, Map<String, Object> paramMap) throws AxelorException {

    return FullContextHelper.filterOne(modelName, queryStr, paramMap);
  }

  public static List<FullContext> filter(String modelName, String queryStr) {

    return FullContextHelper.filter(modelName, queryStr);
  }

  public static List<FullContext> filter(String modelName, String queryStr, Object... params) {

    return FullContextHelper.filter(modelName, queryStr, params);
  }

  public static List<FullContext> filter(
      String modelName, String queryStr, Map<String, Object> paramMap) {

    return FullContextHelper.filter(modelName, queryStr, paramMap);
  }

  public static FullContext create(String modelName) {
    return FullContextHelper.create(modelName);
  }

  public static void createVariable(FullContext wkfContext, DelegateExecution execution) {

    if (wkfContext.get("processInstanceId") == null) {
      wkfContext.put("processInstanceId", execution.getProcessInstanceId());
    }

    String varName = Beans.get(WkfCommonService.class).getVarName(wkfContext);
    execution.setVariable(
        varName,
        Variables.objectValue(wkfContext, true)
            .serializationDataFormat(SerializationDataFormats.JSON)
            .create());
    execution.setVariable(varName + "Id", wkfContext.get("id"));
  }

  public static ObjectValue createVariable(Object variable) {

    return Variables.objectValue(variable, true)
        .serializationDataFormat(SerializationDataFormats.JSON)
        .create();
  }

  @Transactional
  public static FullContext save(Object object) {

    return FullContextHelper.save(object);
  }

  public static JpaRepository<? extends Model> getRepository(String modelName) {

    return FullContextHelper.getRepository(modelName);
  }

  public static Object getVariable(String processInstanceId, String variable) {

    if (processInstanceId == null) {
      return null;
    }

    RuntimeService runtimeService =
        Beans.get(ProcessEngineService.class).getEngine().getRuntimeService();

    Object value = runtimeService.getVariable(processInstanceId, variable);

    return value;
  }

  public static FullContext find(String modelName, Object recordId) throws AxelorException {

    return FullContextHelper.find(modelName, recordId);
  }

  public static void evalStartConditions(String variableName, Object value) throws AxelorException {
    RuntimeService runtimeService =
        Beans.get(ProcessEngineService.class).getEngine().getRuntimeService();

    runtimeService
        .createConditionEvaluation()
        .setVariable(variableName, value)
        .evaluateStartConditions();
  }

  public static List<Long> getIdList(List<FullContext> lines) {

    List<Long> values = new ArrayList<Long>();

    for (FullContext fullContext : lines) {
      values.add((Long) fullContext.get("id"));
    }

    return values;
  }
}
