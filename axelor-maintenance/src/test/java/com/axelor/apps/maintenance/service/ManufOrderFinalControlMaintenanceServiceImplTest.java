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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.manuforder.ManufOrderFinalControlResult.Status;
import com.axelor.apps.production.service.manuforder.ManufOrderFinalControlService;
import com.axelor.apps.quality.db.repo.ControlEntryRepository;
import com.axelor.apps.quality.db.repo.ControlPlanRepository;
import com.axelor.apps.quality.service.ControlEntryService;
import com.axelor.apps.quality.service.app.AppQualityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ManufOrderFinalControlMaintenanceServiceImplTest {

  private AppQualityService appQualityService;
  private AppBaseService appBaseService;
  private ManufOrderFinalControlService service;
  private ManufOrder manufOrder;

  @BeforeEach
  void setUp() {
    appQualityService = mock(AppQualityService.class);
    appBaseService = mock(AppBaseService.class);
    service =
        new ManufOrderFinalControlMaintenanceServiceImpl(
            mock(ControlPlanRepository.class),
            mock(ControlEntryRepository.class),
            mock(ControlEntryService.class),
            appQualityService,
            appBaseService);
    when(appBaseService.isApp("maintenance")).thenReturn(true);
    manufOrder = new ManufOrder();
  }

  @Test
  void maintenanceManufOrderNeedsNoFinalControl() {
    manufOrder.setTypeSelect(ManufOrderRepository.TYPE_MAINTENANCE);

    assertEquals(Status.NOT_REQUIRED, service.evaluate(manufOrder).getStatus());
    verify(appQualityService, never()).isApp("quality");
  }

  @Test
  void productionManufOrderIsEvaluatedAsUsual() {
    manufOrder.setTypeSelect(ManufOrderRepository.TYPE_PRODUCTION);

    service.evaluate(manufOrder);

    verify(appQualityService).isApp("quality");
  }

  @Test
  void maintenanceAppOffKeepsTheProductionBehaviour() {
    when(appBaseService.isApp("maintenance")).thenReturn(false);
    manufOrder.setTypeSelect(ManufOrderRepository.TYPE_MAINTENANCE);

    service.evaluate(manufOrder);

    verify(appQualityService).isApp("quality");
  }
}
