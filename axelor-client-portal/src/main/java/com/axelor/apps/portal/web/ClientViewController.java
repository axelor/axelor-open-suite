package com.axelor.apps.portal.web;

import com.axelor.apps.portal.service.ClientViewInterface;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.HashMap;
import java.util.Map;

public class ClientViewController {
  static final String TITLE_COMPANY_TICKETS = /*$$(*/ "Tickets %s" /*)*/;

  public void completeClientViewIndicators(ActionRequest request, ActionResponse response) {
    Map<String, Object> map = new HashMap<>();
    map = Beans.get(ClientViewInterface.class).updateClientViewIndicators();

    response.setValues(map);
  }
}
