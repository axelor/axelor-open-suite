package com.axelor.apps.budget.service.move;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.budget.service.BudgetAccountService;
import com.axelor.apps.budget.service.BudgetBudgetDistributionService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class MoveBudgetServiceImpl implements MoveBudgetService {

  protected MoveLineBudgetService moveLineBudgetService;
  protected BudgetBudgetDistributionService budgetDistributionService;
  protected MoveRepository moveRepo;
  protected AppAccountService appAccountService;
  protected BudgetAccountService budgetAccountService;
  protected MoveLineRepository moveLineRepo;

  @Inject
  public MoveBudgetServiceImpl(
      MoveLineBudgetService moveLineBudgetService,
      MoveRepository moveRepo,
      BudgetBudgetDistributionService budgetDistributionService,
      AppAccountService appAccountService,
      BudgetAccountService budgetAccountService,
      MoveLineRepository moveLineRepo) {

    this.moveLineBudgetService = moveLineBudgetService;
    this.budgetDistributionService = budgetDistributionService;
    this.moveRepo = moveRepo;
    this.appAccountService = appAccountService;
    this.budgetAccountService = budgetAccountService;
    this.moveLineRepo = moveLineRepo;
  }

  @Override
  public String computeBudgetDistribution(Move move) {
    List<String> alertMessageTokenList = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      for (MoveLine moveLine : move.getMoveLineList()) {

        if (CollectionUtils.isNotEmpty(moveLine.getAnalyticMoveLineList())
            && budgetAccountService.checkAccountType(moveLine.getAccount())) {
          String alertMessage = moveLineBudgetService.computeBudgetDistribution(moveLine);

          if (!Strings.isNullOrEmpty(alertMessage)) {
            alertMessageTokenList.add(alertMessage);
          }
        }
      }
    }

    return String.join(", ", alertMessageTokenList);
  }

  @Override
  public void getBudgetExceedAlert(Move move) throws AxelorException {
    String budgetExceedAlert = "";

    List<MoveLine> moveLineList = move.getMoveLineList();

    if (appAccountService.isApp("budget")
        && appAccountService.getAppBudget().getCheckAvailableBudget()
        && CollectionUtils.isNotEmpty(moveLineList)
        && move.getStatusSelect() != MoveRepository.STATUS_NEW
        && move.getStatusSelect() != MoveRepository.STATUS_CANCELED) {

      Map<Budget, BigDecimal> amountPerBudgetMap = new HashMap<>();

      for (MoveLine moveLine : moveLineList) {
        if (CollectionUtils.isNotEmpty(moveLine.getBudgetDistributionList())
            && !AccountTypeRepository.TYPE_INCOME.equals(
                moveLine.getAccount().getAccountType().getTechnicalTypeSelect())) {

          for (BudgetDistribution budgetDistribution : moveLine.getBudgetDistributionList()) {
            Budget budget = budgetDistribution.getBudget();

            if (!amountPerBudgetMap.containsKey(budget)) {
              amountPerBudgetMap.put(budget, budgetDistribution.getAmount());
            } else {
              BigDecimal oldAmount = amountPerBudgetMap.get(budget);
              amountPerBudgetMap.remove(budget);
              amountPerBudgetMap.put(budget, oldAmount.add(budgetDistribution.getAmount()));
            }
          }
        }
      }
      for (Map.Entry<Budget, BigDecimal> budgetEntry : amountPerBudgetMap.entrySet()) {
        budgetExceedAlert +=
            budgetDistributionService.getBudgetExceedAlert(
                budgetEntry.getKey(), budgetEntry.getValue(), move.getDate());
      }

      if (!Strings.isNullOrEmpty(budgetExceedAlert)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, budgetExceedAlert);
      }
    }
  }

  @Override
  public boolean isBudgetInLines(Move move) {
    if (move == null || CollectionUtils.isEmpty(move.getMoveLineList())) {
      return false;
    }

    List<MoveLine> moveLineList =
        move.getMoveLineList().stream()
            .filter(it -> budgetAccountService.checkAccountType(it.getAccount()))
            .collect(Collectors.toList());

    return CollectionUtils.isNotEmpty(moveLineList)
        && moveLineList.stream()
            .anyMatch(ml -> CollectionUtils.isNotEmpty(ml.getBudgetDistributionList()));
  }

  @Override
  public boolean checkMissingBudgetDistributionOnAccountedMove(Move move) {
    return !isBudgetInLines(move)
        && move.getJournal() != null
        && ((move.getJournal().getAllowAccountingDaybook()
                && move.getStatusSelect() == MoveRepository.STATUS_DAYBOOK)
            || (!move.getJournal().getAllowAccountingDaybook()
                && move.getStatusSelect() == MoveRepository.STATUS_NEW));
  }
}
