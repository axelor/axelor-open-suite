/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.csv.script;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Map;

public class ImportPartnerAccount {

  @Inject private AccountRepository accountRepo;

  public Object importAccountingSituation(Object bean, Map<String, Object> values) {
    assert bean instanceof Partner;
    try {
      Partner partner = (Partner) bean;
      for (Company company : partner.getCompanySet()) {
        AccountingSituation accountingSituation = new AccountingSituation();
        accountingSituation.setPartner(partner);
        accountingSituation.setCompany(company);
        accountingSituation.setCustomerAccount(
            accountRepo
                .all()
                .filter(
                    "self.code = ?1 AND self.company = ?2",
                    values.get("customerAccount_code").toString(),
                    company)
                .fetchOne());
        accountingSituation.setSupplierAccount(
            accountRepo
                .all()
                .filter(
                    "self.code = ?1 AND self.company = ?2",
                    values.get("supplierAccount_code").toString(),
                    company)
                .fetchOne());
        if (partner.getAccountingSituationList() == null) {
          partner.setAccountingSituationList(new ArrayList<AccountingSituation>());
        }
        partner.getAccountingSituationList().add(accountingSituation);
      }
      return partner;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return bean;
  }
}
