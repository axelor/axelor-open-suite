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
package com.axelor.apps.budget.service.purchaseorder;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class PurchaseOrderLineBudgetServiceImpl implements PurchaseOrderLineBudgetService {
  protected BudgetService budgetService;
  protected BudgetRepository budgetRepository;
  protected BudgetDistributionService budgetDistributionService;
  protected PurchaseOrderLineRepository purchaseOrderLineRepo;
  protected AppBudgetService appBudgetService;
  protected BudgetToolsService budgetToolsService;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public PurchaseOrderLineBudgetServiceImpl(
      BudgetService budgetService,
      BudgetRepository budgetRepository,
      BudgetDistributionService budgetDistributionService,
      PurchaseOrderLineRepository purchaseOrderLineRepo,
      AppBudgetService appBudgetService,
      BudgetToolsService budgetToolsService,
      CurrencyScaleService currencyScaleService) {
    this.budgetService = budgetService;
    this.budgetRepository = budgetRepository;
    this.budgetDistributionService = budgetDistributionService;
    this.purchaseOrderLineRepo = purchaseOrderLineRepo;
    this.appBudgetService = appBudgetService;
    this.budgetToolsService = budgetToolsService;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  @Transactional
  public String computeBudgetDistribution(
      PurchaseOrder purchaseOrder, PurchaseOrderLine purchaseOrderLine) throws AxelorException {
    if (purchaseOrder == null || purchaseOrderLine == null) {
      return "";
    }
    purchaseOrderLine.clearBudgetDistributionList();
    purchaseOrderLine.setBudgetStr("");
    String alertMessage =
        budgetDistributionService.createBudgetDistribution(
            purchaseOrderLine.getAnalyticMoveLineList(),
            purchaseOrderLine.getAccount(),
            purchaseOrder.getCompany(),
            purchaseOrder.getOrderDate(),
            purchaseOrderLine.getCompanyExTaxTotal(),
            purchaseOrderLine.getFullName(),
            purchaseOrderLine);
    fillBudgetStrOnLine(purchaseOrderLine, true);
    purchaseOrderLineRepo.save(purchaseOrderLine);
    return alertMessage;
  }

  @Override
  @Transactional
  public void fillBudgetStrOnLine(PurchaseOrderLine purchaseOrderLine, boolean multiBudget) {
    purchaseOrderLine.setBudgetStr(this.searchAndFillBudgetStr(purchaseOrderLine, multiBudget));
    purchaseOrderLineRepo.save(purchaseOrderLine);
  }

  @Override
  public String searchAndFillBudgetStr(PurchaseOrderLine purchaseOrderLine, boolean multiBudget) {
    String budgetStr = "";
    if (!multiBudget && purchaseOrderLine.getBudget() != null) {
      budgetStr = purchaseOrderLine.getBudget().getFullName();
    } else if (multiBudget
        && !CollectionUtils.isEmpty(purchaseOrderLine.getBudgetDistributionList())) {
      List<Budget> budgetList = new ArrayList();
      for (BudgetDistribution budgetDistribution : purchaseOrderLine.getBudgetDistributionList()) {
        budgetList.add(budgetDistribution.getBudget());
      }
      budgetStr = budgetList.stream().map(b -> b.getFullName()).collect(Collectors.joining(" - "));
    }
    return budgetStr;
  }

  @Override
  public String getBudgetDomain(PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder)
      throws AxelorException {
    Company company = null;
    LocalDate date = null;
    Set<GlobalBudget> globalBudgetSet = new HashSet<>();
    if (purchaseOrder != null) {
      if (purchaseOrder.getCompany() != null) {
        company = purchaseOrder.getCompany();
      }
      if (purchaseOrder.getOrderDate() != null) {
        date = purchaseOrder.getOrderDate();
      }

      if (purchaseOrder.getProject() != null
          && !ObjectUtils.isEmpty(purchaseOrder.getProject().getGlobalBudgetSet())) {
        globalBudgetSet = purchaseOrder.getProject().getGlobalBudgetSet();
      }
    }

    String technicalTypeSelect =
        Optional.of(purchaseOrderLine)
            .map(PurchaseOrderLine::getAccount)
            .map(Account::getAccountType)
            .map(AccountType::getTechnicalTypeSelect)
            .orElse(AccountTypeRepository.TYPE_CHARGE);

    return budgetDistributionService.getBudgetDomain(
        company, date, technicalTypeSelect, purchaseOrderLine.getAccount(), globalBudgetSet);
  }

  @Override
  public void checkAmountForPurchaseOrderLine(PurchaseOrderLine purchaseOrderLine)
      throws AxelorException {
    if (purchaseOrderLine.getBudgetDistributionList() != null
        && !purchaseOrderLine.getBudgetDistributionList().isEmpty()) {
      BigDecimal totalAmount = BigDecimal.ZERO;
      for (BudgetDistribution budgetDistribution : purchaseOrderLine.getBudgetDistributionList()) {
        if (currencyScaleService
                .getCompanyScaledValue(budgetDistribution, budgetDistribution.getAmount())
                .compareTo(
                    currencyScaleService.getCompanyScaledValue(
                        budgetDistribution, purchaseOrderLine.getCompanyExTaxTotal()))
            > 0) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(BudgetExceptionMessage.BUDGET_DISTRIBUTION_LINE_SUM_GREATER_PO),
              budgetDistribution.getBudget().getCode(),
              purchaseOrderLine.getProductCode());
        } else {
          totalAmount = totalAmount.add(budgetDistribution.getAmount());
        }
      }
      if (totalAmount.compareTo(purchaseOrderLine.getCompanyExTaxTotal()) > 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BudgetExceptionMessage.BUDGET_DISTRIBUTION_LINE_SUM_LINES_GREATER_PO),
            purchaseOrderLine.getProductCode());
      }
    }
  }
}
