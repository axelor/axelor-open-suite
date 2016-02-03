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
