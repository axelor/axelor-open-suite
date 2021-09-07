package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.base.db.Company;
import java.util.List;

public interface AnalyticAxisFetchService {

  List<AnalyticMoveLine> findByAnalyticAxisAndAnotherCompany(
      AnalyticAxis analyticAxis, Company company);
}
