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
import com.axelor.apps.bpm.db.WkfTaskConfig;
import com.axelor.apps.bpm.db.repo.WkfTaskConfigRepository;
import com.axelor.apps.bpm.service.WkfCommonService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaAttrs;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.CatchEvent;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

public class WkfNodeService {

  @Inject protected MetaAttrsService metaAttrsService;

  @Inject protected WkfTaskConfigRepository wkfConfigRepository;

  @Inject protected WkfMenuService wkfMenuService;

  public List<MetaAttrs> extractNodes(
      WkfModel wkfModel, BpmnModelInstance bpmInstance, Map<String, String> processMap) {

    Map<String, WkfTaskConfig> configMap = new HashMap<>();
    if (wkfModel.getWkfTaskConfigList() != null) {
      for (WkfTaskConfig config : wkfModel.getWkfTaskConfigList()) {
        configMap.put(config.getName(), config);
      }
    }

    List<MetaAttrs> metaAttrsList = new ArrayList<MetaAttrs>();

    Collection<FlowNode> activities = new ArrayList<FlowNode>();
    activities.addAll(bpmInstance.getModelElementsByType(Activity.class));
    activities.addAll(bpmInstance.getModelElementsByType(CatchEvent.class));
    activities.addAll(bpmInstance.getModelElementsByType(EndEvent.class));

    if (activities != null) {
      for (FlowNode activity : activities) {
        WkfTaskConfig config = updateTaskConfig(wkfModel, configMap, metaAttrsList, activity);
        Process process = findProcess(activity);
        if (process != null) {
          config.setProcessId(processMap.get(process.getId()));
        }
        updateMenus(config, false);
        wkfModel.addWkfTaskConfigListItem(config);
      }
    }

    for (String name : configMap.keySet()) {
      updateMenus(configMap.get(name), true);
      wkfModel.removeWkfTaskConfigListItem(configMap.get(name));
    }

    return metaAttrsList;
  }

  private Process findProcess(FlowNode activity) {

    ModelElementInstance modelElementInstance = activity.getParentElement();

    while (modelElementInstance != null) {
      if (modelElementInstance instanceof Process) {
        return (Process) modelElementInstance;
      }
      modelElementInstance = modelElementInstance.getParentElement();
    }

    return null;
  }

  public WkfTaskConfig updateTaskConfig(
      WkfModel wkfModel,
      Map<String, WkfTaskConfig> configMap,
      List<MetaAttrs> metaAttrsList,
      FlowNode activity) {

    WkfTaskConfig config;

    if (configMap.containsKey(activity.getId())) {
      config = configMap.get(activity.getId());
      configMap.remove(activity.getId());
    } else {
      config = new WkfTaskConfig();
      config.setName(activity.getId());
      wkfConfigRepository.save(config);
    }
    config.setDescription(activity.getName());
    config.setType(activity.getElementType().getTypeName());
    config.setButton(null);
    config.setExpression(null);
    config =
        (WkfTaskConfig)
            Beans.get(WkfCommonService.class)
                .addProperties(WkfPropertyMapper.FIELD_MAP, config, activity);
    ExtensionElements extensionElements = activity.getExtensionElements();
    if (extensionElements != null) {
      for (ModelElementInstance modelElementInstance : extensionElements.getElements()) {
        metaAttrsList.addAll(
            metaAttrsService.createMetaAttrs(
                activity.getId(), modelElementInstance, config, wkfModel.getId().toString()));
      }
    }

    return config;
  }

  private void updateMenus(WkfTaskConfig taskConfig, boolean remove) {

    if (!remove && taskConfig.getNewMenu()) {
      wkfMenuService.createOrUpdateMenu(taskConfig);
    } else {
      wkfMenuService.removeMenu(taskConfig);
    }

    if (!remove && taskConfig.getUserNewMenu()) {
      wkfMenuService.createOrUpdateUserMenu(taskConfig);
    } else {
      wkfMenuService.removeUserMenu(taskConfig);
    }
  }
}
