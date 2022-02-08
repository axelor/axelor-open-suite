/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychain;
import com.axelor.apps.supplychain.service.invoice.SubscriptionInvoiceService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
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
      response.setFlash(
          String.format(
              I18n.get(
                  com.axelor.apps.supplychain.exception.IExceptionMessage
                      .TOTAL_SUBSCRIPTION_INVOICE_GENERATED),
              invoices.size()));

      if (!CollectionUtils.isEmpty(invoices)) {
        response.setReload(true);
      }
    } catch (Exception e) {
      response.setFlash(
          String.format(
              I18n.get(
                  com.axelor.apps.supplychain.exception.IExceptionMessage
                      .SUBSCRIPTION_INVOICE_GENERATION_ERROR),
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
