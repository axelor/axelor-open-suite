package com.axelor.apps.businessproject.service.invoice.breakdown.sectionprocessors;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.businessproject.service.invoice.breakdown.BreakdownDisplayLine;
import com.axelor.apps.businessproject.service.invoice.breakdown.MaterialGroup;
import com.axelor.apps.businessproject.service.invoice.breakdown.SequenceCounter;
import com.axelor.apps.businessproject.service.invoice.breakdown.classify.InvoiceLineClassification;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Renders the expense section of breakdown.
 *
 * <p>Each expense line is rendered first, immediately followed by its charged fee line if one
 * exists. The section closes with a Material Cost Total = sum of all expense lines + all fee lines.
 */
public class ExpenseBreakdownSectionProcessor implements InvoiceBreakdownSectionProcessor {

  @Override
  public boolean supports(InvoiceLineClassification classification) {
    return classification != null
        && classification.getMaterialGroups() != null
        && !classification.getMaterialGroups().isEmpty();
  }

  @Override
  public BreakdownDisplayLine.SectionType getSectionType() {
    return BreakdownDisplayLine.SectionType.MATERIAL;
  }

  @Override
  public List<BreakdownDisplayLine> process(
      InvoiceLineClassification classification, SequenceCounter sequence) {
    List<BreakdownDisplayLine> lines = new ArrayList<>();
    BigDecimal sectionTotal = BigDecimal.ZERO;

    for (MaterialGroup group : classification.getMaterialGroups()) {
      InvoiceLine expenseLine = group.getMaterialLine();

      // Expense line
      lines.add(
          BreakdownDisplayLine.regular(
              sequence.next(),
              expenseLine.getProductName(),
              expenseLine.getQty(),
              expenseLine.getUnit() != null ? expenseLine.getUnit().getName() : "--",
              expenseLine.getPrice(),
              expenseLine.getExTaxTotal(),
              buildBillingDetails(expenseLine),
              null,
              getSectionType()));

      sectionTotal = sectionTotal.add(orZero(expenseLine.getExTaxTotal()));

      // Expense generated fee line plced directly below if present
      if (group.hasFee()) {
        InvoiceLine feeLine = group.getFeeLine();
        BigDecimal percentage =
            orZero(feeLine.getQty())
                .multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP);

        String billingDetails =
            String.format(
                I18n.get("Added fee of %.2f%% on %s"), percentage, expenseLine.getProductName());

        lines.add(
            BreakdownDisplayLine.regular(
                sequence.next(),
                feeLine.getProductName(),
                percentage,
                "%",
                feeLine.getPrice(),
                feeLine.getExTaxTotal(),
                billingDetails,
                null,
                getSectionType()));

        sectionTotal = sectionTotal.add(orZero(feeLine.getExTaxTotal()));
      }
    }

    lines.add(
        BreakdownDisplayLine.total(
            I18n.get("Material Cost Total"), sectionTotal, getSectionType()));

    return lines;
  }

  // TODO: Extract this to a helper as many processors will need to build something similar
  private String buildBillingDetails(InvoiceLine line) {
    if (line.getUnit() == null) return null;
    return String.format(I18n.get("Biled for %.2f %s(s)"), line.getQty(), line.getUnit().getName());
  }

  private BigDecimal orZero(BigDecimal value) {
    return value != null ? value : BigDecimal.ZERO;
  }
}
