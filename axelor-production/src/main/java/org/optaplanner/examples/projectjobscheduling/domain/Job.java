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
import java.util.ArrayList;
import java.util.List;

@XStreamAlias("PjsJob")
public class Job extends AbstractPersistable {

  private Project project;
  private JobType jobType;
  private List<ExecutionMode> executionModeList;

  private boolean pinned;
  private Integer pinnedDate;
  private ExecutionMode pinnedExecutionMode;

  private List<Job> successorJobList;

  public Job(Project project, List<Job> successorJobList, JobType jobType) {
    this.executionModeList = new ArrayList<ExecutionMode>();
    this.project = project;
    this.successorJobList = successorJobList;
    this.jobType = jobType;
  }

  public Project getProject() {
    return project;
  }

  public JobType getJobType() {
    return jobType;
  }

  public List<ExecutionMode> getExecutionModeList() {
    return executionModeList;
  }

  public void addExecutionMode(ExecutionMode executionMode) {
    this.executionModeList.add(executionMode);
  }

  public boolean isPinned() {
    return pinned;
  }

  public void setPinned(boolean pinned) {
    this.pinned = pinned;
  }

  public Integer getPinnedDate() {
    return pinnedDate;
  }

  public void setPinnedDate(Integer pinnedDate) {
    this.pinnedDate = pinnedDate;
  }

  public ExecutionMode getPinnedExecutionMode() {
    return pinnedExecutionMode;
  }

  public void setPinnedExecutionMode(ExecutionMode pinnedExecutionMode) {
    this.pinnedExecutionMode = pinnedExecutionMode;
  }

  public List<Job> getSuccessorJobList() {
    return successorJobList;
  }

  // ************************************************************************
  // Complex methods
  // ************************************************************************

}
