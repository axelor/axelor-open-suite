/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.manufacturingoperation.planning;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.ManufacturingOperation;
import com.axelor.apps.production.db.repo.ManufacturingOperationRepository;
import com.axelor.apps.production.model.machine.MachineTimeSlot;
import com.axelor.apps.production.service.machine.MachineService;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationService;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationStockMoveService;
import com.axelor.utils.helpers.date.DurationHelper;
import com.axelor.utils.helpers.date.LocalDateTimeHelper;
import com.google.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;

public class ManufacturingOperationPlanningAsapFiniteCapacityService
    extends ManufacturingOperationPlanningCommonService {

  protected MachineService machineService;

  @Inject
  protected ManufacturingOperationPlanningAsapFiniteCapacityService(
      ManufacturingOperationService manufacturingOperationService,
      ManufacturingOperationStockMoveService manufacturingOperationStockMoveService,
      ManufacturingOperationRepository manufacturingOperationRepository,
      AppBaseService appBaseService,
      MachineService machineService) {
    super(
        manufacturingOperationService,
        manufacturingOperationStockMoveService,
        manufacturingOperationRepository,
        appBaseService);
    this.machineService = machineService;
  }

  @Override
  protected void planWithStrategy(ManufacturingOperation manufacturingOperation)
      throws AxelorException {

    Machine machine = manufacturingOperation.getMachine();
    LocalDateTime plannedStartDate = manufacturingOperation.getPlannedStartDateT();
    LocalDateTime lastOperationDate =
        manufacturingOperationService.getLastOperationDate(manufacturingOperation);
    LocalDateTime maxDate = LocalDateTimeHelper.max(plannedStartDate, lastOperationDate);
    if (machine != null) {
      MachineTimeSlot freeMachineTimeSlot =
          machineService.getClosestAvailableTimeSlotFrom(
              machine,
              maxDate,
              maxDate.plusSeconds(
                  manufacturingOperationService.getDuration(manufacturingOperation)),
              manufacturingOperation);

      manufacturingOperation.setPlannedStartDateT(freeMachineTimeSlot.getStartDateT());
      manufacturingOperation.setPlannedEndDateT(freeMachineTimeSlot.getEndDateT());

      Long plannedDuration =
          DurationHelper.getSecondsDuration(
              Duration.between(
                  manufacturingOperation.getPlannedStartDateT(),
                  manufacturingOperation.getPlannedEndDateT()));
      manufacturingOperation.setPlannedDuration(plannedDuration);
    } else {
      manufacturingOperation.setPlannedStartDateT(maxDate);
      manufacturingOperation.setPlannedEndDateT(
          manufacturingOperation
              .getPlannedStartDateT()
              .plusSeconds(manufacturingOperationService.getDuration(manufacturingOperation)));

      Long plannedDuration =
          DurationHelper.getSecondsDuration(
              Duration.between(
                  manufacturingOperation.getPlannedStartDateT(),
                  manufacturingOperation.getPlannedEndDateT()));
      manufacturingOperation.setPlannedDuration(plannedDuration);
    }

    manufacturingOperation.setRealStartDateT(null);
    manufacturingOperation.setRealEndDateT(null);
    manufacturingOperation.setRealDuration(null);
  }
}
