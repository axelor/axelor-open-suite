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
package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.contract.db.repo.ConsumptionLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.apps.supplychain.service.workflow.WorkflowCancelServiceSupplychainImpl;
import com.google.inject.Inject;

public class WorkflowCancelServiceContractImpl extends WorkflowCancelServiceSupplychainImpl {

  protected ConsumptionLineRepository consumptionLineRepo;

  @Inject
  public WorkflowCancelServiceContractImpl(
      SaleOrderInvoiceService saleOrderInvoiceService,
      PurchaseOrderInvoiceService purchaseOrderInvoiceService,
      SaleOrderRepository saleOrderRepository,
      PurchaseOrderRepository purchaseOrderRepository,
      ConsumptionLineRepository consumptionLineRepo) {
    super(
        saleOrderInvoiceService,
        purchaseOrderInvoiceService,
        saleOrderRepository,
        purchaseOrderRepository);
    this.consumptionLineRepo = consumptionLineRepo;
  }

  @Override
  public void afterCancel(Invoice invoice) throws AxelorException {
    super.afterCancel(invoice);
    invoice.getInvoiceLineList().stream()
        .filter(invoiceLine -> invoiceLine.getContractLine() != null)
        .forEach(
            invoiceLine -> {
              invoiceLine.getContractLine().setIsInvoiced(false);
              invoiceLine.setContractLine(null);
            });

    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      consumptionLineRepo
          .all()
          .filter("self.invoiceLine = ?", invoiceLine)
          .fetch()
          .forEach(
              consumptionLine -> {
                consumptionLine.setIsInvoiced(false);
                consumptionLine.setInvoiceLine(null);
              });
    }
  }
}
