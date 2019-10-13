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
package org.optaplanner.examples.projectjobscheduling.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import java.util.List;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeFactory;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.CustomShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableReference;
import org.optaplanner.examples.projectjobscheduling.domain.solver.DelayStrengthComparator;
import org.optaplanner.examples.projectjobscheduling.domain.solver.ExecutionModeStrengthWeightFactory;
import org.optaplanner.examples.projectjobscheduling.domain.solver.NotSourceOrSinkAllocationFilter;
import org.optaplanner.examples.projectjobscheduling.domain.solver.PredecessorsDoneDateUpdatingVariableListener;

@PlanningEntity(movableEntitySelectionFilter = NotSourceOrSinkAllocationFilter.class)
@XStreamAlias("PjsAllocation")
public class Allocation extends AbstractPersistable {

  private Job job;

  private Allocation sourceAllocation;
  private Allocation sinkAllocation;
  private List<Allocation> predecessorAllocationList;
  private List<Allocation> successorAllocationList;

  // Planning variables: changes during planning, between score calculations.
  private ExecutionMode executionMode;
  private Integer delay; // In days

  // Shadow variables
  private Integer predecessorsDoneDate;

  public Allocation(
      Job job,
      List<Allocation> predecessorAllocationList,
      List<Allocation> successorAllocationList,
      Allocation sourceAllocation,
      Allocation sinkAllocation,
      Integer predecessorsDoneDate) {
    this.job = job;
    this.predecessorAllocationList = predecessorAllocationList;
    this.successorAllocationList = successorAllocationList;
    this.sourceAllocation = sourceAllocation;
    this.sinkAllocation = sinkAllocation;
    this.predecessorsDoneDate = predecessorsDoneDate;
  }

  public Allocation() {}

  public Job getJob() {
    return job;
  }

  public Allocation getSourceAllocation() {
    return sourceAllocation;
  }

  public Allocation getSinkAllocation() {
    return sinkAllocation;
  }

  public List<Allocation> getPredecessorAllocationList() {
    return predecessorAllocationList;
  }

  public List<Allocation> getSuccessorAllocationList() {
    return successorAllocationList;
  }

  @PlanningVariable(
    valueRangeProviderRefs = {"executionModeRange"},
    strengthWeightFactoryClass = ExecutionModeStrengthWeightFactory.class
  )
  public ExecutionMode getExecutionMode() {
    return executionMode;
  }

  public void setExecutionMode(ExecutionMode executionMode) {
    this.executionMode = executionMode;
  }

  @PlanningVariable(
    valueRangeProviderRefs = {"delayRange"},
    strengthComparatorClass = DelayStrengthComparator.class
  )
  public Integer getDelay() {
    return delay;
  }

  public void setDelay(Integer delay) {
    this.delay = delay;
  }

  @CustomShadowVariable(
    variableListenerClass = PredecessorsDoneDateUpdatingVariableListener.class,
    sources = {
      @PlanningVariableReference(variableName = "executionMode"),
      @PlanningVariableReference(variableName = "delay")
    }
  )
  public Integer getPredecessorsDoneDate() {
    return predecessorsDoneDate;
  }

  public void setPredecessorsDoneDate(Integer predecessorsDoneDate) {
    this.predecessorsDoneDate = predecessorsDoneDate;
  }

  // ************************************************************************
  // Complex methods
  // ************************************************************************

  public Integer getStartDate() {
    if (predecessorsDoneDate == null) {
      return null;
    }
    return predecessorsDoneDate + (delay == null ? 0 : delay);
  }

  public Integer getEndDate() {
    if (predecessorsDoneDate == null) {
      return null;
    }
    return predecessorsDoneDate
        + (delay == null ? 0 : delay)
        + (executionMode == null ? 0 : executionMode.getDuration());
  }

  public Project getProject() {
    return job.getProject();
  }

  public int getProjectCriticalPathEndDate() {
    return job.getProject().getCriticalPathEndDate();
  }

  public JobType getJobType() {
    return job.getJobType();
  }

  public String getLabel() {
    return "Job " + job.getId();
  }

  // ************************************************************************
  // Ranges
  // ************************************************************************

  @ValueRangeProvider(id = "executionModeRange")
  public List<ExecutionMode> getExecutionModeRange() {
    return job.getExecutionModeList();
  }

  @ValueRangeProvider(id = "delayRange")
  public CountableValueRange<Integer> getDelayRange() {
    return ValueRangeFactory.createIntValueRange(0, 50000);
  }
}
