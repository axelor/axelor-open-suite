/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.invoice.BudgetInvoiceLineService;
import com.axelor.apps.budget.service.move.MoveLineBudgetService;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderLineBudgetService;
import com.axelor.apps.budget.service.saleorder.SaleOrderLineBudgetService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.time.LocalDate;

public class BudgetDistributionController {

  public void setAmountAvailableOnBudget(ActionRequest request, ActionResponse response) {
    try {
      BudgetDistribution budgetDistribution = request.getContext().asType(BudgetDistribution.class);

      Context parentContext = request.getContext().getParent();
      Context grandParentContext = null;
      LocalDate date = null;
      if (parentContext == null) {
        return;
      }
      grandParentContext = request.getContext().getParent().getParent();
      if (grandParentContext == null) {
        return;
      }
      if (InvoiceLine.class.equals(parentContext.getContextClass())
          && budgetDistribution.getBudget() != null) {
        Invoice invoice = grandParentContext.asType(Invoice.class);
        if (invoice != null) {
          date =
              invoice.getInvoiceDate() != null
                  ? invoice.getInvoiceDate()
                  : Beans.get(AppBaseService.class).getTodayDate(invoice.getCompany());
        }
      } else if (PurchaseOrderLine.class.equals(parentContext.getContextClass())
          && budgetDistribution.getBudget() != null) {
        PurchaseOrder purchaseOrder = grandParentContext.asType(PurchaseOrder.class);
        if (purchaseOrder != null && purchaseOrder.getOrderDate() != null) {
          date = purchaseOrder.getOrderDate();
        }
      } else if (MoveLine.class.equals(parentContext.getContextClass())
          && budgetDistribution.getBudget() != null) {
        Move move = grandParentContext.asType(Move.class);
        if (move != null && move.getDate() != null) {
          date = move.getDate();
        } else {
          date = parentContext.asType(MoveLine.class).getDate();
        }
      } else if (SaleOrderLine.class.equals(parentContext.getContextClass())
          && budgetDistribution.getBudget() != null) {
        SaleOrder saleOrder = grandParentContext.asType(SaleOrder.class);
        if (saleOrder != null) {
          date =
              saleOrder.getOrderDate() != null
                  ? saleOrder.getOrderDate()
                  : saleOrder.getCreationDate();
        }
      }

      Beans.get(BudgetDistributionService.class)
          .computeBudgetDistributionSumAmount(budgetDistribution, date);
      response.setValues(budgetDistribution);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setBudgetDomain(ActionRequest request, ActionResponse response) {
    try {
      Context parentContext = request.getContext().getParent();
      String query = "";
      if (parentContext != null
          && PurchaseOrderLine.class.equals(parentContext.getContextClass())) {
        PurchaseOrderLine purchaseOrderLine = parentContext.asType(PurchaseOrderLine.class);
        PurchaseOrder purchaseOrder = null;

        if (PurchaseOrder.class.equals(parentContext.getParent().getContextClass())) {
          purchaseOrder = parentContext.getParent().asType(PurchaseOrder.class);
        }
        query =
            Beans.get(PurchaseOrderLineBudgetService.class)
                .getBudgetDomain(purchaseOrderLine, purchaseOrder);

      } else if (parentContext != null && MoveLine.class.equals(parentContext.getContextClass())) {
        Move move = null;
        MoveLine moveLine = parentContext.asType(MoveLine.class);
        if (parentContext.getParent() != null
            && Move.class.equals(parentContext.getParent().getContextClass())) {
          move = parentContext.getParent().asType(Move.class);
        } else if (parentContext.asType(MoveLine.class).getMove() != null) {
          move = parentContext.asType(MoveLine.class).getMove();
        }
        query = Beans.get(MoveLineBudgetService.class).getBudgetDomain(move, moveLine);

      } else if (parentContext != null
          && InvoiceLine.class.equals(parentContext.getContextClass())) {

        if (Invoice.class.equals(parentContext.getParent().getContextClass())) {
          Invoice invoice = parentContext.getParent().asType(Invoice.class);
          InvoiceLine invoiceLine = parentContext.asType(InvoiceLine.class);
          query = Beans.get(BudgetInvoiceLineService.class).getBudgetDomain(invoice, invoiceLine);
        }
      } else if (parentContext != null
          && SaleOrderLine.class.equals(parentContext.getContextClass())) {
        SaleOrderLine saleOrderLine = parentContext.asType(SaleOrderLine.class);
        SaleOrder saleOrder = parentContext.getParent().asType(SaleOrder.class);
        query =
            Beans.get(SaleOrderLineBudgetService.class).getBudgetDomain(saleOrderLine, saleOrder);
      }

      response.setAttr("budget", "domain", query);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void purchaseOrderOnNew(ActionRequest request, ActionResponse response) {
    try {
      Context parentContext = request.getContext().getParent();
      if (parentContext != null
          && PurchaseOrderLine.class.equals(parentContext.getContextClass())) {
        PurchaseOrderLine purchaseOrderLine = parentContext.asType(PurchaseOrderLine.class);
        if (purchaseOrderLine.getProduct() != null) {
          response.setValue("product", purchaseOrderLine.getProduct());
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void saleOrderOnNew(ActionRequest request, ActionResponse response) {
    try {
      Context parentContext = request.getContext().getParent();
      if (parentContext != null && SaleOrderLine.class.equals(parentContext.getContextClass())) {
        SaleOrderLine saleOrderLine = parentContext.asType(SaleOrderLine.class);
        if (saleOrderLine.getProduct() != null) {
          response.setValue("product", saleOrderLine.getProduct());
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void invoiceOnNew(ActionRequest request, ActionResponse response) {
    try {
      Context parentContext = request.getContext().getParent();
      if (parentContext != null && InvoiceLine.class.equals(parentContext.getContextClass())) {
        InvoiceLine invoiceLine = parentContext.asType(InvoiceLine.class);
        if (invoiceLine.getInvoice() != null
            && invoiceLine.getInvoice().getPurchaseOrder() != null) {
          response.setValue("purchaseOrder", invoiceLine.getInvoice().getPurchaseOrder());
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
