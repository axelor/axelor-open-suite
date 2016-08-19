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
package com.axelor.studio.service.data.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.db.DataManager;
import com.axelor.studio.db.ViewItem;
import com.axelor.studio.db.repo.ViewItemRepository;
import com.axelor.studio.service.ConfigurationService;
import com.axelor.studio.service.data.DataCommon;
import com.axelor.studio.service.data.validator.DataValidatorService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * Service class import Models from excel file (xlsx). One excel sheet represent
 * one model. It will create MetaModel, MetaField, ViewBuilder and MenuBuilder
 * from sheet data. Excel sheet must be in predefine format to be able to
 * import. Also process is all or none , so if any single error occur it will
 * abort importing. It process MetaMenu to create model and MetaField to create
 * field xml.
 * 
 * @author axelor
 *
 */
public class DataImportService extends DataCommon {

	private final static Logger log = LoggerFactory
			.getLogger(DataImportService.class);
	
	private Map<String, Map<String, List<String>>> moduleMap;

	private Map<String, Map<String, List<MetaField>>> gridViewMap;
	
	private Map<Long, Integer> fieldSeqMap;
	
	private Map<String, MetaModel> nestedModels;
	
	@Inject
	private DataValidatorService validatorService;
	
	@Inject
	private DataModuleService moduleService;
	
	@Inject
	private DataViewService dataViewService;
	
	@Inject
	private ConfigurationService configService;
	
	@Inject
	private MetaModelRepository metaModelRepo;
	
	@Inject
	private ViewItemRepository viewItemRepo;
	
	@Inject
	private MetaFieldRepository metaFieldRepo;
	
	@Inject
	private DataFieldService dataFieldService;
	
	@Inject
	private DataGridService dataGridService;
	
	@Inject
	private DataMenuService menuService;
	
	/**
	 * Root method to access the service. It will call other methods required to
	 * import model.
	 * 
	 * @param modelImporter
	 *            ModelImporter class that store all import meta file.
	 * @return Return true if import done successfully else false.
	 * @throws AxelorException 
	 */
	public File importData(DataManager dataManager) throws AxelorException {
		
		File inputFile = MetaFiles.getPath(dataManager.getMetaFile())
				.toFile();

		if (!inputFile.exists()) {
			throw new AxelorException(I18n.get("Input file not exist"), 4);
		}

		moduleMap = new HashMap<String, Map<String,List<String>>>();
		fieldSeqMap = new HashMap<Long, Integer>();
		gridViewMap = new HashMap<String, Map<String,List<MetaField>>>();
		nestedModels = new HashMap<String, MetaModel>();

		try {
			
			FileInputStream fis = new FileInputStream(inputFile);
			XSSFWorkbook workBook = new XSSFWorkbook(fis);
			
			File logFile = validatorService.validate(workBook);
			if(logFile != null){
				return logFile;
			}
			
			dataViewService.clear();
			
			XSSFSheet sheet = workBook.getSheet("Modules");
			moduleService.createModules(sheet);

			processSheets(workBook.iterator());
			
			sheet = workBook.getSheet("Menu");
			menuService.importMenus(sheet);
			
			generateGridView();

		} catch (IOException e) {
			throw new AxelorException(e, 5);
		}
		
		return null;

	}

	/**
	 * Method create list of MetaModels by reading given input file.
	 * 
	 * @param inputFile
	 *            Data input file.
	 * @return List of MetaModels created.
	 * @throws IOException
	 *             Exception in file handling.
	 * @throws AxelorException 
	 */
	private void processSheets(Iterator<XSSFSheet> sheetIter) throws IOException, AxelorException {

		if (!sheetIter.hasNext()) {
			return;
		}
		
		XSSFSheet sheet = sheetIter.next();
		String name = sheet.getSheetName(); 
		if (!name.equals("Modules") && !name.equals("Menu")) {
			log.debug("Importing sheet: {}", name);
			Iterator<Row> rowIter = sheet.rowIterator();
			if (rowIter.hasNext()) {
				rowIter.next();
			}
			extractRow(rowIter);
		}
		
		processSheets(sheetIter);
	}
	
	/**
	 * Create MetaModel from data sheet data.
	 * 
	 * @param sheet
	 *            Excel sheet to process
	 * @return MetaModel created
	 * @throws AxelorException 
	 */

