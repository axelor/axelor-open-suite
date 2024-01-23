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
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceViewService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.math.BigDecimal;

@Singleton
public class PurchaseOrderInvoiceController {

  public void generateInvoice(ActionRequest request, ActionResponse response) {

    PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

    purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId());

    try {
      Beans.get(PurchaseOrderInvoiceService.class)
          .displayErrorMessageIfPurchaseOrderIsInvoiceable(purchaseOrder, BigDecimal.ZERO, false);
      Invoice invoice = Beans.get(PurchaseOrderInvoiceService.class).generateInvoice(purchaseOrder);
      if (invoice != null) {
        response.setReload(true);
        response.setView(
            ActionView.define(I18n.get(SupplychainExceptionMessage.PO_INVOICE_2))
                .model(Invoice.class.getName())
                .add("form", "invoice-form")
                .add("grid", InvoiceViewService.computeInvoiceGridName(invoice))
                .param("search-filters", InvoiceViewService.computeInvoiceFilterName(invoice))
                .domain("self.purchaseOrder.id = " + invoice.getId())
                .domain("self.operationTypeSelect = " + invoice.getOperationTypeSelect())
                .context("_operationTypeSelect", invoice.getOperationTypeSelect())
                .context("_showRecord", String.valueOf(invoice.getId()))
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void showPopUpInvoicingWizard(ActionRequest request, ActionResponse response) {
    try {
      PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
      purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId());
      response.setView(
          ActionView.define(I18n.get("Invoicing"))
              .model(PurchaseOrder.class.getName())
              .add("form", "purchase-order-invoicing-wizard-form")
              .param("popup", "reload")
              .param("show-toolbar", "false")
              .param("show-confirm", "false")
              .param("popup-save", "false")
              .param("forceEdit", "true")
              .context("_showRecord", String.valueOf(purchaseOrder.getId()))
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateAdvancePayment(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    try {
      PurchaseOrder purchaseOrder = context.asType(PurchaseOrder.class);
      boolean isPercent = (Boolean) context.getOrDefault("isPercent", false);
      BigDecimal amountToInvoice =
          new BigDecimal(context.getOrDefault("amountToInvoice", "0").toString());

      purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId());
      Beans.get(PurchaseOrderInvoiceService.class)
          .displayErrorMessageIfPurchaseOrderIsInvoiceable(
              purchaseOrder, amountToInvoice, isPercent);
      Invoice invoice =
          Beans.get(PurchaseOrderInvoiceService.class)
              .generateSupplierAdvancePayment(purchaseOrder, amountToInvoice, isPercent);

      if (invoice != null) {
        response.setCanClose(true);
        response.setView(
            ActionView.define(I18n.get("Invoice generated"))
                .model(Invoice.class.getName())
                .add("form", "invoice-form")
                .add("grid", "invoice-grid")
                .param("search-filters", "customer-invoices-filters")
                .context("_showRecord", String.valueOf(invoice.getId()))
                .context("_operationTypeSelect", InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE)
                .context(
                    "todayDate",
                    Beans.get(AppSupplychainService.class).getTodayDate(purchaseOrder.getCompany()))
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setWizardDefaultValues(ActionRequest request, ActionResponse response) {
    try {
      response.setAttr("$operationSelect", "value", "1");
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
