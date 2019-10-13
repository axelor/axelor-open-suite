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
package org.optaplanner.examples.projectjobscheduling.solver.score.drools;

import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.optaplanner.examples.projectjobscheduling.domain.resource.Resource;

public class RenewableResourceUsedDay implements Serializable {

  private final Resource resource;
  private final int usedDay;

  public RenewableResourceUsedDay(Resource resource, int usedDay) {
    this.resource = resource;
    this.usedDay = usedDay;
  }

  public Resource getResource() {
    return resource;
  }

  public int getUsedDay() {
    return usedDay;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o instanceof RenewableResourceUsedDay) {
      RenewableResourceUsedDay other = (RenewableResourceUsedDay) o;
      return new EqualsBuilder()
          .append(resource, other.resource)
          .append(usedDay, other.usedDay)
          .isEquals();
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(resource).append(usedDay).toHashCode();
  }

  @Override
  public String toString() {
    return resource + " on " + usedDay;
  }

  public int getResourceCapacity() {
    return resource.getCapacity();
  }
}
