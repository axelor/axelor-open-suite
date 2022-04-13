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
package com.axelor.apps.bpm.service;

import com.axelor.apps.bpm.context.WkfContextHelper;
import com.axelor.apps.bpm.db.WkfProcessConfig;
import com.axelor.apps.bpm.db.repo.WkfModelRepository;
import com.axelor.apps.bpm.db.repo.WkfProcessConfigRepository;
import com.axelor.apps.tool.StringTool;
import com.axelor.apps.tool.context.FullContext;
import com.axelor.db.EntityHelper;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.script.SimpleBindings;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.variable.serializer.jpa.JPAVariableSerializer;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.Variables.SerializationDataFormats;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WkfCommonServiceImpl implements WkfCommonService {

  protected static final Logger log = LoggerFactory.getLogger(WkfCommonServiceImpl.class);

  @Inject protected WkfProcessConfigRepository wkfProcessConfigRepository;

  @Inject protected MetaJsonFieldRepository metaJsonFieldRepository;

  @Inject protected WkfModelRepository wkfModelRepository;

  @Override
  public WkfProcessConfig findCurrentProcessConfig(Model model) {

    return findProcessConfig(model, true, WkfModelRepository.STATUS_ON_GOING);
  }

  @Override
  public WkfProcessConfig findOldProcessConfig(Model model) {

    return findProcessConfig(model, false, WkfModelRepository.STATUS_TERMINATED);
  }

  protected WkfProcessConfig findProcessConfig(Model model, boolean isActive, int status) {

    List<WkfProcessConfig> configs =
        wkfProcessConfigRepository
            .all()
            .filter(
                "(self.metaModel.fullName = ?1 OR self.metaJsonModel.name = ?1) "
                    + "AND self.wkfProcess.wkfModel.statusSelect = ?2 "
                    + "AND self.wkfProcess.wkfModel.isActive is ?3 "
                    + "AND (self.isStartModel is true OR self.processPath is not null)",
                getModelName(model),
                status,
                isActive)
            .order("pathCondition")
            .fetch();

    Map<String, Object> ctxMap = new HashMap<String, Object>();
    ctxMap.put(getVarName(model), new FullContext(model));

    for (WkfProcessConfig config : configs) {
      boolean condition = true;
      if (config.getPathCondition() != null) {
        condition = (boolean) evalExpression(ctxMap, config.getPathCondition());
      }
      if (condition) {
        return config;
      }
    }

    return null;
  }

  @Override
  public Object evalExpression(Map<String, Object> varMap, String expr) {

    if (Strings.isNullOrEmpty(expr)) {
      return null;
    }

    if (expr.startsWith("${") && expr.endsWith("}")) {
      expr = expr.replaceFirst("\\$\\{", "");
      expr = expr.substring(0, expr.length() - 1);
    }

    GroovyScriptHelper helper;
    if (varMap instanceof Context) {
      helper = new GroovyScriptHelper((Context) varMap);
    } else {
      SimpleBindings simpleBindings = new SimpleBindings();
      simpleBindings.putAll(varMap);
      helper = new GroovyScriptHelper(simpleBindings);
    }
    helper.getBindings().put("$ctx", WkfContextHelper.class);
    Object result = null;
    try {
      result = helper.eval(expr);
    } catch (Exception e) {
    }
    log.debug("Eval expr: {}, result: {}", expr, result);
    return result;
  }

  @Override
  public Map<String, Object> createVariables(Map<String, Object> modelMap) {

    Map<String, Object> varMap = new HashMap<String, Object>();
    for (String name : modelMap.keySet()) {

      Object model = modelMap.get(name);

      if (model == null) {
        varMap.put(name, Variables.objectValue(null, true).create());
        continue;
      }

      ObjectValue var = null;
      Long id = null;
      if (model instanceof Model) {
        var =
            Variables.objectValue(model, true)
                .serializationDataFormat(JPAVariableSerializer.NAME)
                .create();
        id = ((Model) model).getId();
      } else {
        var =
            Variables.objectValue(model, true)
                .serializationDataFormat(SerializationDataFormats.JSON)
                .create();

        if (model instanceof FullContext) {
          id = (Long) ((FullContext) model).get("id");
        }
      }
      varMap.put(name, var);

      if (id != null) {
        varMap.put(name + "Id", Variables.longValue(id));
      }
    }

    log.debug("Process variables: {}", varMap);
    return varMap;
  }

  @Override
  public String getVarName(Object model) {

    String name = null;
    if (model instanceof Context) {
      name = (String) ((Context) model).get("jsonModel");
      if (name == null) {
        name = ((Context) model).getContextClass().getSimpleName();
      }
    } else if (model instanceof MetaJsonRecord) {
      name = ((MetaJsonRecord) model).getJsonModel();
    } else if (model instanceof String) {
      name = (String) model;
      if (name.contains(".")) {
        name = name.substring(name.lastIndexOf(".") + 1);
      }
    } else if (model instanceof Model) {
      name = model.getClass().getSimpleName();
    }

    if (name != null) {
      name = StringTool.toFirstLower(name);
    }

    return name;
  }

  private String getModelName(Model model) {

    if (model instanceof MetaJsonRecord) {
      return ((MetaJsonRecord) model).getJsonModel();
    }

    return model.getClass().getName();
  }

  @Override
  public Object findRelatedRecord(Model model, String path) throws AxelorException {

    Object object = null;
    FullContext wkfModel = new FullContext(model);

    if (path.startsWith("_find(")) {
      List<String> params = Arrays.asList(path.replace("_find(", "").replace(")", "").split(","));
      if (params.size() >= 2) {
        List<Object> queryParams =
            params.stream().map(it -> evalExpression(wkfModel, it)).collect(Collectors.toList());
        String queryModel = (String) queryParams.get(0);
        queryParams.remove(0);
        String query = (String) queryParams.get(0);
        queryParams.remove(0);
        log.debug("Find model: {}, query: {}, params: {}", queryModel, query, queryParams);
        object = WkfContextHelper.filterOne(queryModel, query, queryParams.toArray());
      }
    } else {
      object = evalExpression(new FullContext(model), path);
    }

    return object;
  }

  @Override
  public Model addProperties(
      Map<String, String> propertyMap, Model model, ModelElementInstance element) {

    Mapper mapper = Mapper.of(EntityHelper.getEntityClass(model));

    for (String property : propertyMap.keySet()) {
      Object value =
          element.getAttributeValueNs(
              BpmnParser.CAMUNDA_BPMN_EXTENSIONS_NS, propertyMap.get(property));
      if (value != null && value.equals("undefined")) {
        value = null;
      }
      Property field = mapper.getProperty(property);
      if (field.isReference()) {
        try {
          value =
              JpaRepository.of((Class<? extends Model>) field.getTarget())
                  .all()
                  .filter("self.name = ?1", value)
                  .fetchOne();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      mapper.set(model, property, value);
    }

    return model;
  }
}
