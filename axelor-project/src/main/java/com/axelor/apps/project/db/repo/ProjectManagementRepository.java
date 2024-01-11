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
package com.axelor.apps.project.db.repo;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.service.ProjectTaskService;
import com.axelor.team.db.Team;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class ProjectManagementRepository extends ProjectRepository {

  @Inject ProjectTaskService projectTaskService;

  protected void setAllProjectFullName(Project project) {
    String projectCode =
        (Strings.isNullOrEmpty(project.getCode())) ? "" : project.getCode() + " - ";
    project.setFullName(projectCode + project.getName());
    if (project.getChildProjectList() != null && !project.getChildProjectList().isEmpty()) {
      for (Project child : project.getChildProjectList()) {
        String code = (Strings.isNullOrEmpty(child.getCode())) ? "" : child.getCode() + " - ";
        child.setFullName(code + child.getName());
      }
    }
  }

  public static void setAllProjectMembersUserSet(Project project) {
    if (project.getParentProject() == null && project.getChildProjectList() != null) {
      project.getChildProjectList().stream()
          .filter(Project::getExtendsMembersFromParent)
          .peek(p -> project.getMembersUserSet().forEach(p::addMembersUserSetItem))
          .forEach(p -> p.setTeam(project.getTeam()));
    } else if (project.getParentProject() != null
        && project.getExtendsMembersFromParent()
        && !project.getSynchronize()) {
      project.getParentProject().getMembersUserSet().forEach(project.getMembersUserSet()::add);
    }
  }

  @Override
  public Project save(Project project) {

    ProjectManagementRepository.setAllProjectMembersUserSet(project);

    if (project.getSynchronize()) {
      Team team = project.getTeam();
      if (team != null) {
        team.clearMembers();
        project.getMembersUserSet().forEach(team::addMember);
      }
    }
    setAllProjectFullName(project);
    project.setDescription(projectTaskService.getTaskLink(project.getDescription()));
    return super.save(project);
  }

  @Override
  public Project copy(Project entity, boolean deep) {
    Project project = super.copy(entity, false);
    project.setCode(null);
    return project;
  }
}
