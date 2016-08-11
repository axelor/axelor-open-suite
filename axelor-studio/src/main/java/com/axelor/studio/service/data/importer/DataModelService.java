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
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaModuleRepository;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.axelor.studio.db.DataManager;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import com.axelor.studio.service.ViewLoaderService;
import com.axelor.studio.service.builder.ModelBuilderService;
import com.axelor.studio.service.data.DataCommonService;
import com.axelor.studio.utils.Namming;
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
public class DataModelService extends DataCommonService {

	private final static Logger log = LoggerFactory
			.getLogger(DataModelService.class);
	
	private Map<String, Map<String, List<String>>> moduleMap;

	private Map<String, Map<String, List<MetaField>>> gridViewMap;
	
	private Map<Long, Integer> fieldSeqMap;
	
	private Map<String, MetaModel> nestedModels;
	
	private Map<String, String> modulePriorityMap;
	
	private Row row = null;
	
	@Inject
	private MetaSelectRepository metaSelectRepo;
	
	@Inject
	private DataValidatorService validatorService;
	
	@Inject
	private MetaModuleRepository metaModuleRepo;
	
	/**
	 * Root method to access the service. It will call other methods required to
	 * import model.
	 * 
	 * @param modelImporter
	 *            ModelImporter class that store all import meta file.
	 * @return Return true if import done successfully else false.
	 * @throws AxelorException 
	 */
	public File importModels(DataManager dataManager) throws AxelorException {
		
		File inputFile = MetaFiles.getPath(dataManager.getMetaFile())
				.toFile();

		if (!inputFile.exists()) {
			throw new AxelorException(I18n.get("Input file not exist"), 4);
		}

		moduleMap = new HashMap<String, Map<String,List<String>>>();
		fieldSeqMap = new HashMap<Long, Integer>();
		gridViewMap = new HashMap<String, Map<String,List<MetaField>>>();
		nestedModels = new HashMap<String, MetaModel>();
		modulePriorityMap = new HashMap<String, String>();

		try {
			
			FileInputStream fis = new FileInputStream(inputFile);
			XSSFWorkbook workBook = new XSSFWorkbook(fis);
			
			File logFile = validatorService.validate(workBook);
			if(logFile != null){
				return logFile;
			}
			
			viewImporterService.clear();
			
			log.debug("Non customised modules: {}", configService.getNonCustomizedModules());
			
			proccessSheet(workBook);
			
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
	private void proccessSheet(XSSFWorkbook workBook) throws IOException, AxelorException {

		Iterator<XSSFSheet> sheetIter = workBook.iterator();
		
		createCustomModules(sheetIter.next());
		
		while (sheetIter.hasNext()) {
			XSSFSheet sheet = sheetIter.next();
			log.debug("Importing sheet: {}", sheet.getSheetName());
			Iterator<Row> rowIter = sheet.rowIterator();
			if (rowIter.hasNext()) {
				rowIter.next();
			}
			extractRow(rowIter);
		}

	}
	
	@Transactional
	public void createCustomModules(XSSFSheet sheet) {
		
		Iterator<Row> rowIterator = sheet.rowIterator();
		
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			if (row.getRowNum() == 0) {
				continue;
			}
			
			String name = getValue(row, 0);
			if (name == null) {
				continue;
			}
			if (configService.getNonCustomizedModules().contains(name)) {
				continue;
			}
			MetaModule module = configService.getCustomizedModule(name);
			if (module == null) {
				module = new MetaModule(name);
			}
			module.setDepends(getValue(row, 1));
			module.setTitle(getValue(row, 2));
			module.setModuleVersion(getValue(row, 3));
			module.setDescription(getValue(row, 4));
			module.setCustomised(true);
			
			metaModuleRepo.save(module);
			modulePriorityMap.put(name, getValue(row, 5));
			log.debug("Module found/created: {}", module);
		}
		
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
		this.row = rowIter.next();
		String module = getValue(row, MODULE);
		if (module == null) {
			extractRow(rowIter);
			return;
		}
		
		if (module.startsWith("*")) {
			replace = false;
			module = module.replace("*", "");
		}
		
		String checkModule = getValue(row, IF_MODULE);
		if (!Strings.isNullOrEmpty(checkModule)) {
			module = checkModule;
		}
		
		MetaModule metaModule = getModule(module);
		if (metaModule == null) {
			extractRow(rowIter);
			return;
		}
		
		importModel(metaModule, replace);

		extractRow(rowIter);
	}
	
	private void importModel(MetaModule metaModule, boolean replace) throws AxelorException {

		String name = getValue(row, MODEL);
		String parentField = null;

		String modelName = name;
		if (name != null && name.contains("(")) {
			String[] names = name.split("\\(");
			modelName = names[0];
			parentField = names[1].replace(")", "");
		}
		
		MetaModel model = null;
		if (!Strings.isNullOrEmpty(modelName)) {
			model = getModel(modelName, metaModule);
			if (parentField != null && !nestedModels.containsKey(name)) {
				nestedModels.put(name, getNestedModel(metaModule, model, parentField));
			}
		}

		String[] basic = getBasic(parentField);
		MetaField metaField = null;
		if (model != null && fieldTypes.containsKey(basic[0])) {
			if (parentField == null) {
				metaField = createMetaField(model, basic, metaModule);
			}
			else {
				MetaModel nestedModel = nestedModels.get(name);
				metaField = createMetaField(nestedModel, basic, metaModule);
				modelName = nestedModel.getName();
			}
			
			addGridField(metaModule.getName(), metaField, getValue(row, GRID), modelName);
			
		}

		if (!Strings.isNullOrEmpty(basic[0]) && !ignoreTypes.contains(basic[0])) {
			viewImporterService.addViewElement(model, basic, row, metaField, replace, modulePriorityMap);
		}
	}
	
	private MetaModule getModule(String name) {
		
		MetaModule metaModule = null;
		
		if (configService.getNonCustomizedModules().contains(name)) {
			return null;
		}
		
		if (!name.isEmpty()) {
			metaModule = configService.getCustomizedModule(name);
		}
			
		return metaModule;
	}
	
	
	private String[] getBasic(String parentField) {

		String fieldType = getValue(row, TYPE);
		String ref = null;
		if (fieldType != null) {
			String[] type = fieldType.split("\\(");
			fieldType = type[0];
			if (type.length > 1) {
				ref = type[1].replace(")", "");
			}
		}
		
		if(frMap.containsKey(fieldType)) {
			fieldType = frMap.get(fieldType);
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

		return new String[] { fieldType, ref, name, title, parentField };
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
	
	@Transactional
	public MetaField createMetaField(MetaModel metaModel, String[] basic, MetaModule metaModule) throws AxelorException {

		String model = metaModel.getName();
		
		MetaField field = metaFieldRepo
				.all()
				.filter("self.name = ?1 and self.metaModel = ?2", basic[2],
						metaModel).fetchOne();
		
		
		if (field == null) {
			validateFieldName(basic[2], row.getRowNum(), model);
			field = new MetaField();
			field.setName(basic[2]);
			if (!ModelBuilderService.isReserved(basic[2])) {
				field.setCustomised(true);
			}
		} else if (!field.getCustomised()){
			return field;
		}
		
		addTranslation(basic[3], getValue(row, TITLE_FR), "fr");
		
		moduleMap.get(metaModule.getName()).get(model).add(basic[2]);
		
		if (fieldTypes.containsKey(basic[1])) {
			field.setFieldType(fieldTypes.get(basic[1]));
		}
		else {
			field.setFieldType(fieldTypes.get(basic[0]));
		}
		
		field.setLabel(basic[3]);

		switch (basic[0]) {
			case "text":
				field.setLarge(true);
				break;
			case "html":
				field.setLarge(true);
				break;
			case "url":
				field.setIsUrl(true);
				break;
			case "duration":
				field.setIsDuration(true);
				break;
			case "select":
				field = setSelectionField(model, field, metaModule);
				break;
			case "multiselect":
				field = setSelectionField(model, field, metaModule);
				field.setMultiselect(true);
				break;

		}
		
		String help = getValue(row, HELP);
		String helpFr = getValue(row, HELP_FR);
		if (help == null) {
			help = helpFr;
		}
		else {
			addTranslation(help, helpFr, "fr");
		}
		
		field.setHelpText(help);
		field.setMetaModel(metaModel);
		field.setMetaModule(metaModule);
		field.setSequence(getFieldSequence(metaModel.getId()));
		field = updateFieldTypeName(basic[0], basic[1], field, metaModule.getName());

		return metaFieldRepo.save(field);

	}
	
	private void validateFieldName(String name, int rowNum, String model) throws AxelorException {
		
		if (Strings.isNullOrEmpty(name)) {
			throw new AxelorException(
							I18n.get("No title or name for field row number: %s, model: %s"), 
							1, rowNum + 1, model);
		}
		
		if (Namming.isKeyword(name) || Namming.isReserved(name)) {
			throw new AxelorException(
					I18n.get("Can't use reserve word for name. Row number: %s, model: %s"), 
					1, rowNum + 1, model);
		}
		
		
	}

	
	/**
	 * Method to create MetaSelect from selection options given for field. It
	 * search for existing MetaSelect if only one entry is there.
	 * 
	 * @param modelName
	 *            Name of model used to create new MetaSelect name.
	 * @param field
	 *            MetaField to update with MetaSelect.
	 * @param row
	 *            Excel sheet row containing data.
	 * @return Updated MetaField.
	 * @throws AxelorException 
	 */
	private MetaField setSelectionField(String modelName, MetaField field, MetaModule module) throws AxelorException {

		String[] selection = getSelection(modelName, field.getName());
		
		MetaSelect metaSelect = metaSelectRepo.findByName(selection[0]);
		
		if (selection[1] != null) {

			if(metaSelect == null) {
				metaSelect = new MetaSelect(selection[0]);
				metaSelect.setCustomised(true);
				metaSelect.setMetaModule(module);
			}
			else{
				metaSelect.clearItems();
			}
	
			String[] option = selection[1].split(",");
			String selectFr = getValue(row, SELECT_FR);
			String[] optionFr = null;
			if (selectFr != null) {
				optionFr = selectFr.split(",");
			}
	
			for (Integer count = 0; count < option.length; count++) {
				MetaSelectItem metaSelectItem = new MetaSelectItem();
				String title = option[count];
				String titleFR = null;
				if (optionFr != null && optionFr.length > count) {
					titleFR = optionFr[count];
				}
				if (title.contains(":")) {
					String[] values = title.split(":");
					if (values.length > 1) {
						metaSelectItem.setValue(values[0]);
						metaSelectItem.setTitle(values[1]);
						if (titleFR != null && titleFR.contains(":")) {
							String[] valuesFr = titleFR.split(":");
							if (valuesFr.length > 1){
								addTranslation(values[1], valuesFr[1], "fr" );
							}
						}
					}
				}
				else {
					metaSelectItem.setValue(count.toString());
					metaSelectItem.setTitle(title.trim());
					if (titleFR != null) {
						addTranslation(title, titleFR, "fr" );
					}
				}
				metaSelectItem.setOrder(count);
				metaSelect.addItem(metaSelectItem);
	
			}
		}
		
		if (metaSelect != null) {
			field.setMetaSelect(metaSelect);
		}

		return field;
	}
	
	private String[] getSelection(String modelName, String fieldName) throws AxelorException {
		
		String selection = getValue(row, SELECT);
		if (selection == null) {
			selection = getValue(row, SELECT_FR);
		}

		if (Strings.isNullOrEmpty(selection)) {
			throw new AxelorException(I18n
					.get("Blank selection for object: %s, row: %s"), 1,
					modelName, row.getRowNum() + 1);
		}
		selection = selection.trim();
		
		String name = null;
		if (selection.contains("(")) {
			String[] select = selection.split("\\(");
			name = select[0];
			if (select.length > 1) {
				if (select[1].endsWith(")")) {
					select[1] = select[1].substring(0, select[1].length()-1);
				}
				selection = select[1];
			}
			else {
				selection = null;
			}
		}
		
		if (name == null) {
			name = inflector.dasherize(modelName) + "-"
					+ inflector.dasherize(fieldName) + "-select";
			name = name.replace("-", ".");
		}
		
		return new String[] {name, selection};
	}

	/**
	 * Method set 'typeName' of MetaField, it convert simple type names used in
	 * sheet into ADK supported type Names. Like it convert 'int' to 'Integer'
	 * and 'char' to 'String'.
	 * 
	 * @param fieldType
	 *            Simple field type.
	 * @param field
	 *            MetaField to update.
	 * @param row
	 *            Excel sheet row to find reference model in case of relational
	 *            field.
	 * @return Updated MetaField.
	 * @throws AxelorException 
	 */
	private MetaField updateFieldTypeName(String fieldType, String refModel,
			MetaField field, String module) throws AxelorException {
		
		if (referenceTypes.contains(fieldType) || fieldType.equals("file")) {
			if (fieldType.equals("file")) {
				refModel = "MetaFile";
			} else {
				if (Strings.isNullOrEmpty(refModel)) {
					throw new AxelorException(
							I18n.get("No reference model found for field : %s model: %s"),
									1, field.getLabel(), field.getMetaModel()
											.getName());
				}
				refModel = refModel.trim();
				refModel = inflector.camelize(refModel);
				updateModuleMap(refModel, module);
			}
			field.setTypeName(refModel);
			field.setRelationship(relationshipMap.get(fieldType));
		} else {
			String typeName = modelBuilderService.getFieldTypeName(fieldTypes.get(fieldType));
			field.setTypeName(typeName);
			if (typeName.equals("String") 
					&& refModel != null
					&& refModel.equals("name")) {
				field.setNameColumn(true);
			}
		}

		return field;

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

	private Integer getFieldSequence(Long modelId) {

		Integer seq = 0;

		if (fieldSeqMap.containsKey(modelId)) {
			seq = fieldSeqMap.get(modelId);
		}

		fieldSeqMap.put(modelId, seq + 1);

		return seq;

	}

	@Transactional
	public void createGridView(String module, MetaModel model) {

		String viewName = ViewLoaderService.getDefaultViewName(model.getName(),
				"grid");

		ViewBuilder viewBuilder = viewLoaderService.getViewBuilder(module, viewName, "grid");
		if (viewBuilder == null) {
			viewBuilder = new ViewBuilder(viewName);
			viewBuilder.setMetaModule(getModule(module));
		}

		viewBuilder.setViewType("grid");
		viewBuilder.setMetaModel(model);
		viewBuilder.setModel(model.getFullName());
		viewBuilder.setEdited(true);
		String title = model.getTitle();
		if(Strings.isNullOrEmpty(title)) {
			title = model.getName();
		}
		viewBuilder.setTitle(title);
		viewBuilder.clearViewItemList();
		
		int seq = 0;
		for (MetaField field : gridViewMap.get(module).get(model.getName())) {
			ViewItem viewItem = new ViewItem(field.getName());
			viewItem.setFieldType(field.getFieldType());
			viewItem.setMetaField(field);
			viewItem.setSequence(seq);
			viewBuilder.addViewItemListItem(viewItem);
			
			seq++;
		}

		viewBuilderRepo.save(viewBuilder);

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
	
	private void generateGridView() throws AxelorException {
		
		if (!viewImporterService.getViewActionMap().isEmpty()) {
			throw new AxelorException(I18n.get("Wizard views not found: %s"), 1,
					viewImporterService.getViewActionMap().keySet());
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
					if (fields != null && !fields.isEmpty()) {
						createGridView(module, model);
					}
					else {
						viewLoaderService.getDefaultGrid(module, model, true);
					}
				}
		        
			}
		}
	}
	
	
}
