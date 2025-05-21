/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.address.AddressService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.businessproject.service.projecttask.ProjectTaskBusinessProjectService;
import com.axelor.apps.businessproject.service.projecttask.ProjectTaskReportingValuesComputingService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectHistoryLine;
import com.axelor.apps.project.db.ProjectStatus;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTemplate;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectStatusRepository;
import com.axelor.apps.project.db.repo.WikiRepository;
import com.axelor.apps.project.exception.ProjectExceptionMessage;
import com.axelor.apps.project.service.ProjectCreateTaskService;
import com.axelor.apps.project.service.ProjectNameComputeService;
import com.axelor.apps.project.service.ProjectServiceImpl;
import com.axelor.apps.project.service.ProjectTimeUnitService;
import com.axelor.apps.project.service.ResourceBookingService;
import com.axelor.apps.project.service.UnitConversionForProjectService;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderGeneratorService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderStockLocationService;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.utils.helpers.date.LocalDateHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProjectBusinessServiceImpl extends ProjectServiceImpl
    implements ProjectBusinessService {

  protected PartnerService partnerService;
  protected AddressService addressService;
  protected AppBusinessProjectService appBusinessProjectService;
  protected ProjectTaskBusinessProjectService projectTaskBusinessProjectService;
  protected ProjectTaskReportingValuesComputingService projectTaskReportingValuesComputingService;
  protected AppBaseService appBaseService;
  protected InvoiceRepository invoiceRepository;
  protected UnitConversionForProjectService unitConversionForProjectService;
  protected SaleOrderStockLocationService saleOrderStockLocationService;
  protected ProjectTimeUnitService projectTimeUnitService;
  protected SaleOrderGeneratorService saleOrderGeneratorService;

  public static final int BIG_DECIMAL_SCALE = 2;
  public static final String FA_LEVEL_UP = "arrow-90deg-up";
  public static final String FA_LEVEL_DOWN = "arrow-90deg-down";
  public static final String ICON_EQUAL = "equal";

  @Inject
  public ProjectBusinessServiceImpl(
      ProjectRepository projectRepository,
      ProjectStatusRepository projectStatusRepository,
      AppProjectService appProjectService,
      ProjectCreateTaskService projectCreateTaskService,
      WikiRepository wikiRepo,
      ResourceBookingService resourceBookingService,
      ProjectNameComputeService projectNameComputeService,
      PartnerService partnerService,
      AddressService addressService,
      AppBusinessProjectService appBusinessProjectService,
      ProjectTaskBusinessProjectService projectTaskBusinessProjectService,
      ProjectTaskReportingValuesComputingService projectTaskReportingValuesComputingService,
      AppBaseService appBaseService,
      InvoiceRepository invoiceRepository,
      UnitConversionForProjectService unitConversionForProjectService,
      SaleOrderStockLocationService saleOrderStockLocationService,
      ProjectTimeUnitService projectTimeUnitService,
      SaleOrderGeneratorService saleOrderGeneratorService) {
    super(
        projectRepository,
        projectStatusRepository,
        appProjectService,
        projectCreateTaskService,
        wikiRepo,
        resourceBookingService,
        projectNameComputeService);
    this.partnerService = partnerService;
    this.addressService = addressService;
    this.appBusinessProjectService = appBusinessProjectService;
    this.projectTaskBusinessProjectService = projectTaskBusinessProjectService;
    this.projectTaskReportingValuesComputingService = projectTaskReportingValuesComputingService;
    this.appBaseService = appBaseService;
    this.invoiceRepository = invoiceRepository;
    this.unitConversionForProjectService = unitConversionForProjectService;
    this.saleOrderStockLocationService = saleOrderStockLocationService;
    this.projectTimeUnitService = projectTimeUnitService;
    this.saleOrderGeneratorService = saleOrderGeneratorService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public SaleOrder generateQuotation(Project project) throws AxelorException {

    Partner clientPartner = project.getClientPartner();
    Partner contactPartner = project.getContactPartner();
    if (contactPartner == null && clientPartner.getContactPartnerSet().size() == 1) {
      contactPartner = clientPartner.getContactPartnerSet().iterator().next();
    }

    Company company = project.getCompany();

    Currency currency = getCurrency(project, clientPartner, company);

    SaleOrder order =
        saleOrderGeneratorService.createSaleOrder(
            clientPartner, company, contactPartner, currency, null);

    order.setProject(projectRepository.find(project.getId()));

    if (project.getPriceList() != null) {
      order.setPriceList(project.getPriceList());
    }

    if (order.getPriceList() != null) {
      order.setHideDiscount(order.getPriceList().getHideDiscount());
    }

    return Beans.get(SaleOrderRepository.class).save(order);
  }

  protected Currency getCurrency(Project project, Partner clientPartner, Company company) {
    Currency currency;
    if (project.getCurrency() != null) {
      currency = project.getCurrency();
    } else if (clientPartner.getCurrency() != null) {
      currency = clientPartner.getCurrency();
    } else {
      currency = company.getCurrency();
    }
    return currency;
  }

  /**
   * Generate project form SaleOrder and set bi-directional.
   *
   * @param saleOrder The order of origin.
   * @return The project generated.
   */
  @Override
  public Project generateProject(SaleOrder saleOrder) throws AxelorException {
    Project project = projectRepository.findByName(saleOrder.getFullName() + "_project");
    project =
        project == null
            ? this.generateProject(
                null,
                saleOrder.getFullName() + "_project",
                saleOrder.getSalespersonUser(),
                saleOrder.getCompany(),
                saleOrder.getClientPartner())
            : project;
    saleOrder.setProject(project);
    project.setDescription(saleOrder.getDescription());
    return project;
  }

  @Override
  public Project generateProject(
      Project parentProject,
      String fullName,
      User assignedTo,
      Company company,
      Partner clientPartner)
      throws AxelorException {
    Project project =
        super.generateProject(parentProject, fullName, assignedTo, company, clientPartner);

    if (!appBusinessProjectService.isApp("business-project")) {
      return project;
    }

    if (assignedTo != null) {
      project.addMembersUserSetItem(assignedTo);
    }
    if (parentProject != null) {
      project.setManageTimeSpent(parentProject.getManageTimeSpent());
    }
    project.setCompany(company);
    if (parentProject != null && parentProject.getIsInvoicingTimesheet()) {
      project.setIsInvoicingTimesheet(true);
    }

    project.setNumberHoursADay(appBaseService.getDailyWorkHours());
    project.setProjectTimeUnit(appBaseService.getUnitDays());
    return project;
  }

  @Override
  public Project generatePhaseProject(SaleOrderLine saleOrderLine, Project parent)
      throws AxelorException {
    return generateProject(
        parent,
        saleOrderLine.getFullName(),
        saleOrderLine.getSaleOrder().getSalespersonUser(),
        parent.getCompany(),
        parent.getClientPartner());
  }

  @Override
  public Project generateProject(
      ProjectTemplate projectTemplate, String projectCode, Partner clientPartner) {
    Project project = super.generateProject(projectTemplate, projectCode, clientPartner);

    project.setCompany(projectTemplate.getCompany());
    if (projectTemplate.getIsBusinessProject()) {
      project.setCurrency(clientPartner.getCurrency());
      if (clientPartner.getPartnerAddressList() != null
          && !clientPartner.getPartnerAddressList().isEmpty()) {
        project.setCustomerAddress(
            clientPartner.getPartnerAddressList().iterator().next().getAddress());
      }
      if (clientPartner.getSalePartnerPriceList() != null
          && clientPartner.getSalePartnerPriceList().getPriceListSet() != null
          && !clientPartner.getSalePartnerPriceList().getPriceListSet().isEmpty()) {
        project.setPriceList(
            clientPartner.getSalePartnerPriceList().getPriceListSet().iterator().next());
      }
      project.setIsInvoicingExpenses(projectTemplate.getIsInvoicingExpenses());
      project.setIsInvoicingPurchases(projectTemplate.getIsInvoicingPurchases());
      project.setInvoicingComment(projectTemplate.getInvoicingComment());
      project.setIsBusinessProject(projectTemplate.getIsBusinessProject());

      if (projectTemplate.getCompany() == null
          && !ObjectUtils.isEmpty(clientPartner.getCompanySet())
          && clientPartner.getCompanySet().size() == 1) {
        project.setCompany(clientPartner.getCompanySet().iterator().next());
      }
    }
    project.setProjectFolderSet(new HashSet<>(projectTemplate.getProjectFolderSet()));

    return project;
  }

  @Override
  public void computeProjectTotals(Project project) throws AxelorException {

    project = projectRepository.find(project.getId());
    List<ProjectTask> projectTaskList =
        project.getProjectTaskList().stream()
            .filter(projectTask -> projectTask.getParentTask() == null)
            .collect(Collectors.toList());
    for (ProjectTask projectTask : projectTaskList) {
      projectTaskReportingValuesComputingService.computeProjectTaskTotals(projectTask);
    }

    computeProjectReportingValues(project, projectTaskList);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void computeProjectReportingValues(Project project, List<ProjectTask> projectTaskList)
      throws AxelorException {
    computeTimeFollowUp(project, projectTaskList);
    computeFinancialFollowUp(project, projectTaskList);
    computeInvoicingFollowUp(project);
    projectRepository.save(project);
  }

  protected void computeTimeFollowUp(Project project, List<ProjectTask> projectTaskList)
      throws AxelorException {
    BigDecimal totalSoldTime = BigDecimal.ZERO;
    BigDecimal totalUpdatedTime = BigDecimal.ZERO;
    BigDecimal totalPlannedTime = BigDecimal.ZERO;
    BigDecimal totalSpentTime = BigDecimal.ZERO;

    Unit projectUnit = projectTimeUnitService.getProjectDefaultHoursTimeUnit(project);

    for (ProjectTask projectTask : projectTaskList) {
      Unit projectTaskUnit = projectTimeUnitService.getTaskDefaultHoursTimeUnit(projectTask);

      if (!projectTaskBusinessProjectService.isTimeUnitValid(projectTaskUnit)) {
        continue;
      }
      totalSoldTime =
          totalSoldTime.add(
              unitConversionForProjectService.convert(
                  projectTaskUnit,
                  projectUnit,
                  projectTask.getSoldTime(),
                  BIG_DECIMAL_SCALE,
                  project));
      totalUpdatedTime =
          totalUpdatedTime.add(
              unitConversionForProjectService.convert(
                  projectTaskUnit,
                  projectUnit,
                  projectTask.getUpdatedTime(),
                  BIG_DECIMAL_SCALE,
                  project));
      totalPlannedTime =
          totalPlannedTime.add(
              unitConversionForProjectService.convert(
                  projectTaskUnit,
                  projectUnit,
                  projectTask.getPlannedTime(),
                  BIG_DECIMAL_SCALE,
                  project));
      totalSpentTime =
          totalSpentTime.add(
              unitConversionForProjectService.convert(
                  projectTaskUnit,
                  projectUnit,
                  projectTask.getSpentTime(),
                  BIG_DECIMAL_SCALE,
                  project));
    }

    project.setSoldTime(totalSoldTime);
    project.setUpdatedTime(totalUpdatedTime);
    project.setPlannedTime(totalPlannedTime);
    project.setSpentTime(totalSpentTime);

    BigDecimal percentageLimit = BigDecimal.valueOf(999.99);

    if (totalUpdatedTime.signum() > 0) {
      project.setPercentageOfProgress(
          projectTaskBusinessProjectService.verifiedLimitFollowUp(
              totalSpentTime
                  .multiply(new BigDecimal("100"))
                  .divide(totalUpdatedTime, BIG_DECIMAL_SCALE, RoundingMode.HALF_UP),
              percentageLimit));
    }

    if (totalSoldTime.signum() > 0) {
      project.setPercentageOfConsumption(
          projectTaskBusinessProjectService.verifiedLimitFollowUp(
              totalSpentTime
                  .multiply(new BigDecimal("100"))
                  .divide(totalSoldTime, BIG_DECIMAL_SCALE, RoundingMode.HALF_UP),
              percentageLimit));
    }

    project.setRemainingAmountToDo(
        projectTaskBusinessProjectService.verifiedLimitFollowUp(
            totalUpdatedTime
                .subtract(totalSpentTime)
                .setScale(BIG_DECIMAL_SCALE, RoundingMode.HALF_UP),
            BigDecimal.valueOf(9999.99)));
  }

  protected void computeFinancialFollowUp(Project project, List<ProjectTask> projectTaskList) {

    BigDecimal initialTurnover =
        projectTaskList.stream()
            .map(ProjectTask::getTurnover)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal initialCosts =
        projectTaskList.stream()
            .map(ProjectTask::getInitialCosts)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal initialMargin = initialTurnover.subtract(initialCosts);

    project.setTurnover(initialTurnover);
    project.setInitialCosts(initialCosts);

    project.setInitialMargin(initialMargin);
    if (initialCosts.signum() != 0) {
      project.setInitialMarkup(
          initialMargin
              .multiply(new BigDecimal("100"))
              .divide(initialCosts, BIG_DECIMAL_SCALE, RoundingMode.HALF_UP));
    }

    // forecast
    BigDecimal forecastCosts =
        projectTaskList.stream()
            .map(ProjectTask::getForecastCosts)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    project.setForecastCosts(forecastCosts);

    project.setForecastMargin(project.getTurnover().subtract(forecastCosts));
    if (forecastCosts.compareTo(BigDecimal.ZERO) != 0) {
      project.setForecastMarkup(
          project
              .getForecastMargin()
              .multiply(new BigDecimal("100"))
              .divide(forecastCosts, BIG_DECIMAL_SCALE, RoundingMode.HALF_UP));
    }

    BigDecimal realTurnover =
        projectTaskList.stream()
            .map(ProjectTask::getRealTurnover)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal realCosts =
        projectTaskList.stream()
            .map(ProjectTask::getRealCosts)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal realMargin = realTurnover.subtract(realCosts);

    project.setRealTurnover(realTurnover);
    project.setRealCosts(realCosts);
    project.setRealMargin(realMargin);

    if (realCosts.signum() != 0) {
      project.setRealMarkup(
          realMargin
              .multiply(new BigDecimal("100"))
              .divide(realCosts, BIG_DECIMAL_SCALE, RoundingMode.HALF_UP));
    }

    BigDecimal landingCosts =
        projectTaskList.stream()
            .map(ProjectTask::getLandingCosts)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    project.setLandingCosts(landingCosts);
    BigDecimal landingMargin = initialTurnover.subtract(landingCosts);

    project.setLandingMargin(landingMargin);

    if (landingCosts.signum() != 0) {
      project.setLandingMarkup(
          landingMargin
              .multiply(new BigDecimal("100"))
              .divide(landingCosts, BIG_DECIMAL_SCALE, RoundingMode.HALF_UP));
    }
  }

  protected void computeInvoicingFollowUp(Project project) {
    List<Invoice> ventilatedInvoices =
        invoiceRepository
            .all()
            .filter("self.project.id = :projectId AND self.statusSelect = :invoiceStatusVentilated")
            .bind("projectId", project.getId())
            .bind("invoiceStatusVentilated", InvoiceRepository.STATUS_VENTILATED)
            .fetch();

    BigDecimal totalInvoiced = BigDecimal.ZERO;
    BigDecimal invoicedThisMonth = BigDecimal.ZERO;
    BigDecimal invoicedLastMonth = BigDecimal.ZERO;
    BigDecimal totalPaid = BigDecimal.ZERO;

    for (Invoice ventilatedInvoice : ventilatedInvoices) {
      totalInvoiced = totalInvoiced.add(processTotalInvoiced(ventilatedInvoice));
      invoicedThisMonth = invoicedThisMonth.add(processInvoicedThisMonth(ventilatedInvoice));
      invoicedLastMonth = invoicedLastMonth.add(processInvoicedLastMonth(ventilatedInvoice));
      totalPaid = totalPaid.add(processTotalPaid(ventilatedInvoice));
    }

    project.setTotalInvoiced(totalInvoiced);
    project.setInvoicedThisMonth(invoicedThisMonth);
    project.setInvoicedLastMonth(invoicedLastMonth);
    project.setTotalPaid(totalPaid);
  }

  protected BigDecimal processTotalInvoiced(Invoice ventilatedInvoice) {
    switch (ventilatedInvoice.getOperationTypeSelect()) {
      case InvoiceRepository.OPERATION_TYPE_CLIENT_SALE:
        return ventilatedInvoice.getCompanyExTaxTotal();
      case InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND:
        return ventilatedInvoice.getCompanyExTaxTotal().negate();
      default:
        return BigDecimal.ZERO;
    }
  }

  protected BigDecimal processInvoicedThisMonth(Invoice ventilatedInvoice) {
    if (LocalDateHelper.isInTheSameMonth(
        ventilatedInvoice.getInvoiceDate(),
        appBaseService.getTodayDateTime(ventilatedInvoice.getCompany()).toLocalDate())) {
      return processTotalInvoiced(ventilatedInvoice);
    }
    return BigDecimal.ZERO;
  }

  protected BigDecimal processInvoicedLastMonth(Invoice ventilatedInvoice) {
    if (isLastMonth(
        ventilatedInvoice.getInvoiceDate(),
        appBaseService.getTodayDateTime(ventilatedInvoice.getCompany()).toLocalDate())) {
      return processTotalInvoiced(ventilatedInvoice);
    }
    return BigDecimal.ZERO;
  }

  protected BigDecimal processTotalPaid(Invoice ventilatedInvoice) {
    switch (ventilatedInvoice.getOperationTypeSelect()) {
      case InvoiceRepository.OPERATION_TYPE_CLIENT_SALE:
        if (ventilatedInvoice.getExTaxTotal().compareTo(BigDecimal.ZERO) != 0) {
          return ventilatedInvoice
              .getCompanyExTaxTotal()
              .divide(ventilatedInvoice.getExTaxTotal(), RoundingMode.HALF_UP)
              .multiply(ventilatedInvoice.getAmountPaid())
              .setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
      case InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND:
        if (ventilatedInvoice.getExTaxTotal().compareTo(BigDecimal.ZERO) != 0) {
          return ventilatedInvoice
              .getCompanyExTaxTotal()
              .divide(ventilatedInvoice.getExTaxTotal(), RoundingMode.HALF_UP)
              .multiply(ventilatedInvoice.getAmountPaid())
              .setScale(2, RoundingMode.HALF_UP)
              .negate();
        }
        return BigDecimal.ZERO;
      default:
        return BigDecimal.ZERO;
    }
  }

  protected boolean isLastMonth(LocalDate date, LocalDate today) {
    if (today.getMonthValue() > 1) {
      return date.getMonthValue() == today.getMonthValue() - 1 && date.getYear() == today.getYear();
    }
    return date.getMonthValue() == 12 && date.getYear() == today.getYear() - 1;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void backupToProjectHistory(Project project) {

    project.addProjectHistoryLineListItem(createProjectHistoryLine(project));

    projectRepository.save(project);
  }

  protected ProjectHistoryLine createProjectHistoryLine(Project project) {
    ProjectHistoryLine projectHistoryLine = new ProjectHistoryLine();

    computeTimeFields(projectHistoryLine, project);
    computeFinancialFields(projectHistoryLine, project);
    computeInvoiceFields(projectHistoryLine, project);

    return projectHistoryLine;
  }

  protected void computeTimeFields(ProjectHistoryLine projectHistoryLine, Project project) {
    projectHistoryLine.setSoldTime(project.getSoldTime());
    projectHistoryLine.setUpdatedTime(project.getUpdatedTime());
    projectHistoryLine.setPlannedTime(project.getPlannedTime());
    projectHistoryLine.setSpentTime(project.getSpentTime());
    projectHistoryLine.setPercentageOfProgress(project.getPercentageOfProgress());
    projectHistoryLine.setPercentageOfConsumption(project.getPercentageOfConsumption());
    projectHistoryLine.setRemainingAmountToDo(project.getRemainingAmountToDo());
  }

  protected void computeFinancialFields(ProjectHistoryLine projectHistoryLine, Project project) {
    projectHistoryLine.setTurnover(project.getTurnover());
    projectHistoryLine.setInitialCosts(project.getInitialCosts());
    projectHistoryLine.setInitialMargin(project.getInitialMargin());
    projectHistoryLine.setInitialMarkup(project.getInitialMarkup());
    projectHistoryLine.setRealTurnover(project.getRealTurnover());
    projectHistoryLine.setRealCosts(project.getRealCosts());
    projectHistoryLine.setRealMargin(project.getRealMargin());
    projectHistoryLine.setRealMarkup(project.getRealMarkup());
    projectHistoryLine.setForecastCosts(project.getForecastCosts());
    projectHistoryLine.setForecastMargin(project.getForecastMargin());
    projectHistoryLine.setForecastMarkup(project.getForecastMarkup());
    projectHistoryLine.setLandingCosts(project.getLandingCosts());
    projectHistoryLine.setLandingMargin(project.getLandingMargin());
    projectHistoryLine.setLandingMarkup(project.getLandingMarkup());
  }

  protected void computeInvoiceFields(ProjectHistoryLine projectHistoryLine, Project project) {
    projectHistoryLine.setTotalInvoiced(project.getTotalInvoiced());
    projectHistoryLine.setInvoicedThisMonth(project.getInvoicedThisMonth());
    projectHistoryLine.setInvoicedLastMonth(project.getInvoicedLastMonth());
    projectHistoryLine.setTotalPaid(project.getTotalPaid());
  }

  @Override
  public Map<String, Object> processRequestToDisplayTimeReporting(Long id) throws AxelorException {

    Project project = projectRepository.find(id);

    Map<String, Object> data = new HashMap<>();
    data.put("soldTime", project.getSoldTime());
    data.put("updatedTime", project.getUpdatedTime());
    data.put("plannedTime", project.getPlannedTime());
    data.put("spentTime", project.getSpentTime());
    data.put(
        "unit",
        Optional.ofNullable(project.getProjectTimeUnit())
            .map(unit -> unit.getName() + "(s)")
            .orElse(""));
    data.put("progress", project.getPercentageOfProgress() + " %");
    data.put("consumption", project.getPercentageOfConsumption() + " %");
    data.put("remaining", project.getRemainingAmountToDo());

    return data;
  }

  @Override
  public Map<String, Object> processRequestToDisplayFinancialReporting(Long id)
      throws AxelorException {

    Project project = projectRepository.find(id);

    Map<String, Object> data = new HashMap<>();
    BigDecimal turnover = project.getTurnover();
    BigDecimal initialCosts = project.getInitialCosts();
    BigDecimal initialMargin = project.getInitialMargin();
    BigDecimal initialMarkup = project.getInitialMarkup();
    BigDecimal realTurnover = project.getRealTurnover();
    BigDecimal realCosts = project.getRealCosts();
    BigDecimal realMargin = project.getRealMargin();
    BigDecimal realMarkup = project.getRealMarkup();
    BigDecimal forecastCosts = project.getForecastCosts();
    BigDecimal forecastMargin = project.getForecastMargin();
    BigDecimal forecastMarkup = project.getForecastMarkup();
    BigDecimal landingCosts = project.getLandingCosts();
    BigDecimal landingMargin = project.getLandingMargin();
    BigDecimal landingMarkup = project.getLandingMarkup();
    BigDecimal totalInvoiced = project.getTotalInvoiced();
    BigDecimal invoicedThisMonth = project.getInvoicedThisMonth();
    BigDecimal invoicedLastMonth = project.getInvoicedLastMonth();
    BigDecimal totalPaid = project.getTotalPaid();

    data.put("turnover", turnover);
    data.put("initialCosts", initialCosts);
    data.put("initialMargin", initialMargin);
    data.put("initialMarkup", initialMarkup);
    data.put("realTurnover", realTurnover);
    data.put("realCosts", realCosts);
    data.put("realMargin", realMargin);
    data.put("realMarkup", realMarkup);
    data.put("forecastCosts", forecastCosts);
    data.put("forecastMargin", forecastMargin);
    data.put("forecastMarkup", forecastMarkup);
    data.put("landingCosts", landingCosts);
    data.put("landingMargin", landingMargin);
    data.put("landingMarkup", landingMarkup);
    data.put("totalInvoiced", totalInvoiced);
    data.put("invoicedThisMonth", invoicedThisMonth);
    data.put("invoicedLastMonth", invoicedLastMonth);
    data.put("totalPaid", totalPaid);
    if (project.getCompany() != null && project.getCompany().getCurrency() != null) {
      data.put("currencySymbol", project.getCompany().getCurrency().getSymbol());
    }

    List<ProjectHistoryLine> projectHistoryLineList = project.getProjectHistoryLineList();

    if (projectHistoryLineList.isEmpty()) {
      return data;
    }

    // compare to previous data
    Comparator<ProjectHistoryLine> projectHistoryLineComparator =
        Comparator.comparing(ProjectHistoryLine::getCreatedOn);

    ProjectHistoryLine projectHistoryLine =
        projectHistoryLineList.stream().max(projectHistoryLineComparator).get();

    data.put(
        "turnoverProgress", getProgressIcon(projectHistoryLine.getTurnover().compareTo(turnover)));
    data.put(
        "initialCostsProgress",
        getProgressIcon(projectHistoryLine.getInitialCosts().compareTo(initialCosts)));
    data.put(
        "initialMarginProgress",
        getProgressIcon(projectHistoryLine.getInitialMargin().compareTo(initialMargin)));
    data.put(
        "initialMarkupProgress",
        getProgressIcon(projectHistoryLine.getInitialMarkup().compareTo(initialMarkup)));
    data.put(
        "realTurnoverProgress",
        getProgressIcon(projectHistoryLine.getRealTurnover().compareTo(realTurnover)));
    data.put(
        "realCostsProgress",
        getProgressIcon(projectHistoryLine.getRealCosts().compareTo(realCosts)));
    data.put(
        "realMarginProgress",
        getProgressIcon(projectHistoryLine.getRealMargin().compareTo(realMargin)));
    data.put(
        "realMarkupProgress",
        getProgressIcon(projectHistoryLine.getRealMarkup().compareTo(realMarkup)));
    data.put(
        "forecastCostsProgress",
        getProgressIcon(projectHistoryLine.getForecastCosts().compareTo(forecastCosts)));
    data.put(
        "forecastMarginProgress",
        getProgressIcon(projectHistoryLine.getForecastMargin().compareTo(forecastMargin)));
    data.put(
        "forecastMarkupProgress",
        getProgressIcon(projectHistoryLine.getForecastMarkup().compareTo(forecastMarkup)));
    data.put(
        "landingCostsProgress",
        getProgressIcon(projectHistoryLine.getLandingCosts().compareTo(landingCosts)));
    data.put(
        "landingMarginProgress",
        getProgressIcon(projectHistoryLine.getLandingMargin().compareTo(landingMargin)));
    data.put(
        "landingMarkupProgress",
        getProgressIcon(projectHistoryLine.getLandingMarkup().compareTo(landingMarkup)));
    data.put(
        "totalInvoicedProgress",
        getProgressIcon(projectHistoryLine.getTotalInvoiced().compareTo(totalInvoiced)));
    data.put(
        "invoicedThisMonthProgress",
        getProgressIcon(projectHistoryLine.getInvoicedThisMonth().compareTo(invoicedThisMonth)));
    data.put(
        "invoicedLastMonthProgress",
        getProgressIcon(projectHistoryLine.getInvoicedLastMonth().compareTo(invoicedLastMonth)));
    data.put(
        "totalPaidProgress",
        getProgressIcon(projectHistoryLine.getTotalPaid().compareTo(totalPaid)));

    return data;
  }

  protected String getProgressIcon(int comparisonResult) {
    switch (comparisonResult) {
      case 0:
        return ICON_EQUAL;
      case 1:
        return FA_LEVEL_DOWN;
      case -1:
        return FA_LEVEL_UP;
      default:
        return "";
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void transitionBetweenPaidStatus(Project project) throws AxelorException {
    ProjectStatus completedStatus = appProjectService.getCompletedProjectStatus();
    ProjectStatus completedPaidStatus =
        appProjectService.getAppProject().getCompletedPaidProjectStatus();
    if (completedPaidStatus == null) {
      throw new AxelorException(
          appProjectService.getAppProject(),
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProjectExceptionMessage.PROJECT_CONFIG_COMPLETED_PAID_PROJECT_STATUS_MISSING));
    }

    List<Invoice> invoiceList =
        invoiceRepository.all().filter("self.project = :project").bind("project", project).fetch();

    if (Objects.equals(project.getProjectStatus(), completedStatus)) {
      if (invoiceList.stream().noneMatch(invoice -> invoice.getAmountRemaining().signum() != 0)) {
        project.setProjectStatus(completedPaidStatus);
      }
    } else if (Objects.equals(project.getProjectStatus(), completedPaidStatus)) {
      if (invoiceList.stream().anyMatch(invoice -> invoice.getAmountRemaining().signum() != 0)) {
        project.setProjectStatus(completedStatus);
      }
    }
  }

  @Override
  public Map<String, Object> getTaskView(
      Project project, String title, String domain, Map<String, Object> context) {
    String gridName = "project-task-grid";
    String formName = "project-task-form";

    if (project.getIsBusinessProject()) {
      gridName = "business-project-task-grid";
      formName = "business-project-task-form";
      domain = domain.concat(" AND self.project.isBusinessProject = true");
    } else {
      domain = domain.concat(" AND self.project.isBusinessProject = false");
    }

    ActionView.ActionViewBuilder builder =
        ActionView.define(I18n.get(title))
            .model(ProjectTask.class.getName())
            .add("grid", gridName)
            .add("form", formName)
            .domain(domain)
            .param("details-view", "true");

    if (project.getIsShowKanbanPerCategory() && project.getIsShowCalendarPerCategory()) {
      builder.add("kanban", "task-per-category-kanban");
      builder.add("calendar", "project-task-per-category-calendar");
    } else {
      builder.add("kanban", "project-task-kanban");
      builder.add("calendar", "project-task-per-status-calendar");
    }

    if (ObjectUtils.notEmpty(context)) {
      context.forEach(builder::context);
    }
    return builder.map();
  }

  public List<String> checkPercentagesOver1000OnTasks(Project project) {
    BigDecimal percentageLimit = BigDecimal.valueOf(999.99);
    return project.getProjectTaskList().stream()
        .filter(
            projectTask ->
                projectTask.getPercentageOfProgress().compareTo(percentageLimit) == 0
                    || projectTask.getPercentageOfConsumption().compareTo(percentageLimit) == 0
                    || projectTask.getRemainingAmountToDo().compareTo(BigDecimal.valueOf(9999.99))
                        == 0)
        .map(ProjectTask::getName)
        .collect(Collectors.toList());
  }
}
