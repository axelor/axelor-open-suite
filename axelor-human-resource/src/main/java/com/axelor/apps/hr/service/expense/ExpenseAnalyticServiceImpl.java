package com.axelor.apps.hr.service.expense;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Singleton
public class ExpenseAnalyticServiceImpl implements ExpenseAnalyticService {

  protected AppAccountService appAccountService;
  protected AnalyticMoveLineService analyticMoveLineService;

  @Inject
  public ExpenseAnalyticServiceImpl(
      AppAccountService appAccountService, AnalyticMoveLineService analyticMoveLineService) {
    this.appAccountService = appAccountService;
    this.analyticMoveLineService = analyticMoveLineService;
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
}
