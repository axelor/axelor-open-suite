package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.repo.AnalyticAxisRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AnalyticAxisByCompanyServiceImpl implements AnalyticAxisByCompanyService {

  protected AnalyticAxisRepository analyticAxisRepository;

  @Inject
  public AnalyticAxisByCompanyServiceImpl(AnalyticAxisRepository analyticAxisRepository) {
    this.analyticAxisRepository = analyticAxisRepository;
  }

  @Override
  public String getAxisDomain(AccountConfig accountConfig) {
    if (accountConfig != null) {
      List<Long> idList = new ArrayList<Long>();
      for (AnalyticAxisByCompany axisByCompany : accountConfig.getAnalyticAxisByCompanyList()) {
        idList.add(axisByCompany.getAnalyticAxis().getId());
      }
      for (AnalyticAxis analyticAxis :
          analyticAxisRepository
              .all()
              .filter("self.company != :company AND self.company IS NOT NULL")
              .bind("company", accountConfig.getCompany())
              .fetch()) {
        idList.add(analyticAxis.getId());
      }
      if (!ObjectUtils.isEmpty(idList)) {
        String idListStr =
            idList.stream().map(id -> id.toString()).collect(Collectors.joining(","));
        return "self.id NOT IN (" + idListStr + ")";
      }
    }
    return null;
  }
}
