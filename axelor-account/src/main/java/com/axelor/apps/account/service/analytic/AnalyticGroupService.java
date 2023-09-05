package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import java.util.Map;

public interface AnalyticGroupService {

  Map<String, Map<String, Object>> getAnalyticAxisDomainAttrsMap(
      AnalyticLine analyticLine, Company company) throws AxelorException;

  Map<String, Object> getAnalyticAccountValueMap(AnalyticLine analyticLine, Company company)
      throws AxelorException;
}
