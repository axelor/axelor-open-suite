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
package com.axelor.apps.hr.service.sprint;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.project.db.AllocationLine;
import com.axelor.apps.project.db.AllocationPeriod;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.db.repo.SprintRepository;
import com.axelor.apps.project.service.sprint.SprintServiceImpl;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class SprintHRServiceImpl extends SprintServiceImpl implements SprintHRService {

  protected SprintRepository sprintRepo;
  protected AllocationLineService allocationLineHRService;

  @Inject
  public SprintHRServiceImpl(
      ProjectTaskRepository projectTaskRepo,
      SprintRepository sprintRepo,
      AllocationLineService allocationLineHRService) {

    super(projectTaskRepo);

    this.sprintRepo = sprintRepo;
    this.allocationLineHRService = allocationLineHRService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void generateAllocations(Sprint sprint) throws AxelorException {

    Set<AllocationPeriod> allocationPeriods = sprint.getAllocationPeriodSet();

    if (CollectionUtils.isEmpty(allocationPeriods)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get("No allocation periods found for the sprint"));
    }

    Set<User> projectUsers =
        Optional.ofNullable(sprint.getProject())
            .map(Project::getMembersUserSet)
            .orElse(Collections.emptySet());

    if (CollectionUtils.isEmpty(projectUsers)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD, I18n.get("No members found for the project"));
    }

    List<AllocationLine> allocationLinesToSave = new ArrayList<>();

    for (AllocationPeriod period : allocationPeriods) {

      for (User user : projectUsers) {

        try {
          AllocationLine allocationLine =
              allocationLineHRService.createAllocationLineIfNotExists(sprint, period, user);

          if (allocationLine != null) {
            allocationLinesToSave.add(allocationLine);
          }
        } catch (AxelorException e) {
          TraceBackService.trace(e);
        }
      }
    }

    if (!allocationLinesToSave.isEmpty()) {
      allocationLinesToSave.forEach(sprint::addAllocationLineListItem);
      sprintRepo.save(sprint);
    }
  }
}
