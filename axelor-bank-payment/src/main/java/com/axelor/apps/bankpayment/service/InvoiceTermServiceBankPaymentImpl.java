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
package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.service.JournalService;
import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceTermDateComputeService;
import com.axelor.apps.account.service.invoice.InvoiceTermFinancialDiscountService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpToolService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpUpdateService;
import com.axelor.apps.account.service.invoice.InvoiceTermServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceTermToolService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoiceTermPaymentService;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;

public class InvoiceTermServiceBankPaymentImpl extends InvoiceTermServiceImpl {

  protected final BankDetailsBankPaymentService bankDetailsBankPaymentService;

  @Inject
  public InvoiceTermServiceBankPaymentImpl(
      InvoiceTermRepository invoiceTermRepo,
      InvoiceRepository invoiceRepo,
      AppAccountService appAccountService,
      JournalService journalService,
      InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService,
      UserRepository userRepo,
      PfpService pfpService,
      CurrencyScaleService currencyScaleService,
      DMSFileRepository DMSFileRepo,
      InvoiceTermPaymentService invoiceTermPaymentService,
      CurrencyService currencyService,
      AppBaseService appBaseService,
      InvoiceTermPfpUpdateService invoiceTermPfpUpdateService,
      InvoiceTermToolService invoiceTermToolService,
      InvoiceTermPfpToolService invoiceTermPfpToolService,
      InvoiceTermDateComputeService invoiceTermDateComputeService,
      BankDetailsBankPaymentService bankDetailsBankPaymentService) {
    super(
        invoiceTermRepo,
        invoiceRepo,
        appAccountService,
        journalService,
        invoiceTermFinancialDiscountService,
        userRepo,
        pfpService,
        currencyScaleService,
        DMSFileRepo,
        invoiceTermPaymentService,
        currencyService,
        appBaseService,
        invoiceTermPfpUpdateService,
        invoiceTermToolService,
        invoiceTermPfpToolService,
        invoiceTermDateComputeService);
    this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
  }

  @Override
  public InvoiceTerm createInvoiceTerm(
      Invoice invoice,
      Move move,
      MoveLine moveLine,
      BankDetails bankDetails,
      User pfpUser,
      PaymentMode paymentMode,
      LocalDate date,
      LocalDate estimatedPaymentDate,
      BigDecimal amount,
      BigDecimal percentage,
      int sequence,
      boolean isHoldBack)
      throws AxelorException {
    InvoiceTerm newInvoiceTerm =
        super.createInvoiceTerm(
            invoice,
            move,
            moveLine,
            bankDetails,
            pfpUser,
            paymentMode,
            date,
            estimatedPaymentDate,
            amount,
            percentage,
            sequence,
            isHoldBack);

    bankDetailsBankPaymentService
        .getBankDetailsLinkedToActiveUmr(
            paymentMode, newInvoiceTerm.getPartner(), newInvoiceTerm.getCompany())
        .stream()
        .findAny()
        .ifPresent(newInvoiceTerm::setBankDetails);

    return newInvoiceTerm;
  }
}
