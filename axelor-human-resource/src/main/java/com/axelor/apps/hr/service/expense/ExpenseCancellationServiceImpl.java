package com.axelor.apps.hr.service.expense;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.HRConfig;
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
import wslite.json.JSONException;

@Singleton
public class ExpenseCancellationServiceImpl implements ExpenseCancellationService {
  protected PeriodService periodService;
  protected HRConfigService hrConfigService;
  protected TemplateMessageService templateMessageService;
  protected ExpenseRepository expenseRepository;
  protected MoveRepository moveRepository;

  @Inject
  public ExpenseCancellationServiceImpl(
      PeriodService periodService,
      HRConfigService hrConfigService,
      TemplateMessageService templateMessageService,
      ExpenseRepository expenseRepository,
      MoveRepository moveRepository) {
    this.periodService = periodService;
    this.hrConfigService = hrConfigService;
    this.templateMessageService = templateMessageService;
    this.expenseRepository = expenseRepository;
    this.moveRepository = moveRepository;
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
