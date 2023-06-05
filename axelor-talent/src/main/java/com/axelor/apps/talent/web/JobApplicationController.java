/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.talent.web;

import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.talent.db.JobApplication;
import com.axelor.apps.talent.db.repo.JobApplicationRepository;
import com.axelor.apps.talent.service.JobApplicationService;
import com.axelor.dms.db.DMSFile;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.Map;

@Singleton
public class JobApplicationController {

  public void hire(ActionRequest request, ActionResponse response) {

    JobApplication jobApplication = request.getContext().asType(JobApplication.class);

    jobApplication = Beans.get(JobApplicationRepository.class).find(jobApplication.getId());

    Employee employee = Beans.get(JobApplicationService.class).hire(jobApplication);

    response.setReload(true);

    response.setView(
        ActionView.define(I18n.get("Employee"))
            .model(Employee.class.getName())
            .add("grid", "employee-grid")
            .add("form", "employee-form")
            .param("search-filters", "employee-filters")
            .context("_showRecord", employee.getId())
            .map());
  }

  public void setSocialNetworkUrl(ActionRequest request, ActionResponse response) {

    JobApplication application = request.getContext().asType(JobApplication.class);
    Map<String, String> urlMap =
        Beans.get(PartnerService.class)
            .getSocialNetworkUrl(application.getFirstName(), application.getLastName(), 2);
    response.setAttr("linkedinLabel", "title", urlMap.get("linkedin"));
  }

  public void showResume(ActionRequest request, ActionResponse response) {
    try {
      JobApplication application = request.getContext().asType(JobApplication.class);

      application = Beans.get(JobApplicationRepository.class).find(application.getId());

      if (application.getResumeId() != null) {
        response.setView(
            ActionView.define(I18n.get("JobApplication.resume"))
                .model(DMSFile.class.getName())
                .add("form", "dms-file-form")
                .context("_showRecord", application.getResumeId().toString())
                .map());
      } else {
        response.setAlert(I18n.get("No resume found"));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setDMSFile(ActionRequest request, ActionResponse response) {
    try {
      JobApplication application = request.getContext().asType(JobApplication.class);

      application = Beans.get(JobApplicationRepository.class).find(application.getId());

      Beans.get(JobApplicationService.class).setDMSFile(application);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
