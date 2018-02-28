/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.studio.web;

import java.util.List;

import com.axelor.meta.db.MetaField;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.Filter;
import com.axelor.studio.service.FilterService;
import com.google.inject.Inject;

public class FilterController {

	@Inject
	private FilterService filterService;

	public void updateTargetField(ActionRequest request, ActionResponse response) {

		Filter filter = request.getContext().asType(Filter.class);

		MetaField metaField = filter.getMetaField();

		if (metaField != null && metaField.getRelationship() != null) {
			String relationship = metaField.getRelationship();
			if (relationship != null) {
				response.setValue("targetType", relationship);
			} else {
				response.setValue("targetType", metaField.getTypeName());
			}
			response.setValue("targetField", metaField.getName());
		}

		else {
			response.setValue("targetField", null);
			response.setValue("targetType", null);

		}

		response.setValue("filterOperator", null);

	}

	public void updateTargetDetails(ActionRequest request,
			ActionResponse response) {

		Filter filter = request.getContext().asType(Filter.class);

		MetaField metaField = filter.getMetaField();
		String targetField = filter.getTargetField();

		if (targetField != null && metaField != null
				&& metaField.getRelationship() != null) {
			List<Object> target = filterService.getTargetField(metaField,
					targetField);
			metaField = (MetaField) target.get(1);
			String relationship = metaField.getRelationship();
			if (relationship != null) {
				response.setValue("targetType", relationship);
			} else {
				response.setValue("targetType", metaField.getTypeName());
			}
			response.setValue("targetField", target.get(0));
		} else {
			response.setValue("targetType", null);

		}

		response.setValue("filterOperator", null);

	}
}
