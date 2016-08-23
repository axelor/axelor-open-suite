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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.repo.MetaModuleRepository;
import com.axelor.meta.schema.views.AbstractWidget;
import com.axelor.studio.service.ViewLoaderService;
import com.axelor.studio.service.data.CommonService;
import com.axelor.studio.service.data.importer.DataReader;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class ExportService {
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	private String menuPath = null;
	
	private Map<String, String> processedMenus = new HashMap<String, String>();
	
	private Set<String> viewProcessed = new HashSet<String>(); 
	
	private Map<String, String[]> docMap = new HashMap<String, String[]>();
	
	private Map<String, List<String[]>> commentMap = new HashMap<String, List<String[]>>();
	
	private DataWriter writer;
	
	private String writerKey;
	
	private List<String> exportModules = new ArrayList<String>();
	
	@Inject
	private CommonService common;
	
	@Inject
	private MetaFiles metaFiles;
	
	@Inject
	private ExportModel dataExportModel;
	
	@Inject
	private MetaModuleRepository metaModuleRepo;
	
	@Inject
	private ExportMenu exportMenu;
	
	@Inject
	private ExportAction exportAction;
	
	public MetaFile export(MetaFile oldFile, DataWriter writer, DataReader reader) {
		
		setExportModules();
		
		this.writer = writer;
		this.writer.initialize(metaFiles);
		
		if  (oldFile != null) {
			if (reader.initialize(oldFile)) {
				updateDocMap(reader);
			}
		}
		
		addModules(reader);

		exportMenu.export(writer, exportModules);
		
		exportAction.export(writer);
		
		processMenu();
		
		return this.writer.export(oldFile);
	}
	
	public static String getModuleToCheck(AbstractWidget item, String module) {
		
		String moduleName = item.getModuleToCheck();
		
		if  (Strings.isNullOrEmpty(moduleName)) {
			moduleName = module;
		}
		
		return moduleName;
	}

	private void setExportModules() {
		
		List<MetaModule> modules = metaModuleRepo
				.all()
				.filter("(self.installed = true OR self.customised = true) and self.name != 'axelor-core'").fetch();
		
		for (MetaModule module : modules) {
			exportModules.add(module.getName());
		}
		
	}
	
	public boolean isExportModule(String name) {
		
		return exportModules.contains(name);
	}
	
	public boolean isViewProcessed(String name) {
		
		return viewProcessed.contains(name);
	}
	
	public void addViewProcessed(String name) {
		
		viewProcessed.add(name);
	}
	
	private void addModules(DataReader reader) {
		
		String[] keys = reader.getKeys();
		
		if (keys != null) {
			for (int count = 0 ; count < reader.getTotalLines(keys[0]); count++) {
				String[] row = reader.read(keys[0], count);
				writer.write(keys[0], count, row);
			}
		}
		
		else {
			writer.write("Modules", 0, CommonService.MODULE_HEADERS);
		}
		
	}
	
	protected void writeRow(String[] values, boolean newForm) {
		
		if(newForm){
			addGeneralRow(writerKey, values);
		}
		
		values[CommonService.MENU] = menuPath;
		
		values = addHelp(null, values);
		
		writer.write(writerKey, null, values);
		
		addComments(writerKey, null, values, false);
		
	}
	
	private void processMenu() {
		
		List<MetaMenu> menus = 	exportMenu.getMenus(exportModules);
		
		for (MetaMenu menu : menus) {
			String name = menu.getName();
			if (processedMenus.containsKey(name)) {
				continue;
			}
			
			updateMenuPath(menu);
			
			if (menu.getParent() == null) {
				String title = menu.getTitle();
				writerKey = I18n.get(title);
				if (processedMenus.containsValue(title)) {
					writerKey += "(" + menu.getId() + ")";
				}
				writer.write(writerKey, null, CommonService.HEADERS);
			}
			
			MetaAction action = menu.getAction();;
			if (action != null && action.getType().equals("action-view")) {
				dataExportModel.export(this, action);
			}
			
			processedMenus.put(name, menu.getTitle());
		}
		
	}
	
	private String[] addHelp(String docKey, String[] vals) {
		
		if (!docMap.isEmpty()) {
			
			if (docKey == null) {
				docKey = getDocKey(vals);
			}
			if (docMap.containsKey(docKey)) {
				return (String[]) ArrayUtils.addAll(vals, docMap.get(docKey));
			}
		}
		
		return vals;
	}
	
	private String getDocKey(String[] values) {
		
		 String name = getFieldName(values);
		 
		 String model = values[CommonService.MODEL];
		 if (model != null) {
			 String[] modelSplit = model.split("\\.");
			 model = modelSplit[modelSplit.length - 1];
		 }
		 
		 String key =  model
				+ "," + values[CommonService.VIEW]
				+ "," + getFieldType(values[CommonService.TYPE]) 
			    + "," + name;
		 
		return key;
		 
	}
	
	private String getFieldName(String[] row) {
		
		String name = row[CommonService.NAME];
		
		if (Strings.isNullOrEmpty(name)) {
			name =  row[CommonService.TITLE];
			if (!Strings.isNullOrEmpty(name)) {
				name = common.getFieldName(name);
			}
		}
		
		return name;
	}

	protected void setMenuPath(String menuPath) {
		this.menuPath = menuPath;
	}
	
	private void addGeneralRow(String key, String[] values) {
		
		String[] vals = new String[CommonService.HEADERS.length];
		vals[CommonService.MODULE] = values[CommonService.MODULE];
		vals[CommonService.MODEL] = values[CommonService.MODEL];
		vals[CommonService.VIEW] = values[CommonService.VIEW];
		vals[CommonService.TYPE] = "general"; 
		
		if (menuPath != null) {
			vals[CommonService.MENU] = menuPath;
			menuPath = null;
		}	
		
		vals = addHelp(null, vals);
		writer.write(key, null, vals);
		
		addComments(key, null, vals, false);
		
	}
	
	private void updateMenuPath(MetaMenu metaMenu) {
		
		List<String> menus = new ArrayList<String>();
		menus.add(metaMenu.getTitle());
		
		addParentMenus(menus, metaMenu);
		
		Collections.reverse(menus);
		
		menuPath = Joiner.on("/").join(menus);
	}
	
	private void addParentMenus(List<String> menus, MetaMenu metaMenu) {
		
		MetaMenu parentMenu = metaMenu.getParent();
		
		if (parentMenu != null) {
			menus.add(parentMenu.getTitle());
			addParentMenus(menus, parentMenu);
		}
	}
	

	private void updateDocMap(DataReader reader) {
		
		String[] keys = reader.getKeys();
		
		if (keys == null || keys.length == 1) {
			return;
		}
		
		keys = Arrays.copyOfRange(keys, 1, keys.length);
		
		for (String key : keys) {
			
			log.debug("Loading key: {}", key);
			String lastKey = key;
			
			for (int count = 0; count < reader.getTotalLines(key); count ++) {
				
				String[] row = reader.read(key, count);
				
				if (row == null) {
					continue;
				}
				
				if (count == 0) {
					if (row.length > CommonService.HELP) {
						docMap.put(lastKey, Arrays.copyOfRange(row, CommonService.HELP, row.length));
					}
					continue;
				}
				
				String name = getFieldName(row);
				
				String type = row[CommonService.TYPE];
				if (type == null) {
					continue;
				}
				
				String model = row[CommonService.MODEL];
				if (model != null) {
					model = common.inflector.camelize(model);
				}
				
				String view = row[CommonService.VIEW];
				if (model != null && view == null) {
					view = ViewLoaderService.getDefaultViewName(model, "form");
				}
				
				if (updateComment(lastKey, type, row)) {
					continue;
				}
				
				lastKey = model + "," + view + "," + getFieldType(type) + "," +  name;
				if (row.length > CommonService.HELP) {
					docMap.put(lastKey, Arrays.copyOfRange(row, CommonService.HELP, row.length));
				}
			}
		}
			
		
	}
	
	private boolean updateComment(String lastKey, String type, String[] row) {
		
		if (type.contains("(")) {
			type = type.substring(0, type.indexOf("("));
		}
		
		if (!CommonService.FIELD_TYPES.containsKey(type) 
				&& !CommonService.VIEW_ELEMENTS.containsKey(type)) {

				List<String[]> rows = new ArrayList<String[]>();
				if (commentMap.containsKey(lastKey)) {
					rows = commentMap.get(lastKey);
				}
				
				rows.add(row);
				
				commentMap.put(lastKey, rows);
				
				return true;
		}
		
		return false;
	}
	
	private Integer addComments(String writeKey, Integer index, String[] values, boolean header)  {
		 
		 if (commentMap.isEmpty()) {
			 return index;
		 }
		 
		 String key = null;
		 if (header) {
			 key = writeKey;
		 }
		 else {
			 key = getDocKey(values);
		 }
		 
		 if (commentMap.containsKey(key)) {
			for (String[] row : commentMap.get(key)){
				if (index != null) {
					index++;
				}
				writer.write(writeKey, index, row);
			}
		 }
		 
		 return index;
	}
	
	
	private String getFieldType(String type) {
		
		if (type == null) {
			return type;
		}
		type = type.trim();
		
		if (type.contains("(")) {
			type = type.substring(0, type.indexOf("("));
		}
		
		if(CommonService.FR_MAP.containsKey(type)) {
			type = CommonService.FR_MAP.get(type);
		}
		
		if (CommonService.FIELD_TYPES.containsKey(type)) {
			type = CommonService.FIELD_TYPES.get(type);
		}
		else if (CommonService.VIEW_ELEMENTS.containsKey(type)) {
			type = CommonService.VIEW_ELEMENTS.get(type);
		}
		
		type = type.toUpperCase();
		
		if (type.startsWith("PANEL")) {
			return "PANEL";
		}
		
		if (type.startsWith("WIZARD")) {
			return "BUTTON";
		}
		
		return type.replace("-", "_");
	}
	
	
}
