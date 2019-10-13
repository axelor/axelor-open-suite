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
import org.optaplanner.examples.projectjobscheduling.domain.resource.LocalResource;

@XStreamAlias("PjsProject")
public class Project extends AbstractPersistable {

  private int releaseDate;
  private int criticalPathDuration;

  private List<LocalResource> localResourceList;
  private List<Job> jobList;

  public Project() {
    this(0);
  }

  public Project(int releaseDate) {
    this.releaseDate = releaseDate;

    this.jobList = new ArrayList<Job>();
    this.localResourceList = new ArrayList<LocalResource>();
  }

  public int getReleaseDate() {
    return releaseDate;
  }

  public int getCriticalPathDuration() {
    return criticalPathDuration;
  }

  public void setCriticalPathDuration(int criticalPathDuration) {
    this.criticalPathDuration = criticalPathDuration;
  }

  public void addJob(Job job) {
    this.jobList.add(job);
  }

  public List<Job> getJobList() {
    return jobList;
  }

  // ************************************************************************
  // Complex methods
  // ************************************************************************

  public int getCriticalPathEndDate() {
    return releaseDate + criticalPathDuration;
  }

  public String getLabel() {
    return "Project " + id;
  }
}
