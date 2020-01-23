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
package com.axelor.studio.service.wkf;

import com.axelor.auth.db.repo.RoleRepository;
import com.axelor.common.Inflector;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.actions.ActionAttrs;
import com.axelor.meta.schema.actions.ActionAttrs.Attribute;
import com.axelor.meta.schema.actions.ActionGroup;
import com.axelor.meta.schema.actions.ActionGroup.ActionItem;
import com.axelor.meta.schema.actions.ActionMethod;
import com.axelor.meta.schema.actions.ActionMethod.Call;
import com.axelor.studio.db.Wkf;
import com.axelor.studio.db.WkfNode;
import com.axelor.studio.db.WkfTransition;
import com.axelor.studio.db.repo.WkfRepository;
import com.axelor.studio.service.StudioMetaService;
import com.axelor.studio.service.filter.FilterGroovyService;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service class handle workflow processing. It updated related models/views according to update in
 * workflow. Also remove effects of workflow it some workflow deleted. Call node and transition
 * service for further processing.
 *
 * @author axelor
 */
public class WkfService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected Wkf workflow = null;

  protected String wkfCode = null;

  protected Inflector inflector;

  protected Integer wkfSequence = 0;

  protected Integer wkfSequencePreview = 0;

  protected String applyCondition = null;

  protected String trackingAction = null;

  protected String trackingActionPreview = null;

  @Inject protected RoleRepository roleRepo;

  @Inject private WkfNodeService nodeService;
  @Inject private WkfTransitionService transitionService;
  @Inject private StudioMetaService metaService;
  @Inject private MetaJsonFieldRepository jsonFieldRepo;
  @Inject private MetaJsonModelRepository jsonModelRepo;
  @Inject private MetaSelectRepository metaSelectRepo;
  @Inject private FilterGroovyService filterGroovyService;
  @Inject private WkfRepository wkfRepository;

  /**
   * Method to process workflow. It calls node and transition service for nodes and transitions
   * linked with workflow.
   *
   * @param wkf Worklfow to process.
   * @return Exception string if any issue in processing else null.
   */
  public String process(Wkf wkf) {

    try {
      initService(wkf);
      createTrackingAction();
      createPreviewTrackingAction();
      setWkfSequence();
      setWkfPreviewSequence();
      setView();

      List<String[]> actions = nodeService.process();
      setPreview();
      actions.addAll(transitionService.process());
      actions.add(new String[] {"save"});
      actions.add(new String[] {trackingAction});
      updateActionGroup("action-group-" + wkfCode, actions);

      List<String[]> previewActions = new ArrayList<String[]>();
      previewActions.add(new String[] {"save"});
      previewActions.add(new String[] {trackingActionPreview});
      updateActionGroup("action-group-" + workflow.getCode(), previewActions);

      setStatusValue();

      MetaStore.clear();

    } catch (Exception e) {
      e.printStackTrace();
      return e.toString();
    }

    return null;
  }

  private void initService(Wkf wkf) {
    workflow = wkf;
    inflector = Inflector.getInstance();
    wkfCode = "wkf" + wkf.getCode().toString() + "wkf";
    trackingAction = "action-method-wkf-track-" + wkfCode;
    trackingActionPreview = "action-method-wkf-track-" + workflow.getCode();
  }

  private void setView() {

    //    clearOldStatusField();

    MetaJsonField status = workflow.getStatusField();

    applyCondition =
        filterGroovyService.getGroovyFilters(workflow.getConditions(), workflow.getJsonField());

    MetaJsonField panel = getJsonField(wkfCode + "Panel", "panel");
    panel.setSequence(wkfSequence - 49);
    panel.setVisibleInGrid(false);
    panel.setIsWkf(true);
    panel.setWidgetAttrs("{\"colSpan\": \"12\"}");
    saveJsonField(panel);

    status.setSequence(wkfSequence - 48);
    status.setSelection(getSelectName());
    status.setWidget(null);
    status.setIsWkf(true);
    status.setReadonly(true);
    status.setShowIf(applyCondition);
    status.setWidgetAttrs("{\"colSpan\": \"10\"}");
    if (workflow.getDisplayTypeSelect() == 0) {
      status.setWidget("NavSelect");
    }
    saveJsonField(workflow.getStatusField());

    MetaJsonField trackFlow = getJsonField(wkfCode + "TrackFlow", "button");
    trackFlow.setSequence(wkfSequence - 47);
    trackFlow.setTitle("Track flow");
    trackFlow.setWidgetAttrs("{\"colSpan\": \"2\"}");
    trackFlow.setOnClick(WkfTrackingService.ACTION_OPEN_TRACK);
    trackFlow.setIsWkf(true);
    trackFlow.setVisibleInGrid(false);
    trackFlow.setHidden(!workflow.getIsTrackFlow());
    saveJsonField(trackFlow);

    MetaJsonField wkfEnd = getJsonField(wkfCode + "Separator", "separator");
    wkfEnd.setSequence(wkfSequence);
    wkfEnd.setVisibleInGrid(false);
    wkfEnd.setIsWkf(true);
    wkfEnd.setWidgetAttrs("{\"colSpan\": \"12\"}");
    saveJsonField(panel);

    //    setTrackOnSave(workflow, false);
  }

  private void setPreview() {

    if (findJsonField(wkfCode + workflow.getCode() + "Panel") == null) {

      MetaJsonField panel = getJsonField(wkfCode + "Panel", "panel");
      panel = jsonFieldRepo.copy(panel, true);
      panel.setName(wkfCode + workflow.getCode() + "Panel");
      createPreviewMetaJsonField(
          panel, 49, "$record.code == '" + workflow.getCode() + "' && $record.id != null");
    }

    if (findJsonField(wkfCode + workflow.getStatusField().getName()) == null) {

      MetaJsonField status = jsonFieldRepo.copy(workflow.getStatusField(), true);

      status.setName(wkfCode + workflow.getStatusField().getName());
      createPreviewMetaJsonField(
          status, 48, "$record.code == '" + workflow.getCode() + "' && $record.id != null");
    }

    if (findJsonField(wkfCode + workflow.getCode() + "TrackFlow") == null) {
      MetaJsonField trackFlow = getJsonField(wkfCode + "TrackFlow", "button");
      trackFlow = jsonFieldRepo.copy(trackFlow, true);
      trackFlow.setName(wkfCode + workflow.getCode() + "TrackFlow");
      createPreviewMetaJsonField(
          trackFlow,
          47,
          "$record.code == '"
              + workflow.getCode()
              + "' && $record.isTrackFlow == true && $record.id != null");
    }

    if (findJsonField(wkfCode + workflow.getCode() + "Seprator") == null) {
      MetaJsonField wkfEnd = getJsonField(wkfCode + "Separator", "separator");
      wkfEnd = jsonFieldRepo.copy(wkfEnd, true);
      wkfEnd.setName(wkfCode + workflow.getCode() + "Seprator");
      createPreviewMetaJsonField(
          wkfEnd, 0, "$record.code == '" + workflow.getCode() + "' && $record.id != null");
    }
  }

  public void createPreviewMetaJsonField(MetaJsonField jsonField, int sequence, String condition) {

    jsonField.setSequence(wkfSequencePreview - sequence);
    jsonField.setModel("com.axelor.studio.db.Wkf");
    jsonField.setModelField("attrs");
    jsonField.setJsonModel(null);
    jsonField.setShowIf(condition);
    saveJsonField(jsonField);
  }

  @Transactional
  public void clearOldStatusField() {

    MetaJsonField field = null;
    if (workflow.getIsJson()) {
      field =
          jsonFieldRepo
              .all()
              .filter(
                  "self.isWkf = true "
                      + "and self.jsonModel.name = :workflowModel "
                      + "and self.type in ('string','integer')")
              .bind("workflowModel", workflow.getModel())
              .fetchOne();
    } else {
      field =
          jsonFieldRepo
              .all()
              .filter(
                  "self.isWkf = true "
                      + "and self.model = :workflowModel "
                      + "and self.modelField = :workflowJsonField "
                      + "and self.type in ('string','integer')")
              .bind("workflowModel", workflow.getModel())
              .bind("workflowJsonField", workflow.getJsonField())
              .fetchOne();
    }

    if (field != null) {
      if (field.getSelection() != null) {
        log.debug("Cleaning old status field: {}", field);
        MetaSelect oldSelect = metaSelectRepo.findByName(field.getSelection());
        if (oldSelect != null) {
          log.debug("Removing old wkf selection: {}", oldSelect);
          metaSelectRepo.remove(oldSelect);
        }
      }
      field.setIsWkf(false);
      field.setSelection(null);
      field.setSequence(0);
      field.setReadonly(false);
      jsonFieldRepo.save(field);
    }
  }

  public String getSelectName() {

    if (workflow != null) {
      MetaJsonField wkfField = workflow.getStatusField();
      String selectName = "wkf." + inflector.dasherize(workflow.getName()).replace("_", ".");
      selectName += "." + inflector.dasherize(wkfField.getName()).replace("_", ".") + ".select";

      return selectName;
    }

    return null;
  }

  /**
   * Update xml of MetaAction with xml string passed. It creates new MetaAction if no MetaAction not
   * found.
   *
   * @param actionName Name of MetaAction to update.
   * @param actionType Type of MetaAction.
   * @param xml Xml to update in MetaAction.
   */
  protected ActionGroup updateActionGroup(String name, List<String[]> actions) {

    ActionGroup actionGroup = new ActionGroup();
    actionGroup.setName(name);
    List<ActionItem> actionItems = new ArrayList<>();

    for (String[] action : actions) {
      ActionItem actionItem = new ActionItem();
      actionItem.setName(action[0]);
      if (action.length > 1) {
        actionItem.setCondition(action[1]);
      }
      actionItems.add(actionItem);
    }

    actionGroup.setActions(actionItems);

    String xml = XMLViews.toXml(actionGroup, true);

    metaService.updateMetaAction(name, "action-group", xml, null);

    return actionGroup;
  }

  public MetaJsonField getJsonField(String name, String type) {

    MetaJsonField field = null;
    if (workflow.getIsJson()) {
      field =
          jsonFieldRepo
              .all()
              .filter(
                  "self.isWkf = true "
                      + "and self.jsonModel.name = ?1 "
                      + "and self.name = ?2 and self.type = ?3",
                  workflow.getModel(),
                  name,
                  type)
              .fetchOne();
      if (field == null) {
        field = new MetaJsonField();
        field.setModel(MetaJsonRecord.class.getName());
        field.setModelField("attrs");
        field.setJsonModel(jsonModelRepo.findByName(workflow.getModel()));
      }
    } else {
      field =
          jsonFieldRepo
              .all()
              .filter(
                  "self.isWkf = true "
                      + "and self.model = :workflowModel "
                      + "and self.modelField = :workflowJsonField "
                      + "and self.name = :name and self.type = :type")
              .bind("workflowModel", workflow.getModel())
              .bind("workflowJsonField", workflow.getJsonField())
              .bind("name", name)
              .bind("type", type)
              .fetchOne();

      log.debug(
          "Searching json field with model: {}, field: {}, name: {}, type: {}",
          workflow.getModel(),
          workflow.getJsonField(),
          name,
          type);
      if (field == null) {
        field = new MetaJsonField();
        field.setModel(workflow.getModel());
        field.setModelField(workflow.getJsonField());
        field.setName(name);
      }
    }

    field.setType(type);
    field.setName(name);
    field.setIsWkf(true);
    field.setShowIf(applyCondition);

    return saveJsonField(field);
  }

  @Transactional
  public MetaJsonField saveJsonField(MetaJsonField jsonField) {

    return jsonFieldRepo.save(jsonField);
  }

  public MetaJsonField findJsonField(String name) {
    return jsonFieldRepo.findByName(name);
  }

  @Transactional
  public void clearWkf(Wkf wkf) {

    initService(wkf);

    String actions =
        "action-" + wkfCode + ",action-group-" + wkfCode + ",action-group-" + workflow.getCode();
    actions = clearFields(wkf, actions);

    StringBuilder builder = new StringBuilder(actions);
    for (WkfNode node : wkf.getNodes()) {
      if (!node.getMetaActionSet().isEmpty()) {
        builder.append("," + nodeService.getActionName(node.getName()));
      }
    }
    builder.append("," + trackingAction + "," + trackingActionPreview);
    actions = builder.toString();

    metaService.removeMetaActions(actions);

    String select = getSelectName();
    MetaSelect metaSelect = metaSelectRepo.findByName(select);
    if (metaSelect != null) {
      metaSelectRepo.remove(metaSelect);
    }

    MetaJsonField status = wkf.getStatusField();
    status.setWidget(null);
    status.setSelection(null);
    status.setWidgetAttrs(null);
    saveJsonField(status);

    //    setTrackOnSave(wkf, true);
  }

  public List<MetaJsonField> clearMetaJsonField(Wkf wkf) {

    List<MetaJsonField> metaJsonFieldList =
        jsonFieldRepo
            .all()
            .filter(
                "self.name LIKE :code and self.model =?1 and self.modelField =?2",
                "com.axelor.studio.db.Wkf",
                "attrs")
            .bind("code", wkfCode + "%")
            .fetch();

    return metaJsonFieldList;
  }

  private String clearFields(Wkf wkf, String actions) {

    List<MetaJsonField> fields = getFields(wkf);
    List<MetaJsonField> jsonFields = clearMetaJsonField(wkf);

    @SuppressWarnings("unchecked")
    List<MetaJsonField> combinedList = ListUtils.union(fields, jsonFields);

    StringBuilder builder = new StringBuilder(actions);
    for (MetaJsonField field : combinedList) {
      if (field.getIsWkf() && !field.equals(wkf.getStatusField())) {
        if (field.getOnClick() != null) {
          builder.append("," + field.getOnClick());
        }
        jsonFieldRepo.remove(field);
      }
    }

    return builder.toString();
  }

  private List<MetaJsonField> getFields(Wkf wkf) {

    List<MetaJsonField> fields;

    String query = "self.isWkf = true and self.name LIKE '" + wkfCode + "%' and ";

    if (wkf.getIsJson()) {
      fields =
          jsonFieldRepo.all().filter(query + "self.jsonModel.name = ?1", wkf.getModel()).fetch();
    } else {
      fields =
          jsonFieldRepo
              .all()
              .filter(
                  query + "self.model = ?1 and self.modelField = ?2",
                  wkf.getModel(),
                  wkf.getJsonField())
              .fetch();
    }
    return fields;
  }

  @SuppressWarnings("unchecked")
  @Transactional
  public String clearOldButtons(List<String> skipList) {

    log.debug("Cleaning old buttons. Skip list: {}", skipList);
    if (skipList.isEmpty()) {
      return null;
    }

    skipList.add(wkfCode + "TrackFlow");

    ArrayList<String> actions = new ArrayList<>();

    List<MetaJsonField> fields = null;
    List<MetaJsonField> metaJsonFields = null;
    List<MetaJsonField> combinedList = new ArrayList<MetaJsonField>();

    if (workflow.getIsJson()) {
      fields =
          jsonFieldRepo
              .all()
              .filter(
                  "self.type = 'button' "
                      + "and self.jsonModel.name = ?1 "
                      + "and self.isWkf = true "
                      + "and self.name not in (?2) "
                      + "and self.name LIKE ?3",
                  workflow.getModel(),
                  skipList,
                  wkfCode + "%")
              .fetch();

      metaJsonFields =
          jsonFieldRepo
              .all()
              .filter(
                  "self.type = 'button' and self.name LIKE :code and self.model =?1 and self.modelField =?2 and self.name not in (?3)",
                  "com.axelor.studio.db.Wkf",
                  "attrs",
                  skipList)
              .bind("code", wkfCode + "Transition%" + "Button")
              .fetch();

      combinedList = ListUtils.union(fields, metaJsonFields);

    } else {
      fields =
          jsonFieldRepo
              .all()
              .filter(
                  "self.type = 'button' "
                      + "and self.model = ?1 "
                      + "and self.modelField = ?2 "
                      + "and self.name not in (?3) "
                      + "and self.name LIKE ?4",
                  workflow.getModel(),
                  workflow.getJsonField(),
                  skipList,
                  wkfCode + "%")
              .fetch();
    }

    log.debug("Total Buttons to remove: {}", combinedList.size());

    Iterator<MetaJsonField> buttons = combinedList.iterator();

    MetaJsonModel jsonModel = jsonModelRepo.findByName(workflow.getModel());
    while (buttons.hasNext()) {

      MetaJsonField button = buttons.next();
      String onClick = button.getOnClick();
      log.debug("Removing button : {}, onClick: {}", button.getName(), onClick);

      if (onClick != null) {
        for (String action : onClick.split(",")) {
          if (!action.equals("action-group-" + wkfCode)
              && !action.equals("action-group-" + workflow.getCode())) {
            actions.add(action);
          }
        }
      }
      buttons.remove();
      jsonModel.getFields().remove(button);
      jsonFieldRepo.remove(button);
    }

    return Joiner.on(",").join(actions);
  }

  public void clearNodes(Collection<WkfNode> nodes) {

    List<String> actions = new ArrayList<>();

    for (WkfNode node : nodes) {
      if (workflow == null) {
        workflow = node.getWkf();
        inflector = Inflector.getInstance();
      }
      if (!node.getMetaActionSet().isEmpty()) {
        actions.add(nodeService.getActionName(node.getName()));
      }
    }

    metaService.removeMetaActions(Joiner.on(",").join(actions));
  }

  //  @Transactional
  //  public void setTrackOnSave(Wkf wkf, boolean remove) {
  //    if (wkf.getIsJson()) {
  //      MetaJsonModel jsonModel = jsonModelRepo.findByName(wkf.getModel());
  //      if (jsonModel != null) {
  //        String onSave =
  //            metaService.updateAction(
  //                jsonModel.getOnSave(), "save," + trackingAction, remove);
  //        jsonModel.setOnSave(onSave);
  //        jsonModelRepo.save(jsonModel);
  //      }
  //    }
  //  }

  @Transactional
  public void createTrackingAction() {

    ActionMethod actionMethod = new ActionMethod();
    actionMethod.setName(trackingAction);
    Call call = new Call();
    call.setMethod("track(" + workflow.getId() + "," + "__self__,false)");
    call.setController("com.axelor.studio.service.wkf.WkfTrackingService");
    actionMethod.setCall(call);
    String xml = XMLViews.toXml(actionMethod, true);

    metaService.updateMetaAction(trackingAction, "action-method", xml, null);
  }

  @Transactional
  public void createPreviewTrackingAction() {

    ActionMethod actionMethod = new ActionMethod();
    actionMethod.setName(trackingActionPreview);
    Call call = new Call();
    call.setMethod("track(" + workflow.getId() + "," + "__self__,true)");
    call.setController("com.axelor.studio.service.wkf.WkfTrackingService");
    actionMethod.setCall(call);
    String xml = XMLViews.toXml(actionMethod, true);

    metaService.updateMetaAction(trackingActionPreview, "action-method", xml, null);
  }

  @Transactional
  public void setWkfSequence() {

    Wkf wkf =
        wkfRepository
            .all()
            .filter("self.model = ?1", workflow.getModel())
            .order("wkfSequence")
            .fetchOne();

    if (wkf.getId() != workflow.getId() && workflow.getWkfSequence() == 0) {
      workflow.setWkfSequence(wkf.getWkfSequence() - 50);
      wkfRepository.save(workflow);
    } else if (workflow.getWkfSequence() == 0) {
      workflow.setWkfSequence(-1);
      wkfRepository.save(workflow);
    }

    wkfSequence = workflow.getWkfSequence();
  }

  @Transactional
  public void setWkfPreviewSequence() {
    int buttonCount = 0;
    int newButton = 0;

    for (WkfTransition transition : workflow.getTransitions()) {
      if (transition.getIsButton()) {
        buttonCount++;
      }
      if (transition.getIsButton() && transition.getConditions() == null) {
        newButton++;
      }
    }

    if (newButton != buttonCount && newButton > 0) {

      MetaJsonField jsonField =
          jsonFieldRepo
              .all()
              .filter(
                  "self.type = :type AND self.model = :model AND self.jsonModel is null AND self.name LIKE :code")
              .bind("type", "separator")
              .bind("model", "com.axelor.studio.db.Wkf")
              .bind("code", "%" + workflow.getCode() + "%")
              .order("sequence")
              .fetchOne();
      wkfSequencePreview = jsonField.getSequence();
      return;
    }

    MetaJsonField metaJsonField =
        jsonFieldRepo
            .all()
            .filter("self.type = :type AND self.model = :model AND self.jsonModel is null")
            .bind("type", "separator")
            .bind("model", "com.axelor.studio.db.Wkf")
            .order("sequence")
            .fetchOne();

    if (metaJsonField == null) {
      wkfSequencePreview = -1;
    } else if (buttonCount > 0) {
      wkfSequencePreview = metaJsonField.getSequence() - (15);
    }
  }

  public void setStatusValue() {

    ActionAttrs actionAttrs = new ActionAttrs();
    actionAttrs.setName("action-wkf-attrs-set-status-field-value");

    Attribute attribute = new Attribute();
    attribute.setExpression("eval:'1'");
    attribute.setFieldName(
        "wkf" + workflow.getCode() + "wkf" + workflow.getStatusField().getName());
    attribute.setName("value");

    List<Attribute> attributes = new ArrayList<ActionAttrs.Attribute>();
    attributes.add(attribute);

    actionAttrs.setAttributes(attributes);

    String xml = XMLViews.toXml(actionAttrs, true);
    metaService.updateMetaAction("action-wkf-attrs-set-status-field-value", "action-attrs", xml, null);
  }
}
