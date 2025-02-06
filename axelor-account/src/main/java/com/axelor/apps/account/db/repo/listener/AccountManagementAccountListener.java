package com.axelor.apps.account.db.repo.listener;

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.inject.Beans;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class AccountManagementAccountListener {

  @PrePersist
  @PreUpdate
  protected void checkTaxes(AccountManagement accountManagement) throws AxelorException {
    if (accountManagement.getPurchaseTaxSet() != null) {
      Beans.get(TaxAccountService.class)
          .checkTaxesNotOnlyNonDeductibleTaxes(accountManagement.getPurchaseTaxSet());
    }
  }
}
