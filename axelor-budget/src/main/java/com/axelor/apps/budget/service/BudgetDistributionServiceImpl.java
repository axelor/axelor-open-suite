/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.BudgetLine;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetDistributionRepository;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.auth.db.AuditableModel;
import com.axelor.common.ObjectUtils;
import com.axelor.db.EntityHelper;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.date.LocalDateHelper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class BudgetDistributionServiceImpl implements BudgetDistributionService {

  protected BudgetDistributionRepository budgetDistributionRepository;
  protected BudgetLineService budgetLineService;
  protected BudgetLevelService budgetLevelService;
  protected BudgetRepository budgetRepo;

  protected BudgetService budgetService;
  protected BudgetToolsService budgetToolsService;
  protected CurrencyScaleServiceBudget currencyScaleServiceBudget;

  @Inject
  public BudgetDistributionServiceImpl(
      BudgetDistributionRepository budgetDistributionRepository,
      BudgetLineService budgetLineService,
      BudgetLevelService budgetLevelService,
      BudgetRepository budgetRepo,
      BudgetService budgetService,
      BudgetToolsService budgetToolsService,
      CurrencyScaleServiceBudget currencyScaleServiceBudget) {
    this.budgetDistributionRepository = budgetDistributionRepository;
    this.budgetLineService = budgetLineService;
    this.budgetLevelService = budgetLevelService;
    this.budgetRepo = budgetRepo;
    this.budgetService = budgetService;
    this.budgetToolsService = budgetToolsService;
    this.currencyScaleServiceBudget = currencyScaleServiceBudget;
  }

  @Override
  public BudgetDistribution createDistributionFromBudget(
      Budget budget, BigDecimal amount, LocalDate date) {
    BudgetDistribution budgetDistribution = new BudgetDistribution();
    budgetDistribution.setBudget(budget);
    budgetDistribution.setBudgetAmountAvailable(
        budgetToolsService.getAvailableAmountOnBudget(budget, date));
    budgetDistribution.setAmount(currencyScaleServiceBudget.getCompanyScaledValue(budget, amount));

    return budgetDistribution;
  }

  @Override
  public String getBudgetExceedAlert(Budget budget, BigDecimal amount, LocalDate date) {

    String budgetExceedAlert = "";

    Integer budgetControlLevel = budgetToolsService.getBudgetControlLevel(budget);
    if (budget == null || budgetControlLevel == null) {
      return budgetExceedAlert;
    }
    BigDecimal budgetToCompare = BigDecimal.ZERO;
    String budgetName = budget.getName();

    switch (budgetControlLevel) {
      case BudgetLevelRepository.BUDGET_LEVEL_AVAILABLE_AMOUNT_BUDGET_LINE:
        for (BudgetLine budgetLine : budget.getBudgetLineList()) {
          if (LocalDateHelper.isBetween(budgetLine.getFromDate(), budgetLine.getToDate(), date)) {
            budgetToCompare =
                currencyScaleServiceBudget.getCompanyScaledValue(
                    budget, budgetLine.getAvailableAmount());
            budgetName +=
                ' ' + budgetLine.getFromDate().toString() + ':' + budgetLine.getToDate().toString();
            break;
          }
        }
        break;
      case BudgetLevelRepository.BUDGET_LEVEL_AVAILABLE_AMOUNT_BUDGET:
        budgetToCompare =
            currencyScaleServiceBudget.getCompanyScaledValue(budget, budget.getAvailableAmount());
        break;
      case BudgetLevelRepository.BUDGET_LEVEL_AVAILABLE_AMOUNT_BUDGET_SECTION:
        budgetToCompare =
            currencyScaleServiceBudget.getCompanyScaledValue(
                budget.getBudgetLevel(), budget.getBudgetLevel().getTotalAmountAvailable());
        budgetName = budget.getBudgetLevel().getName();
        break;
      case BudgetLevelRepository.BUDGET_LEVEL_AVAILABLE_AMOUNT_BUDGET_GROUP:
        budgetToCompare =
            currencyScaleServiceBudget.getCompanyScaledValue(
                budget.getBudgetLevel().getParentBudgetLevel(),
                budget.getBudgetLevel().getParentBudgetLevel().getTotalAmountAvailable());
        budgetName = budget.getBudgetLevel().getParentBudgetLevel().getName();
        break;
      default:
        budgetToCompare =
            currencyScaleServiceBudget.getCompanyScaledValue(
                budget.getBudgetLevel().getParentBudgetLevel().getGlobalBudget(),
                budget
                    .getBudgetLevel()
                    .getParentBudgetLevel()
                    .getGlobalBudget()
                    .getTotalAmountAvailable());
        budgetName = budget.getBudgetLevel().getParentBudgetLevel().getGlobalBudget().getName();
        break;
    }
    if (budgetToCompare.compareTo(currencyScaleServiceBudget.getCompanyScaledValue(budget, amount))
        < 0) {
      budgetExceedAlert =
          String.format(
              I18n.get(BudgetExceptionMessage.BUGDET_EXCEED_ERROR),
              budgetName,
              budgetToCompare,
              budget
                  .getBudgetLevel()
                  .getParentBudgetLevel()
                  .getGlobalBudget()
                  .getCompany()
                  .getCurrency()
                  .getSymbol());
    }
    return budgetExceedAlert;
  }

  @Override
  @Transactional
  public void computePaidAmount(Invoice invoice, BigDecimal ratio) {
    if (!CollectionUtils.isEmpty(invoice.getInvoiceLineList())) {
      for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
        if (!CollectionUtils.isEmpty(invoiceLine.getBudgetDistributionList())) {
          Budget budget = null;
          for (BudgetDistribution budgetDistribution : invoiceLine.getBudgetDistributionList()) {
            budget = budgetDistribution.getBudget();
            budget.setTotalAmountPaid(
                currencyScaleServiceBudget.getCompanyScaledValue(
                    budget,
                    budget
                        .getTotalAmountPaid()
                        .add(budgetDistribution.getAmount().multiply(ratio))));
            budgetRepo.save(budget);
          }
        }
      }
    }
  }

  @Override
  public String createBudgetDistribution(
      List<AnalyticMoveLine> analyticMoveLineList,
      Account account,
      Company company,
      LocalDate date,
      BigDecimal amount,
      String name,
      AuditableModel object)
      throws AxelorException {
    List<String> alertMessageTokenList = new ArrayList<>();

    if (!CollectionUtils.isEmpty(analyticMoveLineList)) {
      List<AnalyticAxis> authorizedAxis = budgetToolsService.getAuthorizedAnalyticAxis(company);
      if (CollectionUtils.isEmpty(authorizedAxis)) {
        return "";
      }
      for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
        if (authorizedAxis.contains(analyticMoveLine.getAnalyticAxis())) {
          String key = budgetService.computeKey(account, company, analyticMoveLine);

          if (!Strings.isNullOrEmpty(key)) {
            Budget budget = budgetService.findBudgetWithKey(key, date);

            if (budget != null) {
              BudgetDistribution budgetDistribution =
                  createDistributionFromBudget(
                      budget,
                      currencyScaleServiceBudget.getCompanyScaledValue(
                          budget,
                          amount
                              .multiply(analyticMoveLine.getPercentage())
                              .divide(new BigDecimal(100))),
                      date);

              linkBudgetDistributionWithParent(budgetDistribution, object);

            } else {
              alertMessageTokenList.add(
                  String.format(
                      "%s - %s %s",
                      name,
                      I18n.get("Analytic account"),
                      analyticMoveLine.getAnalyticAccount().getCode()));
            }
          }
        }
      }
    }
    return String.join(", ", alertMessageTokenList);
  }

  protected void linkBudgetDistributionWithParent(
      BudgetDistribution budgetDistribution, AuditableModel object) {

    if (MoveLine.class.equals(EntityHelper.getEntityClass(object))) {
      MoveLine moveLine = (MoveLine) object;
      moveLine.addBudgetDistributionListItem(budgetDistribution);
    } else if (PurchaseOrderLine.class.equals(EntityHelper.getEntityClass(object))) {
      PurchaseOrderLine purchaseOrderLine = (PurchaseOrderLine) object;
      purchaseOrderLine.addBudgetDistributionListItem(budgetDistribution);
    } else if (SaleOrderLine.class.equals(EntityHelper.getEntityClass(object))) {
      SaleOrderLine saleOrderLine = (SaleOrderLine) object;
      saleOrderLine.addBudgetDistributionListItem(budgetDistribution);
    } else if (InvoiceLine.class.equals(EntityHelper.getEntityClass(object))) {
      InvoiceLine invoiceLine = (InvoiceLine) object;
      invoiceLine.addBudgetDistributionListItem(budgetDistribution);
    }
  }

  public void computeBudgetDistributionSumAmount(
      BudgetDistribution budgetDistribution, LocalDate computeDate) {

    if (budgetDistribution.getBudget() != null && computeDate != null) {
      budgetDistribution.setBudgetAmountAvailable(
          budgetToolsService.getAvailableAmountOnBudget(
              budgetDistribution.getBudget(), computeDate));
    } else {
      budgetDistribution.setBudgetAmountAvailable(BigDecimal.ZERO);
    }
  }

  @Override
  public String getBudgetDomain(Company company, LocalDate date, String technicalTypeSelect) {
    String budget = "self.budgetLevel.parentBudgetLevel.globalBudget";
    String query =
        String.format(
            "self.totalAmountExpected > 0 AND self.statusSelect = %d ",
            BudgetRepository.STATUS_VALIDATED);

    if (company != null) {
      query.concat(
          String.format(" AND %s.company.id = %d", budget, company != null ? company.getId() : 0));
    }
    if (date != null) {
      query =
          query.concat(
              String.format(" AND self.fromDate <= '%s' AND self.toDate >= '%s'", date, date));
    }
    if (AccountTypeRepository.TYPE_INCOME.equals(technicalTypeSelect)) {
      query =
          query.concat(
              String.format(
                  " AND %s.budgetTypeSelect = %d ",
                  budget, BudgetLevelRepository.BUDGET_LEVEL_BUDGET_TYPE_SELECT_SALE));
    } else if (AccountTypeRepository.TYPE_CHARGE.equals(technicalTypeSelect)) {
      query =
          query.concat(
              String.format(
                  " AND %s.budgetTypeSelect in (%d,%d) ",
                  budget,
                  BudgetLevelRepository.BUDGET_LEVEL_BUDGET_TYPE_SELECT_PURCHASE,
                  BudgetLevelRepository.BUDGET_LEVEL_BUDGET_TYPE_SELECT_PURCHASE_AND_INVESTMENT));
    } else if (AccountTypeRepository.TYPE_IMMOBILISATION.equals(technicalTypeSelect)) {
      query =
          query.concat(
              String.format(
                  " AND %s.budgetTypeSelect in (%d,%d) ",
                  budget,
                  BudgetLevelRepository.BUDGET_LEVEL_BUDGET_TYPE_SELECT_INVESTMENT,
                  BudgetLevelRepository.BUDGET_LEVEL_BUDGET_TYPE_SELECT_PURCHASE_AND_INVESTMENT));
    } else {
      query = "self.id = 0";
    }

    return query;
  }

  @Override
  public void autoComputeBudgetDistribution(
      List<AnalyticMoveLine> analyticMoveLineList,
      Account account,
      Company company,
      LocalDate date,
      BigDecimal amount,
      AuditableModel object) {
    if (ObjectUtils.isEmpty(analyticMoveLineList)) {
      return;
    }
    for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
      String key = budgetService.computeKey(account, company, analyticMoveLine);

      if (!Strings.isNullOrEmpty(key)) {
        Budget budget = budgetService.findBudgetWithKey(key, date);

        if (budget != null) {
          GlobalBudget globalBudget = budgetToolsService.getGlobalBudgetUsingBudget(budget);
          if (globalBudget != null && globalBudget.getAutomaticBudgetComputation()) {
            BudgetDistribution budgetDistribution =
                createDistributionFromBudget(
                    budget,
                    currencyScaleServiceBudget.getCompanyScaledValue(
                        budget,
                        amount
                            .multiply(analyticMoveLine.getPercentage())
                            .divide(new BigDecimal(100))),
                    date);
            linkBudgetDistributionWithParent(budgetDistribution, object);
          }
        }
      }
    }
  }
}
