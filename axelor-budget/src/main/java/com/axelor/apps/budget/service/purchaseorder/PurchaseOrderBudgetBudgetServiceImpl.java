package com.axelor.apps.budget.service.purchaseorder;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.repo.BudgetDistributionRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.BudgetBudgetDistributionService;
import com.axelor.apps.budget.service.BudgetBudgetService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.supplychain.service.PurchaseOrderBudgetServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderLineBudgetService;
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
public class PurchaseOrderBudgetBudgetServiceImpl extends PurchaseOrderBudgetServiceImpl
    implements PurchaseOrderBudgetBudgetService {
  protected BudgetRepository budgetRepository;
  protected PurchaseOrderLineBudgetBudgetService purchaseOrderLineBudgetBudgetService;
  protected BudgetBudgetDistributionService budgetDistributionService;
  protected AppAccountService appAccountService;
  protected PurchaseOrderRepository purchaseOrderRepo;
  protected BudgetBudgetService budgetBudgetService;

  @Inject
  public PurchaseOrderBudgetBudgetServiceImpl(
      PurchaseOrderLineBudgetService purchaseOrderLineBudgetService,
      BudgetDistributionRepository budgetDistributionRepo,
      BudgetRepository budgetRepository,
      BudgetBudgetDistributionService budgetDistributionService,
      AppAccountService appAccountService,
      PurchaseOrderRepository purchaseOrderRepo,
      PurchaseOrderLineBudgetBudgetService purchaseOrderLineBudgetBudgetService,
      BudgetBudgetService budgetBudgetService) {
    super(purchaseOrderLineBudgetService, budgetDistributionRepo);
    this.budgetRepository = budgetRepository;
    this.budgetDistributionService = budgetDistributionService;
    this.appAccountService = appAccountService;
    this.purchaseOrderRepo = purchaseOrderRepo;
    this.purchaseOrderLineBudgetBudgetService = purchaseOrderLineBudgetBudgetService;
    this.budgetBudgetService = budgetBudgetService;
  }

  @Override
  @CallMethod
  public String getBudgetExceedAlert(PurchaseOrder purchaseOrder) {
    String budgetExceedAlert = "";

    List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrder.getPurchaseOrderLineList();

    if (appAccountService.isApp("budget")
        && appAccountService.getAppBudget().getCheckAvailableBudget()
        && CollectionUtils.isNotEmpty(purchaseOrderLineList)) {

      Map<Budget, BigDecimal> amountPerBudgetMap = new HashMap<>();

      for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {
        if (appAccountService.getAppBudget().getManageMultiBudget()
            && CollectionUtils.isNotEmpty(purchaseOrderLine.getBudgetDistributionList())) {

          for (BudgetDistribution budgetDistribution :
              purchaseOrderLine.getBudgetDistributionList()) {
            Budget budget = budgetDistribution.getBudget();

            if (!amountPerBudgetMap.containsKey(budget)) {
              amountPerBudgetMap.put(budget, budgetDistribution.getAmount());
            } else {
              BigDecimal oldAmount = amountPerBudgetMap.get(budget);
              amountPerBudgetMap.remove(budget);
              amountPerBudgetMap.put(budget, oldAmount.add(budgetDistribution.getAmount()));
            }
          }

          for (Map.Entry<Budget, BigDecimal> budgetEntry : amountPerBudgetMap.entrySet()) {
            budgetExceedAlert +=
                budgetDistributionService.getBudgetExceedAlert(
                    budgetEntry.getKey(), budgetEntry.getValue(), purchaseOrder.getOrderDate());
          }
        } else {
          Budget budget = purchaseOrderLine.getBudget();
          if (budget == null && purchaseOrder.getBudget() != null) {
            budget = purchaseOrder.getBudget();

            if (!amountPerBudgetMap.containsKey(budget)) {
              amountPerBudgetMap.put(budget, purchaseOrderLine.getExTaxTotal());
            } else {
              BigDecimal oldAmount = amountPerBudgetMap.get(budget);
              amountPerBudgetMap.put(budget, oldAmount.add(purchaseOrderLine.getExTaxTotal()));
            }

            budgetExceedAlert +=
                budgetDistributionService.getBudgetExceedAlert(
                    budget, amountPerBudgetMap.get(budget), purchaseOrder.getOrderDate());
          }
        }
      }
    }
    return budgetExceedAlert;
  }

  @Override
  public void generateBudgetDistribution(PurchaseOrder purchaseOrder) {
    if (CollectionUtils.isNotEmpty(purchaseOrder.getPurchaseOrderLineList())) {
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
            budgetDistribution = new BudgetDistribution();
            budgetDistribution.setBudget(purchaseOrderLine.getBudget());
            purchaseOrderLine.addBudgetDistributionListItem(budgetDistribution);
          }

          budgetDistribution.setAmount(purchaseOrderLine.getCompanyExTaxTotal());
        }
      }
    }
  }

  @Override
  @Transactional
  public String computeBudgetDistribution(PurchaseOrder purchaseOrder) {
    List<String> alertMessageTokenList = new ArrayList<>();
    if (!CollectionUtils.isEmpty(purchaseOrder.getPurchaseOrderLineList())) {
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
        String alertMessage =
            purchaseOrderLineBudgetBudgetService.computeBudgetDistribution(purchaseOrderLine);
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
        budgetBudgetService.validateBudgetDistributionAmounts(
            poLine.getBudgetDistributionList(),
            poLine.getCompanyExTaxTotal(),
            poLine.getProduct().getCode());
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
}
