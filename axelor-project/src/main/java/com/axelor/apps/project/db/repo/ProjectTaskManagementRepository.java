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
package com.axelor.apps.project.db.repo;

import com.axelor.apps.project.db.ProjectTask;
import com.google.common.base.Strings;

public class ProjectTaskManagementRepository extends ProjectTaskRepository {
	
	
	@Override
	public ProjectTask save(ProjectTask projectTask){
		
		String projectCode = ( Strings.isNullOrEmpty(projectTask.getCode()) ) ? "" : projectTask.getCode() + " - ";
		projectTask.setFullName(projectCode + projectTask.getName());
		if (projectTask.getChildProjectTaskList() != null && !projectTask.getChildProjectTaskList().isEmpty()){
			for (ProjectTask child : projectTask.getChildProjectTaskList()) {
				String code = ( Strings.isNullOrEmpty(child.getCode()) ) ? "" : child.getCode() + " - ";
				child.setFullName(code + child.getName());
			}
		}
		
		return super.save(projectTask);
	}

	@Override
	public ProjectTask copy(ProjectTask entity, boolean deep) {
		
		ProjectTask project = super.copy(entity, false);
		project.setStatusSelect(STATE_PLANNED);
		
		return project;
	
	}

}
