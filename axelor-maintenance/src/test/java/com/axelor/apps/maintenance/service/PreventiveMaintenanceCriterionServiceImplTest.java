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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.maintenance.db.EquipementMaintenance;
import com.axelor.apps.maintenance.db.MaintenanceRequest;
import com.axelor.apps.maintenance.db.repo.MaintenanceRequestRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PreventiveMaintenanceCriterionServiceImplTest {

  private AppBaseService appBaseService;
  private MaintenanceRequestRepository maintenanceRequestRepository;
  private PreventiveMaintenanceCriterionServiceImpl service;

  @BeforeEach
  void setUp() {
    appBaseService = mock(AppBaseService.class);
    maintenanceRequestRepository = mock(MaintenanceRequestRepository.class);
    service =
        new PreventiveMaintenanceCriterionServiceImpl(
            appBaseService, maintenanceRequestRepository) {
          @Override
          protected MaintenanceRequest findLastCompletedPreventiveRequest(
              EquipementMaintenance equipment) {
            return null;
          }
        };
  }

  // AC-01 — Anticipation trigger before due date.
  @Test
  void calendarCriterion_anticipationWithinHorizon_triggers() throws AxelorException {
    EquipementMaintenance equipment = new EquipementMaintenance();
    equipment.setMtnEachDay(30);
    equipment.setNextMtnDate(LocalDate.of(2026, 8, 1));
    when(appBaseService.getTodayDate(any())).thenReturn(LocalDate.of(2026, 7, 20));

    Boolean result = service.evaluateCalendarCriterion(equipment, 14);

    assertTrue(result, "Within 14-day horizon (12 days before due date), criterion must trigger");
  }

  // AC-02 — No trigger outside horizon.
  @Test
  void calendarCriterion_anticipationOutsideHorizon_doesNotTrigger() throws AxelorException {
    EquipementMaintenance equipment = new EquipementMaintenance();
    equipment.setMtnEachDay(30);
    equipment.setNextMtnDate(LocalDate.of(2026, 8, 1));
    when(appBaseService.getTodayDate(any())).thenReturn(LocalDate.of(2026, 7, 15));

    Boolean result = service.evaluateCalendarCriterion(equipment, 14);

    assertFalse(
        result, "Outside 14-day horizon (17 days before due date), criterion must not trigger");
  }

  // AC-03 — Backward compatibility (anticipationDays = 0).
  @Test
  void calendarCriterion_zeroAnticipation_dueDate_triggers() throws AxelorException {
    EquipementMaintenance equipment = new EquipementMaintenance();
    equipment.setMtnEachDay(30);
    equipment.setNextMtnDate(LocalDate.of(2026, 8, 1));
    when(appBaseService.getTodayDate(any())).thenReturn(LocalDate.of(2026, 8, 1));

    Boolean result = service.evaluateCalendarCriterion(equipment, 0);

    assertTrue(result, "On due date with anticipation 0, criterion must trigger (legacy behavior)");
  }

  @Test
  void calendarCriterion_zeroAnticipation_dayBefore_doesNotTrigger() throws AxelorException {
    EquipementMaintenance equipment = new EquipementMaintenance();
    equipment.setMtnEachDay(30);
    equipment.setNextMtnDate(LocalDate.of(2026, 8, 1));
    when(appBaseService.getTodayDate(any())).thenReturn(LocalDate.of(2026, 7, 31));

    Boolean result = service.evaluateCalendarCriterion(equipment, 0);

    assertFalse(result, "Day before due date with anticipation 0, criterion must not trigger");
  }

  // Edge — anticipation does NOT shorten the day-based history periodicity.
  @Test
  void calendarCriterion_historyPath_anticipationIgnored() throws AxelorException {
    EquipementMaintenance equipment = new EquipementMaintenance();
    equipment.setMtnEachDay(180);
    // nextMtnDate is null -> history path. lastCompleted returns null in the test override -> falls
    // to the "no history" branch which always returns true. To exercise the history branch we
    // override findLastCompletedPreventiveRequest below.
    PreventiveMaintenanceCriterionServiceImpl historyService =
        new PreventiveMaintenanceCriterionServiceImpl(
            appBaseService, maintenanceRequestRepository) {
          @Override
          protected MaintenanceRequest findLastCompletedPreventiveRequest(
              EquipementMaintenance eq) {
            MaintenanceRequest mr = new MaintenanceRequest();
            mr.setDoneOn(LocalDate.of(2026, 1, 1));
            return mr;
          }
        };
    when(appBaseService.getTodayDate(any())).thenReturn(LocalDate.of(2026, 6, 15));

    Boolean result = historyService.evaluateCalendarCriterion(equipment, 14);

    // 165 days since last completion, threshold is 180. Anticipation must NOT shorten this.
    assertFalse(
        result, "Anticipation must not apply to the day-based history path (BR-02 unchanged)");
  }

  // Negative anticipationDays must be treated as 0.
  @Test
  void calendarCriterion_negativeAnticipation_treatedAsZero() throws AxelorException {
    EquipementMaintenance equipment = new EquipementMaintenance();
    equipment.setMtnEachDay(30);
    equipment.setNextMtnDate(LocalDate.of(2026, 8, 1));
    when(appBaseService.getTodayDate(any())).thenReturn(LocalDate.of(2026, 7, 31));

    Boolean result = service.evaluateCalendarCriterion(equipment, -5);

    assertFalse(result, "Negative anticipation must be clamped to 0 (no early trigger)");
  }

  // Criterion not configured.
  @Test
  void calendarCriterion_mtnEachDayZero_returnsNull() throws AxelorException {
    EquipementMaintenance equipment = new EquipementMaintenance();
    equipment.setMtnEachDay(0);

    Boolean result = service.evaluateCalendarCriterion(equipment, 14);

    assertNull(result, "Criterion not configured (mtnEachDay <= 0) must return null");
  }

  // shouldTriggerMaintenance forwards anticipationDays to the calendar criterion.
  @Test
  void shouldTriggerMaintenance_forwardsAnticipationDaysToCalendar() throws AxelorException {
    EquipementMaintenance equipment = new EquipementMaintenance();
    equipment.setMtnEachDay(30);
    equipment.setMtnEachDuration(0);
    equipment.setCreateMtnRequestSelect(0);
    equipment.setNextMtnDate(LocalDate.of(2026, 8, 1));
    when(appBaseService.getTodayDate(any())).thenReturn(LocalDate.of(2026, 7, 20));

    boolean withAnticipation = service.shouldTriggerMaintenance(equipment, 14);
    boolean withoutAnticipation = service.shouldTriggerMaintenance(equipment, 0);

    assertTrue(
        withAnticipation, "Within 14-day horizon, shouldTriggerMaintenance must return true");
    assertFalse(
        withoutAnticipation,
        "With anticipation 0 and 12 days before due date, shouldTriggerMaintenance must return false");
    assertEquals(true, withAnticipation);
  }
}
