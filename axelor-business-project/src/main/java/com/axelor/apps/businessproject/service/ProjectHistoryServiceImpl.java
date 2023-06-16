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
