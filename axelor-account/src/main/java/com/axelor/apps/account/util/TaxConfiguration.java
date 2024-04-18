package com.axelor.apps.account.util;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.TaxLine;
import java.util.Objects;
import java.util.Optional;

public class TaxConfiguration {

  protected TaxLine taxline;
  protected Account account;
  protected int vatSystem;

  public TaxConfiguration(TaxLine taxline, Account account, int vatSystem) {
    this.taxline = taxline;
    this.account = account;
    this.vatSystem = vatSystem;
  }

  public int hashCode() {
    long accountId = Optional.ofNullable(this.account).map(Account::getId).orElse(0L) * 10000;

    return (int) (accountId + this.taxline.getId() * 10 + this.vatSystem);
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
        && this.taxline.equals(other.taxline)
        && Objects.equals(this.account, other.account);
  }
}
