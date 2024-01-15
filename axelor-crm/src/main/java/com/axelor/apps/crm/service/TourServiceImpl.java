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

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.crm.db.Tour;
import com.axelor.apps.crm.db.TourLine;
import com.axelor.apps.crm.db.repo.TourRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class TourServiceImpl implements TourService {

  protected TourRepository tourRepo;
  protected AppBaseService appBaseService;
  protected TourLineService tourLineService;

  @Inject
  public TourServiceImpl(
      TourRepository tourRepo, AppBaseService appBaseService, TourLineService tourLineService) {
    this.tourRepo = tourRepo;
    this.appBaseService = appBaseService;
    this.tourLineService = tourLineService;
  }

  @Override
  @Transactional
  public void setValidated(Tour tour) {
    if (ObjectUtils.notEmpty(tour.getTourLineList())) {
      for (TourLine tourLine : tour.getTourLineList()) {
        tourLineService.setValidatedAndLastVisitDate(tourLine);
      }
    }
    tourRepo.save(tour);
  }
}
