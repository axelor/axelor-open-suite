package com.axelor.apps.project.db.repo;

import com.axelor.apps.project.db.ProjectTask;

public class ProjectTaskManagementRepository extends ProjectTaskRepository{
	@Override
	public ProjectTask save(ProjectTask projectTask){
		if(projectTask.getCode() != null && !projectTask.getCode().isEmpty()) {
			projectTask.setFullName( projectTask.getCode() + " " + projectTask.getName());
		}
		else{
			projectTask.setFullName(projectTask.getName());
		}
		return super.save(projectTask);
	}
}
