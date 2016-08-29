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
package com.axelor.meta.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axelor.app.AppSettings;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.service.ViewLoaderService;
import com.google.inject.Inject;

public class MetaModelController {

	@Inject
	ViewLoaderService viewLoaderService;

	@Inject
	MetaModelRepository metaModelRepo;

	public void openForm(ActionRequest request, ActionResponse response) throws AxelorException {

		MetaModel model = request.getContext().asType(MetaModel.class);

		ViewBuilder viewBuilder = viewLoaderService
				.getDefaultForm(model.getMetaModule().getName(), metaModelRepo.find(model.getId()), null, true);

		response.setView(ActionView.define(model.getName())
				.model("com.axelor.studio.db.ViewBuilder")
				.add("form", "view-builder-form")
				.add("grid", "view-builder-grid")
				.domain("self.viewType = 'form'")
				.context("_showRecord", viewBuilder.getId().toString())
				.context("_viewType", "form")
				.context("__check_version", "silent").map());

	}

	public void openGrid(ActionRequest request, ActionResponse response) throws AxelorException {

		MetaModel model = request.getContext().asType(MetaModel.class);

		ViewBuilder viewBuilder = viewLoaderService.getDefaultGrid(model.getMetaModule().getName(),
				metaModelRepo.find(model.getId()), false);

		response.setView(ActionView.define(model.getName())
				.model("com.axelor.studio.db.ViewBuilder")
				.add("form", "view-builder-form")
				.add("grid", "view-builder-grid")
				.domain("self.viewType = 'grid'")
				.context("_showRecord", viewBuilder.getId().toString())
				.context("_viewType", "grid").map());

	}

	public void openFieldEditor(ActionRequest request, ActionResponse response) {

		MetaModel model = request.getContext().asType(MetaModel.class);
		
		String baseUrl = AppSettings.get().getBaseURL();
		String url = baseUrl + "/studio/#/Model/" + model.getId();

		Map<String, Object> mapView = new HashMap<String, Object>();
		mapView.put("title", I18n.get("Model editor"));
		mapView.put("resource", url);
		mapView.put("viewType", "html");
		
		response.setView(mapView);
	}
	
	public void setDefault(ActionRequest request, ActionResponse response) {
		
		List<Map<String, Object>>  fields = new ArrayList<Map<String,Object>>();
		
		List<String[]> defaultFields = new ArrayList<String[]>();
		defaultFields.add( new String[] { "id", "Id", "Long" , null});
		defaultFields.add( new String[] { "createdOn", "Created on", "LocalDateTime" , null});
		defaultFields.add( new String[] { "updatedOn", "Updated on", "LocalDateTime" , null});
		defaultFields.add( new String[] { "createdBy", "Created By", "User" , "ManyToOne"});
		defaultFields.add( new String[] { "updatedBy", "Updated By", "User" , "ManyToOne"});
		defaultFields.add( new String[] { "wkfStatus", "Status", "String" , null});
		
		for (String[] val : defaultFields) {
			
			Map<String, Object> values = new HashMap<String, Object>();
			values.put("name", val[0]);
			values.put("label", val[1]);
			values.put("typeName", val[2]);
			values.put("relationship", val[3]);
			
			if (val[0].equals("wkfStatus")) {
				values.put("readonly", true);
				values.put("customised", true);
				values.put("sequence", 1);
			}
			
			fields.add(values);
		}
		
		response.setValue("packageName", "com.axelor.apps.custom.db");
		response.setValue("metaFields", fields);
		response.setValue("customised", true);
		response.setValue("edited", true);
		
	}

}
