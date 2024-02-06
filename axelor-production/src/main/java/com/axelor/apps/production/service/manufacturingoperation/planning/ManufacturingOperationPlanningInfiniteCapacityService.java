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
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.production.db.ManufacturingOperation;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationService;
import com.google.inject.Inject;
import java.time.LocalDateTime;

public class ManufacturingOperationPlanningInfiniteCapacityService {

  protected ManufacturingOperationService manufacturingOperationService;
  protected WeeklyPlanningService weeklyPlanningService;

  @Inject
  public ManufacturingOperationPlanningInfiniteCapacityService(
      ManufacturingOperationService manufacturingOperationService,
      WeeklyPlanningService weeklyPlanningService) {
    this.manufacturingOperationService = manufacturingOperationService;
    this.weeklyPlanningService = weeklyPlanningService;
  }

  protected void searchForNextWorkingDay(
      ManufacturingOperation manufacturingOperation,
      WeeklyPlanning weeklyPlanning,
      LocalDateTime startDate) {
    int daysToAddNbr = 0;
    DayPlanning nextDayPlanning;
    /* We will find the next DayPlanning with at least one working period. */
    do {

      daysToAddNbr++;
      nextDayPlanning =
          weeklyPlanningService.findDayPlanning(
              weeklyPlanning, startDate.toLocalDate().plusDays(daysToAddNbr));
    } while (nextDayPlanning.getAfternoonFrom() == null
        && nextDayPlanning.getMorningFrom() == null);

    /*
     * We will add the nbr of days to retrieve the working day, and set the time to either the first
     * morning period or the first afternoon period.
     */
    if (nextDayPlanning.getMorningFrom() != null) {
      manufacturingOperation.setPlannedStartDateT(
          startDate.toLocalDate().plusDays(daysToAddNbr).atTime(nextDayPlanning.getMorningFrom()));
    } else if (nextDayPlanning.getAfternoonFrom() != null) {
      manufacturingOperation.setPlannedStartDateT(
          startDate
              .toLocalDate()
              .plusDays(daysToAddNbr)
              .atTime(nextDayPlanning.getAfternoonFrom()));
    }
  }

  public void searchForPreviousWorkingDay(
      ManufacturingOperation manufacturingOperation,
      WeeklyPlanning weeklyPlanning,
      LocalDateTime endDate) {
    int daysToSubstractNbr = 0;
    DayPlanning previousDayPlanning;
    /* change comment . We will find the previous DayPlanning with at least one working period. */
    do {

      daysToSubstractNbr++;
      previousDayPlanning =
          weeklyPlanningService.findDayPlanning(
              weeklyPlanning, endDate.toLocalDate().minusDays(daysToSubstractNbr));
    } while (previousDayPlanning.getAfternoonFrom() == null
        && previousDayPlanning.getMorningFrom() == null);

    /*
     * We will subtract the nbr of days to retrieve the working day, and set the time to either the ending of the first
     * morning period or the first afternoon period.
     */
    if (previousDayPlanning.getAfternoonTo() != null) {
      manufacturingOperation.setPlannedEndDateT(
          endDate
              .toLocalDate()
              .minusDays(daysToSubstractNbr)
              .atTime(previousDayPlanning.getAfternoonTo()));
    } else if (previousDayPlanning.getMorningTo() != null) {
      manufacturingOperation.setPlannedEndDateT(
          endDate
              .toLocalDate()
              .minusDays(daysToSubstractNbr)
              .atTime(previousDayPlanning.getMorningTo()));
    }
  }

  public LocalDateTime computePlannedEndDateT(ManufacturingOperation manufacturingOperation)
      throws AxelorException {

    if (manufacturingOperation.getWorkCenter() != null) {
      return manufacturingOperation
          .getPlannedStartDateT()
          .plusSeconds(
              (int)
                  manufacturingOperationService.computeEntireCycleDuration(
                      manufacturingOperation, manufacturingOperation.getManufOrder().getQty()));
    }

    return manufacturingOperation.getPlannedStartDateT();
  }
}
