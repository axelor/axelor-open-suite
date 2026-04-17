package com.axelor.apps.businessproject.service.invoice.breakdown.classify;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.businessproject.service.invoice.breakdown.MaterialGroup;
import java.util.List;

/** Holds invoice lines classified into their breakdown sections. */
public class InvoiceLineClassification {

  private final List<InvoiceLine> timesheetLines;
  private final List<InvoiceLine> extraChargeLines;
  private final List<InvoiceLine> expenseLines;
  private final List<InvoiceLine> extraExpenseLines;
  private final List<MaterialGroup> materialGroups;

  public InvoiceLineClassification(
      List<InvoiceLine> timesheetLines,
      List<InvoiceLine> extraChargeLines,
      List<InvoiceLine> expenseLines,
      List<InvoiceLine> extraExpenseLines,
      List<MaterialGroup> materialGroups) {
    this.timesheetLines = timesheetLines;
    this.extraChargeLines = extraChargeLines;
    this.expenseLines = expenseLines;
    this.extraExpenseLines = extraExpenseLines;
    this.materialGroups = materialGroups;
  }

  public List<InvoiceLine> getTimesheetLines() {
    return timesheetLines;
  }

  public List<InvoiceLine> getExtraChargeLines() {
    return extraChargeLines;
  }

  public List<InvoiceLine> getExpenseLines() {
    return expenseLines;
  }

  public List<InvoiceLine> getExtraExpenseLines() {
    return extraExpenseLines;
  }

  public List<MaterialGroup> getMaterialGroups() {
    return materialGroups;
  }

  public boolean isEmpty() {
    return timesheetLines.isEmpty()
        && extraChargeLines.isEmpty()
        && expenseLines.isEmpty()
        && extraExpenseLines.isEmpty()
        && materialGroups.isEmpty();
  }
}
