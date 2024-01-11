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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
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
import com.axelor.apps.base.service.FrequencyService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectStatus;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.ProjectTaskCategory;
import com.axelor.apps.project.db.TaskTemplate;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.ProjectTaskServiceImpl;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.studio.db.AppBusinessProject;
import com.axelor.utils.QueryBuilder;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProjectTaskBusinessProjectServiceImpl extends ProjectTaskServiceImpl
    implements ProjectTaskBusinessProjectService {

  private PriceListLineRepository priceListLineRepo;
  private PriceListService priceListService;
  private PartnerPriceListService partnerPriceListService;
  private ProductCompanyService productCompanyService;

  @Inject
  public ProjectTaskBusinessProjectServiceImpl(
      ProjectTaskRepository projectTaskRepo,
      FrequencyRepository frequencyRepo,
      FrequencyService frequencyService,
      AppBaseService appBaseService,
      ProjectRepository projectRepository,
      PriceListLineRepository priceListLineRepo,
      PriceListService priceListService,
      PartnerPriceListService partnerPriceListService,
      ProductCompanyService productCompanyService) {
    super(projectTaskRepo, frequencyRepo, frequencyService, appBaseService, projectRepository);
    this.priceListLineRepo = priceListLineRepo;
    this.priceListService = priceListService;
    this.partnerPriceListService = partnerPriceListService;
    this.productCompanyService = productCompanyService;
  }

  @Override
  public ProjectTask create(SaleOrderLine saleOrderLine, Project project, User assignedTo)
      throws AxelorException {
    ProjectTask task = create(saleOrderLine.getFullName() + "_task", project, assignedTo);
    task.setProduct(saleOrderLine.getProduct());
    task.setUnit(saleOrderLine.getUnit());
    task.setCurrency(project.getClientPartner().getCurrency());
    if (project.getPriceList() != null) {
      PriceListLine line =
          priceListLineRepo.findByPriceListAndProduct(
              project.getPriceList(), saleOrderLine.getProduct());
      if (line != null) {
        task.setUnitPrice(line.getAmount());
      }
    }
    if (task.getUnitPrice() == null) {
      Company company =
          saleOrderLine.getSaleOrder() != null ? saleOrderLine.getSaleOrder().getCompany() : null;
      task.setUnitPrice(
          (BigDecimal) productCompanyService.get(saleOrderLine.getProduct(), "salePrice", company));
    }
    task.setDescription(saleOrderLine.getDescription());
    task.setQuantity(saleOrderLine.getQty());
    task.setSaleOrderLine(saleOrderLine);
    task.setToInvoice(
        saleOrderLine.getSaleOrder() != null
            ? saleOrderLine.getSaleOrder().getToInvoiceViaTask()
            : false);
    return task;
  }

  @Override
  public ProjectTask create(
      TaskTemplate template, Project project, LocalDateTime date, BigDecimal qty) {
    ProjectTask task = create(template.getName(), project, template.getAssignedTo());

    task.setTaskDate(date.toLocalDate());
    task.setTaskEndDate(date.plusHours(template.getDuration().longValue()).toLocalDate());

    BigDecimal plannedHrs = template.getTotalPlannedHrs();
    if (template.getIsUniqueTaskForMultipleQuantity() && qty.compareTo(BigDecimal.ONE) > 0) {
      plannedHrs = plannedHrs.multiply(qty);
      task.setName(task.getName() + " x" + qty.intValue());
    }
    task.setTotalPlannedHrs(plannedHrs);

    return task;
  }

  @Override
  public ProjectTask create(String subject, Project project, User assignedTo) {
    ProjectTask task = super.create(subject, project, assignedTo);
    task.setTaskDate(appBaseService.getTodayDate(project.getCompany()));
    return task;
  }

  @Override
  public ProjectTask updateDiscount(ProjectTask projectTask) {
    PriceList priceList = projectTask.getProject().getPriceList();
    if (priceList == null) {
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
            projectTask.getUnit(),
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
            projectTask.setInvoiceLine(invoiceLine);

            List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };

    return invoiceLineGenerator.creates();
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
    projectTask.setUnit(
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
          Set<ProjectStatus> preTaskStatusSet = appBusinessProject.getPreTaskStatusSet();
          projectTask.setToInvoice(
              ObjectUtils.notEmpty(preTaskStatusSet)
                  && preTaskStatusSet.contains(projectTask.getStatus()));
          break;

        case ProjectRepository.INVOICING_SEQ_INVOICE_POST_TASK:
          Set<ProjectStatus> postTaskStatusSet = appBusinessProject.getPostTaskStatusSet();
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
            projectTask.getProject().getClientPartner(), PriceListRepository.TYPE_SALE);
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
    projectTask.setUnit(null);
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
    if (projectTask.getSaleOrderLine() != null || projectTask.getInvoiceLine() != null) {
      return projectTask;
    }
    projectTask = updateTaskFinancialInfo(projectTask);
    return projectTaskRepo.save(projectTask);
  }
}
