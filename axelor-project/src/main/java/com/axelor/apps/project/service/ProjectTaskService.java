package com.axelor.apps.project.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.exception.IExceptionMessage;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;

public class ProjectTaskService extends ProjectTaskRepository{

	public ProjectTask generateProject(ProjectTask parentProject,String fullName, User assignedTo, Company company, Partner clientPartner){
		ProjectTask project = new ProjectTask();
		project.setTypeSelect(ProjectTaskRepository.TYPE_PROJECT);
		project.setStatusSelect(ProjectTaskRepository.STATE_PLANNED);
		project.setProject(parentProject);
		project.setName(fullName);
		if(Strings.isNullOrEmpty(fullName)){
			project.setName("project");
		}
		project.setFullName(project.getName());
		project.setCompany(company);
		project.setClientPartner(clientPartner);
		project.setAssignedTo(assignedTo);
		project.setProgress(0);
		project.addMembersUserSetItem(assignedTo);
		return project;
	}


	public ProjectTask generateTask(ProjectTask project,String fullName, User assignedTo){
		ProjectTask task = new ProjectTask();
		task.setTypeSelect(ProjectTaskRepository.TYPE_TASK);
		task.setStatusSelect(ProjectTaskRepository.STATE_PLANNED);
		task.setProject(project);
		task.setName(fullName);
		if(Strings.isNullOrEmpty(fullName)){
			task.setName(project.getFullName()+"_task");
		}
		task.setFullName(task.getName());
		task.setAssignedTo(assignedTo);
		task.setProgress(0);

		return task;
	}

	public Partner getClientPartnerFromProjectTask(ProjectTask projectTask) throws AxelorException{
		if (projectTask.getProject() == null){
			//it is a root project, can get the client partner
			if(projectTask.getClientPartner() == null){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.PROJECT_CUSTOMER_PARTNER)), IException.CONFIGURATION_ERROR);
			}else{
				return projectTask.getClientPartner();
			}
		}else{
			return this.getClientPartnerFromProjectTask(projectTask.getProject());
		}
	}
}
