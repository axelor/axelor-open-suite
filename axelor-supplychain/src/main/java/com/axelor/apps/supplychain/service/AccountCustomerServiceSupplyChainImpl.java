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
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.service.AccountCustomerServiceImpl;
import com.axelor.apps.account.service.AccountingSituationInitService;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class AccountCustomerServiceSupplyChainImpl extends AccountCustomerServiceImpl {

  @Inject
  public AccountCustomerServiceSupplyChainImpl(
      AccountingSituationService accountingSituationService,
      AccountingSituationInitService accountingSituationInitService,
      AccountingSituationRepository accSituationRepo,
      AppBaseService appBaseService) {
    super(
        accountingSituationService,
        accountingSituationInitService,
        accSituationRepo,
        appBaseService);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public AccountingSituation updateAccountingSituationCustomerAccount(
      AccountingSituation accountingSituation,
      boolean updateCustAccount,
      boolean updateDueCustAccount,
      boolean updateDueDebtRecoveryCustAccount)
      throws AxelorException {

    accountingSituation =
        super.updateAccountingSituationCustomerAccount(
            accountingSituation,
            updateCustAccount,
            updateDueCustAccount,
            updateDueDebtRecoveryCustAccount);

    if (updateCustAccount && appBaseService.isApp("supplychain")) {
      accountingSituationService.updateCustomerCredit(accountingSituation.getPartner());
    }

    return accountingSituation;
  }
}
