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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceNote;
import com.axelor.apps.account.db.InvoiceProductStatement;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.Iterator;

public class InvoiceNoteServiceImpl implements InvoiceNoteService {

  // TODO: Define proper invoiceNoteType constant once the type values are finalized
  protected static final String INVOICE_CATEGORY_NOTE_TYPE = "InvoiceCategory";

  protected AccountConfigService accountConfigService;

  @Inject
  public InvoiceNoteServiceImpl(AccountConfigService accountConfigService) {
    this.accountConfigService = accountConfigService;
  }

  @Override
  public void generateInvoiceNote(Invoice invoice) {
    generateFinancialDiscountNote(invoice);
  }

  @Override
  public void generateInvoiceCategoryNote(Invoice invoice) throws AxelorException {
    clearExistingCategoryNotes(invoice);

    AccountConfig accountConfig = accountConfigService.getAccountConfig(invoice.getCompany());
    if (!accountConfig.getDisplayItemsCategoriesOnPrinting()) {
      return;
    }

    String invoiceCategory = invoice.getInvoiceCategorySelect();
    if (Strings.isNullOrEmpty(invoiceCategory)) {
      return;
    }

    accountConfig.getStatementsForItemsCategoriesList().stream()
        .filter(s -> invoiceCategory.equals(s.getTypeList()))
        .findFirst()
        .map(InvoiceProductStatement::getStatement)
        .ifPresent(
            statement -> {
              // TODO: Set proper name and type once invoiceNoteType values are defined
              // and using correct new invoiceNoteType object
              InvoiceNote invoiceNote = new InvoiceNote();
              invoiceNote.setType(INVOICE_CATEGORY_NOTE_TYPE);
              invoiceNote.setName(INVOICE_CATEGORY_NOTE_TYPE);
              invoiceNote.setNote(statement);
              invoice.addInvoiceNoteListItem(invoiceNote);
            });
  }

  protected void clearExistingCategoryNotes(Invoice invoice) {
    if (invoice.getInvoiceNoteList() == null) {
      return;
    }
    Iterator<InvoiceNote> iterator = invoice.getInvoiceNoteList().iterator();
    while (iterator.hasNext()) {
      InvoiceNote note = iterator.next();
      if (INVOICE_CATEGORY_NOTE_TYPE.equals(note.getType())) {
        iterator.remove();
      }
    }
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
