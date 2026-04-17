package com.axelor.apps.businessproject.service.invoice.breakdown.classify;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.businessproject.service.extracharges.ExtraChargeConstants;
import com.axelor.apps.businessproject.service.invoice.breakdown.MaterialGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvoiceLineClassifier {

  private static final Logger log = LoggerFactory.getLogger(InvoiceLineClassifier.class);

  /**
   * Classifies Invoice lines, based on their various source types.
   *
   * @param invoiceLines
   * @return
   */
  @Nullable
  public InvoiceLineClassification classify(List<InvoiceLine> invoiceLines) {
    if (invoiceLines == null || invoiceLines.isEmpty()) {
      return null;
    }

    List<InvoiceLine> cFeeLines =
        getBySourceType(
            invoiceLines, ExtraChargeConstants.EXPENSE_CHARGED_FEE_INVOICE_LINE_SOURCE_TYPE);
    List<InvoiceLine> timesheetLines =
        getBySourceType(invoiceLines, ExtraChargeConstants.TIMESHEET_INVOICE_LINE_SOURCE_TYPE);
    List<InvoiceLine> extraChargeLines =
        getBySourceType(invoiceLines, ExtraChargeConstants.EXTRA_CHARGE_INVOICE_LINE_SOURCE_TYPE);
    List<InvoiceLine> expenseLines =
        getBySourceType(invoiceLines, ExtraChargeConstants.EXPENSE_INVOICE_LINE_SOURCE_TYPE);
    List<InvoiceLine> extraExpenseLines =
        getBySourceType(invoiceLines, ExtraChargeConstants.EXTRA_EXPENSE_INVOICE_LINE_SOURCE_TYPE);

    List<MaterialGroup> materialGroups = buildMaterialGroup(expenseLines, cFeeLines);

    return new InvoiceLineClassification(
        timesheetLines, extraChargeLines, expenseLines, extraExpenseLines, materialGroups);
  }

  private List<InvoiceLine> getBySourceType(List<InvoiceLine> lines, String sourceType) {
    return lines.stream()
        .filter(line -> sourceType.equals(line.getSourceType()))
        .collect(Collectors.toList());
  }

  private List<InvoiceLine> getCFeeLInes(List<InvoiceLine> lines) {
    return lines.stream()
        .filter(
            line ->
                ExtraChargeConstants.EXPENSE_CHARGED_FEE_INVOICE_LINE_SOURCE_TYPE.equals(
                    line.getSourceType()))
        .collect(Collectors.toList());
  }

  private List<MaterialGroup> buildMaterialGroup(
      List<InvoiceLine> expenseLines, List<InvoiceLine> cFeeLines) {
    List<MaterialGroup> groups = new ArrayList<>();
    for (InvoiceLine expenseLine : expenseLines) {
      InvoiceLine matchedFee = findMatchingFee(expenseLine, cFeeLines);
      groups.add(new MaterialGroup(expenseLine, matchedFee));
    }
    return groups;
  }

  @Nullable
  private InvoiceLine findMatchingFee(InvoiceLine expenseLine, List<InvoiceLine> cFeeLines) {
    if (expenseLine.getSourceIds() == null) {
      return null;
    }

    return cFeeLines.stream()
        .filter(fee -> expenseLine.getSourceIds().equals(fee.getSourceIds()))
        .findFirst()
        .orElse(null);
  }
}
