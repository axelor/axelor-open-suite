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

import com.axelor.apps.base.db.App;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaHelp;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaActionRepository;
import com.axelor.meta.db.repo.MetaHelpRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.View;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.meta.schema.views.AbstractWidget;
import com.axelor.meta.schema.views.Button;
import com.axelor.meta.schema.views.ButtonGroup;
import com.axelor.meta.schema.views.Dashlet;
import com.axelor.meta.schema.views.Field;
import com.axelor.meta.schema.views.FormView;
import com.axelor.meta.schema.views.Label;
import com.axelor.meta.schema.views.Menu;
import com.axelor.meta.schema.views.Menu.Item;
import com.axelor.meta.schema.views.Panel;
import com.axelor.meta.schema.views.PanelEditor;
import com.axelor.meta.schema.views.PanelField;
import com.axelor.meta.schema.views.PanelInclude;
import com.axelor.meta.schema.views.PanelRelated;
import com.axelor.meta.schema.views.PanelStack;
import com.axelor.meta.schema.views.PanelTabs;
import com.axelor.meta.schema.views.Selection.Option;
import com.axelor.meta.schema.views.Spacer;
import com.axelor.studio.service.CommonService;
import com.axelor.studio.service.TranslationService;
import com.axelor.studio.service.ViewLoaderService;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormExporter {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private boolean newForm = false;

  private boolean toolbar = false;

  private List<String[]> o2mViews;

  private ExporterService exporterService;

  @Inject private TranslationService translationService;

  @Inject private MetaHelpRepository metaHelpRepo;

  public List<String[]> export(
      ExporterService exporterService, MetaView formView, List<String> grid)
      throws JAXBException, ClassNotFoundException {

    this.exporterService = exporterService;
    o2mViews = new ArrayList<String[]>();
    /** 1. Is panelTab. 2. Grid view field list. 3. Panel level. 4. Module to check (if-module). */
    Object[] extra = new Object[] {false, grid, null, null};
    Mapper mapper = Mapper.of(Class.forName(formView.getModel()));
    ObjectViews objectViews = XMLViews.fromXML(formView.getXml());

    FormView form = (FormView) objectViews.getViews().get(0);
    newForm = true;
    String simpleName = mapper.getBeanClass().getSimpleName();
    String viewName = form.getName() + "(" + form.getTitle() + ")";

    processForm(form, formView.getModule(), simpleName, viewName, mapper, extra);

    return this.o2mViews;
  }

  private String processForm(
      FormView form, String module, String model, String view, Mapper mapper, Object[] extra) {

    toolbar = false;

    String panelLevel = (String) extra[2];

    log.debug("Processing form: {}", view);

    if (form.getOnNew() != null) {
      addEvent(module, model, view, "onnew", form.getOnNew());
    }

    if (form.getOnLoad() != null) {
      addEvent(module, model, view, "onload", form.getOnLoad());
    }

    if (form.getOnSave() != null) {
      addEvent(module, model, view, "onsave", form.getOnSave());
    }

    List<Button> buttons = form.getToolbar();
    if (buttons != null) {
      toolbar = true;
      for (Button button : buttons) {
        processButton(button, module, model, view, mapper, extra);
      }
      toolbar = false;
    }

    processMenuBarMenu(form.getMenubar(), module, model, view, mapper, extra);

    panelLevel = processItems(form.getItems(), module, model, view, mapper, extra);

    exporterService.addViewProcessed(form.getName());

    return panelLevel;
  }

  private void addEvent(String module, String model, String view, String type, String formula) {

    Map<String, String> valMap = new HashMap<>();
    valMap.put(CommonService.MODULE, module);
    valMap.put(CommonService.MODEL, model);
    valMap.put(CommonService.VIEW, view);
    valMap.put(CommonService.TYPE, type);
    valMap.put(CommonService.FORMULA, formula);

    exporterService.writeRow(valMap, newForm);

    newForm = false;
  }

  private void processMenuBarMenu(
      List<Menu> menubar, String module, String model, String view, Mapper mapper, Object[] extra) {

    if (menubar == null) {
      return;
    }

    for (Menu menu : menubar) {
      String title = menu.getTitle();

      Map<String, String> valMap = new HashMap<>();
      valMap.put(CommonService.MODULE, module);
      valMap.put(CommonService.MODEL, model);
      valMap.put(CommonService.VIEW, view);
      valMap.put(CommonService.TITLE, title);
      valMap.put(CommonService.TITLE_FR, translationService.getTranslation(title, "fr"));
      valMap.put(CommonService.TYPE, "menubar");
      valMap.put(
          CommonService.IF_MODULE, ExporterService.getModuleToCheck(menu, (String) extra[3]));

      exporterService.writeRow(valMap, newForm);
      newForm = false;

      processItems(menu.getItems(), module, model, view, mapper, extra);
    }
  }

  private String processItems(
      List<AbstractWidget> items,
      String module,
      String model,
      String view,
      Mapper mapper,
      Object[] extra) {

    String panelLevel = (String) extra[2];
    if (items == null) {
      return panelLevel;
    }
    boolean panelTab = (boolean) extra[0];
    for (AbstractWidget item : items) {

      if (item.getModuleToCheck() != null
          && !exporterService.isExportModule(item.getModuleToCheck())) {
        continue;
      }

      Class<? extends AbstractWidget> klass = item.getClass();
      String name = klass.getSimpleName();

      String methodName = "process" + name;

      try {

        Method method =
            FormExporter.class.getDeclaredMethod(
                methodName,
                new Class[] {
                  klass, String.class, String.class, String.class, Mapper.class, Object[].class
                });
        method.setAccessible(true);
        extra[0] = panelTab;
        panelLevel = (String) method.invoke(this, item, module, model, view, mapper, extra);
        extra[2] = panelLevel;

      } catch (NoSuchMethodException e) {
        log.debug("No method found: {}", methodName);
      } catch (SecurityException
          | IllegalAccessException
          | IllegalArgumentException
          | InvocationTargetException e) {
        e.printStackTrace();
      }
    }

    return panelLevel;
  }

  @SuppressWarnings("unused")
  private String processPanel(
      Panel panel, String module, String model, String view, Mapper mapper, Object[] extra) {

    String panelType = "panel";
    if ((boolean) extra[0]) {
      panelType = "paneltab";
    }

    if (panel.getSidebar() != null && panel.getSidebar()) {
      panelType = "panelside";
    }

    Map<String, String> valMap = new HashMap<>();
    valMap.put(CommonService.MODULE, module);
    valMap.put(CommonService.MODEL, model);
    valMap.put(CommonService.VIEW, view);
    valMap.put(CommonService.NAME, panel.getName());
    valMap.put(CommonService.TITLE, panel.getTitle());
    valMap.put(CommonService.TITLE_FR, translationService.getTranslation(panel.getTitle(), "fr"));
    valMap.put(CommonService.TYPE, panelType);
    valMap.put(CommonService.IF_CONFIG, panel.getConditionToCheck());
    extra[3] = ExporterService.getModuleToCheck(panel, (String) extra[3]);
    valMap.put(CommonService.IF_MODULE, (String) extra[3]);

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

    if (panel.getColSpan() != null) {
      valMap.put(CommonService.COLSPAN, panel.getColSpan().toString());
    }

    String panelLevel = (String) extra[2];
    panelLevel = getPanelLevel(panelLevel);
    valMap.put(CommonService.PANEL_LEVEL, panelLevel);

    exporterService.writeRow(valMap, newForm);

    newForm = false;

    extra[0] = false;
    extra[2] = panelLevel + ".-1";

    processItems(panel.getItems(), module, model, view, mapper, extra);

    return panelLevel;
  }

  @SuppressWarnings("unused")
  private String processPanelStack(
      PanelStack panel, String module, String model, String view, Mapper mapper, Object[] extra) {
    return processItems(panel.getItems(), module, model, view, mapper, extra);
  }

  private String getPanelLevel(String panelLevel) {

    if (panelLevel == null) {
      return "0";
    }

    String[] levels = panelLevel.split("\\.");
    String lastLevel = levels[levels.length - 1];
    Integer last = (Integer.parseInt(lastLevel) + 1);
    levels[levels.length - 1] = last.toString();

    return Joiner.on(".").join(levels);
  }

  @SuppressWarnings("unused")
  private String processPanelField(
      PanelField panelField,
      String module,
      String model,
      String view,
      Mapper mapper,
      Object[] extra)
      throws ClassNotFoundException {

    processField(panelField, module, model, view, mapper, extra);

    newForm = false;

    if (panelField.getEditor() != null) {
      processPanelEditor(panelField, module, model, view, mapper, extra);
    }

    return (String) extra[2];
  }

  @SuppressWarnings("unused")
  private String processPanelTabs(
      PanelTabs panelTabs,
      String module,
      String model,
      String view,
      Mapper mapper,
      Object[] extra) {

    Map<String, String> valMap = new HashMap<>();
    valMap.put(CommonService.MODULE, module);
    valMap.put(CommonService.MODEL, model);
    valMap.put(CommonService.VIEW, view);
    valMap.put(CommonService.TYPE, "panelbook");
    valMap.put(CommonService.IF_CONFIG, panelTabs.getConditionToCheck());
    extra[3] = ExporterService.getModuleToCheck(panelTabs, (String) extra[3]);
    valMap.put(CommonService.IF_MODULE, (String) extra[3]);

    if (panelTabs.getReadonly() != null && panelTabs.getReadonly()) {
      valMap.put(CommonService.READONLY, "x");
    } else {
      valMap.put(CommonService.READONLY, panelTabs.getReadonlyIf());
    }

    if (panelTabs.getHidden() != null && panelTabs.getHidden()) {
      valMap.put(CommonService.HIDDEN, "x");
    } else {
      valMap.put(CommonService.HIDDEN, panelTabs.getHideIf());
    }

    valMap.put(CommonService.SHOW_IF, panelTabs.getShowIf());

    Integer colspan = panelTabs.getColSpan();
    if (colspan != null) {
      valMap.put(CommonService.COLSPAN, colspan.toString());
    }

    String panelLevel = getPanelLevel((String) extra[2]);
    valMap.put(CommonService.PANEL_LEVEL, panelLevel);

    extra[0] = true;
    extra[2] = panelLevel + ".-1";

    log.debug("Exporting panel book view: {}", view);
    exporterService.writeRow(valMap, newForm);

    processItems(panelTabs.getItems(), module, model, view, mapper, extra);

    return panelLevel;
  }

  private String processField(
      Field field, String module, String model, String view, Mapper mapper, Object[] extra) {

    String paneLevel = (String) extra[2];

    String name = field.getName();

    Map<String, String> valMap = new HashMap<>();
    valMap.put(CommonService.MODULE, module);
    valMap.put(CommonService.MODEL, model);
    valMap.put(CommonService.VIEW, view);
    valMap.put(CommonService.NAME, name);
    valMap.put(CommonService.TITLE, field.getTitle());
    valMap.put(CommonService.TITLE_FR, translationService.getTranslation(field.getTitle(), "fr"));

    valMap.put(CommonService.IF_MODULE, ExporterService.getModuleToCheck(field, (String) extra[3]));

    if (!name.contains(".")) {
      valMap.put(CommonService.TYPE, field.getServerType());
    }
    valMap.put(CommonService.IF_CONFIG, field.getConditionToCheck());

    String target = field.getTarget();

    Property property = mapper.getProperty(name);

    if (property != null) {
      addProperties(valMap, property, target);
    }

    if (target != null && valMap.get(CommonService.TYPE) != null) {
      String[] targets = target.split("\\.");
      valMap.put(
          CommonService.TYPE,
          valMap.get(CommonService.TYPE) + "(" + targets[targets.length - 1] + ")");
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

    if (Strings.isNullOrEmpty(valMap.get(CommonService.TYPE))) {
      valMap.put(CommonService.TYPE, "empty");
    } else {
      valMap.put(
          CommonService.TYPE,
          getType(
              valMap.get(CommonService.TYPE), field.getWidget(), valMap.get(CommonService.SELECT)));
    }

    if (valMap.get(CommonService.TYPE).equals("ENUM")) {
      return paneLevel;
    }

    addExtraAttributes(field, valMap);

    @SuppressWarnings("unchecked")
    List<String> grid = (List<String>) extra[1];
    if (grid.contains(name)) {
      valMap.put(CommonService.GRID, grid.get(0));
    }

    exporterService.writeRow(valMap, newForm);

    newForm = false;

    return paneLevel;
  }

  private void addProperties(Map<String, String> valMap, Property property, String target) {

    valMap.put(CommonService.TYPE, property.getType().name());

    if (valMap.get(CommonService.TITLE) == null) {
      valMap.put(CommonService.TITLE, property.getTitle());
      valMap.put(
          CommonService.TITLE_FR, translationService.getTranslation(property.getTitle(), "fr"));
    }

    valMap.put(CommonService.SELECT, property.getSelection());

    Class<?> targetClass = property.getTarget();
    if (targetClass != null) {
      target = targetClass.getName();
    }

    if (property.isRequired()) {
      valMap.put(CommonService.REQUIRED, "x");
    }

    if (property.isReadonly()) {
      valMap.put(CommonService.READONLY, "x");
    }

    if (property.isHidden()) {
      valMap.put(CommonService.HIDDEN, "x");
    }

    String help = property.getHelp();
    help = findHelp(property, help, valMap.get(CommonService.VIEW), "en");
    if (help != null) {
      valMap.put(CommonService.HELP, "x");
      valMap.put(CommonService.DOC, help);
    }

    String helpFr = property.getHelp();
    helpFr = findHelp(property, help, valMap.get(CommonService.VIEW), "fr");
    if (helpFr != null) {
      valMap.put(CommonService.HELP, "x");
      valMap.put(CommonService.DOC_FR, helpFr);
    }
  }

  private String findHelp(Property property, String help, String view, String language) {

    view = view.split("\\(")[0];

    MetaHelp metaHelp =
        metaHelpRepo
            .all()
            .filter(
                "self.model = ?1 AND self.field = ?2 AND self.language = ?3 AND self.view = ?4",
                property.getEntity().getName(),
                property.getName(),
                language,
                view)
            .fetchOne();

    if (metaHelp != null) {
      return metaHelp.getHelp();
    }

    String translated = translationService.getTranslation(help, language);

    if (translated != null) {
      return translated;
    }

    return help;
  }

  private void addExtraAttributes(Field field, Map<String, String> valMap) {

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

    if (field.getColSpan() != null) {
      valMap.put(CommonService.COLSPAN, field.getColSpan().toString());
    }

    valMap.put(CommonService.WIDGET, field.getWidget());
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

  private String processPanelEditor(
      PanelField panelField,
      String module,
      String model,
      String view,
      Mapper mapper,
      Object[] extra)
      throws ClassNotFoundException {

    PanelEditor panelEditor = panelField.getEditor();

    Property property = mapper.getProperty(panelField.getName());
    if (property != null) {
      model = model + "(" + panelField.getName() + ")";
    }

    extra[0] = false;

    String target = panelField.getTarget();
    if (target != null) {
      newForm = true;
      try {
        Mapper targetMapper = Mapper.of(Class.forName(target));
        processItems(panelEditor.getItems(), module, model, view, targetMapper, extra);
      } catch (IllegalArgumentException e) {
        log.debug("Model not found: {}", target);
      }
    } else {
      processItems(panelEditor.getItems(), module, model, view, mapper, extra);
    }

    return (String) extra[2];
  }

  @SuppressWarnings("unused")
  private String processPanelInclude(
      PanelInclude panelInclude,
      String module,
      String model,
      String view,
      Mapper mapper,
      Object[] extra) {

    AbstractView panelView = panelInclude.getView();

    if (view != null && panelView != null) {
      String name = panelView.getName();
      if (!exporterService.isViewProcessed(name)) {
        extra[3] = ExporterService.getModuleToCheck(panelInclude, (String) extra[3]);
        return processForm((FormView) panelView, module, model, view, mapper, extra);
      }
    } else {
      log.debug("Issue in panel include: {}", panelInclude.getName());
    }

    return (String) extra[2];
  }

  @SuppressWarnings("unused")
  private String processDashlet(
      Dashlet dashlet, String module, String model, String view, Mapper mapper, Object[] extra) {

    MetaAction metaAction = Beans.get(MetaActionRepository.class).findByName(dashlet.getAction());

    if (metaAction != null
        && metaAction.getType().equals("action-view")
        && metaAction.getModel() != null) {

      String target = metaAction.getModel();
      try {
        Class<?> klass = Class.forName(model);
        if (!App.class.isAssignableFrom(klass)) {
          log.debug("Dashlet not considered for:  {}", model);
          return (String) extra[2];
        }
      } catch (ClassNotFoundException e1) {
      }

      try {
        ObjectViews objectViews = XMLViews.fromXML(metaAction.getXml());
        ActionView actionView = (ActionView) objectViews.getActions().get(0);
        view = view.split("\\(")[0];
        String parentView = view + "(" + actionView.getTitle() + ")";
        String titleFR = translationService.getTranslation(actionView.getTitle(), "fr");
        String parentViewFR = view + "(" + titleFR + ")";
        String form = null;
        String grid = null;
        for (View actView : actionView.getViews()) {
          if (actView.getType().equals("form")) {
            form = actView.getName();
          } else if (actView.getType().equals("grid")) {
            grid = actView.getName();
          }
        }
        if (form == null) {
          form = ViewLoaderService.getDefaultViewName(target, "form");
        }
        if (!exporterService.isViewProcessed(form) && !form.equals(view)) {
          o2mViews.add(new String[] {target, form, grid, parentView, parentViewFR});
        }
      } catch (JAXBException e) {
      }
    }

    return (String) extra[2];
  }

  @SuppressWarnings("unused")
  private String processButtonGroup(
      ButtonGroup buttonGroup,
      String module,
      String model,
      String view,
      Mapper mapper,
      Object[] extra) {

    List<AbstractWidget> items = buttonGroup.getItems();

    if (items != null) {
      processItems(items, module, model, view, mapper, extra);
    }

    return (String) extra[2];
  }

  private String processButton(
      Button button, String module, String model, String view, Mapper mapper, Object[] extra) {

    Map<String, String> valMap = new HashMap<>();
    valMap.put(CommonService.MODULE, module);
    valMap.put(CommonService.MODEL, model);
    valMap.put(CommonService.VIEW, view);
    valMap.put(CommonService.NAME, button.getName());
    valMap.put(CommonService.TITLE, button.getTitle());
    valMap.put(CommonService.TITLE_FR, translationService.getTranslation(button.getTitle(), "fr"));
    valMap.put(CommonService.TYPE, "button");
    valMap.put(CommonService.ON_CHANGE, button.getOnClick());
    valMap.put(CommonService.READONLY, button.getReadonlyIf());
    valMap.put(CommonService.HIDDEN, button.getHideIf());
    valMap.put(CommonService.SHOW_IF, button.getShowIf());
    valMap.put(CommonService.IF_CONFIG, button.getConditionToCheck());
    valMap.put(
        CommonService.IF_MODULE, ExporterService.getModuleToCheck(button, (String) extra[3]));

    if (toolbar) {
      valMap.put(CommonService.TYPE, "button(toolbar)");
    }

    if (button.getColSpan() != null) {
      valMap.put(CommonService.COLSPAN, button.getColSpan().toString());
    }

    exporterService.writeRow(valMap, newForm);

    newForm = false;

    return (String) extra[2];
  }

  @SuppressWarnings("unused")
  private String processPanelRelated(
      PanelRelated panelRelated,
      String module,
      String model,
      String view,
      Mapper mapper,
      Object[] extra) {

    Map<String, String> valMap = new HashMap<>();

    valMap.put(CommonService.NAME, panelRelated.getName());
    valMap.put(CommonService.MODULE, module);
    valMap.put(CommonService.MODEL, model);
    valMap.put(CommonService.VIEW, view);
    valMap.put(CommonService.TITLE, panelRelated.getTitle());
    valMap.put(CommonService.TYPE, panelRelated.getServerType());
    valMap.put(CommonService.READONLY, panelRelated.getReadonlyIf());
    valMap.put(CommonService.HIDDEN, panelRelated.getHideIf());
    valMap.put(CommonService.SHOW_IF, panelRelated.getShowIf());
    valMap.put(CommonService.IF_CONFIG, panelRelated.getConditionToCheck());
    valMap.put(
        CommonService.IF_MODULE, ExporterService.getModuleToCheck(panelRelated, (String) extra[3]));

    String target = panelRelated.getTarget();
    Property property = mapper.getProperty(valMap.get(CommonService.NAME));

    if (property != null) {
      valMap.put(CommonService.TYPE, property.getType().name());
      if (valMap.get(CommonService.TITLE) == null) {
        valMap.put(CommonService.TITLE, property.getTitle());
      }
      Class<?> targetClass = property.getTarget();
      if (targetClass != null) {
        target = targetClass.getName();
      }
    } else {
      log.debug(
          "No property found: {}, class: {}",
          valMap.get(CommonService.NAME),
          valMap.get(CommonService.MODEL));
    }

    valMap.put(
        CommonService.TITLE_FR,
        translationService.getTranslation(valMap.get(CommonService.TITLE), "fr"));
    if (valMap.get(CommonService.TYPE) == null) {
      valMap.put(CommonService.TYPE, "o2m");
    }

    if (target != null) {
      view = view.split("\\(")[0];
      String parentView = view + "(" + valMap.get(CommonService.TITLE) + ")";
      String titleFR = translationService.getTranslation(valMap.get(CommonService.TITLE), "fr");
      if (Strings.isNullOrEmpty(titleFR)) {
        valMap.put(CommonService.TITLE, valMap.get(CommonService.TITLE));
      }
      String parentViewFR = view + "(" + valMap.get(CommonService.TITLE) + ")";
      String form = panelRelated.getFormView();
      if (form == null) {
        form = ViewLoaderService.getDefaultViewName(target, "form");
      }
      if (!exporterService.isViewProcessed(form) && !form.equals(view)) {
        o2mViews.add(
            new String[] {target, form, panelRelated.getGridView(), parentView, parentViewFR});
      }
      String[] targets = target.split("\\.");
      valMap.put(
          CommonService.TYPE,
          valMap.get(CommonService.TYPE) + "(" + targets[targets.length - 1] + ")");
    }

    if (Strings.isNullOrEmpty(valMap.get(CommonService.TYPE))) {
      valMap.put(CommonService.TYPE, "empty");
    } else {
      valMap.put(CommonService.TYPE, getType(valMap.get(CommonService.TYPE), null, null));
    }

    if (panelRelated.getColSpan() != null) {
      valMap.put(CommonService.COLSPAN, panelRelated.getColSpan().toString());
    }

    String panelLevel = (String) extra[2];
    panelLevel = getPanelLevel(panelLevel);
    valMap.put(CommonService.PANEL_LEVEL, panelLevel);
    extra[2] = panelLevel;

    exporterService.writeRow(valMap, newForm);

    newForm = false;

    extra[0] = false;

    return panelLevel;
  }

  @SuppressWarnings("unused")
  private String processLabel(
      Label label, String module, String model, String view, Mapper mapper, Object[] extra) {

    Map<String, String> valMap = new HashMap<>();
    valMap.put(CommonService.MODULE, module);
    valMap.put(CommonService.MODEL, model);
    valMap.put(CommonService.VIEW, view);
    valMap.put(CommonService.NAME, label.getName());
    valMap.put(CommonService.TITLE, label.getTitle());
    valMap.put(CommonService.TITLE_FR, translationService.getTranslation(label.getTitle(), "fr"));
    valMap.put(CommonService.TYPE, "label");
    valMap.put(CommonService.IF_CONFIG, label.getConditionToCheck());
    valMap.put(CommonService.HIDDEN, label.getHideIf());
    valMap.put(CommonService.IF_MODULE, ExporterService.getModuleToCheck(label, (String) extra[3]));

    if (label.getColSpan() != null) {
      valMap.put(CommonService.COLSPAN, label.getColSpan().toString());
    }

    exporterService.writeRow(valMap, newForm);

    newForm = false;

    return (String) extra[2];
  }

  @SuppressWarnings("unused")
  private String processItem(
      Item item, String module, String model, String view, Mapper mapper, Object[] extra) {

    Map<String, String> valMap = new HashMap<>();
    valMap.put(CommonService.MODULE, module);
    valMap.put(CommonService.MODEL, model);
    valMap.put(CommonService.VIEW, view);
    valMap.put(CommonService.NAME, item.getName());
    valMap.put(CommonService.TITLE, item.getTitle());
    valMap.put(CommonService.TITLE_FR, translationService.getTranslation(item.getTitle(), "fr"));
    valMap.put(CommonService.TYPE, "menubar.item");
    valMap.put(CommonService.ON_CHANGE, item.getAction());
    valMap.put(CommonService.READONLY, item.getReadonlyIf());
    valMap.put(CommonService.HIDDEN, item.getHideIf());
    valMap.put(CommonService.SHOW_IF, item.getShowIf());
    valMap.put(CommonService.IF_CONFIG, item.getConditionToCheck());
    valMap.put(CommonService.IF_MODULE, ExporterService.getModuleToCheck(item, (String) extra[3]));
    if (item.getHidden() != null && item.getHidden()) {
      valMap.put(CommonService.HIDDEN, "x");
    }

    exporterService.writeRow(valMap, newForm);

    newForm = false;

    return (String) extra[2];
  }

  @SuppressWarnings("unused")
  private String processSpacer(
      Spacer spacer, String module, String model, String view, Mapper mapper, Object[] extra) {

    Map<String, String> valMap = new HashMap<>();
    valMap.put(CommonService.MODULE, module);
    valMap.put(CommonService.MODEL, model);
    valMap.put(CommonService.VIEW, view);
    valMap.put(CommonService.TYPE, "spacer");
    valMap.put(CommonService.IF_CONFIG, spacer.getConditionToCheck());
    valMap.put(
        CommonService.IF_MODULE, ExporterService.getModuleToCheck(spacer, (String) extra[3]));

    Integer colSpan = spacer.getColSpan();
    if (colSpan != null) {
      valMap.put(CommonService.COLSPAN, colSpan.toString());
    }

    exporterService.writeRow(valMap, newForm);

    newForm = false;

    return (String) extra[2];
  }

  private String getType(String type, String widget, String select) {

    if (CommonService.FIELD_TYPES.containsKey(type)
        || CommonService.VIEW_ELEMENTS.containsKey(type)) {
      return type;
    }

    String[] types = type.split("\\(");

    if (CommonService.FIELD_TYPES.containsKey(types[0])
        || CommonService.VIEW_ELEMENTS.containsKey(types[0])) {
      return types[0];
    }

    types[0] = types[0].replace("-", "_");

    switch (types[0].toUpperCase()) {
      case "INTEGER":
        if (!Strings.isNullOrEmpty(select)) {
          return "select";
        }
        if (widget != null && widget.equals("duration")) {
          return "duration";
        }
        return "int";
      case "DECIMAL":
        if (!Strings.isNullOrEmpty(select)) {
          return "select";
        }
        return "decimal";
      case "BOOLEAN":
        return "boolean";
      case "TEXT":
        if (widget != null && widget.equals("html")) {
          return "html";
        }
        return "text";
      case "DATE":
        return "date";
      case "LONG":
        return "long";
      case "TIME":
        return "time";
      case "LOCALDATETIME":
        return "datetime";
      case "DATETIME":
        return "datetime";
      case "LOCALDATE":
        return "date";
      case "LOCALTIME":
        return "time";
      case "ONE_TO_MANY":
        if (types.length == 1) {
          return "o2m";
        }
        return "o2m" + "(" + types[1];
      case "MANY_TO_ONE":
        if (types.length == 1) {
          return "m2o";
        }
        return "m2o" + "(" + types[1];
      case "ONE_TO_ONE":
        if (types.length == 1) {
          return "o2o";
        }
        return "o2o" + "(" + types[1];
      case "MANY_TO_MANY":
        if (types.length == 1) {
          return "m2m";
        }
        return "m2m" + "(" + types[1];
      case "BINARY":
        return "binary";
      case "STRING":
        if (!Strings.isNullOrEmpty(select)) {
          if (widget != null && widget.equals("multi-select")) {
            return "multiselect";
          }
          return "select(char)";
        }
        return "char";
    }

    return type;
  }
}
