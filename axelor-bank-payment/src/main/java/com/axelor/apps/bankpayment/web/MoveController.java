package com.axelor.apps.bankpayment.web;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.bankpayment.service.move.MoveServiceBankPayment;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class MoveController {

  @ErrorException
  public void checkMovePartnerBankDetails(ActionRequest request, ActionResponse response) {
    Move move = request.getContext().asType(Move.class);
    BankDetails partnerBankDetails =
        Beans.get(MoveServiceBankPayment.class).checkMovePartnerBankDetails(move);
    response.setValue("partnerBankDetails", partnerBankDetails);
  }
}
