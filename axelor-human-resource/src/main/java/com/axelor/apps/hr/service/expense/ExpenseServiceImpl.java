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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineGenerateRealService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.account.service.move.MoveCancelService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineConsolidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.db.repo.YearBaseRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmployeeAdvanceUsage;
import com.axelor.apps.hr.db.EmployeeVehicle;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.KilometricAllowParam;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.EmployeeAdvanceService;
import com.axelor.apps.hr.service.KilometricService;
import com.axelor.apps.hr.service.bankorder.BankOrderCreateServiceHr;
import com.axelor.apps.hr.service.config.AccountConfigHRService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.message.db.Message;
import com.axelor.message.service.TemplateMessageService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;
import wslite.json.JSONException;

@Singleton
public class ExpenseServiceImpl implements ExpenseService {

  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected ExpenseRepository expenseRepository;
  protected ExpenseLineRepository expenseLineRepository;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveLineConsolidateService moveLineConsolidateService;
  protected AccountManagementAccountService accountManagementService;
  protected AppAccountService appAccountService;
  protected AccountConfigHRService accountConfigService;
  protected AccountingSituationService accountingSituationService;
  protected AnalyticMoveLineService analyticMoveLineService;
  protected AnalyticMoveLineGenerateRealService analyticMoveLineGenerateRealService;
  protected HRConfigService hrConfigService;
  protected TemplateMessageService templateMessageService;
  protected PaymentModeService paymentModeService;
  protected KilometricService kilometricService;
  protected PeriodRepository periodRepository;
  protected BankDetailsService bankDetailsService;
  protected EmployeeAdvanceService employeeAdvanceService;
  protected PeriodService periodService;
  protected MoveRepository moveRepository;
  protected BankOrderCreateServiceHr bankOrderCreateServiceHr;
  protected BankOrderRepository bankOrderRepository;
  protected ReconcileService reconcileService;
  protected BankOrderService bankOrderService;
  protected MoveCancelService moveCancelService;
  protected AppBaseService appBaseService;
  protected SequenceService sequenceService;

  @Inject
  public ExpenseServiceImpl(
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      ExpenseRepository expenseRepository,
      ExpenseLineRepository expenseLineRepository,
      MoveLineCreateService moveLineCreateService,
      AccountManagementAccountService accountManagementService,
      AppAccountService appAccountService,
      AccountConfigHRService accountConfigService,
      AccountingSituationService accountingSituationService,
      AnalyticMoveLineService analyticMoveLineService,
      AnalyticMoveLineGenerateRealService analyticMoveLineGenerateRealService,
      HRConfigService hrConfigService,
      TemplateMessageService templateMessageService,
      PaymentModeService paymentModeService,
      PeriodRepository periodRepository,
      PeriodService periodService,
      MoveLineConsolidateService moveLineConsolidateService,
      KilometricService kilometricService,
      BankDetailsService bankDetailsService,
      EmployeeAdvanceService employeeAdvanceService,
      MoveRepository moveRepository,
      BankOrderCreateServiceHr bankOrderCreateServiceHr,
      BankOrderRepository bankOrderRepository,
      ReconcileService reconcileService,
      BankOrderService bankOrderService,
      MoveCancelService moveCancelService,
      AppBaseService appBaseService,
      SequenceService sequenceService) {

    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.expenseRepository = expenseRepository;
    this.expenseLineRepository = expenseLineRepository;
    this.moveLineCreateService = moveLineCreateService;
    this.accountManagementService = accountManagementService;
    this.appAccountService = appAccountService;
    this.accountConfigService = accountConfigService;
    this.accountingSituationService = accountingSituationService;
    this.analyticMoveLineService = analyticMoveLineService;
    this.analyticMoveLineGenerateRealService = analyticMoveLineGenerateRealService;
    this.hrConfigService = hrConfigService;
    this.templateMessageService = templateMessageService;
    this.paymentModeService = paymentModeService;
    this.periodRepository = periodRepository;
    this.periodService = periodService;
    this.moveLineConsolidateService = moveLineConsolidateService;
    this.kilometricService = kilometricService;
    this.bankDetailsService = bankDetailsService;
    this.employeeAdvanceService = employeeAdvanceService;
    this.moveRepository = moveRepository;
    this.bankOrderCreateServiceHr = bankOrderCreateServiceHr;
    this.bankOrderRepository = bankOrderRepository;
    this.reconcileService = reconcileService;
    this.bankOrderService = bankOrderService;
    this.moveCancelService = moveCancelService;
    this.appBaseService = appBaseService;
    this.sequenceService = sequenceService;
  }

