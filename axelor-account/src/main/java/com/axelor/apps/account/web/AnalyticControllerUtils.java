package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class AnalyticControllerUtils {

  public static AnalyticLine getParentWithContext(
      ActionRequest request, ActionResponse response, AnalyticMoveLine analyticMoveLine) {
    Context parentContext = request.getContext().getParent();
    AnalyticLine parent = null;
    if (parentContext != null) {
      Class<?> parentClass = request.getContext().getParent().getContextClass();
      if (AnalyticLine.class.isAssignableFrom(parentClass)) {
        parent = request.getContext().getParent().asType(AnalyticLine.class);
      }
    } else {
      if (analyticMoveLine.getMoveLine() != null) {
        parent = analyticMoveLine.getMoveLine();
      } else if (analyticMoveLine.getInvoiceLine() != null) {
        parent = analyticMoveLine.getInvoiceLine();
      } else if (request.getContext().get("invoiceId") != null) {
        Long invoiceId = Long.valueOf((Integer) request.getContext().get("invoiceId"));
        parent = Beans.get(InvoiceLineRepository.class).find(invoiceId);
      }
    }
    return parent;
  }
}
