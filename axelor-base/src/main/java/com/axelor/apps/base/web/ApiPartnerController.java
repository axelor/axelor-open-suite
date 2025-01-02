package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class ApiPartnerController {

  @ErrorException
  public void initPartnerLineList(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();


    String apiRes = context.get("_apiResult").toString();
    String partnerId = context.get("_partnerId").toString();
  }
}
