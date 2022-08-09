/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.service.expense;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
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
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.PriceListLineRepository;
import com.axelor.apps.base.db.repo.YearBaseRepository;
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
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.EmployeeAdvanceService;
import com.axelor.apps.hr.service.KilometricService;
import com.axelor.apps.hr.service.bankorder.BankOrderCreateServiceHr;
import com.axelor.apps.hr.service.config.AccountConfigHRService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.mail.MessagingException;
import wslite.json.JSONException;

@Singleton
public class ExpenseServiceImpl implements ExpenseService {

  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected ExpenseRepository expenseRepository;
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
  protected PeriodRepository periodRepository;

  @Inject
  public ExpenseServiceImpl(
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      ExpenseRepository expenseRepository,
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
      MoveLineConsolidateService moveLineConsolidateService) {

    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.expenseRepository = expenseRepository;
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
    this.moveLineConsolidateService = moveLineConsolidateService;
  }

  @Override
  public ExpenseLine getAndComputeAnalyticDistribution(ExpenseLine expenseLine, Expense expense)
      throws AxelorException {

    if (accountConfigService
            .getAccountConfig(expense.getCompany())
            .getAnalyticDistributionTypeSelect()
        == AccountConfigRepository.DISTRIBUTION_TYPE_FREE) {
      return expenseLine;
    }

    AnalyticDistributionTemplate analyticDistributionTemplate =
        analyticMoveLineService.getAnalyticDistributionTemplate(
            expenseLine.getUser().getPartner(),
            expenseLine.getExpenseProduct(),
            expense.getCompany());

    expenseLine.setAnalyticDistributionTemplate(analyticDistributionTemplate);

    if (expenseLine.getAnalyticMoveLineList() != null) {
      expenseLine.getAnalyticMoveLineList().clear();
    }

    this.computeAnalyticDistribution(expenseLine);

    return expenseLine;
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
    expense.setSentDate(appAccountService.getTodayDate(expense.getCompany()));
    expenseRepository.save(expense);
  }

  @Override
  public Message sendConfirmationEmail(Expense expense)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException {

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

    Employee employee = expense.getUser().getEmployee();
    if (employee == null) {
      throw new AxelorException(
          expense,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.LEAVE_USER_EMPLOYEE),
          expense.getUser().getFullName());
    }

