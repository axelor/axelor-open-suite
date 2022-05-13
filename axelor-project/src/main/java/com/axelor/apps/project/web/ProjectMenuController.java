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
import com.axelor.apps.project.service.ProjectMenuService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProjectMenuController {

  public void allOpenProjectTasks(ActionRequest request, ActionResponse response) {
    response.setView(Beans.get(ProjectMenuService.class).getAllOpenProjectTasks());
  }

  public void allOpenProjectTickets(ActionRequest request, ActionResponse response) {
    response.setView(Beans.get(ProjectMenuService.class).getAllOpenProjectTickets());
  }

  public void allProjects(ActionRequest request, ActionResponse response) {
    response.setView(Beans.get(ProjectMenuService.class).getAllProjects());
  }

  public void allProjectTasks(ActionRequest request, ActionResponse response) {
    response.setView(Beans.get(ProjectMenuService.class).getAllProjectTasks());
  }

  public void allProjectRelatedTasks(ActionRequest request, ActionResponse response) {

    try {
      Project project = request.getContext().asType(Project.class);
      project = Beans.get(ProjectRepository.class).find(project.getId());
      response.setView(Beans.get(ProjectMenuService.class).getAllProjectRelatedTasks(project));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
