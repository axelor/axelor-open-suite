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
import com.thoughtworks.xstream.annotations.XStreamConverter;
import java.util.ArrayList;
import java.util.List;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.score.buildin.bendable.BendableScore;
import org.optaplanner.examples.projectjobscheduling.domain.resource.Resource;
import org.optaplanner.persistence.xstream.api.score.buildin.bendable.BendableScoreXStreamConverter;

@PlanningSolution
@XStreamAlias("PjsSchedule")
public class Schedule extends AbstractPersistable {

  private List<Project> projectList;
  private List<Job> jobList;
  private List<ExecutionMode> executionModeList;
  private List<Resource> resourceList;
  private List<ResourceRequirement> resourceRequirementList;

  private List<Allocation> allocationList;

  @XStreamConverter(BendableScoreXStreamConverter.class)
  private BendableScore score;

  public Schedule() {
    this.jobList = new ArrayList<Job>();
    this.projectList = new ArrayList<Project>();
    this.resourceList = new ArrayList<Resource>();
    this.resourceRequirementList = new ArrayList<ResourceRequirement>();
    this.executionModeList = new ArrayList<ExecutionMode>();
    this.allocationList = new ArrayList<Allocation>();
  }

  @ProblemFactCollectionProperty
  public List<Project> getProjectList() {
    return projectList;
  }

  public void addProject(Project project) {
    addAbstractPersistable(project, this.projectList);
  }

  @ProblemFactCollectionProperty
  public List<Job> getJobList() {
    return jobList;
  }

  public void addJob(Job job) {
    addAbstractPersistable(job, this.jobList);
  }

  @ProblemFactCollectionProperty
  public List<ExecutionMode> getExecutionModeList() {
    return executionModeList;
  }

  public void addExecutionMode(ExecutionMode executionMode) {
    addAbstractPersistable(executionMode, this.executionModeList);
  }

  @ProblemFactCollectionProperty
  public List<Resource> getResourceList() {
    return resourceList;
  }

  public void addResource(Resource resource) {
    addAbstractPersistable(resource, this.resourceList);
  }

  @ProblemFactCollectionProperty
  public List<ResourceRequirement> getResourceRequirementList() {
    return resourceRequirementList;
  }

  public void addResourceRequirement(ResourceRequirement resourceRequirement) {
    addAbstractPersistable(resourceRequirement, this.resourceRequirementList);
  }

  @PlanningEntityCollectionProperty
  public List<Allocation> getAllocationList() {
    return allocationList;
  }

  public void addAllocation(Allocation allocation) {
    addAbstractPersistable(allocation, this.allocationList);
  }

  @PlanningScore(bendableHardLevelsSize = 1, bendableSoftLevelsSize = 2)
  public BendableScore getScore() {
    return score;
  }

  public void setScore(BendableScore score) {
    this.score = score;
  }

  // ************************************************************************
  // Complex methods
  // ************************************************************************

  private Long nextId(List<? extends AbstractPersistable> list) {
    return list.size() > 0 ? list.get(list.size() - 1).getId() + 1 : 0;
  }

  private <T extends AbstractPersistable> void addAbstractPersistable(
      T abstractPersistable, List<T> list) {
    if (abstractPersistable.getId() == null) abstractPersistable.setId(nextId(list));
    list.add(abstractPersistable);
  }
}
