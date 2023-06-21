package com.axelor.apps.budget.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.service.BudgetServiceImpl;
import com.axelor.apps.budget.service.saleorder.SaleOrderBudgetService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.supplychain.db.repo.SaleOrderSupplychainRepository;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.PersistenceException;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderManagementBudgetRepository extends SaleOrderSupplychainRepository {

  @Override
  public SaleOrder save(SaleOrder saleOrder) {
    try {
      saleOrder = super.save(saleOrder);

      Beans.get(SaleOrderBudgetService.class).validateSaleAmountWithBudgetDistribution(saleOrder);

    } catch (AxelorException e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }

    return saleOrder;
  }

  @Override
  public void remove(SaleOrder entity) {
    List<Budget> budgetList = new ArrayList<>();
    if (entity.getStatusSelect() >= SaleOrderRepository.STATUS_ORDER_CONFIRMED) {
      budgetList = cancelSaleOrder(entity);
    }
    super.remove(entity);
    resetBudgets(budgetList);
  }

  public List<Budget> cancelSaleOrder(SaleOrder saleOrder) {
    List<Budget> budgetList = new ArrayList<>();
    if (!CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
      budgetList =
          saleOrder.getSaleOrderLineList().stream()
              .filter(soLine -> !CollectionUtils.isEmpty(soLine.getBudgetDistributionList()))
              .flatMap(soLine -> soLine.getBudgetDistributionList().stream())
              .map(BudgetDistribution::getBudget)
              .collect(Collectors.toList());
      for (SaleOrderLine soLine : saleOrder.getSaleOrderLineList()) {
        soLine.setBudget(null);
        soLine.clearBudgetDistributionList();
      }
    }
    return budgetList;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void resetBudgets(List<Budget> budgetList) {
    BudgetServiceImpl budgetBudgetService = Beans.get(BudgetServiceImpl.class);

    if (!CollectionUtils.isEmpty(budgetList)) {
      for (Budget budget : budgetList) {
        budgetBudgetService.updateLines(budget);
        budgetBudgetService.computeTotalAmountCommitted(budget);
        budgetBudgetService.computeTotalAmountPaid(budget);
        budgetBudgetService.computeToBeCommittedAmount(budget);
      }
    }
  }
}
