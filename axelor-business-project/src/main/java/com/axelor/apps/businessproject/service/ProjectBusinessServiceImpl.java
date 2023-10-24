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

import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectHistoryLine;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTemplate;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectStatusRepository;
import com.axelor.apps.project.db.repo.ProjectTemplateRepository;
import com.axelor.apps.project.service.ProjectServiceImpl;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderCreateService;
import com.axelor.apps.supplychain.service.SaleOrderSupplychainService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppSupplychain;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProjectBusinessServiceImpl extends ProjectServiceImpl
    implements ProjectBusinessService {

  protected PartnerService partnerService;
  protected AddressService addressService;
  protected AppBusinessProjectService appBusinessProjectService;
  protected ProjectTaskBusinessProjectService projectTaskBusinessProjectService;
  protected ProjectTaskReportingValuesComputingService projectTaskReportingValuesComputingService;

  public static final int BIG_DECIMAL_SCALE = 2;
  public static final String FA_LEVEL_UP = "fa-level-up";
  public static final String FA_LEVEL_DOWN = "fa-level-down";
  public static final String ICON_EQUAL = "icon-equal";

  @Inject
  public ProjectBusinessServiceImpl(
      ProjectRepository projectRepository,
      ProjectStatusRepository projectStatusRepository,
      ProjectTemplateRepository projTemplateRepo,
      AppProjectService appProjectService,
      PartnerService partnerService,
      AddressService addressService,
      AppBusinessProjectService appBusinessProjectService,
      ProjectTaskBusinessProjectService projectTaskBusinessProjectService,
      ProjectTaskReportingValuesComputingService projectTaskReportingValuesComputingService) {
    super(projectRepository, projectStatusRepository, appProjectService, projTemplateRepo);
    this.partnerService = partnerService;
    this.addressService = addressService;
    this.appBusinessProjectService = appBusinessProjectService;
    this.projectTaskBusinessProjectService = projectTaskBusinessProjectService;
    this.projectTaskReportingValuesComputingService = projectTaskReportingValuesComputingService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public SaleOrder generateQuotation(Project project) throws AxelorException {
    SaleOrder order = Beans.get(SaleOrderCreateService.class).createSaleOrder(project.getCompany());

    Partner clientPartner = project.getClientPartner();
    Partner contactPartner = project.getContactPartner();
    if (contactPartner == null && clientPartner.getContactPartnerSet().size() == 1) {
      contactPartner = clientPartner.getContactPartnerSet().iterator().next();
    }

    Company company = project.getCompany();

    order.setProject(projectRepository.find(project.getId()));
    order.setClientPartner(clientPartner);
    order.setContactPartner(contactPartner);
    order.setCompany(company);

    order.setMainInvoicingAddress(partnerService.getInvoicingAddress(clientPartner));
    order.setMainInvoicingAddressStr(
        addressService.computeAddressStr(order.getMainInvoicingAddress()));
    order.setDeliveryAddress(partnerService.getDeliveryAddress(clientPartner));
    order.setDeliveryAddressStr(addressService.computeAddressStr(order.getDeliveryAddress()));
    order.setIsNeedingConformityCertificate(clientPartner.getIsNeedingConformityCertificate());
    order.setCompanyBankDetails(
        Beans.get(AccountingSituationService.class)
            .getCompanySalesBankDetails(company, clientPartner));

    if (project.getCurrency() != null) {
      order.setCurrency(project.getCurrency());
    } else if (clientPartner.getCurrency() != null) {
      order.setCurrency(clientPartner.getCurrency());
    } else {
      order.setCurrency(company.getCurrency());
    }

    if (project.getPriceList() != null) {
      order.setPriceList(project.getPriceList());
    } else {
      order.setPriceList(
          Beans.get(PartnerPriceListService.class)
              .getDefaultPriceList(clientPartner, PriceListRepository.TYPE_SALE));
    }

    if (order.getPriceList() != null) {
      order.setHideDiscount(order.getPriceList().getHideDiscount());
    }

    if (clientPartner.getPaymentCondition() != null) {
      order.setPaymentCondition(clientPartner.getPaymentCondition());
    } else {
      if (company != null && company.getAccountConfig() != null) {
        order.setPaymentCondition(company.getAccountConfig().getDefPaymentCondition());
      }
    }

    if (clientPartner.getInPaymentMode() != null) {
      order.setPaymentMode(clientPartner.getInPaymentMode());
    } else {
      if (company != null && company.getAccountConfig() != null) {
        order.setPaymentMode(company.getAccountConfig().getInPaymentMode());
      }
    }

    AppSupplychain appSupplychain = Beans.get(AppSupplychainService.class).getAppSupplychain();
    if (appSupplychain != null) {
      order.setShipmentMode(clientPartner.getShipmentMode());
      order.setFreightCarrierMode(clientPartner.getFreightCarrierMode());
      if (clientPartner.getFreightCarrierMode() != null) {
        order.setCarrierPartner(clientPartner.getFreightCarrierMode().getCarrierPartner());
      }
      Boolean interco =
          appSupplychain.getIntercoFromSale()
              && !order.getCreatedByInterco()
              && clientPartner != null
              && Beans.get(CompanyRepository.class)
                      .all()
                      .filter("self.partner = ?", clientPartner)
                      .fetchOne()
                  != null;
      order.setInterco(interco);

      // Automatic invoiced and delivered partners set in case of partner delegations
      if (appSupplychain.getActivatePartnerRelations()) {
        Beans.get(SaleOrderSupplychainService.class)
            .setDefaultInvoicedAndDeliveredPartnersAndAddresses(order);
      }
    }
    return Beans.get(SaleOrderRepository.class).save(order);
  }

  /**
   * Generate project form SaleOrder and set bi-directional.
   *
   * @param saleOrder The order of origin.
   * @return The project generated.
   */
  @Override
  public Project generateProject(SaleOrder saleOrder) {
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
      Partner clientPartner) {
    Project project =
        super.generateProject(parentProject, fullName, assignedTo, company, clientPartner);

    if (!appBusinessProjectService.isApp("business-project")) {
      return project;
    }

    if (assignedTo != null) {
      project.addMembersUserSetItem(assignedTo);
    }

    project.setImputable(true);
    project.setCompany(company);
    if (parentProject != null && parentProject.getIsInvoicingTimesheet()) {
      project.setIsInvoicingTimesheet(true);
    }

    project.setNumberHoursADay(
        appBusinessProjectService.getAppBusinessProject().getDefaultHoursADay());
    project.setProjectTimeUnit(appBusinessProjectService.getAppBusinessProject().getDaysUnit());
    return project;
  }

  @Override
  public Project generatePhaseProject(SaleOrderLine saleOrderLine, Project parent) {
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
    }
    project.setProjectFolderSet(new HashSet<>(projectTemplate.getProjectFolderSet()));
    project.setCompany(projectTemplate.getCompany());

    return project;
  }

  @Override
  public String getTimeZone(Project project) {
    if (project == null || project.getCompany() == null) {
      return null;
    }
    return project.getCompany().getTimezone();
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
    projectRepository.save(project);
  }

  protected void computeTimeFollowUp(Project project, List<ProjectTask> projectTaskList)
      throws AxelorException {
    BigDecimal totalSoldTime = BigDecimal.ZERO;
    BigDecimal totalUpdatedTime = BigDecimal.ZERO;
    BigDecimal totalPlannedTime = BigDecimal.ZERO;
    BigDecimal totalSpentTime = BigDecimal.ZERO;

    Unit projectUnit = project.getProjectTimeUnit();
    BigDecimal numberHoursADay = project.getNumberHoursADay();

    if (numberHoursADay.signum() <= 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BusinessProjectExceptionMessage.PROJECT_CONFIG_DEFAULT_HOURS_PER_DAY_MISSING));
    }

    for (ProjectTask projectTask : projectTaskList) {
      Unit projectTaskUnit = projectTask.getTimeUnit();
      totalSoldTime =
          totalSoldTime
              .add(
                  getConvertedTime(
                      projectTask.getSoldTime(), projectTaskUnit, projectUnit, numberHoursADay))
              .setScale(BIG_DECIMAL_SCALE, RoundingMode.HALF_UP);
      totalUpdatedTime =
          totalUpdatedTime
              .add(
                  getConvertedTime(
                      projectTask.getUpdatedTime(), projectTaskUnit, projectUnit, numberHoursADay))
              .setScale(BIG_DECIMAL_SCALE, RoundingMode.HALF_UP);
      totalPlannedTime =
          totalPlannedTime
              .add(
                  getConvertedTime(
                      projectTask.getPlannedTime(), projectTaskUnit, projectUnit, numberHoursADay))
              .setScale(BIG_DECIMAL_SCALE, RoundingMode.HALF_UP);
      totalSpentTime =
          totalSpentTime
              .add(
                  getConvertedTime(
                      projectTask.getSpentTime(), projectTaskUnit, projectUnit, numberHoursADay))
              .setScale(BIG_DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    project.setSoldTime(totalSoldTime);
    project.setUpdatedTime(totalUpdatedTime);
    project.setPlannedTime(totalPlannedTime);
    project.setSpentTime(totalSpentTime);

    if (totalUpdatedTime.signum() > 0) {
      project.setPercentageOfProgress(
          totalSpentTime
              .multiply(new BigDecimal("100"))
              .divide(totalUpdatedTime, BIG_DECIMAL_SCALE, RoundingMode.HALF_UP));
    }

    if (totalSoldTime.signum() > 0) {
      project.setPercentageOfConsumption(
          totalSpentTime
              .multiply(new BigDecimal("100"))
              .divide(totalSoldTime, BIG_DECIMAL_SCALE, RoundingMode.HALF_UP));
    }

    project.setRemainingAmountToDo(
        totalUpdatedTime
            .subtract(totalSpentTime)
            .setScale(BIG_DECIMAL_SCALE, RoundingMode.HALF_UP));
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

    BigDecimal forecastCosts =
        projectTaskList.stream()
            .map(ProjectTask::getForecastCosts)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    project.setForecastCosts(forecastCosts);
    BigDecimal forecastMargin = initialTurnover.subtract(forecastCosts);

    project.setForecastMargin(forecastMargin);

    if (forecastCosts.signum() != 0) {
      project.setForecastMarkup(
          forecastMargin
              .multiply(new BigDecimal("100"))
              .divide(forecastCosts, BIG_DECIMAL_SCALE, RoundingMode.HALF_UP));
    }
  }

  protected BigDecimal getConvertedTime(
      BigDecimal duration, Unit fromUnit, Unit toUnit, BigDecimal numberHoursADay)
      throws AxelorException {
    if (appBusinessProjectService.getDaysUnit().equals(fromUnit)
        && appBusinessProjectService.getHoursUnit().equals(toUnit)) {
      return duration.multiply(numberHoursADay);
    } else if (appBusinessProjectService.getHoursUnit().equals(fromUnit)
        && appBusinessProjectService.getDaysUnit().equals(toUnit)) {
      return duration.divide(numberHoursADay, BIG_DECIMAL_SCALE, RoundingMode.HALF_UP);
    } else {
      return duration;
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void backupToProjectHistory(Project project) {
    ProjectHistoryLine projectHistoryLine = new ProjectHistoryLine();
    projectHistoryLine.setSoldTime(project.getSoldTime());
    projectHistoryLine.setUpdatedTime(project.getUpdatedTime());
    projectHistoryLine.setPlannedTime(project.getPlannedTime());
    projectHistoryLine.setSpentTime(project.getSpentTime());
    projectHistoryLine.setPercentageOfProgress(project.getPercentageOfProgress());
    projectHistoryLine.setPercentageOfConsumption(project.getPercentageOfConsumption());
    projectHistoryLine.setRemainingAmountToDo(project.getRemainingAmountToDo());

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

    project.addProjectHistoryLineListItem(projectHistoryLine);

    projectRepository.save(project);
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
}
