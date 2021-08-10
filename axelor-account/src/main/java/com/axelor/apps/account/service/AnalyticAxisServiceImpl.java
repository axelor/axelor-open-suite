package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.repo.AnalyticMoveLineMngtRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;

public class AnalyticAxisServiceImpl implements AnalyticAxisService {

  protected AnalyticMoveLineMngtRepository analyticMoveLineManagementRepository;

  @Inject
  public AnalyticAxisServiceImpl(
      AnalyticMoveLineMngtRepository analyticMoveLineManagementRepository) {
    this.analyticMoveLineManagementRepository = analyticMoveLineManagementRepository;
  }

  @Override
  public boolean checkCompanyOnMoveLine(AnalyticAxis analyticAxis) {
    if (analyticAxis != null && analyticAxis.getCompany() != null) {
      if (ObjectUtils.isEmpty(analyticMoveLineManagementRepository.findByAnalyticAxisAndAnotherCompany(
              analyticAxis, analyticAxis.getCompany()))) {
        return true;
      }
    }
    return false;
  }
}
