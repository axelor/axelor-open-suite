package com.axelor.apps.bankpayment.web;

import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.service.bankstatement.file.afb120.BankStatementLineAFB120Service;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import java.time.LocalDate;

public class BankStatementLineAFB120Controller {

  protected BankStatementLineAFB120Service bankStatementLineAFB120Service;

  @Inject
  public BankStatementLineAFB120Controller(
      BankStatementLineAFB120Service bankStatementLineAFB120Service) {
    this.bankStatementLineAFB120Service = bankStatementLineAFB120Service;
  }

  public void fillBankDetails(ActionRequest request, ActionResponse response)
      throws AxelorException {
    BankDetails bankDetails;
    if (ObjectUtils.notEmpty(AuthUtils.getUser().getActiveCompany())) {
      if (ObjectUtils.notEmpty(AuthUtils.getUser().getActiveCompany().getBankDetailsList())) {
        bankDetails = AuthUtils.getUser().getActiveCompany().getBankDetailsList().get(0);
        response.setValue("bankDetails", bankDetails);
      }
    }
  }

  public void print(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    LocalDate fromDate = LocalDate.parse((String) context.get("fromDate"));
    LocalDate toDate = LocalDate.parse((String) context.get("toDate"));
    String extention = (String) context.get("");
    BankDetails bankDetails = (BankDetails) context.get("bankDetails");
    BankStatementLineAFB120 initialLine =
        bankStatementLineAFB120Service.getFirstInitialLineBetweenDate(
            fromDate, toDate, bankDetails);
    BankStatementLineAFB120 finalLine =
        bankStatementLineAFB120Service.getLastFinalLineBetweenDate(fromDate, toDate, bankDetails);
    if (ObjectUtils.isEmpty(initialLine)) {
      response.setError("No initial balance line between these dates");
    } else {
      if (ObjectUtils.isEmpty(finalLine)) {
        response.setError("No final balance line between these dates");
      } else {
        fromDate = initialLine.getOperationDate();
        toDate = finalLine.getOperationDate();
        String fileLink =
            bankStatementLineAFB120Service.print(
                initialLine, finalLine, fromDate, toDate, bankDetails, extention);
        String name = "";
        response.setView(ActionView.define(name).add("html", fileLink).map());
        response.setReload(true);
      }
    }
  }
}
