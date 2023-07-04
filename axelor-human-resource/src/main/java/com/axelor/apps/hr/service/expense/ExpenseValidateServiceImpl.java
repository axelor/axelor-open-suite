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
import wslite.json.JSONException;

@Singleton
public class ExpenseValidateServiceImpl implements ExpenseValidateService {

  protected KilometricService kilometricService;
  protected ExpenseComputationService expenseComputationService;
  protected EmployeeAdvanceService employeeAdvanceService;
  protected AppAccountService appAccountService;
  protected HRConfigService hrConfigService;
  protected TemplateMessageService templateMessageService;
  protected ExpenseRepository expenseRepository;

  @Inject
  public ExpenseValidateServiceImpl(
      KilometricService kilometricService,
      ExpenseComputationService expenseComputationService,
      EmployeeAdvanceService employeeAdvanceService,
      AppAccountService appAccountService,
      HRConfigService hrConfigService,
      TemplateMessageService templateMessageService,
      ExpenseRepository expenseRepository) {
    this.kilometricService = kilometricService;
    this.expenseComputationService = expenseComputationService;
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
      expenseComputationService.compute(expense);
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
}
