package com.axelor.studio.web;

import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.Wkf;
import com.axelor.studio.db.repo.WkfRepository;
import com.google.inject.Inject;

public class MetaJsonModelController {
	
	
	@Inject
	private WkfRepository wkfRepo;
	
	public void openWorkflow(ActionRequest request, ActionResponse response) {
		
		MetaJsonModel jsonModel = request.getContext().asType(MetaJsonModel.class);
		
		Wkf wkf = wkfRepo.all().filter("self.jsonModel = ?1", jsonModel.getName()).fetchOne();
		
		ActionViewBuilder builder = ActionView.define("Workflow")
			.add("form","wkf-form")
			.model("com.axelor.studio.db.Wkf");
			
		if (wkf == null) {
			builder.context("_jsonModel", jsonModel.getName()) ;
		}
		else {
			builder.context("_showRecord", wkf.getId());
		}
		
		response.setView(builder.map());
		
	}
	

}
