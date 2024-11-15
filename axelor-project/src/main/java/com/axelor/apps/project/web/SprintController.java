/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.service.sprint.SprintService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SprintController {

  public void computeDates(ActionRequest request, ActionResponse response) {

    Sprint sprint = request.getContext().asType(Sprint.class);

    SprintService sprintService = Beans.get(SprintService.class);
    LocalDate fromDate = sprintService.computeFromDate(sprint);
    LocalDate toDate = sprintService.computeToDate(sprint);

    if (fromDate != null && toDate != null) {
      response.setValue("fromDate", fromDate);
      response.setValue("toDate", toDate);
    }
  }

  public void generateSprints(ActionRequest request, ActionResponse response) {

    Object companyContext = request.getContext().get("company");
    Object projectsContext = request.getContext().get("projects");
    Object fromDateContext = request.getContext().get("fromDate");
    Object toDateContext = request.getContext().get("toDate");

    if (companyContext != null
        && projectsContext != null
        && fromDateContext != null
        && toDateContext != null) {

      ProjectRepository projectRepo = Beans.get(ProjectRepository.class);

      Long companyId =
          Long.valueOf(((LinkedHashMap<String, Object>) companyContext).get("id").toString());

      Company company = Beans.get(CompanyRepository.class).find(companyId);
      Set<Project> projectSet =
          ((List<LinkedHashMap<String, Object>>) projectsContext)
              .stream()
                  .map(project -> projectRepo.find(Long.valueOf(project.get("id").toString())))
                  .collect(Collectors.toSet());
      LocalDate fromDate = LocalDate.parse(fromDateContext.toString());
      LocalDate toDate = LocalDate.parse(toDateContext.toString());

      Set<Sprint> sprintSet =
          Beans.get(SprintService.class).generateSprints(company, projectSet, fromDate, toDate);

      if (CollectionUtils.isNotEmpty(sprintSet)) {
        response.setInfo(I18n.get("Sprints generated"));

        ActionView.ActionViewBuilder actionViewBuilder = ActionView.define(I18n.get("Sprints"));
        actionViewBuilder.model(Sprint.class.getName());
        actionViewBuilder.add("grid", "sprint-grid");
        actionViewBuilder.add("form", "sprint-form");
        actionViewBuilder.domain("self.id IN (:sprintIds)");
        actionViewBuilder.context(
            "sprintIds", sprintSet.stream().map(Sprint::getId).collect(Collectors.toList()));

        response.setView(actionViewBuilder.map());
      }
    }
  }
}
