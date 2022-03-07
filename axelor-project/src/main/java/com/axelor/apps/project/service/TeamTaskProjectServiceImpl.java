/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.project.service;

import com.axelor.apps.base.service.TeamTaskServiceImpl;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.project.db.Project;
import com.axelor.auth.db.User;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;

public class TeamTaskProjectServiceImpl extends TeamTaskServiceImpl
    implements TeamTaskProjectService {

  protected AppBaseService appBaseService;

  @Inject
  public TeamTaskProjectServiceImpl(
      TeamTaskRepository teamTaskRepo, AppBaseService appBaseService) {
    super(teamTaskRepo);
    this.appBaseService = appBaseService;
  }

  @Override
  @Transactional
  public TeamTask create(String subject, Project project, User assignedTo) {
    TeamTask task = new TeamTask();
    task.setName(subject);
    task.setAssignedTo(assignedTo);
    task.setTaskDate(appBaseService.getTodayDate(project.getCompany()));
    task.setStatus("new");
    task.setPriority("normal");
    project.addTeamTaskListItem(task);
    teamTaskRepo.save(task);
    return task;
  }

  @Override
  protected void setModuleFields(TeamTask teamTask, LocalDate date, TeamTask newTeamTask) {
    super.setModuleFields(teamTask, date, newTeamTask);

    // Module 'project' fields
    newTeamTask.setProgressSelect(0);
    newTeamTask.setTaskEndDate(date);
  }

  @Override
  protected void updateModuleFields(TeamTask teamTask, TeamTask nextTeamTask) {
    super.updateModuleFields(teamTask, nextTeamTask);

    // Module 'project' fields
    nextTeamTask.setFullName(teamTask.getFullName());
    nextTeamTask.setProject(teamTask.getProject());
    nextTeamTask.setTeamTaskCategory(teamTask.getTeamTaskCategory());
    nextTeamTask.setProgressSelect(0);

    teamTask.getMembersUserSet().forEach(nextTeamTask::addMembersUserSetItem);

    nextTeamTask.setTeam(teamTask.getTeam());
    nextTeamTask.setParentTask(teamTask.getParentTask());
    nextTeamTask.setProduct(teamTask.getProduct());
    nextTeamTask.setUnit(teamTask.getUnit());
    nextTeamTask.setQuantity(teamTask.getQuantity());
    nextTeamTask.setUnitPrice(teamTask.getUnitPrice());
    nextTeamTask.setTaskEndDate(teamTask.getTaskEndDate());
    nextTeamTask.setBudgetedTime(teamTask.getBudgetedTime());
    nextTeamTask.setCurrency(teamTask.getCurrency());
  }

  @Override
  @Transactional
  public void deleteTeamTask(TeamTask teamTask) {
    teamTaskRepo.remove(teamTask);
  }
}
