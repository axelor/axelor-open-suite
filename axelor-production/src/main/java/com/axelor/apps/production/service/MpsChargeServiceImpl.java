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

import com.axelor.apps.production.db.MpsCharge;
import com.axelor.apps.production.db.MpsWeeklySchedule;
import com.axelor.apps.production.db.repo.MpsChargeRepository;
import com.axelor.apps.production.db.repo.MpsWeeklyScheduleRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class MpsChargeServiceImpl implements MpsChargeService {

  final String TITLE_CODE = "code";
  final String TITLE_MONTH = "month";
  final String TITLE_MONTH_NAME = "monthName";
  final String TITLE_COUNT = "count";

  @Override
  public Map<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> countTotalHours(
      LocalDate startMonthDate, LocalDate endMonthDate) {

    List<MpsWeeklySchedule> mpsWeeklyScheduleList =
        Beans.get(MpsWeeklyScheduleRepository.class).all().order("totalHours").fetch();

    Map<YearMonth, Map<DayOfWeek, Integer>> totalWeekDaysCountForGivenMonthsMap = new HashMap<>();
    Integer startEndDateMonthDiff =
        Period.between(startMonthDate.withDayOfMonth(1), endMonthDate.withDayOfMonth(1))
            .getMonths();

    for (int i = 0; i <= startEndDateMonthDiff; i++) {
      totalWeekDaysCountForGivenMonthsMap.put(
          YearMonth.from(startMonthDate.plusMonths(i)),
          countWeeksDaysInMonth(startMonthDate.plusMonths(i)));
    }

    Map<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> totalHoursCountMap = new HashMap<>();
    for (MpsWeeklySchedule mpsWeeklySchedule : mpsWeeklyScheduleList) {
      totalHoursCountMap.put(
          mpsWeeklySchedule,
          countTotalHoursForYearMonth(mpsWeeklySchedule, totalWeekDaysCountForGivenMonthsMap));
    }

    return totalHoursCountMap;
  }

  private Map<DayOfWeek, Integer> countWeeksDaysInMonth(LocalDate date) {
    Map<DayOfWeek, Integer> dayOfWeekCountForMonthMap = new HashMap<>();

    IntStream.rangeClosed(1, YearMonth.from(date).lengthOfMonth())
        .mapToObj(dateOfDay -> LocalDate.of(date.getYear(), date.getMonth(), dateOfDay))
        .forEach(
            day ->
                dayOfWeekCountForMonthMap.put(
                    day.getDayOfWeek(),
                    dayOfWeekCountForMonthMap.getOrDefault(day.getDayOfWeek(), 0) + 1));
    return dayOfWeekCountForMonthMap;
  }

  private Map<YearMonth, BigDecimal> countTotalHoursForYearMonth(
      MpsWeeklySchedule mpsWeeklySchedule,
      Map<YearMonth, Map<DayOfWeek, Integer>> totalWeekDaysCountMap) {

    Map<YearMonth, BigDecimal> totalHoursForYearMonthCountMap = new HashMap<>();
    for (Map.Entry<YearMonth, Map<DayOfWeek, Integer>> weekDaysCount :
        totalWeekDaysCountMap.entrySet()) {
      totalHoursForYearMonthCountMap.put(
          weekDaysCount.getKey(),
          countTotalHoursForMpsWeeklySchedual(mpsWeeklySchedule, weekDaysCount.getValue()));
    }

    return totalHoursForYearMonthCountMap;
  }

  private BigDecimal countTotalHoursForMpsWeeklySchedual(
      MpsWeeklySchedule mpsWeeklySchedule, Map<DayOfWeek, Integer> weekDaysCount) {
    BigDecimal totalHours = BigDecimal.ZERO;

    if (mpsWeeklySchedule.getIsMonday()) {
      totalHours =
          totalHours.add(
              mpsWeeklySchedule
                  .getHoursMonday()
                  .multiply(new BigDecimal(weekDaysCount.get(DayOfWeek.MONDAY))));
    }
    if (mpsWeeklySchedule.getIsTuesday()) {
      totalHours =
          totalHours.add(
              mpsWeeklySchedule
                  .getHoursTuesday()
                  .multiply(new BigDecimal(weekDaysCount.get(DayOfWeek.TUESDAY))));
    }
    if (mpsWeeklySchedule.getIsWednesday()) {
      totalHours =
          totalHours.add(
              mpsWeeklySchedule
                  .getHoursWednesday()
                  .multiply(new BigDecimal(weekDaysCount.get(DayOfWeek.WEDNESDAY))));
    }
    if (mpsWeeklySchedule.getIsThursday()) {
      totalHours =
          totalHours.add(
              mpsWeeklySchedule
                  .getHoursThursday()
                  .multiply(new BigDecimal(weekDaysCount.get(DayOfWeek.THURSDAY))));
    }
    if (mpsWeeklySchedule.getIsFriday()) {
      totalHours =
          totalHours.add(
              mpsWeeklySchedule
                  .getHoursFriday()
                  .multiply(new BigDecimal(weekDaysCount.get(DayOfWeek.FRIDAY))));
    }
    if (mpsWeeklySchedule.getIsSaturday()) {
      totalHours =
          totalHours.add(
              mpsWeeklySchedule
                  .getHoursSaturday()
                  .multiply(new BigDecimal(weekDaysCount.get(DayOfWeek.SATURDAY))));
    }
    if (mpsWeeklySchedule.getIsSunday()) {
      totalHours =
          totalHours.add(
              mpsWeeklySchedule
                  .getHoursSunday()
                  .multiply(new BigDecimal(weekDaysCount.get(DayOfWeek.SUNDAY))));
    }

    return totalHours;
  }

  @Override
  public String getReportData(Long id) {
    MpsCharge mpsCharge = Beans.get(MpsChargeRepository.class).find(id);
    Map<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> totalHoursCountMap =
        countTotalHours(mpsCharge.getStartMonthDate(), mpsCharge.getEndMonthDate());
    ObjectMapper objectMapper = new ObjectMapper();

    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("data", getChartDataMapList(totalHoursCountMap));
    String dataMapJSONString = null;
    try {
      dataMapJSONString = objectMapper.writeValueAsString(dataMap);
    } catch (JsonProcessingException e) {
      TraceBackService.trace(e);
    }
    return dataMapJSONString;
  }

  @Override
  public List<Map<String, Object>> getTableDataMapList(
      Map<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> totalHoursCountMap) {
    List<Map<String, Object>> dataMapList = new ArrayList<>();
    Map<String, Object> totalRowDataMap = new HashMap<>();

    totalRowDataMap.put(TITLE_CODE, "Total");

    for (Map.Entry<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> entry :
        totalHoursCountMap.entrySet()) {

      Map<String, Object> dataMap = new HashMap<>();
      dataMap.put(TITLE_CODE, entry.getKey().getCode());

      for (Map.Entry<YearMonth, BigDecimal> yearsMonthCount : entry.getValue().entrySet()) {
        String month = yearsMonthCount.getKey().getMonth().toString().toLowerCase();
        dataMap.put(month, yearsMonthCount.getValue());
        totalRowDataMap.put(
            month,
            ((BigDecimal) totalRowDataMap.getOrDefault(month, BigDecimal.ZERO))
                .add(yearsMonthCount.getValue()));
      }
      dataMapList.add(dataMap);
    }
    dataMapList.add(totalRowDataMap);
    return dataMapList;
  }

  @Override
  public List<Map<String, Object>> getChartDataMapList(
      Map<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> totalHoursCountMap) {
    List<Map<String, Object>> dataMapList = new ArrayList<>();

    for (Map.Entry<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> totalHoursCount :
        totalHoursCountMap.entrySet()) {

      String mpsWeeklySchedualCode = totalHoursCount.getKey().getCode();

      for (Map.Entry<YearMonth, BigDecimal> yearsMonthCount :
          totalHoursCount.getValue().entrySet()) {

        YearMonth yearMonth = yearsMonthCount.getKey();
        String monthName = yearMonth.getMonth().toString().toLowerCase();

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(TITLE_CODE, mpsWeeklySchedualCode);
        dataMap.put(TITLE_MONTH, yearMonth.getMonthValue());
        dataMap.put(TITLE_MONTH_NAME, monthName);
        dataMap.put(TITLE_COUNT, yearsMonthCount.getValue());
        dataMapList.add(dataMap);
      }
    }
    return dataMapList;
  }
}
