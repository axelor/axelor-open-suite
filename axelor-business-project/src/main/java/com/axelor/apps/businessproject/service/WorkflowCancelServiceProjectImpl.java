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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.businessproject.db.InvoicingProject;
import com.axelor.apps.businessproject.db.repo.InvoicingProjectRepository;
import com.axelor.apps.contract.db.repo.ConsumptionLineRepository;
import com.axelor.apps.contract.service.WorkflowCancelServiceContractImpl;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class WorkflowCancelServiceProjectImpl extends WorkflowCancelServiceContractImpl {

  @Inject InvoicingProjectRepository invoicingProjectRepo;

  protected ConsumptionLineRepository consumptionLineRepo;

  @Inject
  public WorkflowCancelServiceProjectImpl(
      SaleOrderInvoiceService saleOrderInvoiceService,
      PurchaseOrderInvoiceService purchaseOrderInvoiceService,
      SaleOrderRepository saleOrderRepository,
      PurchaseOrderRepository purchaseOrderRepository,
      ConsumptionLineRepository consumptionLineRepo) {
    super(
        saleOrderInvoiceService,
        purchaseOrderInvoiceService,
        saleOrderRepository,
        purchaseOrderRepository,
        consumptionLineRepo);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void afterCancel(Invoice invoice) throws AxelorException {
    super.afterCancel(invoice);

    InvoicingProject invoicingProject =
        invoicingProjectRepo.all().filter("self.invoice = ?", invoice.getId()).fetchOne();

    if (invoicingProject != null) {
      invoicingProject.setStatusSelect(InvoicingProjectRepository.STATUS_CANCELED);
      invoicingProjectRepo.save(invoicingProject);
    }
  }
}
