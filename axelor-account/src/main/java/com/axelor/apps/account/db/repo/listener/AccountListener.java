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
