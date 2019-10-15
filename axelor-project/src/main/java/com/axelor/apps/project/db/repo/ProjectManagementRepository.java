/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.project.db.repo;

import com.axelor.apps.project.db.Project;
import com.axelor.team.db.Team;
import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectManagementRepository extends ProjectRepository {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private void setAllProjectFullName(Project project) {
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
      project
          .getChildProjectList()
          .stream()
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
    setAllProjectFullName(project);
    ProjectManagementRepository.setAllProjectMembersUserSet(project);

    if (project.getSynchronize()) {
      Team team = project.getTeam();
      if (team != null) {
        team.clearMembers();
        project.getMembersUserSet().forEach(team::addMember);
      }
    }
    return super.save(project);
  }

  @Override
  public Project copy(Project entity, boolean deep) {
    Project project = super.copy(entity, false);
    project.setStatusSelect(STATE_NEW);
    project.setProgress(BigDecimal.ZERO);
    return project;
  }
}
