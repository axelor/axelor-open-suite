/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
