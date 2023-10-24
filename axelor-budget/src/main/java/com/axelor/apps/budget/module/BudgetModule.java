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
package com.axelor.apps.budget.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.bankpayment.db.repo.MoveBankPaymentRepository;
import com.axelor.apps.bankpayment.service.move.MoveRemoveServiceBankPaymentImpl;
import com.axelor.apps.bankpayment.service.moveline.MoveLineGroupBankPaymentServiceImpl;
import com.axelor.apps.base.db.repo.AdvancedExportRepository;
import com.axelor.apps.base.db.repo.AdvancedImportBaseRepository;
import com.axelor.apps.budget.db.repo.AdvancedExportBudgetRepository;
import com.axelor.apps.budget.db.repo.AdvancedImportBudgetRepository;
import com.axelor.apps.budget.db.repo.BudgetInvoiceRepository;
import com.axelor.apps.budget.db.repo.BudgetLevelManagementRepository;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.db.repo.BudgetManagementRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.db.repo.MoveBudgetManagementRepository;
import com.axelor.apps.budget.db.repo.PurchaseOrderManagementBudgetRepository;
import com.axelor.apps.budget.export.ExportGlobalBudgetLevelService;
import com.axelor.apps.budget.export.ExportGlobalBudgetLevelServiceImpl;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.AppBudgetServiceImpl;
import com.axelor.apps.budget.service.BudgetAccountConfigService;
import com.axelor.apps.budget.service.BudgetAccountConfigServiceImpl;
import com.axelor.apps.budget.service.BudgetAccountService;
import com.axelor.apps.budget.service.BudgetAccountServiceImpl;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetDistributionServiceImpl;
import com.axelor.apps.budget.service.BudgetLevelService;
import com.axelor.apps.budget.service.BudgetLevelServiceImpl;
import com.axelor.apps.budget.service.BudgetLineService;
import com.axelor.apps.budget.service.BudgetLineServiceImpl;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetServiceImpl;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.BudgetToolsServiceImpl;
import com.axelor.apps.budget.service.invoice.BudgetInvoiceLineService;
import com.axelor.apps.budget.service.invoice.BudgetInvoiceLineServiceImpl;
import com.axelor.apps.budget.service.invoice.BudgetInvoiceService;
import com.axelor.apps.budget.service.invoice.BudgetInvoiceServiceImpl;
import com.axelor.apps.budget.service.invoice.StockMoveInvoiceBudgetServiceImpl;
import com.axelor.apps.budget.service.invoice.WorkflowCancelBudgetServiceImpl;
import com.axelor.apps.budget.service.invoice.WorkflowValidationBudgetServiceImpl;
import com.axelor.apps.budget.service.invoice.WorkflowVentilationBudgetServiceImpl;
import com.axelor.apps.budget.service.move.MoveBudgetService;
import com.axelor.apps.budget.service.move.MoveBudgetServiceImpl;
import com.axelor.apps.budget.service.move.MoveLineBudgetService;
import com.axelor.apps.budget.service.move.MoveLineBudgetServiceImpl;
import com.axelor.apps.budget.service.move.MoveLineGroupBudgetServiceImpl;
import com.axelor.apps.budget.service.move.MoveRemoveBudgetService;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderBudgetService;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderBudgetServiceImpl;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderInvoiceBudgetServiceImpl;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderLineBudgetService;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderLineBudgetServiceImpl;
import com.axelor.apps.budget.service.saleorder.SaleOrderBudgetService;
import com.axelor.apps.budget.service.saleorder.SaleOrderBudgetServiceImpl;
import com.axelor.apps.budget.service.saleorder.SaleOrderLineBudgetService;
import com.axelor.apps.budget.service.saleorder.SaleOrderLineBudgetServiceImpl;
import com.axelor.apps.businessproject.db.repo.InvoiceProjectRepository;
import com.axelor.apps.businessproject.service.InvoiceLineProjectServiceImpl;
import com.axelor.apps.businessproject.service.InvoiceServiceProjectImpl;
import com.axelor.apps.businessproject.service.ProjectStockMoveInvoiceServiceImpl;
import com.axelor.apps.businessproject.service.PurchaseOrderInvoiceProjectServiceImpl;
import com.axelor.apps.businessproject.service.PurchaseOrderWorkflowServiceProjectImpl;
import com.axelor.apps.businessproject.service.SaleOrderInvoiceProjectServiceImpl;
import com.axelor.apps.businessproject.service.WorkflowCancelServiceProjectImpl;
import com.axelor.apps.businessproject.service.WorkflowValidationServiceProjectImpl;
import com.axelor.apps.businessproject.service.WorkflowVentilationProjectServiceImpl;
import com.axelor.apps.supplychain.db.repo.PurchaseOrderSupplychainRepository;

