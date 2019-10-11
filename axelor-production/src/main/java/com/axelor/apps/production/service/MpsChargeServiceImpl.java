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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class MpsChargeServiceImpl implements MpsChargeService {

  final String TITLE_CODE = "code";
  final String TITLE_MONTH = "month";
  final String TITLE_MONTH_NAME = "monthName";
  final String TITLE_COUNT = "count";
  final String TITLE_YEAR_MONTH = "yearMonth";
  final String TITLE_TOTAL_HOURS = "totalHours";

  @Override
  public Map<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> countTotalHours(
      LocalDate startMonthDate, LocalDate endMonthDate) {

    List<MpsWeeklySchedule> mpsWeeklyScheduleList =
        Beans.get(MpsWeeklyScheduleRepository.class).all().order("totalHours").fetch();
    Map<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> totalHoursCountMap = new LinkedHashMap<>();
    for (MpsWeeklySchedule mpsWeeklySchedule : mpsWeeklyScheduleList) {
      Map<YearMonth, BigDecimal> totalHoursCountYearForMpsWeeklySchedualMap =
          countTotalHoursForMpsWeeklySchedual(mpsWeeklySchedule, startMonthDate, endMonthDate);
      totalHoursCountMap.put(mpsWeeklySchedule, totalHoursCountYearForMpsWeeklySchedualMap);
    }

    return totalHoursCountMap;
  }

  private Map<YearMonth, BigDecimal> countTotalHoursForMpsWeeklySchedual(
      MpsWeeklySchedule mpsWeeklySchedule, LocalDate startMonthDate, LocalDate endMonthDate) {

    Map<YearMonth, BigDecimal> totalHoursCountYearMonthForMpsWeeklySchedualMap = new HashMap<>();

    Integer startEndMonthDateDiffInMonths =
        Period.between(startMonthDate.withDayOfMonth(1), endMonthDate.withDayOfMonth(1))
            .getMonths();
    for (int i = 0; i <= startEndMonthDateDiffInMonths; i++) {
      YearMonth yearMonth = YearMonth.from(startMonthDate.plusMonths(i));
      totalHoursCountYearMonthForMpsWeeklySchedualMap.put(
          yearMonth, countTotalHoursForGivenMonth(mpsWeeklySchedule, yearMonth));
    }

    return totalHoursCountYearMonthForMpsWeeklySchedualMap;
  }

  private BigDecimal countTotalHoursForGivenMonth(
      MpsWeeklySchedule mpsWeeklySchedule, YearMonth yearMonth) {

    Integer weekCountForLastDayOfMonth =
        LocalDate.of(yearMonth.getYear(), yearMonth.getMonthValue(), yearMonth.lengthOfMonth())
            .get(WeekFields.of(DayOfWeek.MONDAY, 1).weekOfMonth());
    BigDecimal totalHoursCountForExtraDaysInMonth =
        countTotalHoursForExtraDaysInMonth(mpsWeeklySchedule, yearMonth);
    BigDecimal totalHoursCountForCommonWeeksInMonth =
        mpsWeeklySchedule.getTotalHours().multiply(new BigDecimal(weekCountForLastDayOfMonth));
    BigDecimal totalHoursCountForGivenMonth =
        totalHoursCountForCommonWeeksInMonth.subtract(totalHoursCountForExtraDaysInMonth);

    return totalHoursCountForGivenMonth;
  }

  private BigDecimal countTotalHoursForExtraDaysInMonth(
      MpsWeeklySchedule mpsWeeklySchedule, YearMonth yearMonth) {

    BigDecimal totalHours = BigDecimal.ZERO;

    LocalDate startDayOfMonth = LocalDate.of(yearMonth.getYear(), yearMonth.getMonthValue(), 1);
    LocalDate lastMondayOfPrevMonth =
        startDayOfMonth.minusMonths(1).with(TemporalAdjusters.lastInMonth(DayOfWeek.MONDAY));
    LocalDate firstMondayOfNextMonth =
        startDayOfMonth.plusMonths(1).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));

    MpsWeeklyScheduleService mpsWeeklyScheduleService = Beans.get(MpsWeeklyScheduleService.class);

    if (!startDayOfMonth.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
      totalHours =
          IntStream.rangeClosed(
                  lastMondayOfPrevMonth.getDayOfMonth(), lastMondayOfPrevMonth.lengthOfMonth())
              .mapToObj(
                  dayOfMonth ->
                      LocalDate.of(
                          lastMondayOfPrevMonth.getYear(),
                          lastMondayOfPrevMonth.getMonthValue(),
                          dayOfMonth))
              .map(
                  day ->
                      mpsWeeklyScheduleService.getHoursForWeekDay(
                          mpsWeeklySchedule, day.getDayOfWeek()))
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);
    }
    totalHours =
        IntStream.range(1, firstMondayOfNextMonth.getDayOfMonth())
            .mapToObj(
                dayOfMonth ->
                    LocalDate.of(
                        firstMondayOfNextMonth.getYear(),
                        firstMondayOfNextMonth.getMonthValue(),
                        dayOfMonth))
            .map(
                day ->
                    mpsWeeklyScheduleService.getHoursForWeekDay(
                        mpsWeeklySchedule, day.getDayOfWeek()))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO)
            .add(totalHours);
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

    for (Map.Entry<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> entry :
        totalHoursCountMap.entrySet()) {

      Map<String, Object> dataMap = new HashMap<>();
      dataMap.put(TITLE_CODE, entry.getKey().getCode());

      for (Map.Entry<YearMonth, BigDecimal> yearsMonthCount : entry.getValue().entrySet()) {
        String month = yearsMonthCount.getKey().getMonth().toString().toLowerCase();
        dataMap.put(month, yearsMonthCount.getValue());
      }
      dataMapList.add(dataMap);
    }

    return dataMapList;
  }

  @Override
  public List<Map<String, Object>> getChartDataMapList(
      Map<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> totalHoursCountMap) {
    List<Map<String, Object>> dataMapList = new ArrayList<>();

    for (Map.Entry<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> totalHoursCount :
        totalHoursCountMap.entrySet()) {

      String mpsWeeklySchedualCode = totalHoursCount.getKey().getCode();
      BigDecimal totalHours = totalHoursCount.getKey().getTotalHours();

      for (Map.Entry<YearMonth, BigDecimal> yearsMonthCount :
          totalHoursCount.getValue().entrySet()) {

        YearMonth yearMonth = yearsMonthCount.getKey();
        String monthName = yearMonth.getMonth().toString().toLowerCase();

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(TITLE_CODE, mpsWeeklySchedualCode);
        dataMap.put(TITLE_MONTH, yearMonth.getMonthValue());
        dataMap.put(TITLE_MONTH_NAME, monthName);
        dataMap.put(
            TITLE_YEAR_MONTH,
            LocalDateTime.of(
                    LocalDate.of(yearMonth.getYear(), yearMonth.getMonthValue(), 1), LocalTime.MIN)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        dataMap.put(TITLE_COUNT, yearsMonthCount.getValue());
        dataMap.put(TITLE_TOTAL_HOURS, totalHours);
        dataMapList.add(dataMap);
      }
    }
    return dataMapList;
  }
}
