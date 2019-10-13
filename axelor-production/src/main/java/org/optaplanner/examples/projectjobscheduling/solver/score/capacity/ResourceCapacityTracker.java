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

public abstract class ResourceCapacityTracker {

  protected Resource resource;

  public ResourceCapacityTracker(Resource resource) {
    this.resource = resource;
  }

  public abstract void insert(ResourceRequirement resourceRequirement, Allocation allocation);

  public abstract void retract(ResourceRequirement resourceRequirement, Allocation allocation);

  public abstract int getHardScore();
}
