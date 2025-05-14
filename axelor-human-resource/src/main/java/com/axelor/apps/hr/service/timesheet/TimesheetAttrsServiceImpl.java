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
package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class TimesheetAttrsServiceImpl implements TimesheetAttrsService {

  protected TimesheetService timesheetService;
  protected TimesheetPeriodComputationService timesheetPeriodComputationService;
  protected TimesheetLineService timesheetLineService;

  @Inject
  public TimesheetAttrsServiceImpl(
      TimesheetService timesheetService,
      TimesheetPeriodComputationService timesheetPeriodComputationService,
      TimesheetLineService timesheetLineService) {

    this.timesheetService = timesheetService;
    this.timesheetPeriodComputationService = timesheetPeriodComputationService;
    this.timesheetLineService = timesheetLineService;
  }

  @Override
  public Map<String, Map<String, Object>> getPeriodTotalsAttrsMap(Timesheet timesheet)
      throws AxelorException {

    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    BigDecimal periodTotal = timesheetPeriodComputationService.computePeriodTotal(timesheet);
    BigDecimal periodTotalConvert =
        timesheetLineService.computeHoursDuration(timesheet, periodTotal, false);
    String periodTotalConvertTitle = timesheetService.getPeriodTotalConvertTitle(timesheet);

    setAttr("$periodTotalConvert", "hidden", false, attrsMap);
    setAttr("$periodTotalConvert", "value", periodTotalConvert, attrsMap);
    setAttr("$periodTotalConvert", "title", periodTotalConvertTitle, attrsMap);

    Employee employee = timesheet.getEmployee();
    LocalDate fromDate = timesheet.getFromDate();
    LocalDate toDate = timesheet.getToDate();
    String timeUnit = timesheet.getTimeLoggingPreferenceSelect();

    if (employee != null && fromDate != null && toDate != null && !StringUtils.isEmpty(timeUnit)) {
      BigDecimal periodTotalLeavesAndHolidays =
          timesheetService.computePeriodTotalLeavesAndHolidays(
              employee, fromDate, toDate, timeUnit);
      BigDecimal dueTimeEntries =
          timesheetService
              .computePeriodTotalWorkDurtion(employee, fromDate, toDate, timeUnit)
              .subtract(periodTotalConvert);

      setAttr("$periodTotalLeavesAndHolidays", "hidden", false, attrsMap);
      setAttr("$periodTotalLeavesAndHolidays", "value", periodTotalLeavesAndHolidays, attrsMap);
      setAttr(
          "$periodTotalLeavesAndHolidays",
          "title",
          I18n.get("Leaves and public holidays") + " (" + periodTotalConvertTitle + ")",
          attrsMap);

      setAttr("$periodTotalDueTimeEntries", "hidden", false, attrsMap);
      setAttr("$periodTotalDueTimeEntries", "value", dueTimeEntries, attrsMap);
      setAttr(
          "$periodTotalDueTimeEntries",
          "title",
          I18n.get("Due time entries") + " (" + periodTotalConvertTitle + ")",
          attrsMap);
    }

    return attrsMap;
  }

  protected void setAttr(
      String field, String attr, Object value, Map<String, Map<String, Object>> attrsMap) {

    if (!attrsMap.containsKey(field)) {
      attrsMap.put(field, new HashMap<>());
    }

    attrsMap.get(field).put(attr, value);
  }
}
