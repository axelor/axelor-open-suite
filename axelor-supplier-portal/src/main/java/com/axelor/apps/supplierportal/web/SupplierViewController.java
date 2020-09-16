/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplierportal.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.supplierportal.service.SupplierViewService;
import com.axelor.auth.db.User;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Map;

public class SupplierViewController {

  public void completeSupplierViewIndicators(ActionRequest request, ActionResponse response) {
    try {
      Map<String, Object> map = Beans.get(SupplierViewService.class).updateSupplierViewIndicators();
      response.setValues(map);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /* PurchaseOrder OnClick */
  public void getPurchaseOrders(ActionRequest request, ActionResponse response) {
    try {
      SupplierViewService supplierViewService = Beans.get(SupplierViewService.class);
      User user = supplierViewService.getSupplierUser();
      String domain = Beans.get(SupplierViewService.class).getAwaitingInvoicesOfSupplier(user);
      response.setView(
          ActionView.define(I18n.get("Purchase orders"))
              .model(PurchaseOrder.class.getName())
              .add("grid", "purchase-order-grid")
              .add("form", "purchase-order-form")
              .domain(domain)
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getPurchaseQuotationInProgress(ActionRequest request, ActionResponse response) {
    try {
      SupplierViewService supplierViewService = Beans.get(SupplierViewService.class);
      User user = supplierViewService.getSupplierUser();
      String domain =
          Beans.get(SupplierViewService.class).getPurchaseQuotationsInProgressOfSupplier(user);
      response.setView(
          ActionView.define(I18n.get("Quotation In Progress"))
              .model(PurchaseOrder.class.getName())
              .add("grid", "purchase-order-grid")
              .add("form", "purchase-order-form")
              .domain(domain)
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getLastOrder(ActionRequest request, ActionResponse response) {
    try {
      SupplierViewService supplierViewService = Beans.get(SupplierViewService.class);
      User user = supplierViewService.getSupplierUser();
      String domain = Beans.get(SupplierViewService.class).getLastPurchaseOrderOfSupplier(user);
      response.setView(
          ActionView.define(I18n.get("Last order"))
              .model(PurchaseOrder.class.getName())
              .add("grid", "purchase-order-grid")
              .add("form", "purchase-order-form")
              .domain(domain)
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /* StockMove OnClick*/
  public void getLastDelivery(ActionRequest request, ActionResponse response) {
    try {
      SupplierViewService supplierViewService = Beans.get(SupplierViewService.class);
      User user = supplierViewService.getSupplierUser();
      String domain = Beans.get(SupplierViewService.class).getLastDeliveryOfSupplier(user);
      response.setView(
          ActionView.define(I18n.get("Last delivery"))
              .model(StockMove.class.getName())
              .add("grid", "stock-move-supplier-grid")
              .add("form", "stock-move-supplier-form")
              .domain(domain)
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getNextDelivery(ActionRequest request, ActionResponse response) {
    try {
      SupplierViewService supplierViewService = Beans.get(SupplierViewService.class);
      User user = supplierViewService.getSupplierUser();
      String domain = Beans.get(SupplierViewService.class).getNextDeliveryOfSupplier(user);
      response.setView(
          ActionView.define(I18n.get("Next delivery"))
              .model(StockMove.class.getName())
              .add("grid", "stock-move-supplier-grid")
              .add("form", "stock-move-supplier-form")
              .domain(domain)
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getDeliveriesToPrepare(ActionRequest request, ActionResponse response) {
    try {
      SupplierViewService supplierViewService = Beans.get(SupplierViewService.class);
      User user = supplierViewService.getSupplierUser();
      String domain = Beans.get(SupplierViewService.class).getDeliveriesToPrepareOfSupplier(user);
      response.setView(
          ActionView.define(I18n.get("Deliveries to prepare"))
              .model(StockMove.class.getName())
              .add("grid", "stock-move-supplier-grid")
              .add("form", "stock-move-supplier-form")
              .domain(domain)
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /* Invoice */
  /* TODO REVOIR LE COMPTEUR LE BOUTON*/
  public void getOverdueInvoice(ActionRequest request, ActionResponse response) {
    try {
      SupplierViewService supplierViewService = Beans.get(SupplierViewService.class);
      User user = supplierViewService.getSupplierUser();
      String domain = Beans.get(SupplierViewService.class).getAwaitingInvoicesOfSupplier(user);
      response.setView(
          ActionView.define(I18n.get("Overdue invoices"))
              .model(Invoice.class.getName())
              .add("grid", "invoice-grid")
              .add("form", "invoice-form")
              .domain(domain)
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getAwaitingInvoice(ActionRequest request, ActionResponse response) {
    try {
      SupplierViewService supplierViewService = Beans.get(SupplierViewService.class);
      User user = supplierViewService.getSupplierUser();
      String domain = Beans.get(SupplierViewService.class).getAwaitingInvoicesOfSupplier(user);
      response.setView(
          ActionView.define(I18n.get("Awaiting invoice"))
              .model(Invoice.class.getName())
              .add("grid", "invoice-grid")
              .add("form", "invoice-form")
              .domain(domain)
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getTotalRemaining(ActionRequest request, ActionResponse response) {
    try {
      SupplierViewService supplierViewService = Beans.get(SupplierViewService.class);
      User user = supplierViewService.getSupplierUser();
      String domain = Beans.get(SupplierViewService.class).getTotalRemainingOfSupplier(user);
      response.setView(
          ActionView.define(I18n.get("Total remaining"))
              .model(Invoice.class.getName())
              .add("grid", "invoice-grid")
              .add("form", "invoice-form")
              .domain(domain)
              .map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
