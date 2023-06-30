package com.axelor.apps.hr.service.expense;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.EmployeeAdvanceService;
import com.axelor.apps.hr.service.KilometricService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.message.db.Message;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import wslite.json.JSONException;

@Singleton
public class ExpenseValidateServiceImpl implements ExpenseValidateService {

  protected KilometricService kilometricService;
  protected EmployeeAdvanceService employeeAdvanceService;
  protected AppAccountService appAccountService;
  protected HRConfigService hrConfigService;
  protected TemplateMessageService templateMessageService;
  protected ExpenseRepository expenseRepository;

  @Inject
  public ExpenseValidateServiceImpl(
      KilometricService kilometricService,
      EmployeeAdvanceService employeeAdvanceService,
      AppAccountService appAccountService,
      HRConfigService hrConfigService,
      TemplateMessageService templateMessageService,
      ExpenseRepository expenseRepository) {
    this.kilometricService = kilometricService;
    this.employeeAdvanceService = employeeAdvanceService;
    this.appAccountService = appAccountService;
    this.hrConfigService = hrConfigService;
    this.templateMessageService = templateMessageService;
    this.expenseRepository = expenseRepository;
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
  public Message sendValidationEmail(Expense expense)
      throws AxelorException, ClassNotFoundException, IOException, JSONException {

    HRConfig hrConfig = hrConfigService.getHRConfig(expense.getCompany());

    if (hrConfig.getExpenseMailNotification()) {

      return templateMessageService.generateAndSendMessage(
          expense, hrConfigService.getValidatedExpenseTemplate(hrConfig));
    }
    return null;
  }
}
