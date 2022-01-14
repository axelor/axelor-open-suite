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
package com.axelor.apps.bpm.service.execution;

import com.axelor.apps.bpm.db.WkfInstance;
import com.axelor.apps.bpm.db.WkfProcess;
import com.axelor.apps.bpm.db.WkfProcessConfig;
import com.axelor.apps.bpm.db.WkfTaskConfig;
import com.axelor.apps.bpm.db.repo.WkfInstanceRepository;
import com.axelor.apps.bpm.db.repo.WkfProcessRepository;
import com.axelor.apps.bpm.db.repo.WkfTaskConfigRepository;
import com.axelor.apps.bpm.service.WkfCommonService;
import com.axelor.apps.bpm.translation.ITranslation;
import com.axelor.apps.tool.context.FullContext;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.EntityHelper;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaJsonRecord;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WkfTaskServiceImpl implements WkfTaskService {

  protected static final Logger log = LoggerFactory.getLogger(WkfTaskServiceImpl.class);

  protected static final int RECURSIVE_TASK_EXECUTION_COUNT_LIMIT = 100;

  protected static final int RECURSIVE_TASK_EXECUTION_SECONDS_LIMIT = 10;

  @Inject protected WkfTaskConfigRepository wkfTaskConfigRepository;

  @Inject protected WkfInstanceService wkfInstanceService;

  @Inject protected WkfInstanceRepository wkfInstanceRepository;

  @Inject protected WkfProcessRepository wkfProcessRepository;

  @Inject protected WkfCommonService wkfService;

  protected int recursiveTaskExecutionCount = 0;

  protected LocalTime recursiveTaskExecutionTime = LocalTime.now();

  @Override
  public String runTasks(
      ProcessEngine engine, WkfInstance instance, ProcessInstance processInstance, String signal)
      throws ClassNotFoundException, AxelorException {

    WkfProcess wkfProcess = instance.getWkfProcess();

    List<Task> tasks = getActiveTasks(engine, processInstance.getId());

    boolean taskExecuted = false;
    String helpText = null;

    Map<String, Object> context = getContext(instance);
    // TODO: Check if its required both variables from context and from processInstance, if
    Map<String, Object> processVariables =
        engine.getRuntimeService().getVariables(processInstance.getId());
    processVariables.entrySet().removeIf(it -> Strings.isNullOrEmpty(it.getKey()));

    Map<String, Object> expressionVariables = null;
    Map<String, Object> ctxVariables = wkfService.createVariables(context);

    for (Task task : tasks) {

      WkfTaskConfig config =
          wkfTaskConfigRepository
              .all()
              .filter(
                  "self.name = ? and self.wkfModel.id = ?",
                  task.getTaskDefinitionKey(),
                  wkfProcess.getWkfModel().getId())
              .fetchOne();

      if (config == null) {
        continue;
      }

      List<String> validButtons = getValidButtons(signal, config.getButton());

      if (validButtons == null) {
        continue;
      }

      if (expressionVariables == null) {
        expressionVariables = new HashMap<String, Object>();
        expressionVariables.putAll(processVariables);
        expressionVariables.putAll(context);
      }

      if (!validButtons.isEmpty() || config.getExpression() != null) {

        Map<String, Object> btnVariables = new HashMap<String, Object>();
        for (String button : validButtons) {
          btnVariables.put(button, button.equals(signal));
        }

        Map<String, Object> variables = wkfService.createVariables(btnVariables);
        variables.putAll(ctxVariables);

        if (config.getExpression() != null) {
          expressionVariables.putAll(engine.getTaskService().getVariables(task.getId()));
          expressionVariables.entrySet().removeIf(it -> Strings.isNullOrEmpty(it.getKey()));
          Boolean validExpr =
              (Boolean) wkfService.evalExpression(expressionVariables, config.getExpression());
          if (validExpr == null || !validExpr) {
            log.debug("Not a valid expr: {}", config.getExpression());
            if (!validButtons.isEmpty()) {
              helpText = config.getHelpText();
            }
            continue;
          }

          log.debug("Valid expr: {}", config.getExpression());
        }

        User user = AuthUtils.getUser();
        if (user != null) {
          engine.getTaskService().setAssignee(task.getId(), user.getId().toString());
        } else {
          engine.getTaskService().setAssignee(task.getId(), "0");
        }

        engine.getTaskService().complete(task.getId(), variables);
        taskExecuted = true;
      }
    }

    Execution execution =
        engine
            .getRuntimeService()
            .createExecutionQuery()
            .active()
            .executionId(processInstance.getId())
            .singleResult();
    if (execution != null) {
      engine.getRuntimeService().setVariables(execution.getId(), ctxVariables);
    }

    recursiveTaskExecutionCount++;

    if (recursiveTaskExecutionCount >= RECURSIVE_TASK_EXECUTION_COUNT_LIMIT
        && ChronoUnit.SECONDS.between(recursiveTaskExecutionTime, LocalTime.now())
            <= RECURSIVE_TASK_EXECUTION_SECONDS_LIMIT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get(ITranslation.INFINITE_EXECUTION));
    }
    if (taskExecuted
        && wkfInstanceService.isActiveProcessInstance(
            processInstance.getId(), engine.getRuntimeService())) {
      log.debug("Check tasks again");
      runTasks(engine, instance, processInstance, signal);
    }

    return helpText;
  }

  protected List<String> getValidButtons(String signal, String button) {

    if (button != null) {
      List<String> buttons = Arrays.asList(button.split(","));
      if (buttons.contains(signal)) {
        return buttons;
      }
      return null;
    }

    return new ArrayList<String>();
  }

  protected List<Task> getActiveTasks(ProcessEngine engine, String processInstanceId) {

    List<Task> tasks =
        engine
            .getTaskService()
            .createTaskQuery()
            .active()
            .processInstanceId(processInstanceId)
            .list();

    return tasks;
  }

  protected Map<String, Object> getContext(WkfInstance instance) throws ClassNotFoundException {

    WkfProcess wkfProcess = instance.getWkfProcess();

    Map<String, Object> modelMap = new HashMap<>();

    for (WkfProcessConfig processConfig : wkfProcess.getWkfProcessConfigList()) {

      Model model = null;
      String klassName;
      if (processConfig.getMetaJsonModel() != null) {
        klassName = MetaJsonRecord.class.getName();
      } else {
        klassName = processConfig.getMetaModel().getFullName();
      }
      @SuppressWarnings("unchecked")
      final Class<? extends Model> klass = (Class<? extends Model>) Class.forName(klassName);
      String query = "self.processInstanceId = ?";
      if (processConfig.getMetaJsonModel() != null) {
        query += " AND self.jsonModel = '" + processConfig.getMetaJsonModel().getName() + "'";
      }

      if (model == null)
        model =
            JpaRepository.of(klass)
                .all()
                .filter(query, instance.getInstanceId())
                .order("-id")
                .fetchOne();
      if (model != null) {
        model = EntityHelper.getEntity(model);
        String name = wkfService.getVarName(model);
        modelMap.put(name, new FullContext(model));
      } else {
        log.debug("Model not found with processInstanceId: {}", instance.getInstanceId());
      }
    }

    log.debug("Variable map used: {}", modelMap);

    return modelMap;
  }
}
