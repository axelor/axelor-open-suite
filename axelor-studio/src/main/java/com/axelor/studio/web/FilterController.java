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
