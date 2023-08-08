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
package com.axelor.apps.hr.service.batch;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.batch.BatchCreditTransferExpensePayment;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMergeService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.expense.ExpensePaymentService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchCreditTransferExpensePaymentHR extends BatchCreditTransferExpensePayment {

  protected final Logger log = LoggerFactory.getLogger(getClass());
  protected final AppAccountService appAccountService;
  protected final ExpenseRepository expenseRepo;
  protected final ExpensePaymentService expensePaymentService;
  protected final BankOrderMergeService bankOrderMergeService;

  @Inject
  public BatchCreditTransferExpensePaymentHR(
      AppAccountService appAccountService,
      ExpenseRepository expenseRepo,
      ExpensePaymentService expensePaymentService,
      BankOrderMergeService bankOrderMergeService) {
    this.appAccountService = appAccountService;
    this.expenseRepo = expenseRepo;
    this.expensePaymentService = expensePaymentService;
    this.bankOrderMergeService = bankOrderMergeService;
  }

  @Override
  protected void process() {
    List<Expense> doneList = processExpenses();

    try {
      mergeBankOrders(doneList);
    } catch (Exception ex) {
      TraceBackService.trace(ex);
      ex.printStackTrace();
      log.error("Credit transfer batch for expense payments: mergeBankOrders");
    }
  }

  @Override
  protected void stop() {
    StringBuilder sb = new StringBuilder();
    sb.append(I18n.get(AccountExceptionMessage.BATCH_CREDIT_TRANSFER_REPORT_TITLE)).append(" ");
    sb.append(
        String.format(
            I18n.get(
                    HumanResourceExceptionMessage.BATCH_CREDIT_TRANSFER_EXPENSE_DONE_SINGULAR,
                    HumanResourceExceptionMessage.BATCH_CREDIT_TRANSFER_EXPENSE_DONE_PLURAL,
                    batch.getDone())
                + " ",
            batch.getDone()));
    sb.append(
        String.format(
            I18n.get(
                BaseExceptionMessage.ABSTRACT_BATCH_ANOMALY_SINGULAR,
                BaseExceptionMessage.ABSTRACT_BATCH_ANOMALY_PLURAL,
                batch.getAnomaly()),
            batch.getAnomaly()));
    addComment(sb.toString());
    super.stop();
  }

  /**
   * Process expenses that need to be paid.
   *
   * @return
   */
  protected List<Expense> processExpenses() {
    List<Expense> doneList = new ArrayList<>();
    List<Long> anomalyList = Lists.newArrayList(0L); // Can't pass an empty collection to the query
    AccountingBatch accountingBatch = batch.getAccountingBatch();
    boolean manageMultiBanks = appAccountService.getAppBase().getManageMultiBanks();
    String filter =
        "self.ventilated = true "
            + "AND self.paymentStatusSelect = :paymentStatusSelect "
            + "AND self.company = :company "
            + "AND self.employee.contactPartner.outPaymentMode = :paymentMode "
            + "AND self.id NOT IN (:anomalyList)";

    if (manageMultiBanks) {
      filter += " AND self.bankDetails IN (:bankDetailsSet)";
    }

    Query<Expense> query =
        expenseRepo
            .all()
            .filter(filter)
            .bind("paymentStatusSelect", InvoicePaymentRepository.STATUS_DRAFT)
            .bind("company", accountingBatch.getCompany())
            .bind("paymentMode", accountingBatch.getPaymentMode())
            .bind("anomalyList", anomalyList);

    if (manageMultiBanks) {
      Set<BankDetails> bankDetailsSet = Sets.newHashSet(accountingBatch.getBankDetails());

      if (accountingBatch.getIncludeOtherBankAccounts()) {
        bankDetailsSet.addAll(accountingBatch.getCompany().getBankDetailsList());
      }

      query.bind("bankDetailsSet", bankDetailsSet);
    }

    for (List<Expense> expenseList;
        !(expenseList = query.fetch(FETCH_LIMIT)).isEmpty();
        JPA.clear()) {
      for (Expense expense : expenseList) {
        try {
          addPayment(expense, accountingBatch.getBankDetails());
          doneList.add(expense);
          incrementDone();
        } catch (Exception ex) {
          incrementAnomaly();
          anomalyList.add(expense.getId());
          query.bind("anomalyList", anomalyList);
          TraceBackService.trace(ex, ExceptionOriginRepository.CREDIT_TRANSFER, batch.getId());
          ex.printStackTrace();
          log.error(
              String.format(
                  "Credit transfer batch for expense payment: anomaly for expense %s",
                  expense.getExpenseSeq()));
          break;
        }
      }
    }

    return doneList;
  }

  /**
   * Add a payment to the specified expense.
   *
   * @param expense
   * @param bankDetails
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  protected void addPayment(Expense expense, BankDetails bankDetails) throws AxelorException {
    log.debug(
        String.format(
            "Credit transfer batch for expense payment: adding payment for expense %s",
            expense.getExpenseSeq()));
    expensePaymentService.addPayment(expense, bankDetails);
  }

  /**
   * Merge bank orders.
   *
   * @param doneList
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  protected void mergeBankOrders(List<Expense> doneList) throws AxelorException {
    List<Expense> expenseList = new ArrayList<>();
    List<BankOrder> bankOrderList = new ArrayList<>();

    for (Expense expense : doneList) {
      BankOrder bankOrder = expense.getBankOrder();
      if (bankOrder != null) {
        expenseList.add(expense);
        bankOrderList.add(bankOrder);
      }
    }

    if (bankOrderList.size() > 1) {
      BankOrder mergedBankOrder = bankOrderMergeService.mergeBankOrders(bankOrderList);
      for (Expense expense : expenseList) {
        expense.setBankOrder(mergedBankOrder);
      }
    }
  }
}
