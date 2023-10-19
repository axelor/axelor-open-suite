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
import com.axelor.apps.base.db.BarcodeTypeConfig;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.MachineTool;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.OperationOrderDuration;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.ProdProcessLineService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.i18n.L10n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationOrderServiceImpl implements OperationOrderService {
  protected static final String PRIORITY = "priority";
  protected static final String DATE_TIME = "dateTime";
  protected static final String MACHINE = "machine";
  protected static final String CHARGE = "charge";
  protected BarcodeGeneratorService barcodeGeneratorService;

  protected AppProductionService appProductionService;
  protected WeeklyPlanningService weeklyPlanningService;
  protected ManufOrderStockMoveService manufOrderStockMoveService;
  protected ProdProcessLineService prodProcessLineService;
  protected OperationOrderRepository operationOrderRepository;

  @Inject
  public OperationOrderServiceImpl(
      BarcodeGeneratorService barcodeGeneratorService,
      AppProductionService appProductionService,
      ManufOrderStockMoveService manufOrderStockMoveService,
      ProdProcessLineService prodProcessLineService,
      OperationOrderRepository operationOrderRepository,
      WeeklyPlanningService weeklyPlanningService) {
    this.barcodeGeneratorService = barcodeGeneratorService;
    this.appProductionService = appProductionService;
    this.manufOrderStockMoveService = manufOrderStockMoveService;
    this.prodProcessLineService = prodProcessLineService;
    this.operationOrderRepository = operationOrderRepository;
    this.weeklyPlanningService = weeklyPlanningService;
  }

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Transactional(rollbackOn = {Exception.class})
  public OperationOrder createOperationOrder(ManufOrder manufOrder, ProdProcessLine prodProcessLine)
      throws AxelorException {

    if (prodProcessLine.getWorkCenter() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.PROD_PROCESS_LINE_MISSING_WORK_CENTER),
          prodProcessLine.getProdProcess() != null
              ? prodProcessLine.getProdProcess().getCode()
              : "null",
          prodProcessLine.getName());
    }
    OperationOrder operationOrder =
        this.createOperationOrder(
            manufOrder,
            prodProcessLine.getPriority(),
            prodProcessLine.getWorkCenter(),
            prodProcessLine.getWorkCenter().getMachine(),
            prodProcessLine.getMachineTool(),
            prodProcessLine);

    return Beans.get(OperationOrderRepository.class).save(operationOrder);
  }

  @Transactional
  public OperationOrder createOperationOrder(
      ManufOrder manufOrder,
      int priority,
      WorkCenter workCenter,
      Machine machine,
      MachineTool machineTool,
      ProdProcessLine prodProcessLine) {

    logger.debug(
        "Creation of an operation {} for the manufacturing order {}",
        priority,
        manufOrder.getManufOrderSeq());

    String operationName = prodProcessLine.getName();

    OperationOrder operationOrder =
        new OperationOrder(
            priority,
            this.computeName(manufOrder, priority, operationName),
            operationName,
            manufOrder,
            workCenter,
            machine,
            OperationOrderRepository.STATUS_DRAFT,
            prodProcessLine,
            machineTool);

    operationOrder.setUseLineInGeneratedPurchaseOrder(
        prodProcessLine.getUseLineInGeneratedPurchaseOrder());

    operationOrder.setOutsourcing(prodProcessLine.getOutsourcing());

    return Beans.get(OperationOrderRepository.class).save(operationOrder);
  }

  public String computeName(ManufOrder manufOrder, int priority, String operationName) {

    String name = "";
    if (manufOrder != null) {

      if (manufOrder.getManufOrderSeq() != null) {
        name += manufOrder.getManufOrderSeq();
      } else {
        name += manufOrder.getId();
      }
    }

    name += "-" + priority + "-" + operationName;

    return name;
  }

  @Override
  public void createToConsumeProdProductList(OperationOrder operationOrder) throws AxelorException {

    BigDecimal manufOrderQty = operationOrder.getManufOrder().getQty();
    BigDecimal bomQty = operationOrder.getManufOrder().getBillOfMaterial().getQty();
    ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();

    if (prodProcessLine == null) {

      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(ProductionExceptionMessage.PRODUCTION_PROCESS_IS_EMPTY));
    }
    if (prodProcessLine.getToConsumeProdProductList() != null) {
      for (ProdProduct prodProduct : prodProcessLine.getToConsumeProdProductList()) {

        BigDecimal qty =
            Beans.get(ManufOrderService.class)
                .computeToConsumeProdProductLineQuantity(
                    bomQty, manufOrderQty, prodProduct.getQty());

        operationOrder.addToConsumeProdProductListItem(
            new ProdProduct(prodProduct.getProduct(), qty, prodProduct.getUnit()));
      }
    }
  }

  @Override
  public OperationOrder updateDiffProdProductList(OperationOrder operationOrder)
      throws AxelorException {
    List<ProdProduct> toConsumeList = operationOrder.getToConsumeProdProductList();
    List<StockMoveLine> consumedList = operationOrder.getConsumedStockMoveLineList();
    if (toConsumeList == null || consumedList == null) {
      return operationOrder;
    }
    List<ProdProduct> diffConsumeList =
        createDiffProdProductList(operationOrder, toConsumeList, consumedList);

    operationOrder.clearDiffConsumeProdProductList();
    diffConsumeList.forEach(operationOrder::addDiffConsumeProdProductListItem);
    return operationOrder;
  }

  protected List<OperationOrder> getOperationOrdersInTimeRange(
      LocalDateTime fromDateTime, LocalDateTime toDateTime) {
    return operationOrderRepository
        .all()
        .filter(
            "self.plannedStartDateT <= ?2 AND self.plannedEndDateT >= ?1", fromDateTime, toDateTime)
        .fetch();
  }

  protected Map<Object, BigDecimal> getMachineChargeMap(
      LocalDateTime itDateTime, Boolean chargePerCompany, Machine machineInUse) {

    Map<Object, BigDecimal> machineChargeMap = new HashMap<>();
    List<OperationOrder> operationOrderList =
        getOperationOrdersInTimeRange(itDateTime, itDateTime.plusHours(1));

    for (OperationOrder operationOrder : operationOrderList) {
      if (operationOrder.getWorkCenter() != null
          && operationOrder.getWorkCenter().getMachine() != null) {
        Object machineOrCompany =
            Boolean.TRUE.equals(chargePerCompany)
                    && machineInUse != null
                    && operationOrder
                        .getWorkCenter()
                        .getMachine()
                        .getId()
                        .equals(machineInUse.getId())
                ? operationOrder.getWorkCenter().getMachine().getStockLocation().getCompany()
                : operationOrder.getWorkCenter().getMachine();

        long numberOfMinutes = calculateNumberOfMinutesPerHour(operationOrder, itDateTime);
        BigDecimal percentage = calculateMachineChargePercentagePerHour(numberOfMinutes);

        machineChargeMap.put(
            machineOrCompany,
            machineChargeMap.getOrDefault(machineOrCompany, BigDecimal.ZERO).add(percentage));
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

  public List<Map<String, Object>> chargeByMachineHours(
      LocalDateTime fromDateTime, LocalDateTime toDateTime) throws AxelorException {
    List<Map<String, Object>> dataList = new ArrayList<>();

    LocalDateTime itDateTime = fromDateTime;
    if (Duration.between(fromDateTime, toDateTime).toDays() > 20) {
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
      Map<Object, BigDecimal> chargeMap = getMachineChargeMap(itDateTime, Boolean.FALSE, null);
      Set<Object> keyList = chargeMap.keySet();
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

  protected void adjustChargePerHour(
      Map<Object, BigDecimal> chargeMap, Map<Company, Integer> companyMachineMap) {
    for (Map.Entry<Object, BigDecimal> entry : chargeMap.entrySet()) {
      Object company = entry.getKey();
      BigDecimal charge = entry.getValue();
      Integer divisor = companyMachineMap.getOrDefault(company, 1);
      chargeMap.put(company, charge.divide(BigDecimal.valueOf(divisor), 2, RoundingMode.HALF_UP));
    }
  }

  public List<Map<String, Object>> calculateHourlyMachineCharge(
      LocalDateTime fromDateTime, LocalDateTime toDateTime, Machine machineInUse)
      throws AxelorException {
    List<Map<String, Object>> dataList = new ArrayList<>();
    LocalDateTime itDateTime = fromDateTime;

    if (Duration.between(fromDateTime, toDateTime).toDays() > 20) {
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
                        && machine.getId().equals(machineInUse.getId())
                        && machine.getStockLocation() != null
                        && machine.getStockLocation().getCompany() != null)
            .collect(
                Collectors.groupingBy(
                    machine -> machine.getStockLocation().getCompany(),
                    Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

    while (!itDateTime.isAfter(toDateTime)) {
      Map<Object, BigDecimal> chargeMap =
          getMachineChargeMap(itDateTime, Boolean.TRUE, machineInUse);

      adjustChargePerHour(chargeMap, companyMachineMap);
      Set<Object> keyList = chargeMap.keySet();
      String dateTime = L10n.getInstance().format(itDateTime);
      for (Company key : companyMachineMap.keySet()) {
        BigDecimal charge = chargeMap.getOrDefault(key, BigDecimal.ZERO);
        if (keyList.contains(key)) {
          Map<String, Object> dataMap = new HashMap<>();
          dataMap.put(DATE_TIME, dateTime);
          dataMap.put(CHARGE, charge);
          dataMap.put("company", key.getName());
          dataList.add(dataMap);
        }
      }

      itDateTime = itDateTime.plusHours(1);
    }
    return dataList;
  }

  @Override
  public List<Map<String, Object>> chargeByMachineDays(
      LocalDateTime fromDateTime, LocalDateTime toDateTime) throws AxelorException {
    return chargeByMachineDaysInternal(fromDateTime, toDateTime, null, false);
  }

  @Override
  public List<Map<String, Object>> chargePerMachineDays(
      LocalDateTime fromDateTime, LocalDateTime toDateTime, Set<Machine> machinesInUse)
      throws AxelorException {
    return chargeByMachineDaysInternal(fromDateTime, toDateTime, machinesInUse, true);
  }

  protected List<Map<String, Object>> chargeByMachineDaysInternal(
      LocalDateTime fromDateTime,
      LocalDateTime toDateTime,
      Set<Machine> machinesInUse,
      boolean considerMachine)
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
      getMachineNameList(machinesInUse, considerMachine, machineNameList, operationOrder);
    }

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
          long numberOfMinutes = getNumberOfMinutesMachineUsed(itDateTime, operationOrder);
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

  protected void getMachineNameList(
      Set<Machine> machinesInUse,
      boolean considerMachine,
      Set<Machine> machineNameList,
      OperationOrder operationOrder) {
    if (operationOrder.getWorkCenter() != null
            && operationOrder.getWorkCenter().getMachine() != null
            && (considerMachine
                && !ObjectUtils.isEmpty(machinesInUse)
                && machinesInUse.contains(operationOrder.getWorkCenter().getMachine()))
        || !considerMachine) {
      machineNameList.add(operationOrder.getWorkCenter().getMachine());
    }
  }

  protected long getNumberOfMinutesMachineUsed(
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

  @Override
  public List<ProdProduct> createDiffProdProductList(
      OperationOrder operationOrder,
      List<ProdProduct> prodProductList,
      List<StockMoveLine> stockMoveLineList)
      throws AxelorException {
    List<ProdProduct> diffConsumeList =
        Beans.get(ManufOrderService.class)
            .createDiffProdProductList(prodProductList, stockMoveLineList);
    diffConsumeList.forEach(
        prodProduct -> prodProduct.setDiffConsumeOperationOrder(operationOrder));
    return diffConsumeList;
  }

  @Override
  public void checkConsumedStockMoveLineList(
      OperationOrder operationOrder, OperationOrder oldOperationOrder) throws AxelorException {
    Beans.get(ManufOrderService.class)
        .checkRealizedStockMoveLineList(
            operationOrder.getConsumedStockMoveLineList(),
            oldOperationOrder.getConsumedStockMoveLineList());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateConsumedStockMoveFromOperationOrder(OperationOrder operationOrder)
      throws AxelorException {
    this.updateDiffProdProductList(operationOrder);
    ManufOrder manufOrder = operationOrder.getManufOrder();
    Company company = manufOrder.getCompany();
    List<StockMoveLine> consumedStockMoveLineList = operationOrder.getConsumedStockMoveLineList();
    StockLocation fromStockLocation =
        manufOrderStockMoveService.getFromStockLocationForConsumedStockMove(manufOrder, company);
    StockLocation virtualStockLocation =
        manufOrderStockMoveService.getVirtualStockLocationForConsumedStockMove(manufOrder, company);
    if (consumedStockMoveLineList == null) {
      return;
    }
    Optional<StockMove> stockMoveOpt =
        operationOrder.getInStockMoveList().stream()
            .filter(stockMove -> stockMove.getStatusSelect() == StockMoveRepository.STATUS_PLANNED)
            .findFirst();
    StockMove stockMove;
    if (stockMoveOpt.isPresent()) {
      stockMove = stockMoveOpt.get();
    } else {
      stockMove =
          Beans.get(ManufOrderStockMoveService.class)
              ._createToConsumeStockMove(
                  manufOrder, company, fromStockLocation, virtualStockLocation);
      operationOrder.addInStockMoveListItem(stockMove);
      Beans.get(StockMoveService.class).plan(stockMove);
    }

    Beans.get(ManufOrderService.class)
        .updateStockMoveFromManufOrder(consumedStockMoveLineList, stockMove);
  }

  @Override
  public void createBarcode(OperationOrder operationOrder) {
    if (operationOrder != null && operationOrder.getId() != null) {
      String serialNbr = operationOrder.getId().toString();
      BarcodeTypeConfig barcodeTypeConfig =
          appProductionService.getAppProduction().getBarcodeTypeConfig();
      boolean addPadding = true;
      MetaFile barcodeFile =
          barcodeGeneratorService.createBarCode(
              operationOrder.getId(),
              "OppOrderBarcode%d.png",
              serialNbr,
              barcodeTypeConfig,
              addPadding);
      if (barcodeFile != null) {
        operationOrder.setBarCode(barcodeFile);
      }
    }
  }

  @Override
  public long computeEntireCycleDuration(OperationOrder operationOrder, BigDecimal qty)
      throws AxelorException {
    ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();

    return prodProcessLineService.computeEntireCycleDuration(operationOrder, prodProcessLine, qty);
  }

  /**
   * Computes the duration of all the {@link OperationOrderDuration} of {@code operationOrder}
   *
   * @param operationOrder An operation order
   * @return Real duration of {@code operationOrder}
   */
  @Override
  public Duration computeRealDuration(OperationOrder operationOrder) {
    Duration totalDuration = Duration.ZERO;

    List<OperationOrderDuration> operationOrderDurations =
        operationOrder.getOperationOrderDurationList();
    if (operationOrderDurations != null) {
      for (OperationOrderDuration operationOrderDuration : operationOrderDurations) {
        if (operationOrderDuration.getStartingDateTime() != null
            && operationOrderDuration.getStoppingDateTime() != null) {
          totalDuration =
              totalDuration.plus(
                  Duration.between(
                      operationOrderDuration.getStartingDateTime(),
                      operationOrderDuration.getStoppingDateTime()));
        }
      }
    }

    return totalDuration;
  }

  @Override
  public LocalDateTime getNextOperationDate(OperationOrder operationOrder) {
    ManufOrder manufOrder = operationOrder.getManufOrder();
    OperationOrder nextOperationOrder =
        operationOrderRepository
            .all()
            .filter(
                "self.manufOrder = :manufOrder AND self.priority >= :priority AND self.statusSelect BETWEEN :statusPlanned AND :statusStandby AND self.id != :operationOrderId")
            .bind("manufOrder", manufOrder)
            .bind(PRIORITY, operationOrder.getPriority())
            .bind("statusPlanned", OperationOrderRepository.STATUS_PLANNED)
            .bind("statusStandby", OperationOrderRepository.STATUS_STANDBY)
            .bind("operationOrderId", operationOrder.getId())
            .order(PRIORITY)
            .order("plannedStartDateT")
            .fetchOne();

    LocalDateTime manufOrderPlannedEndDateT = manufOrder.getPlannedEndDateT();
    if (nextOperationOrder == null) {
      return manufOrderPlannedEndDateT;
    }

    LocalDateTime plannedStartDateT = nextOperationOrder.getPlannedStartDateT();

    if (Objects.equals(nextOperationOrder.getPriority(), operationOrder.getPriority())) {
      LocalDateTime plannedEndDateT = nextOperationOrder.getPlannedEndDateT();
      if (plannedEndDateT != null && plannedEndDateT.isBefore(manufOrderPlannedEndDateT)) {
        boolean isOnSameMachine =
            Objects.equals(nextOperationOrder.getMachine(), operationOrder.getMachine());
        return isOnSameMachine ? plannedStartDateT : plannedEndDateT;
      }

    } else if (plannedStartDateT != null && plannedStartDateT.isBefore(manufOrderPlannedEndDateT)) {
      return plannedStartDateT;
    }

    return manufOrderPlannedEndDateT;
  }

  @Override
  public LocalDateTime getLastOperationDate(OperationOrder operationOrder) {
    ManufOrder manufOrder = operationOrder.getManufOrder();
    OperationOrder lastOperationOrder =
        operationOrderRepository
            .all()
            .filter(
                "self.manufOrder = :manufOrder AND ((self.priority = :priority AND self.machine = :machine) OR self.priority < :priority) AND self.statusSelect BETWEEN :statusPlanned AND :statusStandby AND self.id != :operationOrderId")
            .bind("manufOrder", manufOrder)
            .bind(PRIORITY, operationOrder.getPriority())
            .bind("statusPlanned", OperationOrderRepository.STATUS_PLANNED)
            .bind("statusStandby", OperationOrderRepository.STATUS_STANDBY)
            .bind(MACHINE, operationOrder.getMachine())
            .bind("operationOrderId", operationOrder.getId())
            .order("-priority")
            .order("-plannedEndDateT")
            .fetchOne();

    LocalDateTime manufOrderPlannedStartDateT = manufOrder.getPlannedStartDateT();
    if (lastOperationOrder == null) {
      return manufOrderPlannedStartDateT;
    }

    LocalDateTime plannedEndDateT = lastOperationOrder.getPlannedEndDateT();

    if (Objects.equals(lastOperationOrder.getPriority(), operationOrder.getPriority())) {
      LocalDateTime plannedStartDateT = lastOperationOrder.getPlannedStartDateT();
      if (plannedStartDateT != null && plannedStartDateT.isAfter(manufOrderPlannedStartDateT)) {
        boolean isOnSameMachine =
            Objects.equals(lastOperationOrder.getMachine(), operationOrder.getMachine());
        return isOnSameMachine ? plannedEndDateT : plannedStartDateT;
      }

    } else if (plannedEndDateT != null && plannedEndDateT.isAfter(manufOrderPlannedStartDateT)) {
      return plannedEndDateT;
    }

    return manufOrderPlannedStartDateT;
  }

  @Override
  public long getDuration(OperationOrder operationOrder) throws AxelorException {
    if (operationOrder.getWorkCenter() != null) {
      return computeEntireCycleDuration(operationOrder, operationOrder.getManufOrder().getQty());
    }
    return 0;
  }

  /**
   * Sort operationOrders list by priority and id.
   *
   * @param operationOrders
   * @return
   */
  @Override
  public List<OperationOrder> getSortedOperationOrderList(List<OperationOrder> operationOrders) {

    Comparator<OperationOrder> byPriority =
        Comparator.comparing(
            OperationOrder::getPriority, Comparator.nullsFirst(Comparator.naturalOrder()));
    Comparator<OperationOrder> byId =
        Comparator.comparing(
            OperationOrder::getId, Comparator.nullsFirst(Comparator.naturalOrder()));

    return operationOrders.stream()
        .sorted(byPriority.thenComparing(byId))
        .collect(Collectors.toList());
  }

  /**
   * Reverse sort operationOrders list by priority and id.
   *
   * @param operationOrders
   * @return
   */
  @Override
  public List<OperationOrder> getReversedSortedOperationOrderList(
      List<OperationOrder> operationOrders) {

    return Lists.reverse(getSortedOperationOrderList(operationOrders));
  }
}
