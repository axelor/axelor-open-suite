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
package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.budget.service.BudgetDistributionService;
import com.axelor.apps.budget.service.BudgetService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineBudgetServiceImpl implements SaleOrderLineBudgetService {

  protected BudgetService budgetService;
  protected BudgetDistributionService budgetDistributionService;
  protected SaleOrderLineRepository saleOrderLineRepo;
  protected AppBudgetService appBudgetService;

  @Inject
  public SaleOrderLineBudgetServiceImpl(
      BudgetService budgetService,
      BudgetDistributionService budgetDistributionService,
      SaleOrderLineRepository saleOrderLineRepo,
      AppBudgetService appBudgetService) {
    this.budgetService = budgetService;
    this.budgetDistributionService = budgetDistributionService;
    this.saleOrderLineRepo = saleOrderLineRepo;
    this.appBudgetService = appBudgetService;
  }

  @Override
  @Transactional
  public String computeBudgetDistribution(SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    if (saleOrder == null || saleOrderLine == null) {
      return "";
    }
    saleOrderLine.clearBudgetDistributionList();
    saleOrderLine.setBudgetStr("");
    String alertMessage =
        budgetDistributionService.createBudgetDistribution(
            saleOrderLine.getAnalyticMoveLineList(),
            saleOrderLine.getAccount(),
            saleOrder.getCompany(),
            saleOrder.getOrderDate() != null
                ? saleOrder.getOrderDate()
                : saleOrder.getCreationDate(),
            saleOrderLine.getCompanyExTaxTotal(),
            saleOrderLine.getFullName(),
            saleOrderLine);

    fillBudgetStrOnLine(saleOrderLine, true);
    saleOrderLineRepo.save(saleOrderLine);
    return alertMessage;
  }

  @Override
  @Transactional
  public void fillBudgetStrOnLine(SaleOrderLine saleOrderLine, boolean multiBudget) {
    saleOrderLine.setBudgetStr(this.searchAndFillBudgetStr(saleOrderLine, multiBudget));
    saleOrderLineRepo.save(saleOrderLine);
  }

  @Override
  public String searchAndFillBudgetStr(SaleOrderLine saleOrderLine, boolean multiBudget) {
    String budgetStr = "";
    if (!multiBudget && saleOrderLine.getBudget() != null) {
      budgetStr = saleOrderLine.getBudget().getFullName();
    } else if (multiBudget && !CollectionUtils.isEmpty(saleOrderLine.getBudgetDistributionList())) {
      List<Budget> budgetList = new ArrayList();
      for (BudgetDistribution budgetDistribution : saleOrderLine.getBudgetDistributionList()) {
        budgetList.add(budgetDistribution.getBudget());
      }
      budgetStr = budgetList.stream().map(b -> b.getFullName()).collect(Collectors.joining(" - "));
    }
    return budgetStr;
  }

  @Transactional
  @Override
  public List<BudgetDistribution> addBudgetDistribution(SaleOrderLine saleOrderLine) {
    List<BudgetDistribution> budgetDistributionList = new ArrayList<>();
    if (appBudgetService.getAppBudget() != null
        && !appBudgetService.getAppBudget().getManageMultiBudget()
        && saleOrderLine.getBudget() != null) {
      BudgetDistribution budgetDistribution = new BudgetDistribution();
      budgetDistribution.setBudget(saleOrderLine.getBudget());
      budgetDistribution.setBudgetAmountAvailable(
          budgetDistribution.getBudget().getAvailableAmount());
      budgetDistribution.setAmount(saleOrderLine.getExTaxTotal());
      budgetDistributionList.add(budgetDistribution);
      saleOrderLine.setBudgetDistributionList(budgetDistributionList);
    }
    return budgetDistributionList;
  }

  @Override
  public String getBudgetDomain(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    String query =
        "self.totalAmountExpected > 0 AND self.statusSelect = 2 AND self.budgetLevel.parentBudgetLevel.parentBudgetLevel.budgetTypeSelect = 2";
    if (saleOrderLine != null) {
      if (saleOrderLine.getLine() != null) {
        query = query.concat(String.format(" AND self.id = %d", saleOrderLine.getLine().getId()));
      } else if (saleOrderLine.getSection() != null) {
        query =
            query.concat(
                String.format(" AND self.budgetLevel.id = %d", saleOrderLine.getSection().getId()));
      } else if (saleOrderLine.getGroupBudget() != null) {
        query =
            query.concat(
                String.format(
                    " AND self.budgetLevel.parentBudgetLevel.id = %d",
                    saleOrderLine.getGroupBudget().getId()));
      }
      LocalDate date = null;
      if (saleOrder != null) {
        date =
            saleOrderLine.getSaleOrder().getOrderDate() != null
                ? saleOrderLine.getSaleOrder().getOrderDate()
                : saleOrderLine.getSaleOrder().getCreationDate();
      }
      if (date != null) {
        query =
            query.concat(
                String.format(" AND self.fromDate <= '%s' AND self.toDate >= '%s'", date, date));
      }
    }
    return query;
  }

  @Override
  public void checkAmountForSaleOrderLine(SaleOrderLine saleOrderLine) throws AxelorException {
    if (saleOrderLine.getBudgetDistributionList() != null
        && !saleOrderLine.getBudgetDistributionList().isEmpty()) {
      for (BudgetDistribution budgetDistribution : saleOrderLine.getBudgetDistributionList()) {
        if (budgetDistribution.getAmount().compareTo(saleOrderLine.getCompanyExTaxTotal()) > 0) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(BudgetExceptionMessage.BUDGET_DISTRIBUTION_LINE_SUM_GREATER_PO),
              budgetDistribution.getBudget().getCode(),
              saleOrderLine.getProductName());
        }
      }
    }
  }

  @Override
  public void computeBudgetDistributionSumAmount(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    List<BudgetDistribution> budgetDistributionList = saleOrderLine.getBudgetDistributionList();
    BigDecimal budgetDistributionSumAmount = BigDecimal.ZERO;
    LocalDate computeDate =
        saleOrder.getOrderDate() != null ? saleOrder.getOrderDate() : saleOrder.getCreationDate();

    if (budgetDistributionList != null && !budgetDistributionList.isEmpty()) {

      for (BudgetDistribution budgetDistribution : budgetDistributionList) {
        budgetDistributionSumAmount =
            budgetDistributionSumAmount.add(budgetDistribution.getAmount());
        budgetDistributionService.computeBudgetDistributionSumAmount(
            budgetDistribution, computeDate);
      }
    }
    saleOrderLine.setBudgetDistributionSumAmount(budgetDistributionSumAmount);
  }

  @Override
  public String getGroupBudgetDomain(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {

    if (saleOrderLine == null || saleOrder == null || saleOrder.getCompany() == null) {
      return "self.id = 0";
    }

    String query =
        String.format(
            "self.parentBudgetLevel IS NOT NULL AND self.parentBudgetLevel.budgetTypeSelect = %d AND self.parentBudgetLevel.parentBudgetLevel IS NULL AND self.statusSelect = '%s'",
            BudgetLevelRepository.BUDGET_LEVEL_BUDGET_TYPE_SELECT_SALE,
            BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_VALID);

    if (saleOrderLine.getBudget() != null
        && saleOrderLine.getBudget().getBudgetLevel() != null
        && saleOrderLine.getBudget().getBudgetLevel().getParentBudgetLevel() != null) {
      query =
          query.concat(
              String.format(
                  " AND self.id = %d",
                  saleOrderLine.getBudget().getBudgetLevel().getParentBudgetLevel().getId()));
    } else {
      query =
          query.concat(
              String.format(
                  " AND self.parentBudgetLevel.company.id = %d", saleOrder.getCompany().getId()));
    }
    return query;
  }

  @Override
  public String getSectionBudgetDomain(SaleOrderLine saleOrderLine, SaleOrder saleOrder) {

    if (saleOrderLine == null || saleOrder == null || saleOrder.getCompany() == null) {
      return "self.id = 0";
    }

    String query =
        String.format(
            "self.parentBudgetLevel IS NOT NULL AND self.parentBudgetLevel.parentBudgetLevel.budgetTypeSelect = %d AND self.statusSelect = '%s'",
            BudgetLevelRepository.BUDGET_LEVEL_BUDGET_TYPE_SELECT_SALE,
            BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_VALID);
    if (saleOrderLine.getBudget() != null && saleOrderLine.getBudget().getBudgetLevel() != null) {
      query =
          query.concat(
              String.format(
                  " AND self.id = %d", saleOrderLine.getBudget().getBudgetLevel().getId()));
    } else if (saleOrderLine.getGroupBudget() != null) {
      query =
          query.concat(
              String.format(
                  " AND self.parentBudgetLevel.id = %d", saleOrderLine.getGroupBudget().getId()));
    } else {
      query =
          query.concat(
              String.format(
                  " AND self.parentBudgetLevel.company.id = %d", saleOrder.getCompany().getId()));
    }
    return query;
  }

  @Override
  public String getLineBudgetDomain(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, boolean isBudget) {

    if (saleOrderLine == null || saleOrder == null || saleOrder.getCompany() == null) {
      return "self.id = 0";
    }

    String query =
        String.format(
            "self.budgetLevel.parentBudgetLevel.parentBudgetLevel IS NOT NULL AND self.budgetLevel.parentBudgetLevel.parentBudgetLevel.budgetTypeSelect = %d AND self.budgetLevel.parentBudgetLevel.parentBudgetLevel.statusSelect = '%s'",
            BudgetLevelRepository.BUDGET_LEVEL_BUDGET_TYPE_SELECT_SALE,
            BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_VALID);
    if (saleOrderLine.getBudget() != null && !isBudget) {
      query = query.concat(String.format(" AND self.id = %d", saleOrderLine.getBudget().getId()));
    } else if (saleOrderLine.getLine() != null && isBudget) {
      query = query.concat(String.format(" AND self.id = %d", saleOrderLine.getLine().getId()));
    } else if (saleOrderLine.getSection() != null) {
      query =
          query.concat(
              String.format(" AND self.budgetLevel.id = %d", saleOrderLine.getSection().getId()));
    } else if (saleOrderLine.getGroupBudget() != null) {
      query =
          query.concat(
              String.format(
                  " AND self.budgetLevel.parentBudgetLevel.id = %d",
                  saleOrderLine.getGroupBudget().getId()));
    } else {
      query =
          query.concat(
              String.format(
                  " AND self.budgetLevel.parentBudgetLevel.parentBudgetLevel.company.id = %d",
                  saleOrder.getCompany().getId()));
    }
    return query;
  }
}
