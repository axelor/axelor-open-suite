package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.BudgetLine;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequestScoped
public class BudgetLineServiceImpl implements BudgetLineService {

  @Inject
  public BudgetLineServiceImpl() {}

  @Override
  public Optional<BudgetLine> findBudgetLineAtDate(
      List<BudgetLine> budgetLineList, LocalDate date) {
    if (budgetLineList == null || budgetLineList.isEmpty() || date == null) {
      return Optional.empty();
    }
    return budgetLineList.stream()
        .filter(
            budgetLine ->
                (budgetLine.getFromDate().isBefore(date) || budgetLine.getFromDate().isEqual(date))
                    && (budgetLine.getToDate().isAfter(date)
                        || budgetLine.getToDate().isEqual(date)))
        .findFirst();
  }

  @Override
  public BudgetLine resetBudgetLine(BudgetLine entity) {

    entity.setArchived(false);
    entity.setAmountExpected(entity.getAmountExpected());
    entity.setAmountCommitted(BigDecimal.ZERO);
    entity.setRealizedWithNoPo(BigDecimal.ZERO);
    entity.setRealizedWithPo(BigDecimal.ZERO);
    entity.setAvailableAmount(entity.getAmountExpected());
    entity.setAmountRealized(BigDecimal.ZERO);
    entity.setFirmGap(BigDecimal.ZERO);
    entity.setAmountPaid(BigDecimal.ZERO);
    entity.setToBeCommittedAmount(BigDecimal.ZERO);

    return entity;
  }
}
