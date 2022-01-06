/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.google.inject.Inject;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class PaymentSessionServiceImpl implements PaymentSessionService {

  protected InvoiceTermRepository invoiceTermRepo;

  @Inject
  public PaymentSessionServiceImpl(InvoiceTermRepository invoiceTermRepo) {
    this.invoiceTermRepo = invoiceTermRepo;
  }

  @Override
  public String computeName(PaymentSession paymentSession) {
    StringBuilder name = new StringBuilder("Session");
    User createdBy = paymentSession.getCreatedBy();
    Boolean isFr =
        ObjectUtils.notEmpty(createdBy)
            && ObjectUtils.notEmpty(createdBy.getLanguage())
            && createdBy.getLanguage().equals(Locale.FRENCH.getLanguage());
    if (ObjectUtils.notEmpty(paymentSession.getPaymentMode())) {
      name.append(" " + paymentSession.getPaymentMode().getName());
    }
    if (ObjectUtils.notEmpty(paymentSession.getCreatedOn())) {
      name.append(
          (isFr ? " du " : " on the ")
              + paymentSession
                  .getCreatedOn()
                  .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }
    if (ObjectUtils.notEmpty(createdBy)) {
      name.append((isFr ? " par " : " by ") + createdBy.getName());
    }
    return name.toString();
  }

  @Override
  public boolean validateInvoiceTerms(PaymentSession paymentSession) {
    int offset = 0;
    List<InvoiceTerm> invoiceTermList;
    Query<InvoiceTerm> invoiceTermQuery =
        invoiceTermRepo
            .all()
            .filter(
                "self.paymentSession = :paymentSession "
                    + "AND self.isSelectedOnPaymentSession IS TRUE "
                    + "AND self.financialDiscount IS NOT NULL")
            .bind("paymentSession", paymentSession)
            .order("id");

    LocalDate nextSessionDate = paymentSession.getNextSessionDate();

    if (nextSessionDate == null) {
      return true;
    }

    while (!(invoiceTermList = invoiceTermQuery.fetch(AbstractBatch.FETCH_LIMIT, offset))
        .isEmpty()) {
      for (InvoiceTerm invoiceTerm : invoiceTermList) {
        offset++;

        if ((invoiceTerm.getInvoice() != null
                && !invoiceTerm
                    .getInvoice()
                    .getFinancialDiscountDeadlineDate()
                    .isAfter(nextSessionDate))
            || (invoiceTerm.getMoveLine() != null
                && invoiceTerm.getMoveLine().getPartner() != null
                && invoiceTerm.getMoveLine().getPartner().getFinancialDiscount() != null
                && !invoiceTerm
                    .getDueDate()
                    .minusDays(
                        invoiceTerm
                            .getMoveLine()
                            .getPartner()
                            .getFinancialDiscount()
                            .getDiscountDelay())
                    .isAfter(nextSessionDate))) {
          return false;
        }
      }

      JPA.clear();
    }

    return true;
  }
}
