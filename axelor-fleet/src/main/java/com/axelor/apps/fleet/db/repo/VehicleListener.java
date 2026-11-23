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
package com.axelor.apps.fleet.db.repo;

import com.axelor.apps.fleet.db.Vehicle;
import com.axelor.apps.fleet.service.VehicleService;
import com.axelor.inject.Beans;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class VehicleListener {

  @PrePersist
  @PreUpdate
  protected void computeName(Vehicle vehicle) {
    vehicle.setName(Beans.get(VehicleService.class).setVehicleName(vehicle));
  }
}
