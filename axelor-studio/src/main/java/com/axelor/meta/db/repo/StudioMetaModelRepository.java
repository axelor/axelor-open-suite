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

import javax.validation.ValidationException;

import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.studio.service.ModuleRecorderService;
import com.google.inject.Inject;

public class StudioMetaModelRepository extends MetaModelRepository {

	@Inject
	private ModuleRecorderService recorderService;

	@Override
	public MetaModel save(MetaModel metaModel) throws ValidationException {
		
		if (metaModel.getName().equals("Object")) {
			throw new ValidationException(
					I18n.get("Invalid model name 'Object'"));
		}
		
		
		if (!metaModel.getCustomised()) {
			boolean addStatus = true;
			for (MetaField field : metaModel.getMetaFields()) {
				if (field.getName().equals("wkfStatus")) {
					addStatus = false;
					break;
				}
			}
			if (addStatus) {
				MetaField field = new MetaField("wkfStatus", false);
				field.setTypeName("Integer");
				field.setLabel("Status");
				field.setFieldType("integer");
				metaModel.addMetaField(field);
			}
		}

		if (metaModel.getId() != null) {
			metaModel.setCustomised(true);
			metaModel.setEdited(true);
		}
		
		recorderService.setUpdateServer();

		return super.save(metaModel);

	}

	@Override
	public void remove(MetaModel model) {
		
		if (model.getCustomised()) {
			recorderService.setUpdateServer();
		}
		
		super.remove(model);
	}

}
