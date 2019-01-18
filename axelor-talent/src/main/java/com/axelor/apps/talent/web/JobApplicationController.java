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
package com.axelor.apps.talent.web;

import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.talent.db.JobApplication;
import com.axelor.apps.talent.db.repo.JobApplicationRepository;
import com.axelor.apps.talent.service.JobApplicationService;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;

@Singleton
public class JobApplicationController {

  @Inject private JobApplicationRepository jobApplicationRepo;

  @Inject private JobApplicationService jobApplicationService;

  @Inject private PartnerService partnerService;

  public void hire(ActionRequest request, ActionResponse response) {

    JobApplication jobApplication = request.getContext().asType(JobApplication.class);

    jobApplication = jobApplicationRepo.find(jobApplication.getId());

    Employee employee = jobApplicationService.hire(jobApplication);

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
        partnerService.getSocialNetworkUrl(
            application.getFirstName(), application.getLastName(), 2);
    response.setAttr("linkedinLabel", "title", urlMap.get("linkedin"));
  }
}
