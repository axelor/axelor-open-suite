package com.axelor.apps.hr.web;

import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class UserHrController {

	@Inject
	UserRepository userRepo;

	@Inject
	UserHrService userHrService;

	@Transactional
	public void createEmployee(ActionRequest request, ActionResponse response) {
		User user = userRepo.find(request.getContext().asType(User.class).getId());

		userHrService.createEmployee(user);

		response.setReload(true);
	}
}
