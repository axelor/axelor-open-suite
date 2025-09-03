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
package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermPaymentRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermFilterService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.account.service.reconcile.ReconcileCheckService;
import com.axelor.apps.account.service.reconcile.ReconcileInvoiceTermComputationServiceImpl;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class ReconcileInvoiceTermComputationBudgetServiceImpl
    extends ReconcileInvoiceTermComputationServiceImpl {

  protected BudgetDistributionService budgetDistributionService;
  protected AppBudgetService appBudgetService;
  protected ReconcileToolBudgetService reconcileToolBudgetService;

  @Inject
  public ReconcileInvoiceTermComputationBudgetServiceImpl(
      ReconcileCheckService reconcileCheckService,
      CurrencyScaleService currencyScaleService,
      InvoiceTermFilterService invoiceTermFilterService,
      InvoicePaymentCreateService invoicePaymentCreateService,
      InvoiceTermService invoiceTermService,
      InvoicePaymentToolService invoicePaymentToolService,
      CurrencyService currencyService,
      InvoicePaymentRepository invoicePaymentRepository,
      InvoiceTermPaymentRepository invoiceTermPaymentRepository,
      InvoiceRepository invoiceRepository,
      InvoiceTermPfpService invoiceTermPfpService,
      BudgetDistributionService budgetDistributionService,
      AppBudgetService appBudgetService,
      ReconcileToolBudgetService reconcileToolBudgetService) {
    super(
        reconcileCheckService,
        currencyScaleService,
        invoiceTermFilterService,
        invoicePaymentCreateService,
        invoiceTermService,
        invoicePaymentToolService,
        currencyService,
        invoicePaymentRepository,
        invoiceTermPaymentRepository,
        invoiceRepository,
        invoiceTermPfpService);
    this.budgetDistributionService = budgetDistributionService;
    this.appBudgetService = appBudgetService;
    this.reconcileToolBudgetService = reconcileToolBudgetService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updatePayment(
      Reconcile reconcile,
      MoveLine moveLine,
      MoveLine otherMoveLine,
      Invoice invoice,
      Move move,
      Move otherMove,
      BigDecimal amount,
      boolean updateInvoiceTerms)
      throws AxelorException {
    super.updatePayment(
        reconcile, moveLine, otherMoveLine, invoice, move, otherMove, amount, updateInvoiceTerms);

    if (appBudgetService.isApp("budget")) {
      BigDecimal ratio = reconcileToolBudgetService.computeReconcileRatio(invoice, move, amount);
      budgetDistributionService.computePaidAmount(invoice, move, ratio, false);
    }
  }
}
