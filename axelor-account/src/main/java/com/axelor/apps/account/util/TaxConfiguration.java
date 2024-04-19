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
