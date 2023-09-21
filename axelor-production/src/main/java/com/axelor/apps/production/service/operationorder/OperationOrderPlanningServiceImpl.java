package com.axelor.apps.production.service.operationorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.model.machine.MachineTimeSlot;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.production.service.machine.MachineService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.date.DurationTool;
import com.axelor.utils.date.LocalDateTimeUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class OperationOrderPlanningServiceImpl implements OperationOrderPlanningService {

  protected ProductionConfigService productionConfigService;
  protected OperationOrderStockMoveService operationOrderStockMoveService;
  protected WeeklyPlanningService weeklyPlanningService;
  protected MachineService machineService;
  protected OperationOrderRepository operationOrderRepository;
  protected ManufOrderService manufOrderService;
  protected OperationOrderService operationOrderService;

  @Inject
  public OperationOrderPlanningServiceImpl(
      ProductionConfigService productionConfigService,
      OperationOrderStockMoveService operationOrderStockMoveService,
      MachineService machineService,
      OperationOrderRepository operationOrderRepository,
      WeeklyPlanningService weeklyPlanningService,
      ManufOrderService manufOrderService,
      OperationOrderService operationOrderService) {
    this.productionConfigService = productionConfigService;
    this.operationOrderStockMoveService = operationOrderStockMoveService;
    this.machineService = machineService;
    this.operationOrderRepository = operationOrderRepository;
    this.weeklyPlanningService = weeklyPlanningService;
    this.manufOrderService = manufOrderService;
    this.operationOrderService = operationOrderService;
  }

  /**
   * Plan an operation order. For successive calls, must be called by order of operation order
   * priority.
   *
   * @param operationOrder
   * @param cumulatedDuration
   * @return
   * @throws AxelorException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public OperationOrder plan(OperationOrder operationOrder, Long cumulatedDuration)
      throws AxelorException {
    ManufOrder manufOrder = operationOrder.getManufOrder();
    Company company = manufOrder.getCompany();
    ProductionConfig productionConfig = productionConfigService.getProductionConfig(company);
    boolean useAsapScheduling =
        productionConfig.getScheduling()
            == ProductionConfigRepository.AS_SOON_AS_POSSIBLE_SCHEDULING;
    return plan(operationOrder, cumulatedDuration, useAsapScheduling);
  }

  /**
   * Plan an operation order. For successive calls, must be called by order of operation order
   * priority. The order must be ascending if useAsapScheduling is true and descending if not.
   *
   * @param operationOrder
   * @param cumulatedDuration
   * @param useAsapScheduling
   * @return
   * @throws AxelorException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public OperationOrder plan(
      OperationOrder operationOrder, Long cumulatedDuration, boolean useAsapScheduling)
      throws AxelorException {

    if (CollectionUtils.isEmpty(operationOrder.getToConsumeProdProductList())) {
      Beans.get(OperationOrderService.class).createToConsumeProdProductList(operationOrder);
    }

    ManufOrder manufOrder = operationOrder.getManufOrder();
    Company company = manufOrder.getCompany();
    ProductionConfig productionConfig = productionConfigService.getProductionConfig(company);

    switch (productionConfig.getCapacity()) {
      case ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING:
        planPlannedDatesForFiniteCapacityScheduling(operationOrder, useAsapScheduling);

        operationOrder.setRealStartDateT(null);
        operationOrder.setRealEndDateT(null);
        operationOrder.setRealDuration(null);
        break;
      case ProductionConfigRepository.INFINITE_CAPACITY_SCHEDULING:
        planPlannedDatesForInfiniteCapacityScheduling(
            operationOrder, cumulatedDuration, useAsapScheduling);
        break;
      default:
        throw new AxelorException(
            productionConfig,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(
                ProductionExceptionMessage.UNRECOGNIZED_CAPACITY_FOR_COMPANY_PRODUCTION_CONFIG),
            company.getName());
    }

    if (Boolean.TRUE.equals(manufOrder.getIsConsProOnOperation())) {
      operationOrderStockMoveService.createToConsumeStockMove(operationOrder);
    }
    operationOrder.setStatusSelect(OperationOrderRepository.STATUS_PLANNED);

    return operationOrderRepository.save(operationOrder);
  }

  /**
   * Re-plan an operation order. For successive calls, must reset planned dates first, then call by
   * order of operation order priority.
   *
   * @param operationOrder
   * @return
   * @throws AxelorException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public OperationOrder replan(OperationOrder operationOrder) throws AxelorException {

    ManufOrder manufOrder = operationOrder.getManufOrder();
    Company company = manufOrder.getCompany();
    ProductionConfig productionConfig = productionConfigService.getProductionConfig(company);
    boolean useAsapScheduling =
        productionConfig.getScheduling()
            == ProductionConfigRepository.AS_SOON_AS_POSSIBLE_SCHEDULING;

    switch (productionConfig.getCapacity()) {
      case ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING:
        operationOrder.setPlannedStartDateT(null);
        operationOrder.setPlannedEndDateT(null);

        planPlannedDatesForFiniteCapacityScheduling(operationOrder, useAsapScheduling);
        break;
      case ProductionConfigRepository.INFINITE_CAPACITY_SCHEDULING:
        operationOrder.setPlannedStartDateT(this.getLastOperationDate(operationOrder));
        operationOrder.setPlannedEndDateT(this.computePlannedEndDateT(operationOrder));

        operationOrder.setPlannedDuration(
            DurationTool.getSecondsDuration(
                Duration.between(
                    operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT())));
        break;
      default:
        throw new AxelorException(
            productionConfig,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(
                ProductionExceptionMessage.UNRECOGNIZED_CAPACITY_FOR_COMPANY_PRODUCTION_CONFIG),
            company.getName());
    }

    return operationOrderRepository.save(operationOrder);
  }

  protected void planPlannedDatesForFiniteCapacityScheduling(
      OperationOrder operationOrder, boolean useAsapScheduling) throws AxelorException {
    Machine machine = operationOrder.getMachine();
    if (machine != null) {
      planDatesWithMachine(operationOrder, machine, useAsapScheduling);
    } else {
      planDatesWithoutMachine(operationOrder, useAsapScheduling);
    }
  }

  protected void planDatesWithMachine(
      OperationOrder operationOrder, Machine machine, boolean useAsapScheduling)
      throws AxelorException {

    LocalDateTime maxDate;
    if (useAsapScheduling) {
      LocalDateTime plannedStartDate = operationOrder.getPlannedStartDateT();
      LocalDateTime lastOPerationDate = this.getLastOperationDate(operationOrder);
      maxDate = LocalDateTimeUtils.max(plannedStartDate, lastOPerationDate);
    } else {
      LocalDateTime plannedEndDate = operationOrder.getPlannedEndDateT();
      LocalDateTime nextOPerationDate = this.getNextOperationDate(operationOrder);
      maxDate = LocalDateTimeUtils.max(plannedEndDate, nextOPerationDate);
    }

    MachineTimeSlot freeMachineTimeSlot =
        useAsapScheduling
            ? machineService.getClosestAvailableTimeSlotFrom(
                machine, maxDate, maxDate.plusSeconds(getDuration(operationOrder)), operationOrder)
            : machineService.getFurthestAvailableTimeSlotFrom(
                machine,
                maxDate.minusSeconds(getDuration(operationOrder)),
                maxDate,
                operationOrder);
    operationOrder.setPlannedStartDateT(freeMachineTimeSlot.getStartDateT());
    operationOrder.setPlannedEndDateT(freeMachineTimeSlot.getEndDateT());

    Long plannedDuration =
        DurationTool.getSecondsDuration(
            Duration.between(
                operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT()));
    operationOrder.setPlannedDuration(plannedDuration);
  }

  protected void planDatesWithoutMachine(OperationOrder operationOrder, boolean useAsapScheduling)
      throws AxelorException {

    LocalDateTime maxDate;
    if (useAsapScheduling) {
      LocalDateTime plannedStartDate = operationOrder.getPlannedStartDateT();
      LocalDateTime lastOperationDate = this.getLastOperationDate(operationOrder);
      maxDate = LocalDateTimeUtils.max(plannedStartDate, lastOperationDate);

      operationOrder.setPlannedStartDateT(maxDate);

      operationOrder.setPlannedEndDateT(
          operationOrder.getPlannedStartDateT().plusSeconds(this.getDuration(operationOrder)));
    } else {
      LocalDateTime plannedEndDate = operationOrder.getPlannedEndDateT();
      LocalDateTime nextOperationDate = this.getNextOperationDate(operationOrder);
      maxDate = LocalDateTimeUtils.max(plannedEndDate, nextOperationDate);

      operationOrder.setPlannedEndDateT(maxDate);

      operationOrder.setPlannedStartDateT(
          operationOrder.getPlannedEndDateT().minusSeconds(this.getDuration(operationOrder)));
    }

    Long plannedDuration =
        DurationTool.getSecondsDuration(
            Duration.between(
                operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT()));
    operationOrder.setPlannedDuration(plannedDuration);
  }

  protected void planPlannedDatesForInfiniteCapacityScheduling(
      OperationOrder operationOrder, Long cumulatedDuration, boolean useAsapScheduling)
      throws AxelorException {

    Machine machine = operationOrder.getMachine();
    WeeklyPlanning weeklyPlanning = null;
    if (!useAsapScheduling) {
      LocalDateTime plannedEndDate = operationOrder.getPlannedEndDateT();

      LocalDateTime nextOperationDate = this.getNextOperationDate(operationOrder);
      LocalDateTime maxDate = LocalDateTimeUtils.max(plannedEndDate, nextOperationDate);
      operationOrder.setPlannedEndDateT(maxDate);

      if (machine != null) {
        weeklyPlanning = machine.getWeeklyPlanning();
      }
      if (weeklyPlanning != null) {
        this.planWithPlanning(operationOrder, weeklyPlanning, useAsapScheduling);
      }
      operationOrder.setPlannedStartDateT(this.computePlannedStartDateT(operationOrder));

      if (cumulatedDuration != null) {
        operationOrder.setPlannedStartDateT(
            operationOrder
                .getPlannedStartDateT()
                .plusSeconds(cumulatedDuration)
                .minusSeconds(this.getMachineSetupDuration(operationOrder)));
      }

    } else {
      LocalDateTime plannedStartDate = operationOrder.getPlannedStartDateT();

      LocalDateTime lastOperationDate = this.getLastOperationDate(operationOrder);
      LocalDateTime maxDate = LocalDateTimeUtils.max(plannedStartDate, lastOperationDate);
      operationOrder.setPlannedStartDateT(maxDate);

      if (machine != null) {
        weeklyPlanning = machine.getWeeklyPlanning();
      }
      if (weeklyPlanning != null) {
        this.planWithPlanning(operationOrder, weeklyPlanning, useAsapScheduling);
      }

      operationOrder.setPlannedEndDateT(this.computePlannedEndDateT(operationOrder));

      if (cumulatedDuration != null) {
        operationOrder.setPlannedEndDateT(
            operationOrder
                .getPlannedEndDateT()
                .minusSeconds(cumulatedDuration)
                .plusSeconds(this.getMachineSetupDuration(operationOrder)));
      }
    }

    Long plannedDuration =
        DurationTool.getSecondsDuration(
            Duration.between(
                operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT()));

    operationOrder.setPlannedDuration(plannedDuration);
    if (weeklyPlanning != null) {
      this.manageDurationWithMachinePlanning(operationOrder, weeklyPlanning, useAsapScheduling);
    }
  }

  protected LocalDateTime computePlannedEndDateT(OperationOrder operationOrder)
      throws AxelorException {

    if (operationOrder.getWorkCenter() != null) {
      return operationOrder
          .getPlannedStartDateT()
          .plusSeconds(
              (int)
                  operationOrderService.computeEntireCycleDuration(
                      operationOrder, operationOrder.getManufOrder().getQty()));
    }

    return operationOrder.getPlannedStartDateT();
  }

  public LocalDateTime computePlannedStartDateT(OperationOrder operationOrder)
      throws AxelorException {

    if (operationOrder.getWorkCenter() != null) {
      return operationOrder
          .getPlannedEndDateT()
          .minusSeconds(
              (int)
                  operationOrderService.computeEntireCycleDuration(
                      operationOrder, operationOrder.getManufOrder().getQty()));
    }

    return operationOrder.getPlannedEndDateT();
  }

  protected long getMachineSetupDuration(OperationOrder operationOrder) throws AxelorException {
    ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();

    long duration = 0;

    WorkCenter workCenter = prodProcessLine.getWorkCenter();

    if (workCenter == null) {
      return 0;
    }

    int workCenterTypeSelect = workCenter.getWorkCenterTypeSelect();

    if (workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_MACHINE
        || workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_BOTH) {
      Machine machine = workCenter.getMachine();
      if (machine == null) {
        throw new AxelorException(
            workCenter,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(ProductionExceptionMessage.WORKCENTER_NO_MACHINE),
            workCenter.getName());
      }
      duration += workCenter.getStartingDuration();
      duration += workCenter.getEndingDuration();
      duration += workCenter.getSetupDuration();
    }

    return duration;
  }

  /**
   * Set the planned start date of the operation order according to the planning of the machine
   *
   * @param weeklyPlanning
   * @param operationOrder
   * @param useAsapScheduling
   */
  protected void planWithPlanning(
      OperationOrder operationOrder, WeeklyPlanning weeklyPlanning, boolean useAsapScheduling) {
    DayPlanning dayPlanning;
    LocalDateTime endDate = null;
    LocalDateTime startDate = null;
    if (!useAsapScheduling) {
      endDate = operationOrder.getPlannedEndDateT();
      dayPlanning = weeklyPlanningService.findDayPlanning(weeklyPlanning, endDate.toLocalDate());
    } else {
      startDate = operationOrder.getPlannedStartDateT();
      dayPlanning = weeklyPlanningService.findDayPlanning(weeklyPlanning, startDate.toLocalDate());
    }

    if (dayPlanning != null) {
      if (useAsapScheduling) {

        LocalTime firstPeriodFrom = dayPlanning.getMorningFrom();
        LocalTime firstPeriodTo = dayPlanning.getMorningTo();
        LocalTime secondPeriodFrom = dayPlanning.getAfternoonFrom();
        LocalTime secondPeriodTo = dayPlanning.getAfternoonTo();
        LocalTime startDateTime = startDate.toLocalTime();

        /*
         * If the start date is before the start time of the machine (or equal, then the operation
         * order will begins at the same time than the machine Example: Machine begins at 8am. We set
         * the date to 6am. Then the planned start date will be set to 8am.
         */
        if (firstPeriodFrom != null
            && (startDateTime.isBefore(firstPeriodFrom) || startDateTime.equals(firstPeriodFrom))) {
          operationOrder.setPlannedStartDateT(startDate.toLocalDate().atTime(firstPeriodFrom));
        }
        /*
         * If the machine has two periods, with a break between them, and the operation is planned
         * inside this period of time, then we will start the operation at the beginning of the
         * machine second period. Example: Machine hours is 8am to 12 am. 2pm to 6pm. We try to begins
         * at 1pm. The operation planned start date will be set to 2pm.
         */
        else if (firstPeriodTo != null
            && secondPeriodFrom != null
            && (startDateTime.isAfter(firstPeriodTo) || startDateTime.equals(firstPeriodTo))
            && (startDateTime.isBefore(secondPeriodFrom)
                || startDateTime.equals(secondPeriodFrom))) {
          operationOrder.setPlannedStartDateT(startDate.toLocalDate().atTime(secondPeriodFrom));
        }
        /*
         * If the start date is planned after working hours, or during a day off, then we will search
         * for the first period of the machine available. Example: Machine on Friday is 6am to 8 pm.
         * We set the date to 9pm. The next working day is Monday 8am. Then the planned start date
         * will be set to Monday 8am.
         */
        else if ((firstPeriodTo != null
                && secondPeriodFrom == null
                && (startDateTime.isAfter(firstPeriodTo) || startDateTime.equals(firstPeriodTo)))
            || (secondPeriodTo != null
                && (startDateTime.isAfter(secondPeriodTo) || startDateTime.equals(secondPeriodTo)))
            || (firstPeriodFrom == null && secondPeriodFrom == null)) {
          this.searchForNextWorkingDay(operationOrder, weeklyPlanning, startDate);
        }
      } else {

        LocalTime firstPeriodFrom = dayPlanning.getMorningFrom();
        LocalTime firstPeriodTo = dayPlanning.getMorningTo();
        LocalTime secondPeriodTo = dayPlanning.getAfternoonTo();
        LocalTime endDateTime = endDate.toLocalTime();

        /*
         * If the end date is after the end time of the second period (or equal, then the operation
         * order will end at the same time than the machine Example: Machine ends at 8pm. We set the
         * date to 9pm. Then the planned end date will be set to 8pm).
         */
        if (secondPeriodTo != null
            && (endDateTime.isAfter(secondPeriodTo) || endDateTime.equals(secondPeriodTo))) {
          operationOrder.setPlannedEndDateT(endDate.toLocalDate().atTime(secondPeriodTo));
        }
        /*
         * If the end date is after the end time of the first period (or equal, then the operation
         * order will end at the same time than the machine Example: Machine ends at 1pm. We set the
         * date to 2pm. Then the planned end date will be set to 1pm).
         */
        else if (firstPeriodTo != null
            && (endDateTime.isAfter(firstPeriodTo) || endDateTime.equals(firstPeriodTo))) {
          operationOrder.setPlannedEndDateT(endDate.toLocalDate().atTime(firstPeriodTo));
        }
        /*
         * If the end date is planned before working hours, or during a day off, then we will search
         * for the first period of the machine available on the days previous to the current end
         * date. Example: Machine on Friday is 6am to 8 pm. We set the date to 5am. The previous
         * working day is Thursday and it ends at 8pm. Then the planned end date will be set to
         * Thursday 8pm.
         */
        else if (firstPeriodFrom != null
            && (endDateTime.isBefore(firstPeriodFrom) || endDateTime.equals(firstPeriodFrom))) {
          this.searchForPreviousWorkingDay(operationOrder, weeklyPlanning, endDate);
        }
      }
    }
  }

  protected void searchForNextWorkingDay(
      OperationOrder operationOrder, WeeklyPlanning weeklyPlanning, LocalDateTime startDate) {
    int daysToAddNbr = 0;
    DayPlanning nextDayPlanning;
    /* We will find the next DayPlanning with at least one working period. */
    do {

      daysToAddNbr++;
      nextDayPlanning =
          weeklyPlanningService.findDayPlanning(
              weeklyPlanning, startDate.toLocalDate().plusDays(daysToAddNbr));
    } while (nextDayPlanning.getAfternoonFrom() == null
        && nextDayPlanning.getMorningFrom() == null);

    /*
     * We will add the nbr of days to retrieve the working day, and set the time to either the first
     * morning period or the first afternoon period.
     */
    if (nextDayPlanning.getMorningFrom() != null) {
      operationOrder.setPlannedStartDateT(
          startDate.toLocalDate().plusDays(daysToAddNbr).atTime(nextDayPlanning.getMorningFrom()));
    } else if (nextDayPlanning.getAfternoonFrom() != null) {
      operationOrder.setPlannedStartDateT(
          startDate
              .toLocalDate()
              .plusDays(daysToAddNbr)
              .atTime(nextDayPlanning.getAfternoonFrom()));
    }
  }

  public void searchForPreviousWorkingDay(
      OperationOrder operationOrder, WeeklyPlanning weeklyPlanning, LocalDateTime endDate) {
    int daysToSubstractNbr = 0;
    DayPlanning previousDayPlanning;
    /* change comment . We will find the previous DayPlanning with at least one working period. */
    do {

      daysToSubstractNbr++;
      previousDayPlanning =
          weeklyPlanningService.findDayPlanning(
              weeklyPlanning, endDate.toLocalDate().minusDays(daysToSubstractNbr));
    } while (previousDayPlanning.getAfternoonFrom() == null
        && previousDayPlanning.getMorningFrom() == null);

    /*
     * We will subtract the nbr of days to retrieve the working day, and set the time to either the ending of the first
     * morning period or the first afternoon period.
     */
    if (previousDayPlanning.getAfternoonTo() != null) {
      operationOrder.setPlannedEndDateT(
          endDate
              .toLocalDate()
              .minusDays(daysToSubstractNbr)
              .atTime(previousDayPlanning.getAfternoonTo()));
    } else if (previousDayPlanning.getMorningTo() != null) {
      operationOrder.setPlannedEndDateT(
          endDate
              .toLocalDate()
              .minusDays(daysToSubstractNbr)
              .atTime(previousDayPlanning.getMorningTo()));
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void manageDurationWithMachinePlanning(
      OperationOrder operationOrder, WeeklyPlanning weeklyPlanning, boolean useAsapScheduling)
      throws AxelorException {
    LocalDateTime startDate = operationOrder.getPlannedStartDateT();
    LocalDateTime endDate = operationOrder.getPlannedEndDateT();
    DayPlanning dayPlanning =
        weeklyPlanningService.findDayPlanning(weeklyPlanning, startDate.toLocalDate());
    if (dayPlanning != null) {
      LocalTime firstPeriodTo = dayPlanning.getMorningTo();
      LocalTime secondPeriodFrom = dayPlanning.getAfternoonFrom();
      LocalTime startDateTime = startDate.toLocalTime();
      LocalTime endDateTime = endDate.toLocalTime();

      /*
       * If operation begins inside one period of the machine but finished after that period, then
       * we split the operation
       */
      if (firstPeriodTo != null
          && startDateTime.isBefore(firstPeriodTo)
          && endDateTime.isAfter(firstPeriodTo)) {
        LocalDateTime plannedEndDate = startDate.toLocalDate().atTime(firstPeriodTo);
        Long plannedDuration =
            DurationTool.getSecondsDuration(Duration.between(startDate, plannedEndDate));
        operationOrder.setPlannedDuration(plannedDuration);
        operationOrder.setPlannedEndDateT(plannedEndDate);
        operationOrderRepository.save(operationOrder);
        OperationOrder otherOperationOrder = JPA.copy(operationOrder, true);
        otherOperationOrder.setPlannedStartDateT(plannedEndDate);
        if (secondPeriodFrom != null) {
          otherOperationOrder.setPlannedStartDateT(
              startDate.toLocalDate().atTime(secondPeriodFrom));
        } else {
          this.searchForNextWorkingDay(otherOperationOrder, weeklyPlanning, plannedEndDate);
        }
        operationOrderRepository.save(otherOperationOrder);
        this.plan(otherOperationOrder, operationOrder.getPlannedDuration(), useAsapScheduling);
      }
    }
  }

  protected LocalDateTime getLastOperationDate(OperationOrder operationOrder) {
    ManufOrder manufOrder = operationOrder.getManufOrder();
    OperationOrder lastOperationOrder =
        operationOrderRepository
            .all()
            .filter(
                "self.manufOrder = :manufOrder AND ((self.priority = :priority AND self.machine = :machine) OR self.priority < :priority) AND self.statusSelect BETWEEN :statusPlanned AND :statusStandby AND self.id != :operationOrderId")
            .bind("manufOrder", manufOrder)
            .bind("priority", operationOrder.getPriority())
            .bind("statusPlanned", OperationOrderRepository.STATUS_PLANNED)
            .bind("statusStandby", OperationOrderRepository.STATUS_STANDBY)
            .bind("machine", operationOrder.getMachine())
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

  protected LocalDateTime getNextOperationDate(OperationOrder operationOrder) {
    ManufOrder manufOrder = operationOrder.getManufOrder();
    OperationOrder nextOperationOrder =
        operationOrderRepository
            .all()
            .filter(
                "self.manufOrder = :manufOrder AND self.priority >= :priority AND self.statusSelect BETWEEN :statusPlanned AND :statusStandby AND self.id != :operationOrderId")
            .bind("manufOrder", manufOrder)
            .bind("priority", operationOrder.getPriority())
            .bind("statusPlanned", OperationOrderRepository.STATUS_PLANNED)
            .bind("statusStandby", OperationOrderRepository.STATUS_STANDBY)
            .bind("operationOrderId", operationOrder.getId())
            .order("priority")
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

  protected long getDuration(OperationOrder operationOrder) throws AxelorException {
    if (operationOrder.getWorkCenter() != null) {
      return operationOrderService.computeEntireCycleDuration(
          operationOrder, operationOrder.getManufOrder().getQty());
    }
    return 0;
  }

  /**
   * Set planned start and end dates.
   *
   * @param operationOrder
   * @param plannedStartDateT
   * @param plannedEndDateT
   * @return
   * @throws AxelorException
   */
  @Override
  @Transactional
  public OperationOrder setPlannedDates(
      OperationOrder operationOrder, LocalDateTime plannedStartDateT, LocalDateTime plannedEndDateT)
      throws AxelorException {

    operationOrder.setPlannedStartDateT(plannedStartDateT);
    operationOrder.setPlannedEndDateT(plannedEndDateT);

    ManufOrder manufOrder = operationOrder.getManufOrder();
    ProductionConfig productionConfig =
        productionConfigService.getProductionConfig(manufOrder.getCompany());

    if (productionConfig.getCapacity() == ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING) {
      if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_FINISHED) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(ProductionExceptionMessage.OPERATION_ORDER_ALREADY_FINISHED));
      }

      List<OperationOrder> nextOperationOrders =
          getNextOrderedOperationOrders(operationOrder).stream()
              .filter(oo -> oo.getStatusSelect() != OperationOrderRepository.STATUS_FINISHED)
              .collect(Collectors.toList());

      boolean useAsapScheduling =
          productionConfig.getScheduling()
              == ProductionConfigRepository.AS_SOON_AS_POSSIBLE_SCHEDULING;
      for (OperationOrder oo : nextOperationOrders) {
        planPlannedDatesForFiniteCapacityScheduling(oo, useAsapScheduling);
      }
      manufOrderService.updatePlannedDates(operationOrder.getManufOrder());
    }

    return computeDuration(operationOrder);
  }

  protected List<OperationOrder> getNextOrderedOperationOrders(OperationOrder operationOrder) {
    ManufOrder manufOrder = operationOrder.getManufOrder();

    return manufOrder.getOperationOrderList().stream()
        .filter(oo -> oo.getPriority() > operationOrder.getPriority() && !oo.equals(operationOrder))
        .sorted(Comparator.comparingInt(OperationOrder::getPriority))
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public OperationOrder computeDuration(OperationOrder operationOrder) {
    Long duration;

    if (operationOrder.getPlannedStartDateT() != null
        && operationOrder.getPlannedEndDateT() != null) {
      duration =
          DurationTool.getSecondsDuration(
              Duration.between(
                  operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT()));
      operationOrder.setPlannedDuration(duration);
    }

    updateRealDuration(operationOrder);

    return operationOrder;
  }

  /**
   * Set real start and end dates.
   *
   * @param operationOrder
   * @param realStartDateT
   * @param realEndDateT
   * @return
   * @throws AxelorException
   */
  @Override
  @Transactional
  public OperationOrder setRealDates(
      OperationOrder operationOrder, LocalDateTime realStartDateT, LocalDateTime realEndDateT)
      throws AxelorException {

    ManufOrder manufOrder = operationOrder.getManufOrder();
    ProductionConfig productionConfig =
        productionConfigService.getProductionConfig(manufOrder.getCompany());

    if (productionConfig.getCapacity() == ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING
        && operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_FINISHED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.OPERATION_ORDER_ALREADY_FINISHED));
    }

    operationOrder.setRealStartDateT(realStartDateT);
    operationOrder.setRealEndDateT(realEndDateT);
    return computeDuration(operationOrder);
  }

  /**
   * Compute the duration of operation order, then fill {@link OperationOrder#realDuration} with the
   * computed value.
   *
   * @param operationOrder
   */
  @Override
  public void updateRealDuration(OperationOrder operationOrder) {
    long durationLong =
        DurationTool.getSecondsDuration(operationOrderService.computeRealDuration(operationOrder));
    operationOrder.setRealDuration(durationLong);
  }
}
