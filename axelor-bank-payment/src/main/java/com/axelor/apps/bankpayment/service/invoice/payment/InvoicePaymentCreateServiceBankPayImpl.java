/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service.invoice.payment;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateServiceImpl;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMergeService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.List;

public class InvoicePaymentCreateServiceBankPayImpl extends InvoicePaymentCreateServiceImpl {

  @Inject
  public InvoicePaymentCreateServiceBankPayImpl(
      InvoicePaymentRepository invoicePaymentRepository,
      InvoicePaymentToolService invoicePaymentToolService,
      CurrencyService currencyService,
      AppBaseService appBaseService) {

    super(invoicePaymentRepository, invoicePaymentToolService, currencyService, appBaseService);
  }

  @Override
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

    if (invoicePaymentList.isEmpty()) {
      return invoicePaymentList;
    }

    if (paymentMode.getGenerateBankOrder()) {
      Beans.get(BankOrderMergeService.class).mergeFromInvoicePayments(invoicePaymentList);
    }
    return invoicePaymentList;
  }
}
