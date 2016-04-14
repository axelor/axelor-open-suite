package com.axelor.apps.project.db.repo;

import com.axelor.apps.project.db.ProjectTask;
import com.google.common.base.Strings;

public class ProjectTaskManagementRepository extends ProjectTaskRepository {
	
	
	@Override
	public ProjectTask save(ProjectTask projectTask){
		
		projectTask.setFullName(projectTask.getCode() + " - " + projectTask.getName());
		
		if (projectTask.getChildProjectTaskList() != null && !projectTask.getChildProjectTaskList().isEmpty()){
			for (ProjectTask child : projectTask.getChildProjectTaskList()) {
				String code = ( Strings.isNullOrEmpty(child.getCode()) ) ? "" : child.getCode();
				String name = ( Strings.isNullOrEmpty(child.getName()) ) ? "" : child.getName();
				child.setFullName(code + " - " + name);
			}
		}
		
		return super.save(projectTask);
	}

}
