package com.axelor.apps.businesssupport.service;

import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.businessproject.service.TeamTaskBusinessProjectServiceImpl;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import java.time.LocalDate;

public class TeamTaskBusinessSupportServiceImpl extends TeamTaskBusinessProjectServiceImpl {

  @Inject
  public TeamTaskBusinessSupportServiceImpl(
      TeamTaskRepository teamTaskRepo,
      PriceListLineRepository priceListLineRepository,
      PriceListService priceListService) {
    super(teamTaskRepo, priceListLineRepository, priceListService);
  }

  @Override
  protected void setModuleFields(TeamTask teamTask, LocalDate date, TeamTask newTeamTask) {
    super.setModuleFields(teamTask, date, newTeamTask);

    // Module 'business support' fields
    newTeamTask.setAssignment(TeamTaskRepository.ASSIGNMENT_PROVIDER);
  }

  @Override
  protected void updateModuleFields(TeamTask teamTask, TeamTask nextTeamTask) {
    super.updateModuleFields(teamTask, nextTeamTask);

    // Module 'business support' fields
    nextTeamTask.setAssignment(TeamTaskRepository.ASSIGNMENT_PROVIDER);
    nextTeamTask.setIsPrivate(teamTask.getIsPrivate());
    nextTeamTask.setTargetVersion(teamTask.getTargetVersion());
  }
}
