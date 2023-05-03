package com.axelor.apps.bankpayment.web;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.bankpayment.service.moveline.MoveLineGroupBankPaymentService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class MoveLineController {
  public void bankReconciledAmountOnChange(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);

      response.setValues(
          Beans.get(MoveLineGroupBankPaymentService.class)
              .getBankReconciledAmountOnChangeValuesMap(moveLine));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setSelectedBankReconciliation(ActionRequest request, ActionResponse response) {
    try {
      MoveLine moveLine = request.getContext().asType(MoveLine.class);

      Beans.get(MoveLineService.class).setIsSelectedBankReconciliation(moveLine);

      response.setValue("isSelectedBankReconciliation", moveLine.getIsSelectedBankReconciliation());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
