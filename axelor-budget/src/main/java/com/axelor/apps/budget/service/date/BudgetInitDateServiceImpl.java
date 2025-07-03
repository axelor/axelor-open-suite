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
package com.axelor.apps.budget.service.date;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.google.inject.Inject;

public class BudgetInitDateServiceImpl implements BudgetInitDateService {

  protected BudgetDateService budgetDateService;

  @Inject
  public BudgetInitDateServiceImpl(BudgetDateService budgetDateService) {
    this.budgetDateService = budgetDateService;
  }

  @Override
  public void initializeBudgetDates(Invoice invoice) throws AxelorException {
    String coherenceError =
        budgetDateService.checkDateCoherence(
            invoice.getBudgetFromDate(), invoice.getBudgetToDate());
    if (StringUtils.notEmpty(coherenceError)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, coherenceError, invoice);
    }

    if (ObjectUtils.isEmpty(invoice.getInvoiceLineList())) {
      return;
    }

    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      invoiceLine.setBudgetFromDate(invoice.getBudgetFromDate());
      invoiceLine.setBudgetToDate(invoice.getBudgetToDate());
    }

    throwBudgetDateExceptionIfNeeded(budgetDateService.checkBudgetDates(invoice));
  }

  @Override
  public void initializeBudgetDates(PurchaseOrder purchaseOrder) throws AxelorException {
    String coherenceError =
        budgetDateService.checkDateCoherence(
            purchaseOrder.getBudgetFromDate(), purchaseOrder.getBudgetToDate());
    if (StringUtils.notEmpty(coherenceError)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, coherenceError, purchaseOrder);
    }

    if (ObjectUtils.isEmpty(purchaseOrder.getPurchaseOrderLineList())) {
      return;
    }

    for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
      purchaseOrderLine.setBudgetFromDate(purchaseOrder.getBudgetFromDate());
      purchaseOrderLine.setBudgetToDate(purchaseOrder.getBudgetToDate());
    }

    throwBudgetDateExceptionIfNeeded(budgetDateService.checkBudgetDates(purchaseOrder));
  }

  @Override
  public void initializeBudgetDates(SaleOrder saleOrder) throws AxelorException {
    String coherenceError =
        budgetDateService.checkDateCoherence(
            saleOrder.getBudgetFromDate(), saleOrder.getBudgetToDate());
    if (StringUtils.notEmpty(coherenceError)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, coherenceError, saleOrder);
    }

    if (ObjectUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
      return;
    }

    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      saleOrderLine.setBudgetFromDate(saleOrder.getBudgetFromDate());
      saleOrderLine.setBudgetToDate(saleOrder.getBudgetToDate());
    }

    throwBudgetDateExceptionIfNeeded(budgetDateService.checkBudgetDates(saleOrder));
  }

  @Override
  public void initializeBudgetDates(Move move) throws AxelorException {
    String coherenceError =
        budgetDateService.checkDateCoherence(move.getBudgetFromDate(), move.getBudgetToDate());
    if (StringUtils.notEmpty(coherenceError)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, coherenceError, move);
    }

    if (ObjectUtils.isEmpty(move.getMoveLineList())) {
      return;
    }

    for (MoveLine moveLine : move.getMoveLineList()) {
      moveLine.setBudgetFromDate(move.getBudgetFromDate());
      moveLine.setBudgetToDate(move.getBudgetToDate());
    }

    throwBudgetDateExceptionIfNeeded(budgetDateService.checkBudgetDates(move));
  }

  protected void throwBudgetDateExceptionIfNeeded(String errorStr) throws AxelorException {
    if (StringUtils.notEmpty(errorStr)) {
      throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, errorStr);
    }
  }
}
