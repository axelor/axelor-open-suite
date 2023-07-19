/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import java.util.Arrays;
import java.util.List;

public class BudgetAccountServiceImpl implements BudgetAccountService {
  @Override
  public boolean checkAccountType(Account account) {
    if (account == null || account.getAccountType() == null) {
      return false;
    }

    List<String> accountTypeList =
        Arrays.asList(
            AccountTypeRepository.TYPE_CHARGE,
            AccountTypeRepository.TYPE_INCOME,
            AccountTypeRepository.TYPE_IMMOBILISATION);

    return accountTypeList.contains(account.getAccountType().getTechnicalTypeSelect());
  }
}
