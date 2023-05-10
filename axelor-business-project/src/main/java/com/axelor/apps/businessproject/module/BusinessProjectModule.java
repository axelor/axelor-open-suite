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
package com.axelor.apps.businessproject.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.service.invoice.workflow.validate.WorkflowValidationServiceImpl;
import com.axelor.apps.bankpayment.service.invoice.payment.InvoicePaymentValidateServiceBankPayImpl;
import com.axelor.apps.businessproject.db.repo.AppBusinessProjectManagementRepository;
import com.axelor.apps.businessproject.db.repo.InvoiceProjectRepository;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectManagementRepository;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectRepository;
import com.axelor.apps.businessproject.db.repo.ProjectTaskBusinessProjectRepository;
import com.axelor.apps.businessproject.db.repo.SaleOrderProjectRepository;
import com.axelor.apps.businessproject.service.*;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectServiceImpl;
import com.axelor.apps.businessproject.service.invoice.InvoiceMergingServiceBusinessProjectImpl;
import com.axelor.apps.contract.service.ContractLineServiceImpl;
import com.axelor.apps.contract.service.ContractServiceImpl;
import com.axelor.apps.contract.service.WorkflowCancelServiceContractImpl;
import com.axelor.apps.hr.db.repo.ProjectTaskHRRepository;
import com.axelor.apps.hr.service.expense.ExpenseServiceImpl;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetLineServiceImpl;
import com.axelor.apps.hr.service.timesheet.TimesheetServiceImpl;
import com.axelor.apps.project.service.ProjectServiceImpl;
import com.axelor.apps.project.service.ProjectTaskServiceImpl;
import com.axelor.apps.supplychain.db.repo.InvoiceSupplychainRepository;
import com.axelor.apps.supplychain.db.repo.SaleOrderSupplychainRepository;
import com.axelor.apps.supplychain.service.InvoiceLineSupplychainService;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderWorkflowServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderLineServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.SaleOrderPurchaseServiceImpl;
import com.axelor.apps.supplychain.service.StockMoveInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.invoice.InvoiceMergingServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.workflow.WorkflowVentilationServiceSupplychainImpl;
import com.axelor.studio.db.repo.AppBusinessProjectRepository;

public class BusinessProjectModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(SaleOrderInvoiceServiceImpl.class).to(SaleOrderInvoiceProjectServiceImpl.class);
    bind(PurchaseOrderInvoiceServiceImpl.class).to(PurchaseOrderInvoiceProjectServiceImpl.class);
    bind(TimesheetServiceImpl.class).to(TimesheetProjectServiceImpl.class);
    bind(TimesheetProjectService.class).to(TimesheetProjectServiceImpl.class);
    bind(TimesheetLineServiceImpl.class).to(TimesheetLineProjectServiceImpl.class);
    bind(ExpenseServiceImpl.class).to(ExpenseServiceProjectImpl.class);
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
    bind(PurchaseOrderLineServiceSupplychainImpl.class)
        .to(PurchaseOrderLineServiceProjectImpl.class);
    bind(SaleOrderLineProjectService.class).to(SaleOrderLineProjectServiceImpl.class);
    bind(PurchaseOrderLineProjectService.class).to(PurchaseOrderLineServiceProjectImpl.class);
    bind(ExpenseLineProjectService.class).to(ExpenseLineProjectServiceImpl.class);
    bind(InvoiceLineProjectService.class).to(InvoiceLineProjectServiceImpl.class);
    bind(InvoiceSupplychainRepository.class).to(InvoiceProjectRepository.class);
    bind(WorkflowVentilationServiceSupplychainImpl.class)
        .to(WorkflowVentilationProjectServiceImpl.class);
    bind(TimesheetLineBusinessService.class).to(TimesheetLineProjectServiceImpl.class);
    bind(WorkflowValidationServiceImpl.class).to(WorkflowValidationServiceProjectImpl.class);
    bind(WorkflowCancelServiceContractImpl.class).to(WorkflowCancelServiceProjectImpl.class);
    bind(ProjectTaskHRRepository.class).to(ProjectTaskBusinessProjectRepository.class);
    bind(SaleOrderLineServiceSupplyChainImpl.class).to(SaleOrderLineProjectServiceImpl.class);
    bind(InvoiceLineSupplychainService.class).to(InvoiceLineProjectServiceImpl.class);
    bind(ContractServiceImpl.class).to(ProjectContractServiceImpl.class);
    bind(ContractLineServiceImpl.class).to(ContractLineServiceProjectImpl.class);
    bind(AppBusinessProjectRepository.class).to(AppBusinessProjectManagementRepository.class);
    bind(InvoicePaymentValidateServiceBankPayImpl.class)
        .to(InvoicePaymentValidateProjectServiceImpl.class);
    bind(ProjectAnalyticMoveLineService.class).to(ProjectAnalyticMoveLineServiceImpl.class);
    bind(PurchaseOrderWorkflowServiceSupplychainImpl.class)
        .to(PurchaseOrderWorkflowServiceProjectImpl.class);
    bind(InvoiceMergingServiceSupplychainImpl.class)
        .to(InvoiceMergingServiceBusinessProjectImpl.class);
    bind(ProjectPlanningTimeServiceImpl.class)
        .to(ProjectPlanningTimeBusinessProjectServiceImpl.class);
  }
}
