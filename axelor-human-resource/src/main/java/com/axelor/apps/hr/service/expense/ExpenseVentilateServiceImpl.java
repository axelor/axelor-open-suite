/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineGenerateRealService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineConsolidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineRecordService;
import com.axelor.apps.account.util.TaxAccountToolService;
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
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.config.AccountConfigHRService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.i18n.I18n;
import com.google.common.collect.Sets;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
  protected MoveLineRecordService moveLineRecordService;
  protected AccountManagementService accountManagementService;
  protected TaxAccountToolService taxAccountToolService;

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
      ExpenseRepository expenseRepository,
      MoveLineRecordService moveLineRecordService,
      AccountManagementService accountManagementService,
      TaxAccountToolService taxAccountToolService) {
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
    this.moveLineRecordService = moveLineRecordService;
    this.accountManagementService = accountManagementService;
    this.taxAccountToolService = taxAccountToolService;
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
              sequence,
              expense.getSentDateTime().toLocalDate(),
              Expense.class,
              "expenseSeq",
              expense));
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
    Account taxAccount = accountConfigHRService.getExpenseTaxAccount(accountConfig);

    if (taxTotal.signum() != 0) {
      Map<ExpenseTaxConfiguration, BigDecimal> expenseLinesTotalTax =
          this.computeExpenseLinesTotalTax(expenseLineList, expense, taxAccount);

      Currency currency = move.getCurrency();
      Currency companyCurrency = companyConfigService.getCompanyCurrency(move.getCompany());
      BigDecimal currencyRate =
          currencyService.getCurrencyConversionRate(currency, companyCurrency, moveDate);

      for (Map.Entry<ExpenseTaxConfiguration, BigDecimal> entry : expenseLinesTotalTax.entrySet()) {
        ExpenseTaxConfiguration taxConfig = entry.getKey();

        BigDecimal amountConvertedInCompanyCurrency =
            currencyService.getAmountCurrencyConvertedUsingExchangeRate(
                entry.getValue(), currencyRate, companyCurrency);

        MoveLine taxMoveLine =
            moveLineCreateService.createMoveLine(
                move,
                partner,
                taxAccount,
                entry.getValue(),
                amountConvertedInCompanyCurrency,
                currencyRate,
                true,
                moveDate,
                moveDate,
                moveDate,
                moveLineCounter++,
                expense.getExpenseSeq(),
                expense.getFullName());

        taxMoveLine.setTaxLineSet(Sets.newHashSet(taxConfig.getTaxLine()));
        taxMoveLine.setTaxRate(taxConfig.getTaxLine().getValue());
        taxMoveLine.setTaxCode(taxConfig.getTaxLine().getTax().getCode());
        taxMoveLine.setVatSystemSelect(taxConfig.getVatSystem());

        moveLines.add(taxMoveLine);
      }
    }

    BigDecimal totalAmount =
        expenseLineList.stream()
            .map(ExpenseLine::getTotalAmount)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

    Account employeeAccount = accountingSituationService.getEmployeeAccount(partner, company);

    Currency companyCurrency = companyConfigService.getCompanyCurrency(move.getCompany());
    BigDecimal currencyRate =
        currencyService.getCurrencyConversionRate(move.getCurrency(), companyCurrency, moveDate);

    BigDecimal amountConvertedInCompanyCurrency =
        currencyService.getAmountCurrencyConvertedUsingExchangeRate(
            totalAmount, currencyRate, companyCurrency);

    moveLines.add(
        moveLineCreateService.createMoveLine(
            move,
            partner,
            employeeAccount,
            totalAmount,
            amountConvertedInCompanyCurrency,
            currencyRate,
            false,
            moveDate,
            moveDate,
            moveDate,
            moveLineCounter++,
            expense.getExpenseSeq(),
            expense.getFullName()));

    for (MoveLine moveLine : moveLines) {
      moveLineRecordService.refreshAccountInformation(moveLine, move);
    }
    move.getMoveLineList().addAll(moveLines);
    move.setExpense(expense);
    moveValidateService.accounting(move);
    return move;
  }

  protected Map<ExpenseTaxConfiguration, BigDecimal> computeExpenseLinesTotalTax(
      List<ExpenseLine> expenseLineList, Expense expense, Account taxAccount)
      throws AxelorException {
    Map<ExpenseTaxConfiguration, BigDecimal> result = new HashMap<>();

    Partner partner = expense.getEmployee().getContactPartner();
    FiscalPosition fiscalPosition = partner != null ? partner.getFiscalPosition() : null;

    List<ExpenseLine> taxedExpenseLines =
        expenseLineList.stream().filter(this::expenseLineHasTax).toList();

    for (ExpenseLine expenseLine : taxedExpenseLines) {
      addExpenseLineTaxesToMap(result, expenseLine, expense, taxAccount, fiscalPosition);
    }

    return result;
  }

  protected boolean expenseLineHasTax(ExpenseLine expenseLine) {
    return expenseLine.getTotalTax() != null && expenseLine.getTotalTax().signum() != 0;
  }

  protected void addExpenseLineTaxesToMap(
      Map<ExpenseTaxConfiguration, BigDecimal> taxMap,
      ExpenseLine expenseLine,
      Expense expense,
      Account taxAccount,
      FiscalPosition fiscalPosition)
      throws AxelorException {

    Set<TaxLine> taxLineSet = getTaxLineSetForExpenseLine(expenseLine, expense, fiscalPosition);
    if (CollectionUtils.isEmpty(taxLineSet)) {
      return;
    }

    int vatSystem = computeVatSystem(expense, taxAccount);
    BigDecimal totalTaxRate = computeTotalTaxRate(taxLineSet);

    for (TaxLine taxLine : taxLineSet) {
      ExpenseTaxConfiguration taxConfig = new ExpenseTaxConfiguration(taxLine, vatSystem);
      BigDecimal taxAmount =
          computeProportionalTaxAmount(
              expenseLine.getTotalTax(), taxLine, totalTaxRate, taxLineSet.size());
      taxMap.merge(taxConfig, taxAmount, BigDecimal::add);
    }
  }

  protected Set<TaxLine> getTaxLineSetForExpenseLine(
      ExpenseLine expenseLine, Expense expense, FiscalPosition fiscalPosition)
      throws AxelorException {
    return accountManagementService.getTaxLineSet(
        expenseLine.getExpenseDate(),
        expenseLine.getExpenseProduct(),
        expense.getCompany(),
        fiscalPosition,
        true);
  }

  protected int computeVatSystem(Expense expense, Account taxAccount) throws AxelorException {
    return taxAccountToolService.calculateVatSystem(
        expense.getEmployee().getContactPartner(), expense.getCompany(), taxAccount, true, false);
  }

  protected BigDecimal computeTotalTaxRate(Set<TaxLine> taxLineSet) {
    return taxLineSet.stream()
        .map(TaxLine::getValue)
        .filter(java.util.Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  protected BigDecimal computeProportionalTaxAmount(
      BigDecimal totalTax, TaxLine taxLine, BigDecimal totalTaxRate, int taxLineCount) {
    if (taxLineCount == 1 || totalTaxRate.signum() == 0) {
      return totalTax;
    }
    BigDecimal taxLineRate = taxLine.getValue() != null ? taxLine.getValue() : BigDecimal.ZERO;
    return totalTax.multiply(taxLineRate).divide(totalTaxRate, 2, RoundingMode.HALF_UP);
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

    MoveLine moveLine =
        moveLineCreateService.createMoveLine(
            move,
            partner,
            productAccount,
            expenseLine.getUntaxedAmount(),
            expenseLine.getCompanyUntaxedAmount(),
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
