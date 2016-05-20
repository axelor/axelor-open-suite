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

}
