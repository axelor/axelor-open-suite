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
package com.axelor.apps.production.service;

import com.axelor.apps.production.db.MpsWeeklySchedule;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public interface MpsChargeService {

  public Map<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> countTotalHours(
      LocalDate startMonthDate, LocalDate endMonthDate);

  public List<Map<String, Object>> getTableDataMapList(
      Map<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> totalHoursCountMap);

  public List<Map<String, Object>> getChartDataMapList(
      Map<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> totalHoursCountMap);

  public String getReportData(Long id);
}
