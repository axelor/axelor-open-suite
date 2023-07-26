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
