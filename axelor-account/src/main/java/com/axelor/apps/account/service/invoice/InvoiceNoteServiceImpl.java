package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceNote;
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
      String noteTitle = String.format("Financial Discount %.2f%%", discountRate);

      InvoiceNote invoiceNote = new InvoiceNote(noteTitle);
      invoiceNote.setType("Financial Discount");
      invoiceNote.setNote(discount.getLegalNotice());

      invoice.addInvoiceNoteListItem(invoiceNote);
    }
  }
}
