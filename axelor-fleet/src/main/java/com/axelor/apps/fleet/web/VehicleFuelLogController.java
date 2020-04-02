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
package com.axelor.apps.fleet.web;

import com.axelor.apps.fleet.db.VehicleFuelLog;
import com.axelor.apps.fleet.service.VehicleFuelLogService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.math.BigDecimal;

@Singleton
public class VehicleFuelLogController {

  @Inject VehicleFuelLogService fuelService;

  public void calculateTotalPrice(ActionRequest request, ActionResponse response) {
    VehicleFuelLog vehicleFuelLog = request.getContext().asType(VehicleFuelLog.class);
    BigDecimal totalPrice = fuelService.calculateTotalPrice(vehicleFuelLog);
    response.setValue("totalPrice", totalPrice);
  }
}
