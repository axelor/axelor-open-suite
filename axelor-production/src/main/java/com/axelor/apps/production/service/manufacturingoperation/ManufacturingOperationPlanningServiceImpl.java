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
package com.axelor.apps.production.service.manufacturingoperation;

import static com.axelor.apps.production.exceptions.ProductionExceptionMessage.INVALID_SCHEDULING_AND_CAPACITY_CONFIGURATION;
import static com.axelor.apps.production.exceptions.ProductionExceptionMessage.YOUR_SCHEDULING_CONFIGURATION_IS_AT_THE_LATEST_YOU_NEED_TO_FILL_THE_PLANNED_END_DATE;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ManufacturingOperation;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.production.db.repo.ManufacturingOperationRepository;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.production.service.machine.MachineService;
import com.axelor.apps.production.service.manufacturingoperation.planning.ManufacturingOperationPlanningAsapFiniteCapacityService;
import com.axelor.apps.production.service.manufacturingoperation.planning.ManufacturingOperationPlanningAsapInfiniteCapacityService;
import com.axelor.apps.production.service.manufacturingoperation.planning.ManufacturingOperationPlanningAtTheLatestFiniteCapacityService;
import com.axelor.apps.production.service.manufacturingoperation.planning.ManufacturingOperationPlanningAtTheLatestInfiniteCapacityService;
import com.axelor.apps.production.service.manufacturingoperation.planning.ManufacturingOperationPlanningCommonService;
import com.axelor.apps.production.service.manufacturingoperation.planning.ManufacturingOperationPlanningInfiniteCapacityService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.helpers.date.DurationHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ManufacturingOperationPlanningServiceImpl
    implements ManufacturingOperationPlanningService {

  protected ProductionConfigService productionConfigService;
  protected ManufacturingOperationStockMoveService manufacturingOperationStockMoveService;
  protected WeeklyPlanningService weeklyPlanningService;
  protected MachineService machineService;
  protected ManufacturingOperationRepository manufacturingOperationRepository;
  protected ManufOrderService manufOrderService;
  protected ManufacturingOperationService manufacturingOperationService;
  protected ManufacturingOperationPlanningInfiniteCapacityService
      manufacturingOperationPlanningInfiniteCapacityService;
  protected ManufOrderWorkflowService manufOrderWorkflowService;

  @Inject
  public ManufacturingOperationPlanningServiceImpl(
      ProductionConfigService productionConfigService,
      ManufacturingOperationStockMoveService manufacturingOperationStockMoveService,
      MachineService machineService,
      ManufacturingOperationRepository manufacturingOperationRepository,
      WeeklyPlanningService weeklyPlanningService,
      ManufOrderService manufOrderService,
      ManufacturingOperationService manufacturingOperationService,
      ManufacturingOperationPlanningInfiniteCapacityService
          manufacturingOperationPlanningInfiniteCapacityService,
      ManufOrderWorkflowService manufOrderWorkflowService) {
    this.productionConfigService = productionConfigService;
    this.manufacturingOperationStockMoveService = manufacturingOperationStockMoveService;
    this.machineService = machineService;
    this.manufacturingOperationRepository = manufacturingOperationRepository;
    this.weeklyPlanningService = weeklyPlanningService;
    this.manufOrderService = manufOrderService;
    this.manufacturingOperationService = manufacturingOperationService;
    this.manufacturingOperationPlanningInfiniteCapacityService =
        manufacturingOperationPlanningInfiniteCapacityService;
    this.manufOrderWorkflowService = manufOrderWorkflowService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void plan(List<ManufacturingOperation> manufacturingOperations) throws AxelorException {
    if (CollectionUtils.isEmpty(manufacturingOperations)) {
      return;
    }

    ManufOrder manufOrder = manufacturingOperations.get(0).getManufOrder();
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

    ManufacturingOperationPlanningStrategy manufacturingOperationPlanningStrategy =
        getManufacturingOperationPlanningStrategy(scheduling, capacity);

    ManufacturingOperationPlanningCommonService manufacturingOperationPlanningCommonService;
    switch (manufacturingOperationPlanningStrategy) {
      case ManufacturingOperationPlanningAsapFiniteCapacity:
        manufacturingOperationPlanningCommonService =
            Beans.get(ManufacturingOperationPlanningAsapFiniteCapacityService.class);
        break;
      case ManufacturingOperationPlanningAsapInfiniteCapacity:
        manufacturingOperationPlanningCommonService =
            Beans.get(ManufacturingOperationPlanningAsapInfiniteCapacityService.class);
        break;
      case ManufacturingOperationPlanningAtTheLatestFiniteCapacity:
        manufacturingOperationPlanningCommonService =
            Beans.get(ManufacturingOperationPlanningAtTheLatestFiniteCapacityService.class);
        break;
      case ManufacturingOperationPlanningAtTheLatestInfiniteCapacity:
        manufacturingOperationPlanningCommonService =
            Beans.get(ManufacturingOperationPlanningAtTheLatestInfiniteCapacityService.class);
        break;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(INVALID_SCHEDULING_AND_CAPACITY_CONFIGURATION));
    }

    List<ManufacturingOperation> sortedManufacturingOperations =
        useAsapScheduling
            ? manufacturingOperationService.getSortedManufacturingOperationList(
                manufacturingOperations)
            : manufacturingOperationService.getReversedSortedManufacturingOperationList(
                manufacturingOperations);

    for (ManufacturingOperation manufacturingOperation : sortedManufacturingOperations) {
      manufacturingOperationPlanningCommonService.plan(manufacturingOperation);
    }
    manufOrderWorkflowService.setManufacturingOperationMaxPriority(manufOrder);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void replan(List<ManufacturingOperation> manufacturingOperations) throws AxelorException {
    if (CollectionUtils.isEmpty(manufacturingOperations)) {
      return;
    }

    ManufOrder manufOrder = manufacturingOperations.get(0).getManufOrder();
    Company company = manufOrder.getCompany();
    ProductionConfig productionConfig = productionConfigService.getProductionConfig(company);
    Integer capacity = productionConfig.getCapacity();

    if (capacity == ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING) {
      manufacturingOperations.forEach(
          oo -> {
            oo.setPlannedStartDateT(null);
            oo.setPlannedEndDateT(null);
          });
      plan(manufacturingOperations);
    } else if (capacity == ProductionConfigRepository.INFINITE_CAPACITY_SCHEDULING) {
      for (ManufacturingOperation manufacturingOperation : manufacturingOperations) {
        manufacturingOperation.setPlannedStartDateT(
            manufacturingOperationService.getLastOperationDate(manufacturingOperation));
        manufacturingOperation.setPlannedEndDateT(
            manufacturingOperationPlanningInfiniteCapacityService.computePlannedEndDateT(
                manufacturingOperation));

        manufacturingOperation.setPlannedDuration(
            DurationHelper.getSecondsDuration(
                Duration.between(
                    manufacturingOperation.getPlannedStartDateT(),
                    manufacturingOperation.getPlannedEndDateT())));
      }
    } else {
      throw new AxelorException(
          productionConfig,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.UNRECOGNIZED_CAPACITY_FOR_COMPANY_PRODUCTION_CONFIG),
          company.getName());
    }
    manufacturingOperations.forEach(manufacturingOperationRepository::save);
  }

  protected ManufacturingOperationPlanningStrategy getManufacturingOperationPlanningStrategy(
      Integer scheduling, Integer capacity) throws AxelorException {
    if (scheduling == ProductionConfigRepository.AS_SOON_AS_POSSIBLE_SCHEDULING) {
      if (capacity == ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING) {
        return ManufacturingOperationPlanningStrategy
            .ManufacturingOperationPlanningAsapFiniteCapacity;
      } else if (capacity == ProductionConfigRepository.INFINITE_CAPACITY_SCHEDULING) {
        return ManufacturingOperationPlanningStrategy
            .ManufacturingOperationPlanningAsapInfiniteCapacity;
      }
    } else if (scheduling == ProductionConfigRepository.AT_THE_LATEST_SCHEDULING) {
      if (capacity == ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING) {
        return ManufacturingOperationPlanningStrategy
            .ManufacturingOperationPlanningAtTheLatestFiniteCapacity;
      } else if (capacity == ProductionConfigRepository.INFINITE_CAPACITY_SCHEDULING) {
        return ManufacturingOperationPlanningStrategy
            .ManufacturingOperationPlanningAtTheLatestInfiniteCapacity;
      }
    }
    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(INVALID_SCHEDULING_AND_CAPACITY_CONFIGURATION));
  }

  /**
   * Set planned start and end dates.
   *
   * @param manufacturingOperation
   * @param plannedStartDateT
   * @param plannedEndDateT
   * @return
   * @throws AxelorException
   */
  @Override
  @Transactional
  public ManufacturingOperation setPlannedDates(
      ManufacturingOperation manufacturingOperation,
      LocalDateTime plannedStartDateT,
      LocalDateTime plannedEndDateT)
      throws AxelorException {

    manufacturingOperation.setPlannedStartDateT(plannedStartDateT);
    manufacturingOperation.setPlannedEndDateT(plannedEndDateT);

    ManufOrder manufOrder = manufacturingOperation.getManufOrder();
    ProductionConfig productionConfig =
        productionConfigService.getProductionConfig(manufOrder.getCompany());

    boolean useAsapScheduling =
        productionConfig.getScheduling()
            == ProductionConfigRepository.AS_SOON_AS_POSSIBLE_SCHEDULING;

    if (manufacturingOperation.getStatusSelect()
        == ManufacturingOperationRepository.STATUS_FINISHED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.MANUFACTURING_OPERATION_ALREADY_FINISHED));
    }

    List<ManufacturingOperation> manufacturingOperations =
        (useAsapScheduling
                ? getNextOrderedManufacturingOperations(manufacturingOperation)
                : getPreviousOrderedManufacturingOperations(manufacturingOperation))
            .stream()
                .filter(
                    oo -> oo.getStatusSelect() != ManufacturingOperationRepository.STATUS_FINISHED)
                .collect(Collectors.toList());

    plan(manufacturingOperations);
    manufOrderService.updatePlannedDates(manufacturingOperation.getManufOrder());

    if (willPlannedEndDateOverflow(manufacturingOperation)) {
      Integer capacity = productionConfig.getCapacity();
      ManufacturingOperationPlanningCommonService manufacturingOperationPlanningCommonService;
      if (capacity == ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING) {
        manufacturingOperationPlanningCommonService =
            Beans.get(ManufacturingOperationPlanningAsapFiniteCapacityService.class);
      } else {
        manufacturingOperationPlanningCommonService =
            Beans.get(ManufacturingOperationPlanningAsapInfiniteCapacityService.class);
      }
      for (ManufacturingOperation oo :
          getNextOrderedManufacturingOperations(manufacturingOperation)) {
        manufacturingOperationPlanningCommonService.plan(oo);
      }
    }

    return computeDuration(manufacturingOperation);
  }

  @Override
  public boolean willPlannedEndDateOverflow(ManufacturingOperation manufacturingOperation)
      throws AxelorException {
    ManufOrder manufOrder = manufacturingOperation.getManufOrder();
    ProductionConfig productionConfig =
        productionConfigService.getProductionConfig(manufOrder.getCompany());

    if (productionConfig.getScheduling() != ProductionConfigRepository.AT_THE_LATEST_SCHEDULING) {
      return false;
    }

    List<ManufacturingOperation> nextManufacturingOperations =
        getNextOrderedManufacturingOperations(manufacturingOperation);
    if (CollectionUtils.isEmpty(nextManufacturingOperations)) {
      return false;
    }
    ManufacturingOperation nextManufacturingOperation = nextManufacturingOperations.get(0);
    return nextManufacturingOperation
        .getPlannedStartDateT()
        .isBefore(manufacturingOperation.getPlannedEndDateT());
  }

  protected List<ManufacturingOperation> getNextOrderedManufacturingOperations(
      ManufacturingOperation manufacturingOperation) {
    ManufOrder manufOrder = manufacturingOperation.getManufOrder();

    return manufOrder.getManufacturingOperationList().stream()
        .filter(
            oo ->
                oo.getPriority() >= manufacturingOperation.getPriority()
                    && !oo.equals(manufacturingOperation))
        .sorted(
            Comparator.comparingInt(ManufacturingOperation::getPriority)
                .thenComparing(ManufacturingOperation::getId))
        .collect(Collectors.toList());
  }

  protected List<ManufacturingOperation> getPreviousOrderedManufacturingOperations(
      ManufacturingOperation manufacturingOperation) {
    ManufOrder manufOrder = manufacturingOperation.getManufOrder();

    return manufOrder.getManufacturingOperationList().stream()
        .filter(
            oo ->
                oo.getPriority() <= manufacturingOperation.getPriority()
                    && !oo.equals(manufacturingOperation))
        .sorted(
            Comparator.comparingInt(ManufacturingOperation::getPriority)
                .thenComparing(ManufacturingOperation::getId)
                .reversed())
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public ManufacturingOperation computeDuration(ManufacturingOperation manufacturingOperation) {
    Long duration;

    if (manufacturingOperation.getPlannedStartDateT() != null
        && manufacturingOperation.getPlannedEndDateT() != null) {
      duration =
          DurationHelper.getSecondsDuration(
              Duration.between(
                  manufacturingOperation.getPlannedStartDateT(),
                  manufacturingOperation.getPlannedEndDateT()));
      manufacturingOperation.setPlannedDuration(duration);
    }

    updateRealDuration(manufacturingOperation);

    return manufacturingOperation;
  }

  /**
   * Set real start and end dates.
   *
   * @param manufacturingOperation
   * @param realStartDateT
   * @param realEndDateT
   * @return
   * @throws AxelorException
   */
  @Override
  @Transactional
  public ManufacturingOperation setRealDates(
      ManufacturingOperation manufacturingOperation,
      LocalDateTime realStartDateT,
      LocalDateTime realEndDateT)
      throws AxelorException {

    ManufOrder manufOrder = manufacturingOperation.getManufOrder();
    ProductionConfig productionConfig =
        productionConfigService.getProductionConfig(manufOrder.getCompany());

    if (productionConfig.getCapacity() == ProductionConfigRepository.FINITE_CAPACITY_SCHEDULING
        && manufacturingOperation.getStatusSelect()
            == ManufacturingOperationRepository.STATUS_FINISHED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.MANUFACTURING_OPERATION_ALREADY_FINISHED));
    }

    manufacturingOperation.setRealStartDateT(realStartDateT);
    manufacturingOperation.setRealEndDateT(realEndDateT);
    return computeDuration(manufacturingOperation);
  }

  /**
   * Compute the duration of operation order, then fill {@link ManufacturingOperation#realDuration}
   * with the computed value.
   *
   * @param manufacturingOperation
   */
  @Override
  public void updateRealDuration(ManufacturingOperation manufacturingOperation) {
    long durationLong =
        DurationHelper.getSecondsDuration(
            manufacturingOperationService.computeRealDuration(manufacturingOperation));
    manufacturingOperation.setRealDuration(durationLong);
  }
}
