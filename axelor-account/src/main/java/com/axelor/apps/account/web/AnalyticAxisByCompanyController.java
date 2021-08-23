package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.repo.AnalyticAxisRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AnalyticAxisByCompanyController {

  public void setAxisDomain(ActionRequest request, ActionResponse response) throws AxelorException {
    try {
      AnalyticAxisByCompany analyticAxisByCompany =
          request.getContext().asType(AnalyticAxisByCompany.class);

      if (analyticAxisByCompany.getAccountConfig() != null) {
        AccountConfig config = analyticAxisByCompany.getAccountConfig();
        List<Long> idList = new ArrayList<Long>();
        for (AnalyticAxisByCompany axisByCompany : config.getAnalyticAxisByCompanyList()) {
          idList.add(axisByCompany.getAnalyticAxis().getId());
        }
        for (AnalyticAxis analyticAxis :
            Beans.get(AnalyticAxisRepository.class)
                .all()
                .filter("self.company != :company AND self.company IS NOT NULL")
                .bind("company", config.getCompany())
                .fetch()) {
          idList.add(analyticAxis.getId());
        }
        if (!ObjectUtils.isEmpty(idList)) {
          String idListStr =
              idList.stream().map(id -> id.toString()).collect(Collectors.joining(","));
          response.setAttr("analyticAxis", "domain", "self.id NOT IN (" + idListStr + ")");
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setOrderSelect(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      AccountConfig accountConfig = request.getContext().getParent().asType(AccountConfig.class);
      if (accountConfig != null) {
        Integer axisListSize = accountConfig.getAnalyticAxisByCompanyList().size();

        if (axisListSize < accountConfig.getNbrOfAnalyticAxisSelect()) {
          response.setValue("orderSelect", axisListSize + 1);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
