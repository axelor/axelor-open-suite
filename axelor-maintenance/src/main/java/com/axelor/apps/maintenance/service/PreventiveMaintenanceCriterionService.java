package com.axelor.apps.maintenance.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.maintenance.db.EquipementMaintenance;
import com.axelor.apps.production.db.Machine;
import java.math.BigDecimal;

public interface PreventiveMaintenanceCriterionService {

  /**
   * Evaluate the calendar criterion for the given equipment.
   *
   * @param equipment the equipment to evaluate
   * @return true if the calendar criterion is met, false otherwise; null if the criterion is not
   *     configured (mtnEachDay <= 0)
   */
  Boolean evaluateCalendarCriterion(EquipementMaintenance equipment) throws AxelorException;

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
   * configured criteria participate in the evaluation.
   *
   * @param equipment the equipment to evaluate
   * @return true if maintenance should be triggered
   */
  boolean shouldTriggerMaintenance(EquipementMaintenance equipment) throws AxelorException;

  BigDecimal getMachineOperatingHours(Machine machine);
}