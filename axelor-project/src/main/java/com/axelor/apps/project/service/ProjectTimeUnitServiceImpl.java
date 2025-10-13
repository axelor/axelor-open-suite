/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
  protected UnitConversionForProjectService unitConversionForProjectService;

  @Inject
  public ProjectTimeUnitServiceImpl(
      AppBaseService appBaseService,
      UnitConversionForProjectService unitConversionForProjectService) {
    this.appBaseService = appBaseService;
    this.unitConversionForProjectService = unitConversionForProjectService;
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

  @Override
  public BigDecimal convertInProjectTaskUnit(
      ProjectTask projectTask, Unit startUnit, BigDecimal duration) throws AxelorException {
    if (projectTask == null || startUnit == null || duration.signum() == 0) {
      return BigDecimal.ZERO;
    }

    Unit projectTaskUnit = getTaskDefaultTimeUnit(projectTask, startUnit);

    return unitConversionForProjectService.convert(
        startUnit,
        projectTaskUnit,
        duration,
        AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
        projectTask.getProject());
  }
}
