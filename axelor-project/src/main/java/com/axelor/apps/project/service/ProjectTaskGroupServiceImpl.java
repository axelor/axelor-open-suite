package com.axelor.apps.project.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.project.db.ProjectTask;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class ProjectTaskGroupServiceImpl implements ProjectTaskGroupService {

  protected ProjectTaskComputeService projectTaskComputeService;

  @Inject
  public ProjectTaskGroupServiceImpl(ProjectTaskComputeService projectTaskComputeService) {
    this.projectTaskComputeService = projectTaskComputeService;
  }

  @Override
  public Map<String, Object> updateBudgetedTime(ProjectTask projectTask, Unit oldTimeUnit)
      throws AxelorException {
    projectTaskComputeService.computeBudgetedTime(projectTask, oldTimeUnit);

    Map<String, Object> valuesMap = new HashMap<>();

    valuesMap.put("budgetedTime", projectTask.getBudgetedTime());

    return valuesMap;
  }
}
