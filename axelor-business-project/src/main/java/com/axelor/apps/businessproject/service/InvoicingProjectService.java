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
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.print.InvoicePrintServiceImpl;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.printing.template.PrintingTemplateHelper;
import com.axelor.apps.base.service.printing.template.PrintingTemplatePrintService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.db.repo.BusinessProjectBatchRepository;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectRepository;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.businessproject.service.projecttask.ProjectTaskBusinessProjectService;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.apps.project.service.ProjectServiceImpl;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.studio.db.AppBusinessProject;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class InvoicingProjectService {

  @Inject protected ProjectTaskBusinessProjectService projectTaskBusinessProjectService;

  @Inject protected InvoicingProjectRepository invoicingProjectRepo;

  @Inject protected AppBusinessProjectService appBusinessProjectService;

  @Inject protected TimesheetLineBusinessService timesheetLineBusinessService;

  @Inject protected InvoicingProjectStockMovesService invoicingProjectStockMovesService;

  @Inject protected SaleOrderLineRepository saleOrderLineRepository;

  public void setLines(InvoicingProject invoicingProject, Project project, int counter) {
    AppBusinessProject appBusinessProject = appBusinessProjectService.getAppBusinessProject();
    if (appBusinessProject.getAutomaticInvoicing()) {
      projectTaskBusinessProjectService.taskInvoicing(project, appBusinessProject);
      timesheetLineBusinessService.timsheetLineInvoicing(project);
    }

    if (counter > ProjectServiceImpl.MAX_LEVEL_OF_PROJECT) {
      return;
    }
    counter++;

    this.fillLines(invoicingProject, project);

    if (!invoicingProject.getConsolidatePhaseWhenInvoicing()) {
      return;
    }

    List<Project> projectChildrenList =
        Beans.get(ProjectRepository.class).all().filter("self.parentProject = ?1", project).fetch();

    for (Project projectChild : projectChildrenList) {
      this.setLines(invoicingProject, projectChild, counter);
    }

    return;
  }

  public void fillLines(InvoicingProject invoicingProject, Project project) {
    String commonQuery =
        "self.project = :project AND self.toInvoice = true AND self.invoiced = false";

    StringBuilder solQueryBuilder = new StringBuilder(commonQuery);
    solQueryBuilder.append(
        " AND (self.saleOrder.statusSelect = :statusConfirmed OR self.saleOrder.statusSelect = :statusCompleted)");

    Map<String, Object> solQueryMap = new HashMap<>();
    solQueryMap.put("project", project);
    solQueryMap.put("statusConfirmed", SaleOrderRepository.STATUS_ORDER_CONFIRMED);
    solQueryMap.put("statusCompleted", SaleOrderRepository.STATUS_ORDER_COMPLETED);

    StringBuilder polQueryBuilder = new StringBuilder(commonQuery);
    polQueryBuilder.append(
        " AND (self.purchaseOrder.statusSelect = :statusValidated OR self.purchaseOrder.statusSelect = :statusFinished)");

    Map<String, Object> polQueryMap = new HashMap<>();
    polQueryMap.put("project", project);
    polQueryMap.put("statusValidated", PurchaseOrderRepository.STATUS_VALIDATED);
    polQueryMap.put("statusFinished", PurchaseOrderRepository.STATUS_FINISHED);

    if (project.getManageTimeSpent()) {
      StringBuilder logTimesQueryBuilder = new StringBuilder(commonQuery);
      Map<String, Object> logTimesQueryMap = new HashMap<>();
      logTimesQueryMap.put("project", project);

      logTimesQueryBuilder.append(" AND self.timesheet.statusSelect = :statusValidated");
      logTimesQueryMap.put("statusValidated", TimesheetRepository.STATUS_VALIDATED);

      if (invoicingProject.getDeadlineDate() != null) {
        logTimesQueryBuilder.append(" AND self.date <= :deadlineDate");
        logTimesQueryMap.put("deadlineDate", invoicingProject.getDeadlineDate());
      }

      invoicingProject
          .getLogTimesSet()
          .addAll(
              Beans.get(TimesheetLineRepository.class)
                  .all()
                  .filter(logTimesQueryBuilder.toString())
                  .bind(logTimesQueryMap)
                  .fetch());
    }

    StringBuilder expenseLineQueryBuilder = new StringBuilder(commonQuery);
    expenseLineQueryBuilder.append(
        " AND (self.expense.statusSelect = :statusValidated OR self.expense.statusSelect = :statusReimbursed)");

    Map<String, Object> expenseLineQueryMap = new HashMap<>();
    expenseLineQueryMap.put("project", project);
    expenseLineQueryMap.put("statusValidated", ExpenseRepository.STATUS_VALIDATED);
    expenseLineQueryMap.put("statusReimbursed", ExpenseRepository.STATUS_REIMBURSED);

    StringBuilder taskQueryBuilder = new StringBuilder(commonQuery);
    taskQueryBuilder.append(
        " AND (self.invoicingType = :invoicingTypePackage OR self.invoicingType = :invoicingTypeProgress)");
    Map<String, Object> taskQueryMap = new HashMap<>();
    taskQueryMap.put("project", project);
    taskQueryMap.put("invoicingTypePackage", ProjectTaskRepository.INVOICING_TYPE_PACKAGE);
    taskQueryMap.put("invoicingTypeProgress", ProjectTaskRepository.INVOICING_TYPE_ON_PROGRESS);

    if (invoicingProject.getDeadlineDate() != null) {
      solQueryBuilder.append(" AND self.saleOrder.creationDate <= :deadlineDate");
      solQueryMap.put("deadlineDate", invoicingProject.getDeadlineDate());

      polQueryBuilder.append(" AND self.purchaseOrder.orderDate <= :deadlineDate");
      polQueryMap.put("deadlineDate", invoicingProject.getDeadlineDate());

      expenseLineQueryBuilder.append(" AND self.expenseDate <= :deadlineDate");
      expenseLineQueryMap.put("deadlineDate", invoicingProject.getDeadlineDate());
    }

    List<SaleOrderLine> saleOrderLineList =
        saleOrderLineRepository.all().filter(solQueryBuilder.toString()).bind(solQueryMap).fetch();

    invoicingProject
        .getSaleOrderLineSet()
        .addAll(
            saleOrderLineList.stream()
                .filter(
                    sol ->
                        sol.getInvoicingModeSelect()
                            == SaleOrderLineRepository.INVOICING_MODE_DIRECTLY)
                .collect(Collectors.toList()));

    invoicingProject
        .getPurchaseOrderLineSet()
        .addAll(
            Beans.get(PurchaseOrderLineRepository.class)
                .all()
                .filter(polQueryBuilder.toString())
                .bind(polQueryMap)
                .fetch());

    invoicingProject
        .getExpenseLineSet()
        .addAll(
            Beans.get(ExpenseLineRepository.class)
                .all()
                .filter(expenseLineQueryBuilder.toString())
                .bind(expenseLineQueryMap)
                .fetch());

    invoicingProject
        .getProjectTaskSet()
        .addAll(
            Beans.get(ProjectTaskRepository.class)
                .all()
                .filter(taskQueryBuilder.toString())
                .bind(taskQueryMap)
                .fetch());

    Set<StockMoveLine> stockMoveLineSet =
        invoicingProjectStockMovesService.processDeliveredSaleOrderLines(saleOrderLineList);
    invoicingProject.getStockMoveLineSet().addAll(stockMoveLineSet);
  }

  public void clearLines(InvoicingProject invoicingProject) {

    invoicingProject.setSaleOrderLineSet(new HashSet<SaleOrderLine>());
    invoicingProject.setPurchaseOrderLineSet(new HashSet<PurchaseOrderLine>());
    invoicingProject.setLogTimesSet(new HashSet<TimesheetLine>());
    invoicingProject.setExpenseLineSet(new HashSet<ExpenseLine>());
    invoicingProject.setProjectTaskSet(new HashSet<ProjectTask>());
    invoicingProject.setStockMoveLineSet(new HashSet<StockMoveLine>());
  }

  public Company getRootCompany(Project project) {
    if (project.getParentProject() == null) {
      return project.getCompany();
    } else {
      return getRootCompany(project.getParentProject());
    }
  }

  public int countToInvoice(Project project) {

    int toInvoiceCount = 0;

    String query = "self.project = ?1";

    if (project.getIsShowPhasesElements()) {
      query = "(self.project = ?1 OR self.project.parentProject = ?1)";
    }

    query += " AND self.toInvoice = true AND self.invoiced = false";

    toInvoiceCount += Beans.get(SaleOrderLineRepository.class).all().filter(query, project).count();

    toInvoiceCount +=
        Beans.get(PurchaseOrderLineRepository.class).all().filter(query, project).count();

    toInvoiceCount += Beans.get(ExpenseLineRepository.class).all().filter(query, project).count();

    toInvoiceCount += Beans.get(TimesheetLineRepository.class).all().filter(query, project).count();

    toInvoiceCount += Beans.get(ProjectTaskRepository.class).all().filter(query, project).count();

    return toInvoiceCount;
  }

  public void generateAnnex(InvoicingProject invoicingProject) throws AxelorException, IOException {
    PrintingTemplate invoicingProjectAnnexPrintTemplate =
        appBusinessProjectService.getAppBusinessProject().getInvoicingProjectAnnexPrintTemplate();
    if (invoicingProjectAnnexPrintTemplate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.TEMPLATE_CONFIG_NOT_FOUND));
    }

    File file =
        Beans.get(PrintingTemplatePrintService.class)
            .getPrintFile(
                invoicingProjectAnnexPrintTemplate,
                new PrintingGenFactoryContext(invoicingProject));

    MetaFiles metaFiles = Beans.get(MetaFiles.class);
    String fileName = file.getName();
    if (invoicingProject.getAttachAnnexToInvoice()) {
      List<File> fileList = new ArrayList<>();
      Invoice invoice = invoicingProject.getInvoice();
      fileList.add(
          Beans.get(InvoicePrintServiceImpl.class)
              .print(
                  invoice,
                  null,
                  Beans.get(AccountConfigService.class)
                      .getInvoicePrintTemplate(invoice.getCompany()),
                  null));
      metaFiles.attach(new FileInputStream(file), file.getName(), invoice);
      fileList.add(file);
      file = PrintingTemplateHelper.mergeToFile(fileList, Files.getNameWithoutExtension(fileName));
    }
    metaFiles.attach(new FileInputStream(file), fileName, invoicingProject);
  }

  protected String getTimezone(InvoicingProject invoicingProject) {
    if (invoicingProject.getProject() == null
        || invoicingProject.getProject().getCompany() == null) {
      return null;
    }
    return invoicingProject.getProject().getCompany().getTimezone();
  }

  @Transactional
  public InvoicingProject generateInvoicingProject(Project project, int consolidatePhaseSelect) {
    if (project == null) {
      return null;
    }
    InvoicingProject invoicingProject = new InvoicingProject();

    clearLines(invoicingProject);
    setLines(invoicingProject, project, 0);

    if (invoicingProject.getSaleOrderLineSet().isEmpty()
        && invoicingProject.getPurchaseOrderLineSet().isEmpty()
        && invoicingProject.getLogTimesSet().isEmpty()
        && invoicingProject.getExpenseLineSet().isEmpty()
        && invoicingProject.getProjectTaskSet().isEmpty()) {

      return invoicingProject;
    }

    project = JPA.find(Project.class, project.getId());
    invoicingProject.setProject(project);

    if (consolidatePhaseSelect
        == BusinessProjectBatchRepository.CONSOLIDATE_PHASE_CONSOLIDATE_ALL) {
      invoicingProject.setConsolidatePhaseWhenInvoicing(true);
    } else if (consolidatePhaseSelect
        == BusinessProjectBatchRepository.CONSOLIDATE_PHASE_DONT_CONSOLIDATE) {
      invoicingProject.setConsolidatePhaseWhenInvoicing(false);
    } else if (consolidatePhaseSelect
        == BusinessProjectBatchRepository.CONSOLIDATE_PHASE_DEFAULT_VALUE) {
      invoicingProject.setConsolidatePhaseWhenInvoicing(
          invoicingProject.getProject().getConsolidatePhaseWhenInvoicing());
    }
    return invoicingProjectRepo.save(invoicingProject);
  }
}
