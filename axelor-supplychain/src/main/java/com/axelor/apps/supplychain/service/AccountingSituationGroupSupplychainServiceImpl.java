/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationAttrsService;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationGroupServiceImpl;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationRecordService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.studio.db.AppAccount;
import com.google.inject.Inject;
import java.util.Map;

public class AccountingSituationGroupSupplychainServiceImpl
    extends AccountingSituationGroupServiceImpl {

  protected AppSupplychainService appSupplychainService;
  protected AppAccountService appAccountService;

  @Inject
  public AccountingSituationGroupSupplychainServiceImpl(
      AccountingSituationAttrsService accountingSituationAttrsService,
      AccountingSituationRecordService accountingSituationRecordService,
      AccountingSituationSupplychainService accountingSituationService,
      AccountConfigService accountConfigService,
      AppSupplychainService appSupplychainService,
      AppAccountService appAccountService) {
    super(
        accountingSituationAttrsService,
        accountingSituationRecordService,
        accountingSituationService,
        accountConfigService);
    this.appSupplychainService = appSupplychainService;
    this.appAccountService = appAccountService;
  }

  @Override
  public Map<String, Object> getCompanyOnChangeValuesMap(
      AccountingSituation accountingSituation, Partner partner) throws AxelorException {
    Map<String, Object> valuesMap = super.getCompanyOnChangeValuesMap(accountingSituation, partner);

    if (appSupplychainService.isApp("supplychain")) {
      AppAccount appAccount = appAccountService.getAppAccount();
      if (appAccount != null && appAccount.getManageCustomerCredit()) {
        accountingSituation =
            ((AccountingSituationSupplychainService) accountingSituationService)
                .computeUsedCredit(accountingSituation);

        valuesMap.put("usedCredit", accountingSituation.getUsedCredit());
      }
    }

    return valuesMap;
  }
}
