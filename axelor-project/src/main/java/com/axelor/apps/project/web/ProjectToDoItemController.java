package com.axelor.apps.project.web;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectToDoItem;
import com.axelor.common.ObjectUtils;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.ArrayList;
import java.util.List;

public class ProjectToDoItemController {

  public void changeCompleted(ActionRequest request, ActionResponse response) {
    ProjectToDoItem toDoItem = request.getContext().asType(ProjectToDoItem.class);

    List<ProjectToDoItem> updatedToDoList = new ArrayList<>();
    if (!ObjectUtils.isEmpty(toDoItem.getProjectToDoItemList())) {
      for (ProjectToDoItem child : toDoItem.getProjectToDoItemList()) {
        child.setCompleted(toDoItem.getCompleted());
        updatedToDoList.add(child);
      }
    }

    response.setValue("projectToDoItemList", updatedToDoList);
  }

  public void resetCompletedFields(ActionRequest request, ActionResponse response) {
    List<ProjectToDoItem> projectToDoItemList = getProjectToDoItemList(request);

    if (!ObjectUtils.isEmpty(projectToDoItemList)) {
      for (ProjectToDoItem child : projectToDoItemList) {
        if (!ObjectUtils.isEmpty(child.getProjectToDoItemList())) {
          child.setCompleted(
              child.getProjectToDoItemList().stream().allMatch(ProjectToDoItem::getCompleted));
        }
      }
    }

    response.setValue("projectToDoItemList", projectToDoItemList);
  }

  protected static List<ProjectToDoItem> getProjectToDoItemList(ActionRequest request) {
    if (Project.class.equals(request.getContext().getContextClass())) {
      return request.getContext().asType(Project.class).getProjectToDoItemList();
    } else if (ProjectTask.class.equals(request.getContext().getContextClass())) {
      return request.getContext().asType(ProjectTask.class).getProjectToDoItemList();
    }

    return new ArrayList<>();
  }
}
