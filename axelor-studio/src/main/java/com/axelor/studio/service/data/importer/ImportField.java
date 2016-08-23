package com.axelor.studio.service.data.importer;

import java.math.BigDecimal;

import org.apache.poi.ss.usermodel.Row;

import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.axelor.studio.service.builder.ModelBuilderService;
import com.axelor.studio.service.data.CommonService;
import com.axelor.studio.service.data.TranslationService;
import com.axelor.studio.utils.Namming;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ImportField extends CommonService {
	
	@Inject
	private MetaFieldRepository metaFieldRepo;
	
	@Inject
	private TranslationService translationService;
	
	@Inject
	private ModelBuilderService modelBuilderService;
	
	@Inject
	private MetaSelectRepository metaSelectRepo;

	@Transactional
	public MetaField importField(Row row, String[] basic, MetaModel metaModel, 
			MetaModule metaModule, Integer sequence) throws AxelorException {

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
		
		translationService.addTranslation(basic[3], getValue(row, TITLE_FR), "fr");
		
		if (CommonService.FIELD_TYPES.containsKey(basic[1])) {
			field.setFieldType(CommonService.FIELD_TYPES.get(basic[1]));
		}
		else {
			field.setFieldType(CommonService.FIELD_TYPES.get(basic[0]));
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
				field = setSelectionField(row, model, field, metaModule);
				break;
			case "multiselect":
				field = setSelectionField(row, model, field, metaModule);
				field.setMultiselect(true);
				break;

		}
		
		String help = getValue(row, HELP);
		String helpFr = getValue(row, HELP_FR);
		if (help == null) {
			help = helpFr;
		}
		else {
			translationService.addTranslation(help, helpFr, "fr");
		}
		
		field.setHelpText(help);
		field.setMetaModel(metaModel);
		field.setMetaModule(metaModule);
		field.setSequence(sequence);
		
		field = updateFieldTypeName(basic[0], basic[1], field, metaModule.getName());
		
		field = processFormula(field, getValue(row, FORMULA));
		

		return metaFieldRepo.save(field);

	}
	
	private MetaField processFormula(MetaField field, String formula) {
		
		if (formula == null) {
			return field;
		}
		
		for (String expr: formula.split(",")) {
			String[] exprs = expr.split(":");
			if (exprs.length < 2) {
				continue;
			}
			
			String type = field.getTypeName();
			
			switch(exprs[0]) {
				case "mappedBy":
					field.setMappedBy(exprs[1]);
					break;
				case "max":
					if (type.equals("Integer") || type.equals("String")) {
						field.setIntegerMax(Integer.parseInt(exprs[1]));
					}
					else if (type.equals("BigDecimal")) { 
						field.setDecimalMax(new BigDecimal(exprs[1]));
					}
					break;
				case "min":
					if (type.equals("Integer") || type.equals("String")) {
						field.setIntegerMin(Integer.parseInt(exprs[1]));
					}
					else if (type.equals("BigDecimal")) { 
						field.setDecimalMin(new BigDecimal(exprs[1]));
					}
					break;
			}
			
		}
		
		return field;
		
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
	private MetaField setSelectionField(Row row, String modelName, MetaField field, MetaModule module) throws AxelorException {

		String[] selection = getSelection(row, modelName, field.getName());
		
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
					titleFR = optionFr[count].trim();
				}
				if (title.contains(":")) {
					String[] values = title.split(":");
					if (values.length > 1) {
						metaSelectItem.setValue(values[0].trim());
						metaSelectItem.setTitle(values[1].trim());
						if (titleFR != null && titleFR.contains(":")) {
							String[] valuesFr = titleFR.split(":");
							if (valuesFr.length > 1){
								translationService.addTranslation(values[1].trim(), valuesFr[1].trim(), "fr" );
							}
						}
					}
				}
				else {
					Integer val = count + 1;
					metaSelectItem.setValue(val.toString());
					metaSelectItem.setTitle(title.trim());
					if (titleFR != null) {
						translationService.addTranslation(title.trim(), titleFR, "fr" );
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
	
	private String[] getSelection(Row row, String modelName, String fieldName) throws AxelorException {
		
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
		
		if (CommonService.RELATIONAL_TYPES.containsKey(fieldType)) {
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
			}
			field.setTypeName(refModel);
			field.setRelationship(CommonService.RELATIONAL_TYPES.get(fieldType));
		} else {
			String typeName = modelBuilderService.getFieldTypeName(CommonService.FIELD_TYPES.get(fieldType));
			field.setTypeName(typeName);
			if (typeName.equals("String") 
					&& refModel != null
					&& refModel.equals("name")) {
				field.setNameColumn(true);
			}
		}

		return field;

	}
	
}
