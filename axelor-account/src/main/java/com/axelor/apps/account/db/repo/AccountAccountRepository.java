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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.PersistenceException;
import org.apache.commons.collections.CollectionUtils;

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
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  @Override
  public Account copy(Account entity, boolean deep) {
    Account account = super.copy(entity, deep);
    account.setCode(String.format("%s (copy)", account.getCode()));
    account.setName(String.format("%s (copy)", account.getName()));
    account.setStatusSelect(AccountRepository.STATUS_INACTIVE);
    return account;
  }

  @Override
  public void remove(Account account) {
    if (account.getIsRegulatoryAccount()) {
      Exception e =
          new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(AccountExceptionMessage.ACCOUNT_REGULATORY_REMOVE));
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e);
    }

    if (CollectionUtils.isNotEmpty(account.getCompatibleAccountSet())) {
      this.clearCompatibleAccountSet(account);
    }

    super.remove(account);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void clearCompatibleAccountSet(Account account) {
    for (Account compatibleAccount : account.getCompatibleAccountSet()) {
      compatibleAccount.getCompatibleAccountSet().remove(account);
      this.save(compatibleAccount);
    }

    account.setCompatibleAccountSet(new HashSet<>());
    this.save(account);
  }
}
