package com.axelor.studio.service.data.importer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.common.Inflector;
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

public class ModelImporter {
	
	private final static Logger log = LoggerFactory.getLogger(ModelImporter.class);
	
	@Inject
	private MetaFieldRepository metaFieldRepo;
	
	@Inject
	private FieldImporter fieldImporter;
	
	@Inject
	private MetaModelRepository metaModelRepo;
	
	@Inject
	private CommonService commonService;
	
	private ImporterService importerService;
	
	public void importModel(ImporterService importerService, String[] row, int rowNum,  MetaModule metaModule) throws AxelorException {
		
		String name = row[CommonService.MODEL];
		if (name == null) {
			return;
		}
		
		this.importerService = importerService;
		
		String[] names = name.split("\\(");
		MetaModel model = getModel(names[0], metaModule);
		
		MetaModel nestedModel = null;
		String parentField = null;
		if (names.length > 1) {
			parentField = names[1].replace(")", "");
			nestedModel = importerService.getNestedModels(name);
			if (nestedModel == null) {
				nestedModel = createNestedModel(metaModule, model, parentField);
				importerService.addNestedModel(name, nestedModel);
			}
		}

		String[] basic = getBasic(row, parentField);
		
		MetaField metaField = null;
		if (CommonService.FIELD_TYPES.containsKey(basic[0]) && !basic[2].startsWith("$")) {
			if (nestedModel != null) {
				metaField = addField(basic, row, rowNum, nestedModel, metaModule);
			}
			else {
				metaField = addField(basic, row, rowNum, model, metaModule);
			}
		}

		if (!Strings.isNullOrEmpty(basic[0]) 
				&& (!CommonService.IGNORE_TYPES.contains(basic[0]) || basic[0].equals("empty"))) {
			importerService.addView(model, basic, row, rowNum, metaField);
		}
	}
	
	private MetaField addField(String[] basic, String[] row, int rowNum, MetaModel model, MetaModule metaModule) throws AxelorException {
		
		Integer sequence = importerService.getFieldSeq(model.getId());
		MetaField metaField = fieldImporter.importField(row, rowNum, basic, model, metaModule, sequence);
		if (metaField.getCustomised()) {
			importerService.updateModuleMap(metaModule.getName(), model.getName(), basic[2]);
		}
		
		String addGrid = row[CommonService.GRID];
		if (addGrid != null && addGrid.equalsIgnoreCase("x")) {
			importerService.addGridField(metaModule.getName(), model.getName(), metaField);
		}

		return metaField;
	}
	
	private String[] getBasic(String[] row, String parentField) {

		String fieldType = row[CommonService.TYPE];
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

		String name = row[CommonService.NAME];
		String title = row[CommonService.TITLE];
		String titleFr = row[CommonService.TITLE_FR];
		if (Strings.isNullOrEmpty(title)) {
			title = titleFr;
		}
		
		if (Strings.isNullOrEmpty(name) 
				&& !Strings.isNullOrEmpty(title) 
				&& !fieldType.equals("label")) {
			name = commonService.getFieldName(title);
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
		
		name = Inflector.getInstance().camelize(name);
		
		MetaModel model = metaModelRepo.findByName(name);
		
		importerService.updateModuleMap(module.getName(), name, null);
		
		if (model == null) {
			model = new MetaModel(name);
			model.setMetaModule(module);
			MetaField metaField = new MetaField("wkfStatus", false);
			metaField.setTypeName("String");
			metaField.setFieldType("string");
			metaField.setLabel("Status");
			metaField.setSequence(0);
			metaField.setCustomised(true);
			model.addMetaField(metaField);
			importerService.updateModuleMap(module.getName(), name, "wkfStatus");
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
