package com.axelor.studio.web;

import java.io.IOException;

import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.MetaModel;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.ModuleRecorder;
import com.axelor.studio.service.ModuleRecorderService;
import com.google.inject.Inject;

public class ModuleRecorderController {

	@Inject
	private ModuleRecorderService moduleRecorderService;

	@Inject
	private MetaModelRepository metaModelRepo;

	public void update(ActionRequest request, ActionResponse response) {
		
		ModuleRecorder moduleRecorder = request.getContext().asType(
				ModuleRecorder.class);
		
		response.setSignal("refresh-app", true);
		response.setFlash(moduleRecorderService.update(moduleRecorder));

	}

	public void checkEdited(ActionRequest request, ActionResponse response) {

		MetaModel metaModel = metaModelRepo.all()
				.filter("self.edited = true and self.customised = true")
				.fetchOne();

		if (metaModel != null) {
			response.setAlert("Server restart required due to updated models."
					+ " Are you sure to continue ?");
		}

	}
	
	public void reset(ActionRequest request, ActionResponse response) throws IOException {
		
		ModuleRecorder moduleRecorder = request.getContext().asType(
				ModuleRecorder.class);
		
		response.setFlash(moduleRecorderService.reset(moduleRecorder));
		response.setSignal("refresh-app", true);
		
	}
	
}
