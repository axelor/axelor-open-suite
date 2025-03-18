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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.supplychain.service.InvoiceLineSupplierCatalogService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class InvoiceLineController {

  public Invoice getInvoice(Context context) {

    Context parentContext = context.getParent();

    Invoice invoice = parentContext.asType(Invoice.class);

    if (!parentContext.getContextClass().toString().equals(Invoice.class.toString())) {

      InvoiceLine invoiceLine = context.asType(InvoiceLine.class);

      invoice = invoiceLine.getInvoice();
    }

    return invoice;
  }

  public void checkQty(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      InvoiceLine invoiceLine = context.asType(InvoiceLine.class);
      Invoice invoice = request.getContext().getParent().asType(Invoice.class);

      Beans.get(InvoiceLineSupplierCatalogService.class)
          .checkMinQty(invoice, invoiceLine, request, response);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
