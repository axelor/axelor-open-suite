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

public class AccountingService {

  private static boolean DEFAULT_UPDATE_CUSTOMER_ACCOUNT = true;

  private static final ThreadLocal<Boolean> threadLocal = new ThreadLocal<Boolean>();

  public static void setUpdateCustomerAccount(boolean updateCustomerAccount) {

    threadLocal.set(updateCustomerAccount);
  }

  public static boolean getUpdateCustomerAccount() {

    if (threadLocal.get() != null) {
      return threadLocal.get();
    }

    return DEFAULT_UPDATE_CUSTOMER_ACCOUNT;
  }
}
