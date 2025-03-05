package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.common.StringUtils;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import java.util.Optional;

public class ProjectPlanningTimeToolServiceImpl implements ProjectPlanningTimeToolService {

  protected AppBaseService appBaseService;

  @Inject
  public ProjectPlanningTimeToolServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public Unit getDefaultTimeUnit(ProjectPlanningTime projectPlanningTime) throws AxelorException {
    ProjectTask projectTask = projectPlanningTime.getProjectTask();
    if (projectTask != null && projectTask.getTimeUnit() != null) {
      return projectTask.getTimeUnit();
    }

    Project project = projectPlanningTime.getProject();
    if (project != null && project.getProjectTimeUnit() != null) {
      return project.getProjectTimeUnit();
    }

    Employee employee = projectPlanningTime.getEmployee();
    if (employee != null && StringUtils.notEmpty(employee.getTimeLoggingPreferenceSelect())) {
      return getTimeUnitUsingTimeLoggingPreference(employee.getTimeLoggingPreferenceSelect());
    }

    String timeLoggingSelect =
        Optional.ofNullable(appBaseService.getAppBase())
            .map(AppBase::getTimeLoggingPreferenceSelect)
            .orElse("");
    if (StringUtils.notEmpty(timeLoggingSelect)) {
      return getTimeUnitUsingTimeLoggingPreference(timeLoggingSelect);
    }

    return null;
  }

  protected Unit getTimeUnitUsingTimeLoggingPreference(String timeLoggingPreferenceSelect)
      throws AxelorException {
    switch (timeLoggingPreferenceSelect) {
      case EmployeeRepository.TIME_PREFERENCE_HOURS:
        return appBaseService.getUnitHours();
      case EmployeeRepository.TIME_PREFERENCE_DAYS:
        return appBaseService.getUnitDays();
      case EmployeeRepository.TIME_PREFERENCE_MINUTES:
        return appBaseService.getUnitMinutes();
    }

    return null;
  }
}
