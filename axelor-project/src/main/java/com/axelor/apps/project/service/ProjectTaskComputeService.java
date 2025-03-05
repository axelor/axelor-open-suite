package com.axelor.apps.project.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.project.db.ProjectTask;

public interface ProjectTaskComputeService {
  void computeBudgetedTime(ProjectTask projectTask, Unit oldTimeUnit) throws AxelorException;
}
