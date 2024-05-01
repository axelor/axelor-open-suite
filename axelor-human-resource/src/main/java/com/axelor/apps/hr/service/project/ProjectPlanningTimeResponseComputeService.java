package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.rest.dto.ProjectPlanningTImeRestrictedValueResponse;

public interface ProjectPlanningTimeResponseComputeService {

  ProjectPlanningTImeRestrictedValueResponse computeProjectPlanningTimeResponse(Company company)
      throws AxelorException;
}
