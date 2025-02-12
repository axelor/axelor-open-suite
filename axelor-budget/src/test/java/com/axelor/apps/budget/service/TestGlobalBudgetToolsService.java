/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
  private GlobalBudget globalBudget;
  private Budget budget;

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
    LoaderHelper loaderHelper = Beans.get(LoaderHelper.class);
    loaderHelper.importCsv("data/budget-input.xml");
    loaderHelper.importCsv("data/budget-template-input.xml");
  }

  @Test
  void testGlobalGetAllBudgetsWithoutGlobal() {
    Assertions.assertEquals(
        0,
        globalBudgetToolsService.getAllBudgets(null).size(),
        "Budget list size without global budget");
  }

  @Test
  void testGlobalGetAllBudgetsWithGlobal() {
    givenGlobalBudget(1011L);
    Assertions.assertEquals(
        4,
        globalBudgetToolsService.getAllBudgets(globalBudget).size(),
        "Budget list size with global budget");
  }

  @Test
  void testLevelGetAllBudgets() {
    givenGlobalBudget(1011L);
    Assertions.assertEquals(
        2,
        globalBudgetToolsService
            .getAllBudgets(globalBudget.getBudgetLevelList().get(0), new ArrayList<>())
            .size(),
        "Budget list size with the first budget level");
  }

  @Test
  void testGetAllBudgetsIdWithoutGlobal() {
    Assertions.assertEquals(
        0L,
        globalBudgetToolsService.getAllBudgetIds(null).get(0),
        "Budget list ids size without global budget");
  }

  @Test
  void testGetAllBudgetsIdWithGlobal() {
    givenGlobalBudget(1011L);
    Assertions.assertEquals(
        4,
        globalBudgetToolsService.getAllBudgetIds(globalBudget).size(),
        "Budget list ids size with global budget");
  }

  @Test
  void testGetAllBudgetLineIds() {
    givenGlobalBudget(1011L);
    Assertions.assertEquals(
        8,
        globalBudgetToolsService.getAllBudgetLineIds(globalBudget).size(),
        "Budget line list ids size without global budget");
  }

  @Test
  void testGlobalGetAllBudgetLevelsWithoutGlobal() {
    Assertions.assertEquals(
        0,
        globalBudgetToolsService.getAllBudgetLevels(null).size(),
        "Budget level list size without global budget");
  }

  @Test
  void testGlobalGetAllBudgetLevelsWithGlobal() {
    givenGlobalBudget(1011L);
    Assertions.assertEquals(
        4,
        globalBudgetToolsService.getAllBudgetLevels(globalBudget).size(),
        "Budget level list size with global budget");
  }

  @Test
  void testGetAllBudgetLevelIds() {
    givenGlobalBudget(1011L);
    Assertions.assertEquals(
        4,
        globalBudgetToolsService.getAllBudgetLevelIds(globalBudget).size(),
        "Budget level ids list size without global budget");
  }

  @Test
  void testLevelGetAllBudgetLevels() {
    givenGlobalBudget(1011L);
    Assertions.assertEquals(
        1,
        globalBudgetToolsService
            .getAllBudgetLevels(globalBudget.getBudgetLevelList().get(0), new ArrayList<>())
            .size(),
        "Budget level list size with the first budget level");
  }

  @Test
  void testFillGlobalBudgetOnBudget() {
    givenBudgetAndGlobalBudget(1011L, 1011L);
    budget.setGlobalBudget(null);
    globalBudgetToolsService.fillGlobalBudgetOnBudget(globalBudget);
    Assertions.assertEquals(budget.getGlobalBudget(), globalBudget);
  }

  private void givenGlobalBudget(Long importId) {
    this.globalBudget =
        globalBudgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", importId)
            .fetchOne();
  }

  private void givenBudget(Long importId) {
    this.budget =
        budgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", importId)
            .fetchOne();
  }

  private void givenBudgetAndGlobalBudget(Long budgetImportId, Long globalBudgetImportId) {
    givenGlobalBudget(globalBudgetImportId);
    givenBudget(budgetImportId);
  }
}
