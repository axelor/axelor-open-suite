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
package com.axelor.apps.budget.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.service.move.record.MoveGroupServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineConsolidateServiceImpl;
import com.axelor.apps.account.service.moveline.MoveLineCreateServiceImpl;
import com.axelor.apps.account.service.reconcile.ReconcileInvoiceTermComputationServiceImpl;
import com.axelor.apps.account.service.reconcile.UnreconcileServiceImpl;
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
import com.axelor.apps.budget.db.repo.GlobalBudgetManagementRepository;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.apps.budget.db.repo.MoveBudgetManagementRepository;
import com.axelor.apps.budget.db.repo.PurchaseOrderManagementBudgetRepository;
import com.axelor.apps.budget.db.repo.SaleOrderBudgetRepository;
import com.axelor.apps.budget.export.ExportBudgetCallableService;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.AppBudgetServiceImpl;
import com.axelor.apps.budget.service.BudgetAccountConfigService;
import com.axelor.apps.budget.service.BudgetAccountConfigServiceImpl;
import com.axelor.apps.budget.service.BudgetAccountService;
import com.axelor.apps.budget.service.BudgetAccountServiceImpl;
import com.axelor.apps.budget.service.BudgetComputeHiddenDateService;
import com.axelor.apps.budget.service.BudgetComputeHiddenDateServiceImpl;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetDistributionServiceImpl;
import com.axelor.apps.budget.service.BudgetGroupService;
import com.axelor.apps.budget.service.BudgetGroupServiceImpl;
import com.axelor.apps.budget.service.BudgetLevelResetToolService;
import com.axelor.apps.budget.service.BudgetLevelResetToolServiceImpl;
import com.axelor.apps.budget.service.BudgetLevelService;
import com.axelor.apps.budget.service.BudgetLevelServiceImpl;
import com.axelor.apps.budget.service.BudgetLineResetToolService;
import com.axelor.apps.budget.service.BudgetLineResetToolServiceImpl;
import com.axelor.apps.budget.service.BudgetLineService;
import com.axelor.apps.budget.service.BudgetLineServiceImpl;
import com.axelor.apps.budget.service.BudgetResetToolService;
import com.axelor.apps.budget.service.BudgetResetToolServiceImpl;
import com.axelor.apps.budget.service.BudgetScenarioLineService;
import com.axelor.apps.budget.service.BudgetScenarioLineServiceImpl;
import com.axelor.apps.budget.service.BudgetScenarioService;
import com.axelor.apps.budget.service.BudgetScenarioServiceImpl;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetServiceImpl;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.BudgetToolsServiceImpl;
import com.axelor.apps.budget.service.BudgetVersionService;
import com.axelor.apps.budget.service.BudgetVersionServiceImpl;
import com.axelor.apps.budget.service.ReconcileInvoiceTermComputationBudgetServiceImpl;
import com.axelor.apps.budget.service.ReconcileToolBudgetService;
import com.axelor.apps.budget.service.ReconcileToolBudgetServiceImpl;
import com.axelor.apps.budget.service.UnreconcileBudgetServiceImpl;
import com.axelor.apps.budget.service.compute.BudgetLineComputeService;
import com.axelor.apps.budget.service.compute.BudgetLineComputeServiceImpl;
import com.axelor.apps.budget.service.date.BudgetDateService;
import com.axelor.apps.budget.service.date.BudgetDateServiceImpl;
import com.axelor.apps.budget.service.date.BudgetInitDateService;
import com.axelor.apps.budget.service.date.BudgetInitDateServiceImpl;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetGroupService;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetGroupServiceImpl;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetResetToolService;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetResetToolServiceImpl;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetService;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetServiceImpl;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetToolsService;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetToolsServiceImpl;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetWorkflowService;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetWorkflowServiceImpl;
import com.axelor.apps.budget.service.invoice.BudgetInvoiceLineComputeServiceImpl;
import com.axelor.apps.budget.service.invoice.BudgetInvoiceLineService;
import com.axelor.apps.budget.service.invoice.BudgetInvoiceLineServiceImpl;
import com.axelor.apps.budget.service.invoice.BudgetInvoiceService;
import com.axelor.apps.budget.service.invoice.BudgetInvoiceServiceImpl;
import com.axelor.apps.budget.service.invoice.InvoiceToolBudgetService;
import com.axelor.apps.budget.service.invoice.InvoiceToolBudgetServiceImpl;
import com.axelor.apps.budget.service.invoice.StockMoveInvoiceBudgetServiceImpl;
import com.axelor.apps.budget.service.move.MoveBudgetDistributionService;
import com.axelor.apps.budget.service.move.MoveBudgetDistributionServiceImpl;
import com.axelor.apps.budget.service.move.MoveBudgetService;
import com.axelor.apps.budget.service.move.MoveBudgetServiceImpl;
import com.axelor.apps.budget.service.move.MoveGroupBudgetServiceImpl;
import com.axelor.apps.budget.service.move.MoveLineBudgetService;
import com.axelor.apps.budget.service.move.MoveLineBudgetServiceImpl;
import com.axelor.apps.budget.service.move.MoveLineConsolidateBudgetServiceImpl;
import com.axelor.apps.budget.service.move.MoveLineCreateBudgetServiceImpl;
import com.axelor.apps.budget.service.move.MoveLineGroupBudgetServiceImpl;
import com.axelor.apps.budget.service.move.MoveLineToolBudgetService;
import com.axelor.apps.budget.service.move.MoveLineToolBudgetServiceImpl;
import com.axelor.apps.budget.service.move.MoveRemoveBudgetService;
import com.axelor.apps.budget.service.move.MoveReverseServiceBudgetImpl;
import com.axelor.apps.budget.service.move.MoveValidateBudgetServiceImpl;
import com.axelor.apps.budget.service.observer.SaleOrderBudgetObserver;
import com.axelor.apps.budget.service.observer.SaleOrderLineBudgetObserver;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderBudgetService;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderBudgetServiceImpl;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderInvoiceBudgetServiceImpl;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderLineBudgetService;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderLineBudgetServiceImpl;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderLineGroupBudgetServiceImpl;
import com.axelor.apps.budget.service.saleorder.SaleOrderBudgetService;
import com.axelor.apps.budget.service.saleorder.SaleOrderBudgetServiceImpl;
import com.axelor.apps.budget.service.saleorder.SaleOrderCheckBudgetService;
import com.axelor.apps.budget.service.saleorder.SaleOrderCheckBudgetServiceImpl;
import com.axelor.apps.budget.service.saleorder.SaleOrderLineProductBudgetService;
import com.axelor.apps.budget.service.saleorder.SaleOrderLineProductBudgetServiceImpl;
import com.axelor.apps.budget.service.saleorder.status.SaleOrderFinalizeBudgetServiceImpl;
import com.axelor.apps.budget.service.saleorderline.SaleOrderLineBudgetService;
import com.axelor.apps.budget.service.saleorderline.SaleOrderLineBudgetServiceImpl;
import com.axelor.apps.budget.service.saleorderline.SaleOrderLineComputeBudgetServiceImpl;
import com.axelor.apps.budget.service.saleorderline.SaleOrderLineViewBudgetService;
import com.axelor.apps.budget.service.saleorderline.SaleOrderLineViewBudgetServiceImpl;
import com.axelor.apps.businessproject.db.repo.InvoiceProjectRepository;
import com.axelor.apps.businessproject.service.InvoiceLineProjectServiceImpl;
import com.axelor.apps.businessproject.service.ProjectStockMoveInvoiceServiceImpl;
import com.axelor.apps.businessproject.service.PurchaseOrderInvoiceProjectServiceImpl;
import com.axelor.apps.businessproject.service.PurchaseOrderLineServiceProjectImpl;
import com.axelor.apps.businessproject.service.PurchaseOrderWorkflowServiceProjectImpl;
import com.axelor.apps.businessproject.service.SaleOrderInvoiceProjectServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseMoveReverseServiceImpl;
import com.axelor.apps.hr.service.move.MoveValidateHRServiceImpl;
import com.axelor.apps.sale.service.saleorder.SaleOrderCheckServiceImpl;
import com.axelor.apps.supplychain.db.repo.PurchaseOrderSupplychainRepository;
import com.axelor.apps.supplychain.db.repo.SaleOrderSupplychainRepository;
import com.axelor.apps.supplychain.service.saleorder.status.SaleOrderFinalizeSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineComputeSupplychainServiceImpl;
import java.util.concurrent.Callable;

