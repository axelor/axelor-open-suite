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
package com.axelor.csv.script;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.service.AccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import java.util.Map;

public class ImportAccount {

  private static ThreadLocal<Integer> lineNo = new ThreadLocal<>();

  public Object importAccount(Object bean, Map<String, Object> values) {
    Integer line = lineNo.get();

    if (line == null) {
      lineNo.set(1);
      line = 1;
    } else {
      lineNo.set(line + 1);
    }

    if (bean == null) {
      return null;
    }
    assert bean instanceof Account;
    Account account = (Account) bean;

    try {
      if (account.getCompany() != null
          && Beans.get(AccountConfigService.class)
              .getAccountConfig(account.getCompany())
              .getHasAccountCodeFixedNbrChar()) {
        account = Beans.get(AccountService.class).fillAccountCodeOnImport(account, line);
      }
    } catch (AxelorException e) {
      TraceBackService.trace(e);
    }
    return account;
  }

  public void resetLineNo(Map<String, Object> values) {
    lineNo.set(1);
  }
}
