package com.axelor.apps.project.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;

public class ProjectTimeUnitServiceImpl implements ProjectTimeUnitService {

  protected AppBaseService appBaseService;

  @Inject
  public ProjectTimeUnitServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public Unit getTaskDefaultHoursTimeUnit(ProjectTask projectTask) throws AxelorException {
    AppBase appBase = appBaseService.getAppBase();
    Unit timeUnit =
        getTaskDefaultTimeUnit(
            projectTask, Optional.ofNullable(appBase).map(AppBase::getUnitHours).orElse(null));

    if (timeUnit == null && projectTask != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProjectExceptionMessage.PROJECT_TASK_NO_UNIT_FOUND),
          projectTask.getName());
    }

    return timeUnit;
  }

  @Override
  public Unit getProjectDefaultHoursTimeUnit(Project project) throws AxelorException {
    AppBase appBase = appBaseService.getAppBase();
    Unit timeUnit =
        getProjectDefaultTimeUnit(
            project, Optional.ofNullable(appBase).map(AppBase::getUnitHours).orElse(null));

    if (timeUnit == null && project != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProjectExceptionMessage.PROJECT_NO_UNIT_FOUND),
          project.getName());
    }

    return timeUnit;
  }

  @Override
  public BigDecimal getDefaultNumberHoursADay(Project project) throws AxelorException {
    AppBase appBase = appBaseService.getAppBase();
    BigDecimal numberHoursADay =
        Optional.ofNullable(project)
            .filter(p -> p.getNumberHoursADay() != null && p.getNumberHoursADay().signum() > 0)
            .map(Project::getNumberHoursADay)
            .orElse(
                Optional.ofNullable(appBase)
                    .map(AppBase::getDailyWorkHours)
                    .orElse(BigDecimal.ZERO));

    if (numberHoursADay.signum() <= 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProjectExceptionMessage.PROJECT_CONFIG_DEFAULT_HOURS_PER_DAY_MISSING));
    }

    return numberHoursADay;
  }

  protected Unit getTaskDefaultTimeUnit(ProjectTask projectTask, Unit defaultUnit) {
    if (projectTask == null) {
      return null;
    }

    if (projectTask.getTimeUnit() != null) {
      return projectTask.getTimeUnit();
    }

    return getProjectDefaultTimeUnit(projectTask.getProject(), defaultUnit);
  }

  protected Unit getProjectDefaultTimeUnit(Project project, Unit defaultUnit) {
    return Optional.ofNullable(project).map(Project::getProjectTimeUnit).orElse(defaultUnit);
  }
}
