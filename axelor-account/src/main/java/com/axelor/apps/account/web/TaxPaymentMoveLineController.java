package com.axelor.apps.account.web;

import com.axelor.apps.account.db.TaxPaymentMoveLine;
import com.axelor.apps.account.service.TaxPaymentMoveLineService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class TaxPaymentMoveLineController {

  public void computeTaxAmount(ActionRequest request, ActionResponse response) {
    try {
      TaxPaymentMoveLine taxPaymentMoveLine = request.getContext().asType(TaxPaymentMoveLine.class);
      taxPaymentMoveLine =
          Beans.get(TaxPaymentMoveLineService.class).computeTaxAmount(taxPaymentMoveLine);
      response.setValue("taxAmount", taxPaymentMoveLine.getTaxAmount());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
