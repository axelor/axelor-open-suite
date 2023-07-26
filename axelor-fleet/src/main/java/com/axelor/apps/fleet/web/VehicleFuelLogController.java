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
package com.axelor.apps.fleet.web;

import com.axelor.apps.fleet.db.VehicleFuelLog;
import com.axelor.apps.fleet.service.VehicleFuelLogService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.math.BigDecimal;

@Singleton
public class VehicleFuelLogController {

  public void calculateTotalPrice(ActionRequest request, ActionResponse response) {
    VehicleFuelLog vehicleFuelLog = request.getContext().asType(VehicleFuelLog.class);
    BigDecimal totalPrice =
        Beans.get(VehicleFuelLogService.class).calculateTotalPrice(vehicleFuelLog);
    response.setValue("totalPrice", totalPrice);
  }
}
