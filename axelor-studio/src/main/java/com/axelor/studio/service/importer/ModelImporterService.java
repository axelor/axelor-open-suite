package com.axelor.studio.service.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.MetaTranslation;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.axelor.studio.db.ModelImporter;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
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
public class ModelImporterService extends ImporterService {

	private final static Logger log = LoggerFactory
			.getLogger(ModelImporterService.class);

	private Map<String, String> i18nMap;

	private Map<String, List<String>> modelMap = new HashMap<String, List<String>>();

	private Map<String, List<MetaField>> listViewMap = new HashMap<String, List<MetaField>>();

	private Map<Long, Integer> fieldSeqMap = new HashMap<Long, Integer>();
	
	@Inject
	private MetaSelectRepository metaSelectRepo;
	
	@Inject
	private InputValidatorService validatorService;

	/**
	 * Root method to access the service. It will call other methods required to
	 * import model.
	 * 
	 * @param modelImporter
	 *            ModelImporter class that store all import meta file.
	 * @return Return true if import done successfully else false.
	 */
	public File importModels(ModelImporter modelImporter) {

		File inputFile = MetaFiles.getPath(modelImporter.getMetaFile())
				.toFile();

		if (!inputFile.exists()) {
			throw new ValidationException(I18n.get("Input file not exist"));
		}

		i18nMap = new HashMap<String, String>();
		modelMap = new HashMap<String, List<String>>();
		fieldSeqMap = new HashMap<Long, Integer>();
		listViewMap = new HashMap<String, List<MetaField>>();

		try {
			
			FileInputStream fis = new FileInputStream(inputFile);
			XSSFWorkbook workBook = new XSSFWorkbook(fis);
			
			File logFile = validatorService.validate(workBook);
			if(logFile != null){
				return logFile;
			}
			
			extractModels(workBook);
			proccessSheet(workBook);

			if (!viewImporterService.getViewActionMap().isEmpty()) {
				throw new ValidationException(String.format(
						I18n.get("Wizard views not found: %s"),
						viewImporterService.getViewActionMap().keySet()));
			}

			log.debug("List view specified for models: {}",
					listViewMap.keySet());
			for (String name : modelMap.keySet()) {
				clearModel(name);
				MetaModel model = metaModelRepo.findByName(name);
				if (model != null && modelMap.get(name).size() > 1) {
					if (listViewMap.containsKey(name)) {
						createListView(model);
					} else {
						viewLoaderService.getDefaultGrid(model, true);
					}
				}
			}

			generateTranslation("fr");

			viewImporterService.clear();

		} catch (IOException e) {
			e.printStackTrace();
			throw new ValidationException(e.getMessage());
		}
		
		return null;

	}

	private void extractModels(XSSFWorkbook modelBook) throws IOException {

		Iterator<XSSFSheet> sheetIter = modelBook.iterator();
		while (sheetIter.hasNext()) {
			XSSFSheet sheet = sheetIter.next();
			Iterator<Row> rowIter = sheet.rowIterator();
			if (rowIter.hasNext()) {
				rowIter.next();
			}
			while (rowIter.hasNext()) {
				Row row = rowIter.next();
				String name = getString(row.getCell(MODEL));
				if (!Strings.isNullOrEmpty(name)) {
					getModel(name);
				}
			}
		}

	}

	/**
	 * Method create list of MetaModels by reading given input file.
	 * 
	 * @param inputFile
	 *            Excel input file.
	 * @return List of MetaModels created.
	 * @throws IOException
	 *             Exception in file handling.
	 */
	private void proccessSheet(XSSFWorkbook workBook) throws IOException {

		Iterator<XSSFSheet> sheetIter = workBook.iterator();
		while (sheetIter.hasNext()) {
			XSSFSheet sheet = sheetIter.next();
			Iterator<Row> rowIter = sheet.rowIterator();
			if (rowIter.hasNext()) {
				rowIter.next();
			}
			extractRow(rowIter);
		}

	}

	/**
	 * Create MetaModel from excel sheet data.
	 * 
	 * @param sheet
	 *            Excel sheet to process
	 * @return MetaModel created
	 */

