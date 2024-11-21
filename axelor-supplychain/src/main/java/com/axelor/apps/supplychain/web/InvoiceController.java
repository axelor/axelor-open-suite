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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychain;
import com.axelor.apps.supplychain.service.invoice.SubscriptionInvoiceService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class InvoiceController {

  public void fillInLines(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);

    response.setValues(invoice);
  }

  public void generateSubscriptionInvoices(ActionRequest request, ActionResponse response) {

    try {
      List<Invoice> invoices =
          Beans.get(SubscriptionInvoiceService.class).generateSubscriptionInvoices();
      response.setInfo(
          String.format(
              I18n.get(SupplychainExceptionMessage.TOTAL_SUBSCRIPTION_INVOICE_GENERATED),
              invoices.size()));

      if (!CollectionUtils.isEmpty(invoices)) {
        response.setReload(true);
      }
    } catch (Exception e) {
      response.setInfo(
          String.format(
              I18n.get(SupplychainExceptionMessage.SUBSCRIPTION_INVOICE_GENERATION_ERROR),
              e.getMessage()));
      TraceBackService.trace(e);
    }
  }

  public void updateProductQtyWithPackHeaderQty(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    if (Boolean.FALSE.equals(Beans.get(AppSaleService.class).getAppSale().getEnablePackManagement())
        || !Beans.get(InvoiceLineService.class)
            .isStartOfPackTypeLineQtyChanged(invoice.getInvoiceLineList())) {
      return;
    }
    try {
      Beans.get(InvoiceServiceSupplychain.class).updateProductQtyWithPackHeaderQty(invoice);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
    response.setReload(true);
  }
}
