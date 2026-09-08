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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.maintenance.db.EquipementMaintenance;
import com.axelor.apps.production.db.Machine;
import java.math.BigDecimal;

public interface PreventiveMaintenanceCriterionService {

  /**
   * Evaluate the calendar criterion for the given equipment.
   *
   * <p>When {@code nextMtnDate} is set, the trigger fires as soon as {@code today >= nextMtnDate -
   * anticipationDays}. The {@code anticipationDays} value gives planners lead time to prepare the
   * intervention; a value of 0 reproduces the legacy behavior (trigger on the due date). The value
   * is not applied to the history-based or no-history paths.
   *
   * @param equipment the equipment to evaluate
   * @param anticipationDays number of days before {@code nextMtnDate} to start triggering; values
   *     less than 0 are treated as 0
   * @return true if the calendar criterion is met, false otherwise; null if the criterion is not
   *     configured (mtnEachDay <= 0)
   */
  Boolean evaluateCalendarCriterion(EquipementMaintenance equipment, int anticipationDays)
      throws AxelorException;

  /**
   * Evaluate the operating hours criterion for the given equipment.
   *
   * @param equipment the equipment to evaluate
   * @return true if the hours criterion is met, false otherwise; null if the criterion is not
   *     configured (mtnEachDuration <= 0 or no machine)
   */
  Boolean evaluateOperatingHoursCriterion(EquipementMaintenance equipment) throws AxelorException;

  /**
   * Evaluate combined criteria according to the equipment's trigger mode.
   *
   * <p>Uses createMtnRequestSelect: 0 = "First reached" (OR), 1 = "All reached" (AND). Only
   * configured criteria participate in the evaluation. The {@code anticipationDays} value applies
   * to the calendar criterion only.
   *
   * @param equipment the equipment to evaluate
   * @param anticipationDays anticipation horizon in days for the calendar criterion
   * @return true if maintenance should be triggered
   */
  boolean shouldTriggerMaintenance(EquipementMaintenance equipment, int anticipationDays)
      throws AxelorException;

  BigDecimal getMachineOperatingHours(Machine machine);
}
