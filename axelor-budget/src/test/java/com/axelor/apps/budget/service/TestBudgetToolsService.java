/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.base.AxelorException;
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
  private GlobalBudget globalBudget;
  private Budget budget;
  private BudgetLevel budgetLevel;
  private Role role;
  private User user;

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
    final LoaderHelper loaderHelper = Beans.get(LoaderHelper.class);
    loaderHelper.importCsv("data/base-config.xml");
    loaderHelper.importCsv("data/budget-input.xml");
    loaderHelper.importCsv("data/budget-template-input.xml");
  }

  // If the user's roleSet and groupSet are empty, the method will return false
  @Test
  void testCheckBudgetKeyWithoutUserRoles() throws AxelorException {
    givenGlobalRoleAndUser(1011L, "", "ADMIN");
    Assertions.assertFalse(
        budgetToolsService.checkBudgetKeyAndRole(globalBudget.getCompany(), user));
  }

  // If the user's roleSet and groupSet doesn't contains any roles in the config's roleSet, the
  // method will return false
  @Test
  void testCheckBudgetKeyWithWrongRoles() throws AxelorException {
    givenGlobalRoleAndUser(1011L, "HR", "ADMIN");
    Assertions.assertFalse(
        budgetToolsService.checkBudgetKeyAndRole(globalBudget.getCompany(), user));
  }

  // If one of the config's roleSet are in the user's roles configured, the method will return
  // true
  @Test
  void testCheckBudgetKeyWithRoles() throws AxelorException {
    givenGlobalRoleAndUser(1011L, "ACC", "ADMIN");
    Assertions.assertTrue(
        budgetToolsService.checkBudgetKeyAndRole(globalBudget.getCompany(), user));
  }

  // If the config's roleSet is empty, the method will always return true
  @Test
  void testCheckBudgetKeyWithoutRoles() throws AxelorException {
    givenGlobalRoleAndUser(1011L, "ACC", "ADMIN");
    AccountConfig accountConfig = globalBudget.getCompany().getAccountConfig();
    accountConfig.clearBudgetDistributionRoleSet();
    user.clearRoles();
    Assertions.assertTrue(
        budgetToolsService.checkBudgetKeyAndRole(globalBudget.getCompany(), user));
    accountConfig.addBudgetDistributionRoleSetItem(role);
  }

  @Test
  void testGetGlobalBudgetUsingBudgetWithLinks() {
    givenBudgetAndGlobalBudget(1014L, 1011L);
    Assertions.assertEquals(budgetToolsService.getGlobalBudgetUsingBudget(budget), globalBudget);
  }

  @Test
  void testGetGlobalBudgetUsingBudgetWithoutGlobal() {
    givenBudgetAndGlobalBudget(1014L, 1011L);
    budget.setGlobalBudget(null);
    Assertions.assertEquals(budgetToolsService.getGlobalBudgetUsingBudget(budget), globalBudget);
  }

  @Test
  void testGetGlobalBudgetUsingBudgetWithoutLinks() {
    givenBudgetAndGlobalBudget(1014L, 1011L);
    budget.setGlobalBudget(null);
    budget.setBudgetLevel(null);
    Assertions.assertNull(budgetToolsService.getGlobalBudgetUsingBudget(budget));
  }

  @Test
  void testGetGlobalBudgetUsingBudgetLevelWithLinks() {
    givenBudgetGlobalAndLevel(1013L, 1011L, 1015L);
    Assertions.assertEquals(
        budgetToolsService.getGlobalBudgetUsingBudgetLevel(budgetLevel), globalBudget);
  }

  @Test
  void testGetGlobalBudgetUsingBudgetLevelWithoutLinks() {
    givenBudgetGlobalAndLevel(1013L, 1011L, 1015L);
    BudgetLevel parent = budgetLevel.getParentBudgetLevel();
    budgetLevel.setParentBudgetLevel(null);
    Assertions.assertNull(budgetToolsService.getGlobalBudgetUsingBudgetLevel(budgetLevel));
    budgetLevel.setParentBudgetLevel(parent);
  }

  @Test
  void testGetBudgetStructureUsingBudgetWithLinks() {
    givenBudgetLevel(1054L);
    BudgetStructure budgetStructure = budgetLevel.getParentBudgetLevel().getBudgetStructure();
    Budget budget = new Budget("TEST", "TEST");
    budgetLevel.addBudgetListItem(budget);
    Assertions.assertEquals(
        budgetToolsService.getBudgetStructureUsingBudget(budget), budgetStructure);
  }

  @Test
  void testGetBudgetStructureUsingBudgetWithoutLinks() {
    Budget budget = new Budget("TEST", "TEST");
    budget.setBudgetLevel(null);
    Assertions.assertNull(budgetToolsService.getBudgetStructureUsingBudget(budget));
  }

  @Test
  void testGetBudgetStructureUsingBudgetLevelWithLinks() {
    givenBudgetLevel(1053L);
    BudgetStructure budgetStructure = budgetLevel.getParentBudgetLevel().getBudgetStructure();
    Assertions.assertEquals(
        budgetToolsService.getBudgetStructureUsingBudgetLevel(budgetLevel), budgetStructure);
  }

  @Test
  void testGetBudgetStructureUsingBudgetLevelWithoutLinks() {
    givenBudgetLevel(1053L);
    budgetLevel.setParentBudgetLevel(null);
    Assertions.assertNull(budgetToolsService.getBudgetStructureUsingBudgetLevel(budgetLevel));
  }

  @Test
  void testCheckBudgetKeyInConfigWithConfig() throws AxelorException {
    givenGlobalBudget(1011L);
    Assertions.assertTrue(budgetToolsService.checkBudgetKeyInConfig(globalBudget.getCompany()));
  }

  @Test
  void testCheckBudgetKeyInConfigWithoutConfig() throws AxelorException {
    givenGlobalBudget(1011L);
    AccountConfig accountConfig = globalBudget.getCompany().getAccountConfig();
    accountConfig.setEnableBudgetKey(false);
    Assertions.assertFalse(budgetToolsService.checkBudgetKeyInConfig(globalBudget.getCompany()));
    accountConfig.setEnableBudgetKey(true);
  }

  @Test
  void testGetAvailableAmountOnBudgetDefault() {
    givenGetAvailableAmount(
        1011L, GlobalBudgetRepository.GLOBAL_BUDGET_AVAILABLE_AMOUNT_DEFAULT_VALUE);
    Assertions.assertEquals(
        200, budgetToolsService.getAvailableAmountOnBudget(budget, LocalDate.now()).intValue());
  }

  @Test
  void testGetAvailableAmountOnBudgetBudget() {
    givenGetAvailableAmount(1011L, GlobalBudgetRepository.GLOBAL_BUDGET_AVAILABLE_AMOUNT_BUDGET);
    Assertions.assertEquals(
        400, budgetToolsService.getAvailableAmountOnBudget(budget, LocalDate.now()).intValue());
  }

  @Test
  void testGetAvailableAmountOnBudgetGlobal() {
    givenGetAvailableAmount(
        1011L, GlobalBudgetRepository.GLOBAL_BUDGET_AVAILABLE_AMOUNT_GLOBAL_BUDGET);
    Assertions.assertEquals(
        4300, budgetToolsService.getAvailableAmountOnBudget(budget, LocalDate.now()).intValue());
  }

  @Test
  void testGetBudgetControlLevelDefault() {
    givenGetAvailableAmount(
        1012L, GlobalBudgetRepository.GLOBAL_BUDGET_AVAILABLE_AMOUNT_DEFAULT_VALUE);
    Assertions.assertEquals(
        budgetToolsService.getBudgetControlLevel(budget),
        GlobalBudgetRepository.GLOBAL_BUDGET_AVAILABLE_AMOUNT_BUDGET_LINE);
  }

  @Test
  void testGetBudgetControlLevelGlobal() {
    givenGetAvailableAmount(
        1012L, GlobalBudgetRepository.GLOBAL_BUDGET_AVAILABLE_AMOUNT_GLOBAL_BUDGET);
    Assertions.assertEquals(
        budgetToolsService.getBudgetControlLevel(budget),
        GlobalBudgetRepository.GLOBAL_BUDGET_AVAILABLE_AMOUNT_GLOBAL_BUDGET);
  }

  @Test
  void testGetBudgetControlLevelDisabled() {
    givenGetAvailableAmount(
        1012L, GlobalBudgetRepository.GLOBAL_BUDGET_AVAILABLE_AMOUNT_GLOBAL_BUDGET);
    changeCheckAvailableBudget(false);
    Assertions.assertNull(budgetToolsService.getBudgetControlLevel(budget));
  }

  @Test
  void testGetBudgetRemainingAmountToAllocateDefault() {
    List<BudgetDistribution> budgetDistributionList = givenBudgetAndBudgetDistribution(1011L, 0);
    Assertions.assertEquals(
        400,
        budgetToolsService
            .getBudgetRemainingAmountToAllocate(budgetDistributionList, budget.getAvailableAmount())
            .intValue());
  }

  @Test
  void testGetBudgetRemainingAmountToAllocateSubtract() {
    List<BudgetDistribution> budgetDistributionList = givenBudgetAndBudgetDistribution(1011L, 100);
    Assertions.assertEquals(
        300,
        budgetToolsService
            .getBudgetRemainingAmountToAllocate(budgetDistributionList, budget.getAvailableAmount())
            .intValue());
  }

  @Test
  void testGetBudgetRemainingAmountToAllocateMax() {
    List<BudgetDistribution> budgetDistributionList = givenBudgetAndBudgetDistribution(1011L, 450);
    Assertions.assertEquals(
        0,
        budgetToolsService
            .getBudgetRemainingAmountToAllocate(budgetDistributionList, budget.getAvailableAmount())
            .intValue());
  }

  @Transactional
  protected void changeCheckAvailableBudget(boolean checkAvailableBudget) {
    AppBudget appBudget = appBudgetRepository.all().fetchOne();
    if (appBudget.getCheckAvailableBudget() != checkAvailableBudget) {
      appBudget.setCheckAvailableBudget(checkAvailableBudget);
      appBudgetRepository.save(appBudget);
    }
  }

  protected void fillBudgetDistributionList(
      List<BudgetDistribution> budgetDistributionList, Budget budget, BigDecimal amount) {
    BudgetDistribution budgetDistribution = new BudgetDistribution();
    budgetDistribution.setBudget(budget);
    budgetDistribution.setAmount(amount);
    budgetDistributionList.add(budgetDistribution);
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

  private void givenBudgetGlobalAndLevel(
      Long budgetImportId, Long globalBudgetImportId, Long budgetLevelImportId) {
    givenGlobalBudget(globalBudgetImportId);
    givenBudget(budgetImportId);
    givenBudgetLevel(budgetLevelImportId);
  }

  private void givenGetAvailableAmount(Long budgetImportId, Integer availableCheck) {
    changeCheckAvailableBudget(true);
    givenBudget(budgetImportId);
    givenGlobalBudget(1011L);
    globalBudget.setCheckAvailableSelect(availableCheck);
  }

  private List<BudgetDistribution> givenBudgetAndBudgetDistribution(
      Long importId, int distributionAmount) {
    givenBudget(importId);
    List<BudgetDistribution> budgetDistributionList = new ArrayList<>();
    fillBudgetDistributionList(budgetDistributionList, budget, new BigDecimal(distributionAmount));
    return budgetDistributionList;
  }

  private void givenGlobalRoleAndUser(
      Long globalImportId, String roleImportId, String userImportId) {
    givenGlobalBudget(globalImportId);
    this.role =
        roleRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", roleImportId)
            .fetchOne();
    User user =
        userRepository
            .all()
            .filter("self.importId = :importId")
            .bind("importId", userImportId)
            .fetchOne();
    user.clearRoles();
    if (role != null) {
      user.addRole(role);
    }
    this.user = user;
  }
}
