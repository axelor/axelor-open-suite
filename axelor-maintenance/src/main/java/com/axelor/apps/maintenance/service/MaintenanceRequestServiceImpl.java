/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.maintenance.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.maintenance.db.MaintenanceRequest;
import com.axelor.apps.maintenance.db.repo.MaintenanceRequestRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MaintenanceRequestServiceImpl implements MaintenanceRequestService {

  @Inject private MaintenanceRequestRepository maintenanceRequestRepo;

  @Inject private AppBaseService appBaseService;

  @Transactional
  public MaintenanceRequest start(MaintenanceRequest maintenanceRequest) {

    maintenanceRequest.setStatusSelect(MaintenanceRequestRepository.STATUS_IN_PROGRESS);

    return maintenanceRequestRepo.save(maintenanceRequest);
  }

  @Transactional
  public MaintenanceRequest complete(MaintenanceRequest maintenanceRequest) {

    maintenanceRequest.setStatusSelect(MaintenanceRequestRepository.STATUS_COMPLETED);
    maintenanceRequest.setDoneOn(appBaseService.getTodayDate());

    return maintenanceRequestRepo.save(maintenanceRequest);
  }

  @Transactional
  public MaintenanceRequest cancel(MaintenanceRequest maintenanceRequest) {

    maintenanceRequest.setStatusSelect(MaintenanceRequestRepository.STATUS_CANCELED);

    return maintenanceRequestRepo.save(maintenanceRequest);
  }

  @Transactional
  public MaintenanceRequest replan(MaintenanceRequest maintenanceRequest) {

    maintenanceRequest.setStatusSelect(MaintenanceRequestRepository.STATUS_PLANNED);

    return maintenanceRequestRepo.save(maintenanceRequest);
  }
}
