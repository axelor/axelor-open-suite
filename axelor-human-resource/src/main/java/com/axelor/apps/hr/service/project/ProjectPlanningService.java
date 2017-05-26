package com.axelor.apps.hr.service.project;

import java.math.BigDecimal;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanning;
import com.axelor.exception.AxelorException;
import com.axelor.team.db.TeamTask;

public interface ProjectPlanningService {
	
	public ProjectPlanning updatePlanningTime(ProjectPlanning planning) throws AxelorException;
	
	public void updateTaskPlannedHrs(TeamTask teamTask);
	
	public void updateProjectPlannedHrs(Project project);
	
	public BigDecimal getTotalPlannedHrs(ProjectPlanning planning);
}
