/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.project.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.TypedQuery;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanning;
import com.axelor.apps.project.db.repo.ProjectPlanningRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.exception.IExceptionMessage;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.team.db.TeamTask;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProjectService {

	public static int MAX_LEVEL_OF_PROJECT = 10;
	
	@Inject
	private ProjectRepository projectRepo;
	
	@Inject
	private ProjectPlanningRepository projectPlanningRepo;

	public Project generateProject(Project parentProject, String fullName, User assignedTo, Company company, Partner clientPartner){
		Project project = new Project();
		project.setStatusSelect(ProjectRepository.STATE_PLANNED);
		project.setProject(parentProject);
		project.setName(fullName);
		if(Strings.isNullOrEmpty(fullName)){
			project.setName("project");
		}
		project.setFullName(project.getName());
		project.setCompany(company);
		project.setClientPartner(clientPartner);
		project.setAssignedTo(assignedTo);
		project.setProgress(BigDecimal.ZERO);
		return project;
	}


	public Partner getClientPartnerFromProject(Project project) throws AxelorException{
		return this.getClientPartnerFromProject(project, 0);
	}

	private Partner getClientPartnerFromProject(Project project, int counter) throws AxelorException{
		if (project.getProject() == null){
			//it is a root project, can get the client partner
			if(project.getClientPartner() == null){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.PROJECT_CUSTOMER_PARTNER)), IException.CONFIGURATION_ERROR);
			}else{
				return project.getClientPartner();
			}
		}else{
			if (counter > MAX_LEVEL_OF_PROJECT){
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.PROJECT_DEEP_LIMIT_REACH)), IException.CONFIGURATION_ERROR);
			}else{
				return this.getClientPartnerFromProject(project.getProject(), counter++);
			}
		}
	}

	public BigDecimal computeDurationFromChildren(Long projectId) {
		String query = "SELECT SUM(pt.duration)"
				+ " FROM Project as pt"
				+ " WHERE pt.project.id = :projectId";

		TypedQuery<BigDecimal> q = JPA.em().createQuery(query, BigDecimal.class);
		q.setParameter("projectId", projectId);
		return q.getSingleResult();	
	}
	
	@Transactional
	public List<ProjectPlanning> createPlanning(Project project) {
		
		project = projectRepo.find(project.getId());
		
		List<ProjectPlanning> plannings = new ArrayList<ProjectPlanning>();
		
		if (project.getExcludePlanning()) {
			return plannings;
		}
		
		if (project.getAssignedTo() != null) {
			ProjectPlanning projectPlanning = projectPlanningRepo.all().filter("self.project = ?1 and self.task is null", project).fetchOne();
			if (projectPlanning == null) {
				projectPlanning = new ProjectPlanning();
				projectPlanning.setProject(project);
				projectPlanning.setUser(project.getAssignedTo());
				projectPlanning.setFromDate(project.getFromDate());
				projectPlanning.setToDate(project.getToDate());
			}
			plannings.add(projectPlanningRepo.save(projectPlanning));
		}
		
		for (TeamTask task : project.getTeamTaskList()) {
			ProjectPlanning taskPlanning = createPlanning(project, task);
			if (taskPlanning != null) {
				plannings.add(taskPlanning);
			}
		}
		
		for (Project child : project.getChildProjectList()) {
			plannings.addAll(createPlanning(child));
		}
		
		return plannings;
		
	}
	
	@Transactional
	public ProjectPlanning createPlanning(Project project, TeamTask task) {
		
		if (task.getAssignedTo() != null) {
			ProjectPlanning projectPlanning = projectPlanningRepo.all().filter("self.project = ?1 and self.task = ?2", project, task).fetchOne();
			if (projectPlanning == null) {
				projectPlanning = new ProjectPlanning();
				projectPlanning.setProject(project);
				projectPlanning.setUser(task.getAssignedTo());
				projectPlanning.setTask(task);
				projectPlanning.setFromDate(task.getTaskDate().atStartOfDay());
				projectPlanning = projectPlanningRepo.save(projectPlanning);;
			}
			return projectPlanning;
		}
		
		return null;
		
	}
}
