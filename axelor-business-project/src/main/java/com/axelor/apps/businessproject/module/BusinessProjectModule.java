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
import com.axelor.apps.businessproject.db.repo.*;
import com.axelor.apps.businessproject.service.*;
import com.axelor.apps.businessproject.service.analytic.AnalyticLineModelProjectServiceImpl;
import com.axelor.apps.businessproject.service.analytic.ProjectAnalyticMoveLineService;
import com.axelor.apps.businessproject.service.analytic.ProjectAnalyticMoveLineServiceImpl;
import com.axelor.apps.businessproject.service.analytic.ProjectAnalyticTemplateService;
import com.axelor.apps.businessproject.service.analytic.ProjectAnalyticTemplateServiceImpl;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectServiceImpl;
import com.axelor.apps.businessproject.service.approvalitem.ApprovalItemManagementService;
import com.axelor.apps.businessproject.service.approvalitem.ApprovalItemManagementServiceImpl;
import com.axelor.apps.businessproject.service.config.BusinessProjectConfigService;
import com.axelor.apps.businessproject.service.config.BusinessProjectConfigServiceImpl;
import com.axelor.apps.businessproject.service.extraexpense.ExtraExpenseInvoiceService;
import com.axelor.apps.businessproject.service.extraexpense.ExtraExpenseInvoiceServiceImpl;
import com.axelor.apps.businessproject.service.extraexpense.ExtraExpenseLineService;
import com.axelor.apps.businessproject.service.extraexpense.ExtraExpenseLineServiceImpl;
import com.axelor.apps.businessproject.service.invoice.InvoiceMergingServiceBusinessProjectImpl;
import com.axelor.apps.businessproject.service.invoice.InvoicePrintBusinessProjectService;
import com.axelor.apps.businessproject.service.invoice.InvoicePrintBusinessProjectServiceImpl;
import com.axelor.apps.businessproject.service.invoice.breakdown.display.InvoiceBreakdownDisplayService;
import com.axelor.apps.businessproject.service.invoice.breakdown.display.InvoiceBreakdownDisplayServiceImpl;
import com.axelor.apps.businessproject.service.invoice.breakdown.print.InvoiceBreakdownPrintService;
import com.axelor.apps.businessproject.service.invoice.breakdown.print.InvoiceBreakdownPrintServiceImpl;
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
import com.axelor.apps.businessproject.service.pushnotification.TaskAssignmentNotificationRule;
import com.axelor.apps.businessproject.service.statuschange.ProjectStatusChangeService;
import com.axelor.apps.businessproject.service.statuschange.ProjectStatusChangeServiceImpl;
import com.axelor.apps.businessproject.service.statuschange.TaskStatusChangeService;
import com.axelor.apps.businessproject.service.statuschange.TaskStatusChangeServiceImpl;
import com.axelor.apps.businessproject.service.subcontractortask.SubcontractorTaskInvoiceService;
import com.axelor.apps.businessproject.service.subcontractortask.SubcontractorTaskInvoiceServiceImpl;
import com.axelor.apps.businessproject.service.taskreport.*;
import com.axelor.apps.contract.service.ContractInvoicingServiceImpl;
import com.axelor.apps.contract.service.ContractLineServiceImpl;
import com.axelor.apps.contract.service.PurchaseOrderInvoiceContractServiceImpl;
import com.axelor.apps.contract.service.SaleOrderInvoiceContractServiceImpl;
import com.axelor.apps.contract.service.WorkflowCancelServiceContractImpl;
import com.axelor.apps.contract.service.WorkflowVentilationContractServiceImpl;
import com.axelor.apps.hr.db.repo.ExpenseHRRepository;
import com.axelor.apps.hr.db.repo.ProjectHRRepository;
import com.axelor.apps.hr.db.repo.ProjectTaskHRRepository;
import com.axelor.apps.hr.db.repo.TimesheetLineHRRepository;
import com.axelor.apps.hr.event.ICalendarEventObserver;
import com.axelor.apps.hr.service.expense.*;
import com.axelor.apps.hr.service.expense.expenseline.ExpenseLineResponseComputeServiceImpl;
import com.axelor.apps.hr.service.project.TaskTemplateHrServiceImpl;
import com.axelor.apps.hr.service.timesheet.*;
import com.axelor.apps.project.db.repo.ProjectStatusRepository;
import com.axelor.apps.project.db.repo.TaskStatusRepository;
import com.axelor.apps.project.service.ProjectMenuServiceImpl;
import com.axelor.apps.project.service.ProjectServiceImpl;
import com.axelor.apps.project.service.ProjectTaskGroupServiceImpl;
import com.axelor.apps.project.service.ProjectTaskServiceImpl;
import com.axelor.apps.project.service.ProjectTemplateServiceImpl;
import com.axelor.apps.project.service.ProjectToolServiceImpl;
import com.axelor.apps.supplychain.db.repo.InvoiceSupplychainRepository;
import com.axelor.apps.supplychain.service.AnalyticLineModelServiceImpl;
import com.axelor.apps.supplychain.service.InvoiceLineSupplychainService;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderWorkflowServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.StockMoveInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.invoice.InvoiceMergingServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderPurchaseServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineInitValueSupplychainServiceImpl;
import com.axelor.studio.db.repo.AppBusinessProjectRepository;

