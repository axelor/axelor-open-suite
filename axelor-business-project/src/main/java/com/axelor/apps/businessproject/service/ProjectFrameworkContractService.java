package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.project.db.ProjectTask;
import java.util.Map;

public interface ProjectFrameworkContractService {

  Map<String, Object> getProductDataFromContract(ProjectTask projectTask) throws AxelorException;
}
