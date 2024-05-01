package com.axelor.apps.hr.service.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.rest.dto.ProjectPlanningTImeRestrictedValueResponse;
import com.axelor.apps.project.db.PlannedTimeValue;
import com.axelor.apps.project.db.ProjectConfig;
import com.axelor.apps.project.service.config.ProjectConfigService;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectPlanningTimeResponseComputeServiceImpl
    implements ProjectPlanningTimeResponseComputeService {

  protected ProjectConfigService projectConfigService;

  @Inject
  public ProjectPlanningTimeResponseComputeServiceImpl(ProjectConfigService projectConfigService) {
    this.projectConfigService = projectConfigService;
  }

  @Override
  public ProjectPlanningTImeRestrictedValueResponse computeProjectPlanningTimeResponse(
      Company company) throws AxelorException {
    ProjectConfig projectConfig = projectConfigService.getProjectConfig(company);
    boolean isSelectionOnDisplayPlannedTime = projectConfig.getIsSelectionOnDisplayPlannedTime();

    if (isSelectionOnDisplayPlannedTime) {
      List<Long> plannedTimeValueIdList =
          projectConfig.getPlannedTimeValueList().stream()
              .map(PlannedTimeValue::getId)
              .collect(Collectors.toList());
      return new ProjectPlanningTImeRestrictedValueResponse(
          projectConfig.getVersion(), true, plannedTimeValueIdList);
    }
    return new ProjectPlanningTImeRestrictedValueResponse(projectConfig.getVersion(), false, null);
  }
}