	private void extractRow(Iterator<Row> rowIter) {

		if (!rowIter.hasNext()) {
			return;
		}

		Row row = rowIter.next();
		String name = getString(row.getCell(MODEL));
		MetaModel model = null;
		if (!Strings.isNullOrEmpty(name)) {
			name = name.split("\\(")[0];
			name = inflector.camelize(name);
			model = metaModelRepo.findByName(name);
		}

		String[] basic = getBasic(row);
		MetaField metaField = null;
		if (model != null && typeMap.containsKey(basic[0])) {
			metaField = createMetaField(model, basic, row);
			String addToList = getString(row.getCell(LIST));
			if (addToList != null && addToList.equalsIgnoreCase("x")) {
				if (!listViewMap.containsKey(name)) {
					listViewMap.put(name, new ArrayList<MetaField>());
				}
				listViewMap.get(name).add(metaField);
			}
		}

		if (!Strings.isNullOrEmpty(basic[0]) && !ignoreTypes.contains(basic[0])) {
			viewImporterService.addViewElement(model, basic, row, metaField);
		}

		extractRow(rowIter);
	}

	private String[] getBasic(Row row) {

		String fieldType = getString(row.getCell(TYPE));
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

		String name = getString(row.getCell(NAME));
		String title = getTitle(row);

		if (Strings.isNullOrEmpty(name) 
				&& !Strings.isNullOrEmpty(title) 
				&& !fieldType.equals("label")) {
			name = getFieldName(title);
		}

		return new String[] { fieldType, ref, name, title };
	}

	@Transactional
	public MetaField createMetaField(MetaModel metaModel, String[] basic,
			Row row) {

		log.debug("Create field with basic: {}", Arrays.asList(basic));

		String name = basic[2];
		if (Strings.isNullOrEmpty(name)) {
			throw new ValidationException(
					String.format(
							I18n.get("No title or name for field row number: %s, model: %s"),
							row.getRowNum() + 1, metaModel.getName()));
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
		}
		field.setFieldType(typeMap.get(basic[0]));
		modelMap.get(objName).add(name);
		field.setCustomised(true);

		// if("string,decimal,integer".contains(typeMap.get(fieldType))){
		// updateMinMax(field, row, fieldType.equals("decimal"));
		// }

		updateCommonAttr(field, row);

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
		
		field.setHelpText(getString(row.getCell(HELP)));
		field.setMetaModel(metaModel);
		field.setSequence(getFieldSequence(metaModel.getId()));
		field = updateFieldTypeName(basic[0], basic[1], field);

		return metaFieldRepo.save(field);

	}

	private String getTitle(Row row) {

		String title = getString(row.getCell(TITLE));
		String titleTr = getString(row.getCell(TITLE_TR));

		if (Strings.isNullOrEmpty(title)) {
			return titleTr;
		}

		if (!Strings.isNullOrEmpty(title) && !Strings.isNullOrEmpty(titleTr)) {
			i18nMap.put(title, titleTr);
		}

		return title;
	}

