package com.axelor.apps.account.web;

import com.axelor.apps.account.service.analytic.TradingNameAnalyticServiceImpl;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class TradingNameController {

  public void showAnalyticPanel(ActionRequest request, ActionResponse response) {
    try {
      TradingName tradingName = request.getContext().asType(TradingName.class);
      boolean emptyTemplate =
          Beans.get(TradingNameAnalyticServiceImpl.class).isAnalyticTypeByTradingName(tradingName);
      response.setAttr("analyticDistributionPanel", "hidden", emptyTemplate);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void emptyAnalyticDistributionTemplate(ActionRequest request, ActionResponse response) {
    try {
      TradingName tradingName = request.getContext().asType(TradingName.class);
      if (tradingName.getAnalyticDistributionTemplate() == null) {
        return;
      }
      boolean emptyTemplate =
          Beans.get(TradingNameAnalyticServiceImpl.class).isAnalyticTypeByTradingName(tradingName);

      if (emptyTemplate) {
        response.setValue("analyticDistributionTemplate", null);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAnalyticDistributionTemplate(ActionRequest request, ActionResponse response) {
    try {
      TradingName tradingName = request.getContext().asType(TradingName.class);

      response.setAttr(
          "analyticDistributionTemplate",
          "domain",
          Beans.get(TradingNameAnalyticServiceImpl.class).getDomainOnCompany(tradingName));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