    if (expense.getPeriod() == null) {
      throw new AxelorException(
          expense,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.EXPENSE_MISSING_PERIOD));
    }

    List<ExpenseLine> kilometricExpenseLineList = expense.getKilometricExpenseLineList();
    KilometricService kilometricService = Beans.get(KilometricService.class);
    if (ObjectUtils.notEmpty(kilometricExpenseLineList)) {
      for (ExpenseLine line : kilometricExpenseLineList) {
        BigDecimal amount = kilometricService.computeKilometricExpense(line, employee);
        line.setTotalAmount(amount);
        line.setUntaxedAmount(amount);

        kilometricService.updateKilometricLog(line, employee);
      }
      compute(expense);
    }

    Beans.get(EmployeeAdvanceService.class).fillExpenseWithAdvances(expense);
    expense.setStatusSelect(ExpenseRepository.STATUS_VALIDATED);
    expense.setValidatedBy(AuthUtils.getUser());
    expense.setValidationDate(appAccountService.getTodayDate(expense.getCompany()));

    if (expense.getUser().getPartner() != null) {
      PaymentMode paymentMode = expense.getUser().getPartner().getOutPaymentMode();
      expense.setPaymentMode(paymentMode);
    }
    expenseRepository.save(expense);
  }

  @Override
  public Message sendValidationEmail(Expense expense)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException {

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
    expense.setRefusalDate(appAccountService.getTodayDate(expense.getCompany()));
    expenseRepository.save(expense);
  }

  @Override
  public Message sendRefusalEmail(Expense expense)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException {

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
    Partner partner = expense.getUser().getPartner();

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    if (partner == null) {
      throw new AxelorException(
          expense,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(com.axelor.apps.account.exception.IExceptionMessage.USER_PARTNER),
          expense.getUser().getName());
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
            description);

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

    moveValidateService.validate(move);

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
          I18n.get(com.axelor.apps.account.exception.IExceptionMessage.MOVE_LINE_4),
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
    expenseLine
        .getAnalyticMoveLineList()
        .forEach(
            analyticMoveLine ->
                moveLine.addAnalyticMoveLineListItem(
                    analyticMoveLineGenerateRealService.createFromForecast(
                        analyticMoveLine, moveLine)));
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
    Beans.get(PeriodService.class).testOpenPeriod(move.getPeriod());
    try {
      Beans.get(MoveRepository.class).remove(move);
      expense.setMove(null);
      expense.setVentilated(false);
      expense.setStatusSelect(ExpenseRepository.STATUS_CANCELED);
    } catch (Exception e) {
      throw new AxelorException(
          e,
          expense,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(com.axelor.apps.hr.exception.IExceptionMessage.EXPENSE_CANCEL_MOVE));
    }

    expenseRepository.save(expense);
  }

  @Override
  public Message sendCancellationEmail(Expense expense)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException {

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

    expense.setPaymentDate(appAccountService.getTodayDate(expense.getCompany()));

    PaymentMode paymentMode = expense.getPaymentMode();

    if (paymentMode == null) {
      paymentMode = expense.getUser().getPartner().getOutPaymentMode();

      if (paymentMode == null) {
        throw new AxelorException(
            expense,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(IExceptionMessage.EXPENSE_MISSING_PAYMENT_MODE));
      }
      expense.setPaymentMode(paymentMode);
    }

    if (paymentMode.getGenerateBankOrder()) {
      BankOrder bankOrder =
          Beans.get(BankOrderCreateServiceHr.class).createBankOrder(expense, bankDetails);
      expense.setBankOrder(bankOrder);
      Beans.get(BankOrderRepository.class).save(bankOrder);
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
    Partner partner = expense.getUser().getPartner();
    LocalDate paymentDate = expense.getPaymentDate();
    BigDecimal paymentAmount = expense.getInTaxTotal();
    BankDetails companyBankDetails = company.getDefaultBankDetails();
    String origin = expense.getExpenseSeq();

    Account employeeAccount;

    Journal journal =
        paymentModeService.getPaymentModeJournal(paymentMode, company, companyBankDetails);

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
            null,
            paymentMode,
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            origin,
            null);

    move.addMoveLineListItem(
        moveLineCreateService.createMoveLine(
            move,
            partner,
            paymentModeService.getPaymentModeAccount(paymentMode, company, companyBankDetails),
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

    moveValidateService.validate(move);
    expense.setPaymentMove(move);

    Beans.get(ReconcileService.class).reconcile(expenseMoveLine, employeeMoveLine, true, false);

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
    addPayment(expense, expense.getBankDetails());
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
            I18n.get(IExceptionMessage.EXPENSE_PAYMENT_CANCEL));
      } else if (bankOrder.getStatusSelect() != BankOrderRepository.STATUS_CANCELED) {
        Beans.get(BankOrderService.class).cancelBankOrder(bankOrder);
      }
    }

    Move paymentMove = expense.getPaymentMove();
    if (paymentMove != null) {
      expense.setPaymentMove(null);
      Beans.get(MoveCancelService.class).cancel(paymentMove);
    }
    expense.setPaymentStatusSelect(InvoicePaymentRepository.STATUS_CANCELED);
    expense.setStatusSelect(ExpenseRepository.STATUS_VALIDATED);
    expense.setPaymentDate(null);
    expense.setPaymentAmount(BigDecimal.ZERO);
    expenseRepository.save(expense);
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
  public Expense getOrCreateExpense(User user) {
    Expense expense =
        expenseRepository
            .all()
            .filter(
                "self.statusSelect = ?1 AND self.user.id = ?2",
                ExpenseRepository.STATUS_DRAFT,
                user.getId())
            .order("-id")
            .fetchOne();
    if (expense == null) {
      expense = new Expense();
      expense.setUser(user);
      Company company = null;
      if (user.getEmployee() != null && user.getEmployee().getMainEmploymentContract() != null) {
        company = user.getEmployee().getMainEmploymentContract().getPayCompany();
      } else {
        company = user.getActiveCompany();
      }

      Period period =
          Beans.get(PeriodRepository.class)
              .all()
              .filter(
                  "self.fromDate <= ?1 AND self.toDate >= ?1 AND self.allowExpenseCreation = true AND self.year.company = ?2 AND self.year.typeSelect = ?3",
                  Beans.get(AppBaseService.class).getTodayDate(company),
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
      expense.setExpenseSeq(Beans.get(SequenceService.class).getDraftSequenceNumber(expense));
    }
  }

  private void setExpenseSeq(Expense expense) throws AxelorException {
    if (!Beans.get(SequenceService.class).isEmptyOrDraftSequenceNumber(expense.getExpenseSeq())) {
      return;
    }

    HRConfig hrConfig = hrConfigService.getHRConfig(expense.getCompany());
    Sequence sequence = hrConfigService.getExpenseSequence(hrConfig);

    if (sequence != null) {
      expense.setExpenseSeq(
          Beans.get(SequenceService.class).getSequenceNumber(sequence, expense.getSentDate()));
      if (expense.getExpenseSeq() != null) {
        return;
      }
    }

    throw new AxelorException(
        expense,
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(IExceptionMessage.HR_CONFIG_NO_EXPENSE_SEQUENCE),
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
    if (expense.getUser() == null
        || expense.getUser().getEmployee() == null
        || expenseDate == null) {
      return kilometricAllowParamList;
    }

    List<EmployeeVehicle> vehicleList = expense.getUser().getEmployee().getEmployeeVehicleList();

    for (EmployeeVehicle vehicle : vehicleList) {
      LocalDate startDate = vehicle.getStartDate();
      LocalDate endDate = vehicle.getEndDate();
      if (startDate == null) {
        if (endDate == null || expenseDate.compareTo(endDate) <= 0) {
          kilometricAllowParamList.add(vehicle.getKilometricAllowParam());
        }
      } else if (endDate == null) {
        if (expenseDate.compareTo(startDate) >= 0) {
          kilometricAllowParamList.add(vehicle.getKilometricAllowParam());
        }
      } else if (expenseDate.compareTo(startDate) >= 0 && expenseDate.compareTo(endDate) <= 0) {
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

    if (expense.getUser() != null && expense.getUser().getEmployee() == null) {
      return kilometricAllowParamList;
    }

    List<EmployeeVehicle> vehicleList = expense.getUser().getEmployee().getEmployeeVehicleList();
    LocalDate expenseDate = expenseLine.getExpenseDate();

    if (expenseDate == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.KILOMETRIC_ALLOWANCE_NO_DATE_SELECTED));
    }

    for (EmployeeVehicle vehicle : vehicleList) {
      if (vehicle.getKilometricAllowParam() == null) {
        break;
      }
      LocalDate startDate = vehicle.getStartDate();
      LocalDate endDate = vehicle.getEndDate();
      if ((startDate == null && (endDate == null || expenseDate.compareTo(endDate) <= 0))
          || (endDate == null
              && (expenseDate.compareTo(startDate) >= 0
                  || (expenseDate.compareTo(startDate) >= 0
                      && expenseDate.compareTo(endDate) <= 0)))) {
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
    ExpenseLineRepository expenseLineRepository = Beans.get(ExpenseLineRepository.class);
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
            .max(LocalDate::compareTo)
            .orElse(null));
  }

  protected void updatePeriod(Expense expense) {
    if (expense.getMoveDate() != null) {
      LocalDate moveDate = expense.getMoveDate();
      if (expense.getPeriod() == null
          || !(moveDate.compareTo(expense.getPeriod().getFromDate()) >= 0)
          || !(moveDate.compareTo(expense.getPeriod().getToDate()) <= 0)) {
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
