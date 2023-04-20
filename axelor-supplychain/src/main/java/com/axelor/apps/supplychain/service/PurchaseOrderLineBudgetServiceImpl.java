package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.BudgetDistribution;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class PurchaseOrderLineBudgetServiceImpl implements PurchaseOrderLineBudgetService {
  protected BudgetSupplychainService budgetSupplychainService;

  @Inject
  public PurchaseOrderLineBudgetServiceImpl(BudgetSupplychainService budgetSupplychainService) {
    this.budgetSupplychainService = budgetSupplychainService;
  }

  public void computeBudgetDistributionSumAmount(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder) {
    List<BudgetDistribution> budgetDistributionList = purchaseOrderLine.getBudgetDistributionList();
    BigDecimal budgetDistributionSumAmount = BigDecimal.ZERO;
    LocalDate computeDate = purchaseOrder.getOrderDate();

    if (CollectionUtils.isNotEmpty(budgetDistributionList)) {
      for (BudgetDistribution budgetDistribution : budgetDistributionList) {
        budgetDistributionSumAmount =
            budgetDistributionSumAmount.add(budgetDistribution.getAmount());

        budgetSupplychainService.computeBudgetDistributionSumAmount(
            budgetDistribution, computeDate);
      }
    }

    purchaseOrderLine.setBudgetDistributionSumAmount(budgetDistributionSumAmount);
  }
}
