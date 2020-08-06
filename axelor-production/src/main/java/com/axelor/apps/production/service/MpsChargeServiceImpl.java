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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.axelor.apps.production.db.MpsCharge;
import com.axelor.apps.production.db.MpsChargeLine;
import com.axelor.apps.production.db.MpsWeeklySchedule;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.MpsChargeLineRepository;
import com.axelor.apps.production.db.repo.MpsChargeRepository;
import com.axelor.apps.production.db.repo.MpsWeeklyScheduleRepository;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.persist.Transactional;

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
  
  @Transactional
  public void createDummy(int week, MpsCharge mpsCharge) {
	  MpsChargeLine dummy = new MpsChargeLine();
	  dummy.setStartingDate(mpsCharge
		      .getStartMonthDate()
		      .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
		      .with(ChronoField.DAY_OF_WEEK, DayOfWeek.MONDAY.getValue()));
	  dummy.setEndingDate(dummy.getStartingDate().plusMonths((int)(Math.random()*10)));
	  List<SaleOrder> listSaleOrder = Beans.get(SaleOrderRepository.class).all().fetch();
	  List<WorkCenter> listWorkCenter = Beans.get(WorkCenterRepository.class).all().fetch();
	  dummy.setWorkCenter(listWorkCenter.get((int)(Math.random()*listWorkCenter.size())));
	  dummy.setSaleOrder(listSaleOrder.get((int)(Math.random()*listSaleOrder.size())));
	  dummy.setCustomerWantedDate(dummy.getEndingDate().plusDays(10));
	  dummy.setMpsCharge(mpsCharge);
	  dummy.setPriority((int)(Math.random()*4)+1);
	  Beans.get(MpsChargeLineRepository.class).save(dummy);
  }
  
  @Override
  public Map<MpsWeeklySchedule, Map<Integer, BigDecimal>> countTotalWeekHours(
      LocalDate startMonthDate, LocalDate endMonthDate) {
    List<MpsWeeklySchedule> mpsWeeklyScheduleList =
        Beans.get(MpsWeeklyScheduleRepository.class).all().order("totalHours").fetch();

    Map<MpsWeeklySchedule, Map<Integer, BigDecimal>> totalHoursWeekCountMap = new LinkedHashMap<>();
    for (MpsWeeklySchedule mpsWeeklySchedule : mpsWeeklyScheduleList) {
      Map<Integer, BigDecimal> totalHoursWeekMap = new HashMap<>();
      int startWeek = startMonthDate.getDayOfYear() / 7;
      int endWeek = endMonthDate.getDayOfYear() / 7;
      if (endWeek < startWeek) {
        for (int i = 1; i <= endWeek; i++) {
          totalHoursWeekMap.put(i, mpsWeeklySchedule.getTotalHours());
        }
        endWeek = 53;
      }
      for (int i = startWeek; i <= endWeek; i++) {
        totalHoursWeekMap.put(i, mpsWeeklySchedule.getTotalHours());
      }
      totalHoursWeekCountMap.put(mpsWeeklySchedule, totalHoursWeekMap);
    }
    return totalHoursWeekCountMap;
  }

  @Override
  public List<Map<String, Object>> getTableDataWeekMapList(
      Map<MpsWeeklySchedule, Map<Integer, BigDecimal>> totalHoursCountMap) {
    List<Map<String, Object>> dataMapList = new ArrayList<>();
    for (Map.Entry<MpsWeeklySchedule, Map<Integer, BigDecimal>> entry :
        totalHoursCountMap.entrySet()) {

      Map<String, Object> dataMap = new HashMap<>();
      dataMap.put(TITLE_CODE, entry.getKey().getCode());

      for (Map.Entry<Integer, BigDecimal> yearsWeekCount : entry.getValue().entrySet()) {
        String week = yearsWeekCount.getKey().toString().toLowerCase();
        dataMap.put(week, yearsWeekCount.getValue());
      }
      dataMapList.add(dataMap);
    }

    return dataMapList;
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
    dataMap.put("data", getChartDataMapList(totalHoursCountMap, mpsCharge));
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
      Map<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> totalHoursCountMap, MpsCharge mpsCharge) {
    List<Map<String, Object>> dataMapList = new ArrayList<>();
    List<Map<String, Object>> chargeDataMapList = new ArrayList<>();
    boolean chargeDataToSet = true;
    for (Map.Entry<MpsWeeklySchedule, Map<YearMonth, BigDecimal>> totalHoursCount :
        totalHoursCountMap.entrySet()) {

      String mpsWeeklySchedualCode = totalHoursCount.getKey().getCode();
      BigDecimal totalHours = totalHoursCount.getKey().getTotalHours();

      for (Map.Entry<YearMonth, BigDecimal> yearsMonthCount :
          totalHoursCount.getValue().entrySet()) {

        YearMonth yearMonth = yearsMonthCount.getKey();
        String monthName = yearMonth.getMonth().toString().toLowerCase();
        if (chargeDataToSet) {
          Map<String, Object> dataMap = new HashMap<>();
          dataMap.put(TITLE_CODE, "Charge estimée");
          dataMap.put(TITLE_MONTH, yearMonth.getMonthValue());
          dataMap.put(TITLE_MONTH_NAME, monthName);
          dataMap.put(
              TITLE_YEAR_MONTH,
              LocalDateTime.of(
                      LocalDate.of(yearMonth.getYear(), yearMonth.getMonthValue(), 1),
                      LocalTime.MIN)
                  .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
          dataMap.put(TITLE_COUNT, getChargeLinesDataMap(yearMonth, mpsCharge));
          chargeDataMapList.add(dataMap);
        }
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
        chargeDataMapList.add(dataMap);
      }
      chargeDataToSet = false;
    }
    return chargeDataMapList;
  }

  public List<Map<String, Object>> getChartDataMapWeekList(
      Map<MpsWeeklySchedule, Map<Integer, BigDecimal>> totalHoursCountMap, MpsCharge mpsCharge) {
    List<Map<String, Object>> dataMapList = new ArrayList<>();
    List<Map<String, Object>> chargeDataMapList = new ArrayList<>();
    boolean chargeDataToSet = true;
    for (Map.Entry<MpsWeeklySchedule, Map<Integer, BigDecimal>> totalHoursCount :
        totalHoursCountMap.entrySet()) {

      String mpsWeeklySchedualCode = totalHoursCount.getKey().getCode();
      BigDecimal totalHours = totalHoursCount.getKey().getTotalHours();

      for (Map.Entry<Integer, BigDecimal> yearsMonthCount : totalHoursCount.getValue().entrySet()) {

        Integer yearMonth = yearsMonthCount.getKey();
        String monthName = yearMonth.toString().toLowerCase();
        if (chargeDataToSet) {
          Map<String, Object> dataMap = new HashMap<>();
          dataMap.put(TITLE_CODE, "test");
          dataMap.put(TITLE_MONTH, yearMonth.toString());
          dataMap.put(TITLE_MONTH_NAME, monthName);
          dataMap.put(
              TITLE_YEAR_MONTH,
              mpsCharge
                  .getStartMonthDate()
                  .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, yearMonth)
                  .with(ChronoField.DAY_OF_WEEK, DayOfWeek.MONDAY.getValue()));
          dataMap.put(TITLE_COUNT, getChargeLinesDataMap(yearMonth, mpsCharge));
          chargeDataMapList.add(dataMap);
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(TITLE_CODE, mpsWeeklySchedualCode);
        dataMap.put(TITLE_MONTH, yearMonth.toString());
        dataMap.put(TITLE_MONTH_NAME, monthName);
        dataMap.put(TITLE_YEAR_MONTH, yearMonth);
        dataMap.put(TITLE_COUNT, yearsMonthCount.getValue());
        dataMap.put(TITLE_TOTAL_HOURS, totalHours);
        dataMapList.add(dataMap);
        chargeDataMapList.add(dataMap);
      }
      chargeDataToSet = false;
    }
    return chargeDataMapList;
  }

  public BigDecimal getChargeLinesDataMap(YearMonth month, MpsCharge mpsCharge) {
    if (mpsCharge.getWorkCenter() == null) return BigDecimal.ZERO;
    return Beans.get(MpsChargeLineRepository.class)
        .all()
        .filter(
            "self.workCenter = :workCenter AND self.mpsCharge = :mpsChargeId AND MONTH(self.startingDate) = :month")
        .bind("mpsChargeId", mpsCharge.getId())
        .bind("month", month.getMonthValue())
        .bind("workCenter", mpsCharge.getWorkCenter().getId())
        .fetchStream()
        .map(it -> new BigDecimal(it.getDuration()))
        .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
  }

  public BigDecimal getChargeLinesDataMap(Integer month, MpsCharge mpsCharge) {
    if (mpsCharge.getWorkCenter() == null) return BigDecimal.ZERO;
    return Beans.get(MpsChargeLineRepository.class)
        .all()
        .filter(
            "self.workCenter = :workCenter AND self.mpsCharge = :mpsChargeId AND (self.startingDate BETWEEN :fromDate AND :toDate )")
        .bind("mpsChargeId", mpsCharge.getId())
        .bind(
            "fromDate",
            mpsCharge
                .getStartMonthDate()
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, month)
                .with(ChronoField.DAY_OF_WEEK, DayOfWeek.MONDAY.getValue()))
        .bind(
            "toDate",
            mpsCharge
                .getStartMonthDate()
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, month)
                .with(ChronoField.DAY_OF_WEEK, DayOfWeek.SUNDAY.getValue()))
        .bind("workCenter", mpsCharge.getWorkCenter().getId())
        .fetchStream()
        .map(it -> new BigDecimal(it.getDuration()))
        .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
  }
}
