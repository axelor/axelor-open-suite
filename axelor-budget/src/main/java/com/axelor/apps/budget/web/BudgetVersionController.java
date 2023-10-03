package com.axelor.apps.budget.web;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetVersion;
import com.axelor.apps.budget.db.VersionExpectedAmountsLine;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import java.util.List;
import java.util.stream.Collectors;

public class BudgetVersionController {

  public void notifyLineAmounts(ActionRequest request, ActionResponse response) {
    BudgetVersion budgetVersion = request.getContext().asType(BudgetVersion.class);

    if (budgetVersion == null
        || ObjectUtils.isEmpty(budgetVersion.getVersionExpectedAmountsLineList())) {
      return;
    }

    List<String> budgetCodeError =
        budgetVersion.getVersionExpectedAmountsLineList().stream()
            .filter(
                line ->
                    line.getBudget() != null
                        && line.getExpectedAmount() != null
                        && line.getExpectedAmount()
                                .compareTo(line.getBudget().getTotalAmountRealized())
                            < 0)
            .map(VersionExpectedAmountsLine::getBudget)
            .map(Budget::getCode)
            .collect(Collectors.toList());
    if (!ObjectUtils.isEmpty(budgetCodeError)) {
      response.setInfo(
          String.format(
              I18n.get(BudgetExceptionMessage.VERSION_LINE_EXCEED_REALIZED),
              Joiner.on(", ").join(budgetCodeError)));
    }
  }
}
