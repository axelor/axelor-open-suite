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
package com.axelor.apps.project.service;

import com.axelor.apps.base.db.Frequency;
import com.axelor.apps.base.db.repo.FrequencyRepository;
import com.axelor.apps.base.service.FrequencyService;
import com.axelor.apps.base.service.TeamTaskServiceImpl;
import com.axelor.apps.project.db.Project;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.List;

public class TeamTaskServiceProjectImpl extends TeamTaskServiceImpl
    implements TeamTaskProjectService {

  @Inject
  public TeamTaskServiceProjectImpl(TeamTaskRepository teamTaskRepo) {
    super(teamTaskRepo);
  }

  @Override
  public TeamTask create(String subject, Project project, User assignedTo) {
    TeamTask task = new TeamTask();
    task.setName(subject);
    task.setAssignedTo(assignedTo);
    task.setTaskDate(LocalDate.now());
    task.setStatus("new");
    task.setPriority("normal");
    project.addTeamTaskListItem(task);
    return task;
  }

  @Override
  public void generateTasks(TeamTask teamTask, Frequency frequency) {
    List<LocalDate> taskDates =
        Beans.get(FrequencyService.class)
            .getDates(frequency, teamTask.getTaskDate(), frequency.getEndDate());

    taskDates.removeIf(date -> date.equals(teamTask.getTaskDate()));

    TeamTask lastTask = teamTask;
    for (LocalDate date : taskDates) {
      TeamTask newTeamTask = teamTaskRepo.copy(teamTask, false);
      newTeamTask.setIsFirst(false);
      newTeamTask.setHasDateOrFrequencyChanged(false);
      newTeamTask.setDoApplyToAllNextTasks(false);
      newTeamTask.setFrequency(
          Beans.get(FrequencyRepository.class).copy(teamTask.getFrequency(), false));
      newTeamTask.setTaskDate(date);
      newTeamTask.setTaskDeadline(date);
      newTeamTask.setNextTeamTask(null);

      // Module 'project' fields
      newTeamTask.setProgressSelect(0);
      newTeamTask.setTaskEndDate(date);

      teamTaskRepo.save(newTeamTask);

      lastTask.setNextTeamTask(newTeamTask);
      teamTaskRepo.save(lastTask);
      lastTask = newTeamTask;
    }
  }

  @Override
  public void updateNextTask(TeamTask teamTask) {
    TeamTask nextTeamTask = teamTask.getNextTeamTask();
    if (nextTeamTask != null) {
      nextTeamTask.setName(teamTask.getName());
      nextTeamTask.setTeam(teamTask.getTeam());
      nextTeamTask.setPriority(teamTask.getPriority());
      nextTeamTask.setStatus(teamTask.getStatus());
      nextTeamTask.setTaskDuration(teamTask.getTaskDuration());
      nextTeamTask.setAssignedTo(teamTask.getAssignedTo());
      nextTeamTask.setDescription(teamTask.getDescription());

      // Module 'project' fields
      nextTeamTask.setFullName(teamTask.getFullName());
      nextTeamTask.setProject(teamTask.getProject());
      nextTeamTask.setProjectCategory(teamTask.getProjectCategory());
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

      teamTaskRepo.save(nextTeamTask);
      updateNextTask(nextTeamTask);
    }
  }
}
