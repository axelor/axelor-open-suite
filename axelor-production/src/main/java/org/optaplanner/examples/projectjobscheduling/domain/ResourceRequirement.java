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
import org.optaplanner.examples.projectjobscheduling.domain.resource.Resource;

@XStreamAlias("PjsResourceRequirement")
public class ResourceRequirement extends AbstractPersistable {

  private ExecutionMode executionMode;
  private Resource resource;
  private int requirement;

  public ResourceRequirement(ExecutionMode executionMode, Resource resource, int requirement) {
    this.executionMode = executionMode;
    this.resource = resource;
    this.requirement = requirement;
  }

  public ExecutionMode getExecutionMode() {
    return executionMode;
  }

  public Resource getResource() {
    return resource;
  }

  public int getRequirement() {
    return requirement;
  }

  // ************************************************************************
  // Complex methods
  // ************************************************************************

  public boolean isResourceRenewable() {
    return resource.isRenewable();
  }
}
