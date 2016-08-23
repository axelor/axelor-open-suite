package com.axelor.studio.service.data.importer;

import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.service.data.CommonService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ImportModel extends CommonService {
	
	private final static Logger log = LoggerFactory.getLogger(ImportModel.class);
	
	@Inject
	private MetaFieldRepository metaFieldRepo;
	
	@Inject
	private ImportField importField;
	
	@Inject
	private MetaModelRepository metaModelRepo;
	
	private ImportService importService;
	
	public void importModel(ImportService importService, Row row, MetaModule metaModule) throws AxelorException {
		
		String name = getValue(row, MODEL);
		if (name == null) {
			return;
		}
		
		this.importService = importService;
		
		String[] names = name.split("\\(");
		MetaModel model = getModel(names[0], metaModule);
		
		MetaModel nestedModel = null;
		String parentField = null;
		if (names.length > 1) {
			parentField = names[1].replace(")", "");
			nestedModel = importService.getNestedModels(name);
			if (nestedModel == null) {
				nestedModel = createNestedModel(metaModule, model, parentField);
				importService.addNestedModel(name, nestedModel);
			}
		}

		String[] basic = getBasic(row, parentField);
		
		MetaField metaField = null;
		if (CommonService.FIELD_TYPES.containsKey(basic[0]) && !basic[2].startsWith("$")) {
			if (nestedModel != null) {
				metaField = addField(basic, row, nestedModel, metaModule);
			}
			else {
				metaField = addField(basic, row, model, metaModule);
			}
		}

		if (!Strings.isNullOrEmpty(basic[0]) 
				&& (!CommonService.IGNORE_TYPES.contains(basic[0]) || basic[0].equals("empty"))) {
			importService.addView(model, basic, row, metaField);
		}
	}
	
	private MetaField addField(String[] basic, Row row, MetaModel model, MetaModule metaModule) throws AxelorException {
		
		Integer sequence = importService.getFieldSeq(model.getId());
		MetaField metaField = importField.importField(row, basic, model, metaModule, sequence);
		if (metaField.getCustomised()) {
			importService.updateModuleMap(metaModule.getName(), model.getName(), basic[2]);
		}
		
		importService.addGridField(metaModule.getName(), model.getName(), metaField, getValue(row, GRID));
		
		return metaField;
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
		
		if(CommonService.FR_MAP.containsKey(fieldType)) {
			fieldType = CommonService.FR_MAP.get(fieldType);
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
	
	private MetaModel createNestedModel(MetaModule module, MetaModel parentModel, String nestedField) {
		
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
		
		importService.updateModuleMap(module.getName(), name, null);
		
		if (model == null) {
			model = new MetaModel(name);
			model.setMetaModule(module);
			MetaField metaField = new MetaField("wkfStatus", false);
			metaField.setTypeName("String");
			metaField.setFieldType("string");
			metaField.setLabel("Status");
			metaField.setCustomised(true);
			model.addMetaField(metaField);
			importService.updateModuleMap(module.getName(), name, "wkfStatus");
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
	
}
