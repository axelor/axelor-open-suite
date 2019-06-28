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

import com.axelor.apps.tool.service.TranslationService;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaJsonFieldRepository;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.views.FormView;
import com.axelor.meta.schema.views.Selection.Option;
import com.axelor.studio.service.CommonService;
import com.axelor.studio.service.builder.ViewBuilderService;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormExporter {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private ExcelExporterService excelExporterService;

  @Inject private TranslationService translationService;

  @Inject private MetaJsonFieldRepository metaJsonFieldRepo;

  @Inject private MetaJsonModelRepository metaJsonModelrepo;

  @Inject private MetaModelRepository metaModelRepo;

  @Inject private ViewBuilderService viewBuilderService;

  @Inject private MetaViewRepository metaViewRepo;

  public void export(ExcelExporterService excelExporterService, MetaView formView, String model) {
    try {
      this.excelExporterService = excelExporterService;

      MetaModel metaModel = metaModelRepo.all().filter("self.fullName = ?", model).fetchOne();
      if (metaModel == null) {
        MetaJsonModel jsonModel = metaJsonModelrepo.findByName(model);
        processCustomItem(jsonModel, formView);
      } else {
        processRealItem(metaModel);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void processCustomItem(MetaJsonModel model, MetaView formView) throws JAXBException {

    ObjectViews objectViews = XMLViews.fromXML(formView.getXml());
    FormView form = (FormView) objectViews.getViews().get(0);

    String viewName = viewBuilderService.getDefaultViewName(formView.getType(), model.getName());

    log.debug("Processing form: {}", viewName);

    processForm(form, viewName);

    List<MetaJsonField> fields = model.getFields();
    Collections.sort(fields, (field1, field2) -> field1.getSequence() - field2.getSequence());

    processItem(null, fields, viewName);

    excelExporterService.addViewProcessed(viewName);
  }

  private void processForm(FormView form, String view) {

    if (form.getOnNew() != null) {
      addEvent(view, "onnew", form.getOnNew());
    }

    if (form.getOnSave() != null) {
      addEvent(view, "onsave", form.getOnSave());
    }
  }

  private void addEvent(String view, String type, String formula) {

    Map<String, String> valMap = new HashMap<>();
    valMap.put(CommonService.VIEW, "form(" + view + ")");
    valMap.put(CommonService.TYPE, type);
    valMap.put(CommonService.FORMULA, formula);

    excelExporterService.writeRow(valMap);
  }

  private void processRealItem(MetaModel model) {
    List<MetaJsonField> fields =
        metaJsonFieldRepo.all().filter("self.model = ?1", model.getFullName()).fetch();

    Collections.sort(fields, (field1, field2) -> field1.getSequence() - field2.getSequence());

    String viewName = "";
    processItem(model, fields, viewName);
  }

  private void processItem(MetaModel model, List<MetaJsonField> fields, String viewName) {

    int panelCounter = 0;
    MetaJsonField panelField = null;
    boolean isField = false;

    for (MetaJsonField field : fields) {
      isField = false;

      if (model != null) {
        viewName = getViewName(field, model);

        if (!excelExporterService.isViewProcessed(viewName)) {
          log.debug("Processing form: {}", viewName);
          excelExporterService.addViewProcessed(viewName);
        }
      }

      if (field.getType().equals("panel")) {
        if (panelCounter > 0) {
          processPanelEnd(panelField, viewName);
        }
        processPanel(field, viewName);
        panelField = field;
        panelCounter++;
        continue;
      }

      processField(field, viewName);
      isField = true;

      if (panelCounter > 0 && fields.get(fields.size() - 1).equals(field)) {
        processPanelEnd(panelField, viewName);
      }
    }
    if (!isField) {
      processPanelEnd(panelField, viewName);
    }
  }

  private String getViewName(MetaJsonField field, MetaModel model) {
    String viewName = "";

    if (field.getModelField().equals("attrs") || field.getModelField().equals("attributes")) {
      viewName = viewBuilderService.getDefaultViewName("form", model.getName());

    } else {
      viewName = "";
      List<MetaView> views =
          metaViewRepo
              .all()
              .filter(
                  "self.model = ?1 and self.type = ?2 and (self.extension is null or self.extension = ?3)",
                  model.getFullName(),
                  "form",
                  false)
              .fetch();

      try {
        for (MetaView view : views) {
          if (view.getXml().contains(field.getModelField())) {
            ObjectViews objectViews = XMLViews.fromXML(view.getXml());
            FormView form = (FormView) objectViews.getViews().get(0);
            viewName += form.getName() + ",";
          }
        }
        viewName = viewName.length() > 0 ? viewName.substring(0, viewName.length() - 1) : "";
      } catch (JAXBException e) {
        e.printStackTrace();
      }
    }
    return viewName;
  }

  private void processPanel(MetaJsonField panel, String view) {

    Map<String, String> valMap = new HashMap<>();

    valMap.put(CommonService.VIEW, "form(" + view + ")");
    valMap.put(CommonService.TYPE, panel.getType());
    valMap.put(CommonService.NAME, panel.getName() + "(start)");
    valMap.put(CommonService.TITLE, panel.getTitle());
    valMap.put(CommonService.TITLE_FR, translationService.getTranslation(panel.getTitle(), "fr"));

    if (panel.getReadonly() != null && panel.getReadonly()) {
      valMap.put(CommonService.READONLY, "x");
    } else {
      valMap.put(CommonService.READONLY, panel.getReadonlyIf());
    }

    if (panel.getRequired() != null && panel.getRequired()) {
      valMap.put(CommonService.REQUIRED, "x");
    } else {
      valMap.put(CommonService.REQUIRED, panel.getRequiredIf());
    }

    if (panel.getHidden() != null && panel.getHidden()) {
      valMap.put(CommonService.HIDDEN, "x");
    } else {
      valMap.put(CommonService.HIDDEN, panel.getHideIf());
    }

    if (panel.getIsWkf() != null && panel.getIsWkf()) {
      valMap.put(CommonService.WKF, "x");
    }

    valMap.put(CommonService.SEQUENCE, panel.getSequence().toString());
    valMap.put(CommonService.SHOW_IF, panel.getShowIf());
    valMap.put(CommonService.ON_CHANGE, panel.getOnChange());
    valMap.put(CommonService.INCLUDE_IF, panel.getIncludeIf());
    valMap.put(CommonService.HELP, panel.getHelp());
    valMap.put(CommonService.WIDGET_ATTRS, panel.getWidgetAttrs());

    excelExporterService.writeRow(valMap);
  }

  private void processPanelEnd(MetaJsonField panel, String view) {

    Map<String, String> valMap = new HashMap<>();

    valMap.put(CommonService.VIEW, "form(" + view + ")");
    valMap.put(CommonService.TYPE, panel.getType());
    valMap.put(CommonService.NAME, panel.getName() + "(end)");
    valMap.put(CommonService.TITLE, panel.getTitle());
    valMap.put(CommonService.TITLE_FR, translationService.getTranslation(panel.getTitle(), "fr"));

    excelExporterService.writeRow(valMap);
  }

  private void processField(MetaJsonField field, String view) {

    Map<String, String> valMap = new HashMap<>();
    valMap.put(CommonService.VIEW, "form(" + view + ")");
    valMap.put(CommonService.NAME, field.getName());
    valMap.put(CommonService.TITLE, field.getTitle());
    valMap.put(CommonService.TITLE_FR, translationService.getTranslation(field.getTitle(), "fr"));

    String target = null;
    if (CommonService.RELATIONAL_JSON_FIELD_TYPES.contains(field.getType())) {
      target = field.getTargetJsonModel().getName();

      valMap.put(CommonService.TYPE, field.getType().replace("json-", "") + "(" + target + ")");

    } else if (CommonService.RELATIONAL_FIELD_TYPES.contains(field.getType())) {
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
    if (field.getIsWkf() != null && field.getIsWkf()) {
      valMap.put(CommonService.WKF, "x");
    }
    addExtraAttributes(field, valMap);

    excelExporterService.writeRow(valMap);
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

    valMap.put(CommonService.CONTEXT_FIELD, field.getContextField());
    valMap.put(CommonService.CONTEXT_FIELD_TARGET, field.getContextFieldTarget());
    valMap.put(CommonService.CONTEXT_FIELD_TARGET_NAME, field.getContextFieldTargetName());
    valMap.put(CommonService.CONTEXT_FIELD_TITLE, field.getContextFieldTitle());
    valMap.put(CommonService.CONTEXT_FIELD_VALUE, field.getContextFieldValue());
    valMap.put(CommonService.DOMAIN, field.getDomain());

    if (field.getType().equals("Enum")) {
      valMap.put(CommonService.ENUM_TYPE, field.getEnumType());
    }
    valMap.put(CommonService.SEQUENCE, field.getSequence().toString());

    if (CommonService.RELATIONAL_FIELD_TYPES.contains(field.getType())) {
      valMap.put(CommonService.FORM_VIEW, field.getFormView());
      valMap.put(CommonService.GRID_VIEW, field.getGridView());
    }
    valMap.put(CommonService.HELP, field.getHelp());

    if (field.getHidden() != null && field.getHidden()) {
      valMap.put(CommonService.HIDDEN, "x");
    } else if (valMap.get(CommonService.HIDDEN) == null) {
      valMap.put(CommonService.HIDDEN, field.getHideIf());
    }
    valMap.put(CommonService.INCLUDE_IF, field.getIncludeIf());
    valMap.put(CommonService.MAX_SIZE, field.getMaxSize().toString());
    valMap.put(CommonService.MIN_SIZE, field.getMinSize().toString());

    if (field.getNameField()) {
      valMap.put(CommonService.NAME_FIELD, "x");
    }
    valMap.put(CommonService.ON_CHANGE, field.getOnChange());
    valMap.put(CommonService.ON_CLICK, field.getOnClick());

    if (field.getType().equals("String")) {
      valMap.put(CommonService.REGEX, field.getRegex());
    }

    if (field.getReadonly() != null && field.getReadonly()) {
      valMap.put(CommonService.READONLY, "x");
    } else if (valMap.get(CommonService.READONLY) == null) {
      valMap.put(CommonService.READONLY, field.getReadonlyIf());
    }

    if (field.getRequired() != null && field.getRequired()) {
      valMap.put(CommonService.REQUIRED, "x");
    } else if (valMap.get(CommonService.REQUIRED) == null) {
      valMap.put(CommonService.REQUIRED, field.getRequiredIf());
    }
    valMap.put(CommonService.SHOW_IF, field.getShowIf());

    if (field.getRoles() != null) {
      List<String> roleNames =
          field.getRoles().stream().map(it -> it.getName()).collect(Collectors.toList());
      valMap.put(CommonService.ROLES, Joiner.on("|").join(roleNames));
    }

    if (field.getType().equals("Decimal")) {
      valMap.put(CommonService.PRECISION, field.getPrecision().toString());
      valMap.put(CommonService.SCALE, field.getScale().toString());
    }
    valMap.put(CommonService.VALUE_EXPR, field.getValueExpr());

    if (field.getVisibleInGrid()) {
      valMap.put(CommonService.VISIBLE_IN_GRID, "x");
    }
    valMap.put(CommonService.WIDGET, field.getWidget());
    valMap.put(CommonService.WIDGET_ATTRS, field.getWidgetAttrs());
  }
}
