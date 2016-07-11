package com.axelor.studio.service.importer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;

import com.axelor.common.Inflector;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.MetaTranslationRepository;
import com.axelor.studio.db.repo.ViewBuilderRepository;
import com.axelor.studio.db.repo.ViewItemRepository;
import com.axelor.studio.service.ViewLoaderService;
import com.axelor.studio.service.builder.ModelBuilderService;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public abstract class ImporterService {

	public final static Map<String, String> typeMap;

	static {
		Map<String, String> map = new HashMap<String, String>();
		map.put("char", "string");
		map.put("html", "string");
		map.put("text", "string");
		map.put("url", "string");
		map.put("date", "date");
		map.put("datetime", "datetime");
		map.put("duration", "integer");
		map.put("select", "integer");
		map.put("multiselect", "string");
		map.put("m2o", "many-to-one");
		map.put("o2m", "one-to-many");
		map.put("m2m", "many-to-many");
		map.put("boolean", "boolean");
		map.put("int", "integer");
		map.put("decimal", "decimal");
		map.put("file", "many-to-one");
		typeMap = Collections.unmodifiableMap(map);
	}
	
	protected final static List<String> viewElements;
	
	static {
		List<String> elements = new ArrayList<String>();
		elements.add("panel");
		elements.add("panelbook");
		elements.add("panelside");
		elements.add("paneltab");
		elements.add("menu");
		elements.add("button");
		elements.add("wizard");
		elements.add("error");
		elements.add("warning");
		elements.add("general");
		elements.add("tip");
		elements.add("warn");
		elements.add("note");
		elements.add("label");
		viewElements = Collections.unmodifiableList(elements);
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
		frMap = Collections.unmodifiableMap(map);
	}

	protected final static int MODULE = 0;
	protected final static int MODEL = 1;
	protected final static int VIEW = 2;
	protected final static int NAME = 3;
	protected final static int TITLE = 4;
	protected final static int TITLE_TR = 5;
	protected final static int TYPE = 6;
	protected final static int SELECT = 7;
	protected final static int SELECT_TR = 8;
	protected final static int REQUIRED = 11;
	protected final static int READONLY = 12;
	protected final static int FORMULA = 13;
	protected final static int EVENT = 14;
	protected final static int DOMAIN = 15;
	protected final static int HIDE_IF = 16;
	protected final static int REQUIRED_IF = 17;
	protected final static int READONLY_IF = 18;
	protected final static int LIST = 19;
	protected final static int HELP = 20;
	// private final static int ATTR_HIDDEN = 7;
	// private final static int ATTR_MIN = 8;
	// private final static int ATTR_MAX = 9;
	// private final static int ATTR_HELP = 10;
	// private final static int ATTR_HELP_TR = 11;
	protected final static Map<String, String> relationshipMap;

	static {
		Map<String, String> map = new HashMap<String, String>();
		map = new HashMap<String, String>();
		map.put("o2m", "OneToMany");
		map.put("m2m", "ManyToMany");
		map.put("m2o", "ManyToOne");
		map.put("file", "ManyToOne");
		relationshipMap = Collections.unmodifiableMap(map);
	}

	@Inject
	protected MetaModelRepository metaModelRepo;

	@Inject
	protected ViewImporterService viewImporterService;

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

	public final Inflector inflector = Inflector.getInstance();

	protected String getString(Cell cell) {

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
	 * simplify title and make standard field name from it..
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
