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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectHistoryLine;
import com.axelor.apps.project.db.repo.ProjectHistoryLineRepository;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ProjectHistoryServiceImpl implements ProjectHistoryService {

  protected ProjectHistoryLineRepository projectHistoryLineRepository;

  @Inject
  public ProjectHistoryServiceImpl(ProjectHistoryLineRepository projectHistoryLineRepository) {
    this.projectHistoryLineRepository = projectHistoryLineRepository;
  }

  @Override
  public Map<String, Object> processRequestToDisplayProjectHistory(Long id) {

    ProjectHistoryLine projectHistoryLine = projectHistoryLineRepository.find(id);

    Map<String, Object> data = new HashMap<>();
    data.put("soldTime", projectHistoryLine.getSoldTime());
    data.put("updatedTime", projectHistoryLine.getUpdatedTime());
    data.put("plannedTime", projectHistoryLine.getPlannedTime());
    data.put("spentTime", projectHistoryLine.getSpentTime());
    data.put(
        "unit",
        Optional.ofNullable(projectHistoryLine.getProject())
            .map(Project::getProjectTimeUnit)
            .map(unit -> unit.getName() + "(s)")
            .orElse(""));
    data.put("progress", projectHistoryLine.getPercentageOfProgress() + " %");
    data.put("consumption", projectHistoryLine.getPercentageOfConsumption() + " %");
    data.put("remaining", projectHistoryLine.getRemainingAmountToDo());
    data.put("turnover", projectHistoryLine.getTurnover());
    data.put("initialCosts", projectHistoryLine.getInitialCosts());
    data.put("initialMargin", projectHistoryLine.getInitialMargin());
    data.put("initialMarkup", projectHistoryLine.getInitialMarkup());
    data.put("realTurnover", projectHistoryLine.getRealTurnover());
    data.put("realCosts", projectHistoryLine.getRealCosts());
    data.put("realMargin", projectHistoryLine.getRealMargin());
    data.put("realMarkup", projectHistoryLine.getRealMarkup());
    data.put("forecastCosts", projectHistoryLine.getForecastCosts());
    data.put("forecastMargin", projectHistoryLine.getForecastMargin());
    data.put("forecastMarkup", projectHistoryLine.getForecastMarkup());

    return data;
  }
}
