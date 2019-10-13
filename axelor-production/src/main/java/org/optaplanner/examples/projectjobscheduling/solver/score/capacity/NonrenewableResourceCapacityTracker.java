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

import org.optaplanner.examples.projectjobscheduling.domain.Allocation;
import org.optaplanner.examples.projectjobscheduling.domain.ResourceRequirement;
import org.optaplanner.examples.projectjobscheduling.domain.resource.Resource;

public class NonrenewableResourceCapacityTracker extends ResourceCapacityTracker {

  protected int capacity;
  protected int used;

  public NonrenewableResourceCapacityTracker(Resource resource) {
    super(resource);
    if (resource.isRenewable()) {
      throw new IllegalArgumentException(
          "The resource (" + resource + ") is expected to be nonrenewable.");
    }
    capacity = resource.getCapacity();
    used = 0;
  }

  @Override
  public void insert(ResourceRequirement resourceRequirement, Allocation allocation) {
    used += resourceRequirement.getRequirement();
  }

  @Override
  public void retract(ResourceRequirement resourceRequirement, Allocation allocation) {
    used -= resourceRequirement.getRequirement();
  }

  @Override
  public int getHardScore() {
    if (capacity >= used) {
      return 0;
    }
    return capacity - used;
  }
}
