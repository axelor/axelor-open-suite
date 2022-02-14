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
package com.axelor.apps.dmn.service;

import com.axelor.apps.bpm.db.DmnField;
import com.axelor.apps.bpm.db.DmnTable;
import com.axelor.apps.bpm.db.WkfDmnModel;
import com.axelor.apps.bpm.db.repo.WkfDmnModelRepository;
import com.axelor.apps.bpm.service.init.ProcessEngineService;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DmnDeploymentServiceImpl implements DmnDeploymentService {

  protected final Logger log = LoggerFactory.getLogger(DmnDeploymentServiceImpl.class);

  @Override
  @Transactional
  public void deploy(WkfDmnModel wkfDmnModel) {

    if (wkfDmnModel.getDiagramXml() == null) {
      return;
    }

    ProcessEngine engine = Beans.get(ProcessEngineService.class).getEngine();

    String key = wkfDmnModel.getId() + ".dmn";
    DmnModelInstance dmnModelInstance =
        Dmn.readModelFromStream(new ByteArrayInputStream(wkfDmnModel.getDiagramXml().getBytes()));

    DeploymentBuilder deploymentBuilder =
        engine.getRepositoryService().createDeployment().addModelInstance(key, dmnModelInstance);

    deploymentBuilder.deploy();

    setDecisionTables(wkfDmnModel, dmnModelInstance);

    Beans.get(WkfDmnModelRepository.class).save(wkfDmnModel);
  }

  private void setDecisionTables(WkfDmnModel wkfDmnModel, DmnModelInstance dmnModelInstance) {

    Map<String, DmnTable> dmnTableMap = new HashMap<String, DmnTable>();

    if (wkfDmnModel.getDmnTableList() != null) {
      dmnTableMap =
          wkfDmnModel.getDmnTableList().stream()
              .collect(Collectors.toMap(DmnTable::getDecisionId, dmnTable -> dmnTable));
    }
    wkfDmnModel.clearDmnTableList();

    Collection<DecisionTable> tables = dmnModelInstance.getModelElementsByType(DecisionTable.class);

    for (DecisionTable table : tables) {

      String decisionId = table.getParentElement().getAttributeValue("id");
      String decisionName = table.getParentElement().getAttributeValue("name");
      DmnTable dmnTable = dmnTableMap.get(decisionId);
      log.debug("Find decision table for id: {}, found: {}", decisionId, dmnTable);
      if (dmnTable == null) {
        dmnTable = new DmnTable();
        dmnTable.setWkfDmnModel(wkfDmnModel);
      }
      dmnTable.setName(decisionName);
      dmnTable.setDecisionId(decisionId);
      setDmnField(table, dmnTable);
      wkfDmnModel.addDmnTableListItem(dmnTable);
    }
  }

  private void setDmnField(DecisionTable decisionTable, DmnTable dmnTable) {

    Map<String, DmnField> outputFieldMap = new HashMap<String, DmnField>();
    if (dmnTable.getOutputDmnFieldList() != null) {
      outputFieldMap =
          dmnTable.getOutputDmnFieldList().stream()
              .collect(Collectors.toMap(DmnField::getName, dmnField -> dmnField));
    }
    dmnTable.clearOutputDmnFieldList();

    for (Output output : decisionTable.getOutputs()) {
      DmnField field = outputFieldMap.get(output.getName());
      log.debug("Find output for name: {}, found: {}", output.getName(), field);
      if (field == null) {
        field = new DmnField(output.getName());
        field.setOutputDmnTable(dmnTable);
      } else {
        field.setName(output.getName());
      }
      field.setFieldType(output.getTypeRef());
      dmnTable.addOutputDmnFieldListItem(field);
    }
  }
}
