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
package com.axelor.apps.budget.service.saleorderline;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineBudgetServiceImpl implements SaleOrderLineBudgetService {

  protected BudgetService budgetService;
  protected BudgetDistributionService budgetDistributionService;
  protected SaleOrderLineRepository saleOrderLineRepo;
  protected AppBudgetService appBudgetService;
  protected BudgetToolsService budgetToolsService;
  protected AccountManagementAccountService accountManagementAccountService;

  @Inject
  public SaleOrderLineBudgetServiceImpl(
      BudgetService budgetService,
      BudgetDistributionService budgetDistributionService,
      SaleOrderLineRepository saleOrderLineRepo,
      AppBudgetService appBudgetService,
      BudgetToolsService budgetToolsService,
      AccountManagementAccountService accountManagementAccountService) {
    this.budgetService = budgetService;
    this.budgetDistributionService = budgetDistributionService;
    this.saleOrderLineRepo = saleOrderLineRepo;
    this.appBudgetService = appBudgetService;
    this.budgetToolsService = budgetToolsService;
    this.accountManagementAccountService = accountManagementAccountService;
  }

  @Override
  @Transactional
  public String computeBudgetDistribution(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    if (saleOrder == null || saleOrderLine == null) {
      return "";
    }
    saleOrderLine.clearBudgetDistributionList();
    saleOrderLine.setBudgetStr("");
    String alertMessage =
        budgetDistributionService.createBudgetDistribution(
            saleOrderLine.getAnalyticMoveLineList(),
            saleOrderLine.getAccount(),
            saleOrder.getCompany(),
            saleOrder.getOrderDate() != null
                ? saleOrder.getOrderDate()
                : saleOrder.getCreationDate(),
            saleOrderLine.getCompanyExTaxTotal(),
            saleOrderLine.getFullName(),
            saleOrderLine);

    fillBudgetStrOnLine(saleOrderLine, true);
    saleOrderLineRepo.save(saleOrderLine);
    return alertMessage;
  }

  @Override
  @Transactional
  public void fillBudgetStrOnLine(SaleOrderLine saleOrderLine, boolean multiBudget) {
    saleOrderLine.setBudgetStr(this.searchAndFillBudgetStr(saleOrderLine, multiBudget));
    saleOrderLineRepo.save(saleOrderLine);
  }

  @Override
  public String searchAndFillBudgetStr(SaleOrderLine saleOrderLine, boolean multiBudget) {
    String budgetStr = "";
    if (!multiBudget && saleOrderLine.getBudget() != null) {
      budgetStr = saleOrderLine.getBudget().getFullName();
    } else if (multiBudget && !CollectionUtils.isEmpty(saleOrderLine.getBudgetDistributionList())) {
      List<Budget> budgetList = new ArrayList();
      for (BudgetDistribution budgetDistribution : saleOrderLine.getBudgetDistributionList()) {
        budgetList.add(budgetDistribution.getBudget());
      }
      budgetStr = budgetList.stream().map(b -> b.getFullName()).collect(Collectors.joining(" - "));
    }
    return budgetStr;
  }

  @Override
  public String getBudgetDomain(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Company company = null;
    LocalDate date = null;
    Set<GlobalBudget> globalBudgetSet = new HashSet<>();
    if (saleOrder != null) {
      if (saleOrder.getCompany() != null) {
        company = saleOrder.getCompany();
      }
      date =
          saleOrder.getOrderDate() != null ? saleOrder.getOrderDate() : saleOrder.getCreationDate();

      if (saleOrder.getProject() != null
          && !ObjectUtils.isEmpty(saleOrder.getProject().getGlobalBudgetSet())) {
        globalBudgetSet = saleOrder.getProject().getGlobalBudgetSet();
      }
    }

    return budgetDistributionService.getBudgetDomain(
        company,
        date,
        AccountTypeRepository.TYPE_INCOME,
        saleOrderLine.getAccount(),
        globalBudgetSet);
  }

  @Override
  public void checkAmountForSaleOrderLine(SaleOrderLine saleOrderLine) throws AxelorException {
    if (saleOrderLine.getBudgetDistributionList() != null
        && !saleOrderLine.getBudgetDistributionList().isEmpty()) {
      BigDecimal totalAmount = BigDecimal.ZERO;
      for (BudgetDistribution budgetDistribution : saleOrderLine.getBudgetDistributionList()) {
        if (budgetDistribution.getAmount().compareTo(saleOrderLine.getCompanyExTaxTotal()) > 0) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(BudgetExceptionMessage.BUDGET_DISTRIBUTION_LINE_SUM_GREATER_PO),
              budgetDistribution.getBudget().getCode(),
              saleOrderLine.getProductName());
        } else {
          totalAmount = totalAmount.add(budgetDistribution.getAmount());
        }
      }
      if (totalAmount.compareTo(saleOrderLine.getCompanyExTaxTotal()) > 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BudgetExceptionMessage.BUDGET_DISTRIBUTION_LINE_SUM_LINES_GREATER_PO),
            saleOrderLine.getProductName());
      }
    }
  }

  @Override
  public Map<String, Object> setProductAccount(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    Product product = saleOrderLine.getProduct();

    if (product == null) {
      values.put("account", null);
      saleOrderLine.setAccount(null);
    } else if (saleOrder != null) {
      Account account =
          accountManagementAccountService.getProductAccount(
              saleOrderLine.getProduct(),
              saleOrder.getCompany(),
              saleOrder.getFiscalPosition(),
              false,
              false);
      if (account.getCode().startsWith("2")
          || account.getCode().startsWith("4")
          || account.getCode().startsWith("7")) {
        values.put("account", account);
        saleOrderLine.setAccount(account);
      }
    }
    return values;
  }

  @Override
  public Map<String, Object> resetBudget(SaleOrderLine saleOrderLine) {
    Map<String, Object> values = new HashMap<>();
    BigDecimal budgetRemainingAmountToAllocate = saleOrderLine.getCompanyExTaxTotal();
    saleOrderLine.setBudgetRemainingAmountToAllocate(budgetRemainingAmountToAllocate);
    saleOrderLine.setBudgetDistributionList(new ArrayList<>());
    saleOrderLine.setBudget(null);
    values.put("budgetRemainingAmountToAllocate", budgetRemainingAmountToAllocate);
    values.put("budgetDistributionList", new ArrayList<>());
    values.put("budget", null);
    return values;
  }
}
