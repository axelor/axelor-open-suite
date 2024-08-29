package com.axelor.apps.businessproject.service.projecttask;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.project.db.ProjectTask;
import java.util.Map;

public interface ProjectTaskGroupService {
  Map<String, Object> updateBudgetedTime(ProjectTask projectTask, Unit oldTimeUnit)
      throws AxelorException;

  Map<String, Object> updateSoldTime(ProjectTask projectTask) throws AxelorException;

  Map<String, Object> updateUpdatedTime(ProjectTask projectTask) throws AxelorException;

  Map<String, Object> updateQuantity(ProjectTask projectTask) throws AxelorException;

  Map<String, Object> updateFinancialDatas(ProjectTask projectTask) throws AxelorException;
}
