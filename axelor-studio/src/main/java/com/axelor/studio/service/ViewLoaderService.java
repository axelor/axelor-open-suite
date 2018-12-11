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
package com.axelor.studio.service;

import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.meta.schema.views.AbstractWidget;
import com.axelor.meta.schema.views.Button;
import com.axelor.meta.schema.views.CalendarView;
import com.axelor.meta.schema.views.Field;
import com.axelor.meta.schema.views.FormView;
import com.axelor.meta.schema.views.GridView;
import com.axelor.meta.schema.views.KanbanView;
import com.axelor.meta.schema.views.Menu;
import com.axelor.meta.schema.views.Panel;
import com.axelor.meta.schema.views.PanelInclude;
import com.axelor.meta.schema.views.PanelRelated;
import com.axelor.meta.schema.views.PanelTabs;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import com.axelor.studio.db.repo.ViewBuilderRepository;
import com.axelor.studio.service.creator.ModelCreatorService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service use to find default ViewBuilder of type grid and form for a model or create new if
 * not found.
 *
 * @author axelor
 */
public class ViewLoaderService {

  private Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private ViewBuilder viewBuilder;

  @Inject private MetaViewRepository metaViewRepo;

  @Inject private MetaFieldRepository metaFieldRepo;

  @Inject private ViewBuilderRepository viewBuilderRepo;

  @Inject private ModelCreatorService modelBuilderService;

  @Inject private ConfigurationService configSerivice;

  /**
   * Load selected MetaView of ViewBuilder. It create ViewPanel and ViewField records from MetaView.
   *
   * @param builder ViewBuilder to update.
   * @return Updated ViewBuilder.
   */
  public ViewBuilder loadMetaView(ViewBuilder builder) {

    viewBuilder = builder;

    MetaView metaView = viewBuilder.getMetaViewGenerated();

    try {
      if (metaView != null) {

        metaView = metaViewRepo.find(metaView.getId());

        ObjectViews objectViews = XMLViews.fromXML(metaView.getXml());

        if (objectViews != null) {
          List<AbstractView> views = objectViews.getViews();
          if (!views.isEmpty()) {
            loadView(views.get(0));
          }
        }
      }
    } catch (JAXBException e) {
      e.printStackTrace();
    }

    return viewBuilder;
  }

  private void loadView(AbstractView abstractView) {

    viewBuilder.setModel(abstractView.getModel());
    log.debug("Loading view: {}, Model: {}", abstractView.getName(), abstractView.getModel());
    clearExistingFields();

    loadToolbar(abstractView.getToolbar());
    loadMenubar(abstractView.getMenubar());
    //		viewBuilder.setCss(abstractView.getCss());
    if (abstractView.getEditable() != null) {
      viewBuilder.setEditable(abstractView.getEditable());
    }
    viewBuilder.setModel(abstractView.getModel());

    if (abstractView instanceof FormView) {
      loadForm((FormView) abstractView);
    } else if (abstractView instanceof GridView) {
      loadGrid((GridView) abstractView);
    } else if (abstractView instanceof CalendarView) {
      loadCalendar((CalendarView) abstractView);
    } else if (abstractView instanceof KanbanView) {
      loadKanban((KanbanView) abstractView);
    }
  }

  private void clearExistingFields() {
    //		viewBuilder.clearMenubarViewItemList();
    //		viewBuilder.clearSideViewPanelList();
    //		viewBuilder.clearViewPanelList();
    //		viewBuilder.clearToolbarViewItemList();
    viewBuilder.clearViewItemList();
  }

  private void loadForm(FormView formView) {
    //		viewBuilder.setOnSave(formView.getOnSave());
    viewBuilder.setOnNew(formView.getOnNew());
    viewBuilder.setOnLoad(formView.getOnLoad());
    loadPanel(formView.getItems().iterator(), null, 0, false, null, null);
  }

  private void loadGrid(GridView gridView) {
    viewBuilder.setOnNew(gridView.getOnNew());
    loadFields(gridView.getItems());
  }

  private void loadCalendar(CalendarView calendarView) {
    viewBuilder.setOnLoad(calendarView.getOnChange());
    viewBuilder.setEventLength(calendarView.getEventLength());
    //		viewBuilder.setEventStartMetaField(findMetaField(calendarView.getEventStart()));
    //		viewBuilder.setEventStopMetaField(findMetaField(calendarView.getEventStart()));
    //		viewBuilder.setColorByMetaField(findMetaField(calendarView.getColorBy()));
    viewBuilder.setModeSelect(calendarView.getMode());
    loadFields(calendarView.getItems());
  }

