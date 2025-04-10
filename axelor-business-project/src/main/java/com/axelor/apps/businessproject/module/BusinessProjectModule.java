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
package com.axelor.apps.businessproject.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.service.invoice.InvoiceTermDateComputeServiceImpl;
import com.axelor.apps.account.service.invoice.print.InvoicePrintServiceImpl;
import com.axelor.apps.account.service.invoice.workflow.validate.WorkflowValidationServiceImpl;
import com.axelor.apps.bankpayment.service.invoice.payment.InvoicePaymentValidateServiceBankPayImpl;
import com.axelor.apps.businessproject.db.repo.AppBusinessProjectManagementRepository;
import com.axelor.apps.businessproject.db.repo.InvoiceProjectRepository;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectManagementRepository;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectRepository;
import com.axelor.apps.businessproject.db.repo.ProjectTaskBusinessProjectRepository;
import com.axelor.apps.businessproject.db.repo.SaleOrderProjectRepository;
import com.axelor.apps.businessproject.service.BusinessProjectClosingControlService;
import com.axelor.apps.businessproject.service.BusinessProjectClosingControlServiceImpl;
import com.axelor.apps.businessproject.service.BusinessProjectService;
import com.axelor.apps.businessproject.service.BusinessProjectServiceImpl;
import com.axelor.apps.businessproject.service.ContractLineServiceProjectImpl;
import com.axelor.apps.businessproject.service.ExpenseInvoiceLineServiceProjectImpl;
import com.axelor.apps.businessproject.service.ExpenseLineCreateServiceProjectImpl;
import com.axelor.apps.businessproject.service.ExpenseLineProjectService;
import com.axelor.apps.businessproject.service.ExpenseLineProjectServiceImpl;
import com.axelor.apps.businessproject.service.ExpenseLineResponseComputeServiceProjectImpl;
import com.axelor.apps.businessproject.service.ExpenseLineUpdateServiceProjectImpl;
import com.axelor.apps.businessproject.service.InvoiceLineProjectService;
import com.axelor.apps.businessproject.service.InvoiceLineProjectServiceImpl;
import com.axelor.apps.businessproject.service.InvoicePaymentValidateProjectServiceImpl;
import com.axelor.apps.businessproject.service.InvoiceServiceProject;
import com.axelor.apps.businessproject.service.InvoiceServiceProjectImpl;
import com.axelor.apps.businessproject.service.InvoiceTermDateComputeProjectServiceImpl;
import com.axelor.apps.businessproject.service.InvoicingProjectStockMovesService;
import com.axelor.apps.businessproject.service.InvoicingProjectStockMovesServiceImpl;
import com.axelor.apps.businessproject.service.ProductTaskTemplateService;
import com.axelor.apps.businessproject.service.ProductTaskTemplateServiceImpl;
import com.axelor.apps.businessproject.service.ProjectBusinessService;
import com.axelor.apps.businessproject.service.ProjectBusinessServiceImpl;
import com.axelor.apps.businessproject.service.ProjectContractInvoicingServiceImpl;
import com.axelor.apps.businessproject.service.ProjectFrameworkContractService;
import com.axelor.apps.businessproject.service.ProjectFrameworkContractServiceImpl;
import com.axelor.apps.businessproject.service.ProjectGenerateInvoiceService;
import com.axelor.apps.businessproject.service.ProjectGenerateInvoiceServiceImpl;
import com.axelor.apps.businessproject.service.ProjectHistoryService;
import com.axelor.apps.businessproject.service.ProjectHistoryServiceImpl;
import com.axelor.apps.businessproject.service.ProjectHoldBackLineService;
import com.axelor.apps.businessproject.service.ProjectHoldBackLineServiceImpl;
import com.axelor.apps.businessproject.service.ProjectMenuBusinessServiceImpl;
import com.axelor.apps.businessproject.service.ProjectPurchaseServiceImpl;
import com.axelor.apps.businessproject.service.ProjectRestService;
import com.axelor.apps.businessproject.service.ProjectRestServiceImpl;
import com.axelor.apps.businessproject.service.ProjectStockMoveInvoiceServiceImpl;
import com.axelor.apps.businessproject.service.ProjectTemplateBusinessServiceImpl;
import com.axelor.apps.businessproject.service.ProjectToolBusinessProjectServiceImpl;
import com.axelor.apps.businessproject.service.PurchaseOrderInvoiceProjectServiceImpl;
import com.axelor.apps.businessproject.service.PurchaseOrderLineProjectService;
import com.axelor.apps.businessproject.service.PurchaseOrderLineServiceProjectImpl;
import com.axelor.apps.businessproject.service.PurchaseOrderProjectService;
import com.axelor.apps.businessproject.service.PurchaseOrderServiceProjectImpl;
import com.axelor.apps.businessproject.service.PurchaseOrderWorkflowServiceProjectImpl;
import com.axelor.apps.businessproject.service.SaleOrderInvoiceProjectServiceImpl;
import com.axelor.apps.businessproject.service.SaleOrderLineDomainProjectService;
import com.axelor.apps.businessproject.service.SaleOrderLineDomainProjectServiceImpl;
import com.axelor.apps.businessproject.service.SaleOrderLineInitValueProjectServiceImpl;
import com.axelor.apps.businessproject.service.SaleOrderLineProjectService;
import com.axelor.apps.businessproject.service.SaleOrderLineProjectServiceImpl;
import com.axelor.apps.businessproject.service.SaleOrderLineViewProjectService;
import com.axelor.apps.businessproject.service.SaleOrderLineViewProjectServiceImpl;
import com.axelor.apps.businessproject.service.TimesheetLineBusinessService;
import com.axelor.apps.businessproject.service.TimesheetLineCreateProjectServiceImpl;
import com.axelor.apps.businessproject.service.TimesheetLineProjectServiceImpl;
import com.axelor.apps.businessproject.service.TimesheetProjectInvoiceServiceImpl;
import com.axelor.apps.businessproject.service.TimesheetProjectPPTServiceImpl;
import com.axelor.apps.businessproject.service.TimesheetProjectService;
import com.axelor.apps.businessproject.service.TimesheetProjectServiceImpl;
import com.axelor.apps.businessproject.service.WorkflowCancelServiceProjectImpl;
import com.axelor.apps.businessproject.service.WorkflowValidationServiceProjectImpl;
import com.axelor.apps.businessproject.service.WorkflowVentilationProjectServiceImpl;
import com.axelor.apps.businessproject.service.analytic.AnalyticLineModelProjectServiceImpl;
import com.axelor.apps.businessproject.service.analytic.ProjectAnalyticMoveLineService;
import com.axelor.apps.businessproject.service.analytic.ProjectAnalyticMoveLineServiceImpl;
import com.axelor.apps.businessproject.service.analytic.ProjectAnalyticTemplateService;
import com.axelor.apps.businessproject.service.analytic.ProjectAnalyticTemplateServiceImpl;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectServiceImpl;
import com.axelor.apps.businessproject.service.config.BusinessProjectConfigService;
import com.axelor.apps.businessproject.service.config.BusinessProjectConfigServiceImpl;
import com.axelor.apps.businessproject.service.invoice.InvoiceMergingServiceBusinessProjectImpl;
import com.axelor.apps.businessproject.service.invoice.InvoicePrintBusinessProjectService;
import com.axelor.apps.businessproject.service.invoice.InvoicePrintBusinessProjectServiceImpl;
import com.axelor.apps.businessproject.service.observer.SaleOrderLineProjectObserver;
import com.axelor.apps.businessproject.service.projectgenerator.factory.ProjectGeneratorSaleService;
import com.axelor.apps.businessproject.service.projectgenerator.factory.ProjectGeneratorSaleServiceImpl;
import com.axelor.apps.businessproject.service.projecttask.ProjectTaskBusinessProjectService;
import com.axelor.apps.businessproject.service.projecttask.ProjectTaskBusinessProjectServiceImpl;
import com.axelor.apps.businessproject.service.projecttask.ProjectTaskComputeBusinessService;
import com.axelor.apps.businessproject.service.projecttask.ProjectTaskComputeBusinessServiceImpl;
import com.axelor.apps.businessproject.service.projecttask.ProjectTaskGroupBusinessService;
import com.axelor.apps.businessproject.service.projecttask.ProjectTaskGroupBusinessServiceImpl;
import com.axelor.apps.businessproject.service.projecttask.ProjectTaskProgressUpdateService;
import com.axelor.apps.businessproject.service.projecttask.ProjectTaskProgressUpdateServiceImpl;
import com.axelor.apps.businessproject.service.projecttask.ProjectTaskReportingValuesComputingService;
import com.axelor.apps.businessproject.service.projecttask.ProjectTaskReportingValuesComputingServiceImpl;
import com.axelor.apps.businessproject.service.projecttask.TaskTemplateBusinessProjectServiceImpl;
import com.axelor.apps.contract.service.ContractInvoicingServiceImpl;
import com.axelor.apps.contract.service.ContractLineServiceImpl;
import com.axelor.apps.contract.service.WorkflowCancelServiceContractImpl;
import com.axelor.apps.contract.service.WorkflowVentilationContractServiceImpl;
import com.axelor.apps.hr.db.repo.ProjectTaskHRRepository;
import com.axelor.apps.hr.event.ICalendarEventObserver;
import com.axelor.apps.hr.service.expense.ExpenseInvoiceLineServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseLineCreateServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseLineUpdateServiceImpl;
import com.axelor.apps.hr.service.expense.expenseline.ExpenseLineResponseComputeServiceImpl;
import com.axelor.apps.hr.service.project.TaskTemplateHrServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetInvoiceServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetLineCreateServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetLineServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetProjectPlanningTimeServiceImpl;
import com.axelor.apps.project.service.ProjectMenuServiceImpl;
import com.axelor.apps.project.service.ProjectServiceImpl;
import com.axelor.apps.project.service.ProjectTaskGroupServiceImpl;
import com.axelor.apps.project.service.ProjectTaskServiceImpl;
import com.axelor.apps.project.service.ProjectTemplateServiceImpl;
import com.axelor.apps.project.service.ProjectToolServiceImpl;
import com.axelor.apps.supplychain.db.repo.InvoiceSupplychainRepository;
import com.axelor.apps.supplychain.db.repo.SaleOrderSupplychainRepository;
import com.axelor.apps.supplychain.service.AnalyticLineModelServiceImpl;
import com.axelor.apps.supplychain.service.InvoiceLineSupplychainService;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderWorkflowServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.StockMoveInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.invoice.InvoiceMergingServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderPurchaseServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineInitValueSupplychainServiceImpl;
import com.axelor.studio.db.repo.AppBusinessProjectRepository;

