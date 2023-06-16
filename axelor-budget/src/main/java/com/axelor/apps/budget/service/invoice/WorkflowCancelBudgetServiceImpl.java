package com.axelor.apps.budget.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectRepository;
import com.axelor.apps.businessproject.service.WorkflowCancelServiceProjectImpl;
import com.axelor.apps.contract.db.repo.ConsumptionLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class WorkflowCancelBudgetServiceImpl extends WorkflowCancelServiceProjectImpl {

  protected BudgetInvoiceService budgetInvoiceService;

  @Inject
  public WorkflowCancelBudgetServiceImpl(
      SaleOrderInvoiceService saleOrderInvoiceService,
      PurchaseOrderInvoiceService purchaseOrderInvoiceService,
      SaleOrderRepository saleOrderRepository,
      PurchaseOrderRepository purchaseOrderRepository,
      ConsumptionLineRepository consumptionLineRepo,
      InvoicingProjectRepository invoicingProjectRepo,
      BudgetInvoiceService budgetInvoiceService) {
    super(
        saleOrderInvoiceService,
        purchaseOrderInvoiceService,
        saleOrderRepository,
        purchaseOrderRepository,
        consumptionLineRepo,
        invoicingProjectRepo);
    this.budgetInvoiceService = budgetInvoiceService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void afterCancel(Invoice invoice) throws AxelorException {
    super.afterCancel(invoice);

    budgetInvoiceService.updateBudgetLinesFromInvoice(invoice);

    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      invoiceLine.clearBudgetDistributionList();
    }
  }
}
