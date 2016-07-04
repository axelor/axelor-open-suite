package com.axelor.studio.web;

import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.StudioTemplate;
import com.axelor.studio.db.repo.StudioTemplateRepository;
import com.axelor.studio.service.template.StudioTemplateService;
import com.google.inject.Inject;

public class StudioTemplateController {

	@Inject
	private StudioTemplateService studioTemplateService;

	@Inject
	private StudioTemplateRepository templateRepo;

	public void importTemplate(ActionRequest request, ActionResponse response) {

		StudioTemplate template = request.getContext().asType(
				StudioTemplate.class);
		template = templateRepo.find(template.getId());

		String msg = studioTemplateService.importTemplate(template);
		response.setFlash(msg);
		response.setReload(true);
	}

	public void exportTemplate(ActionRequest request, ActionResponse response) {

		StudioTemplate template = request.getContext().asType(
				StudioTemplate.class);

		MetaFile metaFile = studioTemplateService.export(template.getName());
		if (metaFile == null) {
			response.setFlash(I18n.get("Error in export"));
		}

		response.setValue("metaFile", metaFile);
	}

}