public class BusinessProjectModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(SaleOrderInvoiceServiceImpl.class).to(SaleOrderInvoiceProjectServiceImpl.class);
    bind(PurchaseOrderInvoiceServiceImpl.class).to(PurchaseOrderInvoiceProjectServiceImpl.class);
    bind(TimesheetProjectService.class).to(TimesheetProjectServiceImpl.class);
    bind(TimesheetLineServiceImpl.class).to(TimesheetLineProjectServiceImpl.class);
    bind(ExpenseInvoiceLineServiceImpl.class).to(ExpenseInvoiceLineServiceProjectImpl.class);
    bind(ProjectServiceImpl.class).to(ProjectBusinessServiceImpl.class);
    bind(ProjectBusinessService.class).to(ProjectBusinessServiceImpl.class);
    bind(InvoicingProjectRepository.class).to(InvoicingProjectManagementRepository.class);
    bind(AppBusinessProjectService.class).to(AppBusinessProjectServiceImpl.class);
    bind(InvoiceServiceSupplychainImpl.class).to(InvoiceServiceProjectImpl.class);
    bind(InvoiceServiceProject.class).to(InvoiceServiceProjectImpl.class);
    bind(ProjectTaskServiceImpl.class).to(ProjectTaskBusinessProjectServiceImpl.class);
    bind(ProjectTaskBusinessProjectService.class).to(ProjectTaskBusinessProjectServiceImpl.class);
    bind(SaleOrderSupplychainRepository.class).to(SaleOrderProjectRepository.class);
    bind(ProductTaskTemplateService.class).to(ProductTaskTemplateServiceImpl.class);
    bind(StockMoveInvoiceServiceImpl.class).to(ProjectStockMoveInvoiceServiceImpl.class);
    bind(SaleOrderPurchaseServiceImpl.class).to(ProjectPurchaseServiceImpl.class);
    bind(PurchaseOrderLineServiceSupplyChainImpl.class)
        .to(PurchaseOrderLineServiceProjectImpl.class);
    bind(SaleOrderLineProjectService.class).to(SaleOrderLineProjectServiceImpl.class);
    bind(PurchaseOrderLineProjectService.class).to(PurchaseOrderLineServiceProjectImpl.class);
    bind(ExpenseLineProjectService.class).to(ExpenseLineProjectServiceImpl.class);
    bind(InvoiceLineProjectService.class).to(InvoiceLineProjectServiceImpl.class);
    bind(InvoiceSupplychainRepository.class).to(InvoiceProjectRepository.class);
    bind(WorkflowVentilationContractServiceImpl.class)
        .to(WorkflowVentilationProjectServiceImpl.class);
    bind(TimesheetLineBusinessService.class).to(TimesheetLineProjectServiceImpl.class);
    bind(WorkflowValidationServiceImpl.class).to(WorkflowValidationServiceProjectImpl.class);
    bind(WorkflowCancelServiceContractImpl.class).to(WorkflowCancelServiceProjectImpl.class);
    bind(ProjectTaskHRRepository.class).to(ProjectTaskBusinessProjectRepository.class);
    bind(InvoiceLineSupplychainService.class).to(InvoiceLineProjectServiceImpl.class);
    bind(ContractInvoicingServiceImpl.class).to(ProjectContractInvoicingServiceImpl.class);
    bind(ContractLineServiceImpl.class).to(ContractLineServiceProjectImpl.class);
    bind(AppBusinessProjectRepository.class).to(AppBusinessProjectManagementRepository.class);
    bind(InvoicePaymentValidateServiceBankPayImpl.class)
        .to(InvoicePaymentValidateProjectServiceImpl.class);
    bind(ProjectAnalyticMoveLineService.class).to(ProjectAnalyticMoveLineServiceImpl.class);
    bind(PurchaseOrderWorkflowServiceSupplychainImpl.class)
        .to(PurchaseOrderWorkflowServiceProjectImpl.class);
    bind(InvoiceMergingServiceSupplychainImpl.class)
        .to(InvoiceMergingServiceBusinessProjectImpl.class);
    bind(ProjectTaskReportingValuesComputingService.class)
        .to(ProjectTaskReportingValuesComputingServiceImpl.class);
    bind(ProjectHistoryService.class).to(ProjectHistoryServiceImpl.class);
    bind(PurchaseOrderProjectService.class).to(PurchaseOrderServiceProjectImpl.class);
    bind(ExpenseLineCreateServiceImpl.class).to(ExpenseLineCreateServiceProjectImpl.class);
    bind(ExpenseLineResponseComputeServiceImpl.class)
        .to(ExpenseLineResponseComputeServiceProjectImpl.class);
    bind(AnalyticLineModelServiceImpl.class).to(AnalyticLineModelProjectServiceImpl.class);
    bind(TimesheetProjectPlanningTimeServiceImpl.class).to(TimesheetProjectPPTServiceImpl.class);
    bind(TimesheetInvoiceServiceImpl.class).to(TimesheetProjectInvoiceServiceImpl.class);
    bind(ExpenseLineUpdateServiceImpl.class).to(ExpenseLineUpdateServiceProjectImpl.class);
    bind(TimesheetLineCreateServiceImpl.class).to(TimesheetLineCreateProjectServiceImpl.class);
    bind(InvoicingProjectStockMovesService.class).to(InvoicingProjectStockMovesServiceImpl.class);
    bind(ProjectHoldBackLineService.class).to(ProjectHoldBackLineServiceImpl.class);
    bind(ProjectTaskProgressUpdateService.class).to(ProjectTaskProgressUpdateServiceImpl.class);
    bind(ProjectFrameworkContractService.class).to(ProjectFrameworkContractServiceImpl.class);
    bind(ICalendarEventObserver.class);
    bind(BusinessProjectClosingControlService.class)
        .to(BusinessProjectClosingControlServiceImpl.class);
    bind(ProjectGenerateInvoiceService.class).to(ProjectGenerateInvoiceServiceImpl.class);
    bind(BusinessProjectConfigService.class).to(BusinessProjectConfigServiceImpl.class);
    bind(ProjectAnalyticTemplateService.class).to(ProjectAnalyticTemplateServiceImpl.class);
    bind(InvoicePrintServiceImpl.class).to(InvoicePrintBusinessProjectServiceImpl.class);
    bind(InvoicePrintBusinessProjectService.class).to(InvoicePrintBusinessProjectServiceImpl.class);
    bind(ProjectTemplateServiceImpl.class).to(ProjectTemplateBusinessServiceImpl.class);
    bind(ProjectMenuServiceImpl.class).to(ProjectMenuBusinessServiceImpl.class);
    bind(SaleOrderLineDomainProjectService.class).to(SaleOrderLineDomainProjectServiceImpl.class);
    bind(SaleOrderLineViewProjectService.class).to(SaleOrderLineViewProjectServiceImpl.class);
    bind(SaleOrderLineProjectObserver.class);
    bind(SaleOrderLineInitValueSupplychainServiceImpl.class)
        .to(SaleOrderLineInitValueProjectServiceImpl.class);
    bind(BusinessProjectService.class).to(BusinessProjectServiceImpl.class);
    bind(ProjectRestService.class).to(ProjectRestServiceImpl.class);

    bind(ProjectTaskGroupBusinessService.class).to(ProjectTaskGroupBusinessServiceImpl.class);
    bind(ProjectTaskGroupServiceImpl.class).to(ProjectTaskGroupBusinessServiceImpl.class);
    bind(ProjectTaskComputeBusinessService.class).to(ProjectTaskComputeBusinessServiceImpl.class);

    bind(ProjectToolServiceImpl.class).to(ProjectToolBusinessProjectServiceImpl.class);
    bind(TaskTemplateHrServiceImpl.class).to(TaskTemplateBusinessProjectServiceImpl.class);
    bind(ProjectGeneratorSaleService.class).to(ProjectGeneratorSaleServiceImpl.class);
    bind(InvoiceTermDateComputeServiceImpl.class)
        .to(InvoiceTermDateComputeProjectServiceImpl.class);
  }
}
