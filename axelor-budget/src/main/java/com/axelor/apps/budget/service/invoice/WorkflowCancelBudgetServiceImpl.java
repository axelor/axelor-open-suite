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
package com.axelor.apps.budget.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectRepository;
import com.axelor.apps.businessproject.service.WorkflowCancelServiceProjectImpl;
import com.axelor.apps.contract.db.repo.ConsumptionLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderInvoiceService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class WorkflowCancelBudgetServiceImpl extends WorkflowCancelServiceProjectImpl {

  protected BudgetInvoiceService budgetInvoiceService;
  protected AppBudgetService appBudgetService;

  @Inject
  public WorkflowCancelBudgetServiceImpl(
      SaleOrderInvoiceService saleOrderInvoiceService,
      PurchaseOrderInvoiceService purchaseOrderInvoiceService,
      SaleOrderRepository saleOrderRepository,
      PurchaseOrderRepository purchaseOrderRepository,
      ConsumptionLineRepository consumptionLineRepo,
      InvoicingProjectRepository invoicingProjectRepo,
      BudgetInvoiceService budgetInvoiceService,
      AppBudgetService appBudgetService) {
    super(
        saleOrderInvoiceService,
        purchaseOrderInvoiceService,
        saleOrderRepository,
        purchaseOrderRepository,
        consumptionLineRepo,
        invoicingProjectRepo);
    this.budgetInvoiceService = budgetInvoiceService;
    this.appBudgetService = appBudgetService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void afterCancel(Invoice invoice) throws AxelorException {
    super.afterCancel(invoice);

    if (!appBudgetService.isApp("budget")) {
      return;
    }

    budgetInvoiceService.updateBudgetLinesFromInvoice(invoice);

    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      invoiceLine.clearBudgetDistributionList();
    }
  }
}
