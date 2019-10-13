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
package org.optaplanner.examples.projectjobscheduling.solver.score;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.core.impl.score.director.incremental.AbstractIncrementalScoreCalculator;
import org.optaplanner.examples.projectjobscheduling.domain.Allocation;
import org.optaplanner.examples.projectjobscheduling.domain.ExecutionMode;
import org.optaplanner.examples.projectjobscheduling.domain.JobType;
import org.optaplanner.examples.projectjobscheduling.domain.Project;
import org.optaplanner.examples.projectjobscheduling.domain.ResourceRequirement;
import org.optaplanner.examples.projectjobscheduling.domain.Schedule;
import org.optaplanner.examples.projectjobscheduling.domain.resource.Resource;
import org.optaplanner.examples.projectjobscheduling.solver.score.capacity.NonrenewableResourceCapacityTracker;
import org.optaplanner.examples.projectjobscheduling.solver.score.capacity.RenewableResourceCapacityTracker;
import org.optaplanner.examples.projectjobscheduling.solver.score.capacity.ResourceCapacityTracker;

public class ProjectJobSchedulingIncrementalScoreCalculator
    extends AbstractIncrementalScoreCalculator<Schedule> {

  private Map<Resource, ResourceCapacityTracker> resourceCapacityTrackerMap;
  private Map<Project, Integer> projectEndDateMap;
  private int maximumProjectEndDate;

  private int hard0Score;
  private int hard1Score;
  private int soft0Score;
  private int soft1Score;

  @Override
  public void resetWorkingSolution(Schedule schedule) {
    List<Resource> resourceList = schedule.getResourceList();
    resourceCapacityTrackerMap = new HashMap<>(resourceList.size());
    for (Resource resource : resourceList) {
      resourceCapacityTrackerMap.put(
          resource,
          resource.isRenewable()
              ? new RenewableResourceCapacityTracker(resource)
              : new NonrenewableResourceCapacityTracker(resource));
    }
    List<Project> projectList = schedule.getProjectList();
    projectEndDateMap = new HashMap<>(projectList.size());
    maximumProjectEndDate = 0;
    hard0Score = 0;
    hard1Score = 0;
    soft0Score = 0;
    soft1Score = 0;
    int minimumReleaseDate = Integer.MAX_VALUE;
    for (Project p : projectList) {
      minimumReleaseDate = Math.min(p.getReleaseDate(), minimumReleaseDate);
    }
    soft1Score += minimumReleaseDate;
    for (Allocation allocation : schedule.getAllocationList()) {
      insert(allocation);
    }
  }

  @Override
  public void beforeEntityAdded(Object entity) {
    // Do nothing
  }

  @Override
  public void afterEntityAdded(Object entity) {
    insert((Allocation) entity);
  }

  @Override
  public void beforeVariableChanged(Object entity, String variableName) {
    retract((Allocation) entity);
  }

  @Override
  public void afterVariableChanged(Object entity, String variableName) {
    insert((Allocation) entity);
  }

  @Override
  public void beforeEntityRemoved(Object entity) {
    retract((Allocation) entity);
  }

  @Override
  public void afterEntityRemoved(Object entity) {
    // Do nothing
  }

  private void insert(Allocation allocation) {
    // Job precedence is built-in
    // Resource capacity
    ExecutionMode executionMode = allocation.getExecutionMode();
    if (executionMode != null && allocation.getJob().getJobType() == JobType.STANDARD) {
      for (ResourceRequirement resourceRequirement : executionMode.getResourceRequirementList()) {
        ResourceCapacityTracker tracker =
            resourceCapacityTrackerMap.get(resourceRequirement.getResource());
        hard0Score -= tracker.getHardScore();
        tracker.insert(resourceRequirement, allocation);
        hard0Score += tracker.getHardScore();
      }
    }
    // Pinned Jobs
    if (allocation.getJob().isPinned()) {
      Integer diff = -Math.abs(allocation.getJob().getPinnedDate() - allocation.getStartDate());
      hard1Score += diff;
    }
    // Total project delay and total make span
    if (allocation.getJob().getJobType() == JobType.SINK) {
      Integer endDate = allocation.getEndDate();
      if (endDate != null) {
        Project project = allocation.getProject();
        projectEndDateMap.put(project, endDate);
        // Total project delay
        soft0Score -= endDate - project.getCriticalPathEndDate();
        // Total make span
        if (endDate > maximumProjectEndDate) {
          soft1Score -= endDate - maximumProjectEndDate;
          maximumProjectEndDate = endDate;
        }
      }
    }
  }

  private void retract(Allocation allocation) {
    // Job precedence is built-in
    // Resource capacity
    ExecutionMode executionMode = allocation.getExecutionMode();
    if (executionMode != null && allocation.getJob().getJobType() == JobType.STANDARD) {
      for (ResourceRequirement resourceRequirement : executionMode.getResourceRequirementList()) {
        ResourceCapacityTracker tracker =
            resourceCapacityTrackerMap.get(resourceRequirement.getResource());
        hard0Score -= tracker.getHardScore();
        tracker.retract(resourceRequirement, allocation);
        hard0Score += tracker.getHardScore();
      }
    }
    // Pinned Jobs
    if (allocation.getJob().isPinned()) {
      Integer diff = Math.abs(allocation.getJob().getPinnedDate() - allocation.getStartDate());
      hard1Score += diff;
    }
    // Total project delay and total make span
    if (allocation.getJob().getJobType() == JobType.SINK) {
      Integer endDate = allocation.getEndDate();
      if (endDate != null) {
        Project project = allocation.getProject();
        projectEndDateMap.remove(project);
        // Total project delay
        soft0Score += endDate - project.getCriticalPathEndDate();
        // Total make span
        if (endDate == maximumProjectEndDate) {
          updateMaximumProjectEndDate();
          soft1Score += endDate - maximumProjectEndDate;
        }
      }
    }
  }

  private void updateMaximumProjectEndDate() {
    int maximum = 0;
    for (Integer endDate : projectEndDateMap.values()) {
      if (endDate > maximum) {
        maximum = endDate;
      }
    }
    maximumProjectEndDate = maximum;
  }

  @Override
  public Score calculateScore() {
    return BendableScore.valueOf(
        new int[] {hard0Score, hard1Score}, new int[] {soft0Score, soft1Score});
  }
}
