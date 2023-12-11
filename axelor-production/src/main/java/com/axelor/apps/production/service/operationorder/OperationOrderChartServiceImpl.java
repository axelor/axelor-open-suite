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
package com.axelor.apps.production.service.operationorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.i18n.L10n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class OperationOrderChartServiceImpl implements OperationOrderChartService {

  protected static final int MAX_DAYS = 20;
  protected static final String DATE_TIME = "dateTime";
  protected static final String MACHINE = "machine";
  protected static final String COMPANY = "company";
  protected static final String CHARGE = "charge";

  protected WeeklyPlanningService weeklyPlanningService;
  protected OperationOrderRepository operationOrderRepository;

  @Inject
  public OperationOrderChartServiceImpl(
      OperationOrderRepository operationOrderRepository,
      WeeklyPlanningService weeklyPlanningService) {
    this.operationOrderRepository = operationOrderRepository;
    this.weeklyPlanningService = weeklyPlanningService;
  }

  public List<Map<String, Object>> chargeByMachineHours(
      LocalDateTime fromDateTime, LocalDateTime toDateTime) throws AxelorException {
    List<Map<String, Object>> dataList = new ArrayList<>();

    LocalDateTime itDateTime = fromDateTime;
    if (Duration.between(fromDateTime, toDateTime).toDays() > MAX_DAYS) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.CHARGE_MACHINE_DAYS));
    }

    List<OperationOrder> operationOrderListTemp =
        getOperationOrdersInTimeRange(fromDateTime, toDateTime);

    Set<Machine> machineNameList =
        operationOrderListTemp.stream()
            .map(OperationOrder::getWorkCenter)
            .filter(Objects::nonNull)
            .map(WorkCenter::getMachine)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    while (!itDateTime.isAfter(toDateTime)) {
      Map<Machine, BigDecimal> chargeMap = getMachineChargeMap(itDateTime);
      Set<Machine> keyList = chargeMap.keySet();
      String dateTime = L10n.getInstance().format(itDateTime);
      for (Machine key : machineNameList) {
        BigDecimal charge = chargeMap.getOrDefault(key, BigDecimal.ZERO);
        if (keyList.contains(key)) {
          Map<String, Object> dataMap = new HashMap<>();
          dataMap.put(DATE_TIME, dateTime);
          dataMap.put(CHARGE, charge);
          dataMap.put(MACHINE, key.getName());
          dataList.add(dataMap);
        }
      }
      itDateTime = itDateTime.plusHours(1);
    }
    return dataList;
  }

  public List<Map<String, Object>> calculateHourlyMachineCharge(
      LocalDateTime fromDateTime, LocalDateTime toDateTime, Machine machineInUse)
      throws AxelorException {
    List<Map<String, Object>> dataList = new ArrayList<>();
    LocalDateTime itDateTime = fromDateTime;

    if (Duration.between(fromDateTime, toDateTime).toDays() > MAX_DAYS) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.CHARGE_MACHINE_DAYS));
    }

    List<OperationOrder> operationOrderListTemp =
        getOperationOrdersInTimeRange(fromDateTime, toDateTime);

    Map<Company, Integer> companyMachineMap =
        operationOrderListTemp.stream()
            .filter(op -> Objects.nonNull(op.getWorkCenter()))
            .map(op -> op.getWorkCenter().getMachine())
            .filter(
                machine ->
                    machine != null
                        && machine.equals(machineInUse)
                        && machine.getStockLocation() != null
                        && machine.getStockLocation().getCompany() != null)
            .collect(
                Collectors.groupingBy(
                    machine -> machine.getStockLocation().getCompany(),
                    Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

    while (!itDateTime.isAfter(toDateTime)) {
      Map<Company, BigDecimal> chargeMap = getCompanyChargeMap(itDateTime, machineInUse);

      adjustChargePerHour(chargeMap, companyMachineMap);
      Set<Company> keyList = chargeMap.keySet();
      String dateTime = L10n.getInstance().format(itDateTime);
      for (Company key : companyMachineMap.keySet()) {
        BigDecimal charge = chargeMap.getOrDefault(key, BigDecimal.ZERO);
        if (keyList.contains(key)) {
          Map<String, Object> dataMap = new HashMap<>();
          dataMap.put(DATE_TIME, dateTime);
          dataMap.put(CHARGE, charge);
          dataMap.put(COMPANY, key.getName());
          dataList.add(dataMap);
        }
      }

      itDateTime = itDateTime.plusHours(1);
    }
    return dataList;
  }

  protected Map<Company, BigDecimal> getCompanyChargeMap(
      LocalDateTime itDateTime, Machine machineInUse) {

    Map<Company, BigDecimal> companyChargeMap = new HashMap<>();
    List<OperationOrder> operationOrderList =
        getOperationOrdersInTimeRange(itDateTime, itDateTime.plusHours(1));

    for (OperationOrder operationOrder : operationOrderList) {
      Machine machine =
          operationOrder.getWorkCenter() != null
              ? operationOrder.getWorkCenter().getMachine()
              : null;

      if (machine != null
          && machine.getStockLocation() != null
          && machine.getId().equals(machineInUse.getId())) {
        Company company = machine.getStockLocation().getCompany();

        long numberOfMinutes = calculateNumberOfMinutesPerHour(operationOrder, itDateTime);
        BigDecimal percentage = calculateMachineChargePercentagePerHour(numberOfMinutes);

        companyChargeMap.put(
            company, companyChargeMap.getOrDefault(company, BigDecimal.ZERO).add(percentage));
      }
    }

    return companyChargeMap;
  }

  protected Map<Machine, BigDecimal> getMachineChargeMap(LocalDateTime itDateTime) {

    Map<Machine, BigDecimal> machineChargeMap = new HashMap<>();
    List<OperationOrder> operationOrderList =
        getOperationOrdersInTimeRange(itDateTime, itDateTime.plusHours(1));

    for (OperationOrder operationOrder : operationOrderList) {
      if (operationOrder.getWorkCenter() != null
          && operationOrder.getWorkCenter().getMachine() != null) {
        Machine machine = operationOrder.getWorkCenter().getMachine();

        long numberOfMinutes = calculateNumberOfMinutesPerHour(operationOrder, itDateTime);
        BigDecimal percentage = calculateMachineChargePercentagePerHour(numberOfMinutes);

        machineChargeMap.put(
            machine, machineChargeMap.getOrDefault(machine, BigDecimal.ZERO).add(percentage));
      }
    }

    return machineChargeMap;
  }

  protected long calculateNumberOfMinutesPerHour(
      OperationOrder operationOrder, LocalDateTime itDateTime) {
    LocalDateTime start = operationOrder.getPlannedStartDateT();
    LocalDateTime end = operationOrder.getPlannedEndDateT();

    if (start.isBefore(itDateTime)) {
      start = itDateTime;
    }

    if (end.isAfter(itDateTime.plusHours(1))) {
      end = itDateTime.plusHours(1);
    }

    long numberOfMinutes = Duration.between(start, end).toMinutes();
    return Math.min(numberOfMinutes, 60);
  }

  protected BigDecimal calculateMachineChargePercentagePerHour(long numberOfMinutes) {
    return new BigDecimal(numberOfMinutes)
        .multiply(new BigDecimal(100))
        .divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
  }

  protected void adjustChargePerHour(
      Map<Company, BigDecimal> chargeMap, Map<Company, Integer> companyMachineMap) {
    for (Map.Entry<Company, BigDecimal> entry : chargeMap.entrySet()) {
      Company company = entry.getKey();
      BigDecimal charge = entry.getValue();
      Integer divisor = companyMachineMap.getOrDefault(company, 1);
      chargeMap.put(company, charge.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP));
    }
  }

  @Override
  public List<Map<String, Object>> chargeByMachineDays(
      LocalDateTime fromDateTime, LocalDateTime toDateTime) throws AxelorException {

    fromDateTime = fromDateTime.withHour(0).withMinute(0);
    toDateTime = toDateTime.withHour(23).withMinute(59);

    if (Duration.between(fromDateTime, toDateTime).toDays() > 500) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.CHARGE_MACHINE_DAYS));
    }

    List<OperationOrder> operationOrderListTemp =
        getOperationOrdersInTimeRange(fromDateTime, toDateTime);

    Set<Machine> machineNameList = new HashSet<>();
    for (OperationOrder operationOrder : operationOrderListTemp) {
      if (operationOrder.getWorkCenter() != null
          && operationOrder.getWorkCenter().getMachine() != null) {
        machineNameList.add(operationOrder.getWorkCenter().getMachine());
      }
    }

    return chargeByMachineDaysInternal(fromDateTime, toDateTime, machineNameList);
  }

  @Override
  public List<Map<String, Object>> chargePerMachineDays(
      LocalDateTime fromDateTime, LocalDateTime toDateTime, Set<Machine> machinesInUse)
      throws AxelorException {

    fromDateTime = fromDateTime.withHour(0).withMinute(0);
    toDateTime = toDateTime.withHour(23).withMinute(59);

    if (Duration.between(fromDateTime, toDateTime).toDays() > 500) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.CHARGE_MACHINE_DAYS));
    }

    List<OperationOrder> operationOrderListTemp =
        getOperationOrdersInTimeRange(fromDateTime, toDateTime);

    Set<Machine> machineNameList = new HashSet<>();
    for (OperationOrder operationOrder : operationOrderListTemp) {
      if (operationOrder.getWorkCenter() != null
          && operationOrder.getWorkCenter().getMachine() != null
          && machinesInUse.contains(operationOrder.getWorkCenter().getMachine())) {
        machineNameList.add(operationOrder.getWorkCenter().getMachine());
      }
    }

    return chargeByMachineDaysInternal(fromDateTime, toDateTime, machineNameList);
  }

  protected List<Map<String, Object>> chargeByMachineDaysInternal(
      LocalDateTime fromDateTime, LocalDateTime toDateTime, Set<Machine> machineNameList) {

    List<Map<String, Object>> dataList = new ArrayList<>();
    LocalDateTime itDateTime = fromDateTime;

    while (!itDateTime.isAfter(toDateTime)) {
      Map<Machine, BigDecimal> map = new HashMap<>();
      List<OperationOrder> operationOrderList =
          getOperationOrdersInTimeRange(itDateTime, itDateTime.plusHours(1));

      for (OperationOrder operationOrder : operationOrderList) {
        if (operationOrder.getWorkCenter() != null
            && operationOrder.getWorkCenter().getMachine() != null) {
          Machine machine = operationOrder.getWorkCenter().getMachine();
          long numberOfMinutes = getNumberOfMinutesMachineUsedTotal(itDateTime, operationOrder);
          getNumberOfMinutesPerDay(itDateTime, map, operationOrder, machine, numberOfMinutes);
        }
      }
      String itDate = L10n.getInstance().format(itDateTime.toLocalDate());
      for (Machine key : machineNameList) {
        if (map.containsKey(key)) {
          dataList.stream()
              .filter(
                  mapIt ->
                      mapIt.get(DATE_TIME).equals(itDate)
                          && mapIt.get(MACHINE).equals(key.getName()))
              .findFirst()
              .ifPresentOrElse(
                  mapIt -> mapIt.put(CHARGE, ((BigDecimal) mapIt.get(CHARGE)).add(map.get(key))),
                  () -> {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put(DATE_TIME, itDate);
                    dataMap.put(CHARGE, map.get(key));
                    dataMap.put(MACHINE, key.getName());
                    dataList.add(dataMap);
                  });
        }
      }

      itDateTime = itDateTime.plusHours(1);
    }

    return dataList;
  }

  protected long getNumberOfMinutesMachineUsedTotal(
      LocalDateTime itDateTime, OperationOrder operationOrder) {

    LocalDateTime plannedStartDate = operationOrder.getPlannedStartDateT();
    LocalDateTime plannedEndDate = operationOrder.getPlannedEndDateT();

    long numberOfMinutes = 0;

    if (plannedStartDate.isBefore(itDateTime)) {
      numberOfMinutes = Math.min(Duration.between(itDateTime, plannedEndDate).toMinutes(), 60);
    } else if (plannedEndDate.isAfter(itDateTime.plusHours(1))) {
      numberOfMinutes =
          Math.min(Duration.between(plannedStartDate, itDateTime.plusHours(1)).toMinutes(), 60);
    } else {
      numberOfMinutes =
          Math.min(Duration.between(plannedStartDate, plannedEndDate).toMinutes(), 60);
    }

    return numberOfMinutes;
  }

  protected void getNumberOfMinutesPerDay(
      LocalDateTime itDateTime,
      Map<Machine, BigDecimal> map,
      OperationOrder operationOrder,
      Machine machine,
      long numberOfMinutes) {

    long numberOfMinutesPerDay = 0;

    WorkCenter workCenter = operationOrder.getWorkCenter();
    Machine workCenterMachine = workCenter.getMachine();

    if (workCenterMachine.getWeeklyPlanning() != null) {
      DayPlanning dayPlanning =
          weeklyPlanningService.findDayPlanning(
              workCenterMachine.getWeeklyPlanning(),
              LocalDateTime.parse(itDateTime.toString(), DateTimeFormatter.ISO_DATE_TIME)
                  .toLocalDate());

      if (dayPlanning != null) {
        numberOfMinutesPerDay =
            calculateMinutes(dayPlanning.getMorningFrom(), dayPlanning.getMorningTo())
                + calculateMinutes(dayPlanning.getAfternoonFrom(), dayPlanning.getAfternoonTo());
      }
    }

    if (numberOfMinutesPerDay == 0) {
      numberOfMinutesPerDay = 60L * 24;
    }

    BigDecimal percentage =
        new BigDecimal(numberOfMinutes)
            .multiply(new BigDecimal(100))
            .divide(new BigDecimal(numberOfMinutesPerDay), 2, RoundingMode.HALF_UP);

    map.merge(machine, percentage, BigDecimal::add);
  }

  protected long calculateMinutes(LocalTime from, LocalTime to) {
    return (from != null && to != null) ? Duration.between(from, to).toMinutes() : 0;
  }

  protected List<OperationOrder> getOperationOrdersInTimeRange(
      LocalDateTime fromDateTime, LocalDateTime toDateTime) {
    return operationOrderRepository
        .all()
        .filter("self.plannedStartDateT <= :toDateTime AND self.plannedEndDateT >= :fromDateTime")
        .bind("fromDateTime", fromDateTime)
        .bind("toDateTime", toDateTime)
        .fetch();
  }
}
