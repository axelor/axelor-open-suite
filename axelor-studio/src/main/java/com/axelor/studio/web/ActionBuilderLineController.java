package com.axelor.studio.web;

import java.util.Arrays;

import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.ActionBuilderLine;
import com.google.inject.Inject;

public class ActionBuilderLineController {

	@Inject
	private MetaFieldRepository metaFieldRepo;

	public void validateTarget(ActionRequest request, ActionResponse response) {

		ActionBuilderLine line = request.getContext().asType(
				ActionBuilderLine.class);

		String targetField = line.getTargetField();
		MetaField metaField = line.getMetaField();

		if (targetField != null && metaField != null) {

			String[] target = targetField.split("\\.");
			if (!target[0].equals(metaField.getName())) {
				response.setError("Target initials not match with selected field.");
			} else {
				String invalidField = validate(target, metaField);
				if (invalidField != null) {
					response.setError("Invalide target field '" + invalidField
							+ "'");
					response.setValue("targetField", null);
				}
			}

		}

	}

	private String validate(String[] target, MetaField metaField) {

		if (metaField.getRelationship() != null && target.length > 1) {
			target = Arrays.copyOfRange(target, 1, target.length);
			metaField = metaFieldRepo
					.all()
					.filter("self.name = ?1 and self.metaModel.name = ?2",
							target[0], metaField.getTypeName()).fetchOne();
			if (metaField == null) {
				return target[0];
			}
			return validate(target, metaField);

		}

		return null;
	}
}
