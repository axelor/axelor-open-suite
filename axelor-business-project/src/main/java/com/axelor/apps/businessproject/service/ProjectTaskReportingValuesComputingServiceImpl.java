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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public class ProjectTaskReportingValuesComputingServiceImpl
    implements ProjectTaskReportingValuesComputingService {

  private ProjectTaskRepository projectTaskRepo;
  private TimesheetLineRepository timesheetLineRepository;
  private AppBusinessProjectService appBusinessProjectService;

  public static final int RESULT_SCALE = 2;
  public static final int COMPUTATION_SCALE = 5;

  // AppBusinessProject config
  private Unit daysUnit;
  private Unit hoursUnit;
  private BigDecimal defaultHoursADay;

  @Inject
  public ProjectTaskReportingValuesComputingServiceImpl(
      ProjectTaskRepository projectTaskRepo,
      TimesheetLineRepository timesheetLineRepository,
      AppBusinessProjectService appBusinessProjectService) {
    this.projectTaskRepo = projectTaskRepo;
    this.timesheetLineRepository = timesheetLineRepository;
    this.appBusinessProjectService = appBusinessProjectService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void computeProjectTaskTotals(ProjectTask projectTask) throws AxelorException {

    // get AppBusinessProject config
    daysUnit = appBusinessProjectService.getDaysUnit();
    hoursUnit = appBusinessProjectService.getHoursUnit();
    Project project = projectTask.getProject();

    if (Objects.isNull(project)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          String.format(
              I18n.get(BusinessProjectExceptionMessage.PROJECT_TASK_NO_PROJECT_FOUND),
              projectTask.getName()));
    }

    defaultHoursADay = project.getNumberHoursADay();

    if (defaultHoursADay.signum() <= 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          String.format(
              I18n.get(
                  BusinessProjectExceptionMessage.PROJECT_CONFIG_DEFAULT_HOURS_PER_DAY_MISSING),
              projectTask.getName()));
    }

    computeProjectTaskTimes(projectTask);
    computeFinancialReporting(projectTask, project);

    projectTaskRepo.save(projectTask);
  }

  /**
   * Compute plannedTime and spentTime
   *
   * @param projectTask
   * @throws AxelorException
   */
  protected void computeProjectTaskTimes(ProjectTask projectTask) throws AxelorException {
    BigDecimal spentTime = getTaskSpentTime(projectTask);

    BigDecimal plannedTime =
        projectTask.getProjectPlanningTimeList().stream()
            .map(ProjectPlanningTime::getPlannedTime)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // compute task children
    List<ProjectTask> projectTaskList = projectTask.getProjectTaskList();
    for (ProjectTask task : projectTaskList) {
      computeProjectTaskTimes(task);
      // retrieving information from children
      plannedTime = plannedTime.add(task.getPlannedTime());
      spentTime = spentTime.add(task.getSpentTime());
    }

    projectTask.setPlannedTime(plannedTime);
    projectTask.setSpentTime(spentTime);
  }

  /**
   * get specific task timeSpent (without children)
   *
   * @param projectTask
   * @return
   */
  protected BigDecimal getTaskSpentTime(ProjectTask projectTask) {
    List<TimesheetLine> timesheetLines = getValidatedTimesheetLinesForProjectTask(projectTask);
    Unit timeUnit = projectTask.getTimeUnit();
    BigDecimal spentTime = BigDecimal.ZERO;

    for (TimesheetLine timeSheetLine : timesheetLines) {
      spentTime = spentTime.add(convertTimesheetLineDurationToTimeUnit(timeSheetLine, timeUnit));
    }

    return spentTime;
  }

  /**
   * Compute financial information for reporting
   *
   * @param projectTask
   * @throws AxelorException
   */
  protected void computeFinancialReporting(ProjectTask projectTask, Project project)
      throws AxelorException {

    projectTask.setTurnover(
        projectTask
            .getSoldTime()
            .multiply(projectTask.getUnitPrice())
            .setScale(RESULT_SCALE, RoundingMode.HALF_UP));

    SaleOrderLine saleOrderLine = projectTask.getSaleOrderLine();
    Product product = projectTask.getProduct();

    Unit projectTaskUnit = projectTask.getTimeUnit();

    if (projectTask.getSoldTime().signum() <= 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(BusinessProjectExceptionMessage.PROJECT_TASK_SOLD_TIME_ERROR),
              projectTask.getName()));
    }

    // sale order line unit not compatible with BusinessProject configuration
    if (saleOrderLine != null
        && !daysUnit.equals(saleOrderLine.getUnit())
        && !hoursUnit.equals(saleOrderLine.getUnit())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(
                  BusinessProjectExceptionMessage.PROJECT_TASK_PRODUCT_SALE_ORDER_LINE_UNIT_ERROR),
              product.getName()));
    }

    // stock unit not compatible with BusinessProject configuration
    if (saleOrderLine == null
        && product != null
        && !daysUnit.equals(product.getUnit())
        && !hoursUnit.equals(product.getUnit())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(BusinessProjectExceptionMessage.PROJECT_TASK_PRODUCT_STOCK_UNIT_ERROR),
              product.getName()));
    }

    // Initial
    BigDecimal initialCosts = BigDecimal.ZERO;
    BigDecimal forecastUnitCost = BigDecimal.ZERO;
    if (saleOrderLine != null) {
      initialCosts = saleOrderLine.getSubTotalCostPrice();
      forecastUnitCost =
          saleOrderLine
              .getSubTotalCostPrice()
              .divide(projectTask.getSoldTime(), RESULT_SCALE, RoundingMode.HALF_UP);
    } else if (product != null) {
      BigDecimal convertedProductPrice = getProductConvertedPrice(product, projectTaskUnit);
      initialCosts =
          projectTask
              .getSoldTime()
              .multiply(convertedProductPrice)
              .setScale(RESULT_SCALE, RoundingMode.HALF_UP);
      forecastUnitCost = convertedProductPrice;
    }
    projectTask.setInitialCosts(initialCosts);
    projectTask.setForecastUnitCost(forecastUnitCost);

    projectTask.setInitialMargin(projectTask.getTurnover().subtract(projectTask.getInitialCosts()));
    if (initialCosts.signum() > 0) {
      projectTask.setInitialMarkup(
          getPercentValue(
              projectTask
                  .getInitialMargin()
                  .divide(projectTask.getInitialCosts(), COMPUTATION_SCALE, RoundingMode.HALF_UP)));
    }

    // Real
    BigDecimal progress =
        projectTask
            .getSpentTime()
            .divide(projectTask.getUpdatedTime(), RESULT_SCALE, RoundingMode.HALF_UP);
    projectTask.setRealTurnover(
        progress.multiply(projectTask.getTurnover()).setScale(RESULT_SCALE, RoundingMode.HALF_UP));

    BigDecimal realCosts = computeRealCosts(projectTask, project);

    projectTask.setRealCosts(realCosts);
    projectTask.setRealMargin(projectTask.getRealTurnover().subtract(projectTask.getRealCosts()));

    BigDecimal realMarkup = BigDecimal.ZERO;
    if (projectTask.getRealCosts().signum() > 0) {
      realMarkup =
          getPercentValue(
              projectTask
                  .getRealMargin()
                  .divide(projectTask.getRealCosts(), COMPUTATION_SCALE, RoundingMode.HALF_UP));
    }
    projectTask.setRealMarkup(realMarkup);

    // Forecast
    projectTask.setForecastCosts(
        projectTask
            .getRealCosts()
            .add(
                projectTask
                    .getUpdatedTime()
                    .subtract(projectTask.getSpentTime())
                    .multiply(projectTask.getForecastUnitCost()))
            .setScale(RESULT_SCALE, RoundingMode.HALF_UP));
    projectTask.setForecastMargin(
        projectTask.getTurnover().subtract(projectTask.getForecastCosts()));
    if (projectTask.getForecastCosts().signum() > 0) {
      projectTask.setForecastMarkup(
          getPercentValue(
              projectTask
                  .getForecastMargin()
                  .divide(
                      projectTask.getForecastCosts(), COMPUTATION_SCALE, RoundingMode.HALF_UP)));
    }
  }

  protected BigDecimal computeRealCosts(ProjectTask projectTask, Project project)
      throws AxelorException {
    BigDecimal unitCost = computeUnitCost(projectTask, project);
    projectTask.setUnitCost(unitCost);

    BigDecimal timeSpent = getTaskSpentTime(projectTask);

    BigDecimal realCost = timeSpent.multiply(unitCost).setScale(RESULT_SCALE, RoundingMode.HALF_UP);

    // add subtask real cost
    for (ProjectTask task : projectTask.getProjectTaskList()) {
      realCost = realCost.add(computeRealCosts(task, project));
    }
    projectTask.setRealCosts(realCost);
    return realCost;
  }

  /**
   * compute unit cost depending on Project spentTimeCostComputationMethod
   *
   * @param projectTask
   * @return
   * @throws AxelorException
   */
  protected BigDecimal computeUnitCost(ProjectTask projectTask, Project project)
      throws AxelorException {
    BigDecimal unitCost = BigDecimal.ZERO;

    Unit timeUnit = projectTask.getTimeUnit();

    Integer spentTimeCostComputationMethod = project.getSpentTimeCostComputationMethod();

    Product product = projectTask.getProduct();
    switch (spentTimeCostComputationMethod) {
      case ProjectRepository.COMPUTATION_METHOD_SALE_ORDER:
        if (projectTask.getSaleOrderLine() != null) {
          unitCost =
              projectTask
                  .getSaleOrderLine()
                  .getSubTotalCostPrice()
                  .divide(
                      projectTask.getSaleOrderLine().getQty(), RESULT_SCALE, RoundingMode.HALF_UP);
        } else if (product != null) {
          unitCost = getProductConvertedPrice(product, timeUnit);
        }
        break;
      case ProjectRepository.COMPUTATION_METHOD_PRODUCT:
        if (product != null) {
          unitCost = getProductConvertedPrice(product, timeUnit);
        }
        break;
      case ProjectRepository.COMPUTATION_METHOD_EMPLOYEE:
        unitCost = getAverageHourCostFromTimesheetLines(projectTask);

        if (daysUnit.equals(timeUnit)) {
          unitCost = unitCost.multiply(defaultHoursADay);
        }
        break;
      default:
        break;
    }
    return unitCost;
  }

  /**
   * Convert TimesheetLine duration to given time unit
   *
   * @param timesheetLine
   * @param timeUnit
   * @return
   * @throws AxelorException
   */
  protected BigDecimal convertTimesheetLineDurationToTimeUnit(
      TimesheetLine timesheetLine, Unit timeUnit) {
    String timeLoggingUnit = timesheetLine.getTimesheet().getTimeLoggingPreferenceSelect();
    BigDecimal duration = timesheetLine.getDuration();
    BigDecimal convertedDuration = BigDecimal.ZERO;

    switch (timeLoggingUnit) {
      case EmployeeRepository.TIME_PREFERENCE_DAYS:
        if (timeUnit.equals(daysUnit)) {
          convertedDuration = duration;
        }
        if (timeUnit.equals(hoursUnit)) {
          convertedDuration = duration.multiply(defaultHoursADay);
        }
        break;
      case EmployeeRepository.TIME_PREFERENCE_HOURS:
        if (timeUnit.equals(hoursUnit)) {
          convertedDuration = duration;
        }
        if (timeUnit.equals(daysUnit)) {
          convertedDuration = duration.divide(defaultHoursADay, RESULT_SCALE, RoundingMode.HALF_UP);
        }
        break;
      case EmployeeRepository.TIME_PREFERENCE_MINUTES:
        // convert to hours
        convertedDuration = duration.divide(new BigDecimal(60), RESULT_SCALE, RoundingMode.HALF_UP);
        if (timeUnit.equals(daysUnit)) {
          convertedDuration = duration.divide(defaultHoursADay, RESULT_SCALE, RoundingMode.HALF_UP);
        }
        break;
      default:
        break;
    }

    return convertedDuration;
  }

  /**
   * get hour cost average of task timesheetLine
   *
   * @param projectTask
   * @return
   * @throws AxelorException
   */
  protected BigDecimal getAverageHourCostFromTimesheetLines(ProjectTask projectTask) {
    BigDecimal totalCost = BigDecimal.ZERO;
    BigDecimal timeConsidered = BigDecimal.ZERO;
    List<TimesheetLine> timesheetLines = getValidatedTimesheetLinesForProjectTask(projectTask);
    for (TimesheetLine timesheetLine : timesheetLines) {
      BigDecimal hourlyRate = timesheetLine.getEmployee().getHourlyRate();

      // dot not count if no hourlyRate
      if (hourlyRate == null || hourlyRate.signum() <= 0) {
        continue;
      }
      // convert to hours
      BigDecimal duration = convertTimesheetLineDurationToTimeUnit(timesheetLine, hoursUnit);
      timeConsidered = timeConsidered.add(duration);
      totalCost = totalCost.add(hourlyRate.multiply(duration));
    }

    if (timeConsidered.signum() > 0) {
      return totalCost.divide(timeConsidered, RESULT_SCALE, RoundingMode.HALF_UP);
    } else {
      return BigDecimal.ZERO;
    }
  }

  /**
   * Convert Product Price if stock unit is not the same as the task unit
   *
   * @param product
   * @param projectTaskUnit
   * @return
   */
  protected BigDecimal getProductConvertedPrice(Product product, Unit projectTaskUnit) {
    BigDecimal convertedProductPrice = product.getCostPrice();
    if (daysUnit.equals(projectTaskUnit) && hoursUnit.equals(product.getUnit())) {
      convertedProductPrice = convertedProductPrice.multiply(defaultHoursADay);
    } else if (hoursUnit.equals(projectTaskUnit) && daysUnit.equals(product.getUnit())) {
      convertedProductPrice =
          convertedProductPrice.divide(defaultHoursADay, RESULT_SCALE, RoundingMode.HALF_UP);
    }

    return convertedProductPrice;
  }

  /**
   * list of validated
   *
   * @param projectTask
   * @return validated TimesheetLine for given ProjectTask
   */
  protected List<TimesheetLine> getValidatedTimesheetLinesForProjectTask(ProjectTask projectTask) {
    return timesheetLineRepository
        .all()
        .filter("self.timesheet.statusSelect = :status AND self.projectTask = :projectTask")
        .bind("status", TimesheetRepository.STATUS_VALIDATED)
        .bind("projectTask", projectTask)
        .fetch();
  }

  /**
   * get percent value for given bigDecimal value
   *
   * @param decimalValue
   * @return
   */
  protected BigDecimal getPercentValue(BigDecimal decimalValue) {
    return new BigDecimal("100")
        .multiply(decimalValue)
        .setScale(RESULT_SCALE, RoundingMode.HALF_UP);
  }
}
