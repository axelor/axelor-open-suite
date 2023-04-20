package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.Budget;
import com.axelor.apps.account.db.BudgetDistribution;
import com.axelor.apps.account.db.repo.BudgetDistributionRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public class PurchaseOrderBudgetServiceImpl implements PurchaseOrderBudgetService {
  protected PurchaseOrderLineBudgetService purchaseOrderLineBudgetService;
  protected BudgetDistributionRepository budgetDistributionRepo;

  @Inject
  public PurchaseOrderBudgetServiceImpl(
      PurchaseOrderLineBudgetService purchaseOrderLineBudgetService,
      BudgetDistributionRepository budgetDistributionRepo) {
    this.purchaseOrderLineBudgetService = purchaseOrderLineBudgetService;
    this.budgetDistributionRepo = budgetDistributionRepo;
  }

  @Override
  public void generateBudgetDistribution(PurchaseOrder purchaseOrder) {
    if (purchaseOrder.getPurchaseOrderLineList() != null) {
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
        Budget budget = purchaseOrderLine.getBudget();

        if (purchaseOrder.getStatusSelect() == PurchaseOrderRepository.STATUS_REQUESTED
            && budget != null
            && (purchaseOrderLine.getBudgetDistributionList() == null
                || purchaseOrderLine.getBudgetDistributionList().isEmpty())) {

          BudgetDistribution budgetDistribution = new BudgetDistribution();

          budgetDistribution.setBudget(budget);
          budgetDistribution.setAmount(purchaseOrderLine.getCompanyExTaxTotal());
          budgetDistribution.setBudgetAmountAvailable(
              budget.getTotalAmountExpected().subtract(budget.getTotalAmountCommitted()));

          purchaseOrderLine.addBudgetDistributionListItem(budgetDistribution);
        }
      }
    }
  }

  @Transactional
  @Override
  public void applyToallBudgetDistribution(PurchaseOrder purchaseOrder) {
    for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
      BudgetDistribution newBudgetDistribution = new BudgetDistribution();

      newBudgetDistribution.setAmount(purchaseOrderLine.getCompanyExTaxTotal());
      newBudgetDistribution.setBudget(purchaseOrder.getBudget());
      newBudgetDistribution.setPurchaseOrderLine(purchaseOrderLine);

      budgetDistributionRepo.save(newBudgetDistribution);
      purchaseOrderLineBudgetService.computeBudgetDistributionSumAmount(
          purchaseOrderLine, purchaseOrder);
    }
  }

  @Override
  public void setPurchaseOrderLineBudget(PurchaseOrder purchaseOrder) {
    purchaseOrder.getPurchaseOrderLineList().forEach(it -> it.setBudget(purchaseOrder.getBudget()));
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
                budget.getTotalAmountExpected().subtract(budget.getTotalAmountCommitted()));
          }
        }
      }
    }
  }

  @Override
  public boolean isGoodAmountBudgetDistribution(PurchaseOrder purchaseOrder) {
    if (purchaseOrder.getPurchaseOrderLineList() != null) {
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
        if (purchaseOrderLine.getBudgetDistributionList() != null
            && !purchaseOrderLine.getBudgetDistributionList().isEmpty()) {
          BigDecimal budgetDistributionTotalAmount =
              purchaseOrderLine.getBudgetDistributionList().stream()
                  .map(BudgetDistribution::getAmount)
                  .reduce(BigDecimal.ZERO, BigDecimal::add);

          return budgetDistributionTotalAmount.compareTo(purchaseOrderLine.getCompanyExTaxTotal())
              != 0;
        }
      }
    }

    return true;
  }
}
