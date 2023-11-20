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

import static com.axelor.apps.production.exceptions.ProductionExceptionMessage.INVALID_SCHEDULING_AND_CAPACITY_CONFIGURATION;
import static com.axelor.apps.production.exceptions.ProductionExceptionMessage.YOUR_SCHEDULING_CONFIGURATION_IS_AT_THE_LATEST_YOU_NEED_TO_FILL_THE_PLANNED_END_DATE;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.production.service.machine.MachineService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowService;
import com.axelor.apps.production.service.operationorder.planning.OperationOrderPlanningAsapFiniteCapacityService;
import com.axelor.apps.production.service.operationorder.planning.OperationOrderPlanningAsapInfiniteCapacityService;
import com.axelor.apps.production.service.operationorder.planning.OperationOrderPlanningAtTheLatestFiniteCapacityService;
import com.axelor.apps.production.service.operationorder.planning.OperationOrderPlanningAtTheLatestInfiniteCapacityService;
import com.axelor.apps.production.service.operationorder.planning.OperationOrderPlanningCommonService;
import com.axelor.apps.production.service.operationorder.planning.OperationOrderPlanningInfiniteCapacityService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.date.DurationTool;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
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
  protected OperationOrderPlanningInfiniteCapacityService
      operationOrderPlanningInfiniteCapacityService;
  protected ManufOrderWorkflowService manufOrderWorkflowService;

  @Inject
  public OperationOrderPlanningServiceImpl(
      ProductionConfigService productionConfigService,
      OperationOrderStockMoveService operationOrderStockMoveService,
      MachineService machineService,
      OperationOrderRepository operationOrderRepository,
      WeeklyPlanningService weeklyPlanningService,
      ManufOrderService manufOrderService,
      OperationOrderService operationOrderService,
      OperationOrderPlanningInfiniteCapacityService operationOrderPlanningInfiniteCapacityService,
      ManufOrderWorkflowService manufOrderWorkflowService) {
    this.productionConfigService = productionConfigService;
    this.operationOrderStockMoveService = operationOrderStockMoveService;
    this.machineService = machineService;
    this.operationOrderRepository = operationOrderRepository;
    this.weeklyPlanningService = weeklyPlanningService;
    this.manufOrderService = manufOrderService;
    this.operationOrderService = operationOrderService;
    this.operationOrderPlanningInfiniteCapacityService =
        operationOrderPlanningInfiniteCapacityService;
    this.manufOrderWorkflowService = manufOrderWorkflowService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void plan(List<OperationOrder> operationOrders) throws AxelorException {

    ManufOrder manufOrder = operationOrders.get(0).getManufOrder();
    Company company = manufOrder.getCompany();
    ProductionConfig productionConfig = productionConfigService.getProductionConfig(company);

    Integer capacity = productionConfig.getCapacity();
    Integer scheduling = productionConfig.getScheduling();

    boolean useAsapScheduling =
        productionConfig.getScheduling()
            == ProductionConfigRepository.AS_SOON_AS_POSSIBLE_SCHEDULING;

    if (!useAsapScheduling && manufOrder.getPlannedEndDateT() == null) {
      throw new AxelorException(
          manufOrder,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              YOUR_SCHEDULING_CONFIGURATION_IS_AT_THE_LATEST_YOU_NEED_TO_FILL_THE_PLANNED_END_DATE));
    }

    OperationOrderPlanningStrategy operationOrderPlanningStrategy =
        getOperationOrderPlanningStrategy(scheduling, capacity);

    OperationOrderPlanningCommonService operationOrderPlanningCommonService;
    switch (operationOrderPlanningStrategy) {
      case OperationOrderPlanningAsapFiniteCapacity:
        operationOrderPlanningCommonService =
            Beans.get(OperationOrderPlanningAsapFiniteCapacityService.class);
        break;
      case OperationOrderPlanningAsapInfiniteCapacity:
        operationOrderPlanningCommonService =
            Beans.get(OperationOrderPlanningAsapInfiniteCapacityService.class);
        break;
      case OperationOrderPlanningAtTheLatestFiniteCapacity:
        operationOrderPlanningCommonService =
            Beans.get(OperationOrderPlanningAtTheLatestFiniteCapacityService.class);
        break;
      case OperationOrderPlanningAtTheLatestInfiniteCapacity:
        operationOrderPlanningCommonService =
            Beans.get(OperationOrderPlanningAtTheLatestInfiniteCapacityService.class);
        break;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(INVALID_SCHEDULING_AND_CAPACITY_CONFIGURATION));
    }

    List<OperationOrder> sortedOperationOrders =
        useAsapScheduling
            ? operationOrderService.getSortedOperationOrderList(operationOrders)
            : operationOrderService.getReversedSortedOperationOrderList(operationOrders);

    for (OperationOrder operationOrder : sortedOperationOrders) {
      operationOrderPlanningCommonService.plan(operationOrder);
    }
    manufOrderWorkflowService.setOperationOrderMaxPriority(manufOrder);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void replan(List<OperationOrder> operationOrders) throws AxelorException {

    ManufOrder manufOrder = operationOrders.get(0).getManufOrder();
    Company company = manufOrder.getCompany();
    ProductionConfig productionConfig = productionConfigService.getProductionConfig(company);
    Integer capacity = productionConfig.getCapacity();

    if (capacity == ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING) {
      operationOrders.forEach(
          oo -> {
            oo.setPlannedStartDateT(null);
            oo.setPlannedEndDateT(null);
          });
      plan(operationOrders);
    } else if (capacity == ProductionConfigRepository.INFINITE_CAPACITY_SCHEDULING) {
      for (OperationOrder operationOrder : operationOrders) {
        operationOrder.setPlannedStartDateT(
            operationOrderService.getLastOperationDate(operationOrder));
        operationOrder.setPlannedEndDateT(
            operationOrderPlanningInfiniteCapacityService.computePlannedEndDateT(operationOrder));

        operationOrder.setPlannedDuration(
            DurationTool.getSecondsDuration(
                Duration.between(
                    operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT())));
      }
    } else {
      throw new AxelorException(
          productionConfig,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.UNRECOGNIZED_CAPACITY_FOR_COMPANY_PRODUCTION_CONFIG),
          company.getName());
    }
    operationOrders.forEach(operationOrderRepository::save);
  }

  protected OperationOrderPlanningStrategy getOperationOrderPlanningStrategy(
      Integer scheduling, Integer capacity) throws AxelorException {
    if (scheduling == ProductionConfigRepository.AS_SOON_AS_POSSIBLE_SCHEDULING) {
      if (capacity == ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING) {
        return OperationOrderPlanningStrategy.OperationOrderPlanningAsapFiniteCapacity;
      } else if (capacity == ProductionConfigRepository.INFINITE_CAPACITY_SCHEDULING) {
        return OperationOrderPlanningStrategy.OperationOrderPlanningAsapInfiniteCapacity;
      }
    } else if (scheduling == ProductionConfigRepository.AT_THE_LATEST_SCHEDULING) {
      if (capacity == ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING) {
        return OperationOrderPlanningStrategy.OperationOrderPlanningAtTheLatestFiniteCapacity;
      } else if (capacity == ProductionConfigRepository.INFINITE_CAPACITY_SCHEDULING) {
        return OperationOrderPlanningStrategy.OperationOrderPlanningAtTheLatestInfiniteCapacity;
      }
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(INVALID_SCHEDULING_AND_CAPACITY_CONFIGURATION));
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

    boolean useAsapScheduling =
        productionConfig.getScheduling()
            == ProductionConfigRepository.AS_SOON_AS_POSSIBLE_SCHEDULING;

    if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_FINISHED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.OPERATION_ORDER_ALREADY_FINISHED));
    }

    List<OperationOrder> operationOrders =
        (useAsapScheduling
                ? getNextOrderedOperationOrders(operationOrder)
                : getPreviousOrderedOperationOrders(operationOrder))
            .stream()
                .filter(oo -> oo.getStatusSelect() != OperationOrderRepository.STATUS_FINISHED)
                .collect(Collectors.toList());

    plan(operationOrders);
    manufOrderService.updatePlannedDates(operationOrder.getManufOrder());

    if (willPlannedEndDateOverflow(operationOrder)) {
      Integer capacity = productionConfig.getCapacity();
      OperationOrderPlanningCommonService operationOrderPlanningCommonService;
      if (capacity == ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING) {
        operationOrderPlanningCommonService =
            Beans.get(OperationOrderPlanningAsapFiniteCapacityService.class);
      } else {
        operationOrderPlanningCommonService =
            Beans.get(OperationOrderPlanningAsapInfiniteCapacityService.class);
      }
      for (OperationOrder oo : getNextOrderedOperationOrders(operationOrder)) {
        operationOrderPlanningCommonService.plan(oo);
      }
    }

    return computeDuration(operationOrder);
  }

  @Override
  public boolean willPlannedEndDateOverflow(OperationOrder operationOrder) throws AxelorException {
    ManufOrder manufOrder = operationOrder.getManufOrder();
    ProductionConfig productionConfig =
        productionConfigService.getProductionConfig(manufOrder.getCompany());

    if (productionConfig.getScheduling() != ProductionConfigRepository.AT_THE_LATEST_SCHEDULING) {
      return false;
    }

    List<OperationOrder> nextOperationOrders = getNextOrderedOperationOrders(operationOrder);
    if (CollectionUtils.isEmpty(nextOperationOrders)) {
      return false;
    }
    OperationOrder nextOperationOrder = nextOperationOrders.get(0);
    return nextOperationOrder.getPlannedStartDateT().isBefore(operationOrder.getPlannedEndDateT());
  }

  protected List<OperationOrder> getNextOrderedOperationOrders(OperationOrder operationOrder) {
    ManufOrder manufOrder = operationOrder.getManufOrder();

    return manufOrder.getOperationOrderList().stream()
        .filter(
            oo -> oo.getPriority() >= operationOrder.getPriority() && !oo.equals(operationOrder))
        .sorted(
            Comparator.comparingInt(OperationOrder::getPriority)
                .thenComparing(OperationOrder::getId))
        .collect(Collectors.toList());
  }

  protected List<OperationOrder> getPreviousOrderedOperationOrders(OperationOrder operationOrder) {
    ManufOrder manufOrder = operationOrder.getManufOrder();

    return manufOrder.getOperationOrderList().stream()
        .filter(
            oo -> oo.getPriority() <= operationOrder.getPriority() && !oo.equals(operationOrder))
        .sorted(
            Comparator.comparingInt(OperationOrder::getPriority)
                .thenComparing(OperationOrder::getId)
                .reversed())
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
