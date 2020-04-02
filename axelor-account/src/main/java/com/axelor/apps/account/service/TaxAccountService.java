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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxAccount;
import com.axelor.apps.base.db.Company;

public class TaxAccountService {

  public Account getAccount(Tax tax, Company company) {

    TaxAccount taxAccount = this.getTaxAccount(tax, company);

    if (taxAccount != null) {
      return taxAccount.getAccount();
    }

    return null;
  }

  public TaxAccount getTaxAccount(Tax tax, Company company) {

    if (tax.getTaxAccountList() != null) {

      for (TaxAccount taxAccount : tax.getTaxAccountList()) {

        if (taxAccount.getCompany().equals(company)) {
          return taxAccount;
        }
      }
    }

    return null;
  }
}
