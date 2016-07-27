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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.common.ClassUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.actions.Action;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.View;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.meta.schema.views.AbstractWidget;
import com.axelor.meta.schema.views.Button;
import com.axelor.meta.schema.views.ButtonGroup;
import com.axelor.meta.schema.views.Dashboard;
import com.axelor.meta.schema.views.Dashlet;
import com.axelor.meta.schema.views.Field;
import com.axelor.meta.schema.views.FormView;
import com.axelor.meta.schema.views.GridView;
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
import com.axelor.studio.service.ViewLoaderService;
import com.axelor.studio.service.data.DataCommonService;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class DataXmlService extends DataCommonService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final List<String> SUPPORTED_TYPES = Arrays.asList(new String[]{"form", "dashboard", "grid"});
	
	private List<String> viewProcessed = new  ArrayList<String>(); 
	
	private List<String[]> o2mViews = new ArrayList<String[]>();

	private boolean newForm = false;
	
	private boolean toolbar = false;
	
	@Inject
	private MetaViewRepository metaViewRepo;
	
	private DataExportService exportService;
	
	@Inject
	public DataXmlService(DataExportService exportService) {
		this.exportService = exportService;
	}
	
	public void processAction(String model, MetaAction action) {
		
		o2mViews = new ArrayList<String[]>();
		
		Map<String, String> views = new HashMap<String, String>();
		
		try {
			
			ObjectViews objectViews = XMLViews.fromXML(action.getXml());
			ActionView actionView = (ActionView) objectViews.getActions().get(0);
			
			for (View view : actionView.getViews()) {
				String type = view.getType();
				String name = view.getName();
				if  (SUPPORTED_TYPES.contains(type)) {
					views.put(type, name);
				}
			}
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		processModel(model, views);
		
	}
	
	private void processModel(String model, Map<String, String> views) {
		
		try{
			
			MetaView metaView = null;
			Mapper mapper = null;
			if (views.containsKey("dashboard")) {
				String dashboard = views.get("dashboard");
				if (dashboard != null) {
					metaView =  metaViewRepo.all().filter(
							"self.type = 'dashboard'  and self.name = ?", 
							dashboard).fetchOne();
					
					processView(metaView, mapper, "dashboard", null);
				}
				
			}
			else if (model != null && !viewProcessed.contains(views.get("form"))) {
				mapper = Mapper.of(ClassUtils.findClass(model));
				MetaView form = getMetaView(model, "form", views.get("form"));
				MetaView grid = getMetaView(model, "grid", views.get("grid"));
				processView(form, mapper, "form", getGridFields(grid));
				addO2MViews(model);
			}
			
			o2mViews = new ArrayList<String[]>();
		}
		catch (IllegalArgumentException e) {
			log.debug("Model not found: {}", model);
		}
	}
	
	private List<String> getGridFields(MetaView view) {
		
		List<String> fields = new ArrayList<String>();
		
		if (view == null) {
			return fields;
		}
		
		try {
			ObjectViews views = XMLViews.fromXML(view.getXml());
			
			GridView gridView = (GridView) views.getViews().get(0);
			
			String name = view.getName();
			String defaultName = ViewLoaderService.getDefaultViewName(view.getModel(), "grid");
			
			if (name.equals(defaultName)) {
				fields.add("x");
			}
			else {
				fields.add(name);
			}
			
			for (AbstractWidget item : gridView.getItems()) {
				if (item instanceof Field) {
					fields.add(((Field) item).getName());
				}
			}
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		return fields;
	}
	

	private MetaView getMetaView(String model, String type, String name) {
		
		if (name == null) {
			name = ViewLoaderService.getDefaultViewName(model, type);
		}
		MetaView view =  metaViewRepo.all().filter(
				"self.type = ? and self.model = ? and self.name = ?", 
				 type, model, name).fetchOne();
		
		return view;
		
	}
	
	private void processView(MetaView view , Mapper mapper, String type, List<String> grid) {
		
		if (view ==  null) {
			return;
		}
		
		String name = view.getName();
		if (viewProcessed.contains(name)) {
			return;
		}
			
		try {
			ObjectViews views = XMLViews.fromXML(view.getXml());
			
			/**
			 * 1. add new panel.
			 * 2. is panelTab.
			 * 3. grid list.
			 * 4. panel level.
			 */
			Object[] extra = new Object[]{true, false, grid, null};
			
			switch (type) {
			case "form":
				FormView form = (FormView) views.getViews().get(0);
				newForm = true;
				String model = mapper.getBeanClass().getSimpleName();
				String viewName = form.getName() + "(" + form.getTitle() + ")";
				processForm(form, view.getModule(), model, viewName, mapper, extra);
				break;
			case "dashboard":
				if(!exportService.getOnlyPanel()){
					Dashboard dashboard = (Dashboard) views.getViews().get(0);
					processDashboard(dashboard, view.getModule(), extra);
				}
				break;
			}
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
	}
	
	private void addO2MViews(String model) {
		List<String[]> views = new ArrayList<String[]>();
		views.addAll(o2mViews);
		o2mViews.clear();
		
		for (String[] view : views) {
			if (viewProcessed.contains(view[1])) {
				continue;
			}
			
			if (view[3] != null) {
				exportService.setMenuPath(view[3]);
			}
			
			Map<String,String> viewMap = new HashMap<String, String>();
			viewMap.put("form", view[1]);
			viewMap.put("grid", view[2]);
			processModel(view[0], viewMap);
			exportService.setMenuPath(null);
		}
				
		
	}
	
	private String processForm(FormView form, String module, String model, String view,  Mapper mapper, Object[] extra) {
		
		toolbar = false;
		
		String panelLevel = (String) extra[3];
		
//		String view = form.getName() + "(" + form.getTitle() + ")";
		log.debug("Processing form: {}", view);
		
		viewProcessed.add(form.getName());
		
		if (!exportService.getOnlyPanel()) {
			
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
		}
			
		panelLevel = processItems(form.getItems(), module, model, view, mapper, extra);
		
		
		
		return panelLevel;
	}
	
	
	private void addEvent(String module, String model, String view, String type, String formula) {
		
		String[] values = new String[exportService.columns];
		values[MODULE] = module; 
		values[MODEL] = model; 
		values[VIEW] = view; 
	    values[TYPE] = type;
	    values[FORMULA] = formula;
	    
	    exportService.writeRow(values, newForm, false, false);
	    
	    newForm = false;
	}

	private void processMenuBarMenu(List<Menu> menubar, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		if (menubar == null) {
			return;
		}
		
		for (Menu menu : menubar) {
			String title = menu.getTitle();
			
			String[] values = new String[exportService.columns];
			values[MODULE] = getModuleName(menu, module); 
			values[MODEL] = model; 
			values[VIEW] = view; 
		    values[TITLE] = title;
		    values[TYPE] = "menubar"; 
		
			exportService.writeRow(values, newForm, false, false);
			newForm = false;
			
			processItems(menu.getItems(), module, model, view, mapper, extra);
		}
	}
	
	private void processDashboard(Dashboard dashboard, String module, Object[] extra) {
		
		processItems(dashboard.getItems(), module, null, dashboard.getName(), null, extra);
		
		String name = dashboard.getName();

		viewProcessed.add(name);
	}
	
	private String getModuleName(AbstractWidget item, String module) {
		
		String moduleName = item.getModuleToCheck();
		
		if  (Strings.isNullOrEmpty(moduleName)) {
			moduleName = module;
		}
		
		return moduleName;
	}
	
	private String processItems(List<AbstractWidget> items, String module, String model, String view, Mapper mapper, Object[] extra){
		
		String panelLevel = (String) extra[3];
		if (items == null) {
			return panelLevel;
		}
		
		for (AbstractWidget item : items) {
			
			Class<? extends AbstractWidget> klass = item.getClass();
			String name = klass.getSimpleName();
			
			if(exportService.getOnlyPanel() 
					&& !name.equals("Panel") 
					&& !name.equals("PanelTabs") 
					&& !name.equals("PanelInclude")) {
				return panelLevel;
			}
			String methodName = "process" + name;
			
			try {
				
				Method method = getClass().getDeclaredMethod(methodName, 
							new Class[] {klass, 
										 String.class, 
										 String.class,
										 String.class,
										 Mapper.class, 
										 Object[].class});
				method.setAccessible(true);
				panelLevel = (String) method.invoke(this, item, module, model, view, mapper, extra);
				extra[3] = panelLevel;
				
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
		if ((boolean)extra[1]) {
			panelType = "paneltab";
		}
		
		String[] values = new String[exportService.columns];
		values[MODULE] = getModuleName(panel, module); 
		values[MODEL] = model; 
		values[VIEW] =	view;
		values[NAME] = panel.getName(); 
		values[TITLE] =	panel.getTitle();
		values[TYPE] =	panelType; 
		
		if (panel.getColSpan() != null) {
			values[COLSPAN] = panel.getColSpan().toString();
		}
		
		String panelLevel = (String) extra[3];
		panelLevel = getPanelLevel(panelLevel);
		values[PANEL_LEVEL] = panelLevel;
		
		exportService.writeRow(values, newForm, true, false);
		
		newForm = false;
		
		extra[0] = false;
		extra[1] = false;
		extra[3] = panelLevel + ".-1";
		
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
		
		return (String) extra[3];
	}
	
	@SuppressWarnings("unused")
	private String processPanelTabs(PanelTabs panelTabs, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		String[] values = new String[exportService.columns];
		values[MODULE] = getModuleName(panelTabs, module);
		values[MODEL] = model;
		values[VIEW] =	view; 
		values[TYPE] = "panelbook";
		
		Integer colspan = panelTabs.getColSpan();
		if (colspan != null) {
			values[COLSPAN] = colspan.toString();
		}
		
		String panelLevel = getPanelLevel((String) extra[3]);
		values[PANEL_LEVEL] = panelLevel;
		
		extra[0] = true;
		extra[1] = true;
		extra[3] = panelLevel + ".-1";

		log.debug("Exporting panel book view: {}", view);
		exportService.writeRow(values, newForm, false, false);
		
		processItems(panelTabs.getItems(), 
				getModuleName(panelTabs, module), 
				model,
				view,
				mapper,
				extra);
		
		return panelLevel;
	}
	
	
	private String processField(Field field, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		String paneLevel = (String) extra[3];
		
		String name = field.getName();
		if (name.contains(".")) {
			return paneLevel;
		}
		
		String[] values = new String[exportService.columns];
		values[MODULE] = getModuleName(field, module);
		values[MODEL] = model;
		values[VIEW] =	view; 
		values[NAME] =	name; 
		values[TITLE] = field.getTitle();
		values[TYPE] = field.getServerType();
		
		String target = field.getTarget();
		
		Property property = mapper.getProperty(name);
		
		if (property != null) {
			addProperties(values, property, target);
		}
		
		if (target != null && values[TYPE] != null) {
			String[] targets = target.split("\\.");
			values[TYPE] =  values[TYPE] + "(" + targets[targets.length - 1] + ")";
		}
		
		if (field.getSelection() != null) {
			values[SELECT] = field.getSelection();
		}
		
		if (values[SELECT] != null) {
			values[SELECT] = getSelect(values[SELECT]);
		}
		
		if (Strings.isNullOrEmpty(values[TYPE])) {
			values[TYPE] = "empty";
		}
		else {
			values[TYPE] = getType(values[TYPE], 
					field.getWidget(), values[SELECT]);
		}
		
		addExtraAttributes(field, values);
		
		@SuppressWarnings("unchecked")
		List<String> grid = (List<String>) extra[2];
		if (grid.contains(name)) {
			values[GRID] = grid.get(0);
		}
		
		exportService.writeRow(values, newForm, false, false);
		
		newForm = false;
		
		return paneLevel;

	}
	
	private void addProperties(String[] values, Property property, String target) {
		
		values[TYPE] = property.getType().name();
		
		if (values[TITLE] == null) {
			values[TITLE] = property.getTitle();
		}
		
		values[SELECT] = property.getSelection();
		
		Class<?> targetClass = property.getTarget();
		if (targetClass != null) {
			target = targetClass.getName();
		}
		
		if (property.isRequired()) {
			values[REQUIRED] = "x";
		}
		
		if (property.isReadonly()) {
			values[READONLY] = "x";
		}
		
		if (property.isHidden()) {
			values[HIDDEN] = "x";
		}
		
		if (property.getHelp() != null) {
			values[HELP] = property.getHelp();
		}
		
	}
	
	private void addExtraAttributes(Field field , String[] values) {
		
		if (field.getRequired() != null && field.getRequired()) {
			values[REQUIRED] = "x";
		}
		if (field.getRequiredIf() != null) {
			values[REQUIRED_IF] = field.getRequiredIf();
		}
		
		if (field.getReadonly() != null && field.getReadonly()) {
			values[READONLY] = "x";
		}
		if (field.getReadonlyIf() != null) {
			values[READONLY_IF] = field.getReadonlyIf();
		}
		
		if (field.getHidden() != null && field.getHidden()) {
			values[HIDDEN] = "x";
		}
		if (field.getHideIf() != null) {
			values[HIDE_IF] = field.getHideIf();
		}
		
		if (field.getDomain() != null) {
			values[DOMAIN] = field.getDomain();
		}
		
		if (field.getOnChange() != null) {
			values[ON_CHANGE] = field.getOnChange();
		}
		
		if (field.getHelp() != null) {
			values[HELP] = field.getHelp();
		}
		
		if (field.getColSpan() != null) {
			values[COLSPAN] = field.getColSpan().toString();
		}
		
	}
	
	private String getSelect(String selection) {
		
		List<Option> selectionList = MetaStore.getSelectionList(selection);
		if (selectionList == null) {
			return "";
		}
		
		List<String> titles = new ArrayList<String>();
		for (Option option : selectionList) {
			titles.add(option.getValue() + ":" + option.getTitle());
		}
		
		return selection + "(" +  Joiner.on(",").join(titles) + ")";
	}
	
	
	private String processPanelEditor(PanelField panelField, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		PanelEditor panelEditor = panelField.getEditor();
		
		model = model + "(" + panelField.getName() + ")";
		
		extra[0] = false;
		extra[1] = false;
		
		String target = panelField.getTarget();
		if (target != null) {
			newForm = true;
			try {
				Mapper targetMapper = Mapper.of(ClassUtils.findClass(target));
				processItems(panelEditor.getItems(),
						getModuleName(panelField, module), 
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
					getModuleName(panelField, module), 
					model,
					view,
					mapper, 
					extra);
		}
		
		return (String) extra[3];
	}
	
	@SuppressWarnings("unused")
	private String processPanelInclude(PanelInclude panelInclude, String module, String model, String view, Mapper mapper, Object[] extra){
		
		AbstractView panelView = panelInclude.getView();
		
		if  (view != null && panelView != null) {
			String name = panelView.getName();
			if (!viewProcessed.contains(name)) {
				return processForm((FormView) panelView, 
						getModuleName(panelInclude, module),
						model,
						view,
						mapper, 
						extra);
			}
		}
		else{
			log.debug("Issue in panel include: {}", panelInclude.getName());
		}
		
		return (String) extra[3];
	}
	
	
	@SuppressWarnings("unused")
	private String processButtonGroup(ButtonGroup buttonGroup, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		List<AbstractWidget> items = buttonGroup.getItems();
		
		if (items != null) {
			processItems(items, module, model, view, mapper, extra);
		}
		
		return (String) extra[3];
	}
	
	private String processButton(Button button, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		String[] values = new String[exportService.columns];
		values[MODULE] = getModuleName(button, module); 
		values[MODEL] =	model; 
		values[VIEW] =	view; 
		values[NAME] =	button.getName(); 
		values[TITLE] =	button.getTitle(); 
		values[TYPE] = "button"; 
		
		if (toolbar) {
			values[TYPE] = "button(toolbar)";
		}
		
		if (button.getOnClick() != null) {
			values[ON_CLICK] = button.getOnClick();
		}
		
		if (button.getReadonlyIf() != null) {
			values[READONLY_IF] = button.getReadonlyIf();
		}
		
		if (button.getHideIf() != null) {
			values[HIDE_IF] = button.getHideIf();
		}
		
		if (button.getColSpan() != null) {
			values[COLSPAN] = button.getColSpan().toString();
		}
		
		exportService.writeRow(values, newForm, false, false);
		
		newForm = false;
		
		return (String) extra[3];
	}
	
	@SuppressWarnings("unused")
	private String processPanelRelated(PanelRelated panelRelated, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		String[] values = new String[exportService.columns];
		
		values[NAME] = panelRelated.getName();
		values[MODULE] = getModuleName(panelRelated, module); 
		values[MODEL] = model;
		values[VIEW] = view;
		values[TITLE] = panelRelated.getTitle();
		values[TYPE] = panelRelated.getServerType();
		
		String target = panelRelated.getTarget();
		Property property = mapper.getProperty(values[3]);
		
		if (property != null) {
			values[TYPE] = property.getType().name();
			if (values[TITLE] == null) {
				values[TITLE]  = property.getTitle();
			}
			Class<?> targetClass = property.getTarget();
			if (targetClass != null) {
				target = targetClass.getName();
			}
		}
		else {
			log.debug("No property found: {}, class: {}", values[3], values[1]);
		}
		
		if (values[TYPE] == null) {
			values[TYPE] = "o2m";
		}
			
		
		if (target != null) {
			view = view.split("\\(")[0];
			String parentView = view + "(" + values[TITLE] + ")"; 
			String form = panelRelated.getFormView();
			if (form != null && !viewProcessed.contains(form) && !form.equals(view)) {
				o2mViews.add(new String[]{target, form, panelRelated.getGridView(), parentView});
			}
			String[] targets = target.split("\\.");
			values[TYPE] = values[TYPE] + "(" + targets[targets.length - 1] + ")";
		}
		
		if (Strings.isNullOrEmpty(values[TYPE])) {
			values[TYPE] = "empty";
		}
		else {
			values[TYPE] = getType(values[TYPE], null, null);
		}
		
		if (panelRelated.getColSpan() != null) {
			values[COLSPAN] = panelRelated.getColSpan().toString();
		}
		
//		values[PANEL_LEVEL] =  getPanelLevel((String) extra[3]);
		
		exportService.writeRow(values, newForm, false, false);
		
		newForm = false;
		
		extra[1] = false;
		
		return (String) extra[3];
	}
	
	@SuppressWarnings("unused")
	private String processLabel(Label label, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		String[] values = new String[exportService.columns];
		values[MODULE] = getModuleName(label, module); 
		values[MODEL] =	model; 
		values[VIEW] =	view; 
		values[NAME] =	label.getName();
		values[TITLE] =	label.getTitle();
		values[TYPE] =	"label"; 
		
		if (label.getColSpan() != null) {
			values[COLSPAN] = label.getColSpan().toString();
		}
		
		exportService.writeRow(values, newForm, false, false);
		
		newForm = false;
		
		return (String) extra[3];
	}
	
	@SuppressWarnings("unused")
	private String processDashlet(Dashlet dashlet, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		String title = dashlet.getTitle();
		String action = dashlet.getAction();
		
		if (title == null && action != null) {
			Action metaAction = MetaStore.getAction(dashlet.getAction());
			if (metaAction instanceof ActionView) {
				title = ((ActionView) metaAction).getTitle();
			}
		}
		
		if (title != null || dashlet.getName() != null) {
			String[] values = new String[exportService.columns];
			values[MODULE] = getModuleName(dashlet, module); 
			values[MODEL] =	null; 
			values[VIEW] =	view; 
			values[NAME] = dashlet.getName(); 
			values[TITLE] = title;
			values[TYPE] =	"dashlet"; 
		
			if (dashlet.getColSpan() != null) {
				values[COLSPAN] = dashlet.getColSpan().toString();
			}
			exportService.writeRow(values, newForm, false, false);
			newForm = false;
		}
		
		return (String) extra[3];
	}
	
	@SuppressWarnings("unused")
	private String processItem(Item item, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		String[] values = new String[exportService.columns];
		values[MODULE] = getModuleName(item, module); 
		values[MODEL] = model; 
		values[VIEW] =	view; 
		values[NAME] = item.getName();
		values[TITLE] = item.getTitle();
		values[TYPE] = "menubar.item";
		values[ON_CLICK] = item.getAction();
		values[READONLY_IF] = item.getReadonlyIf();
		values[HIDE_IF] = item.getHideIf();
		if (item.getHidden() != null && item.getHidden()) {
			values[HIDDEN] = "x";
		}
		
		exportService.writeRow(values, newForm, false, false);
		
		newForm = false;
		
		return (String) extra[3];
	}
	
	@SuppressWarnings("unused")
	private String processSpacer(Spacer spacer, String module, String model, String view, Mapper mapper, Object[] extra) {
		
		String[] values = new String[exportService.columns];
		values[MODULE] = getModuleName(spacer, module); 
		values[MODEL] = model; 
		values[VIEW] =	view; 
		values[TYPE] = "spacer";
		
		Integer colSpan =  spacer.getColSpan();
		if (colSpan != null) {
			values[COLSPAN] = colSpan.toString();
		}
		
		exportService.writeRow(values, newForm, false, false);
		
		newForm = false;
		
		return (String) extra[3];
	}
	
	private String getType(String type, String widget, String select) {
		
		if (fieldTypes.containsKey(type) || viewElements.containsKey(type)) {
			return type;
		}
		
		String[] types = type.split("\\(");
		
		if (fieldTypes.containsKey(types[0])) {
			return type;
		}
		
		if (viewElements.containsKey(types[0])) {
			return type;
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
				return "o2m" + "(" + types[1];
			case "MANY_TO_ONE":
				return "m2o" + "(" + types[1];
			case "ONE_TO_ONE":
				return "o2o" + "(" + types[1];
			case "MANY_TO_MANY":
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
