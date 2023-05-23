package com.axelor.apps.budget.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.service.invoice.workflow.ventilate.VentilateState;
import com.axelor.apps.bankpayment.db.repo.MoveBankPaymentRepository;
import com.axelor.apps.bankpayment.service.move.MoveRemoveServiceBankPaymentImpl;
import com.axelor.apps.base.db.repo.AdvancedExportBudgetRepository;
import com.axelor.apps.base.db.repo.AdvancedExportRepository;
import com.axelor.apps.base.db.repo.AdvancedImportBaseRepository;
import com.axelor.apps.base.db.repo.AdvancedImportBudgetRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.BankDetailsServiceImpl;
import com.axelor.apps.budget.db.repo.BudgetBudgetRepository;
import com.axelor.apps.budget.db.repo.BudgetInvoiceRepository;
import com.axelor.apps.budget.db.repo.BudgetLevelManagementRepository;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.db.repo.MoveBudgetManagementRepository;
import com.axelor.apps.budget.export.ExportGlobalBudgetLevelService;
import com.axelor.apps.budget.export.ExportGlobalBudgetLevelServiceImpl;
import com.axelor.apps.budget.service.BudgetAccountConfigService;
import com.axelor.apps.budget.service.BudgetAccountConfigServiceImpl;
import com.axelor.apps.budget.service.BudgetAccountService;
import com.axelor.apps.budget.service.BudgetAccountServiceImpl;
import com.axelor.apps.budget.service.BudgetBudgetDistributionService;
import com.axelor.apps.budget.service.BudgetBudgetDistributionServiceImpl;
import com.axelor.apps.budget.service.BudgetBudgetService;
import com.axelor.apps.budget.service.BudgetBudgetServiceImpl;
import com.axelor.apps.budget.service.BudgetInvoiceLineService;
import com.axelor.apps.budget.service.BudgetInvoiceLineServiceImpl;
import com.axelor.apps.budget.service.BudgetInvoiceService;
import com.axelor.apps.budget.service.BudgetInvoiceServiceImpl;
import com.axelor.apps.budget.service.BudgetLevelService;
import com.axelor.apps.budget.service.BudgetLevelServiceImpl;
import com.axelor.apps.budget.service.BudgetLineService;
import com.axelor.apps.budget.service.BudgetLineServiceImpl;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.BudgetToolsServiceImpl;
import com.axelor.apps.budget.service.advanced.imports.AdvancedImportBudgetService;
import com.axelor.apps.budget.service.advanced.imports.AdvancedImportBudgetServiceImpl;
import com.axelor.apps.budget.service.invoice.StockMoveInvoiceBudgetServiceImpl;
import com.axelor.apps.budget.service.invoice.workflow.ventilate.VentilateStateBudget;
import com.axelor.apps.budget.service.move.MoveBudgetService;
import com.axelor.apps.budget.service.move.MoveBudgetServiceImpl;
import com.axelor.apps.budget.service.move.MoveLineBudgetService;
import com.axelor.apps.budget.service.move.MoveLineBudgetServiceImpl;
import com.axelor.apps.budget.service.move.MoveRemoveBudgetService;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderBudgetBudgetService;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderBudgetBudgetServiceImpl;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderLineBudgetBudgetService;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderLineBudgetBudgetServiceImpl;
import com.axelor.apps.budget.service.saleorder.SaleOrderBudgetService;
import com.axelor.apps.budget.service.saleorder.SaleOrderBudgetServiceImpl;
import com.axelor.apps.budget.service.saleorder.SaleOrderLineBudgetService;
import com.axelor.apps.budget.service.saleorder.SaleOrderLineBudgetServiceImpl;
import com.axelor.apps.businessproject.db.repo.InvoiceProjectRepository;
import com.axelor.apps.businessproject.service.InvoiceLineProjectServiceImpl;
import com.axelor.apps.businessproject.service.ProjectStockMoveInvoiceServiceImpl;
import com.axelor.apps.businessproject.service.SaleOrderInvoiceProjectServiceImpl;
import com.axelor.apps.cash.management.service.InvoiceServiceManagementImpl;
import com.axelor.apps.purchase.db.repo.PurchaseOrderManagementBudgetRepository;
import com.axelor.apps.supplychain.db.repo.PurchaseOrderSupplychainRepository;
import com.axelor.apps.supplychain.service.PurchaseOrderWorkflowServiceSupplychainImpl;

