package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.google.inject.persist.Transactional;
import javax.inject.Inject;

public class AnalyticAccountServiceImpl implements AnalyticAccountService {

  protected AnalyticAccountRepository analyticAccountRepository;

  @Inject
  public AnalyticAccountServiceImpl(AnalyticAccountRepository analyticAccountRepository) {
    this.analyticAccountRepository = analyticAccountRepository;
  }

  @Override
  @Transactional
  public void toggleStatusSelect(AnalyticAccount analyticAccount) {
    if (analyticAccount != null) {
      if (analyticAccount.getStatusSelect() == AnalyticAccountRepository.STATUS_INACTIVE) {
        analyticAccount = activate(analyticAccount);
      } else {
        analyticAccount = desactivate(analyticAccount);
      }
      analyticAccountRepository.save(analyticAccount);
    }
  }

  protected AnalyticAccount activate(AnalyticAccount analyticAccount) {
    analyticAccount.setStatusSelect(AnalyticAccountRepository.STATUS_ACTIVE);
    return analyticAccount;
  }

  protected AnalyticAccount desactivate(AnalyticAccount analyticAccount) {
    analyticAccount.setStatusSelect(AnalyticAccountRepository.STATUS_INACTIVE);
    return analyticAccount;
  }
}
