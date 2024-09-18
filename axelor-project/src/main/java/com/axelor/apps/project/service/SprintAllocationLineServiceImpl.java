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
package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.SprintAllocationLine;
import com.axelor.apps.project.db.repo.SprintAllocationLineRepository;
import com.axelor.apps.project.db.repo.SprintRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SprintAllocationLineServiceImpl implements SprintAllocationLineService {

  public SprintAllocationLineRepository sprintAllocationLineRepo;
  public SprintRepository sprintRepo;

  @Inject
  public SprintAllocationLineServiceImpl(
      SprintAllocationLineRepository sprintAllocationLineRepo, SprintRepository sprintRepo) {
    this.sprintAllocationLineRepo = sprintAllocationLineRepo;
    this.sprintRepo = sprintRepo;
  }

  @Override
  @Transactional
  public void updateSprintTotals(SprintAllocationLine sprintAllocationLine) {

    Sprint sprint = sprintAllocationLine.getSprint();

    List<SprintAllocationLine> sprintAllocationLineList =
        sprintAllocationLineRepo.all().filter("self.sprint = ?1", sprint).fetch();

    if (CollectionUtils.isNotEmpty(sprintAllocationLineList)) {
      BigDecimal totalAllocatedTime =
          sprintAllocationLineList.stream()
              .map(SprintAllocationLine::getAllocated)
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      BigDecimal totalPlannedTime =
          sprintAllocationLineList.stream()
              .map(SprintAllocationLine::getPlannedTime)
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      BigDecimal totalRemainingTime =
          sprintAllocationLineList.stream()
              .map(SprintAllocationLine::getRemainingTime)
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      sprint.setTotalAllocatedTime(totalAllocatedTime);
      sprint.setTotalPlannedTime(totalPlannedTime);
      sprint.setTotalRemainingTime(totalRemainingTime);
    } else {
      sprint.setTotalAllocatedTime(BigDecimal.ZERO);
      sprint.setTotalPlannedTime(BigDecimal.ZERO);
      sprint.setTotalRemainingTime(BigDecimal.ZERO);
    }

    sprintRepo.save(sprint);
  }
}
