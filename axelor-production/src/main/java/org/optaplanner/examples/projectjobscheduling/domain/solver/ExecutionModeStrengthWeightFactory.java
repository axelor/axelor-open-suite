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
package org.optaplanner.examples.projectjobscheduling.domain.solver;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory;
import org.optaplanner.examples.projectjobscheduling.domain.ExecutionMode;
import org.optaplanner.examples.projectjobscheduling.domain.ResourceRequirement;
import org.optaplanner.examples.projectjobscheduling.domain.Schedule;
import org.optaplanner.examples.projectjobscheduling.domain.resource.Resource;

public class ExecutionModeStrengthWeightFactory
    implements SelectionSorterWeightFactory<Schedule, ExecutionMode> {

  @Override
  public ExecutionModeStrengthWeight createSorterWeight(
      Schedule schedule, ExecutionMode executionMode) {
    Map<Resource, Integer> requirementTotalMap =
        new HashMap<>(executionMode.getResourceRequirementList().size());
    for (ResourceRequirement resourceRequirement : executionMode.getResourceRequirementList()) {
      requirementTotalMap.put(resourceRequirement.getResource(), 0);
    }
    for (ResourceRequirement resourceRequirement : schedule.getResourceRequirementList()) {
      Resource resource = resourceRequirement.getResource();
      Integer total = requirementTotalMap.get(resource);
      if (total != null) {
        total += resourceRequirement.getRequirement();
        requirementTotalMap.put(resource, total);
      }
    }
    double requirementDesirability = 0.0;
    for (ResourceRequirement resourceRequirement : executionMode.getResourceRequirementList()) {
      Resource resource = resourceRequirement.getResource();
      int total = requirementTotalMap.get(resource);
      if (total > resource.getCapacity()) {
        requirementDesirability +=
            (double) (total - resource.getCapacity())
                * (double) resourceRequirement.getRequirement()
                * (resource.isRenewable() ? 1.0 : 100.0);
      }
    }
    return new ExecutionModeStrengthWeight(executionMode, requirementDesirability);
  }

  public static class ExecutionModeStrengthWeight
      implements Comparable<ExecutionModeStrengthWeight> {

    private final ExecutionMode executionMode;
    private final double requirementDesirability;

    public ExecutionModeStrengthWeight(
        ExecutionMode executionMode, double requirementDesirability) {
      this.executionMode = executionMode;
      this.requirementDesirability = requirementDesirability;
    }

    @Override
    public int compareTo(ExecutionModeStrengthWeight other) {
      return new CompareToBuilder()
          // The less requirementsWeight, the less desirable resources are used
          .append(requirementDesirability, other.requirementDesirability)
          .append(executionMode.getId(), other.executionMode.getId())
          .toComparison();
    }
  }
}
