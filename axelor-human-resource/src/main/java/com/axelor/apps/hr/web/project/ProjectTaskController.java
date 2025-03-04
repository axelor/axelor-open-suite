package com.axelor.apps.hr.web.project;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.service.project.ProjectTaskSprintService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.common.StringUtils;
import com.axelor.db.EntityHelper;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProjectTaskController {

  public void validateSprintPlanification(ActionRequest request, ActionResponse response) {
    ProjectTask projectTask = request.getContext().asType(ProjectTask.class);

    String warning =
        Beans.get(ProjectTaskSprintService.class).getSprintOnChangeWarning(projectTask);
    if (StringUtils.notEmpty(warning)) {
      response.setAlert(warning);
    }
  }

  public void createSprintPlanification(ActionRequest request, ActionResponse response)
      throws AxelorException {
    ProjectTask projectTask =
        EntityHelper.getEntity(request.getContext().asType(ProjectTask.class));
    Beans.get(ProjectTaskSprintService.class).createOrMovePlanification(projectTask);
    response.setValue("oldActiveSprint", projectTask.getActiveSprint());
    response.setValue("oldBudgetedTime", projectTask.getBudgetedTime());
    response.setValue("projectPlanningTimeList", projectTask.getProjectPlanningTimeList());
  }
}
