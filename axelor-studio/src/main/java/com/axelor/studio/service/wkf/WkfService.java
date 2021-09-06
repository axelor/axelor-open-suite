/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaActionRepository;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.actions.ActionAttrs;
import com.axelor.meta.schema.actions.ActionAttrs.Attribute;
import com.axelor.meta.schema.actions.ActionGroup;
import com.axelor.meta.schema.actions.ActionGroup.ActionItem;
import com.axelor.meta.schema.actions.ActionMethod;
import com.axelor.meta.schema.actions.ActionMethod.Call;
import com.axelor.meta.schema.actions.ActionRecord;
import com.axelor.meta.schema.actions.ActionRecord.RecordField;
import com.axelor.meta.schema.views.AbstractWidget;
import com.axelor.meta.schema.views.Extend;
import com.axelor.meta.schema.views.ExtendItemAttribute;
import com.axelor.meta.schema.views.ExtendItemReplace;
import com.axelor.meta.schema.views.FormView;
import com.axelor.meta.schema.views.PanelField;
import com.axelor.rpc.Request;
import com.axelor.rpc.Resource;
import com.axelor.studio.db.MenuBuilder;
import com.axelor.studio.db.Wkf;
import com.axelor.studio.db.WkfNode;
import com.axelor.studio.db.WkfTransition;
import com.axelor.studio.db.repo.MenuBuilderRepo;
import com.axelor.studio.db.repo.WkfRepository;
import com.axelor.studio.exception.IExceptionMessage;
import com.axelor.studio.service.StudioMetaService;
import com.axelor.studio.service.builder.MenuBuilderService;
import com.axelor.studio.service.filter.FilterGroovyService;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
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

  protected String simpleModelName = null;

  protected String viewName = null;

  protected String actionName = null;

  @Inject protected RoleRepository roleRepo;
  @Inject private WkfNodeService nodeService;
  @Inject private WkfTransitionService transitionService;
  @Inject private StudioMetaService metaService;
  @Inject private MetaJsonFieldRepository jsonFieldRepo;
  @Inject private MetaJsonModelRepository jsonModelRepo;
  @Inject private MetaSelectRepository metaSelectRepo;
  @Inject private FilterGroovyService filterGroovyService;
  @Inject private WkfRepository wkfRepository;
  @Inject private MetaViewRepository metaViewRepo;
  @Inject private MetaActionRepository metaActionRepo;
  @Inject Resource<MetaView> resource;

  /**
   * Method to process workflow. It calls node and transition service for nodes and transitions
   * linked with workflow.
   *
   * @param wkf Worklfow to process.
   * @return Exception string if any issue in processing else null.
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public String process(Wkf wkf) throws Exception {

    try {
      initService(wkf);
      createTrackingAction();
      createPreviewTrackingAction();
      setWkfSequence();
      setWkfPreviewSequence();
      Map<String, Object> nodeValues = nodeService.process();

      String defaultValue =
          nodeValues.get("defaultValue") != null ? nodeValues.get("defaultValue").toString() : null;

      if (wkf.getIsJson() || wkf.getJsonField() != null) {
        if (workflow.getJsonField() != null) {
          clearFields("", false);
        }
        clearMetaView();
        setJsonView();
        wkf.getStatusField().setDefaultValue(defaultValue);
        wkf.setGeneratedMetaView(null);

      } else {
        if (workflow.getJsonField() == null) {
          clearFields("", false);
        }
        clearMetaView();
        setMetaView(defaultValue);
      }
      createTrackFlowButton();
      setPreview();

      List<String[]> actions = (List<String[]>) nodeValues.get("nodeActions");
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
      if (Objects.equal(e.getMessage(), IExceptionMessage.CANNOT_ALTER_NODES)) {
        throw e;
      }
      e.printStackTrace();
      return e.toString();
    }

    return null;
  }

  private void initService(Wkf wkf) {
    workflow = wkf;
    inflector = Inflector.getInstance();
    wkfCode = "wkf" + wkf.getCode() + "wkf";
    trackingAction = "action-method-wkf-track-" + wkfCode;
    trackingActionPreview = "action-method-wkf-track-" + workflow.getCode();

    if (!wkf.getIsJson() && wkf.getJsonField() == null) {
      simpleModelName = StringUtils.substringAfterLast(workflow.getModel(), ".");
      viewName = inflector.dasherize(simpleModelName) + "-form";
      actionName = "action-studio-" + inflector.dasherize(simpleModelName) + "-record-set-onnew";
    }
  }

  private void setJsonView() throws AxelorException {

    //    clearOldStatusField();

    MetaJsonField status = workflow.getStatusField();

    String jsonField = workflow.getIsJson() ? null : "$" + workflow.getJsonField();
    if (jsonField != null && jsonField.equals("$null")) {
      jsonField = null;
    }
    applyCondition =
        filterGroovyService.getGroovyFilters(workflow.getConditions(), jsonField, true, false);
    if (!Strings.isNullOrEmpty(applyCondition)) {
      applyCondition = applyCondition.replace("?.", ".");
    }

    MetaJsonField panel = getJsonField(wkfCode + "Panel", "panel");
    panel.setSequence(wkfSequence - 49);
    panel.setVisibleInGrid(false);
    panel.setIsWkf(true);
    panel.setWidgetAttrs("{\"colSpan\": \"12\"}");
    panel.setShowIf(null);
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

    MetaJsonField wkfEnd = getJsonField(wkfCode + "Separator", "separator");
    wkfEnd.setSequence(wkfSequence);
    wkfEnd.setVisibleInGrid(false);
    wkfEnd.setIsWkf(true);
    wkfEnd.setWidgetAttrs("{\"colSpan\": \"12\"}");
    saveJsonField(wkfEnd);

    //    setTrackOnSave(workflow, false);
  }

  private void setPreview() {
    String model = "com.axelor.studio.db.Wkf";
    if (findJsonField(wkfCode + workflow.getCode() + "Panel", model) == null) {

      MetaJsonField panel = getJsonField(wkfCode + "Panel", "panel");
      panel = jsonFieldRepo.copy(panel, true);
      panel.setName(wkfCode + workflow.getCode() + "Panel");
      createPreviewMetaJsonField(
          panel, 49, "$record.code == '" + workflow.getCode() + "' && $record.id != null");
    }

    String statusFieldName = getWkfFieldInfo(workflow)[0];
    if (findJsonField(wkfCode + statusFieldName, model) == null) {

      MetaJsonField status = null;
      if (workflow.getIsJson() || workflow.getJsonField() != null) {
        status = jsonFieldRepo.copy(workflow.getStatusField(), true);
      } else {
        status = new MetaJsonField();
        status.setType(workflow.getStatusMetaField().getTypeName().toLowerCase());
        status.setTitle(workflow.getStatusMetaField().getLabel());
        status.setDefaultValue("1");
        status.setSelection(getSelectName());
        status.setWidgetAttrs("{\"colSpan\": \"12\"}");
        if (workflow.getDisplayTypeSelect() == 0) {
          status.setWidget("NavSelect");
        }
        status.setReadonly(true);
      }
      status.setName(wkfCode + statusFieldName);
      createPreviewMetaJsonField(
          status, 48, "$record.code == '" + workflow.getCode() + "' && $record.id != null");
    }

    if (findJsonField(wkfCode + workflow.getCode() + "TrackFlow", model) == null) {
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

    if (findJsonField(wkfCode + workflow.getCode() + "Spacer", model) == null) {
      MetaJsonField wkfEnd = new MetaJsonField();
      wkfEnd.setName(wkfCode + workflow.getCode() + "Spacer");
      wkfEnd.setType("spacer");
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

  private void setMetaView(String defaultValue) {
    try {
      MetaView existingMetaView = workflow.getMetaView();

      ObjectViews formViews = XMLViews.fromXML(existingMetaView.getXml());
      FormView existingFormView = (FormView) formViews.getViews().get(0);
      String oldOnNew = existingFormView.getOnNew();

      String newOnNew = this.createAction(oldOnNew, defaultValue);

      FormView formView = existingFormView;
      formView.setExtends(addExtendItems(newOnNew));
      formView.setXmlId("studio-" + viewName);
      formView.setExtension(true);
      formView.setItems(null);
      formView.setMenubar(null);
      formView.setToolbar(null);

      MetaView newView =
          metaViewRepo
              .all()
              .filter(
                  "self.name = ?1 AND self.model = ?2 AND self.type = ?3 AND self.xmlId = ?4",
                  viewName,
                  workflow.getModel(),
                  "form",
                  "studio-" + viewName)
              .fetchOne();

      if (newView == null) {
        newView = new MetaView();
        newView.setName(viewName);
        newView.setType(formView.getType());
        newView.setTitle(formView.getTitle());
        newView.setModel(formView.getModel());
        newView.setModule("axelor-studio");
      }
      newView.setXmlId(formView.getXmlId());
      newView.setExtension(true);
      newView.setXml(XMLViews.toXml(formView, true));
      saveMetaView(newView);

    } catch (JAXBException e) {
      String message = I18n.get("Invalid XML.");
      Throwable ex = e.getLinkedException();
      if (ex != null) {
        message = ex.getMessage().replaceFirst("[^:]+\\:(.*)", "$1");
      }
      throw new IllegalArgumentException(message);
    }
  }

  @Transactional
  public String createAction(String oldOnNew, String defaultValue) {

    String newOnNew = null;
    if (Strings.isNullOrEmpty(oldOnNew)) {
      newOnNew = actionName;
    } else {
      newOnNew = oldOnNew + "," + actionName;
    }

    ActionRecord newActionRecord = new ActionRecord();
    newActionRecord.setName(actionName);
    newActionRecord.setModel(workflow.getModel());
    newActionRecord.setFields(new ArrayList<ActionRecord.RecordField>());
    RecordField recordField = new RecordField();
    recordField.setName(workflow.getStatusMetaField().getName());
    recordField.setExpression("eval: " + defaultValue);
    newActionRecord.getFields().add(recordField);

    MetaAction action =
        metaActionRepo
            .all()
            .filter(
                "self.name = ?1 AND self.model = ?2 AND self.type = ?3 AND self.module = ?4",
                actionName,
                simpleModelName,
                "action-record",
                "axelor-studio")
            .fetchOne();

    if (action == null) {
      action = new MetaAction();
      action.setName(actionName);
      action.setType("action-record");
      action.setModel(simpleModelName);
      action.setModule("axelor-studio");
    }
    action.setXml(XMLViews.toXml(newActionRecord, true));
    metaActionRepo.save(action);

    return newOnNew;
  }

  private List<Extend> addExtendItems(String onNew) {
    List<Extend> extendItems = new ArrayList<Extend>();
    try {
      MetaField statusField = workflow.getStatusMetaField();

      // extend for onNew action
      Extend extend1 = new Extend();
      extend1.setTarget("/");
      extend1.setAttributes(new ArrayList<ExtendItemAttribute>());

      ExtendItemAttribute attribute = new ExtendItemAttribute();
      attribute.setName("onNew");
      attribute.setValue(onNew);

      extend1.getAttributes().add(attribute);
      extendItems.add(extend1);

      // Extend for replace field
      Extend extend2 = new Extend();
      String target = "//field[@name='" + statusField.getName() + "']";
      extend2.setTarget(target);
      extend2.setReplaces(new ArrayList<ExtendItemReplace>());

      ExtendItemReplace replace = new ExtendItemReplace();
      replace.setItems(new ArrayList<AbstractWidget>());
      replace.getItems().add(this.createField());
      extend2.getReplaces().add(replace);

      extendItems.add(extend2);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    return extendItems;
  }

  private AbstractWidget createField() throws AxelorException {

    applyCondition =
        filterGroovyService.getGroovyFilters(workflow.getConditions(), "$attrs", false, true);
    if (!Strings.isNullOrEmpty(applyCondition)) {
      applyCondition = applyCondition.replace("?.", ".");
    }

    PanelField field = new PanelField();
    field.setName(workflow.getStatusMetaField().getName());
    field.setSelection(getSelectName());
    field.setReadonly(true);
    field.setColSpan(12);
    field.setWidget(null);
    if (workflow.getDisplayTypeSelect() == 0) {
      field.setWidget("NavSelect");
    }
    field.setShowIf(applyCondition);
    return field;
  }

  @Transactional
  public void clearOldStatusField() {

    MetaJsonField field = null;
    String modelField = workflow.getJsonField() != null ? workflow.getJsonField() : "attrs";
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
              .bind("workflowJsonField", modelField)
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
      String wkfFieldName = getWkfFieldInfo(workflow)[0];
      String selectName = "wkf." + inflector.dasherize(workflow.getName()).replace("_", ".");
      selectName += "." + inflector.dasherize(wkfFieldName).replace("_", ".") + ".select";

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

    metaService.updateMetaAction(name, "action-group", xml, null, name);

    return actionGroup;
  }

  public MetaJsonField getJsonField(String name, String type) {

    MetaJsonField field = null;
    String modelField = workflow.getJsonField() != null ? workflow.getJsonField() : "attrs";
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
              .bind("workflowJsonField", modelField)
              .bind("name", name)
              .bind("type", type)
              .fetchOne();

      log.debug(
          "Searching json field with model: {}, field: {}, name: {}, type: {}",
          workflow.getModel(),
          modelField,
          name,
          type);
      if (field == null) {
        field = new MetaJsonField();
        field.setModel(workflow.getModel());
        field.setModelField(modelField);
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

    workflow.setWkfModel(workflow.getModel());
    return jsonFieldRepo.save(jsonField);
  }

  public MetaJsonField findJsonField(String name, String model) {
    return jsonFieldRepo.all().filter("self.name = ?1 AND self.model = ?2", name, model).fetchOne();
  }

  @Transactional
  public MetaView saveMetaView(MetaView metaView) {
    metaView = metaViewRepo.save(metaView);
    Request request = new Request();
    request.setData(Mapper.toMap(metaView));
    resource.save(request);
    workflow.setGeneratedMetaView(metaView);
    return metaView;
  }

  @Transactional
  public void clearWkf(Wkf wkf) {

    initService(wkf);

    String actions =
        "action-" + wkfCode + ",action-group-" + wkfCode + ",action-group-" + workflow.getCode();
    actions = clearFields(actions, true);

    StringBuilder builder = new StringBuilder(actions);
    for (WkfNode node : wkf.getNodes()) {
      if (!node.getMetaActionSet().isEmpty()) {
        builder.append("," + nodeService.getActionName(node.getName()));
      }

      if (node.getMenuBuilder() != null && node.getMenuBuilder().getMetaMenu() != null) {
        Beans.get(MenuBuilderRepo.class).remove(node.getMenuBuilder());
        node.setMenuBuilder(null);
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

    if (workflow.getIsJson()) {
      MetaJsonField status = wkf.getStatusField();
      status.setWidget(null);
      status.setSelection(null);
      status.setWidgetAttrs(null);
      saveJsonField(status);

    } else if (workflow.getJsonField() != null) {
      jsonFieldRepo.remove(workflow.getStatusField());

    } else {
      clearMetaView();
    }

    //    setTrackOnSave(wkf, true);
  }

  public List<MetaJsonField> clearMetaJsonField() {
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

  @Transactional
  public void clearMetaView() {
    MetaView generatedView = workflow.getGeneratedMetaView();
    if (generatedView == null) {
      return;
    }

    metaViewRepo
        .all()
        .filter(
            "self.name = ?1 AND self.model = ?2 AND self.type = ?3 AND "
                + "self.priority = ?4 AND self.computed = 'true'",
            generatedView.getName(),
            generatedView.getModel(),
            "form",
            generatedView.getPriority() + 1)
        .remove();

    String model = StringUtils.substringAfterLast(generatedView.getModel(), ".");
    String actionName = "action-studio-" + inflector.dasherize(model) + "-record-set-onnew";

    metaActionRepo
        .all()
        .filter(
            "self.name = ?1 AND self.model = ?2 AND self.type = ?3 AND self.module = ?4",
            actionName,
            model,
            "action-record",
            "axelor-studio")
        .remove();

    workflow.setGeneratedMetaView(null);
    metaViewRepo.remove(generatedView);
  }

  @SuppressWarnings("unchecked")
  private String clearFields(String actions, boolean fromRemove) {

    List<MetaJsonField> fields = getFields(fromRemove);
    List<MetaJsonField> jsonFields = clearMetaJsonField();

    List<MetaJsonField> combinedList = ListUtils.union(fields, jsonFields);

    StringBuilder builder = new StringBuilder(actions);
    for (MetaJsonField field : combinedList) {
      if (field.getIsWkf() && !field.equals(workflow.getStatusField())) {
        if (field.getOnClick() != null) {
          builder.append("," + field.getOnClick());
        }
        jsonFieldRepo.remove(field);
      } else {
        jsonFieldRepo.remove(field);
      }
    }

    return builder.toString();
  }

  private List<MetaJsonField> getFields(boolean fromRemove) {

    List<MetaJsonField> fields;

    String modelField = workflow.getJsonField() != null ? workflow.getJsonField() : "attrs";
    String query = "self.isWkf = true and ";

    if (workflow.getIsJson()) {
      fields =
          jsonFieldRepo
              .all()
              .filter(
                  query + "self.name LIKE '" + wkfCode + "%' and self.jsonModel.name = ?1",
                  workflow.getModel())
              .fetch();
    } else {
      fields =
          jsonFieldRepo
              .all()
              .filter(
                  query + "self.name LIKE 'wkf%' and self.model = ?1 and self.modelField = ?2",
                  fromRemove
                      ? workflow.getModel()
                      : workflow.getGeneratedMetaView() != null
                          ? workflow.getGeneratedMetaView().getModel()
                          : workflow.getWkfModel(),
                  modelField)
              .fetch();
    }
    return fields;
  }

  @SuppressWarnings("unchecked")
  @Transactional
  public String clearOldButtons(List<String> skipList) {
    log.debug("Cleaning old buttons. Skip list: {}", skipList);
    if (CollectionUtils.isEmpty(skipList)) {
      return null;
    }

    skipList.add(wkfCode + "TrackFlow");

    ArrayList<String> actions = new ArrayList<>();
    List<MetaJsonField> fields = new ArrayList<MetaJsonField>();
    List<MetaJsonField> finalList = new ArrayList<MetaJsonField>();
    String modelField = workflow.getJsonField() != null ? workflow.getJsonField() : "attrs";

    if (workflow.getIsJson()) {
      fields =
          jsonFieldRepo
              .all()
              .filter(
                  "self.type = 'button' "
                      + "and self.jsonModel.name = ?1 "
                      + "and self.isWkf = true "
                      + "and self.name NOT IN (?2) "
                      + "and self.name LIKE ?3 ",
                  workflow.getModel(),
                  skipList,
                  wkfCode + "%")
              .fetch();
    } else {
      fields =
          jsonFieldRepo
              .all()
              .filter(
                  "self.type = 'button' "
                      + "and self.model = ?1 "
                      + "and self.modelField = ?2 "
                      + "and self.name NOT IN (?3) "
                      + "and self.name LIKE ?4",
                  workflow.getModel(),
                  modelField,
                  skipList,
                  wkfCode + "%")
              .fetch();
    }

    List<MetaJsonField> metaJsonFields =
        jsonFieldRepo
            .all()
            .filter(
                "self.type = 'button' "
                    + "and self.name LIKE :code "
                    + "and self.model = ?1 "
                    + "and self.modelField = ?2 "
                    + "and self.name NOT IN (?3)",
                "com.axelor.studio.db.Wkf",
                "attrs",
                skipList)
            .bind("code", wkfCode + "Transition%" + "Button")
            .fetch();

    finalList = ListUtils.union(fields, metaJsonFields);

    log.debug("Total Buttons to remove: {}", finalList.size());

    Iterator<MetaJsonField> buttons = finalList.iterator();

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
      jsonFieldRepo.remove(button);
    }

    return Joiner.on(",").join(actions);
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
              .bind("type", "spacer")
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
            .bind("type", "spacer")
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
    attribute.setFieldName("wkf" + workflow.getCode() + "wkf" + getWkfFieldInfo(workflow)[0]);
    attribute.setName("value");

    List<Attribute> attributes = new ArrayList<ActionAttrs.Attribute>();
    attributes.add(attribute);

    actionAttrs.setAttributes(attributes);

    String xml = XMLViews.toXml(actionAttrs, true);
    metaService.updateMetaAction(
        "action-wkf-attrs-set-status-field-value",
        "action-attrs",
        xml,
        null,
        "action-wkf-attrs-set-status-field-value");
  }

  public void clearNodes(Collection<WkfNode> nodes) {

    List<String> actions = new ArrayList<>();

    for (WkfNode node : nodes) {
      if (workflow == null) {
        workflow = node.getWkf();
      }
      if (!node.getMetaActionSet().isEmpty()) {
        actions.add(nodeService.getActionName(node.getName()));
      }

      if (node.getMenuBuilder() != null && node.getMenuBuilder().getMetaMenu() != null) {
        Beans.get(MenuBuilderRepo.class).remove(node.getMenuBuilder());
      }
    }

    String actionIds = Joiner.on(",").join(actions);
    metaService.removeMetaActions(actionIds);
  }

  public void createTrackFlowButton() {
    MetaJsonField trackFlow = getJsonField(wkfCode + "TrackFlow", "button");
    trackFlow.setSequence(wkfSequence - 47);
    trackFlow.setTitle("Track flow");
    trackFlow.setWidgetAttrs("{\"colSpan\": \"2\"}");
    trackFlow.setOnClick(WkfTrackingService.ACTION_OPEN_TRACK);
    trackFlow.setIsWkf(true);
    trackFlow.setVisibleInGrid(false);
    trackFlow.setHidden(!workflow.getIsTrackFlow());
    saveJsonField(trackFlow);
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

    metaService.updateMetaAction(trackingAction, "action-method", xml, null, trackingAction);
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

    metaService.updateMetaAction(
        trackingActionPreview, "action-method", xml, null, trackingActionPreview);
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

  public String[] getWkfFieldInfo(Wkf wkf) {
    String wkfFieldName = null;
    String wkfFieldType = null;
    if (wkf.getIsJson() || wkf.getJsonField() != null) {
      MetaJsonField statusField = wkf.getStatusField();
      wkfFieldName = statusField.getName();
      wkfFieldType = statusField.getType();
    } else {
      MetaField statusMetaField = wkf.getStatusMetaField();
      wkfFieldName = statusMetaField.getName();
      wkfFieldType = statusMetaField.getTypeName();
    }
    return new String[] {wkfFieldName, wkfFieldType};
  }

  public void manageMenuBuilder(WkfNode node) {

    if (node.getIsGenerateMenu()) {

      Wkf wkf = node.getWkf();
      MenuBuilder menuBuilder = node.getMenuBuilder();
      if (menuBuilder != null) {

        String menuName =
            String.format("%s-%s-%s", wkf.getModel(), node.getName(), (int) (Math.random() * 100));
        Boolean isJson = wkf.getIsJson();
        String domain = getDomain(wkf, node.getSequence());
        menuBuilder =
            Beans.get(MenuBuilderService.class)
                .updateMenuBuilder(
                    menuBuilder,
                    wkf.getModel(),
                    menuName,
                    wkf.getAppBuilder(),
                    isJson ? MetaJsonRecord.class.getName() : wkf.getModel(),
                    wkf.getIsJson(),
                    domain);
      }
    }
  }

  private String getDomain(Wkf wkf, Integer fieldValue) {

    if (wkf.getIsJson()) {
      return String.format(
          "json_extract_integer(self.attrs, '%s') = %s",
          wkf.getStatusField().getName(), fieldValue);
    } else {
      return String.format("self.%s = %s", wkf.getStatusMetaField().getName(), fieldValue);
    }
  }
}
