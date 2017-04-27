package com.axelor.apps.hr.db.repo;

import com.axelor.apps.hr.service.project.ProjectPlanningService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanning;
import com.axelor.apps.project.db.repo.ProjectPlanningRepository;
import com.axelor.exception.AxelorException;
import com.axelor.team.db.TeamTask;
import com.google.inject.Inject;

public class ProjectPlanningHRRepository extends ProjectPlanningRepository {
	
	@Inject
	private ProjectPlanningService planningService;
	
	@Override
	public ProjectPlanning save(ProjectPlanning projectPlanning) {
		
		try {
			projectPlanning = planningService.updatePlanningTime(projectPlanning);
			planningService.updateTaskPlannedHrs(projectPlanning.getTask());
			planningService.updateProjectPlannedHrs(projectPlanning.getProject());
		} catch (AxelorException e) {
			e.printStackTrace();
		}
		
		return super.save(projectPlanning);
	}
	
	@Override
	public void remove(ProjectPlanning projectPlanning) {
		
		TeamTask task = projectPlanning.getTask();
		Project project = projectPlanning.getProject();
		
		super.remove(projectPlanning);
		
		planningService.updateTaskPlannedHrs(task);
		planningService.updateProjectPlannedHrs(project);
	}
}
