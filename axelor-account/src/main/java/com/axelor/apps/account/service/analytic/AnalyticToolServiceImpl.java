package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import javax.inject.Inject;

public class AnalyticToolServiceImpl implements AnalyticToolService {

  protected AppAccountService appAccountService;
  protected AccountConfigService accountConfigService;

  @Inject
  public AnalyticToolServiceImpl(
      AppAccountService appAccountService, AccountConfigService accountConfigService) {
    this.appAccountService = appAccountService;
    this.accountConfigService = accountConfigService;
  }

  @Override
  public boolean isManageAnalytic(Company company) throws AxelorException {
    return appAccountService.getAppAccount().getManageAnalyticAccounting()
        && company != null
        && accountConfigService.getAccountConfig(company).getManageAnalyticAccounting();
  }
}
