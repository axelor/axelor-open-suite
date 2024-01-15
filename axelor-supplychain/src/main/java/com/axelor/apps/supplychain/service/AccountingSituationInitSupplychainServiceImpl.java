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
import com.axelor.apps.account.service.AccountingSituationInitServiceImpl;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.google.inject.Inject;

public class AccountingSituationInitSupplychainServiceImpl
    extends AccountingSituationInitServiceImpl {

  protected AppAccountService appAccountService;
  protected SaleConfigService saleConfigService;

  @Inject
  public AccountingSituationInitSupplychainServiceImpl(
      SequenceService sequenceService,
      AccountingSituationRepository accountingSituationRepository,
      PaymentModeService paymentModeService,
      AccountConfigService accountConfigService,
      AppAccountService appAccountService,
      SaleConfigService saleConfigService) {
    super(sequenceService, accountingSituationRepository, paymentModeService, accountConfigService);
    this.appAccountService = appAccountService;
    this.saleConfigService = saleConfigService;
  }

  @Override
  public AccountingSituation createAccountingSituation(Partner partner, Company company)
      throws AxelorException {

    AccountingSituation accountingSituation = super.createAccountingSituation(partner, company);

    if (partner.getIsCustomer()
        && appAccountService.getAppAccount().getManageCustomerCredit()
        && appAccountService.isApp("supplychain")) {
      SaleConfig config = saleConfigService.getSaleConfig(accountingSituation.getCompany());
      if (config != null) {
        accountingSituation.setAcceptedCredit(config.getAcceptedCredit());
      }
    }

    return accountingSituation;
  }
}
