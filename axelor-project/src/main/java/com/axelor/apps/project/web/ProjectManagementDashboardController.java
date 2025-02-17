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

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.service.dashboard.ProjectManagementDashboardService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

import java.util.Map;

@Singleton
public class ProjectManagementDashboardController {

  public void getDates(ActionRequest request, ActionResponse response) {

    try {
      response.setValues(Beans.get(ProjectManagementDashboardService.class).getDate());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setSprintDashlet(ActionRequest request, ActionResponse response) {
    ActionView.ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get("Sprint"));
    ActionView.ActionViewBuilder model = actionViewBuilder.model(Sprint.class.getName());
    actionViewBuilder.name("Sprint-resume");
    actionViewBuilder.add("grid", "project-sprint-grid");
    actionViewBuilder.add("form", "sprint-form");
    actionViewBuilder.context("$totalAllocatedTime", "1");
    actionViewBuilder.context("totalEstimatedTime", "2");
    //  actionViewBuilder.domain("self.project.id ="+ids);
    // actionViewBuilder.context("_projectId", projectId);
    response.setView(actionViewBuilder.map());
  }

  public void getData(ActionRequest request, ActionResponse response) {
    Long projectId = Long.valueOf(( (Map<String, Object>) request.getContext().get("project")).get("id").toString());
    ProjectRepository projectRepo = Beans.get(ProjectRepository.class);
    Project project = projectRepo.find(projectId);
    if (project == null) {
      return;
    }

    response.setValues(Beans.get(ProjectManagementDashboardService.class).getData(project));
  }
}
