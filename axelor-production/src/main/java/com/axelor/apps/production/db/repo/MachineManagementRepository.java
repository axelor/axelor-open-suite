/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.db.repo;

import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.MachineTool;
import com.axelor.apps.production.service.app.AppProductionService;
import com.google.inject.Inject;

public class MachineManagementRepository extends MachineRepository {

  protected MachineToolRepository machineToolRepository;
  protected AppProductionService appProductionService;

  @Inject
  public MachineManagementRepository(
      MachineToolRepository machineToolRepository, AppProductionService appProductionService) {
    this.machineToolRepository = machineToolRepository;
    this.appProductionService = appProductionService;
  }

  @Override
  public Machine save(Machine entity) {
    if (appProductionService.getAppProduction().getEnableToolManagement()
        && entity.getMachineToolLineList() != null) {
      for (MachineTool machineTool : entity.getMachineToolLineList()) {
        machineTool.setMachine(entity);
        machineToolRepository.save(machineTool);
      }
    }
    return super.save(entity);
  }
}