  @Override
  public ExpenseLine computeAnalyticDistribution(ExpenseLine expenseLine) {

    List<AnalyticMoveLine> analyticMoveLineList = expenseLine.getAnalyticMoveLineList();

    if ((analyticMoveLineList == null || analyticMoveLineList.isEmpty())) {
      createAnalyticDistributionWithTemplate(expenseLine);
    }
    if (analyticMoveLineList != null) {
      LocalDate date =
          appAccountService.getTodayDate(
              expenseLine.getExpense() != null
                  ? expenseLine.getExpense().getCompany()
                  : Optional.ofNullable(AuthUtils.getUser())
                      .map(User::getActiveCompany)
                      .orElse(null));
      for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
        analyticMoveLineService.updateAnalyticMoveLine(
            analyticMoveLine, expenseLine.getUntaxedAmount(), date);
      }
    }
    return expenseLine;
  }

  @Override
  public ExpenseLine createAnalyticDistributionWithTemplate(ExpenseLine expenseLine) {

    LocalDate date =
        Optional.ofNullable(expenseLine.getExpenseDate())
            .orElse(
                appAccountService.getTodayDate(
                    expenseLine.getExpense() != null
                        ? expenseLine.getExpense().getCompany()
                        : Optional.ofNullable(AuthUtils.getUser())
                            .map(User::getActiveCompany)
                            .orElse(null)));
    List<AnalyticMoveLine> analyticMoveLineList =
        analyticMoveLineService.generateLines(
            expenseLine.getAnalyticDistributionTemplate(),
            expenseLine.getUntaxedAmount(),
            AnalyticMoveLineRepository.STATUS_FORECAST_INVOICE,
            date);

    expenseLine.setAnalyticMoveLineList(analyticMoveLineList);
    return expenseLine;
  }

  @Override
  public Expense compute(Expense expense) {

    BigDecimal exTaxTotal = BigDecimal.ZERO;
    BigDecimal taxTotal = BigDecimal.ZERO;
    BigDecimal inTaxTotal = BigDecimal.ZERO;
    List<ExpenseLine> generalExpenseLineList = expense.getGeneralExpenseLineList();
    List<ExpenseLine> kilometricExpenseLineList = expense.getKilometricExpenseLineList();

    if (generalExpenseLineList != null) {
      for (ExpenseLine expenseLine : generalExpenseLineList) {
        exTaxTotal = exTaxTotal.add(expenseLine.getUntaxedAmount());
        taxTotal = taxTotal.add(expenseLine.getTotalTax());
        inTaxTotal = inTaxTotal.add(expenseLine.getTotalAmount());
      }
    }
    if (kilometricExpenseLineList != null) {
      for (ExpenseLine kilometricExpenseLine : kilometricExpenseLineList) {
        if (kilometricExpenseLine.getUntaxedAmount() != null) {
          exTaxTotal = exTaxTotal.add(kilometricExpenseLine.getUntaxedAmount());
        }
        if (kilometricExpenseLine.getTotalTax() != null) {
          taxTotal = taxTotal.add(kilometricExpenseLine.getTotalTax());
        }
        if (kilometricExpenseLine.getTotalAmount() != null) {
          inTaxTotal = inTaxTotal.add(kilometricExpenseLine.getTotalAmount());
        }
      }
    }
    expense.setExTaxTotal(exTaxTotal);
    expense.setTaxTotal(taxTotal);
    expense.setInTaxTotal(inTaxTotal);
    return expense;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void confirm(Expense expense) throws AxelorException {

    expense.setStatusSelect(ExpenseRepository.STATUS_CONFIRMED);
    expense.setSentDateTime(
        appAccountService.getTodayDateTime(expense.getCompany()).toLocalDateTime());
    expenseRepository.save(expense);
  }

  @Override
  public Message sendConfirmationEmail(Expense expense)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(expense.getCompany());

    if (hrConfig.getExpenseMailNotification()) {

      return templateMessageService.generateAndSendMessage(
          expense, hrConfigService.getSentExpenseTemplate(hrConfig));
    }

    return null;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validate(Expense expense) throws AxelorException {
    if (expense.getPeriod() == null) {
      throw new AxelorException(
          expense,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_MISSING_PERIOD));
    }

    if (expense.getKilometricExpenseLineList() != null
        && !expense.getKilometricExpenseLineList().isEmpty()) {
      for (ExpenseLine line : expense.getKilometricExpenseLineList()) {
        BigDecimal amount = kilometricService.computeKilometricExpense(line, expense.getEmployee());
        line.setTotalAmount(amount);
        line.setUntaxedAmount(amount);

        kilometricService.updateKilometricLog(line, expense.getEmployee());
      }
      compute(expense);
    }

    employeeAdvanceService.fillExpenseWithAdvances(expense);
    expense.setStatusSelect(ExpenseRepository.STATUS_VALIDATED);
    expense.setValidatedBy(AuthUtils.getUser());
    expense.setValidationDateTime(
        appAccountService.getTodayDateTime(expense.getCompany()).toLocalDateTime());

    if (expense.getEmployee().getContactPartner() != null) {
      PaymentMode paymentMode = expense.getEmployee().getContactPartner().getOutPaymentMode();
      expense.setPaymentMode(paymentMode);
    }
    expenseRepository.save(expense);
  }

  @Override
  public Message sendValidationEmail(Expense expense)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(expense.getCompany());

    if (hrConfig.getExpenseMailNotification()) {

      return templateMessageService.generateAndSendMessage(
          expense, hrConfigService.getValidatedExpenseTemplate(hrConfig));
    }

    return null;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void refuse(Expense expense) throws AxelorException {

    expense.setStatusSelect(ExpenseRepository.STATUS_REFUSED);
    expense.setRefusedBy(AuthUtils.getUser());
    expense.setRefusalDateTime(
        appAccountService.getTodayDateTime(expense.getCompany()).toLocalDateTime());
    expenseRepository.save(expense);
  }

  @Override
  public Message sendRefusalEmail(Expense expense)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(expense.getCompany());

    if (hrConfig.getExpenseMailNotification()) {

      return templateMessageService.generateAndSendMessage(
          expense, hrConfigService.getRefusedExpenseTemplate(hrConfig));
    }

    return null;
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
            accountConfigService.getExpenseJournal(accountConfig),
            company,
            null,
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

    Account employeeAccount = accountingSituationService.getEmployeeAccount(partner, company);
    moveLines.add(
        moveLineCreateService.createMoveLine(
            move,
            partner,
            employeeAccount,
            expense.getInTaxTotal(),
            false,
            moveDate,
            moveDate,
            moveLineCounter++,
            expense.getExpenseSeq(),
            expense.getFullName()));

    List<ExpenseLine> expenseLineList = getExpenseLineList(expense);
    for (ExpenseLine expenseLine : expenseLineList) {
      moveLines.add(
          generateMoveLine(expense, company, partner, expenseLine, move, moveLineCounter));
      moveLineCounter++;
    }

    moveLineConsolidateService.consolidateMoveLines(moveLines);
    Account productAccount = accountConfigService.getExpenseTaxAccount(accountConfig);

    BigDecimal taxTotal =
        expenseLineList.stream()
            .map(ExpenseLine::getTotalTax)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

    if (taxTotal.signum() != 0) {
      MoveLine moveLine =
          moveLineCreateService.createMoveLine(
              move,
              partner,
              productAccount,
              taxTotal,
              true,
              moveDate,
              moveDate,
              moveLineCounter,
              expense.getExpenseSeq(),
              expense.getFullName());
      moveLines.add(moveLine);
    }

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
        accountManagementService.getProductAccount(
            product, company, partner.getFiscalPosition(), true, false);

    if (productAccount == null) {
      throw new AxelorException(
          expense,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MOVE_LINE_4),
          count - 1, // we are using the move line sequence count to get the expense line count
          company.getName());
    }

    MoveLine moveLine =
        moveLineCreateService.createMoveLine(
            move,
            partner,
            productAccount,
            expenseLine.getUntaxedAmount(),
            true,
            moveDate,
            moveDate,
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

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancel(Expense expense) throws AxelorException {
    Move move = expense.getMove();
    if (move == null) {
      expense.setStatusSelect(ExpenseRepository.STATUS_CANCELED);
      expenseRepository.save(expense);
      return;
    }
    periodService.testOpenPeriod(move.getPeriod());
    try {
      moveRepository.remove(move);
      expense.setMove(null);
      expense.setVentilated(false);
      expense.setStatusSelect(ExpenseRepository.STATUS_CANCELED);
    } catch (Exception e) {
      throw new AxelorException(
          e,
          expense,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_CANCEL_MOVE));
    }

    expenseRepository.save(expense);
  }

  @Override
  public Message sendCancellationEmail(Expense expense)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(expense.getCompany());

    if (hrConfig.getTimesheetMailNotification()) {

      return templateMessageService.generateAndSendMessage(
          expense, hrConfigService.getCanceledExpenseTemplate(hrConfig));
    }

    return null;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void addPayment(Expense expense, BankDetails bankDetails) throws AxelorException {

    PaymentMode paymentMode = expense.getPaymentMode();

    if (paymentMode == null) {
      paymentMode = expense.getEmployee().getContactPartner().getOutPaymentMode();

      if (paymentMode == null) {
        throw new AxelorException(
            expense,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(HumanResourceExceptionMessage.EXPENSE_MISSING_PAYMENT_MODE));
      }
      expense.setPaymentMode(paymentMode);
    }

    if (paymentMode.getGenerateBankOrder()) {
      BankOrder bankOrder = bankOrderCreateServiceHr.createBankOrder(expense, bankDetails);
      expense.setBankOrder(bankOrder);
      bankOrderRepository.save(bankOrder);
      expense.setPaymentStatusSelect(InvoicePaymentRepository.STATUS_PENDING);
    } else {
      if (accountConfigService
          .getAccountConfig(expense.getCompany())
          .getGenerateMoveForInvoicePayment()) {
        this.createMoveForExpensePayment(expense);
      }
      if (paymentMode.getAutomaticTransmission()) {
        expense.setPaymentStatusSelect(InvoicePaymentRepository.STATUS_PENDING);
      } else {
        expense.setPaymentStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
        expense.setStatusSelect(ExpenseRepository.STATUS_REIMBURSED);
      }
    }
    expense.setPaymentAmount(
        expense
            .getInTaxTotal()
            .subtract(expense.getAdvanceAmount())
            .subtract(expense.getWithdrawnCash())
            .subtract(expense.getPersonalExpenseAmount()));
  }

  public Move createMoveForExpensePayment(Expense expense) throws AxelorException {
    Company company = expense.getCompany();
    PaymentMode paymentMode = expense.getPaymentMode();
    Partner partner = expense.getEmployee().getContactPartner();
    LocalDate paymentDate = expense.getPaymentDate();
    BigDecimal paymentAmount = expense.getInTaxTotal();
    BankDetails bankDetails = expense.getBankDetails();
    String origin = expense.getExpenseSeq();

    Account employeeAccount;

    Journal journal = paymentModeService.getPaymentModeJournal(paymentMode, company, bankDetails);

    MoveLine expenseMoveLine = this.getExpenseEmployeeMoveLineByLoop(expense);

    if (expenseMoveLine == null) {
      return null;
    }
    employeeAccount = expenseMoveLine.getAccount();

    Move move =
        moveCreateService.createMove(
            journal,
            company,
            expense.getMove().getCurrency(),
            partner,
            paymentDate,
            paymentDate,
            paymentMode,
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            origin,
            null,
            bankDetails);

    move.addMoveLineListItem(
        moveLineCreateService.createMoveLine(
            move,
            partner,
            paymentModeService.getPaymentModeAccount(paymentMode, company, bankDetails),
            paymentAmount,
            false,
            paymentDate,
            null,
            1,
            origin,
            null));

    MoveLine employeeMoveLine =
        moveLineCreateService.createMoveLine(
            move,
            partner,
            employeeAccount,
            paymentAmount,
            true,
            paymentDate,
            null,
            2,
            origin,
            null);
    employeeMoveLine.setTaxAmount(expense.getTaxTotal());

    move.addMoveLineListItem(employeeMoveLine);

    moveValidateService.accounting(move);
    expense.setPaymentMove(move);

    reconcileService.reconcile(expenseMoveLine, employeeMoveLine, true, false);

    expenseRepository.save(expense);

    return move;
  }

  protected MoveLine getExpenseEmployeeMoveLineByLoop(Expense expense) {
    MoveLine expenseEmployeeMoveLine = null;
    for (MoveLine moveline : expense.getMove().getMoveLineList()) {
      if (moveline.getCredit().compareTo(BigDecimal.ZERO) > 0) {
        expenseEmployeeMoveLine = moveline;
      }
    }
    return expenseEmployeeMoveLine;
  }

  @Override
  public void addPayment(Expense expense) throws AxelorException {
    BankDetails bankDetails = expense.getBankDetails();
    if (ObjectUtils.isEmpty(bankDetails)) {
      bankDetails = expense.getCompany().getDefaultBankDetails();
    }

    if (ObjectUtils.isEmpty(bankDetails)) {
      throw new AxelorException(
          expense,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_NO_COMPANY_BANK_DETAILS));
    }
    addPayment(expense, bankDetails);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancelPayment(Expense expense) throws AxelorException {
    BankOrder bankOrder = expense.getBankOrder();

    if (bankOrder != null) {
      if (bankOrder.getStatusSelect() == BankOrderRepository.STATUS_CARRIED_OUT
          || bankOrder.getStatusSelect() == BankOrderRepository.STATUS_REJECTED) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(HumanResourceExceptionMessage.EXPENSE_PAYMENT_CANCEL));
      } else if (bankOrder.getStatusSelect() != BankOrderRepository.STATUS_CANCELED) {
        bankOrderService.cancelBankOrder(bankOrder);
      }
    }

    Move paymentMove = expense.getPaymentMove();
    if (paymentMove != null) {
      if (paymentMove.getStatusSelect() == MoveRepository.STATUS_NEW) {
        expense.setPaymentMove(null);
      }
      moveCancelService.cancel(paymentMove);
    }
    resetExpensePaymentAfterCancellation(expense);
  }

  @Override
  @Transactional
  public void resetExpensePaymentAfterCancellation(Expense expense) {
    expense.setPaymentStatusSelect(InvoicePaymentRepository.STATUS_CANCELED);
    expense.setStatusSelect(ExpenseRepository.STATUS_VALIDATED);
    expense.setPaymentDate(null);
    expense.setPaymentAmount(BigDecimal.ZERO);
  }

  @Override
  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<ExpenseLine> expenseLineList, int priority) throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    int count = 0;
    for (ExpenseLine expenseLine : expenseLineList) {

      invoiceLineList.addAll(this.createInvoiceLine(invoice, expenseLine, priority * 100 + count));
      count++;
      expenseLine.setInvoiced(true);
    }

    return invoiceLineList;
  }

  @Override
  public List<InvoiceLine> createInvoiceLine(Invoice invoice, ExpenseLine expenseLine, int priority)
      throws AxelorException {

    Product product = expenseLine.getExpenseProduct();
    InvoiceLineGenerator invoiceLineGenerator = null;
    Integer atiChoice = invoice.getCompany().getAccountConfig().getInvoiceInAtiSelect();
    if (atiChoice == AccountConfigRepository.INVOICE_WT_ALWAYS
        || atiChoice == AccountConfigRepository.INVOICE_WT_DEFAULT) {
      invoiceLineGenerator =
          new InvoiceLineGenerator(
              invoice,
              product,
              product.getName(),
              expenseLine.getUntaxedAmount(),
              expenseLine.getTotalAmount(),
              expenseLine.getUntaxedAmount(),
              expenseLine.getComments(),
              BigDecimal.ONE,
              product.getUnit(),
              null,
              priority,
              BigDecimal.ZERO,
              PriceListLineRepository.AMOUNT_TYPE_NONE,
              expenseLine.getUntaxedAmount(),
              expenseLine.getTotalAmount(),
              false) {

            @Override
            public List<InvoiceLine> creates() throws AxelorException {

              InvoiceLine invoiceLine = this.createInvoiceLine();

              List<InvoiceLine> invoiceLines = new ArrayList<>();
              invoiceLines.add(invoiceLine);

              return invoiceLines;
            }
          };
    } else {
      invoiceLineGenerator =
          new InvoiceLineGenerator(
              invoice,
              product,
              product.getName(),
              expenseLine.getUntaxedAmount(),
              expenseLine.getTotalAmount(),
              expenseLine.getTotalAmount(),
              expenseLine.getComments(),
              BigDecimal.ONE,
              product.getUnit(),
              null,
              priority,
              BigDecimal.ZERO,
              PriceListLineRepository.AMOUNT_TYPE_NONE,
              expenseLine.getUntaxedAmount(),
              expenseLine.getTotalAmount(),
              false) {

            @Override
            public List<InvoiceLine> creates() throws AxelorException {

              InvoiceLine invoiceLine = this.createInvoiceLine();

              List<InvoiceLine> invoiceLines = new ArrayList<>();
              invoiceLines.add(invoiceLine);

              return invoiceLines;
            }
          };
    }

    return invoiceLineGenerator.creates();
  }

  @Override
  public Expense getOrCreateExpense(Employee employee) {
    if (employee == null) {
      return null;
    }

    Expense expense =
        expenseRepository
            .all()
            .filter(
                "self.statusSelect = ?1 AND self.employee.id = ?2",
                ExpenseRepository.STATUS_DRAFT,
                employee.getId())
            .order("-id")
            .fetchOne();

    if (expense == null) {
      expense = new Expense();
      expense.setEmployee(employee);
      Company company = null;
      if (employee.getMainEmploymentContract() != null) {
        company = employee.getMainEmploymentContract().getPayCompany();
      } else if (employee.getUser() != null) {
        company = employee.getUser().getActiveCompany();
      }

      Period period =
          periodRepository
              .all()
              .filter(
                  "self.fromDate <= ?1 AND self.toDate >= ?1 AND self.allowExpenseCreation = true AND self.year.company = ?2 AND self.year.typeSelect = ?3",
                  appBaseService.getTodayDate(company),
                  company,
                  YearBaseRepository.STATUS_OPENED)
              .fetchOne();

      expense.setCompany(company);
      expense.setPeriod(period);
      expense.setStatusSelect(ExpenseRepository.STATUS_DRAFT);
    }
    return expense;
  }

  @Override
  public BigDecimal computePersonalExpenseAmount(Expense expense) {

    BigDecimal personalExpenseAmount = new BigDecimal("0.00");

    for (ExpenseLine expenseLine : getExpenseLineList(expense)) {
      if (expenseLine.getExpenseProduct() != null
          && expenseLine.getExpenseProduct().getPersonalExpense()) {
        personalExpenseAmount = personalExpenseAmount.add(expenseLine.getTotalAmount());
      }
    }
    return personalExpenseAmount;
  }

  @Override
  public BigDecimal computeAdvanceAmount(Expense expense) {

    BigDecimal advanceAmount = new BigDecimal("0.00");

    if (expense.getEmployeeAdvanceUsageList() != null
        && !expense.getEmployeeAdvanceUsageList().isEmpty()) {
      for (EmployeeAdvanceUsage advanceLine : expense.getEmployeeAdvanceUsageList()) {
        advanceAmount = advanceAmount.add(advanceLine.getUsedAmount());
      }
    }

    return advanceAmount;
  }

  @Override
  public Product getKilometricExpenseProduct(Expense expense) throws AxelorException {
    HRConfig hrConfig = hrConfigService.getHRConfig(expense.getCompany());
    return hrConfigService.getKilometricExpenseProduct(hrConfig);
  }

  @Override
  public void setDraftSequence(Expense expense) throws AxelorException {
    if (expense.getId() != null && Strings.isNullOrEmpty(expense.getExpenseSeq())) {
      expense.setExpenseSeq(sequenceService.getDraftSequenceNumber(expense));
    }
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

  @Override
  public List<KilometricAllowParam> getListOfKilometricAllowParamVehicleFilter(
      ExpenseLine expenseLine) throws AxelorException {

    List<KilometricAllowParam> kilometricAllowParamList = new ArrayList<>();

    Expense expense = expenseLine.getExpense();

    if (expense == null) {
      return kilometricAllowParamList;
    }

    if (expense.getId() != null) {
      expense = expenseRepository.find(expense.getId());
    }

    LocalDate expenseDate = expenseLine.getExpenseDate();
    if (expense.getEmployee() == null || expenseDate == null) {
      return kilometricAllowParamList;
    }

    List<EmployeeVehicle> vehicleList = expense.getEmployee().getEmployeeVehicleList();

    for (EmployeeVehicle vehicle : vehicleList) {
      LocalDate startDate = vehicle.getStartDate();
      LocalDate endDate = vehicle.getEndDate();
      if (startDate == null) {
        if (endDate == null || !expenseDate.isAfter(endDate)) {
          kilometricAllowParamList.add(vehicle.getKilometricAllowParam());
        }
      } else if (endDate == null) {
        if (!expenseDate.isBefore(startDate)) {
          kilometricAllowParamList.add(vehicle.getKilometricAllowParam());
        }
      } else if (!expenseDate.isBefore(startDate) && !expenseDate.isAfter(endDate)) {
        kilometricAllowParamList.add(vehicle.getKilometricAllowParam());
      }
    }
    return kilometricAllowParamList;
  }

  @Override
  public List<KilometricAllowParam> getListOfKilometricAllowParamVehicleFilter(
      ExpenseLine expenseLine, Expense expense) throws AxelorException {

    List<KilometricAllowParam> kilometricAllowParamList = new ArrayList<>();

    if (expense == null) {
      return kilometricAllowParamList;
    }

    if (expense.getId() != null) {
      expense = expenseRepository.find(expense.getId());
    }

    if (expense.getEmployee() != null) {
      return kilometricAllowParamList;
    }

    List<EmployeeVehicle> vehicleList = expense.getEmployee().getEmployeeVehicleList();
    LocalDate expenseDate = expenseLine.getExpenseDate();

    if (expenseDate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.KILOMETRIC_ALLOWANCE_NO_DATE_SELECTED));
    }

    for (EmployeeVehicle vehicle : vehicleList) {
      if (vehicle.getKilometricAllowParam() == null) {
        break;
      }
      LocalDate startDate = vehicle.getStartDate();
      LocalDate endDate = vehicle.getEndDate();
      if ((startDate == null && (endDate == null || !expenseDate.isAfter(endDate)))
          || (endDate == null
              && (!expenseDate.isBefore(startDate)
                  || (!expenseDate.isBefore(startDate) && !expenseDate.isAfter(endDate))))) {
        kilometricAllowParamList.add(vehicle.getKilometricAllowParam());
      }
    }
    return kilometricAllowParamList;
  }

  @Override
  public List<ExpenseLine> getExpenseLineList(Expense expense) {
    List<ExpenseLine> expenseLineList = new ArrayList<>();
    if (expense.getGeneralExpenseLineList() != null) {
      expenseLineList.addAll(expense.getGeneralExpenseLineList());
    }
    if (expense.getKilometricExpenseLineList() != null) {
      expenseLineList.addAll(expense.getKilometricExpenseLineList());
    }
    return expenseLineList;
  }

  @Override
  public void completeExpenseLines(Expense expense) {
    List<ExpenseLine> expenseLineList =
        expenseLineRepository
            .all()
            .filter("self.expense.id = :_expenseId")
            .bind("_expenseId", expense.getId())
            .fetch();
    List<ExpenseLine> kilometricExpenseLineList = expense.getKilometricExpenseLineList();
    List<ExpenseLine> generalExpenseLineList = expense.getGeneralExpenseLineList();

    // removing expense from one O2M also remove the link
    for (ExpenseLine expenseLine : expenseLineList) {
      if (!kilometricExpenseLineList.contains(expenseLine)
          && !generalExpenseLineList.contains(expenseLine)) {
        expenseLine.setExpense(null);
        expenseLineRepository.remove(expenseLine);
      }
    }

    // adding expense in one O2M also add the link
    if (kilometricExpenseLineList != null) {
      for (ExpenseLine kilometricLine : kilometricExpenseLineList) {
        if (!expenseLineList.contains(kilometricLine)) {
          kilometricLine.setExpense(expense);
        }
      }
    }
    if (generalExpenseLineList != null) {
      for (ExpenseLine generalExpenseLine : generalExpenseLineList) {
        if (!expenseLineList.contains(generalExpenseLine)) {
          generalExpenseLine.setExpense(expense);
        }
      }
    }
  }

  @Override
  public Expense updateMoveDateAndPeriod(Expense expense) {
    updateMoveDate(expense);
    updatePeriod(expense);
    return expense;
  }

  protected void updateMoveDate(Expense expense) {
    List<ExpenseLine> expenseLines = new ArrayList<>();

    if (expense.getGeneralExpenseLineList() != null) {
      expenseLines.addAll(expense.getGeneralExpenseLineList());
    }
    if (expense.getKilometricExpenseLineList() != null) {
      expenseLines.addAll(expense.getKilometricExpenseLineList());
    }
    expense.setMoveDate(
        expenseLines.stream()
            .map(ExpenseLine::getExpenseDate)
            .filter(Objects::nonNull)
            .max(LocalDate::compareTo)
            .orElse(null));
  }

  protected void updatePeriod(Expense expense) {
    if (expense.getMoveDate() != null) {
      LocalDate moveDate = expense.getMoveDate();
      if (expense.getPeriod() == null
          || !(!moveDate.isBefore(expense.getPeriod().getFromDate()))
          || !(!moveDate.isAfter(expense.getPeriod().getToDate()))) {
        expense.setPeriod(
            periodRepository
                .all()
                .filter(
                    "self.fromDate <= :_moveDate AND self.toDate >= :_moveDate AND"
                        + " self.statusSelect = 1 AND self.allowExpenseCreation = true AND"
                        + " self.year.company = :_company AND self.year.typeSelect = 1")
                .bind("_moveDate", expense.getMoveDate())
                .bind("_company", expense.getCompany())
                .fetchOne());
      }
    }
  }
}
