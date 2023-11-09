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
package com.axelor.apps.hr.service.expense;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.hr.db.Expense;

public interface ExpensePaymentService {

  public void addPayment(Expense expense, BankDetails bankDetails) throws AxelorException;

  public void addPayment(Expense expense) throws AxelorException;

  /**
   * Cancel the payment in the expense in argument. Revert the payment status and clear all payment
   * fields.
   *
   * @param expense
   * @throws AxelorException
   */
  void cancelPayment(Expense expense) throws AxelorException;

  void resetExpensePaymentAfterCancellation(Expense expense);

  public Move createMoveForExpensePayment(Expense expense) throws AxelorException;
}
