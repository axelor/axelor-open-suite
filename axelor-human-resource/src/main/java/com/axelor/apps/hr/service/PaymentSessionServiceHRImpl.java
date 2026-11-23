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
package com.axelor.apps.hr.service;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionCancelService;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionServiceImpl;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionValidateService;
import com.axelor.apps.base.service.DateService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Expense;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
public class PaymentSessionServiceHRImpl extends PaymentSessionServiceImpl {

  protected final AppBaseService appBaseService;

  @Inject
  public PaymentSessionServiceHRImpl(
      PaymentSessionRepository paymentSessionRepository,
      InvoiceTermRepository invoiceTermRepository,
      InvoiceTermService invoiceTermService,
      PaymentSessionValidateService paymentSessionValidateService,
      AccountConfigService accountConfigService,
      DateService dateService,
      PaymentSessionCancelService paymentSessionCancelService,
      PfpService pfpService,
      AppBaseService appBaseService) {
    super(
        paymentSessionRepository,
        invoiceTermRepository,
        invoiceTermService,
        paymentSessionValidateService,
        accountConfigService,
        dateService,
        paymentSessionCancelService,
        pfpService);
    this.appBaseService = appBaseService;
  }

  @Override
  public boolean isNotAwaitingPayment(InvoiceTerm invoiceTerm) {
    if (!appBaseService.isApp("expense")) {
      return super.isNotAwaitingPayment(invoiceTerm);
    }

    if (invoiceTerm != null && invoiceTerm.getMoveLine() != null) {
      Expense expense = invoiceTerm.getMoveLine().getMove().getExpense();

      if (expense != null
          && expense.getPaymentStatusSelect() == InvoicePaymentRepository.STATUS_PENDING) {
        return false;
      }
    }

    return super.isNotAwaitingPayment(invoiceTerm);
  }
}
