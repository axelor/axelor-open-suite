package com.axelor.apps.bankpayment.web;

import com.axelor.apps.bankpayment.db.BankReconciliation;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class BankReconciliationLineController {

  public void setAccountDomain(ActionRequest request, ActionResponse response) {

    try {
      Context parentContext = request.getContext().getParent();
      BankReconciliation bankReconciliation = null;
      if (parentContext != null
          && parentContext
              .getContextClass()
              .toString()
              .equals(BankReconciliation.class.toString())) {
        bankReconciliation = parentContext.asType(BankReconciliation.class);
      } else if (parentContext == null
          || !parentContext
              .getContextClass()
              .toString()
              .equals(BankReconciliation.class.toString())) {
        bankReconciliation = (BankReconciliation) request.getContext().get("bankReconciliation");
      }
      String domain =
          Beans.get(BankReconciliationService.class).getAccountDomain(bankReconciliation);
      response.setAttr("account", "domain", domain);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