  private void loadKanban(KanbanView kanbanView) {
    viewBuilder.setOnLoad(kanbanView.getOnMove());
    viewBuilder.setOnNew(kanbanView.getOnNew());
    //		viewBuilder.setColumnByMetaField(findMetaField(kanbanView.getColumnBy()));
    //		viewBuilder.setSequenceByMetaField(findMetaField(kanbanView.getSequenceBy()));
    viewBuilder.setRecordLimit(kanbanView.getLimit());
    viewBuilder.setCustomSearch(kanbanView.getCustomSearch());
    viewBuilder.setFreeSearchSelect(kanbanView.getFreeSearch());
    viewBuilder.setDraggable(kanbanView.getDraggable());
    //		viewBuilder.setTemplateText(kanbanView.getTemplate());
    loadFields(kanbanView.getItems());
  }

  private MetaField findMetaField(String name) {
    return metaFieldRepo
        .all()
        .filter("self.metaModel.fullName = ?1 and self.name = ?2", viewBuilder.getModel(), name)
        .fetchOne();
  }

  private void loadMenubar(List<Menu> menubar) {

    if (menubar == null) {
      return;
    }
    int sequence = 0;
    for (Menu menu : menubar) {
      ViewItem item = new ViewItem();
      item.setName(menu.getName());
      //			item.setIcon(menu.getIcon());
      //			item.setIfModule(menu.getModuleToCheck());
      //			item.setIfConfig(menu.getConditionToCheck());
      item.setSequence(sequence);
      if (menu.getShowTitle() != null) {
        //				item.setHideTitle(!menu.getShowTitle());
      }
      int seq = 0;
      for (AbstractWidget widget : menu.getItems()) {
        if (widget instanceof Menu.Item) {
          Menu.Item menuItem = (Menu.Item) widget;
          ViewItem subItem = new ViewItem();
          subItem.setName(menuItem.getName());
          //					subItem.setsetTitle(menuItem.getTitle());
          //					subItem.setOnClick(menuItem.getAction());
          //					subItem.setShowIf(menuItem.getShowIf());
          //					subItem.setReadonlyIf(menuItem.getReadonlyIf());
          //					subItem.setHideIf(menuItem.getHideIf());
          //					subItem.setIfModule(menuItem.getModuleToCheck());
          //					subItem.setIfConfig(menuItem.getConditionToCheck());
          subItem.setSequence(seq);
          seq++;
          //					item.addMenubarItem(subItem);
        }
      }
      sequence++;
      //			viewBuilder.addMenubarViewItemListItem(item);
    }
  }

  /**
   * Method create ViewButton for toolbar of ViewBuilder from Buttons of toolbar of FormView.
   *
   * @param toolbar List of Buttons
   */
  private void loadToolbar(List<Button> toolbar) {

    if (toolbar == null) {
      return;
    }
    int seq = 1;
    for (Button toolButton : toolbar) {
      ViewItem viewButton = new ViewItem(toolButton.getName());
      //			viewButton.setTypeSelect(1);
      //      			viewButton.setTitle(toolButton.getTitle());
      //			viewButton.setOnClick(toolButton.getOnClick());
      //			viewButton.setShowIf(toolButton.getShowIf());
      //			viewButton.setHideIf(toolButton.getHideIf());
      //			viewButton.setHidden(toolButton.getHidden());
      //			viewButton.setReadonlyIf(toolButton.getReadonlyIf());
      //			viewButton.setReadonly(toolButton.getReadonly());
      //			viewButton.setIcon(toolButton.getIcon());
      //			viewButton.setPromptMsg(toolButton.getPrompt());
      viewButton.setSequence(seq);
      seq++;
      //			viewBuilder.addToolbarViewItemListItem(viewButton);
    }
  }

