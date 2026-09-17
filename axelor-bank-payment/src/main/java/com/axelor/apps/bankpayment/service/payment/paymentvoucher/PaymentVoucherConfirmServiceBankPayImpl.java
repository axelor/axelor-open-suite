/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bankpayment.service.payment.paymentvoucher;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PayVoucherElementToPay;
import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.PayVoucherElementToPayRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.PaymentVoucherRepository;
import com.axelor.apps.account.service.FinancialDiscountService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveCutOffService;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineFinancialDiscountService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherConfirmService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherControlService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherSequenceService;
import com.axelor.apps.account.service.payment.paymentvoucher.PaymentVoucherToolService;
import com.axelor.apps.account.service.reconcile.ReconcileService;
import com.axelor.apps.account.service.reconcile.foreignexchange.ForeignExchangeGapToolService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderValidationService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class PaymentVoucherConfirmServiceBankPayImpl extends PaymentVoucherConfirmService {

  protected BankOrderCreateService bankOrderCreateService;
  protected BankOrderValidationService bankOrderValidationService;
  protected InvoicePaymentToolService invoicePaymentToolService;

  @Inject
  public PaymentVoucherConfirmServiceBankPayImpl(
      ReconcileService reconcileService,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveCutOffService moveCutOffService,
      MoveLineCreateService moveLineCreateService,
      PaymentService paymentService,
      PaymentModeService paymentModeService,
      PaymentVoucherSequenceService paymentVoucherSequenceService,
      PaymentVoucherControlService paymentVoucherControlService,
      PaymentVoucherToolService paymentVoucherToolService,
      MoveLineInvoiceTermService moveLineInvoiceTermService,
      PayVoucherElementToPayRepository payVoucherElementToPayRepo,
      PaymentVoucherRepository paymentVoucherRepository,
      CurrencyService currencyService,
      InvoiceTermService invoiceTermService,
      InvoiceTermRepository invoiceTermRepository,
      MoveLineFinancialDiscountService moveLineFinancialDiscountService,
      FinancialDiscountService financialDiscountService,
      CurrencyScaleService currencyScaleService,
      InvoicePaymentRepository invoicePaymentRepository,
      ForeignExchangeGapToolService foreignExchangeGapToolService,
      AppBaseService appBaseService,
      BankOrderCreateService bankOrderCreateService,
      BankOrderValidationService bankOrderValidationService,
      InvoicePaymentToolService invoicePaymentToolService) {
    super(
        reconcileService,
        moveCreateService,
        moveValidateService,
        moveCutOffService,
        moveLineCreateService,
        paymentService,
        paymentModeService,
        paymentVoucherSequenceService,
        paymentVoucherControlService,
        paymentVoucherToolService,
        moveLineInvoiceTermService,
        payVoucherElementToPayRepo,
        paymentVoucherRepository,
        currencyService,
        invoiceTermService,
        invoiceTermRepository,
        moveLineFinancialDiscountService,
        financialDiscountService,
        currencyScaleService,
        invoicePaymentRepository,
        foreignExchangeGapToolService,
        appBaseService);
    this.bankOrderCreateService = bankOrderCreateService;
    this.bankOrderValidationService = bankOrderValidationService;
    this.invoicePaymentToolService = invoicePaymentToolService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void confirmPaymentVoucher(PaymentVoucher paymentVoucher) throws AxelorException {
    super.confirmPaymentVoucher(paymentVoucher);
    if (paymentVoucher.getPaymentMode().getGenerateBankOrder()
        && paymentVoucher.getBankOrder() == null) {
      createBankOrder(paymentVoucher);
    }
  }

  protected boolean isAccountingDeferred(PaymentVoucher paymentVoucher) {
    int accountingTriggerSelect = paymentVoucher.getPaymentMode().getAccountingTriggerSelect();
    return accountingTriggerSelect == PaymentModeRepository.ACCOUNTING_TRIGGER_NONE
        || accountingTriggerSelect == PaymentModeRepository.ACCOUNTING_TRIGGER_CONFIRMATION
        || accountingTriggerSelect == PaymentModeRepository.ACCOUNTING_TRIGGER_REALIZATION;
  }

  @Override
  protected boolean shouldGenerateMoveImmediately(PaymentVoucher paymentVoucher) {
    if (!paymentVoucher.getPaymentMode().getGenerateBankOrder()) {
      return true;
    }
    if (paymentVoucher.getBankOrder() != null) {
      return true;
    }
    return !isAccountingDeferred(paymentVoucher);
  }

  @Override
  protected void beforeConfirmReconcile(
      Move move, Reconcile reconcile, PaymentVoucher paymentVoucher, Invoice invoice) {
    if (paymentVoucher == null || paymentVoucher.getBankOrder() == null || invoice == null) {
      return;
    }
    InvoicePayment invoicePayment =
        invoicePaymentRepository
            .all()
            .filter(
                "self.invoice = ?1 AND self.bankOrder = ?2 AND self.move IS NULL AND self.reconcile IS NULL",
                invoice,
                paymentVoucher.getBankOrder())
            .fetchOne();
    if (invoicePayment != null) {
      invoicePayment.setMove(move);
      invoicePayment.setReconcile(reconcile);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void createBankOrder(PaymentVoucher paymentVoucher) throws AxelorException {
    BankOrder bankOrder = bankOrderCreateService.createBankOrder(paymentVoucher);
    if (isAccountingDeferred(paymentVoucher)) {
      createPendingInvoicePayments(paymentVoucher, bankOrder);
    } else {
      linkExistingInvoicePayments(paymentVoucher, bankOrder);
    }
    if (paymentVoucher.getPaymentMode().getAutoConfirmBankOrder()) {
      bankOrderValidationService.confirm(bankOrder);
    }
  }

  protected void linkExistingInvoicePayments(PaymentVoucher paymentVoucher, BankOrder bankOrder) {
    if (paymentVoucher.getGeneratedMove() == null) {
      return;
    }
    for (InvoicePayment invoicePayment :
        invoicePaymentRepository
            .all()
            .filter("self.move = ?1", paymentVoucher.getGeneratedMove())
            .fetch()) {
      invoicePayment.setBankOrder(bankOrder);
      invoicePaymentRepository.save(invoicePayment);
    }
  }

  protected void createPendingInvoicePayments(PaymentVoucher paymentVoucher, BankOrder bankOrder)
      throws AxelorException {
    Set<Invoice> invoices = new LinkedHashSet<>();
    for (PayVoucherElementToPay payVoucherElementToPay :
        paymentVoucher.getPayVoucherElementToPayList()) {
      Invoice invoice =
          Optional.ofNullable(payVoucherElementToPay.getMoveLine())
              .map(MoveLine::getMove)
              .map(Move::getInvoice)
              .orElse(null);
      BigDecimal amount = payVoucherElementToPay.getAmountToPayCurrency();
      if (invoice == null || amount == null || amount.signum() == 0) {
        continue;
      }
      if (payVoucherElementToPay.getApplyFinancialDiscount()) {
        amount =
            amount
                .add(payVoucherElementToPay.getFinancialDiscountAmount())
                .add(payVoucherElementToPay.getFinancialDiscountTaxAmount());
      }

      InvoicePayment invoicePayment = new InvoicePayment();
      invoicePayment.setInvoice(invoice);
      invoicePayment.setAmount(amount);
      invoicePayment.setCurrency(paymentVoucher.getCurrency());
      invoicePayment.setPaymentDate(paymentVoucher.getPaymentDate());
      invoicePayment.setPaymentMode(paymentVoucher.getPaymentMode());
      invoicePayment.setCompanyBankDetails(paymentVoucher.getCompanyBankDetails());
      invoicePayment.setTypeSelect(InvoicePaymentRepository.TYPE_PAYMENT);
      invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_PENDING);
      invoicePayment.setBankOrder(bankOrder);
      invoice.addInvoicePaymentListItem(invoicePayment);
      invoicePaymentRepository.save(invoicePayment);
      invoices.add(invoice);
    }

    for (Invoice invoice : invoices) {
      invoicePaymentToolService.updateAmountPaid(invoice);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void finalizePaymentVoucher(PaymentVoucher paymentVoucher) throws AxelorException {
    if (paymentVoucher.getGeneratedMove() != null) {
      return;
    }
    BankOrder bankOrder = paymentVoucher.getBankOrder();
    paymentVoucher.setPaymentDate(bankOrder.getBankOrderDate());

    createMoveAndConfirm(paymentVoucher);

    Set<Invoice> invoices = new LinkedHashSet<>();
    for (InvoicePayment invoicePayment :
        invoicePaymentRepository.findByBankOrder(bankOrder).fetch()) {
      invoicePayment.setPaymentDate(bankOrder.getBankOrderDate());
      invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
      invoicePaymentRepository.save(invoicePayment);
      invoices.add(invoicePayment.getInvoice());
    }
    for (Invoice invoice : invoices) {
      invoicePaymentToolService.updateAmountPaid(invoice);
    }
    paymentVoucherRepository.save(paymentVoucher);
  }
}
