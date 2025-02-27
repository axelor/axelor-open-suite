package com.axelor.apps.project.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.project.db.ProjectTask;
import com.google.inject.Inject;

public class ProjectTaskComputeServiceImpl implements ProjectTaskComputeService {

  protected ProjectTimeUnitService projectTimeUnitService;
  protected UnitConversionService unitConversionService;
  public static final int COMPUTE_SCALE = 5;

  @Inject
  public ProjectTaskComputeServiceImpl(
      ProjectTimeUnitService projectTimeUnitService, UnitConversionService unitConversionService) {
    this.projectTimeUnitService = projectTimeUnitService;
    this.unitConversionService = unitConversionService;
  }

  @Override
  public void computeBudgetedTime(ProjectTask projectTask, Unit oldTimeUnit)
      throws AxelorException {
    Unit unit = projectTimeUnitService.getTaskDefaultHoursTimeUnit(projectTask);
    if (projectTask == null
        || oldTimeUnit == null
        || unit == null
        || projectTask.getProject() == null) {
      return;
    }
    projectTask.setBudgetedTime(
        unitConversionService.convert(
            oldTimeUnit, unit, projectTask.getBudgetedTime(), COMPUTE_SCALE, null));
  }
}
