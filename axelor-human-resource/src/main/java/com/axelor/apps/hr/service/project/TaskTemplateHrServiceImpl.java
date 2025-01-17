package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.service.TaskTemplateServiceImpl;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TaskTemplateHrServiceImpl extends TaskTemplateServiceImpl {

  protected ProjectPlanningTimeCreateService projectPlanningTimeCreateService;
  protected AppBaseService appBaseService;
  protected ProjectPlanningTimeComputeService projectPlanningTimeComputeService;
  protected ProjectPlanningTimeRepository projectPlanningTimeRepository;

  @Inject
  public TaskTemplateHrServiceImpl(
      ProjectPlanningTimeCreateService projectPlanningTimeCreateService,
      AppBaseService appBaseService,
      ProjectPlanningTimeComputeService projectPlanningTimeComputeService,
      ProjectPlanningTimeRepository projectPlanningTimeRepository) {
    this.projectPlanningTimeCreateService = projectPlanningTimeCreateService;
    this.appBaseService = appBaseService;
    this.projectPlanningTimeComputeService = projectPlanningTimeComputeService;
    this.projectPlanningTimeRepository = projectPlanningTimeRepository;
  }

  @Override
  public void manageTemplateFields(ProjectTask task, TaskTemplate taskTemplate, Project project)
      throws AxelorException {
    super.manageTemplateFields(task, taskTemplate, project);

    manageProjectPlanningTime(taskTemplate, task, project);
  }

  @Transactional(rollbackOn = Exception.class)
  protected void manageProjectPlanningTime(
      TaskTemplate taskTemplate, ProjectTask task, Project project) throws AxelorException {
    task.setTotalPlannedHrs(taskTemplate.getTotalPlannedHrs());

    if (task.getTotalPlannedHrs().signum() > 0
        && task.getAssignedTo() != null
        && task.getProduct() != null) {
      LocalDateTime startDateTime = task.getTaskDate().atStartOfDay();
      BigDecimal dailyWorkHours = appBaseService.getDailyWorkHours();
      Unit unitHours = appBaseService.getUnitHours();
      ProjectPlanningTime projectPlanningTime =
          projectPlanningTimeCreateService.createProjectPlanningTime(
              startDateTime,
              task,
              project,
              100,
              task.getAssignedTo().getEmployee(),
              task.getProduct(),
              dailyWorkHours,
              startDateTime,
              task.getSite(),
              unitHours);

      projectPlanningTime.setDisplayTimeUnit(unitHours);
      projectPlanningTime.setDisplayPlannedTime(task.getTotalPlannedHrs());
      projectPlanningTimeComputeService.computePlannedTimeValues(projectPlanningTime);
      projectPlanningTimeRepository.save(projectPlanningTime);
    }
  }
}
