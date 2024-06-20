package com.axelor.apps.budget.service;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.apps.budget.module.BudgetTest;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetToolsService;
import com.axelor.inject.Beans;
import com.axelor.meta.loader.LoaderHelper;
import com.google.inject.Inject;
import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestGlobalBudgetToolsService extends BudgetTest {

  protected final GlobalBudgetRepository globalBudgetRepository;
  protected final BudgetRepository budgetRepository;
  protected final GlobalBudgetToolsService globalBudgetToolsService;
  protected static final LoaderHelper loaderHelper = Beans.get(LoaderHelper.class);

  @Inject
  public TestGlobalBudgetToolsService(
      GlobalBudgetRepository globalBudgetRepository,
      BudgetRepository budgetRepository,
      GlobalBudgetToolsService globalBudgetToolsService) {
    this.globalBudgetRepository = globalBudgetRepository;
    this.budgetRepository = budgetRepository;
    this.globalBudgetToolsService = globalBudgetToolsService;
  }

  @BeforeAll
  static void setUp() {
    loaderHelper.importCsv("data/budget-input.xml");
    loaderHelper.importCsv("data/budget-template-input.xml");
  }

  @Test
  void testGlobalGetAllBudgets() {
    GlobalBudget globalBudget =
        globalBudgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1011L)
            .fetchOne();
    Assertions.assertEquals(
        0,
        globalBudgetToolsService.getAllBudgets(null).size(),
        "Budget list size without global budget");
    Assertions.assertEquals(
        4,
        globalBudgetToolsService.getAllBudgets(globalBudget).size(),
        "Budget list size with global budget");
  }

  @Test
  void testLevelGetAllBudgets() {
    GlobalBudget globalBudget =
        globalBudgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1011L)
            .fetchOne();
    Assertions.assertEquals(
        2,
        globalBudgetToolsService
            .getAllBudgets(globalBudget.getBudgetLevelList().get(0), new ArrayList<>())
            .size(),
        "Budget list size with the first budget level");
    Assertions.assertEquals(
        2,
        globalBudgetToolsService
            .getAllBudgets(globalBudget.getBudgetLevelList().get(1), new ArrayList<>())
            .size(),
        "Budget list size with the second budget level");
  }

  @Test
  void testGetAllBudgetsId() {
    GlobalBudget globalBudget =
        globalBudgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1011L)
            .fetchOne();
    Assertions.assertEquals(
        0L,
        globalBudgetToolsService.getAllBudgetIds(null).get(0),
        "Budget list ids size without global budget");
    Assertions.assertEquals(
        4,
        globalBudgetToolsService.getAllBudgetIds(globalBudget).size(),
        "Budget list ids size with global budget");
  }

  @Test
  void testGetAllBudgetLineIds() {
    GlobalBudget globalBudget =
        globalBudgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1011L)
            .fetchOne();
    Assertions.assertEquals(
        8,
        globalBudgetToolsService.getAllBudgetLineIds(globalBudget).size(),
        "Budget line list ids size without global budget");
  }

  @Test
  void testGlobalGetAllBudgetLevels() {
    GlobalBudget globalBudget =
        globalBudgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1011L)
            .fetchOne();
    Assertions.assertEquals(
        0,
        globalBudgetToolsService.getAllBudgetLevels(null).size(),
        "Budget level list size without global budget");
    Assertions.assertEquals(
        4,
        globalBudgetToolsService.getAllBudgetLevels(globalBudget).size(),
        "Budget level list size with global budget");
  }

  @Test
  void testGetAllBudgetLevelIds() {
    GlobalBudget globalBudget =
        globalBudgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1011L)
            .fetchOne();
    Assertions.assertEquals(
        4,
        globalBudgetToolsService.getAllBudgetLevelIds(globalBudget).size(),
        "Budget level ids list size without global budget");
  }

  @Test
  void testLevelGetAllBudgetLevels() {
    GlobalBudget globalBudget =
        globalBudgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1011L)
            .fetchOne();
    Assertions.assertEquals(
        1,
        globalBudgetToolsService
            .getAllBudgetLevels(globalBudget.getBudgetLevelList().get(0), new ArrayList<>())
            .size(),
        "Budget level list size with the first budget level");
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
