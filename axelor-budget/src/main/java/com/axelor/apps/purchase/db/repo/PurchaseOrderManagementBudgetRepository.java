package com.axelor.apps.purchase.db.repo;

import com.axelor.apps.account.service.app.AppBudgetService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.service.BudgetBudgetService;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderBudgetBudgetService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.supplychain.db.repo.PurchaseOrderSupplychainRepository;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppBudget;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.PersistenceException;
import org.apache.commons.collections.CollectionUtils;

public class PurchaseOrderManagementBudgetRepository extends PurchaseOrderSupplychainRepository {

  @Override
  public PurchaseOrder save(PurchaseOrder purchaseOrder) {
    try {

      AppBudget appBudget = Beans.get(AppBudgetService.class).getAppBudget();
      if (appBudget != null) {
        Beans.get(PurchaseOrderBudgetBudgetService.class).generateBudgetDistribution(purchaseOrder);
      }

      purchaseOrder = super.save(purchaseOrder);

      List<Budget> updateBudgetList = new ArrayList<>();

      Beans.get(PurchaseOrderBudgetBudgetService.class)
          .validatePurchaseAmountWithBudgetDistribution(purchaseOrder);

      /**
       * This is done because in this project there is the requirement of updating budget's
       * committed amount at any stage of PO. So, if AppBudget.manageMultiBudget is false, and if
       * budget of PO gets changed, there will be two budgetDistributionLines in PO which will
       * create regression as per #26510.
       */
      if (appBudget != null && Boolean.FALSE.equals(appBudget.getManageMultiBudget())) {
        List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrder.getPurchaseOrderLineList();

        if (!CollectionUtils.isEmpty(purchaseOrderLineList)) {
          for (PurchaseOrderLine orderLine : purchaseOrderLineList) {
            if (orderLine.getBudget() != null
                && !CollectionUtils.isEmpty(orderLine.getBudgetDistributionList())) {

              BudgetDistribution tempBudgetDistribution = null;
              for (BudgetDistribution budgetDistribution : orderLine.getBudgetDistributionList()) {
                Budget budget = budgetDistribution.getBudget();
                if (orderLine.getBudget().equals(budget)) {
                  tempBudgetDistribution = budgetDistribution;
                } else {
                  updateBudgetList.add(budget);
                }
              }
              orderLine.clearBudgetDistributionList();
              orderLine.addBudgetDistributionListItem(tempBudgetDistribution);
            }
          }
        }
      }

      if (purchaseOrder.getStatusSelect() != null
          && purchaseOrder.getStatusSelect() == PurchaseOrderRepository.STATUS_REQUESTED) {
        Beans.get(PurchaseOrderBudgetBudgetService.class)
            .updateBudgetLinesFromPurchaseOrder(purchaseOrder);
      }

      if (!CollectionUtils.isEmpty(updateBudgetList)) {

        BudgetBudgetService budgetBudgetService = Beans.get(BudgetBudgetService.class);
        BudgetRepository budgetRepository = Beans.get(BudgetRepository.class);

        for (Budget budget : updateBudgetList) {
          budgetBudgetService.updateLines(budget);
          budgetBudgetService.computeTotalAmountCommitted(budget);
          budgetBudgetService.computeTotalAmountPaid(budget);
          budgetRepository.save(budget);
        }
      }
    } catch (AxelorException e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }

    return purchaseOrder;
  }

  // This remove method is override because when PO gets deleted, budget's committed amount should
  // be calculated again, which is done in cancel method.
  @Override
  public void remove(PurchaseOrder entity) {
    List<Budget> budgetList = new ArrayList<>();
    if (entity.getStatusSelect() >= PurchaseOrderRepository.STATUS_REQUESTED) {
      budgetList = cancelPurchaseOrder(entity);
    }
    super.remove(entity);
    resetBudgets(budgetList);
  }

  public List<Budget> cancelPurchaseOrder(PurchaseOrder purchaseOrder) {
    List<Budget> budgetList = new ArrayList<>();
    if (!CollectionUtils.isEmpty(purchaseOrder.getPurchaseOrderLineList())) {
      budgetList =
          purchaseOrder.getPurchaseOrderLineList().stream()
              .filter(poLine -> !CollectionUtils.isEmpty(poLine.getBudgetDistributionList()))
              .flatMap(poLine -> poLine.getBudgetDistributionList().stream())
              .map(BudgetDistribution::getBudget)
              .collect(Collectors.toList());
      for (PurchaseOrderLine poLine : purchaseOrder.getPurchaseOrderLineList()) {
        poLine.setBudget(null);
        poLine.clearBudgetDistributionList();
      }
    }
    return budgetList;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void resetBudgets(List<Budget> budgetList) {
    BudgetBudgetService budgetBudgetService = Beans.get(BudgetBudgetService.class);

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
