package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.project.db.Project;

public interface TimesheetLineCheckService {

  void checkActivity(Project project, Product product) throws AxelorException;
}