	/**
	 * Update common attributes among fields like required, readonly..etc.
	 * 
	 * @param modelName
	 *            Model name used to create help translation key for a field.
	 * @param field
	 *            MetaField to update.
	 * @param row
	 *            Excel sheet row containing data.
	 */
	private void updateCommonAttr(MetaField field, Row row) {

		String required = getString(row.getCell(REQUIRED));
		if (required != null && required.equalsIgnoreCase("x")) {
			field.setRequired(true);
		} else {
			field.setRequired(false);
		}

		String readonly = getString(row.getCell(READONLY));
		if (readonly != null && readonly.equalsIgnoreCase("x")) {
			field.setReadonly(true);
		} else {
			field.setReadonly(false);
		}

		// cell = row.getCell(ATTR_HIDDEN);
		// if(cell != null && cell.getCellType() == Cell.CELL_TYPE_BOOLEAN){
		// Boolean hidden = cell.getBooleanCellValue();
		// if(hidden != nucell = row.getCell(ATTR_HIDDEN);
		// if(cell != null && cell.getCellType() == Cell.CELL_TYPE_BOOLEAN){
		// Boolean hidden = cell.getBooleanCellValue();
		// if(hidden != null && hidden){
		// field.setHidden(hidden);
		// }
		// }
		//
		// cell = row.getCell(ATTR_HELP);
		// if(cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING){
		// String help = cell.getStringCellValue();
		// if(!Strings.isNullOrEmpty(help)){
		// field.setHelpText(help);
		// }
		// }
		//
		// cell = row.getCell(ATTR_HELP_TR);
		// if(cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING){
		// String helpTr = cell.getStringCellValue();
		// if(!Strings.isNullOrEmpty(helpTr)){
		// i18nMap.put("help:" + modelName + ":" + field.getName(),
		// helpTr.trim());
		// }
		// }ll && hidden){
		// field.setHidden(hidden);
		// }
		// }
		//
		// cell = row.getCell(ATTR_HELP);
		// if(cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING){
		// String help = cell.getStringCellValue();
		// if(!Strings.isNullOrEmpty(help)){
		// field.setHelpText(help);
		// }
		// }
		//
		// cell = row.getCell(ATTR_HELP_TR);
		// if(cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING){
		// String helpTr = cell.getStringCellValue();
		// if(!Strings.isNullOrEmpty(helpTr)){
		// i18nMap.put("help:" + modelName + ":" + field.getName(),
		// helpTr.trim());
		// }
		// }
	}

