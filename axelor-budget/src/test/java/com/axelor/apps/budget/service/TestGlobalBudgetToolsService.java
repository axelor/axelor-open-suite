package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.apps.budget.module.BudgetTest;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetToolsService;
import com.axelor.meta.loader.LoaderHelper;
import com.google.inject.Inject;
import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestGlobalBudgetToolsService extends BudgetTest {

  protected final GlobalBudgetRepository globalBudgetRepository;
  protected final BudgetRepository budgetRepository;
  protected final GlobalBudgetToolsService globalBudgetToolsService;
  protected final LoaderHelper loaderHelper;

  @Inject
  public TestGlobalBudgetToolsService(
      GlobalBudgetRepository globalBudgetRepository,
      BudgetRepository budgetRepository,
      GlobalBudgetToolsService globalBudgetToolsService,
      LoaderHelper loaderHelper) {
    this.globalBudgetRepository = globalBudgetRepository;
    this.budgetRepository = budgetRepository;
    this.globalBudgetToolsService = globalBudgetToolsService;
    this.loaderHelper = loaderHelper;
  }

  @BeforeEach
  void setUp() {
    loaderHelper.importCsv("data/budget-input.xml");
    loaderHelper.importCsv("data/budget-template-input.xml");
  }

  @Test
  void testGetAllBudgets() {
    GlobalBudget globalBudget =
        globalBudgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1011L)
            .fetchOne();
    Assertions.assertEquals(globalBudgetToolsService.getAllBudgets(null).size(), 0);
    Assertions.assertEquals(globalBudgetToolsService.getAllBudgets(globalBudget).size(), 4);
    Assertions.assertEquals(
        globalBudgetToolsService
            .getAllBudgets(globalBudget.getBudgetLevelList().get(0), new ArrayList<>())
            .size(),
        2);
    Assertions.assertEquals(
        globalBudgetToolsService
            .getAllBudgets(globalBudget.getBudgetLevelList().get(1), new ArrayList<>())
            .size(),
        2);
  }

  @Test
  void testGetAllBudgetsId() {
    GlobalBudget globalBudget =
        globalBudgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1011L)
            .fetchOne();
    Assertions.assertEquals(globalBudgetToolsService.getAllBudgetIds(null).get(0), 0L);
    Assertions.assertEquals(globalBudgetToolsService.getAllBudgetIds(globalBudget).size(), 4);
    Assertions.assertEquals(globalBudgetToolsService.getAllBudgetLineIds(globalBudget).size(), 8);
  }

  @Test
  void testGetAllBudgetLevels() {
    GlobalBudget globalBudget =
        globalBudgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1011L)
            .fetchOne();
    Assertions.assertEquals(globalBudgetToolsService.getAllBudgetLevels(null).size(), 0);
    Assertions.assertEquals(globalBudgetToolsService.getAllBudgetLevels(globalBudget).size(), 4);
    Assertions.assertEquals(globalBudgetToolsService.getAllBudgetLevelIds(globalBudget).size(), 4);
    Assertions.assertEquals(
        globalBudgetToolsService
            .getAllBudgetLevels(globalBudget.getBudgetLevelList().get(0), new ArrayList<>())
            .size(),
        1);
  }

  @Test
  void testFillGlobalBudgetOnBudget() {
    Budget budget =
        budgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1011L)
            .fetchOne();
    GlobalBudget globalBudget = budget.getGlobalBudget();
    budget.setGlobalBudget(null);
    globalBudgetToolsService.fillGlobalBudgetOnBudget(globalBudget);
    Assertions.assertEquals(budget.getGlobalBudget(), globalBudget);
  }
}
