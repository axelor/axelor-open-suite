package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountType;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.budget.service.BudgetToolsService;
import com.axelor.apps.budget.service.purchaseorder.PurchaseOrderLineBudgetServiceImpl;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class PurchaseOrderLineBusinessProjectServiceImpl
    extends PurchaseOrderLineBudgetServiceImpl {
  @Inject
  public PurchaseOrderLineBusinessProjectServiceImpl(
      BudgetService budgetService,
      BudgetRepository budgetRepository,
      BudgetDistributionService budgetDistributionService,
      PurchaseOrderLineRepository purchaseOrderLineRepo,
      AppBudgetService appBudgetService,
      BudgetToolsService budgetToolsService,
      CurrencyScaleService currencyScaleService) {
    super(
        budgetService,
        budgetRepository,
        budgetDistributionService,
        purchaseOrderLineRepo,
        appBudgetService,
        budgetToolsService,
        currencyScaleService);
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
}
