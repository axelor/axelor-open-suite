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
package com.axelor.apps.bankpayment.service.invoice.payment;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.InvoiceVisibilityService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoiceTermPaymentService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMergeService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RequestScoped
public class InvoicePaymentCreateServiceBankPayImpl extends InvoicePaymentCreateServiceImpl {

  @Inject
  public InvoicePaymentCreateServiceBankPayImpl(
      InvoicePaymentRepository invoicePaymentRepository,
      InvoicePaymentToolService invoicePaymentToolService,
      CurrencyService currencyService,
      AppBaseService appBaseService,
      InvoiceTermPaymentService invoiceTermPaymentService,
      InvoiceTermService invoiceTermService,
      InvoiceService invoiceService,
      InvoiceVisibilityService invoiceVisibilityService) {

    super(
        invoicePaymentRepository,
        invoicePaymentToolService,
        currencyService,
        appBaseService,
        invoiceTermPaymentService,
        invoiceTermService,
        invoiceService,
        invoiceVisibilityService);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<InvoicePayment> createMassInvoicePayment(
      List<Long> invoiceList,
      PaymentMode paymentMode,
      BankDetails companyBankDetails,
      LocalDate paymentDate,
      LocalDate bankDepositDate,
      String chequeNumber)
      throws AxelorException {

    List<InvoicePayment> invoicePaymentList =
        super.createMassInvoicePayment(
            invoiceList,
            paymentMode,
            companyBankDetails,
            paymentDate,
            bankDepositDate,
            chequeNumber);

    if (!appBaseService.isApp("bank-payment")) {
      return invoicePaymentList;
    }

    if (invoicePaymentList.isEmpty()) {
      return invoicePaymentList;
    }

    if (paymentMode.getGenerateBankOrder()) {
      Beans.get(BankOrderMergeService.class).mergeFromInvoicePayments(invoicePaymentList);
    }
    return invoicePaymentList;
  }

  @Override
  public InvoicePayment createInvoicePayment(Invoice invoice, BigDecimal amount, Move paymentMove)
      throws AxelorException {
    InvoicePayment invoicePayment = null;
    if (invoice != null && !ObjectUtils.isEmpty(invoice.getInvoicePaymentList())) {
      invoicePayment =
          invoice.getInvoicePaymentList().stream()
              .filter(
                  it ->
                      (it.getAmount().compareTo(amount) == 0
                          && it.getMove() == null
                          && it.getReconcile() == null
                          && Objects.equals(paymentMove.getCurrency(), it.getCurrency())
                          && it.getBankOrder() != null
                          && it.getBankOrder().getAccountingTriggerSelect()
                              == PaymentModeRepository.ACCOUNTING_TRIGGER_NONE))
              .findFirst()
              .orElse(null);
    }

    if (invoicePayment == null) {
      return super.createInvoicePayment(invoice, amount, paymentMove);
    }

    invoicePayment.setMove(paymentMove);
    invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);

    invoicePaymentToolService.updateAmountPaid(invoice);
    invoicePaymentRepository.save(invoicePayment);

    return invoicePayment;
  }
}
