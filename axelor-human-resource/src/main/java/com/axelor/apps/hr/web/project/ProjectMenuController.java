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
package com.axelor.apps.hr.web.project;

import com.axelor.apps.project.db.AllocationLine;
import com.axelor.apps.project.db.AllocationPeriod;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.AllocationPeriodRepository;
import com.axelor.apps.project.db.repo.SprintRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class ProjectMenuController {

  public void viewAllocationLines(ActionRequest request, ActionResponse response) {

    Project project = request.getContext().asType(Project.class);

    Sprint sprint = project.getSprint();

    List<Sprint> sprintList =
        sprint != null ? Collections.singletonList(sprint) : new ArrayList<>();
    Set<AllocationPeriod> allocationPeriodSet = new HashSet<>();

    AllocationPeriodRepository allocationPeriodRepo = Beans.get(AllocationPeriodRepository.class);

    Optional.ofNullable(request.getContext().get("allocationPeriodSet"))
        .ifPresent(
            context ->
                ((List<LinkedHashMap<String, Object>>) context)
                    .stream()
                        .map(
                            period ->
                                allocationPeriodRepo.find(
                                    Long.parseLong(period.get("id").toString())))
                        .forEach(allocationPeriodSet::add));

    if (sprint == null && allocationPeriodSet.isEmpty()) {
      sprintList = Beans.get(SprintRepository.class).findByProject(project).fetch();

      if (CollectionUtils.isNotEmpty(sprintList)) {
        sprintList.forEach(s -> allocationPeriodSet.addAll(s.getAllocationPeriodSet()));
      }
    }

    if (sprint != null && CollectionUtils.isEmpty(allocationPeriodSet)) {
      allocationPeriodSet.addAll(sprint.getAllocationPeriodSet());
    }

    ActionView.ActionViewBuilder actionViewBuilder =
        ActionView.define(I18n.get("Allocation lines"))
            .model(AllocationLine.class.getName())
            .add("grid", "allocation-line-sprint-grid")
            .add("form", "allocation-line-form")
            .domain(
                "self.sprint in :_sprintList and self.allocationPeriod in :_allocationPeriodSet")
            .context("_sprintList", sprintList)
            .context("_allocationPeriodSet", allocationPeriodSet);

    response.setView(actionViewBuilder.map());
  }
}
