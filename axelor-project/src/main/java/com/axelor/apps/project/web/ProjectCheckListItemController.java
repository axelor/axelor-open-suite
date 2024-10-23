/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
