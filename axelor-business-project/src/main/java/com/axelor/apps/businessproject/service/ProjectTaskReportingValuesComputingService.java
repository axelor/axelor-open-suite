package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.ProjectTask;

public interface ProjectTaskReportingValuesComputingService {

  void computeProjectTaskTotals(ProjectTask projectTask) throws AxelorException;
}
