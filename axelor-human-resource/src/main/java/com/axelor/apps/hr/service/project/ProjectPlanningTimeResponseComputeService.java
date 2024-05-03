package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.rest.dto.ProjectPlanningTimeRestrictedValueResponse;

public interface ProjectPlanningTimeResponseComputeService {

  ProjectPlanningTimeRestrictedValueResponse computeProjectPlanningTimeResponse(Company company)
      throws AxelorException;
}
