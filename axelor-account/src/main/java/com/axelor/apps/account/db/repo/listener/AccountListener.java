/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.db.repo.listener;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.inject.Beans;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class AccountListener {

  @PrePersist
  @PreUpdate
  protected void checkTaxes(Account account) throws AxelorException {
    if (account != null && account.getDefaultTaxSet() != null) {
      TaxAccountService taxAccountService = Beans.get(TaxAccountService.class);
      taxAccountService.checkTaxesNotOnlyNonDeductibleTaxes(account.getDefaultTaxSet());
      taxAccountService.checkSumOfNonDeductibleTaxes(account.getDefaultTaxSet());
    }
  }
}
