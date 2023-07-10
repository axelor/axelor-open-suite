package com.axelor.apps.hr.service.expense;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.auth.AuthUtils;
import com.axelor.message.db.Message;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import wslite.json.JSONException;

@Singleton
public class ExpenseRefusalServiceImpl implements ExpenseRefusalService {

  protected AppAccountService appAccountService;
  protected HRConfigService hrConfigService;
  protected TemplateMessageService templateMessageService;
  protected ExpenseRepository expenseRepository;

  @Inject
  public ExpenseRefusalServiceImpl(
      AppAccountService appAccountService,
      HRConfigService hrConfigService,
      TemplateMessageService templateMessageService,
      ExpenseRepository expenseRepository) {
    this.appAccountService = appAccountService;
    this.hrConfigService = hrConfigService;
    this.templateMessageService = templateMessageService;
    this.expenseRepository = expenseRepository;
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
}
