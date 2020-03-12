/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.AppProject;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.exception.IExceptionMessage;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.team.db.Team;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import javax.persistence.PersistenceException;

public class ProjectManagementRepository extends ProjectRepository {

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

    ProjectManagementRepository.setAllProjectMembersUserSet(project);

    if (project.getSynchronize()) {
      Team team = project.getTeam();
      if (team != null) {
        team.clearMembers();
        project.getMembersUserSet().forEach(team::addMember);
      }
    }

    try {
      AppProject appProject = Beans.get(AppProjectService.class).getAppProject();

      if (Strings.isNullOrEmpty(project.getCode()) && appProject.getGenerateProjectSequence()) {
        Company company = project.getCompany();
        String seq =
            Beans.get(SequenceService.class)
                .getSequenceNumber(SequenceRepository.PROJECT_SEQUENCE, company);

        if (seq == null) {
          throw new AxelorException(
              company,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessage.PROJECT_SEQUENCE_ERROR),
              company.getName());
        }

        project.setCode(seq);
      }
    } catch (AxelorException e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }
    setAllProjectFullName(project);

    project.setEstimatedTimeHrs(
        project
            .getEstimatedTimeDays()
            .multiply(Beans.get(AppBaseService.class).getAppBase().getDailyWorkHours()));

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
