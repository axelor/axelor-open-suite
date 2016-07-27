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
package com.axelor.meta.db.repo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.studio.service.ModuleRecorderService;
import com.google.inject.Inject;

public class StudioMetaModelRepository extends MetaModelRepository {

	@Inject
	private MetaFieldRepository metaFieldRepo;
	
	@Inject
	private ModuleRecorderService recorderService;

	/**
	 * Extended to validate field name, as it should not contains space and must
	 * be camel case.
	 */
	@Override
	public MetaModel save(MetaModel metaModel) throws ValidationException {
		
		if (metaModel.getName().equals("Object")) {
			throw new ValidationException(
					I18n.get("Invalid model name 'Object'"));
		}
		
		List<MetaField> metaFields = metaModel.getMetaFields();
		
		List<String> defaultFields = new ArrayList<String>();
		defaultFields.add("id");
		defaultFields.add("createdOn");
		defaultFields.add("updatedOn");
		defaultFields.add("createdBy");
		defaultFields.add("updatedBy");

		if (metaFields != null && !metaFields.isEmpty()) {
			String model = metaModel.getName();
			for (MetaField field : metaFields) {
				String name = field.getName();
				if (defaultFields.contains(name)) {
					defaultFields.remove(name);
				}
				if (name.contains(" ")) {
					throw new ValidationException(
							I18n.get("Field name must not contains space: ")
									+ name + " Model: " + model);
				} else if (Character.isUpperCase(name.charAt(0))) {
					throw new ValidationException(
							I18n.get("Field name must follow camel case pattern: ")
									+ name + " Model: " + model);
				}
			}

		}

		if (metaFields == null) {
			metaFields = new ArrayList<MetaField>();
		}

		addDefaultFields(metaModel, metaFields, defaultFields);

		metaModel.setMetaFields(metaFields);
		
		recorderService.setUpdateServer();

		return super.save(metaModel);

	}

	public void addDefaultFields(MetaModel metaModel,
			List<MetaField> metaFields, List<String> defaultFields) {

		for (String name : defaultFields) {

			Map<String, Object> values = new HashMap<String, Object>();

			String[] val = null;
			switch (name) {
			case "id":
				val = new String[] { "id", "Id", "Long" };
				break;
			case "createdOn":
				val = new String[] { "createdOn", "Created on", "LocalDateTime" };
				break;
			case "updatedOn":
				val = new String[] { "updatedOn", "Updated on", "LocalDateTime" };
				break;
			case "createdBy":
				val = new String[] { "createdBy", "Created By", "User" };
				values.put("relationship", "ManyToOne");
				break;
			case "updatedBy":
				val = new String[] { "updatedBy", "Updated By", "User" };
				values.put("relationship", "ManyToOne");
				break;
			}
			values.put("name", val[0]);
			values.put("label", val[1]);
			values.put("typeName", val[2]);
			values.put("metaModel", metaModel);
			MetaField field = metaFieldRepo.create(values);
			metaFields.add(field);
		}
	}
	
	@Override
	public void remove(MetaModel model) {
		
		if (model.getCustomised()) {
			recorderService.setUpdateServer();
		}
		
		super.remove(model);
	}

}
