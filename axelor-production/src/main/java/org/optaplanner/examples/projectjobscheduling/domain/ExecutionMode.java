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

@XStreamAlias("PjsExecutionMode")
public class ExecutionMode extends AbstractPersistable {

  private Job job;
  private int duration; // In days

  private List<ResourceRequirement> resourceRequirementList;

  public ExecutionMode(Job job, int duration) {
    this.job = job;
    this.duration = duration;

    this.resourceRequirementList = new ArrayList<ResourceRequirement>();
  }

  public Job getJob() {
    return job;
  }

  public int getDuration() {
    return duration;
  }

  public List<ResourceRequirement> getResourceRequirementList() {
    return resourceRequirementList;
  }

  public void addResourceRequirement(ResourceRequirement resourceRequirement) {
    this.resourceRequirementList.add(resourceRequirement);
  }

  // ************************************************************************
  // Complex methods
  // ************************************************************************

}
