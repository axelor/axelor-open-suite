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
package com.axelor.apps.businessproject.service.projecttask;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.FrequencyRepository;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.FrequencyService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.businessproject.exception.BusinessProjectExceptionMessage;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.TaskStatus;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.db.repo.TaskStatusProgressByCategoryRepository;
import com.axelor.apps.project.service.ProjectTaskServiceImpl;
import com.axelor.apps.project.service.ProjectTimeUnitService;
import com.axelor.apps.project.service.TaskStatusToolService;
import com.axelor.apps.project.service.TaskTemplateService;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppBusinessProject;
import com.axelor.utils.helpers.QueryBuilder;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class ProjectTaskBusinessProjectServiceImpl extends ProjectTaskServiceImpl
    implements ProjectTaskBusinessProjectService {

  public static final int BIG_DECIMAL_SCALE = 2;
  protected PriceListLineRepository priceListLineRepo;
  protected PriceListService priceListService;
  protected PartnerPriceListService partnerPriceListService;
  protected ProductCompanyService productCompanyService;
  protected TimesheetLineRepository timesheetLineRepository;
  protected ProjectTimeUnitService projectTimeUnitService;
  protected TaskTemplateService taskTemplateService;

  @Inject
  public ProjectTaskBusinessProjectServiceImpl(
      ProjectTaskRepository projectTaskRepo,
      FrequencyRepository frequencyRepo,
      FrequencyService frequencyService,
      AppBaseService appBaseService,
      ProjectRepository projectRepository,
      AppProjectService appProjectService,
      TaskStatusToolService taskStatusToolService,
      TaskStatusProgressByCategoryRepository taskStatusProgressByCategoryRepository,
      PriceListLineRepository priceListLineRepo,
      PriceListService priceListService,
      PartnerPriceListService partnerPriceListService,
      ProductCompanyService productCompanyService,
      TimesheetLineRepository timesheetLineRepository,
      ProjectTimeUnitService projectTimeUnitService,
      TaskTemplateService taskTemplateService) {
    super(
        projectTaskRepo,
        frequencyRepo,
        frequencyService,
        appBaseService,
        projectRepository,
        appProjectService,
        taskStatusToolService,
        taskStatusProgressByCategoryRepository);
    this.priceListLineRepo = priceListLineRepo;
    this.priceListService = priceListService;
    this.partnerPriceListService = partnerPriceListService;
    this.productCompanyService = productCompanyService;
    this.timesheetLineRepository = timesheetLineRepository;
    this.projectTimeUnitService = projectTimeUnitService;
    this.taskTemplateService = taskTemplateService;
  }

  @Override
  public ProjectTask create(SaleOrderLine saleOrderLine, Project project, User assignedTo)
      throws AxelorException {
    ProjectTask task = create(saleOrderLine.getFullName() + "_task", project, assignedTo);
    Product product = saleOrderLine.getProduct();
    task.setProduct(product);
    task.setUnitCost(product.getCostPrice());
    task.setTotalCosts(
        product.getCostPrice().multiply(saleOrderLine.getQty()).setScale(2, RoundingMode.HALF_UP));
    Unit orderLineUnit = saleOrderLine.getUnit();
    task.setInvoicingUnit(orderLineUnit);

    task.setCurrency(project.getClientPartner().getCurrency());
    if (project.getPriceList() != null) {
      PriceListLine line =
          priceListLineRepo.findByPriceListAndProduct(project.getPriceList(), product);
      if (line != null) {
        task.setUnitPrice(line.getAmount());
      }
    }
    if (task.getUnitPrice() == null) {
      Company company =
          saleOrderLine.getSaleOrder() != null ? saleOrderLine.getSaleOrder().getCompany() : null;
      task.setUnitPrice((BigDecimal) productCompanyService.get(product, "salePrice", company));
    }
    task.setDescription(saleOrderLine.getDescription());
    task.setQuantity(saleOrderLine.getQty());
    task.setSaleOrderLine(saleOrderLine);

    if (isTimeUnitValid(orderLineUnit)) {
      task.setTimeUnit(orderLineUnit);
      task.setSoldTime(saleOrderLine.getQty());
      task.setUpdatedTime(saleOrderLine.getQty());
    }
    return task;
  }

  @Override
  public ProjectTask create(
      TaskTemplate template, Project project, LocalDateTime date, BigDecimal qty)
      throws AxelorException {
    ProjectTask task = create(template.getName(), project, template.getAssignedTo());

    task.setTaskDate(date.toLocalDate());
    task.setTaskEndDate(date.plusHours(template.getDuration().longValue()).toLocalDate());

    BigDecimal plannedHrs = template.getTotalPlannedHrs();
    if (template.getIsUniqueTaskForMultipleQuantity() && qty.compareTo(BigDecimal.ONE) > 0) {
      plannedHrs = plannedHrs.multiply(qty);
    }
    task.setTotalPlannedHrs(plannedHrs);
    taskTemplateService.manageTemplateFields(task, template, project);

    return task;
  }

  @Override
  public ProjectTask create(String subject, Project project, User assignedTo) {
    ProjectTask task = super.create(subject, project, assignedTo);
    task.setProjectTaskList(new ArrayList<>());
    task.setProjectPlanningTimeList(new ArrayList<>());
    task.setPurchaseOrderLineList(new ArrayList<>());
    task.setTaskDate(appBaseService.getTodayDate(project.getCompany()));
    return task;
  }

  @Override
  public ProjectTask updateDiscount(ProjectTask projectTask) {
    PriceList priceList =
        Optional.of(projectTask)
            .map(ProjectTask::getProject)
            .map(Project::getPriceList)
            .orElse(null);
    Contract frameworkCustomerContract = projectTask.getFrameworkCustomerContract();
    if (frameworkCustomerContract != null || priceList == null) {
      this.emptyDiscounts(projectTask);
      return projectTask;
    }

    PriceListLine priceListLine =
        this.getPriceListLine(projectTask, priceList, projectTask.getUnitPrice());
    Map<String, Object> discounts =
        priceListService.getReplacedPriceAndDiscounts(
            priceList, priceListLine, projectTask.getUnitPrice());

    if (discounts == null) {
      this.emptyDiscounts(projectTask);
    } else {
      projectTask.setDiscountTypeSelect((Integer) discounts.get("discountTypeSelect"));
      projectTask.setDiscountAmount((BigDecimal) discounts.get("discountAmount"));
      if (discounts.get("price") != null) {
        projectTask.setPriceDiscounted((BigDecimal) discounts.get("price"));
      }
    }
    return projectTask;
  }

  protected void emptyDiscounts(ProjectTask projectTask) {
    projectTask.setDiscountTypeSelect(PriceListLineRepository.AMOUNT_TYPE_NONE);
    projectTask.setDiscountAmount(BigDecimal.ZERO);
    projectTask.setPriceDiscounted(BigDecimal.ZERO);
  }

  protected PriceListLine getPriceListLine(
      ProjectTask projectTask, PriceList priceList, BigDecimal price) {

    return priceListService.getPriceListLine(
        projectTask.getProduct(), projectTask.getQuantity(), priceList, price);
  }

  @Override
  public ProjectTask compute(ProjectTask projectTask) {
    if (projectTask.getProduct() == null && projectTask.getProject() == null
        || projectTask.getUnitPrice() == null
        || projectTask.getQuantity() == null) {
      return projectTask;
    }
    BigDecimal priceDiscounted = this.computeDiscount(projectTask);
    BigDecimal exTaxTotal = this.computeAmount(projectTask.getQuantity(), priceDiscounted);

    projectTask.setPriceDiscounted(priceDiscounted);
    projectTask.setExTaxTotal(exTaxTotal);

    if (projectTask.getProduct() != null) {
      projectTask.setTotalCosts(
          projectTask
              .getUnitCost()
              .multiply(projectTask.getQuantity())
              .setScale(2, RoundingMode.HALF_UP));
    }

    return projectTask;
  }

  protected BigDecimal computeDiscount(ProjectTask projectTask) {

    return priceListService.computeDiscount(
        projectTask.getUnitPrice(),
        projectTask.getDiscountTypeSelect(),
        projectTask.getDiscountAmount());
  }

  protected BigDecimal computeAmount(BigDecimal quantity, BigDecimal price) {

    BigDecimal amount =
        price
            .multiply(quantity)
            .setScale(AppSaleService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);

    return amount;
  }

  @Override
  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<ProjectTask> projectTaskList, int priority) throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    int count = 0;
    for (ProjectTask projectTask : projectTaskList) {
      invoiceLineList.addAll(this.createInvoiceLine(invoice, projectTask, priority * 100 + count));
      count++;
    }

    if (projectTaskList.stream()
        .anyMatch(
            task ->
                task.getInvoicingType().equals(ProjectTaskRepository.INVOICING_TYPE_ON_PROGRESS))) {
      invoice.setOperationSubTypeSelect(InvoiceRepository.OPERATION_SUB_TYPE_IN_PROGRESS_INVOICE);
    }

    return invoiceLineList;
  }

  @Override
  public List<InvoiceLine> createInvoiceLine(Invoice invoice, ProjectTask projectTask, int priority)
      throws AxelorException {

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGenerator(
            invoice,
            projectTask.getProduct(),
            projectTask.getName(),
            projectTask.getUnitPrice(),
            BigDecimal.ZERO,
            projectTask.getPriceDiscounted(),
            projectTask.getDescription(),
            projectTask.getQuantity(),
            projectTask.getInvoicingUnit(),
            null,
            priority,
            projectTask.getDiscountAmount(),
            projectTask.getDiscountTypeSelect(),
            projectTask.getExTaxTotal(),
            BigDecimal.ZERO,
            false) {

          @Override
          public List<InvoiceLine> creates() throws AxelorException {

            InvoiceLine invoiceLine = this.createInvoiceLine();
            invoiceLine.setProject(projectTask.getProject());
            invoiceLine.setSaleOrderLine(projectTask.getSaleOrderLine());
            invoiceLine.addProjectTaskSetItem(projectTask);
            projectTask.addInvoiceLineSetItem(invoiceLine);

            setProgressAndCoefficient(invoiceLine, projectTask);

            List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };

    return invoiceLineGenerator.creates();
  }

  protected void setProgressAndCoefficient(InvoiceLine invoiceLine, ProjectTask projectTask) {
    if (projectTask.getInvoicingType().equals(ProjectTaskRepository.INVOICING_TYPE_ON_PROGRESS)) {
      BigDecimal invoicingProgress = projectTask.getInvoicingProgress();

      BigDecimal progress = projectTask.getProgress();
      invoiceLine.setPreviousProgress(invoicingProgress);
      invoiceLine.setNewProgress(progress);

      invoiceLine.setCoefficient(
          progress
              .subtract(invoicingProgress)
              .divide(BigDecimal.valueOf(100), BIG_DECIMAL_SCALE, RoundingMode.HALF_UP));
    }
  }

  @Override
  protected void updateModuleFields(ProjectTask projectTask, ProjectTask nextProjectTask) {
    super.updateModuleFields(projectTask, nextProjectTask);

    // Module 'business project' fields
    nextProjectTask.setToInvoice(projectTask.getToInvoice());
    nextProjectTask.setExTaxTotal(projectTask.getExTaxTotal());
    nextProjectTask.setDiscountTypeSelect(projectTask.getDiscountTypeSelect());
    nextProjectTask.setDiscountAmount(projectTask.getDiscountAmount());
    nextProjectTask.setPriceDiscounted(projectTask.getPriceDiscounted());
    nextProjectTask.setInvoicingType(projectTask.getInvoicingType());
    nextProjectTask.setCustomerReferral(projectTask.getCustomerReferral());
    nextProjectTask.setTargetVersion(projectTask.getTargetVersion());
  }

  @Override
  public ProjectTask updateTaskFinancialInfo(ProjectTask projectTask) throws AxelorException {
    Product product = projectTask.getProduct();
    if (product != null) {
      projectTask.setInvoicingType(ProjectTaskRepository.INVOICING_TYPE_PACKAGE);
      if (projectTask.getUnitPrice() == null
          || projectTask.getUnitPrice().compareTo(BigDecimal.ZERO) == 0) {
        projectTask.setUnitPrice(this.computeUnitPrice(projectTask));
      }
    } else {
      ProjectTaskCategory projectTaskCategory = projectTask.getProjectTaskCategory();
      if (projectTaskCategory == null) {
        return projectTask;
      }

      Integer invoicingType = projectTaskCategory.getDefaultInvoicingType();
      projectTask.setInvoicingType(invoicingType);
      if (invoicingType.equals(ProjectTaskRepository.INVOICING_TYPE_TIME_SPENT)
          || invoicingType.equals(ProjectTaskRepository.INVOICING_TYPE_PACKAGE)) {
        projectTask.setToInvoice(true);
      }
      product = projectTaskCategory.getDefaultProduct();
      if (product == null) {
        return projectTask;
      }
      projectTask.setProduct(product);
      projectTask.setUnitPrice(this.computeUnitPrice(projectTask));
    }
    Company company =
        projectTask.getProject() != null ? projectTask.getProject().getCompany() : null;
    Unit salesUnit = (Unit) productCompanyService.get(product, "salesUnit", company);
    projectTask.setInvoicingUnit(
        salesUnit != null ? salesUnit : (Unit) productCompanyService.get(product, "unit", company));
    projectTask.setCurrency((Currency) productCompanyService.get(product, "saleCurrency", company));
    projectTask.setQuantity(projectTask.getBudgetedTime());

    projectTask.setQuantity(
        projectTask.getBudgetedTime() == null
                || projectTask.getBudgetedTime().compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ONE
            : projectTask.getBudgetedTime());
    projectTask = this.updateDiscount(projectTask);
    projectTask = this.compute(projectTask);
    return projectTask;
  }

  @Transactional
  @Override
  public ProjectTask updateTaskToInvoice(
      ProjectTask projectTask, AppBusinessProject appBusinessProject) {

    if (projectTask.getInvoicingType() == ProjectTaskRepository.INVOICING_TYPE_PACKAGE
        && !projectTask.getIsTaskRefused()) {

      switch (projectTask.getProject().getInvoicingSequenceSelect()) {
        case ProjectRepository.INVOICING_SEQ_INVOICE_PRE_TASK:
          Set<TaskStatus> preTaskStatusSet = appBusinessProject.getPreTaskStatusSet();
          projectTask.setToInvoice(
              ObjectUtils.notEmpty(preTaskStatusSet)
                  && preTaskStatusSet.contains(projectTask.getStatus()));
          break;

        case ProjectRepository.INVOICING_SEQ_INVOICE_POST_TASK:
          Set<TaskStatus> postTaskStatusSet = appBusinessProject.getPostTaskStatusSet();
          projectTask.setToInvoice(
              ObjectUtils.notEmpty(postTaskStatusSet)
                  && postTaskStatusSet.contains(projectTask.getStatus()));
          break;
      }
    } else {
      projectTask.setToInvoice(
          projectTask.getInvoicingType() == ProjectTaskRepository.INVOICING_TYPE_TIME_SPENT);
    }
    return projectTaskRepo.save(projectTask);
  }

  protected BigDecimal computeUnitPrice(ProjectTask projectTask) throws AxelorException {
    Product product = projectTask.getProduct();
    Company company =
        projectTask.getProject() != null ? projectTask.getProject().getCompany() : null;
    BigDecimal unitPrice = (BigDecimal) productCompanyService.get(product, "salePrice", company);

    PriceList priceList =
        partnerPriceListService.getDefaultPriceList(
            Optional.of(projectTask)
                .map(ProjectTask::getProject)
                .map(Project::getClientPartner)
                .orElse(null),
            PriceListRepository.TYPE_SALE);
    if (priceList == null) {
      return unitPrice;
    }

    PriceListLine priceListLine = this.getPriceListLine(projectTask, priceList, unitPrice);
    Map<String, Object> discounts =
        priceListService.getReplacedPriceAndDiscounts(priceList, priceListLine, unitPrice);

    if (discounts == null) {
      return unitPrice;
    } else {
      unitPrice =
          priceListService.computeDiscount(
              unitPrice,
              (Integer) discounts.get("discountTypeSelect"),
              (BigDecimal) discounts.get("discountAmount"));
    }
    return unitPrice;
  }

  @Override
  public ProjectTask resetProjectTaskValues(ProjectTask projectTask) {
    projectTask.setProduct(null);
    projectTask.setInvoicingType(null);
    projectTask.setToInvoice(null);
    projectTask.setQuantity(null);
    projectTask.setInvoicingUnit(null);
    projectTask.setUnitPrice(null);
    projectTask.setCurrency(null);
    projectTask.setExTaxTotal(null);
    return projectTask;
  }

  @Override
  public QueryBuilder<ProjectTask> getTaskInvoicingFilter() {
    QueryBuilder<ProjectTask> queryBuilder =
        QueryBuilder.of(ProjectTask.class)
            .add("self.project.isBusinessProject = :isBusinessProject")
            .add("self.project.toInvoice = :invoiceable")
            .add("self.toInvoice = :toInvoice")
            .bind("isBusinessProject", true)
            .bind("invoiceable", true)
            .bind("toInvoice", false);

    return queryBuilder;
  }

  @Override
  public void taskInvoicing(Project project, AppBusinessProject appBusinessProject) {
    QueryBuilder<ProjectTask> taskQueryBuilder = getTaskInvoicingFilter();
    taskQueryBuilder =
        taskQueryBuilder.add("self.project.id = :projectId").bind("projectId", project.getId());

    if (!Strings.isNullOrEmpty(appBusinessProject.getExcludeTaskInvoicing())) {
      String filter = "NOT (" + appBusinessProject.getExcludeTaskInvoicing() + ")";
      taskQueryBuilder = taskQueryBuilder.add(filter);
    }
    Query<ProjectTask> taskQuery = taskQueryBuilder.build().order("id");

    int offset = 0;
    List<ProjectTask> projectTaskList;

    while (!(projectTaskList = taskQuery.fetch(AbstractBatch.FETCH_LIMIT, offset)).isEmpty()) {
      offset += projectTaskList.size();
      for (ProjectTask projectTask : projectTaskList) {
        updateTaskToInvoice(projectTask, appBusinessProject);
      }
      JPA.clear();
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public ProjectTask setProjectTaskValues(ProjectTask projectTask) throws AxelorException {
    if (projectTask.getSaleOrderLine() != null
        || CollectionUtils.isNotEmpty(projectTask.getInvoiceLineSet())) {
      return projectTask;
    }
    projectTask = updateTaskFinancialInfo(projectTask);
    return projectTaskRepo.save(projectTask);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void computeProjectTaskTotals(ProjectTask projectTask) throws AxelorException {

    BigDecimal plannedTime = BigDecimal.ZERO;
    BigDecimal spentTime = BigDecimal.ZERO;

    Unit timeUnit = projectTimeUnitService.getTaskDefaultHoursTimeUnit(projectTask);

    if (ObjectUtils.notEmpty(projectTask.getProjectPlanningTimeList())) {
      for (ProjectPlanningTime ppt : projectTask.getProjectPlanningTimeList()) {
        plannedTime =
            plannedTime.add(
                projectTimeUnitService.convertInProjectTaskUnit(
                    projectTask, ppt.getTimeUnit(), ppt.getPlannedTime()));
      }
    }

    List<TimesheetLine> timeSheetLines =
        timesheetLineRepository
            .all()
            .filter("self.timesheet.statusSelect = :status AND self.projectTask = :projectTask")
            .bind("status", TimesheetRepository.STATUS_VALIDATED)
            .bind("projectTask", projectTask)
            .fetch();

    for (TimesheetLine timeSheetLine : timeSheetLines) {
      spentTime =
          spentTime.add(convertTimesheetLineDurationToProjectTaskUnit(timeSheetLine, timeUnit));
    }

    List<ProjectTask> projectTaskList = projectTask.getProjectTaskList();
    for (ProjectTask task : projectTaskList) {
      computeProjectTaskTotals(task);
      plannedTime = plannedTime.add(task.getPlannedTime());
      spentTime = spentTime.add(task.getSpentTime());
    }

    projectTask.setPlannedTime(plannedTime);
    projectTask.setSpentTime(spentTime);

    if (projectTask.getParentTask() == null) {
      computeProjectTaskReporting(projectTask);
    }
    projectTaskRepo.save(projectTask);
  }

  protected BigDecimal convertTimesheetLineDurationToProjectTaskUnit(
      TimesheetLine timesheetLine, Unit timeUnit) throws AxelorException {
    String timeLoggingUnit = timesheetLine.getTimesheet().getTimeLoggingPreferenceSelect();
    BigDecimal duration = timesheetLine.getDuration();
    BigDecimal convertedDuration = BigDecimal.ZERO;

    Unit daysUnit = appBaseService.getUnitDays();
    Unit hoursUnit = appBaseService.getUnitHours();
    BigDecimal defaultHoursADay = appBaseService.getDailyWorkHours();
    if (defaultHoursADay.compareTo(BigDecimal.ZERO) == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_DAILY_WORK_HOURS));
    }

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
          convertedDuration =
              duration.divide(defaultHoursADay, BIG_DECIMAL_SCALE, RoundingMode.HALF_UP);
        }
        break;
      case EmployeeRepository.TIME_PREFERENCE_MINUTES:
        // convert to hours
        convertedDuration =
            duration.divide(new BigDecimal(60), BIG_DECIMAL_SCALE, RoundingMode.HALF_UP);
        if (timeUnit.equals(daysUnit)) {
          convertedDuration =
              duration.divide(defaultHoursADay, BIG_DECIMAL_SCALE, RoundingMode.HALF_UP);
        }
        break;
      default:
        break;
    }

    return convertedDuration;
  }

  /**
   * update project task values for reporting
   *
   * @param projectTask
   * @throws AxelorException
   */
  protected void computeProjectTaskReporting(ProjectTask projectTask) throws AxelorException {
    if (projectTask.getUpdatedTime().signum() <= 0 || projectTask.getSoldTime().signum() <= 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(
              I18n.get(BusinessProjectExceptionMessage.PROJECT_TASK_UPDATE_REPORTING_VALUES_ERROR),
              projectTask.getFullName()));
    }

    BigDecimal percentageOfProgression = projectTask.getSpentTime().multiply(new BigDecimal("100"));
    percentageOfProgression =
        percentageOfProgression.divide(
            projectTask.getUpdatedTime(), BIG_DECIMAL_SCALE, RoundingMode.HALF_UP);

    BigDecimal percentageOfConsumption = projectTask.getSpentTime().multiply(new BigDecimal("100"));
    percentageOfConsumption =
        percentageOfConsumption.divide(
            projectTask.getSoldTime(), BIG_DECIMAL_SCALE, RoundingMode.HALF_UP);
    BigDecimal remainingAmountToDo =
        projectTask
            .getUpdatedTime()
            .subtract(projectTask.getSpentTime())
            .setScale(BIG_DECIMAL_SCALE, RoundingMode.HALF_UP);

    BigDecimal percentageLimit = BigDecimal.valueOf(999.99);

    projectTask.setPercentageOfProgress(
        verifiedLimitFollowUp(percentageOfProgression, percentageLimit));
    projectTask.setPercentageOfConsumption(
        verifiedLimitFollowUp(percentageOfConsumption, percentageLimit));
    projectTask.setRemainingAmountToDo(
        verifiedLimitFollowUp(remainingAmountToDo, BigDecimal.valueOf(9999.99)));
  }

  @Override
  public Map<String, Object> processRequestToDisplayTimeReporting(Long id) throws AxelorException {

    ProjectTask projectTask = projectTaskRepo.find(id);

    Map<String, Object> data = new HashMap<>();
    data.put(
        "unit",
        Optional.ofNullable(projectTimeUnitService.getTaskDefaultHoursTimeUnit(projectTask))
            .map(unit -> unit.getName() + "(s)")
            .orElse(""));
    data.put("progress", projectTask.getPercentageOfProgress() + " %");
    data.put("consumption", projectTask.getPercentageOfConsumption() + " %");
    data.put("remaining", projectTask.getRemainingAmountToDo());

    return data;
  }

  @Override
  public Map<String, Object> processRequestToDisplayFinancialReporting(Long id)
      throws AxelorException {

    ProjectTask projectTask = projectTaskRepo.find(id);

    Map<String, Object> data = new HashMap<>();
    data.put("turnover", projectTask.getTurnover());
    data.put("initialCosts", projectTask.getInitialCosts());
    data.put("initialMargin", projectTask.getInitialMargin());
    data.put("initialMarkup", projectTask.getInitialMarkup());
    data.put("realTurnover", projectTask.getRealTurnover());
    data.put("realCosts", projectTask.getRealCosts());
    data.put("realMargin", projectTask.getRealMargin());
    data.put("realMarkup", projectTask.getRealMarkup());
    data.put("landingCosts", projectTask.getLandingCosts());
    data.put("landingMargin", projectTask.getLandingMargin());
    data.put("landingMarkup", projectTask.getLandingMarkup());
    data.put("forecastCosts", projectTask.getForecastCosts());
    data.put("forecastMargin", projectTask.getForecastMargin());
    data.put("forecastMarkup", projectTask.getForecastMarkup());
    Optional.ofNullable(projectTask.getProject())
        .map(Project::getCompany)
        .map(Company::getCurrency)
        .ifPresent(currency -> data.put("currencySymbol", currency.getSymbol()));

    return data;
  }

  @Override
  public boolean isTimeUnitValid(Unit unit) throws AxelorException {
    return Objects.equals(unit, appBaseService.getUnitDays())
        || Objects.equals(unit, appBaseService.getUnitHours());
  }

  @Override
  public BigDecimal verifiedLimitFollowUp(BigDecimal value, BigDecimal limit) {
    if (value.compareTo(limit) > 0) {
      return limit;
    }
    if (value.compareTo(limit.negate()) < 0) {
      return limit.negate();
    }
    return value;
  }
}
