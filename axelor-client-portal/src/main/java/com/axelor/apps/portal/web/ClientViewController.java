package com.axelor.apps.portal.web;

import com.axelor.apps.portal.service.ClientViewInterface;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.HashMap;
import java.util.Map;

public class ClientViewController {
  public void completeClientViewIndicators(ActionRequest request, ActionResponse response) {
    Map<String, Object> map = new HashMap<>();
    Context context = request.getContext();
    map = Beans.get(ClientViewInterface.class).updateClientViewIndicators();
    response.setValues(map);
  }
}
