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
package com.axelor.apps.project.db.repo;

import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeamTaskProjectRepository extends TeamTaskRepository {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public TeamTask save(TeamTask teamTask) {
    teamTask.setFullName("#" + teamTask.getId() + " " + teamTask.getName());
    return super.save(teamTask);
  }

  @Override
  public Map<String, Object> validate(Map<String, Object> json, Map<String, Object> context) {

    logger.debug("Validate team task:{}", json);

    logger.debug(
        "Planned progress:{}, ProgressSelect: {}, DurationHours: {}, TaskDuration: {}",
        json.get("plannedProgress"),
        json.get("progressSelect"),
        json.get("durationHours"),
        json.get("taskDuration"));

    if (json.get("id") != null) {

      TeamTask savedTask = find(Long.parseLong(json.get("id").toString()));
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
              durationHours.divide(new BigDecimal(24), RoundingMode.HALF_EVEN).intValue());
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
}
