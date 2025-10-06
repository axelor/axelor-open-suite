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
package com.axelor.apps.budget.service.purchaseorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.repo.BudgetDistributionRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.common.StringUtils;
import com.axelor.meta.CallMethod;
import com.axelor.studio.db.AppBudget;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class PurchaseOrderBudgetServiceImpl implements PurchaseOrderBudgetService {

  protected BudgetRepository budgetRepository;
  protected PurchaseOrderLineBudgetService purchaseOrderLineBudgetService;
  protected BudgetDistributionService budgetDistributionService;
  protected BudgetService budgetService;
  protected BudgetDistributionRepository budgetDistributionRepository;
  protected AppBudgetService appBudgetService;
  protected BudgetToolsService budgetToolsService;
  protected CurrencyScaleService currencyScaleService;
  protected final PurchaseOrderRepository purchaseOrderRepo;

  @Inject
  public PurchaseOrderBudgetServiceImpl(
      PurchaseOrderRepository purchaseOrderRepo,
      CurrencyScaleService currencyScaleService,
      BudgetToolsService budgetToolsService,
      AppBudgetService appBudgetService,
      BudgetDistributionRepository budgetDistributionRepository,
      BudgetService budgetService,
      BudgetDistributionService budgetDistributionService,
      PurchaseOrderLineBudgetService purchaseOrderLineBudgetService,
      BudgetRepository budgetRepository) {
    this.purchaseOrderRepo = purchaseOrderRepo;
    this.currencyScaleService = currencyScaleService;
    this.budgetToolsService = budgetToolsService;
    this.appBudgetService = appBudgetService;
    this.budgetDistributionRepository = budgetDistributionRepository;
    this.budgetService = budgetService;
    this.budgetDistributionService = budgetDistributionService;
    this.purchaseOrderLineBudgetService = purchaseOrderLineBudgetService;
    this.budgetRepository = budgetRepository;
  }

  @Override
  @CallMethod
  public String getBudgetExceedAlert(PurchaseOrder purchaseOrder) {
    String budgetExceedAlert = "";

    List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrder.getPurchaseOrderLineList();

    if (appBudgetService.getAppBudget() != null
        && appBudgetService.getAppBudget().getCheckAvailableBudget()
        && CollectionUtils.isNotEmpty(purchaseOrderLineList)) {

      Map<Budget, BigDecimal> amountPerBudgetMap = new HashMap<>();

      for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {
        if (appBudgetService.getAppBudget().getManageMultiBudget()
            && CollectionUtils.isNotEmpty(purchaseOrderLine.getBudgetDistributionList())) {

          for (BudgetDistribution budgetDistribution :
              purchaseOrderLine.getBudgetDistributionList()) {
            Budget budget = budgetDistribution.getBudget();

            budgetToolsService.fillAmountPerBudgetMap(
                budget, budgetDistribution.getAmount(), amountPerBudgetMap);
          }

        } else {
          Budget budget = purchaseOrderLine.getBudget();
          budgetToolsService.fillAmountPerBudgetMap(
              budget, purchaseOrderLine.getCompanyExTaxTotal(), amountPerBudgetMap);
        }
      }

      for (Map.Entry<Budget, BigDecimal> budgetEntry : amountPerBudgetMap.entrySet()) {
        budgetExceedAlert +=
            budgetDistributionService.getBudgetExceedAlert(
                budgetEntry.getKey(), budgetEntry.getValue(), purchaseOrder.getOrderDate());
      }
    }
    return budgetExceedAlert;
  }

  @Override
  public void generateBudgetDistribution(PurchaseOrder purchaseOrder) {
    if (CollectionUtils.isNotEmpty(purchaseOrder.getPurchaseOrderLineList())) {
      AppBudget appBudget = appBudgetService.getAppBudget();
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
        if (purchaseOrderLine.getBudget() != null) {
          BudgetDistribution budgetDistribution = null;

          if (CollectionUtils.isNotEmpty(purchaseOrderLine.getBudgetDistributionList())) {
            budgetDistribution =
                purchaseOrderLine.getBudgetDistributionList().stream()
                    .filter(
                        it ->
                            it.getBudget() != null
                                && it.getBudget().equals(purchaseOrderLine.getBudget()))
                    .findFirst()
                    .orElse(null);
          }

          if (budgetDistribution == null) {
            purchaseOrderLine.clearBudgetDistributionList();

            budgetDistribution = new BudgetDistribution();
            budgetDistribution.setBudget(purchaseOrderLine.getBudget());
            budgetDistribution.setBudgetAmountAvailable(
                budgetToolsService.getAvailableAmountOnBudget(
                    purchaseOrderLine.getBudget(),
                    purchaseOrderLine.getPurchaseOrder() != null
                        ? purchaseOrderLine.getPurchaseOrder().getOrderDate()
                        : null));

            budgetDistributionService.linkBudgetDistributionWithParent(
                budgetDistribution, purchaseOrderLine);
          }

          budgetDistribution.setAmount(
              currencyScaleService.getCompanyScaledValue(
                  budgetDistribution, purchaseOrderLine.getCompanyExTaxTotal()));
        } else if (purchaseOrderLine.getBudget() == null
            && appBudget != null
            && !appBudget.getManageMultiBudget()) {
          purchaseOrderLine.clearBudgetDistributionList();
        }
        purchaseOrderLine.setBudgetRemainingAmountToAllocate(
            budgetToolsService.getBudgetRemainingAmountToAllocate(
                purchaseOrderLine.getBudgetDistributionList(),
                purchaseOrderLine.getCompanyExTaxTotal()));
      }
    }
  }

  @Override
  @Transactional
  public String computeBudgetDistribution(PurchaseOrder purchaseOrder) throws AxelorException {
    List<String> alertMessageTokenList = new ArrayList<>();
    if (!CollectionUtils.isEmpty(purchaseOrder.getPurchaseOrderLineList())) {
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
        String alertMessage =
            purchaseOrderLineBudgetService.computeBudgetDistribution(
                purchaseOrder, purchaseOrderLine);
        if (Strings.isNullOrEmpty(alertMessage)) {
          purchaseOrder.setBudgetDistributionGenerated(true);
        } else {
          alertMessageTokenList.add(alertMessage);
        }
      }
      purchaseOrderRepo.save(purchaseOrder);
    }
    return String.join(", ", alertMessageTokenList);
  }

  @Override
  public void validatePurchaseAmountWithBudgetDistribution(PurchaseOrder purchaseOrder)
      throws AxelorException {
    if (!CollectionUtils.isEmpty(purchaseOrder.getPurchaseOrderLineList())) {
      for (PurchaseOrderLine poLine : purchaseOrder.getPurchaseOrderLineList()) {
        String productCode =
            Optional.of(poLine)
                .map(PurchaseOrderLine::getProduct)
                .map(Product::getCode)
                .orElse(poLine.getProductName());
        if (StringUtils.notEmpty(productCode)) {
          budgetService.validateBudgetDistributionAmounts(
              poLine.getBudgetDistributionList(), poLine.getCompanyExTaxTotal(), productCode);
        }
      }
    }
  }

  @Override
  public boolean isBudgetInLines(PurchaseOrder purchaseOrder) {
    if (!CollectionUtils.isEmpty(purchaseOrder.getPurchaseOrderLineList())) {
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
        if (purchaseOrderLine.getBudget() != null
            || !CollectionUtils.isEmpty(purchaseOrderLine.getBudgetDistributionList())) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void updateBudgetLinesFromPurchaseOrder(PurchaseOrder purchaseOrder) {

    if (CollectionUtils.isNotEmpty(purchaseOrder.getPurchaseOrderLineList())) {
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
        if (CollectionUtils.isNotEmpty(purchaseOrderLine.getBudgetDistributionList())) {
          purchaseOrderLine.getBudgetDistributionList().stream()
              .forEach(
                  budgetDistribution -> {
                    budgetDistribution.setImputationDate(purchaseOrder.getOrderDate());
                    Budget budget = budgetDistribution.getBudget();
                    budgetService.updateLines(budget);
                    budgetService.computeTotalAmountCommitted(budget);
                    budgetService.computeTotalAmountPaid(budget);
                    budgetService.computeToBeCommittedAmount(budget);
                  });
        }
      }
    }
  }

  @Override
  public void setPurchaseOrderLineBudget(PurchaseOrder purchaseOrder) {
    purchaseOrder.getPurchaseOrderLineList().forEach(it -> it.setBudget(purchaseOrder.getBudget()));
  }

  @Transactional
  @Override
  public void applyToallBudgetDistribution(PurchaseOrder purchaseOrder) {
    for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
      BudgetDistribution newBudgetDistribution = new BudgetDistribution();

      newBudgetDistribution.setAmount(
          currencyScaleService.getCompanyScaledValue(
              purchaseOrder.getBudget(), purchaseOrderLine.getCompanyExTaxTotal()));
      newBudgetDistribution.setBudget(purchaseOrder.getBudget());
      newBudgetDistribution.setPurchaseOrderLine(purchaseOrderLine);

      budgetDistributionRepository.save(newBudgetDistribution);
      purchaseOrderLine.setBudgetRemainingAmountToAllocate(BigDecimal.ZERO);
    }
  }

  @Transactional
  @Override
  public void updateBudgetDistributionAmountAvailable(PurchaseOrder purchaseOrder) {
    if (purchaseOrder.getPurchaseOrderLineList() != null) {
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
        List<BudgetDistribution> budgetDistributionList =
            purchaseOrderLine.getBudgetDistributionList();
        Budget budget = purchaseOrderLine.getBudget();

        if (!budgetDistributionList.isEmpty() && budget != null) {
          for (BudgetDistribution budgetDistribution : budgetDistributionList) {
            budgetDistribution.setBudgetAmountAvailable(
                budgetToolsService.getAvailableAmountOnBudget(
                    budget, purchaseOrder.getOrderDate()));
          }
        }
      }
    }
  }

  @Override
  public void autoComputeBudgetDistribution(PurchaseOrder purchaseOrder) throws AxelorException {
    if (!budgetToolsService.canAutoComputeBudgetDistribution(
        purchaseOrder.getCompany(), purchaseOrder.getPurchaseOrderLineList())) {
      return;
    }
    for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
      budgetDistributionService.autoComputeBudgetDistribution(
          purchaseOrderLine.getAnalyticMoveLineList(),
          purchaseOrderLine.getAccount(),
          purchaseOrder.getCompany(),
          purchaseOrder.getOrderDate(),
          purchaseOrderLine.getCompanyExTaxTotal(),
          purchaseOrderLine);
      purchaseOrderLine.setBudgetRemainingAmountToAllocate(
          budgetToolsService.getBudgetRemainingAmountToAllocate(
              purchaseOrderLine.getBudgetDistributionList(),
              purchaseOrderLine.getCompanyExTaxTotal()));
      purchaseOrderLineBudgetService.fillBudgetStrOnLine(purchaseOrderLine, true);
    }
  }
}
