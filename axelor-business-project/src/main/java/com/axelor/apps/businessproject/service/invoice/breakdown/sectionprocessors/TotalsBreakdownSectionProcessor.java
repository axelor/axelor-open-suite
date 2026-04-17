package com.axelor.apps.businessproject.service.invoice.breakdown.sectionprocessors;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.businessproject.service.invoice.breakdown.BreakdownDisplayLine;
import com.axelor.apps.businessproject.service.invoice.breakdown.SequenceCounter;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders the invoice totals block.
 *
 * <p>This processor does not implement InvoiceBreakdownSectionProcessor because it operates on the
 * Invoice directly rather than on classified lines.
 */
public class TotalsBreakdownSectionProcessor {

  public List<BreakdownDisplayLine> process(Invoice invoice, SequenceCounter sequence) {
    List<BreakdownDisplayLine> lines = new ArrayList<>();

    // TODO: Filter out lines which are not meant to be seen on the invoice breakdown for this
    // TODO: totals to be accurate

    // Total Amount Net
    if (invoice.getExTaxTotal() != null) {
      lines.add(
          BreakdownDisplayLine.total(
              I18n.get("Total Amount Net"),
              invoice.getExTaxTotal(),
              BreakdownDisplayLine.SectionType.TOTALS));
    }

    // VAT
    if (invoice.getTaxTotal() != null && invoice.getTaxTotal().compareTo(BigDecimal.ZERO) > 0) {

      BigDecimal vatPercent = calculateVatPercentage(invoice);
      String vatLabel = I18n.get("VAT") + " (" + String.format("%.0f%%", vatPercent) + ")";

      String currency = "EUR";
      if (invoice.getCurrency() != null && invoice.getCurrency().getCodeISO() != null) {
        currency = invoice.getCurrency().getCodeISO();
      }

      lines.add(
          BreakdownDisplayLine.regular(
              sequence.next(),
              vatLabel,
              BigDecimal.ONE,
              currency,
              null,
              invoice.getTaxTotal(),
              null,
              null,
              BreakdownDisplayLine.SectionType.TOTALS));
    }

    // Total Amount Gross
    lines.add(
        BreakdownDisplayLine.total(
            I18n.get("Total Amount Gross"),
            invoice.getInTaxTotal(),
            BreakdownDisplayLine.SectionType.TOTALS));

    return lines;
  }

  // TODO: The Invoice has to have an already defined way to do this. Find it and reuse that
  private BigDecimal calculateVatPercentage(Invoice invoice) {
    if (invoice.getExTaxTotal() != null && invoice.getExTaxTotal().compareTo(BigDecimal.ZERO) > 0) {
      return invoice
          .getTaxTotal()
          .divide(invoice.getExTaxTotal(), 4, RoundingMode.HALF_UP)
          .multiply(BigDecimal.valueOf(100));
    }
    return BigDecimal.valueOf(19);
  }
}
