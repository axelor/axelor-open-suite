package com.axelor.apps.businessproject.service.extracharges;

import com.axelor.apps.account.db.InvoiceLine;
import java.util.ArrayList;
import java.util.List;

public class InvoiceLineClassifier {
  public List<InvoiceLine> getTimesheetLines(List<InvoiceLine> lines) {
    List<InvoiceLine> result = new ArrayList<>();
    for (InvoiceLine line : lines) {
      if (ExtraChargeConstants.TIMESHEET_INVOICE_LINE_SOURCE_TYPE.equals(line.getSourceType())) {
        result.add(line);
      }
    }
    return result;
  }

  public List<InvoiceLine> getExpenseLines(List<InvoiceLine> lines) {
    List<InvoiceLine> result = new ArrayList<>();
    for (InvoiceLine line : lines) {
      if (ExtraChargeConstants.EXPENSE_INVOICE_LINE_SOURCE_TYPE.equals(line.getSourceType())) {
        result.add(line);
      }
    }
    return result;
  }

  public List<InvoiceLine> getExtraChargeLines(List<InvoiceLine> lines) {
    List<InvoiceLine> result = new ArrayList<>();
    for (InvoiceLine line : lines) {
      if (ExtraChargeConstants.EXTRACHARGE_INVOICE_LINE_SOURCE_TYPE.equals(line.getSourceType())) {
        result.add(line);
      }
    }
    return result;
  }

  public List<InvoiceLine> getOtherLines(List<InvoiceLine> lines) {
    List<InvoiceLine> result = new ArrayList<>();
    for (InvoiceLine line : lines) {
      String type = line.getSourceType();
      if (ExtraChargeConstants.INVOICE_LINE_SOURCE_TYPES.contains(type)) {
        result.add(line);
      }
    }
    return result;
  }
}
