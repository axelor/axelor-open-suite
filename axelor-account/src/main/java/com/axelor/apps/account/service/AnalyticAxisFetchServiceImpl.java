package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.module.AccountModule;
import com.axelor.apps.base.db.Company;
import com.axelor.db.Query;
import java.util.List;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;

@Alternative
@Priority(AccountModule.PRIORITY)
public class AnalyticAxisFetchServiceImpl implements AnalyticAxisFetchService {

  @Override
  public List<AnalyticMoveLine> findByAnalyticAxisAndAnotherCompany(
      AnalyticAxis analyticAxis, Company company) {
    return Query.of(AnalyticMoveLine.class)
        .filter("self.analyticAxis = :analyticAxis AND self.account.company != :company")
        .bind("analyticAxis", analyticAxis)
        .bind("company", company)
        .fetch();
  }
}
