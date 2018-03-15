/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.mobile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class BusinessProjectMobileController {

	/*
	 * This method is used in mobile application.
	 * It was in ProjectTaskBusinessServiceImpl
	 * @param request
	 * @param response
	 *
	 * POST /abs-webapp/ws/action/com.axelor.apps.businessproject.mobile.BusinessProjectMobileController:getProjects
	 * Content-Type: application/json
	 *
	 * URL: com.axelor.apps.businessproject.mobile.BusinessProjectMobileController:getProjects
	 * fields: no field
	 *
	 * payload:
	 * { "data": {
	 * 		"action": "com.axelor.apps.businessproject.mobile.BusinessProjectMobileController:getProjects"
	 * } }
	 */
	public void getProjects(ActionRequest request, ActionResponse response){
		List<Map<String,String>> dataList = new ArrayList<Map<String,String>>();
		try{
			User user = AuthUtils.getUser();
			if(user != null){
				List<ProjectTask> projectTaskList = Beans.get(ProjectTaskRepository.class).all().filter("self.imputable = true").fetch();
				for (ProjectTask projectTask : projectTaskList) {
					if((projectTask.getMembersUserSet() != null && projectTask.getMembersUserSet().contains(user))
							|| user.equals(projectTask.getAssignedTo())){
						Map<String, String> map = new HashMap<String,String>();
						map.put("name", projectTask.getName());
						map.put("id", projectTask.getId().toString());
						dataList.add(map);
					}
				}
			}
			response.setData(dataList);
		}
		catch(Exception e){
			response.setStatus(-1);
			response.setError(e.getMessage());
		}
	}
	
}
