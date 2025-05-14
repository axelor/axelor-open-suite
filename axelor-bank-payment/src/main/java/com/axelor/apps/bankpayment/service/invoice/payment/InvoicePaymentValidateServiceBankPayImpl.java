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
package com.axelor.apps.bankpayment.service.invoice.payment;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentMoveCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentValidateServiceImpl;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
public class InvoicePaymentValidateServiceBankPayImpl extends InvoicePaymentValidateServiceImpl {
  protected PaymentModeService paymentModeService;

  @Inject
  public InvoicePaymentValidateServiceBankPayImpl(
      InvoicePaymentRepository invoicePaymentRepository,
      InvoicePaymentToolService invoicePaymentToolService,
      InvoicePaymentMoveCreateService invoicePaymentMoveCreateService,
      PaymentModeService paymentModeService) {
    super(invoicePaymentRepository, invoicePaymentToolService, invoicePaymentMoveCreateService);
    this.paymentModeService = paymentModeService;
  }

  @Override
  protected void setInvoicePaymentStatus(InvoicePayment invoicePayment) throws AxelorException {
    Invoice invoice = invoicePayment.getInvoice();
    PaymentMode paymentMode = invoicePayment.getPaymentMode();
    PaymentSession paymentSession = invoicePayment.getPaymentSession();
    if (paymentMode == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(BankPaymentExceptionMessage.INVOICE_PAYMENT_MODE_MISSING),
          invoice.getInvoiceId());
    }

    if (paymentModeService.isPendingPayment(paymentMode)
        && paymentMode.getGenerateBankOrder()
        && ((paymentSession != null
                && paymentSession.getAccountingTriggerSelect()
                    != PaymentModeRepository.ACCOUNTING_TRIGGER_IMMEDIATE)
            || (paymentSession == null
                && paymentMode.getAccountingTriggerSelect()
                    != PaymentModeRepository.ACCOUNTING_TRIGGER_IMMEDIATE))
        && invoicePayment.getStatusSelect() == InvoicePaymentRepository.STATUS_DRAFT) {
      invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_PENDING);
    } else {
      invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
    }
  }
}
