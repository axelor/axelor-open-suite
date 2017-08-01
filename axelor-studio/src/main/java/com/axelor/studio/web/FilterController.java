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
package com.axelor.studio.web;

import java.util.List;

import org.apache.commons.lang.mutable.MutableInt;

import com.axelor.common.Inflector;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.Filter;
import com.axelor.studio.service.filter.FilterSqlService;
import com.google.inject.Inject;

public class FilterController {

	@Inject
	private FilterSqlService filterSqlService;

	public void updateTargetField(ActionRequest request, ActionResponse response) {

		Filter filter = request.getContext().asType(Filter.class);

		MetaField metaField = filter.getMetaField();
		MetaJsonField metaJson = filter.getMetaJsonField();
		
		Boolean isJson = filter.getIsJson();

		if (!isJson && metaField != null && metaField.getRelationship() != null) {
			response.setValue("targetType", metaField.getRelationship());
			response.setValue("targetField", metaField.getName());
		}
		else if(isJson && metaJson != null && metaJson.getTargetJsonModel() != null) {
			response.setValue("targetType", Inflector.getInstance().camelize(metaJson.getType()));
			response.setValue("targetField", metaJson.getName());
		}	
		else {
			response.setValue("targetField", null);
			response.setValue("targetType", null);
		}

		response.setValue("filterOperator", null);

	}

	public void updateTargetDetails(ActionRequest request,
			ActionResponse response) throws AxelorException {

		Filter filter = request.getContext().asType(Filter.class);

		MetaField metaField = filter.getMetaField();
		MetaJsonField metaJson = filter.getMetaJsonField();
		String targetField = filter.getTargetField();
		
		if (targetField == null) return;
		
		Boolean isJson = filter.getIsJson();
		
		StringBuilder parent = new StringBuilder("self");
		Object target = null;
		if (!isJson && metaField != null
				&& metaField.getRelationship() != null) {
			target = filterSqlService.parseMetaField(metaField, targetField, null, parent);
		} else if(isJson && metaJson != null
				&& metaJson.getTargetJsonModel() != null) {
			target = filterSqlService.parseJsonField(metaJson, targetField, null, parent);
		} 
		
		if (target != null) {
			if (target instanceof MetaField) {
				updateTarget(response, (MetaField)target);
				response.setValue("targetField", target);
			}
			else if (target instanceof MetaJsonField) {
				updateTarget(response, (MetaJsonField)target);
				response.setValue("targetField", target);
			}
			response.setValue("targetField", target);
		}
		else {
			response.setValue("targetType", null);
		}
		
		response.setValue("filterOperator", null);

	} 

	private void updateTarget(ActionResponse response, MetaField metaField) {
		
		String relationship = metaField.getRelationship();
		if (relationship != null) {
			response.setValue("targetType", relationship);
		} else {
			response.setValue("targetType", metaField.getTypeName());
		}
	}
	
	private void updateTarget(ActionResponse response, MetaJsonField metaJson) {
		
		response.setValue("targetType", Inflector.getInstance().camelize(metaJson.getType()));
		response.setValue("isTargetJson", true);
	}
	
}
