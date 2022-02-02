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
package com.axelor.apps.bpm.service.deployment;

import com.axelor.apps.bpm.db.WkfModel;
import com.axelor.apps.bpm.db.WkfProcess;
import com.axelor.apps.bpm.db.WkfProcessConfig;
import com.axelor.apps.bpm.db.repo.WkfModelRepository;
import com.axelor.apps.bpm.db.repo.WkfProcessRepository;
import com.axelor.apps.bpm.service.WkfCommonService;
import com.axelor.apps.bpm.service.init.ProcessEngineService;
import com.axelor.apps.bpm.service.init.WkfProcessApplication;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaAttrs;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanBuilder;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.BpmnModelConstants;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.MultiInstanceLoopCharacteristics;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.xml.ModelInstance;
import org.camunda.bpm.model.xml.impl.ModelBuilderImpl;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BpmDeploymentServiceImpl implements BpmDeploymentService {

  protected Logger log = LoggerFactory.getLogger(BpmDeploymentServiceImpl.class);

  @Inject protected WkfProcessRepository wkfProcessRepository;

  @Inject protected MetaJsonModelRepository metaJsonModelRepository;

  @Inject protected MetaAttrsService metaAttrsService;

  @Inject protected WkfCommonService wkfService;

  protected WkfModel wkfModel;

  protected Map<String, Map<String, String>> migrationMap;

  @Override
  @Transactional
  public void deploy(WkfModel wkfModel, Map<String, Map<String, String>> migrationMap) {

    if (wkfModel.getDiagramXml() == null) {
      return;
    }

    this.wkfModel = wkfModel;
    this.migrationMap = migrationMap;

    ProcessEngine engine = Beans.get(ProcessEngineService.class).getEngine();

    String key = wkfModel.getId() + ".bpmn";
    BpmnModelInstance bpmInstance =
        Bpmn.readModelFromStream(new ByteArrayInputStream(wkfModel.getDiagramXml().getBytes()));

    DeploymentBuilder deploymentBuilder =
        engine
            .getRepositoryService()
            .createDeployment()
            .addModelInstance(key, bpmInstance)
            .source(key);

    Set<MetaFile> dmnFiles = wkfModel.getDmnFileSet();
    if (dmnFiles != null) {
      addDmn(deploymentBuilder, dmnFiles);
    }

    Map<String, String> processMap = deployProcess(engine, deploymentBuilder, bpmInstance);

    List<MetaAttrs> metaAttrsList =
        Beans.get(WkfNodeService.class).extractNodes(wkfModel, bpmInstance, processMap);

    Beans.get(WkfModelRepository.class).save(wkfModel);

    metaAttrsService.saveMetaAttrs(metaAttrsList, wkfModel.getId());
  }

  protected Map<String, String> deployProcess(
      ProcessEngine engine, DeploymentBuilder deploymentBuilder, BpmnModelInstance bpmInstance) {

    Deployment deployment = deploymentBuilder.deploy();

    Map<String, String> processMap = new HashMap<String, String>();

    List<ProcessDefinition> definitions =
        engine
            .getRepositoryService()
            .createProcessDefinitionQuery()
            .deploymentId(deployment.getId())
            .list();

    if (wkfModel.getDeploymentId() != null && migrationMap != null) {
      migrateRunningInstances(wkfModel.getDeploymentId(), engine, definitions);
    }

    if (definitions.size() == 1) {
      wkfModel.setVersionTag(definitions.get(0).getVersionTag());
    }

    wkfModel.setDeploymentId(deployment.getId());

    log.debug("Definitions deployed: {}", definitions.size());
    for (ProcessDefinition definition : definitions) {

      WkfProcess process =
          wkfProcessRepository
              .all()
              .filter(
                  "self.name = ? and self.wkfModel.id = ?", definition.getKey(), wkfModel.getId())
              .fetchOne();

      if (process == null) {
        process = new WkfProcess();
        wkfModel.addWkfProcessListItem(process);
      }

      process.setName(definition.getKey());
      process.setProcessId(definition.getId());
      process.setDescription(definition.getName());

      addProcessConfig(bpmInstance, process);
      addDisplayProperties(bpmInstance, process);

      processMap.put(definition.getKey(), definition.getId());
    }

    engine
        .getManagementService()
        .registerProcessApplication(
            deployment.getId(), Beans.get(WkfProcessApplication.class).getReference());

    return processMap;
  }

  protected void migrateRunningInstances(
      String oldDeploymentId, ProcessEngine engine, List<ProcessDefinition> definitions) {

    List<ProcessDefinition> oldDefinitions =
        engine
            .getRepositoryService()
            .createProcessDefinitionQuery()
            .deploymentId(oldDeploymentId)
            .list();

    log.debug("Old definition size " + oldDefinitions.size());
    for (ProcessDefinition oldDefinition : oldDefinitions) {
      for (ProcessDefinition newDefinition : definitions) {
        if (oldDefinition.getKey().equals(newDefinition.getKey())) {
          log.debug(
              "Migrating from old defintion: {}, to new definition: {}",
              oldDefinition.getKey(),
              newDefinition.getKey());

          MigrationPlan plan = createMigrationPlan(engine, oldDefinition, newDefinition);

          if (plan == null) {
            continue;
          }

          ProcessInstanceQuery query =
              engine
                  .getRuntimeService()
                  .createProcessInstanceQuery()
                  .processDefinitionId(oldDefinition.getId());

          long nbInstances = query.count();
          log.debug("Process instances to migrate: {}", nbInstances);
          if (nbInstances > 0) {
            engine.getRuntimeService().newMigration(plan).processInstanceQuery(query).execute();
          }
        }
      }
    }
  }

  private MigrationPlan createMigrationPlan(
      ProcessEngine engine, ProcessDefinition oldDefinition, ProcessDefinition newDefinition) {

    Map<String, String> processMap = migrationMap.get(newDefinition.getKey());
    if (processMap == null) {
      return null;
    }

    MigrationPlanBuilder planBuilder =
        engine
            .getRuntimeService()
            .createMigrationPlan(oldDefinition.getId(), newDefinition.getId());

    MigrationPlan plan = null;

    ModelInstance modelInstance =
        engine.getRepositoryService().getBpmnModelInstance(oldDefinition.getId());

    for (String key : processMap.keySet()) {
      long count =
          engine
              .getHistoryService()
              .createHistoricActivityInstanceQuery()
              .processDefinitionId(oldDefinition.getId())
              .activityId(key)
              .unfinished()
              .count();

      if (count == 0) {
        continue;
      }

      ModelElementInstance instance = modelInstance.getModelElementById(key);

      String value = processMap.get(key);
      if (value != null) {
        if (instance
            .getElementType()
            .getTypeName()
            .equals(BpmnModelConstants.BPMN_ELEMENT_INTERMEDIATE_CATCH_EVENT)) {
          planBuilder.mapActivities(key, value).updateEventTrigger();
        } else {
          planBuilder.mapActivities(key, value);
        }
      }

      Collection<MultiInstanceLoopCharacteristics> childInstaces =
          instance.getChildElementsByType(MultiInstanceLoopCharacteristics.class);

      if (childInstaces != null && !childInstaces.isEmpty()) {
        planBuilder.mapActivities(key + "#multiInstanceBody", value + "#multiInstanceBody");
      }
    }

    plan = planBuilder.build();

    return plan;
  }

  protected void addDmn(DeploymentBuilder deploymentBuilder, Set<MetaFile> dmnFiles) {

    MetaFileRepository metaFileRepo = Beans.get(MetaFileRepository.class);
    for (MetaFile dmnFile : dmnFiles) {
      dmnFile = metaFileRepo.find(dmnFile.getId());
      deploymentBuilder.addModelInstance(
          dmnFile.getId() + ".dmn", Dmn.readModelFromFile(MetaFiles.getPath(dmnFile).toFile()));
    }
  }

  protected void addDisplayProperties(BpmnModelInstance bpmInstance, WkfProcess process) {

    BaseElement processElement = bpmInstance.getModelElementById(process.getName());

    if (processElement != null) {
      wkfService.addProperties(
          WkfPropertyMapper.PROCESS_DISPLAY_PROPERTIES, process, processElement);
    }
  }

  private void addProcessConfig(BpmnModelInstance bpmInstance, WkfProcess process) {

    BaseElement processElement = bpmInstance.getModelElementById(process.getName());
    ExtensionElements extensionElements = processElement.getExtensionElements();
    if (extensionElements == null) {
      return;
    }

    ModelBuilderImpl builderImpl = new ModelBuilderImpl(null);
    ModelElementType processConfigType =
        builderImpl.defineGenericType(
            "processConfiguration", BpmnParser.CAMUNDA_BPMN_EXTENSIONS_NS);
    List<ModelElementInstance> processConfigElements =
        extensionElements.getElementsQuery().filterByType(processConfigType).list();
    if (processConfigElements == null || processConfigElements.size() == 0) {
      return;
    }

    ModelElementInstance processConfigElement = processConfigElements.get(0);

    Map<String, WkfProcessConfig> configMap = createConfigMap(process);

    ModelElementType processConfigParamType =
        builderImpl.defineGenericType(
            "processConfigurationParameter", BpmnParser.CAMUNDA_BPMN_EXTENSIONS_NS);
    Collection<ModelElementInstance> configParams =
        processConfigElement.getChildElementsByType(processConfigParamType);

    for (ModelElementInstance configParam : configParams) {
      WkfProcessConfig config = getProcessCofig(configMap, configParam);
      config =
          (WkfProcessConfig)
              wkfService.addProperties(
                  WkfPropertyMapper.PROCESS_CONFIG_PROPERTIES, config, configParam);
      process.addWkfProcessConfigListItem(config);
    }
  }

  private Map<String, WkfProcessConfig> createConfigMap(WkfProcess process) {

    Map<String, WkfProcessConfig> configMap = new HashMap<String, WkfProcessConfig>();

    if (process.getWkfProcessConfigList() != null) {
      for (WkfProcessConfig config : process.getWkfProcessConfigList()) {
        if (config.getMetaModel() != null) {
          configMap.put(config.getMetaModel().getName(), config);
        } else if (config.getMetaJsonModel() != null) {
          configMap.put(config.getMetaJsonModel().getName(), config);
        }
      }
      process.clearWkfProcessConfigList();
    }

    return configMap;
  }

  private WkfProcessConfig getProcessCofig(
      Map<String, WkfProcessConfig> configMap, ModelElementInstance configParam) {

    String metaModel =
        configParam.getAttributeValueNs(BpmnParser.CAMUNDA_BPMN_EXTENSIONS_NS, "metaModel");
    if (configMap.containsKey(metaModel)) {
      return configMap.get(metaModel);
    }

    String jsonModel =
        configParam.getAttributeValueNs(BpmnParser.CAMUNDA_BPMN_EXTENSIONS_NS, "metaJsonModel");
    if (configMap.containsKey(jsonModel)) {
      return configMap.get(jsonModel);
    }

    return new WkfProcessConfig();
  }
}
