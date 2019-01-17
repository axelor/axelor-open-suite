/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.fleet.web;

import com.axelor.apps.fleet.db.Vehicle;
import com.axelor.apps.fleet.service.VehicleService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class VehicleController {

  @Inject private VehicleService vehicleService;

  public void setVehicleName(ActionRequest request, ActionResponse response) {
    Vehicle vehicle = request.getContext().asType(Vehicle.class);
    String actualName = vehicleService.setVehicleName(vehicle);
    response.setValue("name", actualName);
  }
}
