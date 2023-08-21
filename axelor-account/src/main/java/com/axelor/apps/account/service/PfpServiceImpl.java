package com.axelor.apps.account.service;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.google.inject.Inject;

public class PfpServiceImpl implements PfpService {

  protected AppAccountService appAccountService;
  protected AccountConfigService accountConfigService;

  @Inject
  public PfpServiceImpl(
      AppAccountService appAccountService, AccountConfigService accountConfigService) {
    this.appAccountService = appAccountService;
    this.accountConfigService = accountConfigService;
  }

  @Override
  public boolean isManagePassedForPayment(Company company) throws AxelorException {
    return company != null
        && appAccountService.getAppAccount() != null
        && appAccountService.getAppAccount().getActivatePassedForPayment()
        && accountConfigService.getAccountConfig(company).getIsManagePassedForPayment();
  }
}