  /**
   * Create ViewPanel from AbstractWidget, with given parent level.
   *
   * @param iterator AbstractWidget iterator
   * @param parentLevel String of parent level
   * @param levelCounter Level counter for current panel.
   * @param isPanelTab Boolean to check if its PanelTab.
   */
  private Integer loadPanel(
      Iterator<AbstractWidget> iterator,
      String parentLevel,
      Integer levelCounter,
      Boolean isPanelTab,
      String ifCondition,
      String moduleCondition) {

    if (!iterator.hasNext()) {
      return levelCounter;
    }

    AbstractWidget widget = iterator.next();
    String currentLevel = levelCounter.toString();
    if (parentLevel != null) {
      currentLevel = parentLevel + "." + currentLevel;
    }

    if (widget.getConditionToCheck() != null) {
      ifCondition = widget.getConditionToCheck();
    }
    if (widget.getModuleToCheck() != null) {
      moduleCondition = widget.getModuleToCheck();
    }

    if (widget instanceof Panel) {

      Panel panel = (Panel) widget;
      //			ViewPanel viewPanel = new ViewPanel();
      //			viewPanel.setName(panel.getName());
      //			viewPanel.setIsPanelTab(isPanelTab);
      //			viewPanel.setIfConfig(ifCondition);
      //			viewPanel.setIfModule(moduleCondition);
      //			loadCommonPanelAttrs(panel, viewPanel);
      //			if (isPanelTab != null && isPanelTab) {
      //				viewPanel.setOnTabSelect(panel.getOnTabSelect());
      //			}
      //			String title = panel.getTitle();
      //			if (title != null) {
      //				viewPanel.setTitle(title);
      //				log.debug("Panel title : {}", title);
      //			}
      //
      //			viewPanel.setPanelLevel(currentLevel);
      //			log.debug("Form Panel : {}", panel);
      //			if (panel.getSidebar() != null && panel.getSidebar()) {
      //				viewBuilder.addSideViewPanelListItem(viewPanel);
      //			} else {
      //				viewBuilder.addViewPanelListItem(viewPanel);
      //			}
      // setPanel(panel.getItems().iterator(), currentLevel, 0);

      levelCounter += 1;
    } else if (widget instanceof PanelTabs) {
      PanelTabs panelTabs = (PanelTabs) widget;
      //			ViewPanel viewPanel = new ViewPanel();
      //			viewPanel.setIsNotebook(true);
      //			viewPanel.setPanelLevel(currentLevel);
      //			viewPanel.setIfConfig(panelTabs.getConditionToCheck());
      //			viewPanel.setIfModule(panelTabs.getModuleToCheck());
      //			loadCommonPanelAttrs(panelTabs, viewPanel);
      //			viewBuilder.addViewPanelListItem(viewPanel);
      //			loadPanel(panelTabs.getItems().iterator(), currentLevel, 0, true, null, null);
      isPanelTab = false;
      levelCounter += 1;
    } else if (widget instanceof PanelInclude) {
      PanelInclude panelInclude = (PanelInclude) widget;
      FormView formView = (FormView) panelInclude.getView();
      if (formView != null) {
        List<AbstractWidget> formItems = formView.getItems();
        if (formItems != null) {
          levelCounter =
              loadPanel(
                  formItems.iterator(),
                  parentLevel,
                  levelCounter,
                  false,
                  panelInclude.getConditionToCheck(),
                  panelInclude.getModuleToCheck());
        }
      }
    } else if (widget instanceof PanelRelated) {
      levelCounter += 1;
    }

    loadPanel(iterator, parentLevel, levelCounter, isPanelTab, null, null);

    return levelCounter;
  }

  ////	private void loadCommonPanelAttrs(AbstractPanel panel, ViewPanel viewPanel) {
  ////
  ////		viewPanel.setShowIf(panel.getShowIf());
  ////		viewPanel.setHideIf(panel.getHideIf());
  ////		viewPanel.setReadonlyIf(panel.getReadonlyIf());
  ////		viewPanel.setHidden(panel.getHidden());
  ////		viewPanel.setReadonly(panel.getReadonly());
  ////
  ////	}

