/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.project.db.repo;

import com.axelor.apps.project.db.ResourceBooking;
import com.axelor.apps.project.module.ProjectModule;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;

@Alternative
@Priority(ProjectModule.PRIORITY)
public class ResourceBookingRepository extends AbstractResourceBookingRepository {

  public String computeName(ResourceBooking resourceBooking) {
    List<String> list = new ArrayList<>();

    if (resourceBooking.getResource() != null) {
      if (resourceBooking.getResource().getResourceType() != null
          && !Strings.isNullOrEmpty(resourceBooking.getResource().getResourceType().getName())) {
        list.add(resourceBooking.getResource().getResourceType().getName());
      }
      if (!Strings.isNullOrEmpty(resourceBooking.getResource().getName())) {
        list.add(resourceBooking.getResource().getName());
      }
    }

    if (resourceBooking.getProject() != null
        && !Strings.isNullOrEmpty(resourceBooking.getProject().getFullName())) {
      list.add(resourceBooking.getProject().getFullName());
    }

    if (resourceBooking.getProjectTask() != null
        && !Strings.isNullOrEmpty(resourceBooking.getProjectTask().getFullName())) {
      list.add(resourceBooking.getProjectTask().getFullName());
    }

    return String.join(" - ", list);
  }

  @Override
  public ResourceBooking save(ResourceBooking entity) {
    if (Strings.isNullOrEmpty(entity.getName())) {
      entity.setName(computeName(entity));
    }
    return super.save(entity);
  }
}
