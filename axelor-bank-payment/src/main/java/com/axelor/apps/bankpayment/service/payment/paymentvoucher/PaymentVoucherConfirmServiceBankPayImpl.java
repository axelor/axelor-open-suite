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

import com.axelor.apps.account.db.PaymentVoucher;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.PayVoucherElementToPayRepository;
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

public class PaymentVoucherConfirmServiceBankPayImpl extends PaymentVoucherConfirmService {

  protected BankOrderCreateService bankOrderCreateService;
  protected BankOrderValidationService bankOrderValidationService;

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
      BankOrderValidationService bankOrderValidationService) {
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

  @Transactional(rollbackOn = {Exception.class})
  public void createBankOrder(PaymentVoucher paymentVoucher) throws AxelorException {
    BankOrder bankOrder = bankOrderCreateService.createBankOrder(paymentVoucher);
    if (paymentVoucher.getPaymentMode().getAutoConfirmBankOrder()) {
      bankOrderValidationService.confirm(bankOrder);
    }
  }
}
