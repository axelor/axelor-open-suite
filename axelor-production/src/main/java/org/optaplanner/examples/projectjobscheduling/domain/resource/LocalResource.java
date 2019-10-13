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
package org.optaplanner.examples.projectjobscheduling.domain.resource;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.optaplanner.examples.projectjobscheduling.domain.Project;

@XStreamAlias("PjsLocalResource")
public class LocalResource extends Resource {

  private Project project;
  private boolean renewable;

  public LocalResource(int capacity, Project project) {
    this(capacity, project, true);
  }

  public LocalResource(int capacity, Project project, boolean renewable) {
    super(capacity);

    this.project = project;
    this.renewable = renewable;
  }

  public Project getProject() {
    return project;
  }

  @Override
  public boolean isRenewable() {
    return renewable;
  }

  // ************************************************************************
  // Complex methods
  // ************************************************************************

}
