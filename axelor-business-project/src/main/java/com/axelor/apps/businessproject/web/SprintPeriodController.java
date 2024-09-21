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
package com.axelor.apps.businessproject.web;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.businessproject.service.sprint.SprintPeriodService;
import com.axelor.apps.project.db.SprintPeriod;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SprintPeriodController {

  @SuppressWarnings("unchecked")
  public void generateSprintPeriods(ActionRequest request, ActionResponse response) {

    Object companyContext = request.getContext().get("company");
    Object fromDateContext = request.getContext().get("fromDate");
    Object toDateContext = request.getContext().get("toDate");
    Object nbOfDaysPerSprintContext = request.getContext().get("nbOfDaysPerSprint");
    Object considerWeekendContext = request.getContext().get("considerWeekend");

    if (companyContext != null
        && fromDateContext != null
        && toDateContext != null
        && nbOfDaysPerSprintContext != null) {

      Long companyId =
          Long.valueOf(((LinkedHashMap<String, Object>) companyContext).get("id").toString());
      LocalDate fromDate = LocalDate.parse(fromDateContext.toString());
      LocalDate toDate = LocalDate.parse(toDateContext.toString());
      int nbOfDaysPerSprint = (Integer) nbOfDaysPerSprintContext;
      boolean considerWeekend = Boolean.TRUE.equals(considerWeekendContext);

      Company company = Beans.get(CompanyRepository.class).find(companyId);

      List<SprintPeriod> sprintPeriodList =
          Beans.get(SprintPeriodService.class)
              .generateSprintPeriods(company, fromDate, toDate, nbOfDaysPerSprint, considerWeekend);

      if (CollectionUtils.isNotEmpty(sprintPeriodList)) {
        response.setInfo(I18n.get("Sprint periods generated"));

        ActionView.ActionViewBuilder actionViewBuilder =
            ActionView.define(I18n.get("Sprint periods"));
        actionViewBuilder.model(SprintPeriod.class.getName());
        actionViewBuilder.add("grid", "sprint-period-grid");
        actionViewBuilder.add("form", "sprint-period-form");
        actionViewBuilder.domain("self.id IN (:sprintPeriodIds)");
        actionViewBuilder.context(
            "sprintPeriodIds",
            sprintPeriodList.stream().map(SprintPeriod::getId).collect(Collectors.toList()));

        response.setView(actionViewBuilder.map());
      }
    }

    response.setReload(true);
  }
}
