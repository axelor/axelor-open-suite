/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
