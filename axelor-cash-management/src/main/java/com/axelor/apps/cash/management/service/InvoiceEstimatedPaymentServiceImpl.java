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
package com.axelor.apps.cash.management.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import java.time.LocalDate;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceEstimatedPaymentServiceImpl implements InvoiceEstimatedPaymentService {

  @Override
  public Invoice computeEstimatedPaymentDate(Invoice invoice) {
    if (CollectionUtils.isEmpty(invoice.getInvoiceTermList())) {
      return invoice;
    }
    if (invoice.getPartner() != null && invoice.getPartner().getPaymentDelay() != null) {

      int paymentDelay = invoice.getPartner().getPaymentDelay().intValue();

      for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
        if (invoiceTerm.getEstimatedPaymentDate() == null) {
          invoiceTerm.setEstimatedPaymentDate(invoiceTerm.getDueDate().plusDays(paymentDelay));
        }
      }
    } else {
      for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
        if (invoiceTerm.getEstimatedPaymentDate() != null) {
          continue;
        }

        LocalDate estimatedPaymentDate = invoiceTerm.getDueDate();

        invoiceTerm.setEstimatedPaymentDate(estimatedPaymentDate);
      }
    }
    return invoice;
  }
}
