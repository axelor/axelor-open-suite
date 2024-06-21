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
package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceTermPaymentRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.db.repo.SubrogationReleaseRepository;
import com.axelor.apps.account.service.SubrogationReleaseWorkflowService;
import com.axelor.apps.account.service.move.PaymentMoveLineDistributionService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelService;
import com.axelor.apps.account.service.reconcile.reconcilegroup.UnreconcileGroupService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnreconcileServiceImpl implements UnreconcileService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppBaseService appBaseService;
  protected ReconcileToolService reconcileToolService;
  protected SubrogationReleaseWorkflowService subrogationReleaseWorkflowService;
  protected UnreconcileGroupService unReconcileGroupService;
  protected InvoicePaymentCancelService invoicePaymentCancelService;
  protected MoveLineTaxService moveLineTaxService;
  protected PaymentMoveLineDistributionService paymentMoveLineDistributionService;
  protected ReconcileRepository reconcileRepository;
  protected InvoicePaymentRepository invoicePaymentRepository;
  protected InvoiceTermPaymentRepository invoiceTermPaymentRepository;

  @Inject
  public UnreconcileServiceImpl(
      AppBaseService appBaseService,
      ReconcileToolService reconcileToolService,
      SubrogationReleaseWorkflowService subrogationReleaseWorkflowService,
      UnreconcileGroupService unReconcileGroupService,
      InvoicePaymentCancelService invoicePaymentCancelService,
      MoveLineTaxService moveLineTaxService,
      PaymentMoveLineDistributionService paymentMoveLineDistributionService,
      ReconcileRepository reconcileRepository,
      InvoicePaymentRepository invoicePaymentRepository,
      InvoiceTermPaymentRepository invoiceTermPaymentRepository) {
    this.appBaseService = appBaseService;
    this.reconcileToolService = reconcileToolService;
    this.subrogationReleaseWorkflowService = subrogationReleaseWorkflowService;
    this.unReconcileGroupService = unReconcileGroupService;
    this.invoicePaymentCancelService = invoicePaymentCancelService;
    this.moveLineTaxService = moveLineTaxService;
    this.paymentMoveLineDistributionService = paymentMoveLineDistributionService;
    this.reconcileRepository = reconcileRepository;
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.invoiceTermPaymentRepository = invoiceTermPaymentRepository;
  }

  /**
   * Permet de déréconcilier
   *
   * @param reconcile Une reconciliation
   * @return L'etat de la réconciliation
   * @throws AxelorException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void unreconcile(Reconcile reconcile) throws AxelorException {

    log.debug("unreconcile : reconcile : {}", reconcile);

    MoveLine debitMoveLine = reconcile.getDebitMoveLine();
    MoveLine creditMoveLine = reconcile.getCreditMoveLine();
    Invoice invoice = debitMoveLine.getMove().getInvoice();

    // Change the state
    reconcile.setStatusSelect(ReconcileRepository.STATUS_CANCELED);
    reconcile.setReconciliationCancelDateTime(
        appBaseService.getTodayDateTime(reconcile.getCompany()).toLocalDateTime());
    // Add the reconciled amount to the reconciled amount in the move line
    creditMoveLine.setAmountPaid(creditMoveLine.getAmountPaid().subtract(reconcile.getAmount()));
    debitMoveLine.setAmountPaid(debitMoveLine.getAmountPaid().subtract(reconcile.getAmount()));

    reconcileRepository.save(reconcile);

    // Update amount remaining on invoice or refund
    reconcileToolService.updatePartnerAccountingSituation(reconcile);
    reconcileToolService.updateInvoiceCompanyInTaxTotalRemaining(reconcile);
    reconcileToolService.updateInvoiceTermsAmountRemaining(reconcile);
    this.updateInvoicePaymentsCanceled(reconcile);
    this.reverseTaxPaymentMoveLines(reconcile);
    this.reversePaymentMoveLineDistributionLines(reconcile);
    if (invoice != null
        && invoice.getSubrogationRelease() != null
        && invoice.getSubrogationRelease().getStatusSelect()
            != SubrogationReleaseRepository.STATUS_ACCOUNTED) {
      subrogationReleaseWorkflowService.goBackToAccounted(invoice.getSubrogationRelease());
    }
    // Update reconcile group
    unReconcileGroupService.remove(reconcile);
  }

  protected void updateInvoicePaymentsCanceled(Reconcile reconcile) throws AxelorException {

    log.debug("updateInvoicePaymentsCanceled : reconcile : {}", reconcile);
    for (InvoicePayment invoicePayment :
        invoicePaymentRepository.findByReconcile(reconcile).fetch()) {
      invoicePaymentCancelService.updateCancelStatus(invoicePayment);
    }

    invoiceTermPaymentRepository
        .findByReconcileId(reconcile.getId())
        .fetch()
        .forEach(it -> it.setInvoiceTerm(null));
  }

  protected void reverseTaxPaymentMoveLines(Reconcile reconcile) throws AxelorException {
    Move debitMove = reconcile.getDebitMoveLine().getMove();
    Move creditMove = reconcile.getCreditMoveLine().getMove();
    Invoice debitInvoice = debitMove.getInvoice();
    Invoice creditInvoice = creditMove.getInvoice();
    if (debitInvoice == null) {
      moveLineTaxService.reverseTaxPaymentMoveLines(reconcile.getDebitMoveLine(), reconcile);
    }
    if (creditInvoice == null) {
      moveLineTaxService.reverseTaxPaymentMoveLines(reconcile.getCreditMoveLine(), reconcile);
    }
  }

  protected void reversePaymentMoveLineDistributionLines(Reconcile reconcile) {
    // FIXME This feature will manage at a first step only reconcile of purchase (journal type of
    // type purchase)
    Move purchaseMove = reconcile.getCreditMoveLine().getMove();
    if (!purchaseMove.getJournal().getJournalType().getCode().equals("ACH")
        || CollectionUtils.isEmpty(reconcile.getPaymentMoveLineDistributionList())) {
      return;
    }
    paymentMoveLineDistributionService.reversePaymentMoveLineDistributionList(reconcile);
  }
}
