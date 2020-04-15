package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AccountingReportType;
import com.axelor.apps.account.service.AccountingReportTypeService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AccountingReportTypeController {
  public void defaultName(ActionRequest request, ActionResponse response) {
    AccountingReportType accountingReport = request.getContext().asType(AccountingReportType.class);

    try {
      Beans.get(AccountingReportTypeService.class).setDefaultName(accountingReport);
      response.setValue("name", accountingReport.getName());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
