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
import com.axelor.apps.sale.db.SaleConfig;
import com.axelor.apps.sale.service.config.SaleConfigServiceImpl;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class SupplychainSaleConfigServiceImpl extends SaleConfigServiceImpl
    implements SupplychainSaleConfigService {

  @Inject private AccountingSituationRepository accountingSituationRepo;

  @Transactional
  public void updateCustomerCredit(SaleConfig saleConfig) {

    List<AccountingSituation> accountingSituationList =
        accountingSituationRepo
            .all()
            .filter("self.partner.isContact = false and self.partner.isCustomer = true")
            .fetch();

    for (AccountingSituation accountingSituation : accountingSituationList) {
      accountingSituation.setAcceptedCredit(saleConfig.getAcceptedCredit());
      accountingSituationRepo.save(accountingSituation);
    }
  }
}
