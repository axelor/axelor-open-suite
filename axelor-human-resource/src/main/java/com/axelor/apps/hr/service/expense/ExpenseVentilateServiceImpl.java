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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineGenerateRealService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineConsolidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.config.CompanyConfigService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.config.AccountConfigHRService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class ExpenseVentilateServiceImpl implements ExpenseVentilateService {

  protected AppAccountService appAccountService;
  protected SequenceService sequenceService;
  protected HRConfigService hrConfigService;
  protected AccountConfigService accountConfigService;
  protected AccountConfigHRService accountConfigHRService;
  protected BankDetailsService bankDetailsService;
  protected MoveCreateService moveCreateService;
  protected MoveLineConsolidateService moveLineConsolidateService;
  protected CompanyConfigService companyConfigService;
  protected CurrencyService currencyService;
  protected MoveLineCreateService moveLineCreateService;
  protected ExpenseLineService expenseLineService;
  protected AccountingSituationService accountingSituationService;
  protected MoveValidateService moveValidateService;
  protected AccountManagementAccountService accountManagementAccountService;
  protected AnalyticMoveLineGenerateRealService analyticMoveLineGenerateRealService;
  protected ExpenseRepository expenseRepository;

  @Inject
  public ExpenseVentilateServiceImpl(
      AppAccountService appAccountService,
      SequenceService sequenceService,
      HRConfigService hrConfigService,
      AccountConfigService accountConfigService,
      AccountConfigHRService accountConfigHRService,
      BankDetailsService bankDetailsService,
      MoveCreateService moveCreateService,
      MoveLineConsolidateService moveLineConsolidateService,
      CompanyConfigService companyConfigService,
      CurrencyService currencyService,
      MoveLineCreateService moveLineCreateService,
      ExpenseLineService expenseLineService,
      AccountingSituationService accountingSituationService,
      MoveValidateService moveValidateService,
      AccountManagementAccountService accountManagementAccountService,
      AnalyticMoveLineGenerateRealService analyticMoveLineGenerateRealService,
      ExpenseRepository expenseRepository) {
    this.appAccountService = appAccountService;
    this.sequenceService = sequenceService;
    this.hrConfigService = hrConfigService;
    this.accountConfigService = accountConfigService;
    this.accountConfigHRService = accountConfigHRService;
    this.bankDetailsService = bankDetailsService;
    this.moveCreateService = moveCreateService;
    this.moveLineConsolidateService = moveLineConsolidateService;
    this.companyConfigService = companyConfigService;
    this.currencyService = currencyService;
    this.moveLineCreateService = moveLineCreateService;
    this.expenseLineService = expenseLineService;
    this.accountingSituationService = accountingSituationService;
    this.moveValidateService = moveValidateService;
    this.accountManagementAccountService = accountManagementAccountService;
    this.analyticMoveLineGenerateRealService = analyticMoveLineGenerateRealService;
    this.expenseRepository = expenseRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Move ventilate(Expense expense) throws AxelorException {

    Move move = null;
    setExpenseSeq(expense);

    if (expense.getInTaxTotal().signum() != 0) {
      move = createAndSetMove(expense);
    }
    expense.setVentilated(true);
    expenseRepository.save(expense);

    return move;
  }

  protected void setExpenseSeq(Expense expense) throws AxelorException {
    if (!sequenceService.isEmptyOrDraftSequenceNumber(expense.getExpenseSeq())) {
      return;
    }

    HRConfig hrConfig = hrConfigService.getHRConfig(expense.getCompany());
    Sequence sequence = hrConfigService.getExpenseSequence(hrConfig);

    if (sequence != null) {
      expense.setExpenseSeq(
          sequenceService.getSequenceNumber(
              sequence, expense.getSentDateTime().toLocalDate(), Expense.class, "expenseSeq"));
      if (expense.getExpenseSeq() != null) {
        return;
      }
    }

    throw new AxelorException(
        expense,
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(HumanResourceExceptionMessage.HR_CONFIG_NO_EXPENSE_SEQUENCE),
        expense.getCompany().getName());
  }

  protected Move createAndSetMove(Expense expense) throws AxelorException {
    LocalDate moveDate = expense.getMoveDate();
    if (moveDate == null) {
      moveDate = appAccountService.getTodayDate(expense.getCompany());
      expense.setMoveDate(moveDate);
    }
    String origin = expense.getExpenseSeq();
    String description = expense.getFullName();
    Company company = expense.getCompany();
    Partner partner = expense.getEmployee().getContactPartner();

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    if (partner == null) {
      throw new AxelorException(
          expense,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.EMPLOYEE_PARTNER),
          expense.getEmployee().getName());
    }

    BankDetails companyBankDetails = null;
    if (company != null) {
      companyBankDetails =
          bankDetailsService.getDefaultCompanyBankDetails(
              company, partner.getInPaymentMode(), partner, null);
    }

    Move move =
        moveCreateService.createMove(
            accountConfigHRService.getExpenseJournal(accountConfig),
            company,
            expense.getCurrency(),
            partner,
            moveDate,
            moveDate,
            partner.getInPaymentMode(),
            partner.getFiscalPosition(),
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE,
            origin,
            description,
            companyBankDetails);

    List<MoveLine> moveLines = new ArrayList<>();

    int moveLineCounter = 1;

    List<ExpenseLine> expenseLineList = expenseLineService.getExpenseLineList(expense);
    for (ExpenseLine expenseLine : expenseLineList) {
      moveLines.add(
          generateMoveLine(expense, company, partner, expenseLine, move, moveLineCounter));
      moveLineCounter++;
    }

    BigDecimal taxTotal =
        expenseLineList.stream()
            .map(ExpenseLine::getTotalTax)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

    moveLineConsolidateService.consolidateMoveLines(moveLines);
    Account productAccount = accountConfigHRService.getExpenseTaxAccount(accountConfig);

    if (taxTotal.signum() != 0) {
      Map<LocalDate, List<ExpenseLine>> expenseLinesByExpenseDate =
          expenseLineList.stream().collect(Collectors.groupingBy(ExpenseLine::getExpenseDate));

      Map<LocalDate, BigDecimal> expenseLinesTotalTax =
          expenseLinesByExpenseDate.entrySet().stream()
              .collect(
                  Collectors.toMap(
                      Map.Entry::getKey,
                      entry ->
                          entry.getValue().stream()
                              .map(ExpenseLine::getTotalTax)
                              .reduce(BigDecimal.ZERO, BigDecimal::add)));

      for (Map.Entry<LocalDate, BigDecimal> entry : expenseLinesTotalTax.entrySet()) {
        Currency currency = move.getCurrency();
        Currency companyCurrency = companyConfigService.getCompanyCurrency(move.getCompany());

        BigDecimal currencyRate =
            currencyService.getCurrencyConversionRate(currency, companyCurrency, entry.getKey());

        BigDecimal amountConvertedInCompanyCurrency =
            currencyService.getAmountCurrencyConvertedUsingExchangeRate(
                entry.getValue(), currencyRate);

        moveLines.add(
            moveLineCreateService.createMoveLine(
                move,
                partner,
                productAccount,
                entry.getValue(),
                amountConvertedInCompanyCurrency,
                currencyRate,
                true,
                moveDate,
                moveDate,
                entry.getKey(),
                moveLineCounter++,
                expense.getExpenseSeq(),
                expense.getFullName()));
      }
    }

    BigDecimal totalMoveLines =
        moveLines.stream().map(MoveLine::getDebit).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

    Account employeeAccount = accountingSituationService.getEmployeeAccount(partner, company);
    moveLines.add(
        moveLineCreateService.createMoveLine(
            move,
            partner,
            employeeAccount,
            totalMoveLines,
            totalMoveLines,
            BigDecimal.ONE,
            false,
            moveDate,
            moveDate,
            moveDate,
            moveLineCounter++,
            expense.getExpenseSeq(),
            expense.getFullName()));

    move.getMoveLineList().addAll(moveLines);

    moveValidateService.accounting(move);

    expense.setMove(move);
    return move;
  }

  /**
   * Generates move line (and analytic move lines) related to an expense line.
   *
   * @param expense the parent expense line.
   * @param company the parent expense company.
   * @param partner the employee's partner.
   * @param expenseLine the expense line.
   * @param move the parent move line.
   * @param count the move line sequence count.
   * @return the generated move line.
   * @throws AxelorException if the product accounting configuration cannot be found.
   */
  protected MoveLine generateMoveLine(
      Expense expense,
      Company company,
      Partner partner,
      ExpenseLine expenseLine,
      Move move,
      int count)
      throws AxelorException {
    Product product = expenseLine.getExpenseProduct();
    LocalDate moveDate = expense.getMoveDate();

    Account productAccount =
        accountManagementAccountService.getProductAccount(
            product, company, partner.getFiscalPosition(), true, false);

    if (productAccount == null) {
      throw new AxelorException(
          expense,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_LINE_4),
          count - 1, // we are using the move line sequence count to get the expense line count
          company.getName());
    }

    Currency currency = move.getCurrency();
    Currency companyCurrency = companyConfigService.getCompanyCurrency(move.getCompany());

    BigDecimal currencyRate =
        currencyService.getCurrencyConversionRate(
            currency, companyCurrency, expenseLine.getExpenseDate());

    BigDecimal amountConvertedInCompanyCurrency =
        currencyService.getAmountCurrencyConvertedUsingExchangeRate(
            expenseLine.getUntaxedAmount(), currencyRate);

    MoveLine moveLine =
        moveLineCreateService.createMoveLine(
            move,
            partner,
            productAccount,
            expenseLine.getUntaxedAmount(),
            amountConvertedInCompanyCurrency,
            currencyRate,
            true,
            moveDate,
            moveDate,
            expenseLine.getExpenseDate(),
            count,
            expense.getExpenseSeq(),
            expenseLine.getComments() != null
                ? expenseLine.getComments().replaceAll("(\r\n|\n\r|\r|\n)", " ")
                : "");

    moveLine.setAnalyticDistributionTemplate(expenseLine.getAnalyticDistributionTemplate());
    List<AnalyticMoveLine> analyticMoveLineList =
        CollectionUtils.isEmpty(moveLine.getAnalyticMoveLineList())
            ? new ArrayList<>()
            : new ArrayList<>(moveLine.getAnalyticMoveLineList());
    moveLine.clearAnalyticMoveLineList();
    expenseLine
        .getAnalyticMoveLineList()
        .forEach(
            analyticMoveLine ->
                moveLine.addAnalyticMoveLineListItem(
                    analyticMoveLineGenerateRealService.createFromForecast(
                        analyticMoveLine, moveLine)));
    if (CollectionUtils.isEmpty(moveLine.getAnalyticMoveLineList())) {
      moveLine.setAnalyticMoveLineList(analyticMoveLineList);
    }
    return moveLine;
  }
}