	private void extractRow(Iterator<Row> rowIter) throws AxelorException {

		if (!rowIter.hasNext()) {
			return;
		}
		
		boolean replace = true;
		Row row = rowIter.next();
		String module = getValue(row, MODULE);
		if (module == null) {
			extractRow(rowIter);
			return;
		}
		
		if (module.startsWith("*")) {
			replace = false;
			module = module.replace("*", "");
		}
		
		MetaModule metaModule = getModule(module, getValue(row, IF_MODULE));
		if (metaModule == null) {
			extractRow(rowIter);
			return;
		}
		
		importModel(row, metaModule, replace);

		extractRow(rowIter);
	}
	
	private void importModel(Row row, MetaModule metaModule, boolean replace) throws AxelorException {

		String name = getValue(row, MODEL);
		if (name == null) {
			return;
		}
		
		String parentField = null;

		String modelName = name;
		if (name.contains("(")) {
			String[] names = name.split("\\(");
			modelName = inflector.camelize(names[0]);
			parentField = names[1].replace(")", "");
		}
		else {
			modelName = inflector.camelize(name);
		}
		
		MetaModel model = null;
		if (!Strings.isNullOrEmpty(modelName)) {
			model = getModel(modelName, metaModule);
			if (parentField != null && !nestedModels.containsKey(name)) {
				nestedModels.put(name, getNestedModel(metaModule, model, parentField));
			}
		}

		String[] basic = getBasic(row, parentField);
		MetaField metaField = null;
		if (model != null && DataCommon.FIELD_TYPES.containsKey(basic[0])) {
			if (parentField == null) {
				Integer sequence = getFieldSequence(model.getId());
				metaField = dataFieldService.createMetaField(row, basic, model, metaModule, sequence);
				if (metaField.getCustomised()) {
					moduleMap.get(metaModule.getName()).get(modelName).add(basic[2]);
				}
			}
			else {
				MetaModel nestedModel = nestedModels.get(name);
				Integer sequence = getFieldSequence(nestedModel.getId());
				metaField = dataFieldService.createMetaField(row, basic, nestedModel, metaModule, sequence);
				modelName = nestedModel.getName();
				if (metaField.getCustomised()) {
					moduleMap.get(metaModule.getName()).get(modelName).add(basic[2]);
				}
			}
			
			addGridField(metaModule.getName(), metaField, getValue(row, GRID), modelName);
			
		}

		if (!Strings.isNullOrEmpty(basic[0]) 
				&& (!DataCommon.IGNORE_TYPES.contains(basic[0]) || basic[0].equals("empty"))) {
			dataViewService.addViewElement(model, basic, row, metaField, replace);
		}
	}
	
	public MetaModule getModule(String module, String checkModule) {
		
		List<String> nonCustomModules = configService.getNonCustomizedModules();
		
		if (module == null || nonCustomModules.contains(module)) {
			return null;
		}
		
		if (checkModule != null && !nonCustomModules.contains(checkModule)) {
			return configService.getCustomizedModule(checkModule);
		}
		
		MetaModule metaModule = configService.getCustomizedModule(module);
		
		if (metaModule  != null && checkModule != null) {
			String depends = metaModule.getDepends();
			if (depends != null && Arrays.asList(depends.split(",")).contains(checkModule)) {
				return metaModule;
			}
			return null;
		}
		
		return metaModule;
	}
	
	
	private String[] getBasic(Row row, String parentField) {

		String fieldType = getValue(row, TYPE);
		String ref = null;
		String form = null;
		String grid = null;
		if (fieldType != null) {
			String[] fieldTypes = fieldType.split(",");
			String[] type = fieldTypes[0].split("\\(");
			fieldType = type[0];
			if (type.length > 1) {
				ref = type[1].replace(")", "");
			}
			if (fieldTypes.length > 1) {
				form = fieldTypes[1];
			}
			if (fieldTypes.length > 2) {
				grid = fieldTypes[2];
			}
			
		}
		
		if(DataCommon.FR_MAP.containsKey(fieldType)) {
			fieldType = DataCommon.FR_MAP.get(fieldType);
		}

		String name = getValue(row, NAME);
		String title = getValue(row, TITLE);
		String titleFr = getValue(row, TITLE_FR);
		if (Strings.isNullOrEmpty(title)) {
			title = titleFr;
		}
		
		if (Strings.isNullOrEmpty(name) 
				&& !Strings.isNullOrEmpty(title) 
				&& !fieldType.equals("label")) {
			name = getFieldName(title);
		}

		return new String[] { fieldType, ref, name, title, parentField, form, grid };
	}
	
