/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.project.service.roadmap;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.Sprint;
import com.google.inject.Inject;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SprintServiceImpl implements SprintService {

  @Inject
  public SprintServiceImpl() {}

  @Override
  public void generateBacklogSprint(Project project) {
    Sprint sprint = new Sprint("Backlog - " + project.getName());
    project.setBacklogSprint(sprint);
  }

  @Override
  public boolean checkSprintOverlap(Project project) {
    List<Sprint> sprintList = project.getSprintList();
    if (CollectionUtils.isEmpty(sprintList)) {
      return false;
    }
    sprintList.sort(Comparator.comparing(Sprint::getFromDate));

    for (int i = 0; i < sprintList.size() - 1; i++) {
      Sprint currentSprint = sprintList.get(i);
      Sprint nextSprint = sprintList.get(i + 1);
      if (isOverlapping(currentSprint, nextSprint)) {
        return true;
      }
    }
    return false;
  }

  protected boolean isOverlapping(Sprint currentSprint, Sprint nextSprint) {
    return currentSprint.getFromDate().isBefore(nextSprint.getToDate())
        && currentSprint.getToDate().isAfter(nextSprint.getFromDate());
  }
}
