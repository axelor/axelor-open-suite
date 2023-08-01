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

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CompanyDateService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.i18n.I18n;
import com.axelor.message.db.Message;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import wslite.json.JSONException;

@Singleton
public class ExpenseConfirmationServiceImpl implements ExpenseConfirmationService {

  protected CompanyDateService companyDateService;
  protected AppAccountService appAccountService;
  protected HRConfigService hrConfigService;
  protected TemplateMessageService templateMessageService;
  protected ExpenseLineRepository expenseLineRepository;
  protected ExpenseRepository expenseRepository;

  @Inject
  public ExpenseConfirmationServiceImpl(
      CompanyDateService companyDateService,
      AppAccountService appAccountService,
      HRConfigService hrConfigService,
      TemplateMessageService templateMessageService,
      ExpenseLineRepository expenseLineRepository,
      ExpenseRepository expenseRepository) {
    this.companyDateService = companyDateService;
    this.appAccountService = appAccountService;
    this.hrConfigService = hrConfigService;
    this.templateMessageService = templateMessageService;
    this.expenseLineRepository = expenseLineRepository;
    this.expenseRepository = expenseRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void confirm(Expense expense) throws AxelorException {
    Employee employee = expense.getEmployee();
    Set<String> invitedDates = new HashSet<>();
    DateTimeFormatter dateFormat = companyDateService.getDateFormat(expense.getCompany());

    for (ExpenseLine expenseLine : expense.getGeneralExpenseLineList()) {
      LocalDate expenseDate = expenseLine.getExpenseDate();
      if (!expenseLine.getExpenseProduct().getDeductLunchVoucher()) {
        continue;
      }
      if (expenseLineRepository
              .all()
              .filter(
                  "self.expenseDate = :date AND :employee MEMBER OF self.invitedCollaboratorSet AND self.id != :id")
              .bind("date", expenseDate)
              .bind("employee", employee)
              .bind("id", expenseLine.getId())
              .fetchOne()
          != null) {
        invitedDates.add(expenseDate.format(dateFormat));
      }
    }

    if (!invitedDates.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          formatMessage(
              I18n.get(HumanResourceExceptionMessage.ALREADY_INVITED_TO_RESTAURANT),
              new ArrayList<>(invitedDates)));
    }
    expense.setStatusSelect(ExpenseRepository.STATUS_CONFIRMED);
    expense.setSentDateTime(
        appAccountService.getTodayDateTime(expense.getCompany()).toLocalDateTime());
    expenseRepository.save(expense);
  }

  protected String formatMessage(String title, List<String> messages) {
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("<b>%s</b><br/>", title));
    sb.append(
        messages.stream()
            .map(item -> String.format("<li>%s</li>", item))
            .collect(Collectors.joining("", "<ul>", "</ul>")));
    return sb.toString();
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
}
