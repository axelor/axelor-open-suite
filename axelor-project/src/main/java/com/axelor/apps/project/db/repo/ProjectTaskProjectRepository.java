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
package com.axelor.apps.project.db.repo;

import com.axelor.apps.base.db.Frequency;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.ProjectTaskService;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectTaskProjectRepository extends ProjectTaskRepository {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public ProjectTask save(ProjectTask projectTask) {
    List<String> composedNames = new ArrayList<>();
    if (projectTask.getId() != null) {
      composedNames.add("#" + projectTask.getId());
    }

    composedNames.add(projectTask.getName());
    projectTask.setFullName(String.join(" ", composedNames));

    ProjectTaskService projectTaskService = Beans.get(ProjectTaskService.class);

    if (projectTask.getDoApplyToAllNextTasks()
        && projectTask.getNextProjectTask() != null
        && projectTask.getHasDateOrFrequencyChanged()) {
      // remove next tasks
      projectTaskService.removeNextTasks(projectTask);

      // regenerate new tasks
      projectTask.setIsFirst(true);
    }

    Project project = projectTask.getProject();
    if (project.getIsShowFrequency()) {
      Frequency frequency = projectTask.getFrequency();
      if (frequency != null
          && projectTask.getIsFirst()
          && projectTask.getNextProjectTask() == null) {
        if (projectTask.getTaskDate() != null) {
          if (frequency.getEndDate().isBefore(projectTask.getTaskDate())) {
            throw new PersistenceException(
                I18n.get(
                    ProjectExceptionMessage
                        .PROJECT_TASK_FREQUENCY_END_DATE_CAN_NOT_BE_BEFORE_TASK_DATE));
          }
        } else {
          throw new PersistenceException(
              I18n.get(ProjectExceptionMessage.PROJECT_TASK_FILL_TASK_DATE));
        }

        projectTaskService.generateTasks(projectTask, frequency);
      }
    }

    if (projectTask.getDoApplyToAllNextTasks()) {
      projectTaskService.updateNextTask(projectTask);
    }

    projectTask.setDoApplyToAllNextTasks(false);
    projectTask.setHasDateOrFrequencyChanged(false);

    if (StringUtils.isEmpty(projectTask.getTicketNumber())
        && Beans.get(AppProjectService.class).getAppProject().getIsEnablePerProjectTaskSequence()) {
      int sequence = project.getNextProjectTaskSequence();
      project.setNextProjectTaskSequence(sequence + 1);
      projectTask.setTicketNumber(project.getCode() + sequence);
    }

    projectTask.setDescription(projectTaskService.getTaskLink(projectTask.getDescription()));

    return super.save(projectTask);
  }

  @Override
  public Map<String, Object> validate(Map<String, Object> json, Map<String, Object> context) {

    logger.debug("Validate project task:{}", json);

    logger.debug(
        "Planned progress:{}, ProgressSelect: {}, DurationHours: {}, TaskDuration: {}",
        json.get("plannedProgress"),
        json.get("progressSelect"),
        json.get("durationHours"),
        json.get("taskDuration"));

    if (json.get("id") != null) {

      ProjectTask savedTask = find(Long.parseLong(json.get("id").toString()));
      if (json.get("plannedProgress") != null) {
        BigDecimal plannedProgress = new BigDecimal(json.get("plannedProgress").toString());
        if (plannedProgress != null
            && savedTask.getPlannedProgress().intValue() != plannedProgress.intValue()) {
          logger.debug(
              "Updating progressSelect: {}", ((int) (plannedProgress.intValue() * 0.10)) * 10);
          json.put("progressSelect", ((int) (plannedProgress.intValue() * 0.10)) * 10);
        }
      } else if (json.get("progressSelect") != null) {
        Integer progressSelect = new Integer(json.get("progressSelect").toString());
        logger.debug("Updating plannedProgress: {}", progressSelect);
        json.put("plannedProgress", new BigDecimal(progressSelect));
      }
      if (json.get("durationHours") != null) {
        BigDecimal durationHours = new BigDecimal(json.get("durationHours").toString());
        if (durationHours != null
            && savedTask.getDurationHours().intValue() != durationHours.intValue()) {
          logger.debug(
              "Updating taskDuration: {}",
              durationHours.divide(new BigDecimal(24), RoundingMode.HALF_UP).intValue());
          json.put("taskDuration", durationHours.multiply(new BigDecimal(3600)).intValue());
        }
      } else if (json.get("taskDuration") != null) {
        Integer taskDuration = new Integer(json.get("taskDuration").toString());
        logger.debug("Updating durationHours: {}", taskDuration / 3600);
        json.put("durationHours", new BigDecimal(taskDuration / 3600));
      }

    } else {

      if (json.get("progressSelect") != null) {
        Integer progressSelect = new Integer(json.get("progressSelect").toString());
        json.put("plannedProgress", new BigDecimal(progressSelect));
      }
      if (json.get("taskDuration") != null) {
        Integer taskDuration = new Integer(json.get("taskDuration").toString());
        json.put("durationHours", new BigDecimal(taskDuration / 3600));
      }
    }

    return super.validate(json, context);
  }

  @Override
  public ProjectTask copy(ProjectTask entity, boolean deep) {
    ProjectTask task = super.copy(entity, deep);
    task.setAssignedTo(null);
    task.setTaskDate(null);
    task.setPriority(null);
    task.setProgressSelect(null);
    task.setTaskEndDate(null);
    task.setMetaFile(null);
    return task;
  }
}
