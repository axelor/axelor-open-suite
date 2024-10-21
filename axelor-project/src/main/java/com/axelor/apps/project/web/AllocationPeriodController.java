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
import com.axelor.apps.project.db.AllocationPeriod;
import com.axelor.apps.project.service.sprint.AllocationPeriodService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class AllocationPeriodController {

  public void generateAllocationPeriods(ActionRequest request, ActionResponse response) {

    Object companyContext = request.getContext().get("company");
    Object fromDateContext = request.getContext().get("fromDate");
    Object toDateContext = request.getContext().get("toDate");
    Object numberOfWeeksPerPeriodContext = request.getContext().get("numberOfWeeksPerPeriod");

    if (companyContext != null
        && fromDateContext != null
        && toDateContext != null
        && numberOfWeeksPerPeriodContext != null) {

      Long companyId =
          Long.valueOf(((LinkedHashMap<String, Object>) companyContext).get("id").toString());

      Company company = Beans.get(CompanyRepository.class).find(companyId);
      LocalDate fromDate = LocalDate.parse(fromDateContext.toString());
      LocalDate toDate = LocalDate.parse(toDateContext.toString());
      int numberOfWeeksPerPeriod = (Integer) numberOfWeeksPerPeriodContext;

      Set<AllocationPeriod> allocationPeriodSet =
          Beans.get(AllocationPeriodService.class)
              .generateAllocationPeriods(company, fromDate, toDate, numberOfWeeksPerPeriod);

      if (CollectionUtils.isNotEmpty(allocationPeriodSet)) {
        response.setInfo(I18n.get("Allocation periods generated"));

        ActionView.ActionViewBuilder actionViewBuilder =
            ActionView.define(I18n.get("Allocation periods"));
        actionViewBuilder.model(AllocationPeriod.class.getName());
        actionViewBuilder.add("grid", "allocation-period-grid");
        actionViewBuilder.add("form", "allocation-period-form");
        actionViewBuilder.domain("self.id IN (:allocationPeriodIds)");
        actionViewBuilder.context(
            "allocationPeriodIds",
            allocationPeriodSet.stream().map(AllocationPeriod::getId).collect(Collectors.toList()));

        response.setView(actionViewBuilder.map());
      }
    }
  }
}
