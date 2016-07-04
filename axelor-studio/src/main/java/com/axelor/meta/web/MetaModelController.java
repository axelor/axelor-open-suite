package com.axelor.meta.web;

import java.util.HashMap;
import java.util.Map;

import com.axelor.app.AppSettings;
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

	public void openForm(ActionRequest request, ActionResponse response) {

		MetaModel model = request.getContext().asType(MetaModel.class);

		ViewBuilder viewBuilder = viewLoaderService
				.getDefaultForm(metaModelRepo.find(model.getId()));

		response.setView(ActionView.define(model.getName())
				.model("com.axelor.studio.db.ViewBuilder")
				.add("form", "view-builder-form")
				.add("grid", "view-builder-grid")
				.domain("self.viewType = 'form'")
				.context("_showRecord", viewBuilder.getId().toString())
				.context("_viewType", "form")
				.context("__check_version", "silent").map());

	}

	public void openGrid(ActionRequest request, ActionResponse response) {

		MetaModel model = request.getContext().asType(MetaModel.class);

		ViewBuilder viewBuilder = viewLoaderService.getDefaultGrid(
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

}
