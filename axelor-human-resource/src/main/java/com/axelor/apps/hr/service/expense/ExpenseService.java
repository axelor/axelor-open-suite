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
package com.axelor.apps.hr.service.expense;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.KilometricAllowParam;
import com.axelor.message.db.Message;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import wslite.json.JSONException;

public interface ExpenseService {

  public ExpenseLine createAnalyticDistributionWithTemplate(ExpenseLine expenseLine)
      throws AxelorException;

  public ExpenseLine computeAnalyticDistribution(ExpenseLine expenseLine) throws AxelorException;

  public Expense compute(Expense expense);

  public void confirm(Expense expense) throws AxelorException;

  public Message sendConfirmationEmail(Expense expense)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  public void validate(Expense expense) throws AxelorException;

  public Message sendValidationEmail(Expense expense)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  public void refuse(Expense expense) throws AxelorException;

  public Message sendRefusalEmail(Expense expense)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  public Move ventilate(Expense expense) throws AxelorException;

  public void cancel(Expense expense) throws AxelorException;

  public Message sendCancellationEmail(Expense expense)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

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

  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<ExpenseLine> expenseLineList, int priority) throws AxelorException;

  public List<InvoiceLine> createInvoiceLine(Invoice invoice, ExpenseLine expenseLine, int priority)
      throws AxelorException;

  /**
   * Get the expense from employee, if no expense is found create one.
   *
   * @param employee
   * @return
   */
  public Expense getOrCreateExpense(Employee employee);

  public BigDecimal computePersonalExpenseAmount(Expense expense);

  public BigDecimal computeAdvanceAmount(Expense expense);

  public Product getKilometricExpenseProduct(Expense expense) throws AxelorException;

  public void setDraftSequence(Expense expense) throws AxelorException;

  public List<KilometricAllowParam> getListOfKilometricAllowParamVehicleFilter(
      ExpenseLine expenseLine) throws AxelorException;

  public List<ExpenseLine> getExpenseLineList(Expense expense);

  /**
   * fill {@link ExpenseLine#expense} in {@link Expense#generalExpenseLineList} and {@link
   * Expense#kilometricExpenseLineList}
   *
   * @param expense
   */
  void completeExpenseLines(Expense expense);

  public List<KilometricAllowParam> getListOfKilometricAllowParamVehicleFilter(
      ExpenseLine expenseLine, Expense expense) throws AxelorException;

  public Move createMoveForExpensePayment(Expense expense) throws AxelorException;

  public Expense updateMoveDateAndPeriod(Expense expense);
}
