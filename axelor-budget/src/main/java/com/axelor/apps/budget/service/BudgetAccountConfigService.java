package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.base.AxelorException;

public interface BudgetAccountConfigService {

  /**
   * Check if the budget key config is complete. If no, an exception is throwed.
   *
   * @param accountConfig
   * @throws AxelorException
   */
  public void checkBudgetKey(AccountConfig accountConfig) throws AxelorException;
}
