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
package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceTermPaymentRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.AccountCustomerService;
import com.axelor.apps.account.service.ReconcileSequenceService;
import com.axelor.apps.account.service.ReconcileServiceImpl;
import com.axelor.apps.account.service.SubrogationReleaseWorkflowService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveAdjustementService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.move.PaymentMoveLineDistributionService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoiceTermPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class ReconcileBudgetServiceImpl extends ReconcileServiceImpl {

  protected BudgetDistributionService budgetDistributionService;
  protected AppBudgetService appBudgetService;

  private final int CALCULATION_SCALE = 10;

  @Inject
  public ReconcileBudgetServiceImpl(
      MoveToolService moveToolService,
      AccountCustomerService accountCustomerService,
      AccountConfigService accountConfigService,
      ReconcileRepository reconcileRepository,
      MoveAdjustementService moveAdjustementService,
      ReconcileSequenceService reconcileSequenceService,
      InvoicePaymentCancelService invoicePaymentCancelService,
      InvoicePaymentCreateService invoicePaymentCreateService,
      MoveLineTaxService moveLineTaxService,
      InvoicePaymentRepository invoicePaymentRepo,
      InvoiceTermService invoiceTermService,
      AppBaseService appBaseService,
      PaymentMoveLineDistributionService paymentMoveLineDistributionService,
      InvoiceTermPaymentService invoiceTermPaymentService,
      InvoiceTermPaymentRepository invoiceTermPaymentRepo,
      InvoicePaymentToolService invoicePaymentToolService,
      MoveLineControlService moveLineControlService,
      MoveLineRepository moveLineRepo,
      SubrogationReleaseWorkflowService subrogationReleaseWorkflowService,
      MoveCreateService moveCreateService,
      MoveLineCreateService moveLineCreateService,
      MoveValidateService moveValidateService,
      InvoiceTermPfpService invoiceTermPfpService,
      CurrencyService currencyService,
      BudgetDistributionService budgetDistributionService,
      AppBudgetService appBudgetService) {
    super(
        moveToolService,
        accountCustomerService,
        accountConfigService,
        reconcileRepository,
        moveAdjustementService,
        reconcileSequenceService,
        invoicePaymentCancelService,
        invoicePaymentCreateService,
        moveLineTaxService,
        invoicePaymentRepo,
        invoiceTermService,
        appBaseService,
        paymentMoveLineDistributionService,
        invoiceTermPaymentService,
        invoiceTermPaymentRepo,
        invoicePaymentToolService,
        moveLineControlService,
        moveLineRepo,
        subrogationReleaseWorkflowService,
        moveCreateService,
        moveLineCreateService,
        moveValidateService,
        invoiceTermPfpService,
        currencyService);
    this.budgetDistributionService = budgetDistributionService;
    this.appBudgetService = appBudgetService;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void updatePayment(
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
      BigDecimal ratio = computeReconcileRatio(invoice, move, amount);
      budgetDistributionService.computePaidAmount(invoice, move, ratio, false);
    }
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
        computeReconcileRatio(debitInvoice, debitMove, reconcile.getAmount()),
        true);
    budgetDistributionService.computePaidAmount(
        creditInvoice,
        creditMove,
        computeReconcileRatio(creditInvoice, creditMove, reconcile.getAmount()),
        true);
  }

  protected BigDecimal computeReconcileRatio(Invoice invoice, Move move, BigDecimal amount) {
    BigDecimal ratio = BigDecimal.ZERO;
    BigDecimal totalAmount = BigDecimal.ZERO;
    if (invoice != null) {
      totalAmount = invoice.getCompanyInTaxTotal();
    } else if (!CollectionUtils.isEmpty(move.getMoveLineList())) {
      totalAmount =
          move.getMoveLineList().stream()
              .map(MoveLine::getDebit)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);
    }

    if (totalAmount.signum() > 0) {
      ratio = amount.divide(totalAmount, CALCULATION_SCALE, RoundingMode.HALF_UP);
    }

    return ratio;
  }
}
