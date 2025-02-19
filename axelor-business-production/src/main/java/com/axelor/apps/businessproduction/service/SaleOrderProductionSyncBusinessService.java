package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.Project;

public interface SaleOrderProductionSyncBusinessService {
  void projectSoListOnChange(Project project) throws AxelorException;
}
