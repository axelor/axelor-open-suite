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
package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveCancelService;
import com.axelor.apps.account.service.reconcile.ReconcileToolService;
import com.axelor.apps.account.service.reconcile.UnreconcileService;
import com.axelor.apps.base.AxelorException;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoicePaymentCancelServiceImpl implements InvoicePaymentCancelService {

  protected InvoicePaymentRepository invoicePaymentRepository;
  protected MoveCancelService moveCancelService;
  protected InvoicePaymentToolService invoicePaymentToolService;
  protected InvoiceTermService invoiceTermService;
  protected ReconcileToolService reconcileToolService;
  protected UnreconcileService unreconcileService;

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public InvoicePaymentCancelServiceImpl(
      InvoicePaymentRepository invoicePaymentRepository,
      MoveCancelService moveCancelService,
      InvoicePaymentToolService invoicePaymentToolService,
      InvoiceTermService invoiceTermService,
      ReconcileToolService reconcileToolService,
      UnreconcileService unreconcileService) {
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.moveCancelService = moveCancelService;
    this.invoicePaymentToolService = invoicePaymentToolService;
    this.invoiceTermService = invoiceTermService;
    this.reconcileToolService = reconcileToolService;
    this.unreconcileService = unreconcileService;
  }

  /**
   * Method to cancel an invoice Payment
   *
   * <p>Cancel the eventual Move and Reconcile Compute the total amount paid on the linked invoice
   * Change the status to cancel
   *
   * @param invoicePayment An invoice payment
   * @throws AxelorException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancel(InvoicePayment invoicePayment) throws AxelorException {
    Move paymentMove = invoicePayment.getMove();

    if (paymentMove != null) {
      unlinkPaymentMoveLine(invoicePayment);
    } else {
      cancelImputedInvoicePayment(invoicePayment);
    }
    updateCancelStatus(invoicePayment);
  }

  @Override
  public void validateBeforeUnlink(InvoicePayment invoicePayment) throws AxelorException {
    if (invoicePayment == null || invoicePayment.getMove() == null) {
      return;
    }

    moveCancelService.checkBeforeCancel(invoicePayment.getMove());
  }

  protected void unlinkPaymentMoveLine(InvoicePayment invoicePayment) throws AxelorException {
    if (invoicePayment == null
        || invoicePayment.getReconcile() == null
        || invoicePayment.getMove() == null) {
      return;
    }

    unreconcileService.unreconcile(invoicePayment.getReconcile());
  }

  @Transactional(rollbackOn = {Exception.class})
  public void updateCancelStatus(InvoicePayment invoicePayment) throws AxelorException {
    invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_CANCELED);

    invoicePaymentToolService.updateAmountPaid(invoicePayment.getInvoice());
    removeAllLinks(invoicePayment);

    invoicePaymentRepository.save(invoicePayment);
  }

  protected void removeAllLinks(InvoicePayment invoicePayment) {
    if (ObjectUtils.notEmpty(invoicePayment.getInvoiceTermPaymentList())) {
      invoicePayment.getInvoiceTermPaymentList().stream()
          .map(InvoiceTermPayment::getInvoiceTerm)
          .filter(Objects::nonNull)
          .forEach(it -> it.setPaymentSession(null));
      invoicePayment.getInvoiceTermPaymentList().forEach(it -> it.setInvoiceTerm(null));
    }
    invoicePayment.setMove(null);
    invoicePayment.setPaymentSession(null);
    invoicePayment.setReconcile(null);
  }

  protected void cancelImputedInvoicePayment(InvoicePayment invoicePayment) throws AxelorException {
    List<Integer> imputationType =
        Arrays.asList(
            InvoicePaymentRepository.TYPE_ADV_PAYMENT_IMPUTATION,
            InvoicePaymentRepository.TYPE_REFUND_IMPUTATION);
    InvoicePayment imputedBy = invoicePayment.getImputedBy();
    if (imputationType.contains(invoicePayment.getTypeSelect())
        && imputedBy != null
        && imputationType.contains(imputedBy.getTypeSelect())) {
      invoiceTermService.updateInvoiceTermsAmountRemaining(
          invoicePayment.getInvoiceTermPaymentList());
      invoiceTermService.updateInvoiceTermsAmountRemaining(imputedBy.getInvoiceTermPaymentList());
      updateCancelStatus(imputedBy);
    }
  }
}
