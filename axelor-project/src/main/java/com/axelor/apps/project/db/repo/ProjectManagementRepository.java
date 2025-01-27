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
package com.axelor.apps.project.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.ProjectTaskService;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.project.service.roadmap.ProjectVersionRemoveService;
import com.axelor.common.StringUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.studio.db.AppProject;
import com.axelor.team.db.Team;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.Map;
import javax.persistence.PersistenceException;

public class ProjectManagementRepository extends ProjectRepository {

  @Inject ProjectTaskService projectTaskService;
  @Inject AppProjectService appProjectService;

  protected void setAllProjectFullName(Project project) throws AxelorException {
    setProjectFullName(project);
    if (project.getChildProjectList() != null && !project.getChildProjectList().isEmpty()) {
      for (Project child : project.getChildProjectList()) {
        setProjectFullName(child);
      }
    }
  }

  protected void setProjectFullName(Project project) throws AxelorException {
    Context scriptContext = new Context(Mapper.toMap(project), Project.class);
    GroovyScriptHelper groovyScriptHelper = new GroovyScriptHelper(scriptContext);

    String fullNameGroovyFormula = appProjectService.getAppProject().getFullNameGroovyFormula();
    if (Strings.isNullOrEmpty(project.getCode())) {
      project.setCode("");
    }
    if (Strings.isNullOrEmpty(project.getName())) {
      project.setName("");
    }
    if (StringUtils.isBlank(fullNameGroovyFormula)) {
      fullNameGroovyFormula = "code +\"-\"+ name";
    }
    try {
      Object result = groovyScriptHelper.eval(fullNameGroovyFormula);
      if (result == null) {
        throw new AxelorException(
            project,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(ProjectExceptionMessage.PROJECT_GROOVY_FORMULA_ERROR));
      }
      project.setFullName(result.toString());
    } catch (Exception e) {
      throw new AxelorException(
          e,
          project,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProjectExceptionMessage.PROJECT_GROOVY_FORMULA_SYNTAX_ERROR));
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

    AppProject appProject = Beans.get(AppProjectService.class).getAppProject();

    try {
      if (StringUtils.isBlank(project.getCode()) && appProject.getGenerateProjectSequence()) {
        Company company = project.getCompany();
        String seq =
            Beans.get(SequenceService.class)
                .getSequenceNumber(
                    SequenceRepository.PROJECT_SEQUENCE, company, Project.class, "code", project);

        if (seq == null) {
          throw new AxelorException(
              company,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(ProjectExceptionMessage.PROJECT_SEQUENCE_ERROR),
              company.getName());
        }
        project.setCode(seq);
      }
    } catch (AxelorException e) {
      throw new PersistenceException(e.getMessage(), e);
    }

    ProjectManagementRepository.setAllProjectMembersUserSet(project);

    if (project.getSynchronize()) {
      Team team = project.getTeam();
      if (team != null) {
        team.clearMembers();
        project.getMembersUserSet().forEach(team::addMember);
      }
    }
    try {
      setAllProjectFullName(project);
    } catch (AxelorException e) {
      TraceBackService.traceExceptionFromSaveMethod(e.getCause());
      throw new PersistenceException(e.getMessage(), e);
    }
    project.setDescription(projectTaskService.getTaskLink(project.getDescription()));
    return super.save(project);
  }

  @Override
  public Project copy(Project entity, boolean deep) {
    Project project = super.copy(entity, false);
    project.setCode(null);
    return project;
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    try {
      final String canceledProjectStatusIdStr = "$canceledProjectStatusId";

      AppProject appProject = Beans.get(AppProjectService.class).getAppProject();

      if (appProject.getCanceledProjectStatus() != null) {
        json.put(canceledProjectStatusIdStr, appProject.getCanceledProjectStatus().getId());
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    return super.populate(json, context);
  }

  @Override
  public void remove(Project entity) {
    Beans.get(ProjectVersionRemoveService.class).removeProjectFromRoadmap(entity);

    super.remove(entity);
  }
}
