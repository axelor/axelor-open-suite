package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.service.AnalyticAxisService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AnalyticAxisController {

  public void checkCompanyOnMoveLine(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {

      AnalyticAxis analyticAxis = request.getContext().asType(AnalyticAxis.class);

      if (analyticAxis.getCompany() != null) {
        if (Beans.get(AnalyticAxisService.class).checkCompanyOnMoveLine(analyticAxis)) {
          response.setError(
              I18n.get(
                  "This axis already contains Analytic Move Lines attached to several companies. Please make sure to correctly reassign the analytic move lines currently attached to this axis to another axis before being able to assign other."));
          response.setValue("company", null);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setGroupingDomain(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      AnalyticAxis analyticAxis = request.getContext().asType(AnalyticAxis.class);

      List<Long> idList = new ArrayList<Long>();
      for (int i = 1; i <= 10; i++) {
        idList.add(Beans.get(AnalyticAxisService.class).getAnalyticGroupingId(analyticAxis, i));
      }

      if (!ObjectUtils.isEmpty(idList)) {
        String idListStr =
            idList.stream().map(id -> id.toString()).collect(Collectors.joining(","));
        for (int i = 1; i <= 10; i++) {
          response.setAttr(
              "analyticGrouping" + i,
              "domain",
              "self.id NOT IN ("
                  + idListStr
                  + ") AND self.analyticAxis.id = "
                  + analyticAxis.getId());
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