public class BudgetModule extends AxelorModule {

  @Override
  protected void configure() {

    bind(BudgetRepository.class).to(BudgetManagementRepository.class);
    bind(BudgetLevelRepository.class).to(BudgetLevelManagementRepository.class);
    bind(AdvancedImportBaseRepository.class).to(AdvancedImportBudgetRepository.class);
    bind(PurchaseOrderSupplychainRepository.class)
        .to(PurchaseOrderManagementBudgetRepository.class);
    bind(AppBudgetService.class).to(AppBudgetServiceImpl.class);
    bind(ExportGlobalBudgetLevelService.class).to(ExportGlobalBudgetLevelServiceImpl.class);
    bind(AdvancedExportRepository.class).to(AdvancedExportBudgetRepository.class);
    bind(BudgetLineService.class).to(BudgetLineServiceImpl.class);
    bind(BudgetToolsService.class).to(BudgetToolsServiceImpl.class);
    bind(BudgetAccountConfigService.class).to(BudgetAccountConfigServiceImpl.class);
    bind(InvoiceProjectRepository.class).to(BudgetInvoiceRepository.class);
    bind(MoveBankPaymentRepository.class).to(MoveBudgetManagementRepository.class);
    bind(BudgetAccountService.class).to(BudgetAccountServiceImpl.class);
    bind(BudgetService.class).to(BudgetServiceImpl.class);
    bind(BudgetLevelService.class).to(BudgetLevelServiceImpl.class);
    bind(BudgetDistributionService.class).to(BudgetDistributionServiceImpl.class);

    bind(MoveLineBudgetService.class).to(MoveLineBudgetServiceImpl.class);
    bind(MoveBudgetService.class).to(MoveBudgetServiceImpl.class);
    bind(MoveRemoveServiceBankPaymentImpl.class).to(MoveRemoveBudgetService.class);
    bind(MoveLineGroupBankPaymentServiceImpl.class).to(MoveLineGroupBudgetServiceImpl.class);

    bind(PurchaseOrderLineBudgetService.class).to(PurchaseOrderLineBudgetServiceImpl.class);
    bind(PurchaseOrderBudgetService.class).to(PurchaseOrderBudgetServiceImpl.class);
    bind(BudgetInvoiceLineService.class).to(BudgetInvoiceLineServiceImpl.class);
    bind(InvoiceLineProjectServiceImpl.class).to(BudgetInvoiceLineServiceImpl.class);
    bind(BudgetInvoiceService.class).to(BudgetInvoiceServiceImpl.class);
    bind(InvoiceServiceProjectImpl.class).to(BudgetInvoiceServiceImpl.class);
    bind(WorkflowCancelServiceProjectImpl.class).to(WorkflowCancelBudgetServiceImpl.class);
    bind(WorkflowValidationServiceProjectImpl.class).to(WorkflowValidationBudgetServiceImpl.class);
    bind(WorkflowVentilationProjectServiceImpl.class)
        .to(WorkflowVentilationBudgetServiceImpl.class);
    bind(BudgetLevelService.class).to(BudgetLevelServiceImpl.class);
    bind(PurchaseOrderWorkflowServiceProjectImpl.class).to(PurchaseOrderBudgetServiceImpl.class);
    bind(SaleOrderLineBudgetService.class).to(SaleOrderLineBudgetServiceImpl.class);
    bind(SaleOrderBudgetService.class).to(SaleOrderBudgetServiceImpl.class);
    bind(SaleOrderInvoiceProjectServiceImpl.class).to(SaleOrderBudgetServiceImpl.class);
    bind(ProjectStockMoveInvoiceServiceImpl.class).to(StockMoveInvoiceBudgetServiceImpl.class);
    bind(PurchaseOrderInvoiceProjectServiceImpl.class)
        .to(PurchaseOrderInvoiceBudgetServiceImpl.class);
  }
}
