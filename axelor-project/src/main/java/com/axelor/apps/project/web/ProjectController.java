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
package com.axelor.apps.project.web;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.service.ProjectTemplateService;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ProjectController {

  @Inject ProjectTemplateService projectTemplateService;
  @Inject ProjectRepository projectRepository;

  public void importMembers(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    if (project.getTeam() != null) {
      project.getTeam().getMembers().forEach(project::addMembersUserSetItem);
      response.setValue("membersUserSet", project.getMembersUserSet());
    }
  }

  public void generateProjectFromTemplate(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    if (project.getId() != null) {
      Project projectCopy =
          projectTemplateService.generateProjectFromTemplate(
              projectRepository.find(project.getId()));
      if (projectCopy != null) {
        response.setView(
            ActionView.define("Project")
                .model(Project.class.getName())
                .add("form", "project-form")
                .add("grid", "project-grid")
                .domain(
                    "self.isProject = true and self.projectTypeSelect = 1 and self.isTemplate = false")
                .context("_showRecord", projectCopy.getId().toString())
                .map());
      }
    }
  }
}
