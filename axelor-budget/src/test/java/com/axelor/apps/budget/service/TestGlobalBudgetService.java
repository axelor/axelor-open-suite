package com.axelor.apps.budget.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetGenerator;
import com.axelor.apps.budget.db.BudgetScenario;
import com.axelor.apps.budget.db.BudgetStructure;
import com.axelor.apps.budget.db.BudgetVersion;
import com.axelor.apps.budget.db.GlobalBudget;
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
  protected final GlobalBudgetService globalBudgetService;
  protected final BudgetVersionService budgetVersionService;
  protected static final LoaderHelper loaderHelper = Beans.get(LoaderHelper.class);

  @Inject
  public TestGlobalBudgetService(
      GlobalBudgetRepository globalBudgetRepository,
      BudgetRepository budgetRepository,
      BudgetStructureRepository budgetStructureRepository,
      BudgetScenarioRepository budgetScenarioRepository,
      BudgetVersionService budgetVersionService) {
    this.globalBudgetRepository = globalBudgetRepository;
    this.budgetRepository = budgetRepository;
    this.budgetStructureRepository = budgetStructureRepository;
    this.budgetScenarioRepository = budgetScenarioRepository;
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
    loaderHelper.importCsv("data/budget-input.xml");
    loaderHelper.importCsv("data/budget-template-input.xml");
  }

  @Test
  void testUpdateGlobalBudgetDates() throws AxelorException {
    GlobalBudget globalBudget =
        globalBudgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1011L)
            .fetchOne();
    globalBudget.setFromDate(LocalDate.of(2024, 1, 1));
    globalBudgetService.updateGlobalBudgetDates(globalBudget);
    Assertions.assertEquals(
        globalBudget.getBudgetList().get(0).getFromDate(), LocalDate.of(2024, 1, 1));
  }

  @Test
  void testValidateDates() {
    GlobalBudget globalBudget =
        globalBudgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1011L)
            .fetchOne();
    globalBudget.setToDate(LocalDate.of(2024, 1, 1));
    Assertions.assertThrows(
        AxelorException.class, () -> globalBudgetService.validateDates(globalBudget));
  }

  @Test
  void testComputeBudgetLevelTotals() {
    Budget budget =
        budgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1011L)
            .fetchOne();
    budget.setTotalAmountExpected(new BigDecimal(1000));
    globalBudgetService.computeBudgetLevelTotals(budget);

    Assertions.assertEquals(4900, budget.getGlobalBudget().getTotalAmountExpected().intValue());
    Assertions.assertEquals(1500, budget.getBudgetLevel().getTotalAmountExpected().intValue());
  }

  @Test
  void testChangeBudgetVersion() throws AxelorException {
    GlobalBudget globalBudget =
        globalBudgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1011L)
            .fetchOne();

    Assertions.assertNull(
        globalBudgetService.changeBudgetVersion(globalBudget, null, false).getActiveVersion());
    BudgetVersion budgetVersion = budgetVersionService.createNewVersion(globalBudget, "version");
    globalBudget = globalBudgetService.changeBudgetVersion(globalBudget, budgetVersion, false);
    Assertions.assertNotNull(globalBudget.getActiveVersion());
    Assertions.assertEquals(
        globalBudget.getActiveVersion().getVersionExpectedAmountsLineList().size(), 4);
  }

  @Test
  void testGenerateGlobalBudget() throws AxelorException {
    BudgetStructure budgetStructure =
        budgetStructureRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1001L)
            .fetchOne();
    BudgetScenario budgetScenario =
        budgetScenarioRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1L)
            .fetchOne();

    BudgetGenerator budgetGenerator = new BudgetGenerator();
    budgetGenerator.setBudgetStructure(budgetStructure);
    budgetGenerator.setCode("GENERATOR");
    budgetGenerator.setName("GENERATOR");

    Year year = new Year();
    year.setCode("2024");
    year.setName("2024");
    year.setFromDate(LocalDate.of(2024, 1, 1));
    year.setToDate(LocalDate.of(2024, 12, 31));

    budgetScenario.addYearSetItem(year);
    budgetGenerator.setBudgetScenario(budgetScenario);
    budgetGenerator.addYearSetItem(year);

    GlobalBudget globalBudget = globalBudgetService.generateGlobalBudget(budgetGenerator, year);
    Assertions.assertNotNull(globalBudget);
  }
}
