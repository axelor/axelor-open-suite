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

import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceNote;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;

public class InvoiceNoteServiceImpl implements InvoiceNoteService {
  @Override
  public void generateInvoiceNote(Invoice invoice) {
    generateFinancialDiscountNote(invoice);
  }

  protected void generateFinancialDiscountNote(Invoice invoice) {
    FinancialDiscount discount = invoice.getFinancialDiscount();
    BigDecimal discountRate = invoice.getFinancialDiscountRate();

    if (discount != null && discountRate != null && discount.getLegalNotice() != null) {
      String noteTitle = String.format(I18n.get("Financial Discount %.2f%%"), discountRate);

      InvoiceNote invoiceNote = new InvoiceNote(noteTitle);
      invoiceNote.setType("Financial Discount");
      invoiceNote.setNote(discount.getLegalNotice());

      invoice.addInvoiceNoteListItem(invoiceNote);
    }
  }
}