public class BudgetModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(BudgetLevelService.class).to(BudgetLevelServiceImpl.class);
    bind(BudgetBudgetService.class).to(BudgetBudgetServiceImpl.class);
    bind(PurchaseOrderBudgetBudgetService.class).to(PurchaseOrderBudgetBudgetServiceImpl.class);
    bind(PurchaseOrderWorkflowServiceSupplychainImpl.class)
        .to(PurchaseOrderBudgetBudgetServiceImpl.class);
    bind(PurchaseOrderLineBudgetBudgetService.class)
        .to(PurchaseOrderLineBudgetBudgetServiceImpl.class);
    bind(BudgetRepository.class).to(BudgetBudgetRepository.class);
    bind(BudgetLevelRepository.class).to(BudgetLevelManagementRepository.class);
    bind(AdvancedImportBaseRepository.class).to(AdvancedImportBudgetRepository.class);
    bind(PurchaseOrderSupplychainRepository.class)
        .to(PurchaseOrderManagementBudgetRepository.class);
    bind(ExportGlobalBudgetLevelService.class).to(ExportGlobalBudgetLevelServiceImpl.class);
    bind(AdvancedExportRepository.class).to(AdvancedExportBudgetRepository.class);
    bind(BankDetailsService.class).to(BankDetailsServiceImpl.class);
    bind(BudgetLineService.class).to(BudgetLineServiceImpl.class);
    bind(VentilateState.class).to(VentilateStateBudget.class);
    bind(BudgetInvoiceService.class).to(BudgetInvoiceServiceImpl.class);
    bind(InvoiceServiceManagementImpl.class).to(BudgetInvoiceServiceImpl.class);
    bind(BudgetToolsService.class).to(BudgetToolsServiceImpl.class);
    bind(BudgetAccountConfigService.class).to(BudgetAccountConfigServiceImpl.class);
    bind(BudgetInvoiceLineService.class).to(BudgetInvoiceLineServiceImpl.class);
    bind(InvoiceLineProjectServiceImpl.class).to(BudgetInvoiceLineServiceImpl.class);
    bind(BudgetBudgetDistributionService.class).to(BudgetBudgetDistributionServiceImpl.class);
    bind(MoveLineBudgetService.class).to(MoveLineBudgetServiceImpl.class);
    bind(MoveBudgetService.class).to(MoveBudgetServiceImpl.class);
    bind(MoveBankPaymentRepository.class).to(MoveBudgetManagementRepository.class);
    bind(MoveRemoveServiceBankPaymentImpl.class).to(MoveRemoveBudgetService.class);
    bind(InvoiceProjectRepository.class).to(BudgetInvoiceRepository.class);
    bind(AdvancedImportBudgetService.class).to(AdvancedImportBudgetServiceImpl.class);
    bind(BudgetAccountService.class).to(BudgetAccountServiceImpl.class);

    bind(SaleOrderBudgetService.class).to(SaleOrderBudgetServiceImpl.class);
    bind(SaleOrderLineBudgetService.class).to(SaleOrderLineBudgetServiceImpl.class);
    bind(SaleOrderInvoiceProjectServiceImpl.class).to(SaleOrderBudgetServiceImpl.class);
    bind(ProjectStockMoveInvoiceServiceImpl.class).to(StockMoveInvoiceBudgetServiceImpl.class);
  }
}