	private MetaModel getNestedModel(MetaModule module, MetaModel parentModel, String nestedField) {
		
		log.debug("Search for nested field: {}, model: {}", nestedField, parentModel.getName());
		
		MetaField metaField = metaFieldRepo.all()
				.filter("self.name = ?1 and self.metaModel = ?2", nestedField, parentModel)
				.fetchOne();
		
		if (metaField != null && metaField.getRelationship() != null) {
			log.debug("Field found with type: {}", metaField.getTypeName());
			return getModel(metaField.getTypeName(), module);
		}
		
		return parentModel;
	}
	
	/**
	 * Set package name and fullName of model and save it.
	 * 
	 * @param models
	 *            List of MetaModels to save.
	 * @return List of saved MetaModels.
	 */
	@Transactional(rollbackOn = { Exception.class })
	public MetaModel getModel(String name, MetaModule module) {
		
		name = inflector.camelize(name);
		
		MetaModel model = metaModelRepo.findByName(name);
		
		updateModuleMap(name, module.getName());
		
		if (model == null) {
			model = new MetaModel(name);
			model.setMetaModule(module);
			MetaField metaField = new MetaField("wkfStatus", false);
			metaField.setTypeName("String");
			metaField.setFieldType("string");
			metaField.setLabel("Status");
			metaField.setCustomised(true);
			model.addMetaField(metaField);
			moduleMap.get(module.getName()).get(name).add("wkfStatus");
		}

		if (model.getPackageName() == null) {
			String[] modules =  module.getName().replace("axelor-", "").split("-");
			model.setPackageName("com.axelor.apps." + modules[0] + ".db");
			model.setFullName("com.axelor.apps." + modules[0] + ".db." + model.getName());
		}
		model.setCustomised(true);
		model.setEdited(true);

		return metaModelRepo.save(model);
	}
	
	private boolean updateModuleMap(String name, String module) {
		
		if (!moduleMap.containsKey(module)) {
			moduleMap.put(module, new HashMap<String, List<String>>());
		}

		Map<String, List<String>> modelMap = moduleMap.get(module);
		if (!modelMap.containsKey(name)) {
			modelMap.put(name, new ArrayList<String>());
			moduleMap.put(module, modelMap);
			return true;
		}
		
		return false;
		
	}
	
	private void generateGridView() throws AxelorException {
		
		if (!dataViewService.getViewActionMap().isEmpty()) {
			throw new AxelorException(I18n.get("Wizard views not found: %s"), 1,
					dataViewService.getViewActionMap().keySet());
		}

		for (String module : moduleMap.keySet()) {
			for (String modelName : moduleMap.get(module).keySet()) {
	 			MetaModel model = metaModelRepo.findByName(modelName);
	 			if (model != null && !model.getCustomised()) {
	 				continue;
	 			}
				model = clearModel(module, model);
				if (model == null || moduleMap.get(module).get(modelName).size() < 1) {
					continue;
				}
				if (gridViewMap.containsKey(module)) {
					List<MetaField> fields = gridViewMap.get(module).get(model);
					dataGridService.createGridView(getModule(module, null), model, fields);
				}
			}
		}
	}

	@Transactional
	public MetaModel clearModel(String module, MetaModel model) {

		List<String> fieldNames = moduleMap.get(module).get(model);
		
		if (fieldNames == null) {
			return model;
		}
		
		Iterator<MetaField> fieldIter = model.getMetaFields().iterator();
		
		while (fieldIter.hasNext()) {
			MetaField field = fieldIter.next();
			if (field.getCustomised() && !fieldNames.contains(field.getName())) {
				log.debug("Removing field : {}", field.getName());
				List<ViewItem> viewItems = viewItemRepo.all().filter("self.metaField = ?1", field).fetch();
				for (ViewItem viewItem : viewItems) {
					viewItemRepo.remove(viewItem);
				}
				fieldIter.remove();
			}
		}
		
		return metaModelRepo.save(model);
	}

	private void addGridField(String module, MetaField meatField, String addToList, String model) {
		
		Map<String, List<MetaField>> gridMap = null; 
		if (!gridViewMap.containsKey(module)) {
			gridViewMap.put(module, new HashMap<String, List<MetaField>>());
		}
		gridMap = gridViewMap.get(module);
		if (!gridMap.containsKey(model)) {
			gridMap.put(model, new ArrayList<MetaField>());
		}
		
		if (addToList != null && addToList.equalsIgnoreCase("x")) {
			gridMap.get(model).add(meatField);
			gridViewMap.put(module, gridMap);
		}
		
	}
	
	private Integer getFieldSequence(Long modelId) {

		Integer seq = 0;

		if (fieldSeqMap.containsKey(modelId)) {
			seq = fieldSeqMap.get(modelId);
		}

		fieldSeqMap.put(modelId, seq + 1);

		return seq;

	}
	
}
