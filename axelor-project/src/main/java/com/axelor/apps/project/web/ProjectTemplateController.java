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

import com.axelor.apps.base.db.AppProject;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTemplate;
import com.axelor.apps.project.db.repo.ProjectTemplateRepository;
import com.axelor.apps.project.service.ProjectService;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.util.LinkedHashMap;

@Singleton
public class ProjectTemplateController {

  public void createProjectFromTemplate(ActionRequest request, ActionResponse response) {

    ProjectTemplate projectTemplate = request.getContext().asType(ProjectTemplate.class);
    AppProject appProject = Beans.get(AppProjectService.class).getAppProject();

    if (appProject.getGenerateProjectSequence() && !projectTemplate.getIsBusinessProject()) {

      Project project;
      try {
        project =
            Beans.get(ProjectService.class).createProjectFromTemplate(projectTemplate, null, null);
        response.setView(
            ActionView.define(I18n.get("Project"))
                .model(Project.class.getName())
                .add("form", "project-form")
                .add("grid", "project-grid")
                .context("_showRecord", project.getId())
                .map());

      } catch (AxelorException e) {
        TraceBackService.trace(response, e);
      }

    } else {
      response.setView(
          ActionView.define(I18n.get("Create project from this template"))
              .model(Wizard.class.getName())
              .add("form", "project-template-wizard-form")
              .param("popup", "reload")
              .param("show-toolbar", "false")
              .param("show-confirm", "false")
              .param("width", "large")
              .param("popup-save", "false")
              .context("_projectTemplate", projectTemplate)
              .context("_businessProject", projectTemplate.getIsBusinessProject())
              .map());
    }
  }

  @SuppressWarnings("unchecked")
  public void createProjectFromWizard(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    String projectTemplateId =
        ((LinkedHashMap<String, Object>) context.get("_projectTemplate")).get("id").toString();
    ProjectTemplate projectTemplate =
        Beans.get(ProjectTemplateRepository.class).find(Long.parseLong(projectTemplateId));

    String projectCode = (String) context.get("code");

    Object clientPartnerContext = context.get("clientPartner");
    Partner clientPartner = null;

    if (clientPartnerContext != null) {
      String clientPartnerId =
          ((LinkedHashMap<String, Object>) clientPartnerContext).get("id").toString();
      clientPartner = Beans.get(PartnerRepository.class).find(Long.parseLong(clientPartnerId));
    }

    Project project;
    try {
      project =
          Beans.get(ProjectService.class)
              .createProjectFromTemplate(projectTemplate, projectCode, clientPartner);
      response.setCanClose(true);

      response.setView(
          ActionView.define(I18n.get("Project"))
              .model(Project.class.getName())
              .add("form", "project-form")
              .add("grid", "project-grid")
              .context("_showRecord", project.getId())
              .map());
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }
}
