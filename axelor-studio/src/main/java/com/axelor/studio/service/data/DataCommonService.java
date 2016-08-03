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
package com.axelor.studio.service.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.axelor.common.Inflector;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.MetaTranslationRepository;
import com.axelor.studio.db.repo.ViewBuilderRepository;
import com.axelor.studio.db.repo.ViewItemRepository;
import com.axelor.studio.service.ConfigurationService;
import com.axelor.studio.service.ViewLoaderService;
import com.axelor.studio.service.builder.ModelBuilderService;
import com.axelor.studio.service.data.importer.DataViewService;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public abstract class DataCommonService {
	
	public static final String[] HEADERS = new String[]{
		"Note",
		"Module", 
		"Object", 
		"View", 
		"Field name", 
		"Field title",
		"Field type", 
		"Selection",
		"Menu",
		"Required",
		"Required if",
		"Readonly",
		"Readonly if",
		"Hidden",
		"Hide if",
		"Show if",
		"If config",
		"Formula",
		"Event",
		"Domain",
		"On change",
		"On click",
		"Colspan",
		"Grid",
		"Help",
		"Panel Level"
	};
	
	
	public final static Map<String, String> fieldTypes;

	static {
		Map<String, String> map = new HashMap<String, String>();
		map.put("char", "string");
		map.put("html", "string");
		map.put("text", "string");
		map.put("url", "string");
		map.put("long", "long");
		map.put("date", "date");
		map.put("datetime", "datetime");
		map.put("time", "time");
		map.put("duration", "integer");
		map.put("select", "integer");
		map.put("multiselect", "string");
		map.put("binary", "binary");
		map.put("m2o", "many-to-one");
		map.put("o2m", "one-to-many");
		map.put("m2m", "many-to-many");
		map.put("o2o", "one-to-one");
		map.put("boolean", "boolean");
		map.put("int", "integer");
		map.put("decimal", "decimal");
		map.put("file", "many-to-one");
		fieldTypes = Collections.unmodifiableMap(map);
	}
	
	public final static Map<String, String> viewElements;

	static {
		Map<String, String> map = new HashMap<String, String>();
		map.put("panel", "panel");
		map.put("panelbook", "panelbook");
		map.put("panelside", "panelside");
		map.put("paneltab", "paneltab");
		map.put("menu", "menu");
		map.put("button", "button");
		map.put("wizard", "wizard");
		map.put("error", "error");
		map.put("note", "note");
		map.put("label", "label");
		map.put("warn", "warn");
		map.put("menubar", "menubar");
		map.put("menubar.item", "menubar");
		map.put("dashlet", "dashlet");
		map.put("general", "general");
		map.put("empty", "empty");
		map.put("onsave", "onsave");
		map.put("onnew", "onnew");
		map.put("onload", "onload");
		map.put("colspan", "colspan");
		map.put("spacer", "spacer");
		viewElements = Collections.unmodifiableMap(map);
	}
	
	
	protected final static List<String> ignoreTypes;
	
	static {
		List<String> types = new ArrayList<String>();
		types.add("general");
		types.add("tip");
		types.add("warn");
		types.add("note");
		ignoreTypes = Collections.unmodifiableList(types);
	}
	
	public final static Map<String, String> frMap;

	static {
		Map<String, String> map = new HashMap<String, String>();
		map = new HashMap<String, String>();
		map.put("chaine", "char");
		map.put("tableau", "o2m");
		map.put("entier", "int");
		map.put("fichier", "file");
		map.put("bouton", "button");
		map.put("Note", "general");
		map.put("case à cocher", "boolean");
		map.put("Astuce", "tip");
		map.put("Attention", "warn");
		frMap = Collections.unmodifiableMap(map);
	}
	
	protected final static int NOTE = 0;
	protected final static int MODULE = 1;
	protected final static int MODEL = 2;
	protected final static int VIEW = 3;
	protected final static int NAME = 4;
	protected final static int TITLE = 5;
	protected final static int TYPE = 6;
	protected final static int SELECT = 7;
	protected final static int MENU = 8;
	protected final static int REQUIRED = 9;
	protected final static int REQUIRED_IF = 10;
	protected final static int READONLY = 11;
	protected final static int READONLY_IF = 12;
	protected final static int HIDDEN = 13;
	protected final static int HIDE_IF = 14;
	protected final static int SHOW_IF = 15;
	protected final static int IF_CONFIG = 16;
	protected final static int FORMULA = 17;
	protected final static int EVENT = 18;
	protected final static int DOMAIN = 19;
	protected final static int ON_CHANGE = 20;
	protected final static int ON_CLICK = 21;
	protected final static int COLSPAN = 22;
	protected final static int GRID = 23;
	protected final static int HELP = 24;
	protected final static int PANEL_LEVEL = 25;
	
	protected final static Map<String, String> relationshipMap;

	static {
		Map<String, String> map = new HashMap<String, String>();
		map = new HashMap<String, String>();
		map.put("o2m", "OneToMany");
		map.put("m2m", "ManyToMany");
		map.put("m2o", "ManyToOne");
		map.put("file", "ManyToOne");
		map.put("o2o", "OneToOne");
		relationshipMap = Collections.unmodifiableMap(map);
	}
	
	protected static final List<String> referenceTypes = Arrays.asList(new String[]{"o2m","m2m","m2o","wizard", "o2o"});
	
	@Inject
	protected MetaModelRepository metaModelRepo;

	@Inject
	protected DataViewService viewImporterService;

	@Inject
	protected ModelBuilderService modelBuilderService;

	@Inject
	protected MetaFieldRepository metaFieldRepo;

	@Inject
	protected ViewLoaderService viewLoaderService;

	@Inject
	protected ViewItemRepository viewItemRepo;

	@Inject
	protected ViewBuilderRepository viewBuilderRepo;

	@Inject
	protected MetaTranslationRepository metaTranslationRepo;
	
	@Inject
	protected ConfigurationService configService;

	public final Inflector inflector = Inflector.getInstance();

	protected String getValue(Row row, int index) {
		
		Cell cell = row.getCell(index);
		if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING) {
			String val = cell.getStringCellValue();
			if(Strings.isNullOrEmpty(val)){
				return null;
			}
			return val;
		}

		return null;
	}
	
	/**
	 * Method to create field name from title if name of field is blank. It will
	 * simplify title and make standard field name from it.
	 * 
	 * @param title
	 *            Title string to process.
	 * @return Name created from title.
	 */
	public String getFieldName(String title) {

		title = title.replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("^[0-9]+",
				"");

		return inflector.camelize(inflector.simplify(title.trim()), true);

	}


}
