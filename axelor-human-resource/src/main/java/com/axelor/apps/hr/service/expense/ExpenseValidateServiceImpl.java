/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.EmployeeAdvanceService;
import com.axelor.apps.hr.service.KilometricExpenseService;
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

@Singleton
public class ExpenseValidateServiceImpl implements ExpenseValidateService {

  protected KilometricService kilometricService;
  protected ExpenseComputationService expenseComputationService;
  protected EmployeeAdvanceService employeeAdvanceService;
  protected AppAccountService appAccountService;
  protected HRConfigService hrConfigService;
  protected TemplateMessageService templateMessageService;
  protected ExpenseRepository expenseRepository;
  protected final KilometricExpenseService kilometricExpenseService;

  @Inject
  public ExpenseValidateServiceImpl(
      KilometricService kilometricService,
      ExpenseComputationService expenseComputationService,
      EmployeeAdvanceService employeeAdvanceService,
      AppAccountService appAccountService,
      HRConfigService hrConfigService,
      TemplateMessageService templateMessageService,
      ExpenseRepository expenseRepository,
      KilometricExpenseService kilometricExpenseService) {
    this.kilometricService = kilometricService;
    this.expenseComputationService = expenseComputationService;
    this.employeeAdvanceService = employeeAdvanceService;
    this.appAccountService = appAccountService;
    this.hrConfigService = hrConfigService;
    this.templateMessageService = templateMessageService;
    this.expenseRepository = expenseRepository;
    this.kilometricExpenseService = kilometricExpenseService;
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

    employeeAdvanceService.fillExpenseWithAdvances(expense);
    expense.setStatusSelect(ExpenseRepository.STATUS_VALIDATED);
    expense.setValidatedBy(AuthUtils.getUser());
    expense.setValidationDateTime(
        appAccountService.getTodayDateTime(expense.getCompany()).toLocalDateTime());

    kilometricExpenseService.updateExpenseLineKilometricLog(expense);

    if (expense.getEmployee().getContactPartner() != null) {
      PaymentMode paymentMode = expense.getEmployee().getContactPartner().getOutPaymentMode();
      expense.setPaymentMode(paymentMode);
    }
    expenseRepository.save(expense);
  }

  @Override
  public Message sendValidationEmail(Expense expense) throws AxelorException {

    HRConfig hrConfig = hrConfigService.getHRConfig(expense.getCompany());

    try {
      if (hrConfig.getExpenseMailNotification()) {

        return templateMessageService.generateAndSendMessage(
            expense, hrConfigService.getValidatedExpenseTemplate(hrConfig));
      }
    } catch (IOException | ClassNotFoundException e) {
      new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY, e.getMessage());
    }

    return null;
  }
}
