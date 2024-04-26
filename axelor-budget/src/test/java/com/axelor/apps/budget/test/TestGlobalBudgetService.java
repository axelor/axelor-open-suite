package com.axelor.apps.budget.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetGenerator;
import com.axelor.apps.budget.db.BudgetScenario;
import com.axelor.apps.budget.db.BudgetStructure;
import com.axelor.apps.budget.db.BudgetVersion;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetLevelManagementRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.db.repo.BudgetScenarioRepository;
import com.axelor.apps.budget.db.repo.BudgetStructureRepository;
import com.axelor.apps.budget.db.repo.BudgetVersionRepository;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.apps.budget.service.BudgetLevelService;
import com.axelor.apps.budget.service.BudgetScenarioLineService;
import com.axelor.apps.budget.service.BudgetScenarioService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.BudgetVersionService;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetServiceImpl;
import com.axelor.apps.budget.service.globalbudget.GlobalBudgetToolsService;
import com.axelor.meta.loader.LoaderHelper;
import com.axelor.utils.junit.BaseTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

class TestGlobalBudgetService extends BaseTest {

  private static GlobalBudgetServiceImpl globalBudgetService;
  private static BudgetVersionService budgetVersionService;
  private static BudgetRepository budgetRepository;
  private static GlobalBudgetRepository globalBudgetRepository;
  private static BudgetScenarioRepository budgetScenarioRepository;
  private static BudgetStructureRepository budgetStructureRepository;
  private static LoaderHelper loaderHelper;

  @BeforeAll
  static void prepare() {

    budgetRepository = mock(BudgetRepository.class);
    globalBudgetRepository = mock(GlobalBudgetRepository.class);
    budgetScenarioRepository = mock(BudgetScenarioRepository.class);
    budgetStructureRepository = mock(BudgetStructureRepository.class);
    BudgetLevelService budgetLevelService = mock(BudgetLevelService.class);
    BudgetService budgetService = mock(BudgetService.class);
    BudgetRepository budgetRepository = mock(BudgetRepository.class);
    BudgetLevelManagementRepository budgetLevelManagementRepository =
        mock(BudgetLevelManagementRepository.class);
    BudgetVersionRepository budgetVersionRepo = mock(BudgetVersionRepository.class);
    BudgetScenarioService budgetScenarioService = mock(BudgetScenarioService.class);
    BudgetToolsService budgetToolsService = mock(BudgetToolsService.class);
    GlobalBudgetToolsService globalBudgetToolsService = mock(GlobalBudgetToolsService.class);
    BudgetScenarioLineService budgetScenarioLineService = mock(BudgetScenarioLineService.class);
    CurrencyScaleService currencyScaleService = mock(CurrencyScaleService.class);
    budgetVersionService = mock(BudgetVersionService.class);
    loaderHelper = mock(LoaderHelper.class);

    globalBudgetService =
        new GlobalBudgetServiceImpl(
            budgetLevelService,
            globalBudgetRepository,
            budgetService,
            budgetRepository,
            budgetLevelManagementRepository,
            budgetVersionRepo,
            budgetScenarioService,
            budgetToolsService,
            globalBudgetToolsService,
            budgetScenarioLineService,
            currencyScaleService);

    prepareGlobalBudgetRepository();
  }

  protected static void prepareGlobalBudgetRepository() {
    when(globalBudgetRepository.save(any(GlobalBudget.class)))
        .then((Answer<GlobalBudget>) invocation -> (GlobalBudget) invocation.getArguments()[0]);
  }

  @BeforeEach
  void setUp() {
    loaderHelper.importCsv("data/budget-input.xml");
  }

  @Test
  void testComputeBudgetLevelTotals() {
    Budget budget =
        budgetRepository.all().filter("self.importId = :importId").bind("importId", 11L).fetchOne();
    budget.setTotalAmountExpected(new BigDecimal(1000));
    globalBudgetService.computeBudgetLevelTotals(budget);
    Assertions.assertTrue(
        new BigDecimal(4900).compareTo(budget.getGlobalBudget().getTotalAmountExpected()) == 0);
    Assertions.assertTrue(
        new BigDecimal(1500).compareTo(budget.getBudgetLevel().getTotalAmountExpected()) == 0);
  }

  @Test
  void testChangeBudgetVersion() throws AxelorException {
    GlobalBudget globalBudget =
        globalBudgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 11L)
            .fetchOne();

    Assertions.assertNull(
        globalBudgetService.changeBudgetVersion(globalBudget, null, false).getActiveVersion());
    BudgetVersion budgetVersion = budgetVersionService.createNewVersion(globalBudget, "version");
    Assertions.assertNotNull(
        globalBudgetService
            .changeBudgetVersion(globalBudget, budgetVersion, false)
            .getActiveVersion());
  }

  @Test
  void testUpdateGlobalBudgetDates() throws AxelorException {
    GlobalBudget globalBudget =
        globalBudgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 11L)
            .fetchOne();
    globalBudget.setFromDate(LocalDate.of(2024, 1, 1));
    globalBudgetService.updateGlobalBudgetDates(globalBudget);
    Assertions.assertEquals(
        globalBudget.getBudgetList().get(0).getFromDate(), LocalDate.of(2024, 1, 1));
  }

  @Test
  void testGenerateGlobalBudget() throws AxelorException {
    BudgetStructure budgetStructure =
        budgetStructureRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1L)
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
    year.setFromDate(LocalDate.of(2024, 1, 1));
    year.setToDate(LocalDate.of(2024, 12, 31));

    budgetScenario.addYearSetItem(year);
    budgetGenerator.setBudgetScenario(budgetScenario);
    budgetGenerator.addYearSetItem(year);

    GlobalBudget globalBudget = globalBudgetService.generateGlobalBudget(budgetGenerator, year);
    Assertions.assertNotNull(globalBudget);
  }
}
