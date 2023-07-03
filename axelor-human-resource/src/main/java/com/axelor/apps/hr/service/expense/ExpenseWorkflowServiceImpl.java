package com.axelor.apps.hr.service.expense;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CompanyDateService;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.auth.AuthUtils;
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
public class ExpenseWorkflowServiceImpl implements ExpenseWorkflowService {

  protected CompanyDateService companyDateService;
  protected AppAccountService appAccountService;
  protected HRConfigService hrConfigService;
  protected TemplateMessageService templateMessageService;
  protected PeriodService periodService;
  protected ExpenseLineRepository expenseLineRepository;
  protected ExpenseRepository expenseRepository;
  protected MoveRepository moveRepository;

  @Inject
  public ExpenseWorkflowServiceImpl(
      CompanyDateService companyDateService,
      AppAccountService appAccountService,
      HRConfigService hrConfigService,
      TemplateMessageService templateMessageService,
      PeriodService periodService,
      ExpenseLineRepository expenseLineRepository,
      ExpenseRepository expenseRepository,
      MoveRepository moveRepository) {
    this.companyDateService = companyDateService;
    this.appAccountService = appAccountService;
    this.hrConfigService = hrConfigService;
    this.templateMessageService = templateMessageService;
    this.periodService = periodService;
    this.expenseLineRepository = expenseLineRepository;
    this.expenseRepository = expenseRepository;
    this.moveRepository = moveRepository;
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
}
