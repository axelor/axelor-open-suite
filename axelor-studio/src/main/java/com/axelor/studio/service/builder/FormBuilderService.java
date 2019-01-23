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
package com.axelor.studio.service.builder;

import com.axelor.common.Inflector;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.views.AbstractWidget;
import com.axelor.meta.schema.views.Button;
import com.axelor.meta.schema.views.Extend;
import com.axelor.meta.schema.views.ExtendItemInsert;
import com.axelor.meta.schema.views.ExtendItemReplace;
import com.axelor.meta.schema.views.Field;
import com.axelor.meta.schema.views.FormView;
import com.axelor.meta.schema.views.Panel;
import com.axelor.meta.schema.views.PanelField;
import com.axelor.meta.schema.views.PanelMail;
import com.axelor.meta.schema.views.PanelTabs;
import com.axelor.meta.schema.views.Position;
import com.axelor.meta.schema.views.Separator;
import com.axelor.meta.schema.views.SimpleWidget;
import com.axelor.studio.service.CommonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class FormBuilderService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final List<String> PARENT_NODE =
      Arrays.asList(new String[] {"form", "panel-tabs"});

  private PanelTabs panelTabs = null;

  private Panel panel = null;

  private List<Extend> extendList = null;

  private Extend extend = null;

  private ExtendItemInsert insert = null;

  private List<ExtendItemInsert> insertList = null;

  private List<AbstractWidget> insertItemList = null;

  private ExtendItemReplace replace = null;

  private List<ExtendItemReplace> replaceList = null;

  private List<AbstractWidget> replaceItemList = null;

  private Document xmlDocument = null;

  private XPath xPath = null;

  @Inject private ViewBuilderService viewBuilderService;

  @Inject private MetaJsonFieldRepository metaJsonFieldRepo;

  @Inject private ModelBuilderService modelBuilderService;

  @Inject private MetaViewRepository metaViewRepo;

  @Inject private MetaFieldRepository metaFieldRepo;

  public FormView build(MetaJsonModel jsonModel, String module) throws AxelorException {

    if (jsonModel == null || module == null || jsonModel.getFields().isEmpty()) {
      return null;
    }

    String model = modelBuilderService.getModelFullName(module, jsonModel.getName());
    String name = viewBuilderService.getDefaultViewName("form", jsonModel.getName());

    FormView formView = new FormView();
    formView.setModel(model);
    formView.setName(name);
    formView.setTitle(jsonModel.getTitle());
    formView.setOnNew(jsonModel.getOnNew());
    formView.setOnSave(jsonModel.getOnSave());
    formView.setXmlId(module + "-" + name);
    List<AbstractWidget> items = createItems(jsonModel.getFields());
    formView.setItems(items);

    return formView;
  }

  public FormView build(MetaModel metaModel, String module) throws AxelorException {

    if (metaModel == null) {
      return null;
    }

    List<MetaJsonField> fields =
        metaJsonFieldRepo.all().filter("self.model = ?1", metaModel.getFullName()).fetch();

    if (fields.isEmpty()) {
      return null;
    }

    String name = viewBuilderService.getDefaultViewName("form", metaModel.getName());

    FormView formView = new FormView();
    formView.setModel(metaModel.getFullName());
    formView.setName(name);
    formView.setTitle(viewBuilderService.getViewTitle(name));
    formView.setExtension(true);
    formView.setXmlId(module + "-" + name);
    List<AbstractWidget> items = createItems(fields);
    formView.setItems(items);

    return formView;
  }

  public FormView buildFromExcel(
      String module, String model, List<Map<String, String>> fieldValues) {

    FormView formView = null;
    String view = fieldValues.get(0).get(CommonService.VIEW);
    String type = view.substring(0, view.indexOf("("));
    view = view.substring(view.indexOf("(") + 1, view.indexOf(")"));

    try {
      MetaView metaView =
          metaViewRepo
              .all()
              .filter("self.name = ?1 and type = ?2 and xmlId is null", view, type)
              .fetchOne();

      if (metaView != null) {
        xPath = XPathFactory.newInstance().newXPath();
        xmlDocument = XMLViews.parseXml(metaView.getXml());
        ObjectViews objectViews = XMLViews.fromXML(metaView.getXml());
        formView = (FormView) objectViews.getViews().get(0);
        addItemsFromExcel(model, formView.getItems(), fieldValues);
        formView = new FormView();
        formView.setModel(metaView.getModel());
        formView.setName(metaView.getName());
        formView.setTitle(metaView.getTitle());
        formView.setXmlId(module + "-" + view);
        formView.setExtension(true);
        formView.setExtends(extendList);

      } else {
        formView = new FormView();
        formView.setModel(modelBuilderService.getModelFullName(module, model));
        formView.setName(view);
        formView.setTitle(Inflector.getInstance().humanize(model));
        List<AbstractWidget> items = createItemsFromExcel(formView, fieldValues);
        formView.setItems(items);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return formView;
  }

  private void addItemsFromExcel(
      String model, List<AbstractWidget> items, List<Map<String, String>> fieldValues) {

    AbstractWidget panelMail =
        items.stream().filter(item -> (item instanceof PanelMail)).findFirst().orElse(null);

    initialize();
    for (Map<String, String> valMap : fieldValues) {

      if (valMap.get(CommonService.TYPE).equals("onnew")
          || valMap.get(CommonService.TYPE).equals("onsave")) {
        continue;
      }

      MetaField metaField =
          metaFieldRepo
              .all()
              .filter(
                  "self.name = ?1 and self.metaModel.name = ?2",
                  valMap.get(CommonService.NAME),
                  model)
              .fetchOne();
      if (metaField != null) {
        continue;
      }

      String position = valMap.get(CommonService.POSITION);
      if (!Strings.isNullOrEmpty(position)) {
        String positionType = position.substring(0, position.indexOf("("));
        String searchField = position.substring(position.indexOf("(") + 1, position.indexOf(")"));

        try {
          addItemsOnPosition(positionType, valMap, searchField);
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        addItems(panelMail, valMap);
      }
    }
    if (insert != null) {
      insert.setItems(insertItemList);
      insertList.add(insert);
      extend.setInserts(insertList);
    }
    if (replace != null) {
      replace.setItems(replaceItemList);
      replaceList.add(replace);
      extend.setReplaces(replaceList);
    }
    extendList.add(extend);
  }

  private void addItemsOnPosition(
      String positionType, Map<String, String> valMap, String searchField)
      throws XPathExpressionException {

    // XPath to parse the XML
    String target = "//*[@name='" + searchField + "']";
    Node searchNode = (Node) xPath.evaluate(target, xmlDocument, XPathConstants.NODE);

    HashMap<String, Object> widgetAttrs = getWidgetAttrs(valMap);

    if (valMap.get(CommonService.TYPE).equals("panel")) {
      Boolean tab = isTabPanel(widgetAttrs);
      panel = createPanel();
      initializeExtend(target, positionType);
      processCommon(panel, valMap, widgetAttrs);
      if (tab) {
        if (panelTabs == null) {
          panelTabs = new PanelTabs();
          panelTabs.setItems(new ArrayList<>());
          addingItemsOnPositionType(positionType, panelTabs);
        }
        panelTabs.getItems().add(panel);
      } else {
        addingItemsOnPositionType(positionType, panel);
      }
    } else if (searchNode.getParentNode() != null
        && !PARENT_NODE.contains(searchNode.getParentNode().getNodeName())) {

      SimpleWidget item = null;
      if (!valMap.get(CommonService.TYPE).equals("button")
          || !valMap.get(CommonService.TYPE).equals("separator")) {

        item = createSimpleFieldItem(valMap);
      } else {
        item = createSimpleItem(valMap);
      }
      if (item != null) {
        processCommon(item, valMap, widgetAttrs);
        addExtendOnPosition(item, positionType, target);
      }
    } else {
      Panel topPanel = createPanel();
      setDefaultPanel(topPanel, valMap, widgetAttrs);
      addExtendOnPosition(topPanel, positionType, target);
    }
  }

  private void initializeExtend(String target, String positionType) {
    extend = new Extend();
    extend.setTarget(target);
    if (positionType.equals("replace")) {
      replace = new ExtendItemReplace();
    } else {
      insert = new ExtendItemInsert();
    }
  }

  private void addingItemsOnPositionType(String positionType, SimpleWidget widget) {
    if (positionType.equals("replace")) {
      replaceItemList.add(widget);
    } else {
      if (positionType.equals("after")) {
        insert.setPosition(Position.AFTER);
      } else if (positionType.equals("before")) {
        insert.setPosition(Position.BEFORE);
      }
      insertItemList.add(widget);
    }
  }

  private void addExtendOnPosition(SimpleWidget widget, String positionType, String target) {
    Extend extend = new Extend();
    extend.setTarget(target);
    List<AbstractWidget> itemList = new ArrayList<>();
    itemList.add(widget);

    if (positionType.equals("replace")) {
      ExtendItemReplace replace = new ExtendItemReplace();
      replace.setItems(itemList);
      List<ExtendItemReplace> replaceList = new ArrayList<>();
      replaceList.add(replace);
      extend.setReplaces(replaceList);

    } else {
      ExtendItemInsert insert = new ExtendItemInsert();
      if (positionType.equals("after")) {
        insert.setPosition(Position.AFTER);
      } else if (positionType.equals("before")) {
        insert.setPosition(Position.BEFORE);
      }
      insert.setItems(itemList);
      List<ExtendItemInsert> insertList = new ArrayList<>();
      insertList.add(insert);
      extend.setInserts(insertList);
    }
    extendList.add(extend);
  }

  private void addItems(AbstractWidget panelMail, Map<String, String> valMap) {

    HashMap<String, Object> widgetAttrs = getWidgetAttrs(valMap);

    if (valMap.get(CommonService.TYPE).equals("panel")) {
      Boolean tab = isTabPanel(widgetAttrs);
      panel = createPanel();
      extend = new Extend();
      insert = new ExtendItemInsert();
      processCommon(panel, valMap, widgetAttrs);
      if (tab) {
        if (panelTabs == null) {
          panelTabs = new PanelTabs();
          panelTabs.setItems(new ArrayList<>());
          addingItemsOnPanelMail(panelMail, panelTabs);
        }
        panelTabs.getItems().add(panel);
      } else {
        addingItemsOnPanelMail(panelMail, panel);
      }
    } else if (panel != null) {
      SimpleWidget item = createSimpleItem(valMap);
      if (item != null) {
        processCommon(item, valMap, widgetAttrs);
        panel.getItems().add(item);
      }
    } else {
      panel = createPanel();
      extend = new Extend();
      insert = new ExtendItemInsert();
      setDefaultPanel(null, valMap, widgetAttrs);
      addingItemsOnPanelMail(panelMail, panel);
    }
  }

  private void addingItemsOnPanelMail(AbstractWidget panelMail, SimpleWidget widget) {
    if (panelMail == null) {
      extend.setTarget("/");
      insert.setPosition(Position.AFTER);
    } else {
      extend.setTarget("panel-mail");
      insert.setPosition(Position.BEFORE);
    }
    insertItemList.add(widget);
  }

  private void initialize() {
    panel = null;
    panelTabs = null;
    extendList = new ArrayList<>();
    insertItemList = new ArrayList<>();
    insertList = new ArrayList<>();
    replaceItemList = new ArrayList<>();
    replaceList = new ArrayList<>();
  }

  private List<AbstractWidget> createItemsFromExcel(
      FormView formView, List<Map<String, String>> fieldValues) {

    List<AbstractWidget> items = new ArrayList<>();
    PanelTabs panelTabs = null;
    Panel panel = null;

    for (Map<String, String> valMap : fieldValues) {

      if (valMap.get(CommonService.TYPE).equals("panel")
          && valMap.get(CommonService.NAME).endsWith("(end)")) {
        continue;
      }

      if (valMap.get(CommonService.TYPE).equals("onnew")
          && valMap.get(CommonService.FORMULA) != null) {
        formView.setOnNew(valMap.get(CommonService.FORMULA));
        continue;
      }

      if (valMap.get(CommonService.TYPE).equals("onsave")
          && valMap.get(CommonService.FORMULA) != null) {
        formView.setOnSave(valMap.get(CommonService.FORMULA));
        continue;
      }

      HashMap<String, Object> widgetAttrs = getWidgetAttrs(valMap);

      if (valMap.get(CommonService.TYPE).equals("panel")) {
        Boolean tab = isTabPanel(widgetAttrs);
        panel = createPanel();
        processCommon(panel, valMap, widgetAttrs);
        if (tab) {
          if (panelTabs == null) {
            panelTabs = new PanelTabs();
            panelTabs.setItems(new ArrayList<>());
            items.add(panelTabs);
          }
          panelTabs.getItems().add(panel);
        } else {
          items.add(panel);
        }
      } else if (panel != null) {
        SimpleWidget item = createSimpleItem(valMap);
        if (item != null) {
          processCommon(item, valMap, widgetAttrs);
          panel.getItems().add(item);
        }
      } else {
        panel = createPanel();
        setDefaultPanel(panel, valMap, widgetAttrs);
        items.add(panel);
      }
    }
    return items;
  }

  private List<AbstractWidget> createItems(List<MetaJsonField> fields) {

    List<AbstractWidget> items = new ArrayList<>();

    PanelTabs panelTabs = null;
    Panel panel = null;
    modelBuilderService.sortJsonFields(fields);

    for (MetaJsonField field : fields) {
      if (field.getIsWkf()) {
        continue;
      }
      HashMap<String, Object> widgetAttrs = getWidgetAttrs(field);

      if (field.getType().equals("panel")) {
        Boolean tab = isTabPanel(widgetAttrs);
        panel = createPanel();
        processCommon(panel, field, widgetAttrs);
        if (tab) {
          if (panelTabs == null) {
            panelTabs = new PanelTabs();
            panelTabs.setItems(new ArrayList<>());
            items.add(panelTabs);
          }
          panelTabs.getItems().add(panel);
        } else {
          items.add(panel);
        }
      } else if (panel != null) {
        SimpleWidget item = createSimpleItem(field);
        if (item != null) {
          processCommon(item, field, widgetAttrs);
          panel.getItems().add(item);
        }
      } else {
        log.debug("Panel null for field: {}", field.getName());
      }
    }

    return items;
  }

  private Boolean isTabPanel(HashMap<String, Object> widgetAttrs) {

    Boolean tab = false;
    if (widgetAttrs.get("tab") != null) {
      tab = Boolean.parseBoolean(widgetAttrs.get("tab").toString());
      widgetAttrs.remove("tab");
    }
    return tab;
  }

  private SimpleWidget createSimpleItem(MetaJsonField field) {

    SimpleWidget item = null;
    switch (field.getType()) {
      case "button":
        item = createButton(field);
        break;
      case "separator":
        item = createSeparator();
        break;
      default:
        item = createField(field);
    }

    return item;
  }

  private SimpleWidget createSimpleItem(Map<String, String> valMap) {

    SimpleWidget item = null;
    switch (valMap.get(CommonService.TYPE)) {
      case "button":
        item = createButton(valMap);
        break;
      case "separator":
        item = createSeparator();
        break;
      default:
        item = createField(valMap);
    }

    return item;
  }

  @SuppressWarnings("unchecked")
  private HashMap<String, Object> getWidgetAttrs(MetaJsonField field) {

    HashMap<String, Object> widgetAttrs = new HashMap<>();

    if (field.getWidgetAttrs() != null) {
      try {
        widgetAttrs = new ObjectMapper().readValue(field.getWidgetAttrs(), HashMap.class);
      } catch (IOException e) {
      }
    }

    return widgetAttrs;
  }

  @SuppressWarnings("unchecked")
  private HashMap<String, Object> getWidgetAttrs(Map<String, String> valMap) {

    HashMap<String, Object> widgetAttrs = new HashMap<>();

    if (valMap.get(CommonService.WIDGET_ATTRS) != null) {
      try {
        widgetAttrs =
            new ObjectMapper().readValue(valMap.get(CommonService.WIDGET_ATTRS), HashMap.class);
      } catch (IOException e) {
      }
    }

    return widgetAttrs;
  }

  private void processCommon(
      SimpleWidget item, MetaJsonField field, HashMap<String, Object> widgetAttrs) {

    item.setName(field.getName());
    if (!(item instanceof PanelField)) {
      item.setTitle(field.getTitle());
    }

    if (field.getReadonly()) {
      item.setReadonly(field.getReadonly());
    }
    if (field.getReadonlyIf() != null) {
      item.setReadonlyIf(field.getReadonlyIf());
    }
    if (field.getHidden()) {
      item.setHidden(field.getHidden());
    }
    item.setHideIf(field.getShowIf());

    if (widgetAttrs.containsKey("colSpan")) {
      Integer colSpan = Integer.parseInt(widgetAttrs.get("colSpan").toString());
      if (colSpan != 6) {
        item.setColSpan(colSpan);
      }
      widgetAttrs.remove("colSpan");
    }

    Map<QName, String> otherAttrs = new HashMap<>();
    for (String key : widgetAttrs.keySet()) {
      if (key.equals("col") || key.equals("width") || key.equals("tab")) {
        continue;
      }
      if (key.equals("showTitle") && widgetAttrs.get("showTitle").equals("true")) {
        continue;
      }
      otherAttrs.put(new QName(key), widgetAttrs.get(key).toString());
    }
    item.setOtherAttributes(otherAttrs);
  }

  private void processCommon(
      SimpleWidget item, Map<String, String> valMap, HashMap<String, Object> widgetAttrs) {

    String name = valMap.get(CommonService.NAME);
    if (valMap.get(CommonService.TYPE).equals("panel")) {
      name = name.replace("(start)", "");
    }
    item.setName(name);
    if (!(item instanceof PanelField)) {
      item.setTitle(valMap.get(CommonService.TITLE));
    }

    if (valMap.get(CommonService.READONLY) != null
        && valMap.get(CommonService.READONLY).equals("x")) {
      item.setReadonly(true);
    }
    if (valMap.get(CommonService.READONLY) != null
        && !valMap.get(CommonService.READONLY).equals("x")) {
      item.setReadonlyIf(valMap.get(CommonService.READONLY));
    }
    if (valMap.get(CommonService.HIDDEN) != null && valMap.get(CommonService.HIDDEN).equals("x")) {
      item.setHidden(true);
    }
    if (valMap.get(CommonService.HIDDEN) != null && !valMap.get(CommonService.HIDDEN).equals("x")) {
      item.setHideIf(valMap.get(CommonService.HIDDEN));
    }
    item.setShowIf(valMap.get(CommonService.SHOW_IF));

    if (widgetAttrs.containsKey("colSpan")) {
      Integer colSpan = Integer.parseInt(widgetAttrs.get("colSpan").toString());
      if (colSpan != 6) {
        item.setColSpan(colSpan);
      }
      widgetAttrs.remove("colSpan");
    }

    Map<QName, String> otherAttrs = new HashMap<>();
    for (String key : widgetAttrs.keySet()) {
      if (key.equals("col") || key.equals("width") || key.equals("tab")) {
        continue;
      }
      if (key.equals("showTitle") && widgetAttrs.get("showTitle").equals("true")) {
        continue;
      }
      otherAttrs.put(new QName(key), widgetAttrs.get(key).toString());
    }
    item.setOtherAttributes(otherAttrs);
  }

  private SimpleWidget createButton(MetaJsonField field) {
    Button button = new Button();
    button.setOnClick(field.getOnClick());

    return button;
  }

  private SimpleWidget createButton(Map<String, String> valMap) {
    Button button = new Button();
    button.setOnClick(valMap.get(CommonService.ON_CLICK));

    return button;
  }

  private SimpleWidget createSeparator() {
    return new Separator();
  }

  private Panel createPanel() {
    Panel panel = new Panel();
    panel.setItems(new ArrayList<>());

    return panel;
  }

  private void setDefaultPanel(
      Panel topPanel, Map<String, String> valMap, HashMap<String, Object> widgetAttrs) {

    SimpleWidget item = createSimpleItem(valMap);
    if (item != null) {
      processCommon(item, valMap, widgetAttrs);
      if (topPanel == null) {
        panel.setTitle("Extra panel");
        panel.getItems().add(item);
      } else {
        topPanel.setTitle("Overview");
        topPanel.getItems().add(item);
      }
    }
  }

  private SimpleWidget createField(MetaJsonField jsonField) {

    PanelField field = new PanelField();
    field.setWidget(jsonField.getWidget());
    field.setOnChange(jsonField.getOnChange());
    if (jsonField.getRequired()) {
      field.setRequired(jsonField.getRequired());
    }
    field.setRequiredIf(jsonField.getRequiredIf());

    return field;
  }

  private SimpleWidget createField(Map<String, String> valMap) {

    PanelField field = new PanelField();
    field.setWidget(valMap.get(CommonService.WIDGET));
    field.setOnChange(valMap.get(CommonService.ON_CHANGE));
    if (valMap.get(CommonService.REQUIRED) != null
        && valMap.get(CommonService.REQUIRED).equals("x")) {
      field.setRequired(true);
    }
    if (valMap.get(CommonService.REQUIRED) != null
        && !valMap.get(CommonService.REQUIRED).equals("x")) {
      field.setRequiredIf(valMap.get(CommonService.REQUIRED));
    }

    return field;
  }

  private SimpleWidget createSimpleFieldItem(Map<String, String> valMap) {

    Field field = new Field();
    field.setWidget(valMap.get(CommonService.WIDGET));
    field.setOnChange(valMap.get(CommonService.ON_CHANGE));
    if (valMap.get(CommonService.REQUIRED) != null
        && valMap.get(CommonService.REQUIRED).equals("x")) {
      field.setRequired(true);
    }
    if (valMap.get(CommonService.REQUIRED) != null
        && !valMap.get(CommonService.REQUIRED).equals("x")) {
      field.setRequiredIf(valMap.get(CommonService.REQUIRED));
    }

    return field;
  }
}
