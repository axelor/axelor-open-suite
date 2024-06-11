package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetStructure;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.apps.budget.module.BudgetTest;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.RoleRepository;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.inject.Beans;
import com.axelor.meta.loader.LoaderHelper;
import com.axelor.studio.db.AppBudget;
import com.axelor.studio.db.repo.AppBudgetRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoper;
import com.google.inject.servlet.ServletScopes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestBudgetToolsService extends BudgetTest {

  protected final GlobalBudgetRepository globalBudgetRepository;
  protected final BudgetRepository budgetRepository;
  protected final BudgetLevelRepository budgetLevelRepository;
  protected final UserRepository userRepository;
  protected final RoleRepository roleRepository;
  protected final AppBudgetRepository appBudgetRepository;
  protected final BudgetToolsService budgetToolsService;
  protected static final LoaderHelper loaderHelper = Beans.get(LoaderHelper.class);

  @Inject
  public TestBudgetToolsService(
      GlobalBudgetRepository globalBudgetRepository,
      BudgetRepository budgetRepository,
      BudgetLevelRepository budgetLevelRepository,
      UserRepository userRepository,
      RoleRepository roleRepository,
      AppBudgetRepository appBudgetRepository) {
    this.globalBudgetRepository = globalBudgetRepository;
    this.budgetRepository = budgetRepository;
    this.budgetLevelRepository = budgetLevelRepository;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.appBudgetRepository = appBudgetRepository;

    RequestScoper scope = ServletScopes.scopeRequest(Collections.emptyMap());
    try (RequestScoper.CloseableScope ignored = scope.open()) {
      this.budgetToolsService = Beans.get(BudgetToolsService.class);
    }
  }

  @BeforeAll
  static void setUp() {
    loaderHelper.importCsv("data/base-config.xml");
    loaderHelper.importCsv("data/budget-input.xml");
    loaderHelper.importCsv("data/budget-template-input.xml");
  }

  @Test
  void testCheckBudgetKeyAndRole() throws AxelorException {
    GlobalBudget globalBudget =
        globalBudgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1011L)
            .fetchOne();
    User user =
        userRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", "ADMIN")
            .fetchOne();
    Role roleAcc =
        roleRepository.all().filter("self.importId = :importId").bind("importId", "ACC").fetchOne();
    Role roleHR =
        roleRepository.all().filter("self.importId = :importId").bind("importId", "HR").fetchOne();
    AccountConfig accountConfig = globalBudget.getCompany().getAccountConfig();

    // If the user's roleSet and groupSet are empty, the method will return false
    Assertions.assertFalse(
        budgetToolsService.checkBudgetKeyAndRole(globalBudget.getCompany(), user));
    user.addRole(roleHR);

    // If the user's roleSet and groupSet doesn't contains any roles in the config's roleSet, the
    // method will return false
    Assertions.assertFalse(
        budgetToolsService.checkBudgetKeyAndRole(globalBudget.getCompany(), user));
    user.addRole(roleAcc);

    // If one of the config's roleSet are in the user's roles configured, the method will return
    // true
    Assertions.assertTrue(
        budgetToolsService.checkBudgetKeyAndRole(globalBudget.getCompany(), user));
    accountConfig.clearBudgetDistributionRoleSet();
    user.clearRoles();

    // If the config's roleSet is empty, the method will always return true
    Assertions.assertTrue(
        budgetToolsService.checkBudgetKeyAndRole(globalBudget.getCompany(), user));
  }

  @Test
  void testGetGlobalBudgetUsingBudget() {
    Budget budget =
        budgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1014L)
            .fetchOne();
    GlobalBudget globalBudget = budget.getGlobalBudget();

    Assertions.assertEquals(budgetToolsService.getGlobalBudgetUsingBudget(budget), globalBudget);
    budget.setGlobalBudget(null);
    Assertions.assertEquals(budgetToolsService.getGlobalBudgetUsingBudget(budget), globalBudget);
    budget.setBudgetLevel(null);
    Assertions.assertNull(budgetToolsService.getGlobalBudgetUsingBudget(budget));
  }

  @Test
  void testGetGlobalBudgetUsingBudgetLevel() {
    Budget budget =
        budgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1013L)
            .fetchOne();
    GlobalBudget globalBudget = budget.getGlobalBudget();
    BudgetLevel budgetLevel = budget.getBudgetLevel();

    Assertions.assertEquals(
        budgetToolsService.getGlobalBudgetUsingBudgetLevel(budgetLevel), globalBudget);
    budgetLevel.setParentBudgetLevel(null);
    Assertions.assertNull(budgetToolsService.getGlobalBudgetUsingBudgetLevel(budgetLevel));
  }

  @Test
  void testGetBudgetStructureUsingBudget() {
    BudgetLevel budgetLevel =
        budgetLevelRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1054L)
            .fetchOne();
    BudgetStructure budgetStructure = budgetLevel.getParentBudgetLevel().getBudgetStructure();

    Budget budget = new Budget("TEST", "TEST");
    budgetLevel.addBudgetListItem(budget);
    Assertions.assertEquals(
        budgetToolsService.getBudgetStructureUsingBudget(budget), budgetStructure);
    budget.setBudgetLevel(null);
    Assertions.assertNull(budgetToolsService.getBudgetStructureUsingBudget(budget));
  }

  @Test
  void testGetBudgetStructureUsingBudgetLevel() {
    BudgetLevel budgetLevel =
        budgetLevelRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1053L)
            .fetchOne();
    BudgetStructure budgetStructure = budgetLevel.getParentBudgetLevel().getBudgetStructure();

    Assertions.assertEquals(
        budgetToolsService.getBudgetStructureUsingBudgetLevel(budgetLevel), budgetStructure);
    budgetLevel.setParentBudgetLevel(null);
    Assertions.assertNull(budgetToolsService.getBudgetStructureUsingBudgetLevel(budgetLevel));
  }

  @Test
  void testCheckBudgetKeyInConfig() throws AxelorException {
    GlobalBudget globalBudget =
        globalBudgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1011L)
            .fetchOne();
    Company company = globalBudget.getCompany();

    Assertions.assertTrue(budgetToolsService.checkBudgetKeyInConfig(company));
    AccountConfig accountConfig = company.getAccountConfig();
    accountConfig.setEnableBudgetKey(false);
    Assertions.assertFalse(budgetToolsService.checkBudgetKeyInConfig(company));
  }

  @Test
  void testGetAvailableAmountOnBudget() {
    GlobalBudget globalBudget =
        globalBudgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1011L)
            .fetchOne();
    Budget budget = globalBudget.getBudgetList().get(0);

    Assertions.assertEquals(
        200, budgetToolsService.getAvailableAmountOnBudget(budget, LocalDate.now()).intValue());

    globalBudget.setCheckAvailableSelect(
        GlobalBudgetRepository.GLOBAL_BUDGET_AVAILABLE_AMOUNT_DEFAULT_VALUE);
    Assertions.assertEquals(
        200, budgetToolsService.getAvailableAmountOnBudget(budget, LocalDate.now()).intValue());

    globalBudget.setCheckAvailableSelect(
        GlobalBudgetRepository.GLOBAL_BUDGET_AVAILABLE_AMOUNT_BUDGET);
    Assertions.assertEquals(
        400, budgetToolsService.getAvailableAmountOnBudget(budget, LocalDate.now()).intValue());

    globalBudget.setCheckAvailableSelect(
        GlobalBudgetRepository.GLOBAL_BUDGET_AVAILABLE_AMOUNT_GLOBAL_BUDGET);
    Assertions.assertEquals(
        4300, budgetToolsService.getAvailableAmountOnBudget(budget, LocalDate.now()).intValue());
  }

  @Test
  void testGetBudgetControlLevel() {
    Budget budget =
        budgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1012L)
            .fetchOne();

    GlobalBudget globalBudget = budget.getGlobalBudget();
    globalBudget.setCheckAvailableSelect(
        GlobalBudgetRepository.GLOBAL_BUDGET_AVAILABLE_AMOUNT_DEFAULT_VALUE);
    Assertions.assertEquals(
        budgetToolsService.getBudgetControlLevel(budget),
        GlobalBudgetRepository.GLOBAL_BUDGET_AVAILABLE_AMOUNT_BUDGET_LINE);

    globalBudget.setCheckAvailableSelect(
        GlobalBudgetRepository.GLOBAL_BUDGET_AVAILABLE_AMOUNT_GLOBAL_BUDGET);
    Assertions.assertEquals(
        budgetToolsService.getBudgetControlLevel(budget),
        GlobalBudgetRepository.GLOBAL_BUDGET_AVAILABLE_AMOUNT_GLOBAL_BUDGET);

    disableCheckAvailableBudget();
    Assertions.assertNull(budgetToolsService.getBudgetControlLevel(budget));
  }

  @Test
  void testGetBudgetRemainingAmountToAllocate() {
    Budget budget =
        budgetRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", 1011L)
            .fetchOne();
    List<BudgetDistribution> budgetDistributionList = new ArrayList<>();
    Assertions.assertEquals(
        400,
        budgetToolsService
            .getBudgetRemainingAmountToAllocate(budgetDistributionList, budget.getAvailableAmount())
            .intValue());
    fillBudgetDistributionList(budgetDistributionList, budget, new BigDecimal(100));
    Assertions.assertEquals(
        300,
        budgetToolsService
            .getBudgetRemainingAmountToAllocate(budgetDistributionList, budget.getAvailableAmount())
            .intValue());
    fillBudgetDistributionList(budgetDistributionList, budget, new BigDecimal(350));
    Assertions.assertEquals(
        0,
        budgetToolsService
            .getBudgetRemainingAmountToAllocate(budgetDistributionList, budget.getAvailableAmount())
            .intValue());
  }

  @Transactional
  protected void disableCheckAvailableBudget() {
    AppBudget appBudget = appBudgetRepository.all().fetchOne();
    appBudget.setCheckAvailableBudget(false);
    appBudgetRepository.save(appBudget);
  }

  protected void fillBudgetDistributionList(
      List<BudgetDistribution> budgetDistributionList, Budget budget, BigDecimal amount) {
    BudgetDistribution budgetDistribution = new BudgetDistribution();
    budgetDistribution.setBudget(budget);
    budgetDistribution.setAmount(amount);
    budgetDistributionList.add(budgetDistribution);
  }
}
