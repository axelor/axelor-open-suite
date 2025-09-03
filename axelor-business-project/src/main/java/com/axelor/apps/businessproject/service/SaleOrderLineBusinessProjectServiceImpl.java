package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.saleorderline.SaleOrderLineBudgetServiceImpl;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class SaleOrderLineBusinessProjectServiceImpl extends SaleOrderLineBudgetServiceImpl {
  @Inject
  public SaleOrderLineBusinessProjectServiceImpl(
      BudgetService budgetService,
      BudgetDistributionService budgetDistributionService,
      SaleOrderLineRepository saleOrderLineRepo,
      AppBudgetService appBudgetService,
      BudgetToolsService budgetToolsService,
      AccountManagementAccountService accountManagementAccountService) {
    super(
        budgetService,
        budgetDistributionService,
        saleOrderLineRepo,
        appBudgetService,
        budgetToolsService,
        accountManagementAccountService);
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
}