public class BudgetModule extends AxelorModule {

  @Override
  protected void configure() {

    bind(BudgetRepository.class).to(BudgetManagementRepository.class);
    bind(BudgetLevelRepository.class).to(BudgetLevelManagementRepository.class);
    bind(AdvancedImportBaseRepository.class).to(AdvancedImportBudgetRepository.class);
    bind(PurchaseOrderSupplychainRepository.class)
        .to(PurchaseOrderManagementBudgetRepository.class);
    bind(AppBudgetService.class).to(AppBudgetServiceImpl.class);
    bind(AdvancedExportRepository.class).to(AdvancedExportBudgetRepository.class);
    bind(BudgetLineService.class).to(BudgetLineServiceImpl.class);
    bind(BudgetToolsService.class).to(BudgetToolsServiceImpl.class);
    bind(BudgetAccountConfigService.class).to(BudgetAccountConfigServiceImpl.class);
    bind(InvoiceProjectRepository.class).to(BudgetInvoiceRepository.class);
    bind(MoveBankPaymentRepository.class).to(MoveBudgetManagementRepository.class);
    bind(BudgetAccountService.class).to(BudgetAccountServiceImpl.class);
    bind(BudgetService.class).to(BudgetServiceImpl.class);
    bind(GlobalBudgetService.class).to(GlobalBudgetServiceImpl.class);
    bind(BudgetLevelService.class).to(BudgetLevelServiceImpl.class);
    bind(BudgetDistributionService.class).to(BudgetDistributionServiceImpl.class);

    bind(MoveLineBudgetService.class).to(MoveLineBudgetServiceImpl.class);
    bind(MoveBudgetService.class).to(MoveBudgetServiceImpl.class);
    bind(MoveRemoveServiceBankPaymentImpl.class).to(MoveRemoveBudgetService.class);
    bind(MoveValidateHRServiceImpl.class).to(MoveValidateBudgetServiceImpl.class);
    bind(MoveLineGroupBankPaymentServiceImpl.class).to(MoveLineGroupBudgetServiceImpl.class);

    bind(PurchaseOrderLineBudgetService.class).to(PurchaseOrderLineBudgetServiceImpl.class);
    bind(PurchaseOrderBudgetService.class).to(PurchaseOrderBudgetServiceImpl.class);
    bind(PurchaseOrderLineServiceProjectImpl.class)
        .to(PurchaseOrderLineGroupBudgetServiceImpl.class);
    bind(BudgetInvoiceLineService.class).to(BudgetInvoiceLineServiceImpl.class);
    bind(BudgetInvoiceService.class).to(BudgetInvoiceServiceImpl.class);
    bind(InvoiceToolBudgetService.class).to(InvoiceToolBudgetServiceImpl.class);
    bind(InvoiceLineProjectServiceImpl.class).to(BudgetInvoiceLineComputeServiceImpl.class);
    bind(BudgetLevelService.class).to(BudgetLevelServiceImpl.class);
    bind(PurchaseOrderWorkflowServiceProjectImpl.class).to(PurchaseOrderBudgetServiceImpl.class);
    bind(SaleOrderLineBudgetService.class).to(SaleOrderLineBudgetServiceImpl.class);
    bind(SaleOrderBudgetService.class).to(SaleOrderBudgetServiceImpl.class);
    bind(SaleOrderInvoiceProjectServiceImpl.class).to(SaleOrderBudgetServiceImpl.class);
    bind(ProjectStockMoveInvoiceServiceImpl.class).to(StockMoveInvoiceBudgetServiceImpl.class);
    bind(PurchaseOrderInvoiceProjectServiceImpl.class)
        .to(PurchaseOrderInvoiceBudgetServiceImpl.class);
    bind(BudgetScenarioLineService.class).to(BudgetScenarioLineServiceImpl.class);
    bind(BudgetVersionService.class).to(BudgetVersionServiceImpl.class);
    bind(BudgetScenarioService.class).to(BudgetScenarioServiceImpl.class);
    bind(GlobalBudgetWorkflowService.class).to(GlobalBudgetWorkflowServiceImpl.class);
    bind(GlobalBudgetGroupService.class).to(GlobalBudgetGroupServiceImpl.class);
    bind(GlobalBudgetRepository.class).to(GlobalBudgetManagementRepository.class);
    bind(GlobalBudgetResetToolService.class).to(GlobalBudgetResetToolServiceImpl.class);
    bind(BudgetLevelResetToolService.class).to(BudgetLevelResetToolServiceImpl.class);
    bind(BudgetResetToolService.class).to(BudgetResetToolServiceImpl.class);
    bind(BudgetLineResetToolService.class).to(BudgetLineResetToolServiceImpl.class);
    bind(SaleOrderSupplychainRepository.class).to(SaleOrderBudgetRepository.class);
    bind(ExpenseMoveReverseServiceImpl.class).to(MoveReverseServiceBudgetImpl.class);
    bind(ReconcileInvoiceTermComputationServiceImpl.class)
        .to(ReconcileInvoiceTermComputationBudgetServiceImpl.class);
    bind(MoveLineCreateServiceImpl.class).to(MoveLineCreateBudgetServiceImpl.class);
    bind(MoveLineConsolidateServiceImpl.class).to(MoveLineConsolidateBudgetServiceImpl.class);
    bind(BudgetGroupService.class).to(BudgetGroupServiceImpl.class);
    bind(GlobalBudgetToolsService.class).to(GlobalBudgetToolsServiceImpl.class);
    bind(BudgetComputeHiddenDateService.class).to(BudgetComputeHiddenDateServiceImpl.class);
    bind(Callable.class).to(ExportBudgetCallableService.class);
    bind(UnreconcileServiceImpl.class).to(UnreconcileBudgetServiceImpl.class);
    bind(ReconcileToolBudgetService.class).to(ReconcileToolBudgetServiceImpl.class);
    bind(SaleOrderLineComputeSupplychainServiceImpl.class)
        .to(SaleOrderLineComputeBudgetServiceImpl.class);
    bind(SaleOrderCheckBudgetService.class).to(SaleOrderCheckBudgetServiceImpl.class);
    bind(SaleOrderFinalizeSupplychainServiceImpl.class)
        .to(SaleOrderFinalizeBudgetServiceImpl.class);
    bind(SaleOrderCheckServiceImpl.class).to(SaleOrderCheckBudgetServiceImpl.class);
    bind(SaleOrderBudgetObserver.class);
    bind(SaleOrderLineViewBudgetService.class).to(SaleOrderLineViewBudgetServiceImpl.class);
    bind(SaleOrderLineBudgetObserver.class);
    bind(SaleOrderLineProductBudgetService.class).to(SaleOrderLineProductBudgetServiceImpl.class);

    bind(MoveBudgetDistributionService.class).to(MoveBudgetDistributionServiceImpl.class);
    bind(BudgetDateService.class).to(BudgetDateServiceImpl.class);
    bind(BudgetInitDateService.class).to(BudgetInitDateServiceImpl.class);

    bind(BudgetLineComputeService.class).to(BudgetLineComputeServiceImpl.class);
    bind(MoveGroupServiceImpl.class).to(MoveGroupBudgetServiceImpl.class);
    bind(MoveLineToolBudgetService.class).to(MoveLineToolBudgetServiceImpl.class);
  }
}
