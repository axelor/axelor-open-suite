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
package org.optaplanner.examples.projectjobscheduling.solver.score.capacity;

import java.util.HashMap;
import java.util.Map;
import org.optaplanner.examples.projectjobscheduling.domain.Allocation;
import org.optaplanner.examples.projectjobscheduling.domain.ResourceRequirement;
import org.optaplanner.examples.projectjobscheduling.domain.resource.Resource;

public class RenewableResourceCapacityTracker extends ResourceCapacityTracker {

  protected int capacityEveryDay;

  protected Map<Integer, Integer> usedPerDay;
  protected int hardScore;

  public RenewableResourceCapacityTracker(Resource resource) {
    super(resource);
    if (!resource.isRenewable()) {
      throw new IllegalArgumentException(
          "The resource (" + resource + ") is expected to be renewable.");
    }
    capacityEveryDay = resource.getCapacity();
    usedPerDay = new HashMap<>();
    hardScore = 0;
  }

  @Override
  public void insert(ResourceRequirement resourceRequirement, Allocation allocation) {
    int startDate = allocation.getStartDate();
    int endDate = allocation.getEndDate();
    int requirement = resourceRequirement.getRequirement();
    for (int i = startDate; i < endDate; i++) {
      Integer used = usedPerDay.get(i);
      if (used == null) {
        used = 0;
      }
      if (used > capacityEveryDay) {
        hardScore += (used - capacityEveryDay);
      }
      used += requirement;
      if (used > capacityEveryDay) {
        hardScore -= (used - capacityEveryDay);
      }
      usedPerDay.put(i, used);
    }
  }

  @Override
  public void retract(ResourceRequirement resourceRequirement, Allocation allocation) {
    int startDate = allocation.getStartDate();
    int endDate = allocation.getEndDate();
    int requirement = resourceRequirement.getRequirement();
    for (int i = startDate; i < endDate; i++) {
      Integer used = usedPerDay.get(i);
      if (used == null) {
        used = 0;
      }
      if (used > capacityEveryDay) {
        hardScore += (used - capacityEveryDay);
      }
      used -= requirement;
      if (used > capacityEveryDay) {
        hardScore -= (used - capacityEveryDay);
      }
      usedPerDay.put(i, used);
    }
  }

  @Override
  public int getHardScore() {
    return hardScore;
  }
}
