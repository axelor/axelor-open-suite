package com.axelor.apps.businessproject.service.extracharges;

import com.axelor.apps.account.db.InvoiceLine;
import java.util.ArrayList;
import java.util.List;

public class InvoiceLineClassifier {
  public List<InvoiceLine> getTimesheetLines(List<InvoiceLine> lines) {
    List<InvoiceLine> result = new ArrayList<>();
    for (InvoiceLine line : lines) {
      if ("TIMESHEET".equals(line.getSourceType())) {
        result.add(line);
      }
    }
    return result;
  }

  public List<InvoiceLine> getExpenseLines(List<InvoiceLine> lines) {
    List<InvoiceLine> result = new ArrayList<>();
    for (InvoiceLine line : lines) {
      if ("EXPENSE".equals(line.getSourceType())) {
        result.add(line);
      }
    }
    return result;
  }

  public List<InvoiceLine> getSurchargeLines(List<InvoiceLine> lines) {
    List<InvoiceLine> result = new ArrayList<>();
    for (InvoiceLine line : lines) {
      if ("SURCHARGE".equals(line.getSourceType())) {
        result.add(line);
      }
    }
    return result;
  }

  public List<InvoiceLine> getOtherLines(List<InvoiceLine> lines) {
    List<InvoiceLine> result = new ArrayList<>();
    for (InvoiceLine line : lines) {
      String type = line.getSourceType();
      if (!"TIMESHEET".equals(type) && !"EXPENSE".equals(type) && !"SURCHARGE".equals(type)) {
        result.add(line);
      }
    }
    return result;
  }
}
