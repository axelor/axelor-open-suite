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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetGenerator;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetScenario;
import com.axelor.apps.budget.db.BudgetStructure;
import com.axelor.apps.budget.db.BudgetVersion;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.db.repo.BudgetScenarioRepository;
import com.axelor.apps.budget.db.repo.BudgetStructureRepository;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.apps.budget.module.BudgetTest;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetService;
import com.axelor.inject.Beans;
import com.axelor.meta.loader.LoaderHelper;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoper;
import com.google.inject.servlet.ServletScopes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestGlobalBudgetService extends BudgetTest {

  protected final GlobalBudgetRepository globalBudgetRepository;
  protected final BudgetRepository budgetRepository;
  protected final BudgetStructureRepository budgetStructureRepository;
  protected final BudgetScenarioRepository budgetScenarioRepository;
  protected final BudgetLevelRepository budgetLevelRepository;
  protected final GlobalBudgetService globalBudgetService;
  protected final BudgetVersionService budgetVersionService;
  private GlobalBudget globalBudget;
  private Budget budget;
  private BudgetLevel budgetLevel;
  private BudgetStructure budgetStructure;
  private BudgetScenario budgetScenario;
  private Year year;
  private BudgetGenerator budgetGenerator;

  @Inject
  public TestGlobalBudgetService(
      GlobalBudgetRepository globalBudgetRepository,
      BudgetRepository budgetRepository,
      BudgetStructureRepository budgetStructureRepository,
      BudgetScenarioRepository budgetScenarioRepository,
      BudgetLevelRepository budgetLevelRepository,
      BudgetVersionService budgetVersionService) {
    this.globalBudgetRepository = globalBudgetRepository;
    this.budgetRepository = budgetRepository;
    this.budgetStructureRepository = budgetStructureRepository;
    this.budgetScenarioRepository = budgetScenarioRepository;
    this.budgetLevelRepository = budgetLevelRepository;
    this.budgetVersionService = budgetVersionService;

    //  We need to initialize this service like that because it is linked to a Request scoped
    // service
    RequestScoper scope = ServletScopes.scopeRequest(Collections.emptyMap());
    try (RequestScoper.CloseableScope ignored = scope.open()) {
      this.globalBudgetService = Beans.get(GlobalBudgetService.class);
    }
  }

  @BeforeAll
  static void setUp() {
    final LoaderHelper loaderHelper = Beans.get(LoaderHelper.class);
    loaderHelper.importCsv("data/budget-input.xml");
    loaderHelper.importCsv("data/budget-template-input.xml");
  }

  @Test
  void testUpdateGlobalBudgetDates() throws AxelorException {
    givenBudgetAndGlobalBudgetWithDates(
        1011L, 1011L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
    globalBudgetService.updateGlobalBudgetDates(globalBudget);
    Assertions.assertEquals(budget.getFromDate(), LocalDate.of(2024, 1, 1));
  }

  @Test
  void testValidateDates() {
    givenBudgetAndGlobalBudgetWithDates(
        null, 1011L, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 12, 31));
    Assertions.assertThrows(
        AxelorException.class, () -> globalBudgetService.validateDates(globalBudget));
  }

  @Test
  void testComputeBudgetLevelTotalsGlobal() {
    givenBudgetGlobalAndLevelRecompute(1011L, 1011L, 1014L, new BigDecimal(1000));
    Assertions.assertEquals(4900, globalBudget.getTotalAmountExpected().intValue());
  }

  @Test
  void testComputeBudgetLevelTotalsLevel() {
    givenBudgetGlobalAndLevelRecompute(1011L, 1011L, 1014L, new BigDecimal(1000));
    Assertions.assertEquals(1500, budgetLevel.getTotalAmountExpected().intValue());
  }

  @Test
  void testChangeBudgetVersionWithoutVersion() throws AxelorException {
    givenGlobalBudgetAndBudgetVersion(1011L, null);
    Assertions.assertNull(
        globalBudgetService.changeBudgetVersion(globalBudget, null, false).getActiveVersion());
  }

  @Test
  void testChangeBudgetVersionWithVersion() throws AxelorException {
    givenGlobalBudgetAndBudgetVersion(1011L, "version");
    Assertions.assertEquals(
        4, globalBudget.getActiveVersion().getVersionExpectedAmountsLineList().size());
  }

  @Test
  void testChangeBudgetVersionActiveVersion() throws AxelorException {
    givenGlobalBudgetAndBudgetVersion(1011L, "version");
    Assertions.assertNotNull(globalBudget.getActiveVersion());
  }

  @Test
  void testGenerateGlobalBudget() throws AxelorException {
    givenGlobalBudgetFromStructure(1001L, 1L);
    Assertions.assertNotNull(globalBudget);
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

  private void givenBudgetLevel(Long importId) {
    this.budgetLevel =
        budgetLevelRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", importId)
            .fetchOne();
  }

  private void givenBudgetAndGlobalBudget(Long budgetImportId, Long globalBudgetImportId) {
    givenGlobalBudget(globalBudgetImportId);
    givenBudget(budgetImportId);
  }

  private void givenBudgetAndGlobalBudgetWithDates(
      Long budgetImportId, Long globalBudgetImportId, LocalDate fromDate, LocalDate toDate) {
    givenBudgetAndGlobalBudget(budgetImportId, globalBudgetImportId);
    globalBudget.setFromDate(fromDate);
    globalBudget.setToDate(toDate);
  }

  private void givenBudgetGlobalAndLevelRecompute(
      Long budgetImportId,
      Long globalBudgetImportId,
      Long budgetLevelImportId,
      BigDecimal newAmount) {
    givenBudget(budgetImportId);
    givenGlobalBudget(globalBudgetImportId);
    givenBudgetLevel(budgetLevelImportId);
    budget.setTotalAmountExpected(new BigDecimal(1000));
    globalBudgetService.computeBudgetLevelTotals(budget);
  }

  private void givenGlobalBudgetAndBudgetVersion(Long globalBudgetImportId, String versionName)
      throws AxelorException {
    givenGlobalBudget(globalBudgetImportId);
    if (versionName == null) {
      globalBudget.setActiveVersion(null);
    } else {
      BudgetVersion budgetVersion =
          budgetVersionService.createNewVersion(globalBudget, versionName);
      globalBudget = globalBudgetService.changeBudgetVersion(globalBudget, budgetVersion, false);
    }
  }

  private void givenBudgetStructure(Long budgetStructureImportId) {
    this.budgetStructure =
        budgetStructureRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", budgetStructureImportId)
            .fetchOne();
  }

  private void givenBudgetScenario(Long budgetScenarioImportId) {
    this.budgetScenario =
        budgetScenarioRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", budgetScenarioImportId)
            .fetchOne();
  }

  private void givenBudgetGenerator() {
    BudgetGenerator budgetGenerator = new BudgetGenerator();
    budgetGenerator.setBudgetStructure(budgetStructure);
    budgetGenerator.setCode("GENERATOR");
    budgetGenerator.setName("GENERATOR");
    this.budgetGenerator = budgetGenerator;
  }

  private void givenYear() {
    Year year = new Year();
    year.setCode("2024");
    year.setName("2024");
    year.setFromDate(LocalDate.of(2024, 1, 1));
    year.setToDate(LocalDate.of(2024, 12, 31));
    this.year = year;
  }

  private void givenGlobalBudgetFromStructure(
      Long budgetStructureImportId, Long budgetScenarioImportId) throws AxelorException {
    givenBudgetStructure(budgetStructureImportId);
    givenBudgetScenario(budgetScenarioImportId);
    givenBudgetGenerator();
    givenYear();

    budgetScenario.addYearSetItem(year);
    budgetGenerator.setBudgetScenario(budgetScenario);
    budgetGenerator.addYearSetItem(year);

    this.globalBudget = globalBudgetService.generateGlobalBudget(budgetGenerator, year);
  }
}
