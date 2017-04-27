package com.axelor.studio.web;


import com.axelor.db.mapper.Mapper;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.inject.Inject;

public class ChartBuilderController {
	
	@Inject
	private MetaModelRepository metaModelRepo;
	
	public String getTarget(MetaField metaField) {
		
		String fieldName = metaField.getName();
		MetaModel targetModel = metaModelRepo.findByName(metaField.getTypeName());
		
		if (targetModel == null) {
			return fieldName;
		}
		
		MetaField targetField =  null;
		boolean name = false;
		boolean code = false;
		for (MetaField field : targetModel.getMetaFields()) {
			if (field.getNameColumn()) {
				targetField = field;
			}
			if (field.getName().equals("name")) {
				name = true;
			}
			if (field.getName().equals("code")) {
				code = true;
			}
		}
		
		if (targetField != null) {
			return fieldName + "." + targetField.getName();
		}
		
		try {
			Mapper mapper = Mapper.of(Class.forName(targetModel.getFullName()));
			if (mapper.getNameField() != null) {
				return fieldName + "." + mapper.getNameField().getName();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		if (name) {
			return fieldName + ".name";
		}
		
		if (code) {
			return fieldName + ".code";
		}
		
		return fieldName;
	}
}