  /**
   * Create ViewField record from AbstractWidget list(Grid view fields) and add it ViewBuilder.
   *
   * @param model Model to find correct MetaField for ViewField.
   * @param items List of AbstractWidgets.
   */
  private void loadFields(List<AbstractWidget> items) {

    Integer counter = 1;
    for (AbstractWidget abstractWidget : items) {

      if (!(abstractWidget instanceof Field)) {
        continue;
      }
      counter++;

      Field field = (Field) abstractWidget;
      ViewItem viewField = new ViewItem();
      //			viewField.setTypeSelect(0);
      String name = field.getName();
      viewField.setName(name);
      //			viewField.setTitle(field.getTitle());
      //			viewField.setOnChange(field.getOnChange());
      //			viewField.setDomainCondition(field.getOnChange());
      //			viewField.setReadonly(field.getReadonly());
      //			viewField.setRequiredIf(field.getRequiredIf());
      //			viewField.setRequired(field.getRequired());
      //			viewField.setReadonlyIf(field.getReadonlyIf());
      //			viewField.setHidden(field.getHidden());
      //			viewField.setReadonlyIf(field.getReadonlyIf());
      viewField.setSequence(counter);
      //			viewField.setIfModule(field.getModuleToCheck());
      //			viewField.setIfConfig(field.getConditionToCheck());
      //			viewField.setFormView(field.getFormView());
      //			viewField.setGridView(field.getGridView());
      //			viewField.setPlaceHolder(field.getPlaceholder());

      String widget = field.getWidget();

      if (widget != null) {
        switch (widget) {
          case "SelectProgress":
            //					viewField.setProgressBar(true);
            break;
          case "html":
            //					viewField.setHtmlWidget(true);
            break;
          default:
            //					viewField.setWidget(widget);
        }
      }

      //			viewField.setsetFieldType("string");
      MetaField metaField = findMetaField(name);
      if (metaField != null) {
        //				viewField.setMetaField(metaField);
        log.debug("Field name: {}", viewField.getName());
        //				viewField.setFieldType(getFieldType(metaField));
      }
      viewBuilder.addViewItemListItem(viewField);
    }
  }

  /**
   * Get simple field type from typeName of MetaField
   *
   * @param metaField MetaField to check for typeName.
   * @return Simple field type.
   */
  public String getFieldType(MetaField metaField) {

    String relationship = metaField.getRelationship();

    if (relationship != null) {
      switch (relationship) {
        case "OneToMany":
          return "one-to-many";
        case "ManyToMany":
          return "many-to-many";
        case "ManyToOne":
          return "many-to-one";
      }
    }

    switch (metaField.getTypeName()) {
      case "String":
        return "string";
      case "Integer":
        return "integer";
      case "Boolean":
        return "boolean";
      case "BigDecimal":
        return "decimal";
      case "Long":
        return "long";
      case "byte[]":
        return "binary";
      case "LocalDate":
        return "date";
      case "DateTime":
        return "datetime";
      case "LocalDateTime":
        return "datetime";
      default:
        return "string";
    }
  }

  /**
   * Method create default view name from given modelName and viewType.
   *
   * @param modelName Model name.
   * @param viewType Type of view
   * @return View name.
   */
  public static String getDefaultViewName(String modelName, String viewType) {

    if (modelName.contains(".")) {
      String[] model = modelName.split("\\.");
      modelName = model[model.length - 1];
    }

    modelName =
        modelName
            .trim()
            .replaceAll("([A-Z]+)([A-Z][a-z])", "$1-$2")
            .replaceAll("([a-z\\d])([A-Z])", "$1-$2")
            .toLowerCase();

    return modelName + "-" + viewType;
  }

  /**
   * Method search default ViewBuilder of type form for a MetaModel with default form view name.
   *
   * @param metaModel MetaModel to find view for.
   * @return Default viewBuilder searched or newly created.
   * @throws AxelorException
   */
  public ViewBuilder getDefaultForm(
      String module, MetaModel metaModel, String title, boolean addParent) throws AxelorException {

    String formName = getDefaultViewName(metaModel.getName(), "form");

    return getViewBuilderForm(module, metaModel, formName, title, addParent);
  }

  /**
   * Method search default ViewBuilder of type form for a MetaModel with default form view name.
   *
   * @param metaModel MetaModel to find view for.
   * @return Default viewBuilder searched or newly created.
   */
  public ViewBuilder getViewBuilder(String module, String viewName, String type) {

    ViewBuilder viewBuilder =
        viewBuilderRepo
            .all()
            .filter("self.name = ?1 AND self.viewType = ?2", viewName, type)
            .fetchOne();
    return viewBuilder;
  }

  @Transactional
  public ViewBuilder getViewBuilderForm(
      String module, MetaModel metaModel, String formName, String title, boolean addParent)
      throws AxelorException {
    String modelName = metaModel.getFullName();

    log.debug("Get default form name: {} model: {}", formName, modelName);

    ViewBuilder viewBuilder =
        viewBuilderRepo
            .all()
            .filter(
                "self.name = ?1 AND self.model = ?2 AND self.viewType = 'form'",
                formName,
                modelName)
            .fetchOne();

    log.debug("ViewBuilder found: {}", viewBuilder);

    MetaModule metaModule = configSerivice.getModule(module);
    if (metaModule == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Module not found: %s"),
          module);
    }

