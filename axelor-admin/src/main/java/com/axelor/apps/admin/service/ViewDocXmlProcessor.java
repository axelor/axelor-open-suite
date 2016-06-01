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
package com.axelor.apps.admin.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.common.ClassUtils;
import com.axelor.common.Inflector;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
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
import com.axelor.meta.schema.views.Label;
import com.axelor.meta.schema.views.Menu;
import com.axelor.meta.schema.views.Menu.Item;
import com.axelor.meta.schema.views.Panel;
import com.axelor.meta.schema.views.PanelEditor;
import com.axelor.meta.schema.views.PanelField;
import com.axelor.meta.schema.views.PanelInclude;
import com.axelor.meta.schema.views.PanelRelated;
import com.axelor.meta.schema.views.PanelTabs;
import com.axelor.meta.schema.views.Selection;
import com.axelor.meta.schema.views.Selection.Option;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class ViewDocXmlProcessor {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private ViewDocExportService exportService;
	
	private Map<String, Map<String,List<String>>> itemCheckMap = new HashMap<String, Map<String, List<String>>>();
	
	private List<String> viewProcessed = new  ArrayList<String>(); 
	
	private Map<String, List<String[]>> o2mViewMap = new HashMap<String, List<String[]>>();
	
	private Inflector inflector;
	
	@Inject
	private MetaViewRepository metaViewRepo;
	
	@Inject
	public ViewDocXmlProcessor(ViewDocExportService exportService){
		inflector = Inflector.getInstance();
		this.exportService = exportService;
	}
	
	protected void processModel(String model, String[] view){
		
		try{
			
			if(view == null || (view[0].equals("form") && view[1] == null)){
				view = new String[]{"form", getFormName(model)};
			}
			
			if(viewProcessed.contains(view[1])){
				return;
			}
			
			if(view[1] == null){
				log.debug("No view name for type: {}, model:{}", view[0], model);
				return;
			}
			
			List<MetaView> metaViews = null;
			Mapper mapper = null;
			if(model != null){
				mapper = Mapper.of(ClassUtils.findClass(model));
				metaViews =  metaViewRepo.all().filter(
						"self.type = ? and self.model = ? and self.name = ?", 
						view[0], model, view[1]).fetch();
				
				if(!itemCheckMap.containsKey(view[1])){
					Map<String, List<String>> map = new HashMap<String, List<String>>();
					itemCheckMap.put(view[1], map);
				}
			}
			else{
				metaViews =  metaViewRepo.all().filter(
						"self.type = ? and self.name = ?", 
						view[0], view[1]).fetch();
				
			}
			
			if(metaViews.isEmpty()){
				log.debug("No view found: {}, model: {}", view[1], model);
			}
			else{
				processView(metaViews.iterator(), mapper, view[0]);
				
				addO2MViews(model);
			}
			
			o2mViewMap = new HashMap<String, List<String[]>>();
		}
		catch(IllegalArgumentException e){
			log.debug("Model not found: {}", model);
		}
	}
	
	protected void processModel(String model, MetaAction action){
		
		String[] viewName = getViewName(action);
		
		if(viewName != null || model != null){
			processModel(model, getViewName(action));
		}
		else{
			log.debug("No model or view for action: {}", action.getName());
		}
	}
	
	
	private boolean isChecked(String view, String type, String item){
		
		if(!itemCheckMap.containsKey(view)){
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			map.put(type, new ArrayList<String>());
			itemCheckMap.put(view, map);
		}
		
		Map<String, List<String>> map = itemCheckMap.get(view);
		if(!map.containsKey(type)){
			map.put(type, new ArrayList<String>());
		}
		
		List<String> checkList = map.get(type);
		
		if(!checkList.contains(item)){
			checkList.add(item);
			return false;
		}
		else{
			return true;
		}
	}
	
	private String getFormName(String model){
		
		String viewName = model.substring(model.lastIndexOf(".")+1);
		viewName = inflector.underscore(viewName);
		viewName = inflector.dasherize(viewName);
		
		return viewName + "-form";
	}
	
	private String[] getViewName(MetaAction action){
		
		try {
			ObjectViews objectViews = XMLViews.fromXML(action.getXml());
			ActionView actionView = (ActionView) objectViews.getActions().get(0);
			for(View view : actionView.getViews()){
				String type = view.getType();
				String name = view.getName();
				if(type.equals("form") || type.equals("dashboard")){
					return new String[]{type, name};
				}
			}
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void processView(Iterator<MetaView> viewIter, Mapper mapper, String type){
		
		if(!viewIter.hasNext()){
			return;
		}
		
		MetaView view = viewIter.next();
		String name = view.getName();
		
		if(!viewProcessed.contains(name)){
			
			try {
				ObjectViews views = XMLViews.fromXML(view.getXml());
				
				switch(type){
					case "form":
						FormView form = (FormView) views.getViews().get(0);
						processForm(form, view.getModule(), mapper, true);
						break;
					case "dashboard":
						if(!exportService.getOnlyPanel()){
							Dashboard dashboard = (Dashboard) views.getViews().get(0);
							processDashboard(dashboard, view.getModule());
						}
						break;
				}
				
			} catch (JAXBException e) {
				e.printStackTrace();
			}
		}
		
		processView(viewIter, mapper, type);
	}
	
	private void addO2MViews(String model){
		
		o2mViewMap.remove(model);
		
		Set<Entry<String, List<String[]>>> entrySet = new HashSet<Map.Entry<String,List<String[]>>>();
		entrySet.addAll(o2mViewMap.entrySet());
		
		for(Entry<String, List<String[]>> entry : entrySet){
			List<String[]> views = entry.getValue();
			String key = entry.getKey();
			
			for(String[] view : views){
				if(view[0] != null){
					exportService.setMenuPath(view[0].split(","));
				}
				if(view[1] == null){
					view[1] = getFormName(key);
				}
				
				processModel(key, new String[]{"form",view[1]});
				String[] menuPath = exportService.getMenuPath();
				if(menuPath != null){
					exportService.setMenuPath(new String[]{"",""});
				}
			}
		}
		
	}
	
	private void processForm(FormView form, String module, Mapper mapper, boolean addPanel){
		
		String name = form.getName();
		log.debug("Processing form: {}", name);
		
		if(!exportService.getOnlyPanel()){
			List<Button> buttons = form.getToolbar();
			if(buttons != null){
				for(Button button : buttons){
					processButton(button, name, module, mapper, false);
				}
			}
		
			List<Menu> menus = form.getMenubar();
			if(menus != null){
				processMenuBarMenu(menus.iterator(), name, module, mapper);
			}
		}
			
		List<AbstractWidget> items = form.getItems();
		if(items != null){
			processItems(items.iterator(), name, module, mapper, addPanel);
		}
		
		viewProcessed.add(name);
	}
	
	
	private void processMenuBarMenu(Iterator<Menu> menuIter, String view, String module, Mapper mapper){
		
		if(!menuIter.hasNext()){
			return;
		}
		
		Menu menu = menuIter.next();
		
		String className = mapper.getBeanClass().getName();
		String title = menu.getTitle();

		if(!Strings.isNullOrEmpty(title) && !isChecked(view, "menu", title)){
			String[] values = new String[]{
					getModuleName(menu, module), 
					className, 
					view, "Toolbar Menu", 
					"", 
					exportService.translate(title, "en"), 
					exportService.translate(title, "fr"),
					"",
					""
			};
		
			exportService.writeRow(values);
		}
		
		List<AbstractWidget> items = menu.getItems();
		if(items != null){
			processItems(items.iterator(), view, module, mapper, false);
		}
		
		processMenuBarMenu(menuIter, view, module, mapper);
	}
	
	private void processDashboard(Dashboard dashboard, String module){
		
		List<AbstractWidget> items = dashboard.getItems();
		if(items != null){
			processItems(items.iterator(), dashboard.getName(), module, null, false);
		}
		
		String name = dashboard.getName();
//		if(!itemCheckMap.containsKey(name)){
//			Map<String, List<String>> map = new HashMap<String, List<String>>();
//			itemCheckMap.put(name, map);
//		}
		viewProcessed.add(name);
	}
	
	private String getModuleName(AbstractWidget item, String module){
		
		String moduleName = item.getModuleToCheck();
		
		if(Strings.isNullOrEmpty(moduleName)){
			moduleName = module;
		}
		
		return moduleName;
	}
	
	private void processItems(Iterator<AbstractWidget> itemIter, String view, String module, Mapper mapper, boolean addPanel){
		
		if(!itemIter.hasNext()){
			return;
		}
		
		AbstractWidget item = itemIter.next();
		
		processElement(item, view, module, mapper, addPanel);
		
		processItems(itemIter, view, module, mapper, addPanel);
	}

	
	private void processElement(AbstractWidget item, String view,
			String module, Mapper mapper, boolean addPanel) {
		
		Class<? extends AbstractWidget> klass = item.getClass();
		String name = klass.getSimpleName();
		if(exportService.getOnlyPanel() 
				&& !name.equals("Panel") 
				&& !name.equals("PanelTabs") 
				&& !name.equals("PanelInclude")){
			return;
		}
		String methodName = "process" + name;
		
		try {
			Method method = getClass().getDeclaredMethod(methodName, 
						new Class[] {klass, String.class, String.class, Mapper.class, boolean.class});
			method.setAccessible(true);
			method.invoke(this, new Object[]{item, view, module, mapper, addPanel});
		} catch (NoSuchMethodException | SecurityException
				| IllegalAccessException | IllegalArgumentException 
				| InvocationTargetException e) {
			log.debug("No method found: {}", methodName);
//			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private void processPanel(Panel panel, String view, String module, Mapper mapper, boolean addPanel){
		
		if(addPanel){
			String className = mapper.getBeanClass().getName();
			String title = panel.getTitle();
	
			if(!Strings.isNullOrEmpty(title) && !isChecked(view, "panel", title)){
				String[] values = new String[]{
						getModuleName(panel, module), 
						className, 
						view, "Panel", 
						panel.getName(), 
						exportService.translate(title, "en"), 
						exportService.translate(title, "fr"),
						"",
						""
				};
	
				exportService.writeRow(values);
			}
		}
		
		processItems(panel.getItems().iterator(), view, module, mapper, false);
	}
	
	@SuppressWarnings("unused")
	private void processPanelField(PanelField panelField, String view, String module, Mapper mapper, boolean addPanel){
		
		processField(panelField, view, module, mapper, addPanel);
		
		processPanelEditor(panelField, view, module, mapper);
	}
	
	@SuppressWarnings("unused")
	private void processPanelTabs(PanelTabs panelTabs, String view, String module, Mapper mapper, boolean addPanel){

		processItems(panelTabs.getItems().iterator(), view, getModuleName(panelTabs, module), mapper, true);
	}
	
	
	private void processField(Field field, String view, String module, Mapper mapper, boolean addPanel){
		
		String name = field.getName();
		if(name.contains(".")){
			return;
		}
		String title = field.getTitle();
		String className = mapper.getBeanClass().getName();
		
		if(isChecked(view, "field", name)){
			return;
		}
		
		Property property = mapper.getProperty(name);
		String type = field.getServerType();
		String target = field.getTarget();
		List<?> selectionList = field.getSelectionList();
		
		if(property != null){
			type = property.getType().name();
			if(title == null){
				title = property.getTitle();
			}
			String selection = property.getSelection();
			if(selection != null && selectionList == null){
				selectionList = MetaStore.getSelectionList(selection);
			}
			
			Class<?> targetClass = property.getTarget();
			if(targetClass != null){
				target = targetClass.getName();
			}
		}
		
		if(target != null && type != null){
			if(type.equals("ONE_TO_MANY") || type.equals("one-to-many")){
				String parentView = view + "(" + exportService.translate(title, "en") + "),"
						  			+ view + "(" + exportService.translate(title, "fr") + ")"; 
				updateO2MViewMap(target, field.getFormView(), parentView);
			}
			String[] targets = target.split("\\.");
			type = type + "(" + targets[targets.length - 1] + ")";
		}
		
		String moduleName = getModuleName(field, module);
		if(name != null){
			String selectEN = "";
			String selectFR = "";
			if(selectionList != null){
				selectEN = updateSelect(selectionList, "en");
				selectFR = updateSelect(selectionList, "fr");
			}
			
			String[] values = new String[]{moduleName, 
					className, 
					view, 
					type, 
					name, 
					exportService.translate(title,"en"), 
					exportService.translate(title, "fr"), 
					selectEN, 
					selectFR
			};
			
			exportService.writeRow(values);
		}

	}
	
	private void updateO2MViewMap(String className, String o2mView, String parentView){
		
		List<String[]> views = o2mViewMap.get(className);
		if(views == null){
			views = new ArrayList<String[]>();
			o2mViewMap.put(className, views);
		}
		if(!Strings.isNullOrEmpty(o2mView) 
				&& !views.contains(o2mView)
				&& !viewProcessed.contains(o2mView)){
			views.add(new String[]{parentView, o2mView});
		}
		else{
			views.add(new String[]{parentView, null});
		}
		
		o2mViewMap.put(className, views);
	}
	
	private String updateSelect(List<?> selection, String lang){
		
		List<String> titles = new ArrayList<String>();
		for(Object object : selection){
			Selection.Option option = (Option) object;
			titles.add(exportService.translate(option.getTitle(), lang));
		}
		
		return Joiner.on(":").join(titles);
	}
	
	
	private void processPanelEditor(PanelField panelField, String view, String module, Mapper mapper) {
		
		PanelEditor panelEditor = panelField.getEditor();

		if(panelEditor != null){
			String target = panelField.getTarget();
		
			if(target != null){
				try{
					Mapper targetMapper = Mapper.of(ClassUtils.findClass(target));
					processItems(panelEditor.getItems().iterator(), view, getModuleName(panelField, module), targetMapper, false);
				}catch(IllegalArgumentException e){
					log.debug("Model not found: {}", target);
				}
			}
			else{
				processItems(panelEditor.getItems().iterator(), view, getModuleName(panelField, module), mapper, false);
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void processPanelInclude(PanelInclude panelInclude, String viewName, String module, Mapper mapper, boolean addPanel){
		
		AbstractView view = panelInclude.getView();
		if(view != null){
			String name = view.getName();
			if(!viewProcessed.contains(name)){
				processForm((FormView)view, getModuleName(panelInclude, module), mapper, addPanel);
			}
		}
		else{
			log.debug("Issue in panel include: {}", panelInclude.getName());
		}
	}
	
	
	@SuppressWarnings("unused")
	private void processButtonGroup(ButtonGroup buttonGroup, String view, String module, Mapper mapper, boolean addPanel){
		
		List<AbstractWidget> items = buttonGroup.getItems();
		if(items != null){
			processItems(items.iterator(), view, module, mapper, addPanel);
		}
		
	}
	
	private void processButton(Button button, String view, String module, Mapper mapper, boolean addPanel){
		
		String name = button.getName();
		String className = mapper.getBeanClass().getName();
		
		if(!isChecked(view, "button", name)){
			String title = button.getTitle();
			String[] values = new String[]{getModuleName(button, module), 
					className, 
					view, 
					"Button", 
					name, 
					exportService.translate(title, "en"), 
					exportService.translate(title, "fr"),
					"", ""
			};
			exportService.writeRow(values);
		}
	}
	
	@SuppressWarnings("unused")
	private void processPanelRelated(PanelRelated panelRelated, String view, String module, Mapper mapper, boolean addPanel){
		
		String name = panelRelated.getName();
		String title = panelRelated.getTitle();
		String className = mapper.getBeanClass().getName();
		
		if(isChecked(view, "field", name)){
			return;
		}
		
		Property property = mapper.getProperty(name);
		String type = panelRelated.getServerType();
		String target = panelRelated.getTarget();
		if(property != null){
			type = property.getType().name();
			if(title == null){
				title = property.getTitle();
			}
			Class<?> targetClass = property.getTarget();
			if(targetClass != null){
				target = targetClass.getName();
			}
		}
		else{
			log.debug("No property found: {}, class: {}", name, className);
		}
		
		if(target != null && type != null){
			if(type.equals("ONE_TO_MANY") || type.equals("one-to-many")){
				String parentView = view + "(" + exportService.translate(title, "en") + "),"
								  + view + "(" + exportService.translate(title, "fr") + ")"; 
				updateO2MViewMap(target, panelRelated.getFormView(), parentView);
			}
			String[] targets = target.split("\\.");
			type = type + "(" + targets[targets.length - 1] + ")";
		}
		
		String[] values = new String[]{getModuleName(panelRelated, module), 
				className, 
				view, 
				type, 
				name, 
				exportService.translate(title,"en"), 
				exportService.translate(title, "fr"), 
				"", 
				""
		};
		
		exportService.writeRow(values);
	}
	
	@SuppressWarnings("unused")
	private void processLabel(Label label, String view, String module, Mapper mapper, boolean addPanel){
		
		String className = mapper.getBeanClass().getName();
		String title = label.getTitle();
		if(isChecked(view, "label", title)){
			return;
		}
		
		String[] values = new String[]{getModuleName(label, module), 
				className, 
				view, 
				"Label", 
				label.getName(), 
				exportService.translate(title,"en"), 
				exportService.translate(title, "fr"), 
				"", 
				""
		};
		
		exportService.writeRow(values);
	}
	
	@SuppressWarnings("unused")
	private void processDashlet(Dashlet dashlet, String view, String module, Mapper mapper, boolean addPanel){
		
		String title = dashlet.getTitle();
		String action = dashlet.getAction();
		String className = "";
		
		if(title == null && action != null){
			ActionView actionView = (ActionView) MetaStore.getAction(dashlet.getAction());
			title = actionView.getTitle();
		}
		
		if(isChecked(view, "dashlet", title)){
			return;
		}

		if(!Strings.isNullOrEmpty(title)){
			
			String[] values = new String[]{
					getModuleName(dashlet, module), 
					className, 
					view, 
					"Dashlet", 
					dashlet.getName(), 
					exportService.translate(title, "en"), 
					exportService.translate(title, "fr"),
					"",
					""
			};

			exportService.writeRow(values);
		}
		
	}
	
	@SuppressWarnings("unused")
	private void processItem(Item item, String view, String module, Mapper mapper, boolean addPanel){
		
		String className = mapper.getBeanClass().getName();
		String title = item.getTitle();
		
		if(!Strings.isNullOrEmpty(title) && !isChecked(view, "menuItem", title)){
			String[] values = new String[]{
					getModuleName(item, module), 
					className, 
					view, 
					"Toolbar MenuItem", 
					item.getName(), 
					exportService.translate(title, "en"), 
					exportService.translate(title, "fr"),
					"",
					""
			};

			exportService.writeRow(values);
		}
	}
}
