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
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.axelor.studio.db.DataManager;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import com.axelor.studio.service.ViewLoaderService;
import com.axelor.studio.service.builder.ModelBuilderService;
import com.axelor.studio.service.data.DataCommonService;
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
	
	private Map<String, List<String>> updateMap;
	
	private Map<String, List<String>> replaceMap;

	private Map<String, List<String>> modelMap;

	private Map<String, List<MetaField>> listViewMap;
	
	private Map<Long, Integer> fieldSeqMap;
	
	private Map<String, MetaModel> nestedModels;
	
	private boolean replace = true;
	
	@Inject
	private MetaSelectRepository metaSelectRepo;
	
	@Inject
	private DataValidatorService validatorService;
	
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

		modelMap = new HashMap<String, List<String>>();
		fieldSeqMap = new HashMap<Long, Integer>();
		listViewMap = new HashMap<String, List<MetaField>>();
		updateMap = new HashMap<String, List<String>>();
		replaceMap  = new HashMap<String, List<String>>();
		nestedModels = new HashMap<String, MetaModel>();
		
		try {
			
			FileInputStream fis = new FileInputStream(inputFile);
			XSSFWorkbook workBook = new XSSFWorkbook(fis);
			
			File logFile = validatorService.validate(workBook);
			if(logFile != null){
				return logFile;
			}
			
			viewImporterService.clear();
			
			addModelsToUpdate(workBook.getSheetAt(0));
			proccessSheet(workBook);

			if (!viewImporterService.getViewActionMap().isEmpty()) {
				throw new AxelorException(I18n.get("Wizard views not found: %s"), 1,
						viewImporterService.getViewActionMap().keySet());
			}

			log.debug("List view specified for models: {}",
					listViewMap.keySet());
			for (String name : modelMap.keySet()) {
				MetaModel model = metaModelRepo.findByName(name);
				model = clearModel(model);
				if (model != null && modelMap.get(name).size() > 1) {
					if (listViewMap.containsKey(name)) {
						createListView(model);
					} else {
						viewLoaderService.getDefaultGrid(model, true);
					}
				}
			}

		} catch (IOException e) {
			throw new AxelorException(e, 5);
		}
		
		return null;

	}

	private void addModelsToUpdate(XSSFSheet sheet) {
		
		Iterator<Row> rowIter = sheet.rowIterator();
		if (rowIter.hasNext()) {
			rowIter.next();
		}
		
		while (rowIter.hasNext()) {
			Row row = rowIter.next();
			String model = getValue(row, 0);
			String type = getValue(row, 2);
			if (model != null && type != null) {
				
				if (type.equals("A")) {
					if (!updateMap.containsKey(model)) {
						updateMap.put(model, new ArrayList<String>());
					}
					String view = getValue(row, 1);
					if (view != null) {
						updateMap.get(model).add(view);
					}
				}
				else if (type.equals("R")){
					if (!replaceMap.containsKey(model)) {
						replaceMap.put(model, new ArrayList<String>());
					}
					String view = getValue(row, 1);
					if (view != null) {
						replaceMap.get(model).add(view);
					}
				}
				
			}
		}
		
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
		sheetIter.next();
		
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

		Row row = rowIter.next();
		String name = getValue(row, MODEL);
		String module = getValue(row, MODULE);
		String view = getValue(row, VIEW);
		String parentField = null;
		
		String modelName = name;
		if (name != null && name.contains("(")) {
			String[] names = name.split("\\(");
			modelName = names[0];
			parentField = names[1].replace(")", "");
		}
		
		replace = true;
		if (noUpdate(module, view, modelName)){
			extractRow(rowIter);
			return;
		}
		
		MetaModel model = null;
		if (!Strings.isNullOrEmpty(modelName)) {
			model = getModel(modelName);
			if (parentField != null && !nestedModels.containsKey(name)) {
				nestedModels.put(name, getNestedModel(model, parentField));
			}
		}

		String[] basic = getBasic(row, parentField);
		MetaField metaField = null;
		if (model != null && fieldTypes.containsKey(basic[0])) {
			
			if (!replace && !getValue(row, MODULE).startsWith("*")) {
				extractRow(rowIter);
				return;
			}
			
			if (parentField == null) {
				metaField = createMetaField(model, basic, row);
			}
			else {
				MetaModel nestedModel = nestedModels.get(name);
				metaField = createMetaField(nestedModel, basic, row);
				modelName = nestedModel.getName();
			}
			String addToList = getValue(row, GRID);
			if (addToList != null && addToList.equalsIgnoreCase("x")) {
				if (!listViewMap.containsKey(modelName)) {
					listViewMap.put(modelName, new ArrayList<MetaField>());
				}
				listViewMap.get(modelName).add(metaField);
			}
		}

		if (!Strings.isNullOrEmpty(basic[0]) && !ignoreTypes.contains(basic[0])) {
			viewImporterService.addViewElement(model, basic, row, metaField, replace);
		}

		extractRow(rowIter);
	}
	
	private boolean noUpdate(String module, String view, String model) {
		
		if (module == null || module.equals(configService.getModuleName())) {
			return false;
		}
		
		if (view == null) {
			return true;
		}
		
		List<String> modify = updateMap.get(model);
		
		if (modify != null && (modify.isEmpty() || modify.contains(view))) {
			replace = false;
			return false;
		}
		
		modify = replaceMap.get(model);
		
		if (modify != null && (modify.isEmpty() || modify.contains(view))) {
			return false;
		}
		
		
		return true;
	}

	private String[] getBasic(Row row, String parentField) {

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

		if (Strings.isNullOrEmpty(name) 
				&& !Strings.isNullOrEmpty(title) 
				&& !fieldType.equals("label")) {
			name = getFieldName(title);
		}

		return new String[] { fieldType, ref, name, title, parentField };
	}
	
	private MetaModel getNestedModel(MetaModel parentModel, String nestedField) {
		
		log.debug("Search for nested field: {}, model: {}", nestedField, parentModel.getName());
		
		MetaField metaField = metaFieldRepo.all()
				.filter("self.name = ?1 and self.metaModel = ?2", nestedField, parentModel)
				.fetchOne();
		
		if (metaField != null && metaField.getRelationship() != null) {
			log.debug("Field found with type: {}", metaField.getTypeName());
			return getModel(metaField.getTypeName());
		}
		
		return parentModel;
	}
	
	@Transactional
	public MetaField createMetaField(MetaModel metaModel, String[] basic,
			Row row) throws AxelorException {

		log.debug("Create field with basic: {}", Arrays.asList(basic));

		String name = basic[2];
		
		if (Strings.isNullOrEmpty(name)) {
			throw new AxelorException(
							I18n.get("No title or name for field row number: %s, model: %s"), 
							1, row.getRowNum() + 1, metaModel.getName());
		}
		
		if (ModelBuilderService.reserveFields.contains(name)) {
			throw new AxelorException(
					I18n.get("Can't use reserve word for name. Row number: %s, model: %s"), 
					1, row.getRowNum() + 1, metaModel.getName());
		}
		
		String objName = metaModel.getName();
		log.debug("Create Meta field: {}", name);

		MetaField field = metaFieldRepo
				.all()
				.filter("self.name = ?1 and self.metaModel = ?2", name,
						metaModel).fetchOne();
		
		log.debug("Field found: {}", field);
		if (field == null) {
			field = new MetaField();
			field.setName(name);
			field.setLabel(basic[3]);
			field.setCustomised(true);
		}
		else if (!field.getCustomised()) {
			field.setCustomised(true);
			field.setExisting(true);
		}
		if (fieldTypes.containsKey(basic[1])) {
			field.setFieldType(fieldTypes.get(basic[1]));
		}
		else {
			field.setFieldType(fieldTypes.get(basic[0]));
		}
		modelMap.get(objName).add(name);
		

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
			field = setSelectionField(objName, field, row);
			break;
		case "multiselect":
			field = setSelectionField(objName, field, row);
			field.setMultiselect(true);
			break;

		}
		
		field.setHelpText(getValue(row, HELP));
		field.setMetaModel(metaModel);
		field.setSequence(getFieldSequence(metaModel.getId()));
		field = updateFieldTypeName(basic[0], basic[1], field);

		return metaFieldRepo.save(field);

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
	private MetaField setSelectionField(String modelName, MetaField field,
			Row row) throws AxelorException {

		String selectName = getValue(row, SELECT);

		if (Strings.isNullOrEmpty(selectName)) {
			throw new AxelorException(I18n
					.get("Blank selection for object: %s, field: %s, row: %s"), 1,
					modelName, field.getLabel(), row.getRowNum() + 1);
		}
		selectName = selectName.trim();

		if(!selectName.contains(",") && !selectName.contains(":")){
			 MetaSelect metaSelect = metaSelectRepo.findByName(selectName);
			 if (metaSelect == null) {
				 throw new AxelorException(I18n
							.get("No selection found for object: %s, field: %s, row: %s"), 1,
					modelName, field.getLabel(), row.getRowNum() + 1);
			 }
			 field.setMetaSelect(metaSelect);
			 return field;
		}
		
		String name = inflector.dasherize(modelName) + "-"
				+ inflector.dasherize(field.getName()) + "-select";
		name = name.replace("-", ".");
		
		if (selectName.contains("(")) {
			String[] select = selectName.split("\\(");
			name = select[0];
			if (select.length > 1) {
				if (select[1].endsWith(")")) {
					select[1] = select[1].substring(0, select[1].length()-1);
				}
				selectName = select[1];
			}
			else {
				selectName = null;
			}
		}

		MetaSelect metaSelect = metaSelectRepo.findByName(name);
		
		if (selectName != null) {

			if(metaSelect == null) {
				metaSelect = new MetaSelect(name);
				metaSelect.setCustomised(true);
			}
			else{
				metaSelect.clearItems();
			}
	
			String[] option = selectName.split(",");
	
			for (Integer count = 0; count < option.length; count++) {
				MetaSelectItem metaSelectItem = new MetaSelectItem();
				String title = option[count];
				if (title.contains(":")) {
					String[] values = title.split(":");
					metaSelectItem.setValue(values[0]);
					metaSelectItem.setTitle(values[1]);
				}
				else {
					metaSelectItem.setValue(count.toString());
					metaSelectItem.setTitle(title.trim());
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
			MetaField field) throws AxelorException {
		
		log.debug("Field type: {}", fieldType);
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
				if (!modelMap.containsKey(refModel)) {
					getModel(refModel);
				}
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
	public MetaModel getModel(String name) {
		
		name = inflector.camelize(name);
		
		MetaModel model = metaModelRepo.findByName(name);
		
		if (modelMap.containsKey(name)) {
			return model;
		}
		
		if (model == null) {
			model = new MetaModel(name);
			MetaField metaField = new MetaField("wkfStatus", false);
			metaField.setTypeName("String");
			metaField.setFieldType("string");
			metaField.setLabel("Status");
			metaField.setCustomised(true);
			model.addMetaField(metaField);
		}

		if (!modelMap.containsKey(name)) {
			modelMap.put(name, new ArrayList<String>());
			modelMap.get(name).add("wkfStatus");
		}

		if (model.getPackageName() == null) {
			model.setPackageName("com.axelor.apps.custom.db");
			model.setFullName("com.axelor.apps.custom.db." + model.getName());
		}
		model.setCustomised(true);
		model.setEdited(true);

		return metaModelRepo.save(model);
	}


	@Transactional
	public MetaModel clearModel(MetaModel model) {

		List<String> fieldNames = modelMap.get(model.getName());
		
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
				if (field.getExisting()) {
					field.setCustomised(false);
				}
				else {
					fieldIter.remove();
				}
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
	public void createListView(MetaModel model) {

		String viewName = ViewLoaderService.getDefaultViewName(model.getName(),
				"grid");

		ViewBuilder viewBuilder = viewBuilderRepo.findByName(viewName);
		if (viewBuilder == null) {
			viewBuilder = new ViewBuilder(viewName);
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
		for (MetaField field : listViewMap.get(model.getName())) {
			ViewItem viewItem = new ViewItem(field.getName());
			viewItem.setFieldType(field.getFieldType());
			viewItem.setMetaField(field);
			viewItem.setSequence(seq);
			viewBuilder.addViewItemListItem(viewItem);
			
			seq++;
		}

		viewBuilderRepo.save(viewBuilder);

	}
	
}
