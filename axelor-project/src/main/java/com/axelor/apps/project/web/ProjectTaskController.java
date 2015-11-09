package com.axelor.apps.project.web;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.service.ProjectTaskService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ProjectTaskController {

	@Inject
	protected ProjectTaskService projectTaskService;

	public void generateProjectFromPartner(ActionRequest request, ActionResponse response){
		Partner partner = Beans.get(PartnerRepository.class).find(new Long(request.getContext().get("_idPartner").toString()));
		User user = AuthUtils.getUser();
		ProjectTask project = projectTaskService.generateProject(null, partner.getName()+" project", user, user.getActiveCompany(), partner);
		response.setValues(project);
	}
}
