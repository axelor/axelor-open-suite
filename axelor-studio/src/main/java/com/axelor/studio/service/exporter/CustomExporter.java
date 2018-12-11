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
package com.axelor.studio.service.exporter;

import com.axelor.db.mapper.Mapper;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.Context;
import com.axelor.meta.schema.actions.ActionView.View;
import com.axelor.meta.schema.views.FormView;
import com.axelor.meta.schema.views.Selection.Option;
import com.axelor.studio.service.CommonService;
import com.axelor.studio.service.TranslationService;
import com.axelor.studio.service.ViewLoaderService;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomExporter {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final List<String> SUPPORTED_TYPES =
      Arrays.asList(new String[] {"form", "dashboard", "grid"});

  private static final List<String> RELATIONAL_CUSTOM_FIELD_TYPES =
      Arrays.asList(new String[] {"json-many-to-one", "json-one-to-many", "json-many-to-many"});

  private static final List<String> RELATIONAL_NORMAL_FIELD_TYPES =
      Arrays.asList(new String[] {"many-to-one", "one-to-many", "many-to-many"});

  private boolean newForm = false;

  @Inject private ModelExporter modelExporter;

  @Inject private MetaJsonModelRepository metaJsonModelRepo;

  @Inject private MetaJsonFieldRepository metaJsonFieldRepo;

  @Inject private TranslationService translationService;

  private ExporterService exporterService;

  public void customExport(ExporterService exporterService, MetaAction action) {

    this.exporterService = exporterService;

    Map<String, String> views = new HashMap<String, String>();
    String jsonModel = null;

    try {

      ObjectViews objectViews = XMLViews.fromXML(action.getXml());
      ActionView actionView = (ActionView) objectViews.getActions().get(0);
      jsonModel = getContextJsonModel(actionView);

      for (View view : actionView.getViews()) {
        String type = view.getType();
        if (SUPPORTED_TYPES.contains(type)) {
          views.put(type, view.getName());
        }
      }

    } catch (JAXBException e) {
      e.printStackTrace();
    }

    processCustomModel(action.getModel(), jsonModel, views);
  }

  @SuppressWarnings("unchecked")
  private String getContextJsonModel(ActionView actionView) {
    String jsonModel = null;

    try {

      Mapper mapper = Mapper.of(ActionView.class);
      Field field = mapper.getBeanClass().getDeclaredField("contexts");
      field.setAccessible(true);
      List<ActionView.Context> contextList = (List<Context>) field.get(actionView);
      if (contextList != null) {
        jsonModel =
            contextList
                .stream()
                .filter(context -> context.getName().equals("jsonModel"))
                .findFirst()
                .get()
                .getExpression();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    return StringUtils.substringBetween(jsonModel, "'", "'");
  }

  private void processCustomModel(String model, String jsonModel, Map<String, String> views) {
    try {

      if (model != null) {
        String name = views.get("form");
        if (exporterService.isViewProcessed(name)) {
          return;
        }

        MetaView formView = modelExporter.getMetaView(model, "form", views.get("form"));
        if (formView == null || exporterService.isViewProcessed(formView.getName())) {
          log.debug("Form view not considered: {}", formView);
          return;
        }

        MetaView grid = modelExporter.getMetaView(model, "grid", views.get("grid"));

        customFormexport(exporterService, formView, getGridFields(grid, jsonModel), jsonModel);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private List<String> getGridFields(MetaView view, String jsonModel) {

    List<String> fields = new ArrayList<String>();

    if (view == null) {
      return fields;
    }

    List<MetaJsonField> gridFields =
        metaJsonFieldRepo
            .all()
            .filter(
                "self.jsonModel.id = ?1 and self.visibleInGrid = true",
                metaJsonModelRepo.findByName(jsonModel).getId())
            .fetch();

    String name = view.getName();
    String defaultName = ViewLoaderService.getDefaultViewName(view.getModel(), "grid");

    if (name.equals(defaultName)) {
      fields.add("x");
    } else {
      fields.add(name);
    }

    for (MetaJsonField field : gridFields) {
      fields.add(field.getName());
    }

    return fields;
  }

  public void customFormexport(
      ExporterService exporterService, MetaView formView, List<String> grid, String jsonModel)
      throws JAXBException, ClassNotFoundException {

    this.exporterService = exporterService;

    Object[] extra = new Object[] {false, grid, null, null};
    Mapper mapper = Mapper.of(Class.forName(formView.getModel()));
    ObjectViews objectViews = XMLViews.fromXML(formView.getXml());

    FormView form = (FormView) objectViews.getViews().get(0);
    newForm = true;
    String simpleName = mapper.getBeanClass().getSimpleName();
    String viewName = form.getName() + "(" + form.getTitle() + ")";

    processCustomForm(form, simpleName, jsonModel, viewName, extra);
  }

  private void processCustomForm(
      FormView form, String model, String jsonModel, String view, Object[] extra) {

    log.debug("Processing form: {}", view);

    model = model + "(MetaJsonModel)";

    processItems(model, jsonModel, view, extra);

    exporterService.addViewProcessed(form.getName());
  }

  private void processItems(String model, String jsonModel, String view, Object[] extra) {

    List<MetaJsonField> customFields =
        metaJsonFieldRepo
            .all()
            .filter("self.jsonModel.id = ?", metaJsonModelRepo.findByName(jsonModel).getId())
            .order("sequence")
            .fetch();

    for (MetaJsonField field : customFields) {
      if (field.getType().equals("panel")) {
        processPanel(field, model, view);
        continue;
      }
      processField(field, model, view, extra);
    }
  }

  private void processPanel(MetaJsonField panel, String model, String view) {

    Map<String, String> valMap = new HashMap<>();
    valMap.put(CommonService.MODEL, model);
    valMap.put(CommonService.VIEW, view);
    valMap.put(CommonService.NAME, panel.getName());
    valMap.put(CommonService.TITLE, panel.getTitle());
    valMap.put(CommonService.TITLE_FR, translationService.getTranslation(panel.getTitle(), "fr"));
    valMap.put(CommonService.TYPE, panel.getType());
    if (panel.getReadonly() != null && panel.getReadonly()) {
      valMap.put(CommonService.READONLY, "x");
    } else {
      valMap.put(CommonService.READONLY, panel.getReadonlyIf());
    }

    if (panel.getHidden() != null && panel.getHidden()) {
      valMap.put(CommonService.HIDDEN, "x");
    } else {
      valMap.put(CommonService.HIDDEN, panel.getHideIf());
    }

    valMap.put(CommonService.SHOW_IF, panel.getShowIf());

    if (panel.getOnChange() != null) {
      valMap.put(CommonService.ON_CHANGE, panel.getOnChange());
    }

    String panelLevel = String.valueOf(0);
    valMap.put(CommonService.PANEL_LEVEL, panelLevel);

    exporterService.writeRow(valMap, newForm);

    newForm = false;
  }

  private void processField(MetaJsonField field, String model, String view, Object[] extra) {

    Map<String, String> valMap = new HashMap<>();
    valMap.put(CommonService.MODEL, model);
    valMap.put(CommonService.VIEW, view);
    valMap.put(CommonService.NAME, field.getName());
    valMap.put(CommonService.TITLE, field.getTitle());
    valMap.put(CommonService.TITLE_FR, translationService.getTranslation(field.getTitle(), "fr"));

    String target = null;
    if (RELATIONAL_CUSTOM_FIELD_TYPES.contains(field.getType())) {
      target = field.getTargetJsonModel().getName();

      valMap.put(CommonService.TYPE, field.getType() + "(" + target + ")");

    } else if (RELATIONAL_NORMAL_FIELD_TYPES.contains(field.getType())) {
      target = field.getTargetModel().substring(field.getTargetModel().lastIndexOf('.') + 1);

      valMap.put(CommonService.TYPE, field.getType() + "(" + target + ")");

    } else {
      valMap.put(CommonService.TYPE, field.getType());
    }

    if (field.getSelection() != null) {
      valMap.put(CommonService.SELECT, field.getSelection());
    }

    if (!Strings.isNullOrEmpty(valMap.get(CommonService.SELECT))) {
      String[] selects = getSelect(valMap.get(CommonService.SELECT));
      if (selects != null) {
        valMap.put(CommonService.SELECT, selects[0]);
        valMap.put(CommonService.SELECT_FR, selects[1]);
      }
    }

    addExtraAttributes(field, valMap);

    @SuppressWarnings("unchecked")
    List<String> grid = (List<String>) extra[1];
    if (grid.contains(field.getName())) {
      valMap.put(CommonService.GRID, grid.get(0));
    }

    exporterService.writeRow(valMap, newForm);

    newForm = false;
  }

  private String[] getSelect(String selection) {

    List<Option> selectionList = MetaStore.getSelectionList(selection);
    if (selectionList == null) {
      log.debug("Blank selection list for selection: {}", selection);
      return null;
    }

    List<String> select = new ArrayList<String>();
    List<String> selectFR = new ArrayList<String>();
    for (Option option : selectionList) {
      select.add(option.getValue() + ":" + option.getTitle());
      String translation = translationService.getTranslation(option.getTitle(), "fr");
      if (translation != null) {
        selectFR.add(option.getValue() + ":" + translation);
      }
    }

    String selectionEN = selection + "(" + Joiner.on(",").join(select) + ")";
    String selectionFR = null;
    if (selectFR != null) {
      selectionFR = selection + "(" + Joiner.on(",").join(selectFR) + ")";
    }

    return new String[] {selectionEN, selectionFR};
  }

  private void addExtraAttributes(MetaJsonField field, Map<String, String> valMap) {

    if (field.getRequired() != null && field.getRequired()) {
      valMap.put(CommonService.REQUIRED, "x");
    } else if (valMap.get(CommonService.REQUIRED) == null) {
      valMap.put(CommonService.REQUIRED, field.getRequiredIf());
    }

    if (field.getReadonly() != null && field.getReadonly()) {
      valMap.put(CommonService.READONLY, "x");
    } else if (valMap.get(CommonService.READONLY) == null) {
      valMap.put(CommonService.READONLY, field.getReadonlyIf());
    }

    if (field.getHidden() != null && field.getHidden()) {
      valMap.put(CommonService.HIDDEN, "x");
    } else if (valMap.get(CommonService.HIDDEN) == null) {
      valMap.put(CommonService.HIDDEN, field.getHideIf());
    }

    valMap.put(CommonService.SHOW_IF, field.getShowIf());

    if (field.getDomain() != null) {
      valMap.put(CommonService.DOMAIN, field.getDomain());
    }

    if (field.getOnChange() != null) {
      valMap.put(CommonService.ON_CHANGE, field.getOnChange());
    }

    if (field.getHelp() != null) {
      String help = field.getHelp();
      if (!Boolean.parseBoolean(help)) {
        valMap.put(CommonService.DOC, field.getHelp());
        valMap.put(CommonService.DOC_FR, translationService.getTranslation(field.getHelp(), "fr"));
      }
    }

    valMap.put(CommonService.WIDGET, field.getWidget());
  }
}
