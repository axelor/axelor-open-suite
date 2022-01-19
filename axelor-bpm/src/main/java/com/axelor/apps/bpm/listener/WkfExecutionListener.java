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
package com.axelor.apps.bpm.listener;

import com.axelor.apps.bpm.db.WkfInstance;
import com.axelor.apps.bpm.db.WkfProcess;
import com.axelor.apps.bpm.db.WkfTaskConfig;
import com.axelor.apps.bpm.db.repo.WkfInstanceRepository;
import com.axelor.apps.bpm.db.repo.WkfProcessRepository;
import com.axelor.apps.bpm.db.repo.WkfTaskConfigRepository;
import com.axelor.apps.bpm.service.execution.WkfInstanceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.util.Collection;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants;
import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstance;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WkfExecutionListener implements ExecutionListener {

  protected static final Logger log = LoggerFactory.getLogger(WkfExecutionListener.class);

  @Override
  public void notify(DelegateExecution execution) throws Exception {

    String eventName = execution.getEventName();

    if (eventName.equals(EVENTNAME_START)) {

      if (execution.getProcessInstance().getActivityInstanceId() == null) {
        createWkfInstance(execution);
      } else {
        processNodeStart(execution);
      }

    } else if (eventName.equals(EVENTNAME_END)) {

      BpmnModelElementInstance modelElementInstance = execution.getBpmnModelElementInstance();
      String typeName = modelElementInstance.getElementType().getTypeName();
      if (modelElementInstance != null
          && typeName.equals(BpmnModelConstants.BPMN_ELEMENT_BUSINESS_RULE_TASK)) {
        checkDMNValue(execution);
      }
    }
  }

  private void checkDMNValue(DelegateExecution execution) throws AxelorException {

    String compulsory =
        execution
            .getBpmnModelElementInstance()
            .getAttributeValueNs(BpmnModelConstants.CAMUNDA_NS, "compulsory");

    if (compulsory != null && compulsory.equals("true")) {
      String varName =
          execution
              .getBpmnModelElementInstance()
              .getAttributeValueNs(
                  BpmnModelConstants.CAMUNDA_NS,
                  BpmnModelConstants.CAMUNDA_ATTRIBUTE_RESULT_VARIABLE);
      if (execution.getVariable(varName) == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get("No result from DMN : %s"),
            execution.getCurrentActivityName());
      }
    }
  }

  private void createWkfInstance(DelegateExecution execution) {

    String instanceId = execution.getProcessInstanceId();
    WkfInstanceRepository instanceRepo = Beans.get(WkfInstanceRepository.class);
    WkfInstance wkfInstance = instanceRepo.findByInstnaceId(instanceId);
    log.debug("Process called with related wkfInstance: {}", wkfInstance);
    if (wkfInstance == null) {
      execution.setVariable(
          getProcessKey(execution, execution.getProcessDefinitionId()),
          execution.getProcessInstanceId());
      createWkfInstance(execution, instanceId, instanceRepo);
    }
  }

  private void processNodeStart(DelegateExecution execution) {

    FlowElement flowElement = execution.getBpmnModelElementInstance();
    if (flowElement == null) {
      return;
    }
    String type = flowElement.getElementType().getTypeName();
    switch (type) {
      case (BpmnModelConstants.BPMN_ELEMENT_END_EVENT):
        sendMessage(flowElement, execution);
        onNodeActivation(execution);
        break;
      case (BpmnModelConstants.BPMN_ELEMENT_INTERMEDIATE_THROW_EVENT):
        sendMessage(flowElement, execution);
        break;
      default:
        if (blockingNode(type)) {
          onNodeActivation(execution);
        }
        break;
    }
  }

  private void sendMessage(FlowElement flowElement, DelegateExecution execution) {

    Collection<MessageEventDefinition> messageDefinitions =
        flowElement.getChildElementsByType(MessageEventDefinition.class);

    if (messageDefinitions.isEmpty()) {
      return;
    }

    MessageEventDefinition messageDefinition = messageDefinitions.iterator().next();

    String message = messageDefinition.getMessage().getName();

    if (message.contains("${")) {
      String[] msg = message.split("\\$\\{");
      String expr = msg[1].replace("}", "");
      message = msg[0] + execution.getVariable(expr);
    }
    log.debug("Sending message: {}", message);

    MessageCorrelationBuilder msgBuilder =
        execution.getProcessEngineServices().getRuntimeService().createMessageCorrelation(message);

    String processKey = getProcessKey(execution, execution.getProcessDefinitionId());
    log.debug("Process key: {}", processKey);

    msgBuilder.setVariable(processKey, execution.getProcessInstanceId());

    Collection<MessageCorrelationResult> results = msgBuilder.correlateAllWithResult();
    log.debug("Message result : {}", results.size());

    for (MessageCorrelationResult result : results) {
      ProcessInstance resultInstance = result.getProcessInstance();
      log.debug("Resulted process instance: {}", resultInstance);
      if (resultInstance != null) {
        execution.setVariable(
            getProcessKey(execution, resultInstance.getProcessDefinitionId()),
            resultInstance.getId());
      }
    }
  }

  private String getProcessKey(DelegateExecution execution, String processDefinitionId) {

    return execution
        .getProcessEngineServices()
        .getRepositoryService()
        .getProcessDefinition(processDefinitionId)
        .getKey();
  }

  @Transactional
  public void createWkfInstance(
      DelegateExecution execution, String instanceId, WkfInstanceRepository instanceRepo) {

    WkfInstance wkfInstance;
    wkfInstance = new WkfInstance();
    wkfInstance.setInstanceId(instanceId);
    WkfProcess wkfProcess =
        Beans.get(WkfProcessRepository.class)
            .all()
            .filter("self.processId = ?1", execution.getProcessDefinitionId())
            .fetchOne();
    wkfInstance.setName(wkfProcess.getProcessId() + " : " + instanceId);
    wkfInstance.setWkfProcess(wkfProcess);
    instanceRepo.save(wkfInstance);
  }

  private void onNodeActivation(DelegateExecution execution) {

    WkfTaskConfig wkfTaskConfig =
        Beans.get(WkfTaskConfigRepository.class)
            .all()
            .filter(
                "self.name = ? and self.wkfModel.id = (select wkfModel.id from WkfProcess where processId = ?)",
                execution.getCurrentActivityId(),
                execution.getProcessDefinitionId())
            .fetchOne();
    log.debug(
        "Task config searched with taskId: {}, processInstanceId: {}, found:{}",
        execution.getCurrentActivityId(),
        execution.getProcessInstanceId(),
        wkfTaskConfig);
    if (wkfTaskConfig != null) {
      Beans.get(WkfInstanceService.class).onNodeActivation(wkfTaskConfig, execution);
    }
  }

  private boolean blockingNode(String type) {

    boolean blockinNode = false;
    switch (type) {
      case (BpmnModelConstants.BPMN_ELEMENT_USER_TASK):
      case (BpmnModelConstants.BPMN_ELEMENT_CATCH_EVENT):
      case (BpmnModelConstants.BPMN_ELEMENT_CALL_ACTIVITY):
        blockinNode = true;
        break;
    }

    return blockinNode;
  }
}
