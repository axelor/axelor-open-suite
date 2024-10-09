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
package com.axelor.apps.project.service.sprint;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.Sprint;
import com.axelor.apps.project.db.SprintAllocationLine;
import com.axelor.apps.project.db.repo.SprintAllocationLineRepository;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class SprintAllocationLineServiceImpl implements SprintAllocationLineService {

  protected SprintAllocationLineRepository sprintAllocationLineRepo;

  @Inject
  public SprintAllocationLineServiceImpl(SprintAllocationLineRepository sprintAllocationLineRepo) {

    this.sprintAllocationLineRepo = sprintAllocationLineRepo;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void sprintOnChange(Project project, Sprint sprint) {

    Set<User> membersUserSet = project.getMembersUserSet();

    if (CollectionUtils.isNotEmpty(membersUserSet)) {
      List<SprintAllocationLine> sprintAllocationLineList = sprint.getSprintAllocationLineList();

      for (User member : membersUserSet) {
        SprintAllocationLine sprintAllocationLine =
            sprintAllocationLineList.stream()
                .filter(line -> line.getUser().equals(member))
                .findFirst()
                .orElse(null);

        if (sprintAllocationLine == null) {
          sprintAllocationLine = new SprintAllocationLine();
          sprintAllocationLine.setSprint(sprint);
          sprintAllocationLine.setUser(member);
          sprintAllocationLineRepo.save(sprintAllocationLine);
        }
      }
    }
  }

  @Override
  public HashMap<String, BigDecimal> computeSprintAllocationLine(
      SprintAllocationLine sprintAllocationLine) {

    HashMap<String, BigDecimal> valueMap = new HashMap<>();

    valueMap.put("leaves", BigDecimal.ZERO);
    valueMap.put("plannedTime", BigDecimal.ZERO);
    valueMap.put("remainingTime", BigDecimal.ZERO);

    return valueMap;
  }
}
