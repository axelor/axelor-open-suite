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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InvoiceTermDateComputeServiceImpl implements InvoiceTermDateComputeService {

  @Inject
  public InvoiceTermDateComputeServiceImpl() {}

  @Override
  public void fillWithInvoiceDueDate(Invoice invoice) {
    if (invoice == null
        || invoice.getDueDate() == null
        || ObjectUtils.isEmpty(invoice.getInvoiceTermList())) {
      return;
    }
    LocalDate dueDate = invoice.getDueDate();
    List<InvoiceTerm> invoiceTermList =
        invoice.getInvoiceTermList().stream()
            .sorted(Comparator.comparing(InvoiceTerm::getDueDate))
            .collect(Collectors.toList());

    updateWithInvoiceDueDate(invoiceTermList.get(invoiceTermList.size() - 1), dueDate);

    Set<InvoiceTerm> invoiceTermSet =
        invoice.getInvoiceTermList().stream()
            .filter(it -> it.getDueDate().isAfter(dueDate))
            .collect(Collectors.toSet());

    for (InvoiceTerm invoiceTerm : invoiceTermSet) {
      updateWithInvoiceDueDate(invoiceTerm, dueDate);
    }
  }

  protected void updateWithInvoiceDueDate(InvoiceTerm invoiceTerm, LocalDate dueDate) {
    if (invoiceTerm == null || dueDate == null) {
      return;
    }

    invoiceTerm.setDueDate(dueDate);
    invoiceTerm.setIsCustomized(true);
  }
}
