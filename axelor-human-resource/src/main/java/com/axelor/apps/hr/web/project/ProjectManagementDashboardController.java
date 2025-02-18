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
package com.axelor.apps.hr.web.project;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.service.ProjectManagementDashboardService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.util.List;
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

  public void computeEmployeeDomainDashboard(ActionRequest request, ActionResponse response) {
    try {
      if (request.getContext().get("project") != null) {
        Long projectId =
            Long.valueOf(((Map) request.getContext().get("project")).get("id").toString());
        Project project = Beans.get(ProjectRepository.class).find(projectId);
        List<Long> idList =
            Beans.get(ProjectManagementDashboardService.class).getFilteredEmployeeIds(project);
        String domain = "self.id in (" + Joiner.on(",").join(idList) + ")";
        response.setAttr("$employee", "domain", domain);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
