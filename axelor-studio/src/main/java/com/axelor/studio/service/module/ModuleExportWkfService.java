package com.axelor.studio.service.module;

import com.axelor.data.csv.CSVBind;
import com.axelor.data.csv.CSVConfig;
import com.axelor.data.csv.CSVInput;
import com.axelor.studio.db.Wkf;
import com.axelor.studio.db.WkfNode;
import com.axelor.studio.db.WkfTransition;
import com.axelor.studio.db.repo.WkfRepository;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

public class ModuleExportWkfService {

  @Inject private WkfRepository wkfRepo;

  @Inject private ModuleExportDataInitService exportDataInitService;

  private static final String[] WKF_HEADER =
      new String[] {
        "name",
        "model",
        "jsonField",
        "isJson",
        "status",
        "displayTypeSelect",
        "bpmnXml",
        "appBuilder.code",
        "description"
      };

  private static final String[] WKF_NODE_HEADER =
      new String[] {
        "name",
        "title",
        "xmlId",
        "wkf",
        "field",
        "fieldModel",
        "sequence",
        "startNode",
        "endNode",
        "actions"
      };

  private static final String[] WKF_TRANSITION_HEADER =
      new String[] {
        "name",
        "xmlId",
        "isButton",
        "buttonTitle",
        "wkf",
        "sourceNode",
        "targetNode",
        "alertTypeSelect",
        "alertMsg",
        "successMsg"
      };

  public void exportWkf(String modulePrefix, ZipOutputStream zipOut, CSVConfig csvConfig)
      throws IOException {

    //		List<Wkf> wkfs = wkfRepo.all().filter("self.isJson = true "
    //				+ "AND self.model in (SELECT name FROM MetaJsonModel WHERE isReal = false) "
    //				+ "OR self.model in (SELECT name FROM MetaModel WHERE isReal = false)")
    //				.fetch();

    List<Wkf> wkfs = wkfRepo.all().fetch();

    List<String[]> data = new ArrayList<>();
    List<WkfNode> nodes = new ArrayList<>();
    List<WkfTransition> transitions = new ArrayList<>();

    for (Wkf wkf : wkfs) {
      data.add(
          new String[] {
            wkf.getName(),
            wkf.getModel(),
            wkf.getJsonField(),
            wkf.getIsJson().toString(),
            wkf.getStatusField().getName(),
            wkf.getDisplayTypeSelect().toString(),
            wkf.getBpmnXml(),
            wkf.getAppBuilder() != null ? wkf.getAppBuilder().getCode() : null,
            wkf.getDescription()
          });
      nodes.addAll(wkf.getNodes());
      transitions.addAll(wkf.getTransitions());
    }

    if (!wkfs.isEmpty()) {

      String fileName = modulePrefix + Wkf.class.getSimpleName() + ".csv";
      exportDataInitService.addCsv(zipOut, fileName, WKF_HEADER, data);

      CSVInput input =
          exportDataInitService.createCSVInput(
              fileName, Wkf.class.getName(), null, "self.name = :name");

      CSVBind bind =
          exportDataInitService.createCSVBind(
              "status",
              "statusField",
              "self.name = :status AND (self.model = :model OR self.jsonModel.name = :model)",
              null,
              true);
      input.getBindings().add(bind);
      csvConfig.getInputs().add(input);

      addNodes(modulePrefix, zipOut, csvConfig, nodes);
      addTransitions(modulePrefix, zipOut, csvConfig, transitions);
    }
  }

  private void addNodes(
      String modulePrefix, ZipOutputStream zipOut, CSVConfig csvConfig, List<WkfNode> nodes)
      throws IOException {

    List<String[]> data = new ArrayList<>();

    for (WkfNode node : nodes) {
      List<String> actions =
          node.getMetaActionSet().stream().map(it -> it.getName()).collect(Collectors.toList());
      data.add(
          new String[] {
            node.getName(),
            node.getTitle(),
            node.getXmlId(),
            node.getWkf().getName(),
            node.getMetaField() != null ? node.getMetaField().getName() : null,
            node.getMetaField() != null ? node.getMetaField().getMetaModel().getName() : null,
            node.getSequence().toString(),
            node.getStartNode().toString(),
            node.getEndNode().toString(),
            Joiner.on("|").join(actions)
          });
    }

    String fileName = modulePrefix + WkfNode.class.getSimpleName() + ".csv";
    exportDataInitService.addCsv(zipOut, fileName, WKF_NODE_HEADER, data);

    CSVInput input =
        exportDataInitService.createCSVInput(
            fileName, WkfNode.class.getName(), null, "self.name = :name and self.wkf.name = :wkf");

    CSVBind bind =
        exportDataInitService.createCSVBind(
            "field",
            "metaField",
            "self.name = :field AND self.metaModel.name = :fieldModel",
            null,
            true);
    input.getBindings().add(bind);

    bind =
        exportDataInitService.createCSVBind(
            "actions",
            "metaActionSet",
            "self.name in :actions",
            "actions.split('|') as List",
            true);
    input.getBindings().add(bind);

    bind = exportDataInitService.createCSVBind("wkf", "wkf", "self.name =:wkf", null, true);
    input.getBindings().add(bind);

    csvConfig.getInputs().add(input);
  }

  private void addTransitions(
      String modulePrefix,
      ZipOutputStream zipOut,
      CSVConfig csvConfig,
      List<WkfTransition> transitions)
      throws IOException {

    List<String[]> data = new ArrayList<>();

    for (WkfTransition transition : transitions) {
      data.add(
          new String[] {
            transition.getName(),
            transition.getXmlId(),
            transition.getIsButton().toString(),
            transition.getButtonTitle(),
            transition.getWkf().getName(),
            transition.getSource().getName(),
            transition.getTarget().getName(),
            transition.getAlertTypeSelect().toString(),
            transition.getAlertMsg(),
            transition.getSuccessMsg()
          });
    }

    String fileName = modulePrefix + WkfTransition.class.getSimpleName() + ".csv";
    exportDataInitService.addCsv(zipOut, fileName, WKF_TRANSITION_HEADER, data);

    CSVInput input =
        exportDataInitService.createCSVInput(
            fileName,
            WkfTransition.class.getName(),
            null,
            "self.name = :name and self.wkf.name = :wkf");

    CSVBind bind = exportDataInitService.createCSVBind("wkf", "wkf", "self.name =:wkf", null, true);
    input.getBindings().add(bind);

    csvConfig.getInputs().add(input);
  }
}
