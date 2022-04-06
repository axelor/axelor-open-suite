/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.project.web;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.exception.IExceptionMessage;
import com.axelor.apps.project.service.ProjectService;
import com.axelor.apps.project.service.ProjectWorkflowService;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class ProjectController {

  public void importMembers(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    if (project.getTeam() != null) {
      project.getTeam().getMembers().forEach(project::addMembersUserSetItem);
      response.setValue("membersUserSet", project.getMembersUserSet());
    }
  }

  public void checkIfResourceBooked(ActionRequest request, ActionResponse response) {
    if (Beans.get(AppProjectService.class).getAppProject().getCheckResourceAvailibility()) {
      Project project = request.getContext().asType(Project.class);
      if (Beans.get(ProjectService.class).checkIfResourceBooked(project)) {
        response.setError(I18n.get(IExceptionMessage.RESOURCE_ALREADY_BOOKED_ERROR_MSG));
      }
    }
  }

  public void startProject(ActionRequest request, ActionResponse response) {
    try {
      Project project = request.getContext().asType(Project.class);
      project = Beans.get(ProjectRepository.class).find(project.getId());
      Beans.get(ProjectWorkflowService.class).startProject(project);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void finishProject(ActionRequest request, ActionResponse response) {
    try {
      Project project = request.getContext().asType(Project.class);
      project = Beans.get(ProjectRepository.class).find(project.getId());
      Beans.get(ProjectWorkflowService.class).finishProject(project);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelProject(ActionRequest request, ActionResponse response) {
    try {
      Project project = request.getContext().asType(Project.class);
      project = Beans.get(ProjectRepository.class).find(project.getId());
      Beans.get(ProjectWorkflowService.class).cancelProject(project);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void backToNewProject(ActionRequest request, ActionResponse response) {
    try {
      Project project = request.getContext().asType(Project.class);
      project = Beans.get(ProjectRepository.class).find(project.getId());
      Beans.get(ProjectWorkflowService.class).backToNewProject(project);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
