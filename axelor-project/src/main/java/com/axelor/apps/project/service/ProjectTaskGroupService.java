package com.axelor.apps.project.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.project.db.ProjectTask;
import java.util.Map;

public interface ProjectTaskGroupService {
  Map<String, Object> updateBudgetedTime(ProjectTask projectTask, Unit oldTimeUnit)
      throws AxelorException;
}
