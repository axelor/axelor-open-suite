package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.repo.AnalyticLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import java.util.Map;

public interface AnalyticAttrsService {

  void addAnalyticAxisAttrs(
      Company company, String parentField, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;

  void addAnalyticAxisDomains(
      AnalyticLine analyticLine, Company company, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;
}
