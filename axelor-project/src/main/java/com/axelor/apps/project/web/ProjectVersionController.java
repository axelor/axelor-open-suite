/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.project.db.ProjectVersion;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.service.roadmap.ProjectVersionService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ProjectVersionController {

  public void showSprints(ActionRequest request, ActionResponse response) {
    ProjectVersion projectVersion = request.getContext().asType(ProjectVersion.class);
    boolean isHidden = false;

    Context parentContext = request.getContext().getParent();
    if (parentContext != null && Project.class.equals(parentContext.getContextClass())) {
      isHidden =
          !ProjectRepository.SPRINT_MANAGEMENT_VERSION.equals(
              request.getContext().getParent().asType(Project.class).getSprintManagementSelect());
    } else {
      Set<Project> projectSet =
          Optional.ofNullable(projectVersion)
              .map(ProjectVersion::getProjectSet)
              .orElse(new HashSet<>());
      if (!ObjectUtils.isEmpty(projectSet)) {
        isHidden =
            projectSet.stream()
                .noneMatch(
                    project ->
                        ProjectRepository.SPRINT_MANAGEMENT_VERSION.equals(
                            project.getSprintManagementSelect()));
      }
    }

    response.setAttr("generateSprintsBtn", "hidden", isHidden || projectVersion.getId() == null);
    response.setAttr("sprintDashletPanel", "hidden", isHidden);
  }

  public void checkIfProjectOrVersionConflicts(ActionRequest request, ActionResponse response) {
    ProjectVersion projectVersion = request.getContext().asType(ProjectVersion.class);
    Project project = null;
    Context parentContext = request.getContext().getParent();
    if (parentContext != null && Project.class.equals(parentContext.getContextClass())) {
      project = parentContext.asType(Project.class);
    }
    String error =
        Beans.get(ProjectVersionService.class)
            .checkIfProjectOrVersionConflicts(projectVersion, project);
    if (StringUtils.notEmpty(error)) {
      response.setError(error);
    }
  }
}
