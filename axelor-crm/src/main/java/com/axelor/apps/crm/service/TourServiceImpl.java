/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.service;

import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.crm.db.Tour;
import com.axelor.apps.crm.db.repo.TourRepository;
import com.axelor.db.EntityHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class TourServiceImpl implements TourService {

  protected TourRepository tourRepository;

  @Inject
  public TourServiceImpl(TourRepository tourRepository) {
    this.tourRepository = tourRepository;
  }

  @Override
  @Transactional
  public void setValidated(Tour tour) {
	 tour = tourRepository.find(tour.getId());
 if (tour.getTourLineList() != null && !tour.getTourLineList().isEmpty()) {
    tour.getTourLineList().stream()
        .map(
            line -> {
              line.setValidated(true);
              line.getPartner().setLastVisitDateT(LocalDateTime.now());
              return line;
            })
        .collect(Collectors.toList());
    tourRepository.save(EntityHelper.getEntity(tour));
    }
  }
}
