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
package com.axelor.apps.production.web;

import com.axelor.apps.production.db.MpsWeeklySchedule;
import com.axelor.apps.production.service.MpsChargeService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MpsChargeController {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void getMpsWeeklyScheduleCustom(ActionRequest request, ActionResponse response) {

    LocalDate startMonthDate =
        LocalDate.parse(
            request.getData().get("startMonthDate").toString(), DateTimeFormatter.ISO_DATE);
    LocalDate endMonthDate =
        LocalDate.parse(
            request.getData().get("endMonthDate").toString(), DateTimeFormatter.ISO_DATE);

    MpsChargeService mpsChargeService = Beans.get(MpsChargeService.class);

    Map<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> totalHoursCountMap =
        mpsChargeService.countTotalHours(startMonthDate, endMonthDate);
    List<Map<String, Object>> dataMapList =
        mpsChargeService.getTableDataMapList(totalHoursCountMap);

    response.setData(dataMapList);
  }

  public void getMpsWeeklyScheduleChartFirstYear(ActionRequest request, ActionResponse response) {

    LocalDate startMonthDate =
        LocalDate.parse(request.getContext().get("startMonthDate").toString());
    LocalDate endMonthDate = LocalDate.parse(request.getContext().get("endMonthDate").toString());

    if (startMonthDate.getYear() != endMonthDate.getYear()) {
      endMonthDate = startMonthDate.with(TemporalAdjusters.lastDayOfYear());
    }

    MpsChargeService mpsChargeService = Beans.get(MpsChargeService.class);

    Map<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> totalHoursCountMap =
        mpsChargeService.countTotalHours(startMonthDate, endMonthDate);
    List<Map<String, Object>> dataMapList =
        mpsChargeService.getChartDataMapList(totalHoursCountMap);

    response.setData(dataMapList);
  }

  public void getMpsWeeklyScheduleChartSecondYear(ActionRequest request, ActionResponse response) {

    LocalDate startMonthDate =
        LocalDate.parse(request.getContext().get("startMonthDate").toString());
    LocalDate endMonthDate = LocalDate.parse(request.getContext().get("endMonthDate").toString());

    if (startMonthDate.getYear() == endMonthDate.getYear()) {
      return;
    }
    startMonthDate = endMonthDate.with(TemporalAdjusters.firstDayOfYear());
    MpsChargeService mpsChargeService = Beans.get(MpsChargeService.class);
    Map<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> totalHoursCountMap =
        mpsChargeService.countTotalHours(startMonthDate, endMonthDate);
    List<Map<String, Object>> dataMapList =
        mpsChargeService.getChartDataMapList(totalHoursCountMap);

    response.setData(dataMapList);
  }
}
