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
package com.axelor.studio.web;

import com.axelor.common.Inflector;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.views.Selection.Option;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.Wkf;
import com.axelor.studio.db.WkfNode;
import com.axelor.studio.db.WkfTransition;
import com.axelor.studio.db.repo.WkfNodeRepository;
import com.axelor.studio.db.repo.WkfRepository;
import com.axelor.studio.db.repo.WkfTransitionRepository;
import com.axelor.studio.exception.IExceptionMessage;
import com.axelor.studio.service.wkf.WkfDesignerService;
import com.axelor.studio.service.wkf.WkfService;
import com.axelor.studio.translation.ITranslation;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

public class WkfController {

  @Inject private WkfRepository wkfRepo;
  @Inject private WkfDesignerService wkfDesignerService;
  @Inject private WkfService wkfService;

  public void processXml(ActionRequest request, ActionResponse response) {
    try {
      Wkf workflow = request.getContext().asType(Wkf.class);
      workflow = wkfRepo.find(workflow.getId());
      wkfDesignerService.processXml(workflow);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void processWkf(ActionRequest request, ActionResponse response) {
    try {
      Wkf workflow = request.getContext().asType(Wkf.class);
      workflow = wkfRepo.find(workflow.getId());
      wkfService.process(workflow);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void onNodeEdit(ActionRequest request, ActionResponse response) {
    try {
      WkfNodeRepository repo = Beans.get(WkfNodeRepository.class);
      WkfNode node = request.getContext().asType(WkfNode.class);
      if (node.getWkf().getId() != null) {
        WkfNode found =
            repo.all()
                .filter(
                    "self.wkf.id = ? and self.xmlId = ?", node.getWkf().getId(), node.getXmlId())
                .fetchOne();
        if (found != null) {
          Map<String, Object> view =
              ActionView.define(I18n.get(ITranslation.WKF_EDIT_NODE))
                  .add("form", "wkf-node-form")
                  .model(WkfNode.class.getName())
                  .context("_showRecord", found.getId())
                  .param("popup", "true")
                  .param("show-toolbar", "false")
                  .param("forceEdit", "true")
                  .map();
          response.setView(view);
        } else {
          response.setFlash(I18n.get(IExceptionMessage.WKF_1));
        }
      } else {
        response.setFlash(I18n.get(IExceptionMessage.WKF_1));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void onTransitionEdit(ActionRequest request, ActionResponse response) {
    try {
      WkfTransitionRepository repo = Beans.get(WkfTransitionRepository.class);
      WkfTransition transition = request.getContext().asType(WkfTransition.class);
      WkfTransition found = repo.all().filter("self.xmlId = ?", transition.getXmlId()).fetchOne();
      if (found != null) {

        Map<String, Object> view =
            ActionView.define(I18n.get(ITranslation.WKF_EDIT_TRANSITION))
                .add("form", "wkf-transition-form")
                .model(WkfTransition.class.getName())
                .context("_showRecord", found.getId())
                .param("popup", "true")
                .param("show-toolbar", "false")
                .param("forceEdit", "true")
                .map();

        response.setView(view);
      } else {
        response.setFlash(I18n.get(IExceptionMessage.WKF_1));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setDefaultNodes(ActionRequest request, ActionResponse response) {

    Wkf wkf = request.getContext().asType(Wkf.class);

    MetaJsonField wkfField = wkf.getStatusField();

    if (wkfField != null) {

      Optional<Pair<String, String>> nodesOpt = getDefaultNodes(wkfField);
      nodesOpt.ifPresent(
          nodes -> {
            String bpmnXml =
                " <?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                    + "<definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                    + "xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" "
                    + "xmlns:x=\"http://axelor.com\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" "
                    + "xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" "
                    + "targetNamespace=\"http://bpmn.io/schema/bpmn\" id=\"Definitions_1\"> "
                    + "<process id=\"Process_1\" name=\""
                    + wkf.getName()
                    + "\" x:id=\""
                    + wkf.getId()
                    + "\" isExecutable=\"false\"> "
                    + nodes.getLeft()
                    + "</process>"
                    + "<bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">"
                    + "<bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"Process_1\">"
                    + nodes.getRight()
                    + "</bpmndi:BPMNPlane>"
                    + "</bpmndi:BPMNDiagram>"
                    + "</definitions>";
            response.setValue("$bpmnDefault", bpmnXml);
          });
    }
  }

  private Optional<Pair<String, String>> getDefaultNodes(MetaJsonField statusField) {

    //		String[] nodes = new String[] {
    //				"<startEvent id=\"StartEvent_1\" /><task id=\"Task_1\" /><endEvent id=\"EndEvent_1\"/>",
    //				"<bpmndi:BPMNShape id=\"_BPMNShape_StartEvent_2\" bpmnElement=\"StartEvent_1\">"
    //					+ "<dc:Bounds x=\"100\" y=\"100\" width=\"36\" height=\"36\"/>"
    //				+ "</bpmndi:BPMNShape>"
    //				+ "<bpmndi:BPMNShape id=\"_BPMNShape_Task_1\" bpmnElement=\"Task_1\">"
    //					+ "<dc:Bounds x=\"250\" y=\"100\" width=\"100\" height=\"80\"/>"
    //				+ "</bpmndi:BPMNShape>"
    //				+ "<bpmndi:BPMNShape id=\"_BPMNShape_EndEvent_2\" bpmnElement=\"EndEvent_1\">"
    //					+ "<dc:Bounds x=\"500\" y=\"100\" width=\"36\" height=\"36\"/>"
    //				+ "</bpmndi:BPMNShape>"
    //		};
    //
    List<Option> select = getSelect(statusField);

    if (!CollectionUtils.isEmpty(select)) {
      StringBuilder elements = new StringBuilder();
      StringBuilder designs = new StringBuilder();
      int count = 1;
      int x = 100;
      int y;
      for (Option option : select) {
        String element = null;
        int width = 100;
        int height = 80;
        if (count == 1) {
          width = 36;
          height = 36;
          y = 125;
          element = "startEvent";
        } else if (count == select.size()) {
          width = 36;
          height = 36;
          x += 150;
          y = 125;
          element = "endEvent";
        } else {
          y = 100;
          x += 150;
          element = "task";
        }

        String elementId = Inflector.getInstance().camelize(element, false) + "_" + count;
        element = "<" + element + " id=\"" + elementId + "\" name=\"" + option.getTitle() + "\" />";
        elements.append(element);

        String design =
            "<bpmndi:BPMNShape id=\"_BPMNShape_"
                + elementId
                + "\" bpmnElement=\""
                + elementId
                + "\" >"
                + "<dc:Bounds x=\""
                + x
                + "\" y=\""
                + y
                + "\" width=\""
                + width
                + "\" height=\""
                + height
                + "\" />"
                + "</bpmndi:BPMNShape>";

        designs.append(design);

        count++;
      }

      if (elements.length() > 0) {
        return Optional.of(Pair.of(elements.toString(), designs.toString()));
      }
    }

    return Optional.empty();
  }

  private List<Option> getSelect(MetaJsonField wkfField) {

    if (wkfField == null) {
      return Collections.emptyList();
    }

    if (wkfField.getSelection() != null) {
      return MetaStore.getSelectionList(wkfField.getSelection());
    }

    return Collections.emptyList();
  }
}
