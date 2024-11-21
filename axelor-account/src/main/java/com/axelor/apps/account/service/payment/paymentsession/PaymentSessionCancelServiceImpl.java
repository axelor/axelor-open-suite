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
package com.axelor.apps.account.service.payment.paymentsession;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public class PaymentSessionCancelServiceImpl implements PaymentSessionCancelService {
  protected PaymentSessionRepository paymentSessionRepo;
  protected InvoiceTermRepository invoiceTermRepo;
  protected int jpaLimit = 4;

  @Inject
  public PaymentSessionCancelServiceImpl(
      PaymentSessionRepository paymentSessionRepo, InvoiceTermRepository invoiceTermRepo) {
    this.paymentSessionRepo = paymentSessionRepo;
    this.invoiceTermRepo = invoiceTermRepo;
  }

  @Override
  public void cancelPaymentSession(PaymentSession paymentSession) {
    this.cancelInvoiceTerms(paymentSession);
    this.saveCanceledPaymentSession(paymentSession);
  }

  @Transactional
  protected void saveCanceledPaymentSession(PaymentSession paymentSession) {
    paymentSession = paymentSessionRepo.find(paymentSession.getId());
    Query<InvoiceTerm> invoiceTermQuery =
        invoiceTermRepo.all().filter("self.paymentSession = ?", paymentSession).order("id");

    if (invoiceTermQuery.fetch().isEmpty()) {
      paymentSession.setStatusSelect(PaymentSessionRepository.STATUS_CANCELLED);
      paymentSessionRepo.save(paymentSession);
    }
  }

  @Override
  public void cancelInvoiceTerms(PaymentSession paymentSession) {
    List<InvoiceTerm> invoiceTermList;
    Query<InvoiceTerm> invoiceTermQuery =
        invoiceTermRepo.all().filter("self.paymentSession = ?", paymentSession).order("id");

    while (!(invoiceTermList = invoiceTermQuery.fetch(jpaLimit)).isEmpty()) {
      for (InvoiceTerm invoiceTerm : invoiceTermList) {
        this.cancelInvoiceTerm(invoiceTerm);
      }
      JPA.clear();
      paymentSession = paymentSessionRepo.find(paymentSession.getId());
    }
  }

  @Transactional
  protected void cancelInvoiceTerm(InvoiceTerm invoiceTerm) {
    invoiceTerm.setPaymentSession(null);
    invoiceTerm.setIsSelectedOnPaymentSession(false);
    invoiceTerm.setApplyFinancialDiscountOnPaymentSession(false);
    invoiceTerm.setPaymentAmount(BigDecimal.ZERO);
    invoiceTerm.setAmountPaid(BigDecimal.ZERO);
  }
}
