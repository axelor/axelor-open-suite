/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineBudgetServiceImpl implements SaleOrderLineBudgetService {

  protected BudgetService budgetService;
  protected BudgetDistributionService budgetDistributionService;
  protected SaleOrderLineRepository saleOrderLineRepo;
  protected AppBudgetService appBudgetService;
  protected BudgetToolsService budgetToolsService;

  @Inject
  public SaleOrderLineBudgetServiceImpl(
      BudgetService budgetService,
      BudgetDistributionService budgetDistributionService,
      SaleOrderLineRepository saleOrderLineRepo,
      AppBudgetService appBudgetService,
      BudgetToolsService budgetToolsService) {
    this.budgetService = budgetService;
    this.budgetDistributionService = budgetDistributionService;
    this.saleOrderLineRepo = saleOrderLineRepo;
    this.appBudgetService = appBudgetService;
    this.budgetToolsService = budgetToolsService;
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

  @Transactional
  @Override
  public List<BudgetDistribution> addBudgetDistribution(SaleOrderLine saleOrderLine) {
    List<BudgetDistribution> budgetDistributionList = new ArrayList<>();
    if (appBudgetService.getAppBudget() != null
        && !appBudgetService.getAppBudget().getManageMultiBudget()
        && saleOrderLine.getBudget() != null) {
      BudgetDistribution budgetDistribution = new BudgetDistribution();
      budgetDistribution.setBudget(saleOrderLine.getBudget());
      LocalDate date = null;
      if (saleOrderLine.getSaleOrder() != null) {
        date =
            saleOrderLine.getSaleOrder().getOrderDate() != null
                ? saleOrderLine.getSaleOrder().getOrderDate()
                : saleOrderLine.getSaleOrder().getCreationDate();
      }

      budgetDistribution.setBudgetAmountAvailable(
          budgetToolsService.getAvailableAmountOnBudget(saleOrderLine.getBudget(), date));
      budgetDistribution.setAmount(saleOrderLine.getExTaxTotal());
      budgetDistributionList.add(budgetDistribution);
      saleOrderLine.setBudgetDistributionList(budgetDistributionList);
    }
    return budgetDistributionList;
  }

  @Override
  public String getBudgetDomain(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Company company = null;
    LocalDate date = null;
    if (saleOrder != null) {
      if (saleOrder.getCompany() != null) {
        company = saleOrder.getCompany();
      }
      date =
          saleOrder.getOrderDate() != null ? saleOrder.getOrderDate() : saleOrder.getCreationDate();
    }

    return budgetDistributionService.getBudgetDomain(
        company, date, AccountTypeRepository.TYPE_INCOME);
  }

  @Override
  public void checkAmountForSaleOrderLine(SaleOrderLine saleOrderLine) throws AxelorException {
    if (saleOrderLine.getBudgetDistributionList() != null
        && !saleOrderLine.getBudgetDistributionList().isEmpty()) {
      for (BudgetDistribution budgetDistribution : saleOrderLine.getBudgetDistributionList()) {
        if (budgetDistribution.getAmount().compareTo(saleOrderLine.getCompanyExTaxTotal()) > 0) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(BudgetExceptionMessage.BUDGET_DISTRIBUTION_LINE_SUM_GREATER_PO),
              budgetDistribution.getBudget().getCode(),
              saleOrderLine.getProductName());
        }
      }
    }
  }

  @Override
  public void computeBudgetDistributionSumAmount(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    List<BudgetDistribution> budgetDistributionList = saleOrderLine.getBudgetDistributionList();
    BigDecimal budgetDistributionSumAmount = BigDecimal.ZERO;
    LocalDate computeDate =
        saleOrder.getOrderDate() != null ? saleOrder.getOrderDate() : saleOrder.getCreationDate();

    if (budgetDistributionList != null && !budgetDistributionList.isEmpty()) {

      for (BudgetDistribution budgetDistribution : budgetDistributionList) {
        budgetDistributionSumAmount =
            budgetDistributionSumAmount.add(budgetDistribution.getAmount());
        budgetDistributionService.computeBudgetDistributionSumAmount(
            budgetDistribution, computeDate);
      }
    }
    saleOrderLine.setBudgetDistributionSumAmount(budgetDistributionSumAmount);
  }
}
