package com.axelor.apps.budget.service.purchaseorder;

import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.exception.IExceptionMessage;
import com.axelor.apps.budget.service.BudgetBudgetDistributionService;
import com.axelor.apps.budget.service.BudgetBudgetService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.supplychain.service.BudgetSupplychainService;
import com.axelor.apps.supplychain.service.PurchaseOrderLineBudgetServiceImpl;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class PurchaseOrderLineBudgetBudgetServiceImpl extends PurchaseOrderLineBudgetServiceImpl
    implements PurchaseOrderLineBudgetBudgetService {
  protected BudgetBudgetService budgetBudgetService;
  protected BudgetRepository budgetRepository;
  protected BudgetBudgetDistributionService budgetBudgetDistributionService;
  protected PurchaseOrderLineRepository purchaseOrderLineRepo;
  protected AppAccountService appAccountService;

  @Inject
  public PurchaseOrderLineBudgetBudgetServiceImpl(
      BudgetSupplychainService budgetSupplychainService,
      BudgetBudgetService budgetBudgetService,
      BudgetRepository budgetRepository,
      BudgetBudgetDistributionService budgetBudgetDistributionService,
      PurchaseOrderLineRepository purchaseOrderLineRepo,
      AppAccountService appAccountService) {
    super(budgetSupplychainService);
    this.budgetBudgetService = budgetBudgetService;
    this.budgetRepository = budgetRepository;
    this.budgetBudgetDistributionService = budgetBudgetDistributionService;
    this.purchaseOrderLineRepo = purchaseOrderLineRepo;
    this.appAccountService = appAccountService;
  }

  @Override
  @Transactional
  public String computeBudgetDistribution(PurchaseOrderLine purchaseOrderLine) {
    if (purchaseOrderLine == null || purchaseOrderLine.getPurchaseOrder() == null) {
      return "";
    }
    purchaseOrderLine.clearBudgetDistributionList();
    purchaseOrderLine.setBudgetStr("");
    String alertMessage =
        budgetBudgetDistributionService.createBudgetDistribution(
            purchaseOrderLine.getAnalyticMoveLineList(),
            purchaseOrderLine.getAccount(),
            purchaseOrderLine.getPurchaseOrder().getCompany(),
            purchaseOrderLine.getPurchaseOrder().getOrderDate(),
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

  @Transactional
  @Override
  public List<BudgetDistribution> addBudgetDistribution(PurchaseOrderLine purchaseOrderLine) {
    List<BudgetDistribution> budgetDistributionList = new ArrayList<>();
    if (!appAccountService.getAppBudget().getManageMultiBudget()
        && purchaseOrderLine.getBudget() != null) {
      BudgetDistribution budgetDistribution = new BudgetDistribution();
      budgetDistribution.setBudget(purchaseOrderLine.getBudget());
      budgetDistribution.setBudgetAmountAvailable(
          budgetDistribution.getBudget().getAvailableAmount());
      budgetDistribution.setAmount(purchaseOrderLine.getExTaxTotal());
      budgetDistributionList.add(budgetDistribution);
      purchaseOrderLine.setBudgetDistributionList(budgetDistributionList);
    }
    return budgetDistributionList;
  }

  @Override
  public String getBudgetDomain(PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder) {
    String query = "self.totalAmountExpected > 0 AND self.statusSelect = 2";
    if (purchaseOrderLine != null) {
      if (purchaseOrderLine.getLine() != null) {
        query =
            query.concat(String.format(" AND self.id = %d", purchaseOrderLine.getLine().getId()));
      } else if (purchaseOrderLine.getSection() != null) {
        query =
            query.concat(
                String.format(
                    " AND self.budgetLevel.id = %d", purchaseOrderLine.getSection().getId()));
      } else if (purchaseOrderLine.getGroupBudget() != null) {
        query =
            query.concat(
                String.format(
                    " AND self.budgetLevel.parentBudgetLevel.id = %d",
                    purchaseOrderLine.getGroupBudget().getId()));
      }
      if (AccountTypeRepository.TYPE_CHARGE.equals(
          purchaseOrderLine.getAccount().getAccountType().getTechnicalTypeSelect())) {
        query =
            query.concat(
                " AND self.budgetLevel.parentBudgetLevel.parentBudgetLevel.budgetTypeSelect in ("
                    + BudgetLevelRepository.BUDGET_LEVEL_BUDGET_TYPE_SELECT_PURCHASE
                    + ","
                    + BudgetLevelRepository.BUDGET_LEVEL_BUDGET_TYPE_SELECT_PURCHASE_AND_INVESTMENT
                    + ")");
      } else if (AccountTypeRepository.TYPE_IMMOBILISATION.equals(
          purchaseOrderLine.getAccount().getAccountType().getTechnicalTypeSelect())) {
        query =
            query.concat(
                " AND self.budgetLevel.parentBudgetLevel.parentBudgetLevel.budgetTypeSelect in ("
                    + BudgetLevelRepository.BUDGET_LEVEL_BUDGET_TYPE_SELECT_INVESTMENT
                    + ","
                    + BudgetLevelRepository.BUDGET_LEVEL_BUDGET_TYPE_SELECT_PURCHASE_AND_INVESTMENT
                    + ")");
      }
    }
    if (purchaseOrder != null) {
      query =
          query.concat(
              String.format(
                  " AND self.budgetLevel.parentBudgetLevel.parentBudgetLevel.company.id = %d",
                  purchaseOrder.getCompany() != null ? purchaseOrder.getCompany().getId() : 0));
      if (purchaseOrder.getBudgetLevel() != null) {
        query =
            query.concat(
                String.format(
                    " AND self.budgetLevel.parentBudgetLevel.parentBudgetLevel.id = %d",
                    purchaseOrder.getBudgetLevel().getId()));
      } else if (purchaseOrder.getCompanyDepartment() != null) {
        query =
            query.concat(
                String.format(
                    " AND self.budgetLevel.parentBudgetLevel.parentBudgetLevel.companyDepartment.id = %d",
                    purchaseOrder.getCompanyDepartment().getId()));
      }
      if (purchaseOrder.getOrderDate() != null) {
        query =
            query.concat(
                String.format(
                    " AND self.fromDate <= '%s' AND self.toDate >= '%s'",
                    purchaseOrder.getOrderDate(), purchaseOrder.getOrderDate()));
      }
    }
    return query;
  }

  @Override
  public void checkAmountForPurchaseOrderLine(PurchaseOrderLine purchaseOrderLine)
      throws AxelorException {
    if (purchaseOrderLine.getBudgetDistributionList() != null
        && !purchaseOrderLine.getBudgetDistributionList().isEmpty()) {
      for (BudgetDistribution budgetDistribution : purchaseOrderLine.getBudgetDistributionList()) {
        if (budgetDistribution.getAmount().compareTo(purchaseOrderLine.getCompanyExTaxTotal())
            > 0) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessage.BUDGET_DISTRIBUTION_LINE_SUM_GREATER_PO),
              budgetDistribution.getBudget().getCode(),
              purchaseOrderLine.getProductCode());
        }
      }
    }
  }
}
