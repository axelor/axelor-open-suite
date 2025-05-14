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
import com.axelor.apps.account.db.repo.InvoiceTermPaymentRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.SubrogationReleaseWorkflowService;
import com.axelor.apps.account.service.move.PaymentMoveLineDistributionService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelService;
import com.axelor.apps.account.service.reconcile.ReconcileToolService;
import com.axelor.apps.account.service.reconcile.UnreconcileServiceImpl;
import com.axelor.apps.account.service.reconcile.foreignexchange.ForeignExchangeGapService;
import com.axelor.apps.account.service.reconcile.reconcilegroup.UnreconcileGroupService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Optional;

public class UnreconcileBudgetServiceImpl extends UnreconcileServiceImpl {

  protected BudgetDistributionService budgetDistributionService;
  protected AppBudgetService appBudgetService;
  protected ReconcileToolBudgetService reconcileToolBudgetService;

  @Inject
  public UnreconcileBudgetServiceImpl(
      AppBaseService appBaseService,
      ReconcileToolService reconcileToolService,
      SubrogationReleaseWorkflowService subrogationReleaseWorkflowService,
      UnreconcileGroupService unReconcileGroupService,
      InvoicePaymentCancelService invoicePaymentCancelService,
      MoveLineTaxService moveLineTaxService,
      PaymentMoveLineDistributionService paymentMoveLineDistributionService,
      ForeignExchangeGapService foreignExchangeGapService,
      ReconcileRepository reconcileRepository,
      InvoicePaymentRepository invoicePaymentRepository,
      InvoiceTermPaymentRepository invoiceTermPaymentRepository,
      BudgetDistributionService budgetDistributionService,
      AppBudgetService appBudgetService,
      ReconcileToolBudgetService reconcileToolBudgetService) {
    super(
        appBaseService,
        reconcileToolService,
        subrogationReleaseWorkflowService,
        unReconcileGroupService,
        invoicePaymentCancelService,
        moveLineTaxService,
        paymentMoveLineDistributionService,
        foreignExchangeGapService,
        reconcileRepository,
        invoicePaymentRepository,
        invoiceTermPaymentRepository);
    this.budgetDistributionService = budgetDistributionService;
    this.appBudgetService = appBudgetService;
    this.reconcileToolBudgetService = reconcileToolBudgetService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void unreconcile(Reconcile reconcile) throws AxelorException {
    super.unreconcile(reconcile);
    if (!appBudgetService.isApp("budget")) {
      return;
    }

    Move debitMove =
        Optional.of(reconcile).map(Reconcile::getDebitMoveLine).map(MoveLine::getMove).orElse(null);
    Invoice debitInvoice = Optional.of(debitMove).map(Move::getInvoice).orElse(null);
    Move creditMove =
        Optional.of(reconcile)
            .map(Reconcile::getCreditMoveLine)
            .map(MoveLine::getMove)
            .orElse(null);
    Invoice creditInvoice = Optional.of(creditMove).map(Move::getInvoice).orElse(null);
    budgetDistributionService.computePaidAmount(
        debitInvoice,
        debitMove,
        reconcileToolBudgetService.computeReconcileRatio(
            debitInvoice, debitMove, reconcile.getAmount()),
        true);
    budgetDistributionService.computePaidAmount(
        creditInvoice,
        creditMove,
        reconcileToolBudgetService.computeReconcileRatio(
            creditInvoice, creditMove, reconcile.getAmount()),
        true);
  }
}
