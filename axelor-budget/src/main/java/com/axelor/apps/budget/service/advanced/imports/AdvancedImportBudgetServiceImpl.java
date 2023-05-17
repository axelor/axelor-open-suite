package com.axelor.apps.budget.service.advanced.imports;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
public class AdvancedImportBudgetServiceImpl implements AdvancedImportBudgetService {

  protected BudgetLevelRepository budgetLevelRepo;

  @Inject
  public AdvancedImportBudgetServiceImpl(BudgetLevelRepository budgetLevelRepo) {
    this.budgetLevelRepo = budgetLevelRepo;
  }

  @Override
  public BudgetLevel setLevelTypeSelect(BudgetLevel budgetLevel) {
    if (budgetLevel.getParentBudgetLevel() != null) {
      budgetLevel.setLevelTypeSelect(
          budgetLevel
                  .getParentBudgetLevel()
                  .getLevelTypeSelect()
                  .equals(BudgetLevelRepository.BUDGET_LEVEL_LEVEL_TYPE_SELECT_GLOBAL)
              ? BudgetLevelRepository.BUDGET_LEVEL_LEVEL_TYPE_SELECT_GROUP
              : BudgetLevelRepository.BUDGET_LEVEL_LEVEL_TYPE_SELECT_SECTION);
    } else {
      budgetLevel.setLevelTypeSelect(BudgetLevelRepository.BUDGET_LEVEL_LEVEL_TYPE_SELECT_GLOBAL);
    }

    if (!Strings.isNullOrEmpty(budgetLevel.getRootParentBudgetLevel())) {

      String filter = null;

      switch (budgetLevel.getLevelTypeSelect()) {
        case BudgetLevelRepository.BUDGET_LEVEL_LEVEL_TYPE_SELECT_GROUP:
          filter = "self.code = ?2 AND self.typeSelect = ?3";
          break;

        case BudgetLevelRepository.BUDGET_LEVEL_LEVEL_TYPE_SELECT_SECTION:
          filter = "self.parentBudgetLevel.code = ?1 AND self.code = ?2 AND self.typeSelect = ?3";
          break;

        default:
          break;
      }

      if (!Strings.isNullOrEmpty(filter)) {
        String globalCode = budgetLevel.getRootParentBudgetLevel();
        String parentCode = budgetLevel.getParentBudgetLevel().getCode();

        BudgetLevel parentLevel =
            budgetLevelRepo
                .all()
                .filter(
                    filter,
                    globalCode,
                    parentCode,
                    BudgetLevelRepository.BUDGET_LEVEL_TYPE_SELECT_TEMPLATE)
                .fetchOne();

        if (parentLevel != null) {
          budgetLevel.setParentBudgetLevel(parentLevel);
        }
      }
    }
    return budgetLevel;
  }

  @Override
  public Budget setBudgetLevel(Budget budget) {
    if (!Strings.isNullOrEmpty(budget.getRootParentBudgetLevel())
        && budget.getBudgetLevel() != null
        && budget.getBudgetLevel().getCode() != null) {
      String globalCode = budget.getRootParentBudgetLevel();
      String sectionCode = budget.getBudgetLevel().getCode();

      BudgetLevel sectionBudgetLevel =
          budgetLevelRepo
              .all()
              .filter(
                  "self.parentBudgetLevel.parentBudgetLevel.code = ?1 AND self.code = ?2 AND self.typeSelect = ?3",
                  globalCode,
                  sectionCode,
                  BudgetLevelRepository.BUDGET_LEVEL_TYPE_SELECT_TEMPLATE)
              .fetchOne();

      if (sectionBudgetLevel != null) {
        budget.setBudgetLevel(sectionBudgetLevel);
      }
    }
    return budget;
  }
}
