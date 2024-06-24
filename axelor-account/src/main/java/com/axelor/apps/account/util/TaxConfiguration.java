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
package com.axelor.apps.account.util;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.TaxLine;
import java.util.Objects;
import java.util.Optional;

public class TaxConfiguration {

  protected TaxLine taxLine;
  protected Account account;
  protected int vatSystem;

  public TaxConfiguration(TaxLine taxLine, Account account, int vatSystem) {
    this.taxLine = taxLine;
    this.account = account;
    this.vatSystem = vatSystem;
  }

  public TaxLine getTaxLine() {
    return taxLine;
  }

  public Account getAccount() {
    return account;
  }

  public int getVatSystem() {
    return vatSystem;
  }

  public int hashCode() {
    long accountId = Optional.ofNullable(this.account).map(Account::getId).orElse(0L) * 10000;

    return (int) (accountId + this.taxLine.getId() * 10 + this.vatSystem);
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (!(o instanceof TaxConfiguration)) {
      return false;
    }

    TaxConfiguration other = (TaxConfiguration) o;

    return this.vatSystem == other.vatSystem
        && this.taxLine.equals(other.taxLine)
        && Objects.equals(this.account, other.account);
  }
}
