/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.project.service;

import com.axelor.apps.project.db.ResourceBooking;
import com.axelor.apps.project.db.repo.ResourceBookingRepository;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ResourceBookingServiceImpl implements ResourceBookingService {

  @Inject ResourceBookingRepository resourceBookingRepo;

  @Override
  public boolean checkIfResourceBooked(ResourceBooking resourceBooking) {
    List<Object> params = new ArrayList<>();

    String query =
        "self.resource.id = ?1 AND self.fromDate != null AND self.toDate != null AND ((?2 BETWEEN self.fromDate AND self.toDate OR ?3 BETWEEN self.fromDate AND self.toDate) OR (self.fromDate BETWEEN ?2 AND ?3 OR self.toDate BETWEEN ?2 AND ?3))";
    params.add(resourceBooking.getResource().getId());
    params.add(resourceBooking.getFromDate());
    params.add(resourceBooking.getToDate());

    if (resourceBooking.getId() != null) {
      query += " AND self.id != ?4";
      params.add(resourceBooking.getId());
    }

    return resourceBookingRepo.all().filter(query, params.toArray()).count() > 0;
  }
}
