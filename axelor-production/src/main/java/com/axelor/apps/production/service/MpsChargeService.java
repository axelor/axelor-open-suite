/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import com.axelor.apps.production.db.MpsCharge;
import com.axelor.apps.production.db.MpsWeeklySchedule;
import com.axelor.apps.production.db.WorkCenter;

public interface MpsChargeService {

  public Map<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> countTotalHours(
      LocalDate startMonthDate, LocalDate endMonthDate, MpsCharge mpsCharge);

  public Map<MpsWeeklySchedule, Map<Integer, BigDecimal>> countTotalWeekHours(
      LocalDate startMonthDate, LocalDate endMonthDate, MpsCharge mpsCharge);

  public List<Map<String, Object>> getTableDataMapList(
      Map<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> totalHoursCountMap);

  public List<Map<String, Object>> getTableDataWeekMapList(
      Map<MpsWeeklySchedule, Map<Integer, BigDecimal>> totalHoursCountMap);

  public List<Map<String, Object>> getChartDataMapWeekList(
      Map<MpsWeeklySchedule, Map<Integer, BigDecimal>> totalHoursCountMap, MpsCharge mpsCharge);

  public List<Map<String, Object>> getChartDataMapList(
      Map<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> totalHoursCountMap, MpsCharge mpsCharge);

  public String getReportData(Long id);
  
  public List<Long> computeWorkCenterDomain(MpsCharge mpsCharge);
}
