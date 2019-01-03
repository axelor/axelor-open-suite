/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.Account;
import com.axelor.db.JPA;
import java.util.Set;
import javax.persistence.PersistenceException;

public class AccountAccountRepository extends AccountRepository {

  @Override
  public Account save(Account account) {
    try {
      if (account.getId() == null) {
        return super.save(account);
      }

      if (account.getReconcileOk()) {
        Set<Account> accountList = account.getCompatibleAccountSet();

        if (accountList != null) {
          for (Account acc : accountList) {
            acc.setReconcileOk(true);
            acc.addCompatibleAccountSetItem(account);
            JPA.save(acc);
          }
        }
      } else {

        if (account.getCompatibleAccountSet() != null) {
          for (Account acc : account.getCompatibleAccountSet()) {
            acc.removeCompatibleAccountSetItem(account);
            if (acc.getCompatibleAccountSet().size() == 0) {
              acc.setReconcileOk(false);
            }
            JPA.save(acc);
          }
          account.getCompatibleAccountSet().clear();
        }
      }
      return super.save(account);
    } catch (Exception e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }
}
