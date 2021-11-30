package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;

public class AnalyticAccountServiceImpl implements AnalyticAccountService {

  public AnalyticAccountServiceImpl() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public int toggleStatusSelect(AnalyticAccount analyticAccount) {
    if (analyticAccount != null) {
      if (analyticAccount.getStatusSelect() == AnalyticAccountRepository.STATUS_INACTIVE) {
        return AnalyticAccountRepository.STATUS_ACTIVE;
      } else {
        return AnalyticAccountRepository.STATUS_INACTIVE;
      }
    }
    return -1;
  }
}
