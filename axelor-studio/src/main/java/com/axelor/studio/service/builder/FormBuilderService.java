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

import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.schema.views.AbstractWidget;
import com.axelor.meta.schema.views.Button;
import com.axelor.meta.schema.views.FormView;
import com.axelor.meta.schema.views.Panel;
import com.axelor.meta.schema.views.PanelField;
import com.axelor.meta.schema.views.PanelTabs;
import com.axelor.meta.schema.views.Separator;
import com.axelor.meta.schema.views.SimpleWidget;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormBuilderService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private ViewBuilderService viewBuilderService;

  @Inject private MetaJsonFieldRepository metaJsonFieldRepo;

  @Inject private ModelBuilderService modelBuilderService;

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
        panel = createPanel(field, (Boolean) widgetAttrs.get("tab"));
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
        item = createSeparator(field);
        break;
      default:
        item = createField(field);
    }

    return item;
  }

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
      item.setColSpan(Integer.parseInt(widgetAttrs.get("colSpan").toString()));
      widgetAttrs.remove("colSpan");
    }

    Map<QName, String> otherAttrs = new HashMap<>();
    for (String key : widgetAttrs.keySet()) {
      if (key.equals("col") || key.equals("width")) {
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

  private SimpleWidget createSeparator(MetaJsonField field) {
    return new Separator();
  }

  private Panel createPanel(MetaJsonField field, Boolean tab) {

    Panel panel = new Panel();
    panel.setItems(new ArrayList<>());

    return panel;
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
}
