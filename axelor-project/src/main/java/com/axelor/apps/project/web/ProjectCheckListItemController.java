package com.axelor.apps.project.web;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectCheckListItem;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.common.ObjectUtils;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.ArrayList;
import java.util.List;

public class ProjectCheckListItemController {

  public void changeCompleted(ActionRequest request, ActionResponse response) {
    ProjectCheckListItem checkListItem = request.getContext().asType(ProjectCheckListItem.class);

    List<ProjectCheckListItem> updatedToDoList = new ArrayList<>();
    if (!ObjectUtils.isEmpty(checkListItem.getProjectCheckListItemList())) {
      for (ProjectCheckListItem child : checkListItem.getProjectCheckListItemList()) {
        child.setCompleted(checkListItem.getCompleted());
        updatedToDoList.add(child);
      }
    }

    response.setValue("projectCheckListItemList", updatedToDoList);
  }

  public void resetCompletedFields(ActionRequest request, ActionResponse response) {
    List<ProjectCheckListItem> projectCheckListItemList = getProjectCheckListItemList(request);

    if (!ObjectUtils.isEmpty(projectCheckListItemList)) {
      for (ProjectCheckListItem child : projectCheckListItemList) {
        if (!ObjectUtils.isEmpty(child.getProjectCheckListItemList())) {
          child.setCompleted(
              child.getProjectCheckListItemList().stream()
                  .allMatch(ProjectCheckListItem::getCompleted));
        }
      }
    }

    response.setValue("projectCheckListItemList", projectCheckListItemList);
  }

  protected static List<ProjectCheckListItem> getProjectCheckListItemList(ActionRequest request) {
    if (Project.class.equals(request.getContext().getContextClass())) {
      return request.getContext().asType(Project.class).getProjectCheckListItemList();
    } else if (ProjectTask.class.equals(request.getContext().getContextClass())) {
      return request.getContext().asType(ProjectTask.class).getProjectCheckListItemList();
    }

    return new ArrayList<>();
  }
}
