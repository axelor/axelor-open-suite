/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.data.exporter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.common.ClassUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
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
import com.axelor.meta.schema.views.PanelTabs;
import com.axelor.meta.schema.views.Selection.Option;
import com.axelor.meta.schema.views.Spacer;
import com.axelor.studio.service.data.DataCommon;
import com.axelor.studio.service.data.DataTranslationService;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class DataExportForm {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private boolean newForm = false;
	
	private boolean toolbar = false;
	
	private List<String[]> o2mViews;
	
	@Inject
	private DataTranslationService translationService;
	
	@Inject
	private DataExportDashboard exportDashboard;
	
	private DataExportService exportService;
	
	public List<String[]> export(DataExportService exportService, MetaView formView, List<String> grid) throws JAXBException {
		
		this.exportService = exportService;
		o2mViews = new ArrayList<String[]>();
		/**
		 * 1. Is panelTab.
		 * 2. Grid view field list.
		 * 3. Panel level.
		 * 4. Module to check (if-module).
		 */
		Object[] extra = new Object[]{false, grid, null, null};
		Mapper mapper = Mapper.of(ClassUtils.findClass(formView.getModel()));
		ObjectViews objectViews = XMLViews.fromXML(formView.getXml());
		
		FormView form = (FormView) objectViews.getViews().get(0);
		newForm = true;
		String simpleName = mapper.getBeanClass().getSimpleName();
		String viewName = form.getName() + "(" + form.getTitle() + ")";
		
		processForm(form, formView.getModule(), simpleName, viewName, mapper, extra);
		
		return this.o2mViews;
	}
	
	private String processForm(FormView form, String module, String model, String view,  Mapper mapper, Object[] extra) {
		
		toolbar = false;
		
		String panelLevel = (String) extra[2];
		
		log.debug("Processing form: {}", view);
		
		exportService.addViewProcessed(form.getName());
		
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
		
		return panelLevel;
	}
	
	
	private void addEvent(String module, String model, String view, String type, String formula) {
		
		String[] values = new String[DataCommon.HEADERS.length];
		values[DataCommon.MODULE] = module; 
		values[DataCommon.MODEL] = model; 
		values[DataCommon.VIEW] = view; 
	    values[DataCommon.TYPE] = type;
	    values[DataCommon.FORMULA] = formula;
	    
	    exportService.writeRow(values, newForm);
	    
	    newForm = false;
	}

	private void processMenuBarMenu(List<Menu> menubar, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		if (menubar == null) {
			return;
		}
		
		for (Menu menu : menubar) {
			String title = menu.getTitle();
			
			String[] values = new String[DataCommon.HEADERS.length];
			values[DataCommon.MODULE] = module; 
			values[DataCommon.MODEL] = model; 
			values[DataCommon.VIEW] = view; 
		    values[DataCommon.TITLE] = title;
		    values[DataCommon.TITLE_FR] = translationService.getTranslation(title, "fr");
		    values[DataCommon.TYPE] = "menubar";
		    values[DataCommon.IF_MODULE] = DataExportService.getModuleToCheck(menu, (String) extra[3]);
		
		    exportService.writeRow(values, newForm);
			newForm = false;
			
			processItems(menu.getItems(), module, model, view, mapper, extra);
		}
	}
	
	private String processItems(List<AbstractWidget> items, String module, String model, String view, Mapper mapper, Object[] extra){
		
		String panelLevel = (String) extra[2];
		if (items == null) {
			return panelLevel;
		}
		boolean panelTab = (boolean) extra[0];
		for (AbstractWidget item : items) {
			
			if (item.getModuleToCheck() != null 
					&& !exportService.isExportModule(item.getModuleToCheck())) {
				continue;
			}

			Class<? extends AbstractWidget> klass = item.getClass();
			String name = klass.getSimpleName();
			
			String methodName = "process" + name;
			
			try {
				
				Method method = DataExportForm.class.getDeclaredMethod(methodName, 
							new Class[] {klass, 
										 String.class, 
										 String.class,
										 String.class,
										 Mapper.class, 
										 Object[].class});
				method.setAccessible(true);
				extra[0] = panelTab;
				panelLevel = (String) method.invoke(this, item, module, model, view, mapper, extra);
				extra[2] = panelLevel;
				
			} catch (NoSuchMethodException e) {
				log.debug("No method found: {}", methodName);
			}
			 catch (SecurityException| IllegalAccessException | IllegalArgumentException 
					| InvocationTargetException e) {
				 e.printStackTrace();
			}
		}
		
		return panelLevel;
	}
	
	
	@SuppressWarnings("unused")
	private String processPanel(Panel panel, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		String panelType = "panel";
		if ((boolean)extra[0]) {
			panelType = "paneltab";
		}
		
		String[] values = new String[DataCommon.HEADERS.length];
		values[DataCommon.MODULE] = module;
		values[DataCommon.MODEL] = model; 
		values[DataCommon.VIEW] =	view;
		values[DataCommon.NAME] = panel.getName(); 
		values[DataCommon.TITLE] =	panel.getTitle();
		values[DataCommon.TITLE_FR] =	translationService.getTranslation(panel.getTitle(), "fr");
		values[DataCommon.TYPE] =	panelType; 
		values[DataCommon.IF_CONFIG] = panel.getConditionToCheck();
		values[DataCommon.IF_MODULE] = DataExportService.getModuleToCheck(panel, (String) extra[3]);
		
		if (panel.getReadonly() != null && panel.getReadonly()) {
			values[DataCommon.READONLY] = "x";
		}
		else {
			values[DataCommon.READONLY] = panel.getReadonlyIf();
		}
		
		if (panel.getHidden() != null && panel.getHidden()) {
			values[DataCommon.HIDDEN] = "x";
		}
		else {
			values[DataCommon.HIDDEN] = panel.getHideIf();
		}
		
		values[DataCommon.SHOW_IF] = panel.getShowIf();
		
		if (panel.getColSpan() != null) {
			values[DataCommon.COLSPAN] = panel.getColSpan().toString();
		}
		
		String panelLevel = (String) extra[2];
		panelLevel = getPanelLevel(panelLevel);
		values[DataCommon.PANEL_LEVEL] = panelLevel;
		
		exportService.writeRow(values, newForm );
		
		newForm = false;
		
		extra[0] = false;
		extra[2] = panelLevel + ".-1";
		
		processItems(panel.getItems(), module, model, view, mapper, extra);
		
		return panelLevel;
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
	private String processPanelField(PanelField panelField, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		processField(panelField, module, model, view, mapper, extra);
		
		newForm = false; 
		
		if (panelField.getEditor() != null) {
			processPanelEditor(panelField, module, model, view, mapper, extra);
		}
		
		return (String) extra[2];
	}
	
	@SuppressWarnings("unused")
	private String processPanelTabs(PanelTabs panelTabs, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		String[] values = new String[DataCommon.HEADERS.length];
		values[DataCommon.MODULE] = module;
		values[DataCommon.MODEL] = model;
		values[DataCommon.VIEW] =	view; 
		values[DataCommon.TYPE] = "panelbook";
		values[DataCommon.IF_CONFIG] = panelTabs.getConditionToCheck();
		values[DataCommon.IF_MODULE] = DataExportService.getModuleToCheck(panelTabs, (String) extra[3]);
		
		if (panelTabs.getReadonly() != null && panelTabs.getReadonly()) {
			values[DataCommon.READONLY] = "x";
		}
		else {
			values[DataCommon.READONLY] = panelTabs.getReadonlyIf();
		}
		
		if (panelTabs.getHidden() != null && panelTabs.getHidden()) {
			values[DataCommon.HIDDEN] = "x";
		}
		else {
			values[DataCommon.HIDDEN] = panelTabs.getHideIf();
		}
		
		values[DataCommon.SHOW_IF] = panelTabs.getShowIf();
		
		Integer colspan = panelTabs.getColSpan();
		if (colspan != null) {
			values[DataCommon.COLSPAN] = colspan.toString();
		}
		
		String panelLevel = getPanelLevel((String) extra[2]);
		values[DataCommon.PANEL_LEVEL] = panelLevel;
		
		extra[0] = true;
		extra[2] = panelLevel + ".-1";

		log.debug("Exporting panel book view: {}", view);
		exportService.writeRow(values, newForm );
		
		processItems(panelTabs.getItems(), 
				module,
				model,
				view,
				mapper,
				extra);
		
		return panelLevel;
	}
	
	
	private String processField(Field field, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		String paneLevel = (String) extra[2];
		
		String name = field.getName();
		
		String[] values = new String[DataCommon.HEADERS.length];
		values[DataCommon.MODULE] = module;
		values[DataCommon.MODEL] = model;
		values[DataCommon.VIEW] =	view; 
		values[DataCommon.NAME] =	name; 
		values[DataCommon.TITLE] = field.getTitle();
		values[DataCommon.TITLE_FR] = translationService.getTranslation(field.getTitle(), "fr");
		values[DataCommon.IF_MODULE] = DataExportService.getModuleToCheck(field, (String) extra[3]);
		
		if (!name.contains(".")) {
			values[DataCommon.TYPE] = field.getServerType();
		}
		values[DataCommon.IF_CONFIG] = field.getConditionToCheck();
		
		String target = field.getTarget();
		
		Property property = mapper.getProperty(name);
		
		if (property != null) {
			addProperties(values, property, target);
		}
//		else {
//			newForm = false;
//			return paneLevel;
//		}
		
		if (target != null && values[DataCommon.TYPE] != null) {
			String[] targets = target.split("\\.");
			values[DataCommon.TYPE] =  values[DataCommon.TYPE] + "(" + targets[targets.length - 1] + ")";
		}
		
		if (field.getSelection() != null) {
			values[DataCommon.SELECT] = field.getSelection();
		}
		
		if (!Strings.isNullOrEmpty(values[DataCommon.SELECT])) {
			String[] selects = getSelect(values[DataCommon.SELECT]);
			if (selects != null) {
				values[DataCommon.SELECT] = selects[0];
				values[DataCommon.SELECT_FR] = selects[1];
			}
		}
		
		if (Strings.isNullOrEmpty(values[DataCommon.TYPE])) {
			values[DataCommon.TYPE] = "empty";
		}
		else {
			values[DataCommon.TYPE] = getType(values[DataCommon.TYPE], 
					field.getWidget(), values[DataCommon.SELECT]);
		}
		
		addExtraAttributes(field, values);
		
		@SuppressWarnings("unchecked")
		List<String> grid = (List<String>) extra[1];
		if (grid.contains(name)) {
			values[DataCommon.GRID] = grid.get(0);
		}
		
		exportService.writeRow(values, newForm );
		
		newForm = false;
		
		return paneLevel;

	}
	
	private void addProperties(String[] values, Property property, String target) {
		
		values[DataCommon.TYPE] = property.getType().name();
		
		if (values[DataCommon.TITLE] == null) {
			values[DataCommon.TITLE] = property.getTitle();
			values[DataCommon.TITLE_FR] = translationService.getTranslation(property.getTitle(), "fr");
		}
		
		values[DataCommon.SELECT] = property.getSelection();
		
		Class<?> targetClass = property.getTarget();
		if (targetClass != null) {
			target = targetClass.getName();
		}
		
		if (property.isRequired()) {
			values[DataCommon.REQUIRED] = "x";
		}
		
		if (property.isReadonly()) {
			values[DataCommon.READONLY] = "x";
		}
		
		if (property.isHidden()) {
			values[DataCommon.HIDDEN] = "x";
		}
		
		if (property.getHelp() != null) {
			String help = property.getHelp();
			if (!Boolean.parseBoolean(help)) {
				values[DataCommon.HELP] = property.getHelp();
				values[DataCommon.HELP_FR] = translationService.getTranslation(property.getHelp(), "fr");
			}
		}
		
	}
	
	private void addExtraAttributes(Field field , String[] values) {
		
		if (field.getRequired() != null && field.getRequired()) {
			values[DataCommon.REQUIRED] = "x";
		}
		else if (values[DataCommon.REQUIRED] == null) {
			values[DataCommon.REQUIRED] = field.getRequiredIf();
		}
		
		if (field.getReadonly() != null && field.getReadonly()) {
			values[DataCommon.READONLY] = "x";
		}
		else if(values[DataCommon.READONLY] == null) {
			values[DataCommon.READONLY] = field.getReadonlyIf();
		}
		
		if (field.getHidden() != null && field.getHidden()) {
			values[DataCommon.HIDDEN] = "x";
		}
		else if (values[DataCommon.HIDDEN] == null){
			values[DataCommon.HIDDEN] = field.getHideIf();
		}
		
		values[DataCommon.SHOW_IF] = field.getShowIf();
		
		if (field.getDomain() != null) {
			values[DataCommon.DOMAIN] = field.getDomain();
		}
		
		if (field.getOnChange() != null) {
			values[DataCommon.ON_CHANGE] = field.getOnChange();
		}
		
		if (field.getHelp() != null) {
			String help = field.getHelp();
			if (!Boolean.parseBoolean(help)) {
				values[DataCommon.HELP] = field.getHelp();
				values[DataCommon.HELP_FR] = translationService.getTranslation(field.getHelp(), "fr");
			}
		}
		
		if (field.getColSpan() != null) {
			values[DataCommon.COLSPAN] = field.getColSpan().toString();
		}
		
		values[DataCommon.WIDGET] = field.getWidget();
		
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
		
		String selectionEN = selection + "(" +  Joiner.on(",").join(select) + ")";
		String selectionFR = null;
		if (selectFR != null) {
			selectionFR = selection + "(" +  Joiner.on(",").join(selectFR) + ")";
		}
		
		return new String[]{selectionEN, selectionFR};
	}
	
	
	private String processPanelEditor(PanelField panelField, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		PanelEditor panelEditor = panelField.getEditor();
		
		model = model + "(" + panelField.getName() + ")";
		
		extra[0] = false;
		
		String target = panelField.getTarget();
		if (target != null) {
			newForm = true;
			try {
				Mapper targetMapper = Mapper.of(ClassUtils.findClass(target));
				processItems(panelEditor.getItems(),
						module,
						model,
						view,
						targetMapper, 
						extra);
			} catch(IllegalArgumentException e){
				log.debug("Model not found: {}", target);
			}
		}
		else {
			processItems(panelEditor.getItems(), 
					module,
					model,
					view,
					mapper, 
					extra);
		}
		
		return (String) extra[2];
	}
	
	@SuppressWarnings("unused")
	private String processPanelInclude(PanelInclude panelInclude, String module, String model, String view, Mapper mapper, Object[] extra){
		
		AbstractView panelView = panelInclude.getView();
		
		if  (view != null && panelView != null) {
			String name = panelView.getName();
			if (!exportService.isViewProcessed(name)) {
				extra[3] = DataExportService.getModuleToCheck(panelInclude, (String)extra[3]);
				return processForm((FormView) panelView, 
						module,
						model,
						view,
						mapper, 
						extra);
			}
		}
		else{
			log.debug("Issue in panel include: {}", panelInclude.getName());
		}
		
		return (String) extra[2];
	}
	
	
	@SuppressWarnings("unused")
	private String processButtonGroup(ButtonGroup buttonGroup, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		List<AbstractWidget> items = buttonGroup.getItems();
		
		if (items != null) {
			processItems(items, module, model, view, mapper, extra);
		}
		
		return (String) extra[2];
	}
	
	private String processButton(Button button, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		String[] values = new String[DataCommon.HEADERS.length];
		values[DataCommon.MODULE] = module;
		values[DataCommon.MODEL] =	model; 
		values[DataCommon.VIEW] =	view; 
		values[DataCommon.NAME] =	button.getName(); 
		values[DataCommon.TITLE] =	button.getTitle(); 
		values[DataCommon.TITLE_FR] =	translationService.getTranslation(button.getTitle(), "fr");
		values[DataCommon.TYPE] = "button"; 
		values[DataCommon.ON_CHANGE] = button.getOnClick();
		values[DataCommon.READONLY] = button.getReadonlyIf();
		values[DataCommon.HIDDEN] = button.getHideIf();
		values[DataCommon.SHOW_IF] = button.getShowIf();
		values[DataCommon.IF_CONFIG] = button.getConditionToCheck();
		values[DataCommon.IF_MODULE] = DataExportService.getModuleToCheck(button, (String)extra[3]);
		
		if (toolbar) {
			values[DataCommon.TYPE] = "button(toolbar)";
		}
		
		if (button.getColSpan() != null) {
			values[DataCommon.COLSPAN] = button.getColSpan().toString();
		}
		
		exportService.writeRow(values, newForm );
		
		newForm = false;
		
		return (String) extra[2];
	}
	
	@SuppressWarnings("unused")
	private String processPanelRelated(PanelRelated panelRelated, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		String[] values = new String[DataCommon.HEADERS.length];
		
		values[DataCommon.NAME] = panelRelated.getName();
		values[DataCommon.MODULE] = module;
		values[DataCommon.MODEL] = model;
		values[DataCommon.VIEW] = view;
		values[DataCommon.TITLE] = panelRelated.getTitle();
		values[DataCommon.TYPE] = panelRelated.getServerType();
		values[DataCommon.READONLY] = panelRelated.getReadonlyIf();
		values[DataCommon.HIDDEN] = panelRelated.getHideIf();
		values[DataCommon.SHOW_IF] = panelRelated.getShowIf();
		values[DataCommon.IF_CONFIG] = panelRelated.getConditionToCheck();
		values[DataCommon.IF_MODULE] = DataExportService.getModuleToCheck(panelRelated, (String)extra[3]);
		
		String target = panelRelated.getTarget();
		Property property = mapper.getProperty(values[DataCommon.NAME]);
		
		if (property != null) {
			values[DataCommon.TYPE] = property.getType().name();
			if (values[DataCommon.TITLE] == null) {
				values[DataCommon.TITLE]  = property.getTitle();
			}
			Class<?> targetClass = property.getTarget();
			if (targetClass != null) {
				target = targetClass.getName();
			}
		}
		else {
			log.debug("No property found: {}, class: {}", values[DataCommon.NAME], values[DataCommon.MODEL]);
		}
		
		values[DataCommon.TITLE_FR] = translationService.getTranslation(values[DataCommon.TITLE], "fr");
		if (values[DataCommon.TYPE] == null) {
			values[DataCommon.TYPE] = "o2m";
		}
			
		
		if (target != null) {
			view = view.split("\\(")[0];
			String parentView = view + "(" + values[DataCommon.TITLE] + ")"; 
			String form = panelRelated.getFormView();
			if (form != null && !exportService.isViewProcessed(form) && !form.equals(view)) {
				o2mViews.add(new String[]{target, form, panelRelated.getGridView(), parentView});
			}
			String[] targets = target.split("\\.");
			values[DataCommon.TYPE] = values[DataCommon.TYPE] + "(" + targets[targets.length - 1] + ")";
		}
		
		if (Strings.isNullOrEmpty(values[DataCommon.TYPE])) {
			values[DataCommon.TYPE] = "empty";
		}
		else {
			values[DataCommon.TYPE] = getType(values[DataCommon.TYPE], null, null);
		}
		
		if (panelRelated.getColSpan() != null) {
			values[DataCommon.COLSPAN] = panelRelated.getColSpan().toString();
		}
		
		String panelLevel = (String) extra[2];
		panelLevel = getPanelLevel(panelLevel);
		values[DataCommon.PANEL_LEVEL] = panelLevel;
		extra[2] = panelLevel;
		
		exportService.writeRow(values, newForm );
		
		newForm = false;
		
		extra[0] = false;
		
		return panelLevel;
	}
	
	@SuppressWarnings("unused")
	private String processLabel(Label label, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		String[] values = new String[DataCommon.HEADERS.length];
		values[DataCommon.MODULE] = module;
		values[DataCommon.MODEL] =	model; 
		values[DataCommon.VIEW] =	view; 
		values[DataCommon.NAME] =	label.getName();
		values[DataCommon.TITLE] =	label.getTitle();
		values[DataCommon.TITLE_FR] =	translationService.getTranslation(label.getTitle(), "fr");
		values[DataCommon.TYPE] =	"label"; 
		values[DataCommon.IF_CONFIG] = label.getConditionToCheck();
		values[DataCommon.HIDDEN] = label.getHideIf();
		values[DataCommon.IF_MODULE] = DataExportService.getModuleToCheck(label, (String)extra[3]);
		
		if (label.getColSpan() != null) {
			values[DataCommon.COLSPAN] = label.getColSpan().toString();
		}
		
		exportService.writeRow(values, newForm );
		
		newForm = false;
		
		return (String) extra[2];
	}
	
	@SuppressWarnings("unused")
	private String processDashlet(Dashlet dashlet, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		String[] values = exportDashboard.processDashlet(dashlet, module, model, view, mapper, (String)extra[3]);
		
		if (values != null) {
			exportService.writeRow(values, newForm );
			newForm = false;
		}
		
		return (String) extra[2];
	}
	
	@SuppressWarnings("unused")
	private String processItem(Item item, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		String[] values = new String[DataCommon.HEADERS.length];
		values[DataCommon.MODULE] = module;
		values[DataCommon.MODEL] = model; 
		values[DataCommon.VIEW] =	view; 
		values[DataCommon.NAME] = item.getName();
		values[DataCommon.TITLE] = item.getTitle();
		values[DataCommon.TITLE_FR] =	translationService.getTranslation(item.getTitle(), "fr");
		values[DataCommon.TYPE] = "menubar.item";
		values[DataCommon.ON_CHANGE] = item.getAction();
		values[DataCommon.READONLY] = item.getReadonlyIf();
		values[DataCommon.HIDDEN] = item.getHideIf();
		values[DataCommon.SHOW_IF] = item.getShowIf();
		values[DataCommon.IF_CONFIG] = item.getConditionToCheck();
		values[DataCommon.IF_MODULE] = DataExportService.getModuleToCheck(item, (String)extra[3]);
		if (item.getHidden() != null && item.getHidden()) {
			values[DataCommon.HIDDEN] = "x";
		}
		
		exportService.writeRow(values, newForm );
		
		newForm = false;
		
		return (String) extra[2];
	}
	
	@SuppressWarnings("unused")
	private String processSpacer(Spacer spacer, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		String[] values = new String[DataCommon.HEADERS.length];
		values[DataCommon.MODULE] = module;
		values[DataCommon.MODEL] = model; 
		values[DataCommon.VIEW] =	view; 
		values[DataCommon.TYPE] = "spacer";
		values[DataCommon.IF_CONFIG] = spacer.getConditionToCheck();
		values[DataCommon.IF_MODULE] = DataExportService.getModuleToCheck(spacer, (String)extra[3]);
		
		Integer colSpan =  spacer.getColSpan();
		if (colSpan != null) {
			values[DataCommon.COLSPAN] = colSpan.toString();
		}
		
		exportService.writeRow(values, newForm );
		
		newForm = false;
		
		return (String) extra[2];
	}
	
	private String getType(String type, String widget, String select) {
		
		if (DataCommon.FIELD_TYPES.containsKey(type) || DataCommon.VIEW_ELEMENTS.containsKey(type)) {
			return type;
		}
		
		String[] types = type.split("\\(");
		
		if (DataCommon.FIELD_TYPES.containsKey(types[0]) || DataCommon.VIEW_ELEMENTS.containsKey(types[0])) {
			return types[0];
		}
		
		types[0] = types[0].replace("-", "_");
		
		switch (types[0].toUpperCase()) {
			case "INTEGER":
				if (!Strings.isNullOrEmpty(select)) {
					if (widget != null && widget.equals("multiselect")) {
						return "multiselect(int)";
					}
					return "select(int)";
				}
				if (widget != null && widget.equals("duration")) {
					return "duration";
				}
				return "int";
			case "DECIMAL":
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
					if (widget != null && widget.equals("multiselect")) {
						return "multiselect(char)";
					}
					return "select(char)";
				}
				return "char";
		}
		
		return type;
	}
}
