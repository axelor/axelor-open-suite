/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.maintenance.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.manuforder.ManufOrderFinalControlServiceImpl;
import com.axelor.apps.quality.db.repo.ControlEntryRepository;
import com.axelor.apps.quality.db.repo.ControlPlanRepository;
import com.axelor.apps.quality.service.ControlEntryService;
import com.axelor.apps.quality.service.app.AppQualityService;
import jakarta.inject.Inject;

public class ManufOrderFinalControlMaintenanceServiceImpl
    extends ManufOrderFinalControlServiceImpl {

  protected final AppBaseService appBaseService;

  @Inject
  public ManufOrderFinalControlMaintenanceServiceImpl(
      ControlPlanRepository controlPlanRepository,
      ControlEntryRepository controlEntryRepository,
      ControlEntryService controlEntryService,
      AppQualityService appQualityService,
      AppBaseService appBaseService) {
    super(controlPlanRepository, controlEntryRepository, controlEntryService, appQualityService);
    this.appBaseService = appBaseService;
  }

  @Override
  protected boolean isFinalControlApplicable(ManufOrder manufOrder) {
    if (appBaseService.isApp("maintenance")
        && manufOrder.getTypeSelect() == ManufOrderRepository.TYPE_MAINTENANCE) {
      return false;
    }
    return super.isFinalControlApplicable(manufOrder);
  }
}