    if (viewBuilder == null) {
      viewBuilder = new ViewBuilder();
      viewBuilder.setName(formName);
      viewBuilder.setModel(modelName);
      //      viewBuilder.setModel(metaModel.getName());
      //			viewBuilder.setMetaModule(metaModule);
      viewBuilder.setViewType("form");
      //			viewBuilder.setEdited(true);
      //			viewBuilder.setRecorded(false);

      if (title == null) {
        title = metaModel.getName();
        //        if (title == null) {
        //          title = metaModel.getName();
        //        }
      }
      viewBuilder.setTitle(title);

      viewBuilder = addDefaultPanel(viewBuilder);

      if (addParent && metaModule.getDepends() != null) {
        viewBuilder = addParentView(viewBuilder, metaModule.getDepends(), formName);
      }
    }

    return viewBuilderRepo.save(viewBuilder);
  }

  @Transactional
  public ViewBuilder addParentView(
      ViewBuilder viewBuilder, Set<MetaModule> parentModules, String name) {

    log.debug("Search parent view for : {}, model: {}", name, viewBuilder.getModel());

    if (parentModules == null || parentModules.isEmpty()) {
      return viewBuilder;
    }

    List<String> modules =
        parentModules.stream().map(MetaModule::getName).collect(Collectors.toList());
    MetaView metaView =
        metaViewRepo
            .all()
            .filter(
                "self.name = ?1 AND self.model = ?2 AND self.type = 'form' and self.module in ?3 ",
                name,
                Beans.get(MetaModelRepository.class)
                    .findByName(viewBuilder.getModel())
                    .getFullName(),
                modules)
            .fetchOne();

    log.debug("Parent View found: {}", metaView);
    if (metaView != null) {
      viewBuilder.setMetaViewGenerated(metaView);
      viewBuilder.setTitle(metaView.getTitle());
      viewBuilder = loadMetaView(viewBuilder);
    } else {
      ViewBuilder parent =
          viewBuilderRepo
              .all()
              .filter(
                  "self.name = ?1 AND self.model = ?2",
                  name,
                  Beans.get(MetaModelRepository.class).findByName(viewBuilder.getModel()).getName(),
                  modules)
              .fetchOne();
      //      viewBuilder.setParentViewBuilder(parent);
    }

    return viewBuilderRepo.save(viewBuilder);
  }

  /**
   * Add default panel in viewBuilder with all custom fields of Model related with ViewBuilder.
   *
   * @param viewBuilder ViewBuilder to update for default panel.
   * @return ViewBuilder with updated default panel.
   */
  private ViewBuilder addDefaultPanel(ViewBuilder viewBuilder) {

    MetaModel metaModel = Beans.get(MetaModelRepository.class).findByName(viewBuilder.getModel());

    List<MetaField> fields = modelBuilderService.getCustomisedFields(metaModel);

    if (fields.isEmpty()) {
      return viewBuilder;
    }

    //    ViewPanel defaultPanel = new ViewPanel();
    //
    //    String panelLevel = getDefaultPanelLevel(viewBuilder);
    //    defaultPanel.setPanelLevel(panelLevel);
    //
    //    modelBuilderService.sortFieldList(fields);
    //
    //    for (MetaField field : fields) {
    //      ViewItem viewField = new ViewItem();
    //      viewField.setTypeSelect(0);
    //      viewField.setName(field.getName());
    //      viewField.setFieldType(field.getFieldType());
    //      // viewField.setTitle(field.getLabel());
    //      viewField.setSequence(field.getSequence());
    //      viewField.setMetaField(field);
    //      if (field.getMultiselect()) {
    //        viewField.setWidget("multi-select");
    //      }
    //      defaultPanel.addViewItemListItem(viewField);
    //    }
    //
    //    viewBuilder.addViewPanelListItem(defaultPanel);

    return viewBuilder;
  }

  /**
   * Method to search default ViewBuilder of type grid for given MetaModel. Create new ViewBuilder
   * if no ViewBuilder found.
   *
   * @param metaModel
   * @return
   * @throws AxelorException
   */
  @Transactional
  public ViewBuilder getDefaultGrid(String module, MetaModel metaModel, boolean reload)
      throws AxelorException {

    String gridName = getDefaultViewName(metaModel.getName(), "grid");
    String modelName = metaModel.getFullName();

    log.debug("Get default grid name: {} model: {}", gridName, modelName);

    ViewBuilder viewBuilder =
        viewBuilderRepo
            .all()
            .filter(
                "self.name = ?1 AND self.model = ?2 AND self.viewType = 'grid'",
                gridName,
                modelName)
            .fetchOne();

    log.debug("ViewBuilder found: {}", viewBuilder);

    if (viewBuilder == null) {

      viewBuilder = new ViewBuilder();
      viewBuilder.setName(gridName);
      viewBuilder.setModel(modelName);
      MetaModule metaModule = configSerivice.getModule(module);
      if (metaModule == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get("Module not found: %s"),
            module);
      }
      //      viewBuilder.setMetaModule(metaModule);
      viewBuilder.setModel(metaModel.getName());
      viewBuilder.setViewType("grid");
      //      viewBuilder.setEdited(true);
      //      viewBuilder.setRecorded(false);
      String title = metaModel.getName();
      //      if (title == null) {
      //        title = metaModel.getName();
      //      }
      viewBuilder.setTitle(title);

      MetaView metaView =
          metaViewRepo
              .all()
              .filter(
                  "self.name = ?1 and self.model = ?2 AND self.type = 'grid'", gridName, modelName)
              .fetchOne();

      log.debug("MetaView found: {}", metaView);

      if (metaView != null) {
        viewBuilder.setMetaViewGenerated(metaView);
        viewBuilder.setTitle(metaView.getTitle());
        viewBuilder = loadMetaView(viewBuilder);
      }

      viewBuilder = addDefaultFields(viewBuilder);
      viewBuilder = viewBuilderRepo.save(viewBuilder);

    } else if (reload) {
      viewBuilder = addDefaultFields(viewBuilder);
      viewBuilder = viewBuilderRepo.save(viewBuilder);
      //      viewBuilder.setEdited(true);
      //      viewBuilder.setRecorded(false);
    }

    return viewBuilder;
  }

  /**
   * Add custom fields in ViewBuilder of type grid.
   *
   * @param viewBuilder ViewBuilder to update.
   * @return Updated ViewBuilder.
   */
  private ViewBuilder addDefaultFields(ViewBuilder viewBuilder) {

    MetaModel model = Beans.get(MetaModelRepository.class).findByName(viewBuilder.getModel());

    Integer sequence = 0;

    if (viewBuilder.getViewItemList() != null) {
      sequence = viewBuilder.getViewItemList().size();
    }

    List<MetaField> fields = modelBuilderService.getCustomisedFields(model);
    modelBuilderService.sortFieldList(fields);

    log.debug("Customized fields in model: {}  total fields : {}", model.getName(), fields.size());

    for (MetaField field : fields) {

      if (sequence > 5) {
        break;
      }

      sequence++;

      ViewItem viewField = new ViewItem();
      //      viewField.setTypeSelect(0);
      viewField.setName(field.getName());
      //      viewField.setFieldType(field.getFieldType());
      viewField.setSequence(sequence);
      //      viewField.setMetaField(field);
      //      if (field.getMultiselect()) {
      //        viewField.setWidget("multi-select");
      //      }
      viewBuilder.addViewItemListItem(viewField);
    }

    return viewBuilder;
  }

  //	/**
  //	 * Get level for default panel to add in ViewBuilder. Default panel will be
  //	 * last panel in case of extended view.
  //	 *
  //	 * @param viewBuilder
  //	 *            ViewBuilder to search for panels.
  //	 * @return Last panel level
  //	 */
  //	private String getDefaultPanelLevel(ViewBuilder viewBuilder) {
  //
  //		Integer lastLevel = 0;
  //
  //		if (viewBuilder.getViewPanelList() == null) {
  //			return "0";
  //		}
  //
  //		List<ViewPanel> viewPanels = viewBuilder.getViewPanelList();
  //		if (viewBuilder.getSideViewPanelList() != null) {
  //			viewPanels.addAll(viewBuilder.getSideViewPanelList());
  //		}
  //
  //		for (ViewPanel viewPanel : viewPanels) {
  //			String panelLevel = viewPanel.getPanelLevel();
  //
  //			if (!Strings.isNullOrEmpty(panelLevel)) {
  //				panelLevel = panelLevel.split("\\.")[0];
  //				try {
  //					Integer level = Integer.parseInt(panelLevel);
  //					if (level >= lastLevel) {
  //						lastLevel = level + 1;
  //					}
  //				} catch (NumberFormatException e) {
  //				}
  //			}
  //		}
  //
  //		return lastLevel.toString();
  //	}

}
