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
package com.axelor.apps.fleet.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.fleet.service.VehicleFuelLogService;
import com.axelor.apps.fleet.service.VehicleFuelLogServiceImpl;
import com.axelor.apps.fleet.service.VehicleService;
import com.axelor.apps.fleet.service.VehicleServiceImpl;

public class FleetModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(VehicleService.class).to(VehicleServiceImpl.class);
    bind(VehicleFuelLogService.class).to(VehicleFuelLogServiceImpl.class);
  }
}
