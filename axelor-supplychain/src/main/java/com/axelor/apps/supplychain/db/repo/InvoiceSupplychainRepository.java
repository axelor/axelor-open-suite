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
package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceManagementRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychain;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;

public class InvoiceSupplychainRepository extends InvoiceManagementRepository {

  @Override
  public Invoice copy(Invoice entity, boolean deep) {
    Invoice copy = super.copy(entity, deep);

    copy.setSaleOrder(null);
    copy.setPurchaseOrder(null);
    copy.setStockMoveSet(null);

    if (copy.getInvoiceLineList() != null && !copy.getInvoiceLineList().isEmpty()) {
      for (InvoiceLine line : copy.getInvoiceLineList()) {
        line.setSaleOrderLine(null);
        line.setPurchaseOrderLine(null);
        line.setStockMoveLine(null);
        line.setOutgoingStockMove(null);
        line.setIncomingStockMove(null);
      }
    }
    return copy;
  }

  @Override
  public Invoice save(Invoice invoice) {
    try {
      InvoiceServiceSupplychain invoiceServiceSupplychain =
          Beans.get(InvoiceServiceSupplychain.class);

      if (Boolean.TRUE.equals(
          Beans.get(AppSaleService.class).getAppSale().getEnablePackManagement())) {
        invoiceServiceSupplychain.computePackTotal(invoice);
      } else {
        invoiceServiceSupplychain.resetPackTotal(invoice);
      }

      return super.save(invoice);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }
}
