package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.auth.db.User;

public interface BudgetToolsService {

  /**
   * Return if the budget key is enabled in config and user is in a group that have permission to
   * deal with budget keys
   *
   * @param company, user
   * @return boolean
   */
  boolean checkBudgetKeyAndRole(Company company, User user) throws AxelorException;

  boolean checkBudgetKeyAndRoleForMove(Move move) throws AxelorException;
}
