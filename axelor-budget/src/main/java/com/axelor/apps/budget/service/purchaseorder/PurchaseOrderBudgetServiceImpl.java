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

import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.repo.BudgetDistributionRepository;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.utils.PurchaseOrderBudgetUtilsService;
import com.axelor.apps.businessproject.service.PurchaseOrderWorkflowServiceProjectImpl;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.supplychain.service.PurchaseOrderStockService;
import com.axelor.apps.supplychain.service.PurchaseOrderSupplychainService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.meta.CallMethod;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class PurchaseOrderBudgetServiceImpl extends PurchaseOrderWorkflowServiceProjectImpl
    implements PurchaseOrderBudgetService {

  protected PurchaseOrderLineBudgetService purchaseOrderLineBudgetService;
  protected BudgetDistributionService budgetDistributionService;
  protected BudgetDistributionRepository budgetDistributionRepository;
  protected AppBudgetService appBudgetService;
  protected BudgetToolsService budgetToolsService;
  protected CurrencyScaleService currencyScaleService;
  protected PurchaseOrderBudgetUtilsService purchaseOrderBudgetUtilsService;

  @Inject
  public PurchaseOrderBudgetServiceImpl(
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderRepository purchaseOrderRepo,
      AppPurchaseService appPurchaseService,
      AppSupplychainService appSupplychainService,
      PurchaseOrderStockService purchaseOrderStockService,
      AppAccountService appAccountService,
      PurchaseOrderSupplychainService purchaseOrderSupplychainService,
      AnalyticMoveLineRepository analyticMoveLineRepository,
      BudgetDistributionService budgetDistributionService,
      PurchaseOrderLineBudgetService purchaseOrderLineBudgetService,
      BudgetDistributionRepository budgetDistributionRepository,
      AppBudgetService appBudgetService,
      BudgetToolsService budgetToolsService,
      CurrencyScaleService currencyScaleService,
      PurchaseOrderBudgetUtilsService purchaseOrderBudgetUtilsService) {
    super(
        purchaseOrderService,
        purchaseOrderRepo,
        appPurchaseService,
        appSupplychainService,
        purchaseOrderStockService,
        appAccountService,
        purchaseOrderSupplychainService,
        analyticMoveLineRepository);
    this.budgetDistributionService = budgetDistributionService;
    this.purchaseOrderLineBudgetService = purchaseOrderLineBudgetService;
    this.budgetDistributionRepository = budgetDistributionRepository;
    this.appBudgetService = appBudgetService;
    this.budgetToolsService = budgetToolsService;
    this.currencyScaleService = currencyScaleService;
    this.purchaseOrderBudgetUtilsService = purchaseOrderBudgetUtilsService;
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
          if (budget == null && purchaseOrder.getBudget() != null) {
            budget = purchaseOrder.getBudget();

            budgetToolsService.fillAmountPerBudgetMap(
                budget, purchaseOrderLine.getCompanyExTaxTotal(), amountPerBudgetMap);
          }
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

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validatePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {
    super.validatePurchaseOrder(purchaseOrder);

    if (!appBudgetService.isApp("budget")) {
      return;
    }

    if (!appBudgetService.getAppBudget().getManageMultiBudget()) {
      purchaseOrderBudgetUtilsService.generateBudgetDistribution(purchaseOrder);
    }

    purchaseOrderBudgetUtilsService.updateBudgetLinesFromPurchaseOrder(purchaseOrder);
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
  @Transactional(rollbackOn = {Exception.class})
  public void cancelPurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {
    super.cancelPurchaseOrder(purchaseOrder);

    if (appBudgetService.getAppBudget() != null) {
      purchaseOrderBudgetUtilsService.updateBudgetLinesFromPurchaseOrder(purchaseOrder);

      if (purchaseOrder.getPurchaseOrderLineList() != null) {
        purchaseOrder.getPurchaseOrderLineList().stream()
            .forEach(
                poLine -> {
                  poLine.clearBudgetDistributionList();
                  poLine.setBudget(null);
                });
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
