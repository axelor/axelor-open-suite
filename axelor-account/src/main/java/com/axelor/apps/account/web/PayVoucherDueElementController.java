package com.axelor.apps.account.web;

import com.axelor.apps.account.db.PayVoucherDueElement;
import com.axelor.apps.account.service.payment.paymentvoucher.PayVoucherDueElementService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PayVoucherDueElementController {

  public void updateAmounts(ActionRequest request, ActionResponse response) {
    try {
      PayVoucherDueElement payVoucherDueElement =
          request.getContext().asType(PayVoucherDueElement.class);

      payVoucherDueElement =
          Beans.get(PayVoucherDueElementService.class).updateAmounts(payVoucherDueElement);
      response.setValues(payVoucherDueElement);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