public class BusinessProjectModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(SaleOrderInvoiceContractServiceImpl.class).to(SaleOrderInvoiceProjectServiceImpl.class);
    bind(PurchaseOrderInvoiceContractServiceImpl.class)
        .to(PurchaseOrderInvoiceProjectServiceImpl.class);
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
    bind(SaleOrderCopyProjectService.class).to(SaleOrderCopyProjectServiceImpl.class);
    bind(TaskReportExpenseService.class).to(TaskReportExpenseServiceImpl.class);
    bind(ExtraExpenseInvoiceService.class).to(ExtraExpenseInvoiceServiceImpl.class);
    bind(TaskReportRepository.class).to(TaskReportBusinessRepository.class);
    //    bind(InvoiceBreakdownDisplayService.class).to(InvoiceBreakdownDisplayServiceImpl.class);
    bind(TaskReportService.class).to(TaskReportServiceImpl.class);
    bind(TaskAssignmentNotificationRule.class).asEagerSingleton();
    bind(ProjectHRRepository.class).to(BusinessProjectManagementRepository.class);
    //    bind(InvoiceBreakdownPrintService.class).to(InvoiceBreakdownPrintServiceImpl.class);
    bind(TaskStatusChangeService.class).to(TaskStatusChangeServiceImpl.class);
    bind(TaskMemberReportService.class).to(TaskMemberReportServiceImpl.class);
    bind(ProjectStatusChangeService.class).to(ProjectStatusChangeServiceImpl.class);
    bind(ExpenseHRRepository.class).to(ExpenseBusinessProjectRepository.class);
    bind(TimesheetLineHRRepository.class).to(TimesheetLineBusinessProjectRepository.class);
    bind(TimesheetLineRemoveServiceImpl.class)
        .to(TimesheetLineRemoveBusinessProjectServiceImpl.class);
    bind(TaskStatusRepository.class).to(TaskStatusBusinessProjectRepository.class);
    bind(ProjectStatusRepository.class).to(ProjectStatusBusinessProjectRepository.class);
    bind(ApprovalItemManagementService.class).to(ApprovalItemManagementServiceImpl.class);
    bind(ProjectFilesService.class).to(ProjectFilesServiceImpl.class);
    bind(TaskMemberReportCreateService.class).to(TaskMemberReportCreateServiceImpl.class);
    bind(ExtraExpenseLineService.class).to(ExtraExpenseLineServiceImpl.class);
    bind(SubcontractorTaskInvoiceService.class).to(SubcontractorTaskInvoiceServiceImpl.class);
    bind(ExtraExpenseLineRepository.class).to(ExtraExpenseLineBusinessProjectRepository.class);
    bind(SubcontractorTaskRepository.class).to(SubcontractorTaskBusinessProjectRepository.class);
    bind(InvoiceBreakdownDisplayService.class).to(InvoiceBreakdownDisplayServiceImpl.class);
    bind(InvoiceBreakdownPrintService.class).to(InvoiceBreakdownPrintServiceImpl.class);
  }
}
