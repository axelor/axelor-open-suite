package com.axelor.apps.businessproject.service.projecttask;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.project.db.ProjectTask;
import java.math.BigDecimal;

public interface ProjectTaskComputeService {
  void computeBudgetedTime(ProjectTask projectTask, Unit oldTimeUnit) throws AxelorException;

  BigDecimal computeSoldTime(ProjectTask projectTask);

  void computeQuantity(ProjectTask projectTask) throws AxelorException;

  void computeFinancialDatas(ProjectTask projectTask) throws AxelorException;
}
