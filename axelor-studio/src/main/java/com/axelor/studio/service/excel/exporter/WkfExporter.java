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
package com.axelor.studio.service.excel.exporter;

import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.studio.db.Wkf;
import com.axelor.studio.db.WkfNode;
import com.axelor.studio.db.WkfTransition;
import com.axelor.studio.service.CommonService;
import com.axelor.studio.service.builder.ModelBuilderService;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WkfExporter {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private ExcelExporterService excelExporterService;

  @Inject private MetaJsonModelRepository metaJsonModelRepo;

  @Inject private ModelBuilderService modelBuilderService;

  public void exportWkf(String moduleName, Wkf wkf, ExcelExporterService excelExporterService) {

    this.excelExporterService = excelExporterService;

    log.debug("Processing sheet: Wkf");

    Map<String, String> valMap = new HashMap<>();
    String model = getModel(wkf, moduleName);

    valMap.put(CommonService.WKF_NAME, wkf.getName());
    valMap.put(CommonService.WKF_MODEL, model);
    valMap.put(CommonService.WKF_JSON_FIELD, wkf.getJsonField());
    valMap.put(
        CommonService.WKF_JSON, wkf.getIsJson() && wkf.getModel().equals(model) ? "x" : null);
    valMap.put(CommonService.WKF_STATUS, wkf.getStatusField().getName());
    valMap.put(CommonService.WKF_DISPLAY, wkf.getDisplayTypeSelect().toString());
    valMap.put(CommonService.WKF_XML, wkf.getBpmnXml());
    valMap.put(
        CommonService.WKF_APP, wkf.getAppBuilder() != null ? wkf.getAppBuilder().getCode() : null);
    valMap.put(CommonService.WKF_DESC, wkf.getDescription());

    excelExporterService.writeWkfRow(valMap, CommonService.WKF_HEADER);
  }

  private String getModel(Wkf wkf, String moduleName) {

    if (wkf.getIsJson()) {
      MetaJsonModel jsonModel = metaJsonModelRepo.findByName(wkf.getModel());
      if (jsonModel != null && jsonModel.getIsReal()) {
        return modelBuilderService.getModelFullName(moduleName, jsonModel.getName());
      }
    }

    return wkf.getModel();
  }

  public void exportWkfNodes(List<WkfNode> wkfNodes) {

    log.debug("Processing sheet: WkfNode");

    for (WkfNode node : wkfNodes) {
      Map<String, String> valMap = new HashMap<>();
      List<String> actions =
          node.getMetaActionSet().stream().map(it -> it.getName()).collect(Collectors.toList());

      valMap.put(CommonService.WKF_NODE_NAME, node.getName());
      valMap.put(CommonService.WKF_NODE_TITLE, node.getTitle());
      valMap.put(CommonService.WKF_NODE_XML, node.getXmlId());
      valMap.put(CommonService.WKF_NODE_WKF, node.getWkf().getName());
      valMap.put(
          CommonService.WKF_NODE_FIELD,
          node.getMetaField() != null ? node.getMetaField().getName() : null);
      valMap.put(
          CommonService.WKF_NODE_FIELD_MODEL,
          node.getMetaField() != null ? node.getMetaField().getMetaModel().getName() : null);
      valMap.put(CommonService.WKF_NODE_SEQ, node.getSequence().toString());
      valMap.put(CommonService.WKF_NODE_TYPE, node.getNodeType().toString());
      valMap.put(CommonService.WKF_NODE_ACTIONS, Joiner.on(",").join(actions));

      excelExporterService.writeWkfRow(valMap, CommonService.WKF_NODE_HEADER);
    }
  }

  public void exportWkfTransition(List<WkfTransition> transitions) {

    log.debug("Processing sheet: WkfTransition");

    for (WkfTransition transition : transitions) {
      Map<String, String> valMap = new HashMap<>();

      valMap.put(CommonService.WKF_TRANS_NAME, transition.getName());
      valMap.put(CommonService.WKF_TRANS_XML, transition.getXmlId());
      valMap.put(CommonService.WKF_TRANS_BUTTON, transition.getIsButton() ? "x" : null);
      valMap.put(CommonService.WKF_TRANS_BUTTON_TITLE, transition.getButtonTitle());
      valMap.put(CommonService.WKF_TRANS_WKF, transition.getWkf().getName());
      valMap.put(CommonService.WKF_TRANS_SOURCE_NODE, transition.getSource().getName());
      valMap.put(CommonService.WKF_TRANS_TARGET_NODE, transition.getTarget().getName());
      valMap.put(CommonService.WKF_TRANS_ALERT_TYPE, transition.getAlertTypeSelect().toString());
      valMap.put(CommonService.WKF_TRANS_ALERT_MSG, transition.getAlertMsg());
      valMap.put(CommonService.WKF_TRANS_SUCCESS_MSG, transition.getSuccessMsg());

      excelExporterService.writeWkfRow(valMap, CommonService.WKF_TRANSITION_HEADER);
    }
  }
}
