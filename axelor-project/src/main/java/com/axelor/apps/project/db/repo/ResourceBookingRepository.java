package com.axelor.apps.project.db.repo;

import com.axelor.apps.project.db.ResourceBooking;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;

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

    if (resourceBooking.getTask() != null
        && !Strings.isNullOrEmpty(resourceBooking.getTask().getFullName())) {
      list.add(resourceBooking.getTask().getFullName());
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
