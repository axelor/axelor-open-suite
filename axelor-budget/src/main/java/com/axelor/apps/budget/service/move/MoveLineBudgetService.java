package com.axelor.apps.budget.service.move;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;

public interface MoveLineBudgetService {

  /**
   * Clear budget distribution, compute the budget key related to this configuration of account and
   * analytic, find the budget related to this key and the move date. Then create an automatic
   * budget distribution with the credit or debit and save the move line. Return an alert message if
   * a budget distribution is not generated
   *
   * @param moveLine
   * @return String
   */
  public String computeBudgetDistribution(MoveLine moveLine);

  /**
   * Take all budget distribution and throw an error if the total amount of budget distribution is
   * superior to the debit or credit of the move line
   *
   * @param moveLine
   * @throws AxelorException
   */
  public void checkAmountForMoveLine(MoveLine moveLine) throws AxelorException;
}
