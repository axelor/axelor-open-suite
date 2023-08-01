package com.axelor.apps.account.web;

import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.TradingName;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.stream.Collectors;

public class TradingNameController {

  public void showAnalyticPanel(ActionRequest request, ActionResponse response) {
    try {
      TradingName tradingName = request.getContext().asType(TradingName.class);
      AccountConfigService accountConfigService = Beans.get(AccountConfigService.class);
      boolean isHidden =
          tradingName.getCompanySet().stream()
                  .filter(
                      c -> {
                        try {
                          return accountConfigService
                                  .getAccountConfig(c)
                                  .getAnalyticDistributionTypeSelect()
                              == 4;
                        } catch (AxelorException e) {
                          return false;
                        }
                      })
                  .count()
              == 0;
      response.setAttr("analyticDistributionPanel", "hidden", isHidden);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void emptyAnalyticDistributionTemplate(ActionRequest request, ActionResponse response) {
    try {
      TradingName tradingName = request.getContext().asType(TradingName.class);
      AccountConfigService accountConfigService = Beans.get(AccountConfigService.class);
      if (tradingName.getAnalyticDistributionTemplate() == null) {
        return;
      }
      boolean emptyTemplate =
          tradingName.getCompanySet().stream()
                  .filter(
                      c -> {
                        try {
                          return accountConfigService
                                  .getAccountConfig(c)
                                  .getAnalyticDistributionTypeSelect()
                              == 4;
                        } catch (AxelorException e) {
                          return false;
                        }
                      })
                  .count()
              == 0;

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
      AccountConfigService accountConfigService = Beans.get(AccountConfigService.class);
      String companyIds =
          tradingName.getCompanySet().stream()
              .map(Company::getId)
              .map(Object::toString)
              .collect(Collectors.joining(","))
              .toString();
      String domain =
          !ObjectUtils.isEmpty(companyIds)
              ? String.format("self.company.id in (%s) AND self.isSpecific = false", companyIds)
              : "self.id = 0";
      response.setAttr("analyticDistributionTemplate", "domain", domain);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
