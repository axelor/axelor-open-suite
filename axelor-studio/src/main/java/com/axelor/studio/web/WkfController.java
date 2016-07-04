package com.axelor.studio.web;

import java.util.Map;

import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.Wkf;
import com.axelor.studio.db.WkfNode;
import com.axelor.studio.db.WkfTransition;
import com.axelor.studio.db.repo.WkfNodeRepository;
import com.axelor.studio.db.repo.WkfRepository;
import com.axelor.studio.db.repo.WkfTransitionRepository;
import com.axelor.studio.service.ViewLoaderService;
import com.axelor.studio.service.wkf.WkfDesignerService;
import com.google.inject.Inject;

public class WkfController {

	@Inject
	WkfRepository wkfRepo;

	@Inject
	WkfDesignerService wkfDesignerService;

	@Inject
	private ViewLoaderService viewLoaderService;

	@Inject
	private MetaModelRepository metaModelRepo;

	public void processWorkFlow(ActionRequest request, ActionResponse response)
			throws Exception {

		Wkf workflow = request.getContext().asType(Wkf.class);
		workflow = wkfRepo.find(workflow.getId());
		workflow.setEdited(true);
		wkfDesignerService.processXml(workflow);
		response.setReload(true);

	}

	public void onNodeEdit(ActionRequest request, ActionResponse response)
			throws Exception {
		WkfNodeRepository repo = Beans.get(WkfNodeRepository.class);
		WkfNode node = request.getContext().asType(WkfNode.class);
		if (node.getWkf().getId() != null) {
			WkfNode found = repo
					.all()
					.filter("self.wkf.id = ? and self.xmlId = ?",
							node.getWkf().getId(), node.getXmlId()).fetchOne();
			if (found != null) {
				Map<String, Object> view = ActionView
						.define(I18n.get("Edit Node"))
						.add("form", "wkf-node-form")
						.model(WkfNode.class.getName())
						.context("_showRecord", found.getId())
						.param("popup", "true").param("show-toolbar", "false")
						.param("forceEdit", "true").map();
				response.setView(view);
			} else {
				response.setFlash(I18n.get("Workflow is not saved"));
			}
		} else {
			response.setFlash(I18n.get("Workflow is not saved"));
		}
	}

	public void onTransitionEdit(ActionRequest request, ActionResponse response)
			throws Exception {
		WkfTransitionRepository repo = Beans.get(WkfTransitionRepository.class);
		WkfTransition transition = request.getContext().asType(
				WkfTransition.class);
		WkfTransition found = repo.all()
				.filter("self.xmlId = ?", transition.getXmlId()).fetchOne();
		if (found != null) {

			Map<String, Object> view = ActionView
					.define(I18n.get("Edit Transition"))
					.add("form", "wkf-transition-form")
					.model(WkfTransition.class.getName())
					.context("_showRecord", found.getId())
					.param("popup", "true").param("show-toolbar", "false")
					.param("forceEdit", "true").map();

			response.setView(view);
		} else {
			response.setFlash(I18n.get("Workflow is not saved"));
		}
	}

	public void setViewBuilder(ActionRequest request, ActionResponse response) {

		Wkf wkf = request.getContext().asType(Wkf.class);
		ViewBuilder viewBuilder = wkf.getViewBuilder();

		if (viewBuilder == null) {
			MetaModel metaModel = wkf.getMetaModel();
			metaModel = metaModelRepo.find(metaModel.getId());
			viewBuilder = new ViewBuilder();
			viewBuilder.setMetaModel(metaModel);
			viewBuilder.setViewType("form");
			viewBuilder.setModel(metaModel.getFullName());
			viewBuilder = viewLoaderService.getDefaultForm(metaModel);
		}

		response.setValue("viewBuilder", viewBuilder);
	}

}
