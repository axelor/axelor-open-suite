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
import com.axelor.apps.crm.db.TourLine;
import com.axelor.apps.crm.db.repo.TourLineRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class TourLineServiceImpl implements TourLineService {

  protected TourLineRepository tourLineRepo;
  protected AppBaseService appBaseService;

  @Inject
  public TourLineServiceImpl(TourLineRepository tourLineRepo, AppBaseService appBaseService) {
    this.tourLineRepo = tourLineRepo;
    this.appBaseService = appBaseService;
  }

  @Override
  @Transactional
  public void setValidatedAndLastVisitDate(TourLine tourLine) {
    if (Boolean.FALSE.equals(tourLine.getIsValidated())) {
      tourLine.setIsValidated(true);
      tourLine.getPartner().setLastVisitDateT(appBaseService.getTodayDateTime().toLocalDateTime());
    }
    tourLineRepo.save(tourLine);
  }
}
