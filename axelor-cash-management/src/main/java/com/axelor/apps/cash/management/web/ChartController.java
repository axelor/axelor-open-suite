package com.axelor.apps.cash.management.web;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.cash.management.service.CashManagementChartService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;
import java.util.Map;

public class ChartController {

  @SuppressWarnings("unchecked")
  public void getCashBalanceData(ActionRequest request, ActionResponse response) {
    try {
      User user = null;
      BankDetails bankDetails = null;
      if (request.getContext().get("_getAllUserData") == null) {
        user = AuthUtils.getUser();
      }
      Map<String, Object> map = (Map<String, Object>) request.getContext().get("bankDetails");
      if (map != null) {
        bankDetails = Mapper.toBean(BankDetails.class, map);
      }
      List<Map<String, Object>> dataList =
          Beans.get(CashManagementChartService.class).getCashBalanceData(user, bankDetails);
      response.setData(dataList);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }
}