	/**
	 * Special method to update min and max property for field types,
	 * string,decimal and integer. String and Integer type of field share same
	 * integer min/max property.
	 * 
	 * @param field
	 *            MetaField to update.
	 * @param row
	 *            Excel sheet row containing data.
	 * @param decimal
	 *            Boolean to check if to process decimal min/max or integer
	 *            min/max.
	 */
	// private void updateMinMax(MetaField field, Row row, boolean decimal){
	//
	// Cell cell = row.getCell(ATTR_MIN);
	// if(cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
	// Double min = cell.getNumericCellValue();
	// if(decimal){
	// field.setDecimalMin(new BigDecimal(min));
	// }
	// else{
	// field.setIntegerMin(min.intValue());
	// }
	// }
	//
	// cell = row.getCell(ATTR_MAX);
	// if(cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
	// Double max = cell.getNumericCellValue();
	// if(decimal){
	// field.setDecimalMax(new BigDecimal(max));
	// }
	// else{
	// field.setIntegerMax(max.intValue());
	// }
	// }
	//
	// }

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
	 */
	private MetaField setSelectionField(String modelName, MetaField field,
			Row row) {

		String selectName = getString(row.getCell(SELECT));
		String selectTr = getString(row.getCell(SELECT_TR));
		String[] titlesTr = null;

		if (Strings.isNullOrEmpty(selectName)) {
			selectName = selectTr;
		} else if (selectTr != null) {
			titlesTr = selectTr.split(",");
		}

		if (Strings.isNullOrEmpty(selectName)) {
			throw new ValidationException(String.format(I18n
					.get("Blank selection for object: %s, field: %s, row: %s"),
					modelName, field.getLabel(), row.getRowNum() + 1));
		}

		selectName = selectName.trim();

		// if(!selectName.contains(",")){
		// MetaSelect metaSelect = metaSelectRepo.findByName(selectName);
		// if(metaSelect == null){
		// errorMessage =
		// String.format(I18n.get("No meta select reference found: %s, field: %s, reference: %s"),
		// modelName, field.getLabel(), selectName);
		// return null;
		// }
		// field.setMetaSelect(metaSelect);
		// }
		// else{
		String name = inflector.dasherize(modelName) + "-"
				+ inflector.dasherize(field.getName()) + "-select";
		name = name.replace("-", ".");

		MetaSelect metaSelect = metaSelectRepo.findByName(name);
		
		if(metaSelect == null) {
			metaSelect = new MetaSelect(name);
		}
		else{
			metaSelect.clearItems();
		}

		String[] titles = selectName.split(",");

		for (Integer count = 0; count < titles.length; count++) {
			MetaSelectItem metaSelectItem = new MetaSelectItem();
			metaSelectItem.setValue(count.toString());
			metaSelectItem.setTitle(titles[count].trim());
			metaSelectItem.setOrder(count);
			metaSelect.addItem(metaSelectItem);

			if (titlesTr != null && titlesTr.length > count) {
				i18nMap.put(titles[count].trim(), titlesTr[count].trim());
			}
		}

		field.setMetaSelect(metaSelect);

		// }

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
	 */
	private MetaField updateFieldTypeName(String fieldType, String refModel,
			MetaField field) {
		
		log.debug("Field type: {}", fieldType);
		if ("m2m,o2m,m2o,file".contains(fieldType)) {
			if (fieldType.equals("file")) {
				refModel = "MetaFile";
			} else {
				if (Strings.isNullOrEmpty(refModel)) {
					throw new ValidationException(
							String.format(
									I18n.get("No reference model found for field : %s model: %s"),
									field.getLabel(), field.getMetaModel()
											.getName()));
				}
				refModel = refModel.trim();
				refModel = inflector.camelize(refModel);
				if (!modelMap.containsKey(refModel)
						&& metaModelRepo.findByName(refModel) == null) {
					throw new ValidationException(
							String.format(
									I18n.get("No reference model: %s found for field : %s model: %s"),
									refModel, field.getLabel(), field
											.getMetaModel().getName()));
				}
			}
			field.setTypeName(refModel);
			field.setRelationship(relationshipMap.get(fieldType));
		} else {
			String typeName = modelBuilderService.getFieldTypeName(typeMap.get(fieldType));
			field.setTypeName(typeName);
			if (typeName != null && typeName.equals("String") && refModel != null
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
		String title = null;
		if (name.contains("(")) {
			String[] names = name.split("\\(");
			name = names[0];
			title = names[1].replace(")", "");
		}
		name = inflector.camelize(name);
		MetaModel model = metaModelRepo.findByName(name);
		if (model == null) {
			model = new MetaModel(name);
			MetaField metaField = new MetaField("wkfStatus", false);
			metaField.setTypeName("String");
			metaField.setFieldType("string");
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
		model.setTitle(title);

		return metaModelRepo.save(model);
	}

	/**
	 * Create translation (MetaTranslation) records for language selected in
	 * ModelImporter. From translation map(i18nMap) created in extractField
	 * method.
	 * 
	 * @param language
	 */
	@Transactional(rollbackOn = { Exception.class })
	public void generateTranslation(String language) {

		for (String key : i18nMap.keySet()) {

			MetaTranslation metaTranslation = metaTranslationRepo.findByKey(
					key, language);
			if (metaTranslation == null) {
				metaTranslation = new MetaTranslation();
				metaTranslation.setKey(key);
				metaTranslation.setLanguage(language);
			}

			metaTranslation.setMessage(i18nMap.get(key));

			metaTranslationRepo.save(metaTranslation);
		}

	}

	@Transactional
	public void clearModel(String model) {

		List<String> fieldNames = modelMap.get(model);

		// if(fieldNames.size() == 1){
		// errorMessage =
		// String.format(I18n.get("No fields defined for model: %s"), model);
		// return;
		// }

		List<MetaField> fields = metaFieldRepo
				.all()
				.filter("self.name not in ?1 and self.metaModel.name = ?2",
						fieldNames, model).fetch();

		Iterator<MetaField> fieldIter = fields.iterator();

		while (fieldIter.hasNext()) {
			MetaField field = fieldIter.next();
			List<ViewItem> viewItems = viewItemRepo.all()
					.filter("self.metaField = ?1", field).fetch();

			Iterator<ViewItem> viewItemIter = viewItems.iterator();

			while (viewItemIter.hasNext()) {
				ViewItem item = viewItemIter.next();
				viewItemRepo.remove(item);
				viewItemIter.remove();
			}

			metaFieldRepo.remove(field);
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

	@Transactional
	public void createListView(MetaModel model) {

		String viewName = viewLoaderService.getDefaultViewName(model.getName(),
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
