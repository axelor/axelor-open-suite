/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.AnalyticDistributionLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetLine;
import com.axelor.apps.budget.db.BudgetScenarioVariable;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.BudgetDistributionRepository;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.db.repo.BudgetLineRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.db.repo.GlobalBudgetRepository;
import com.axelor.apps.budget.exception.BudgetExceptionMessage;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.date.LocalDateHelper;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class BudgetServiceImpl implements BudgetService {

  protected BudgetLineRepository budgetLineRepository;
  protected BudgetRepository budgetRepository;
  protected BudgetLevelRepository budgetLevelRepository;
  protected BudgetDistributionRepository budgetDistributionRepo;
  protected BudgetLineService budgetLineService;
  protected AccountConfigService accountConfigService;
  protected AnalyticMoveLineService analyticMoveLineService;
  protected BudgetDistributionRepository budgetDistributionRepository;
  protected AccountRepository accountRepo;
  protected AnalyticDistributionLineRepository analyticDistributionLineRepo;
  protected BudgetToolsService budgetToolsService;
  protected CurrencyScaleService currencyScaleService;

  @Inject
  public BudgetServiceImpl(
      BudgetLineRepository budgetLineRepository,
      BudgetRepository budgetRepository,
      BudgetLevelRepository budgetLevelRepository,
      BudgetDistributionRepository budgetDistributionRepo,
      BudgetLineService budgetLineService,
      AccountConfigService accountConfigService,
      AnalyticMoveLineService analyticMoveLineService,
      BudgetDistributionRepository budgetDistributionRepository,
      AccountRepository accountRepo,
      AnalyticDistributionLineRepository analyticDistributionLineRepo,
      BudgetToolsService budgetToolsService,
      CurrencyScaleService currencyScaleService) {
    this.budgetLineRepository = budgetLineRepository;
    this.budgetRepository = budgetRepository;
    this.budgetLevelRepository = budgetLevelRepository;
    this.budgetDistributionRepo = budgetDistributionRepo;
    this.budgetLineService = budgetLineService;
    this.accountConfigService = accountConfigService;
    this.analyticMoveLineService = analyticMoveLineService;
    this.budgetDistributionRepository = budgetDistributionRepository;
    this.accountRepo = accountRepo;
    this.analyticDistributionLineRepo = analyticDistributionLineRepo;
    this.budgetToolsService = budgetToolsService;
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public BigDecimal computeTotalAmount(Budget budget) {
    BigDecimal total = BigDecimal.ZERO;
    if (budget.getBudgetLineList() != null) {
      for (BudgetLine budgetLine : budget.getBudgetLineList()) {
        total =
            currencyScaleService.getCompanyScaledValue(
                budget, total.add(budgetLine.getAmountExpected()));
      }
    }
    return total;
  }

  @Override
  @Transactional(rollbackOn = {RuntimeException.class})
  public List<BudgetLine> updateLines(Budget budget) {

    List<BudgetLine> budgetLineList = new ArrayList<>();

    if (budget != null) {
      budgetLineList = budget.getBudgetLineList();

      if (CollectionUtils.isNotEmpty(budgetLineList)) {
        for (BudgetLine budgetLine : budgetLineList) {
          budgetLine.setAmountCommitted(BigDecimal.ZERO);
          budgetLine.setAmountPaid(BigDecimal.ZERO);
        }
      }

      List<BudgetDistribution> budgetDistributionList =
          budgetDistributionRepo
              .all()
              .filter(
                  "self.budget.id = ?1 AND ((self.purchaseOrderLine IS NOT NULL AND self.purchaseOrderLine.purchaseOrder.statusSelect NOT IN (?2)) OR (self.saleOrderLine IS NOT NULL AND self.saleOrderLine.saleOrder.statusSelect NOT IN (?3)))",
                  budget.getId(),
                  PurchaseOrderRepository.STATUS_CANCELED,
                  SaleOrderRepository.STATUS_CANCELED)
              .fetch();
      for (BudgetDistribution budgetDistribution : budgetDistributionList) {
        boolean isPurchase = false;
        LocalDate orderDate = null;
        Integer statusSelect = 0;
        BigDecimal amountInvoiced = BigDecimal.ZERO;
        if (budgetDistribution.getPurchaseOrderLine() != null
            && budgetDistribution.getPurchaseOrderLine().getPurchaseOrder() != null) {
          isPurchase = true;
          orderDate = budgetDistribution.getPurchaseOrderLine().getPurchaseOrder().getOrderDate();
          statusSelect =
              budgetDistribution.getPurchaseOrderLine().getPurchaseOrder().getStatusSelect();
          amountInvoiced =
              currencyScaleService.getCompanyScaledValue(
                  budget,
                  budgetDistribution.getPurchaseOrderLine().getPurchaseOrder().getAmountInvoiced());
        } else if (budgetDistribution.getSaleOrderLine() != null
            && budgetDistribution.getSaleOrderLine().getSaleOrder() != null) {
          orderDate =
              budgetDistribution.getSaleOrderLine().getSaleOrder().getOrderDate() != null
                  ? budgetDistribution.getSaleOrderLine().getSaleOrder().getOrderDate()
                  : budgetDistribution.getSaleOrderLine().getSaleOrder().getCreationDate();
          statusSelect = budgetDistribution.getSaleOrderLine().getSaleOrder().getStatusSelect();
          amountInvoiced =
              currencyScaleService.getCompanyScaledValue(
                  budget, budgetDistribution.getSaleOrderLine().getSaleOrder().getAmountInvoiced());
        }
        if (orderDate != null) {
          for (BudgetLine budgetLine : budgetLineList) {
            LocalDate fromDate = budgetLine.getFromDate();
            LocalDate toDate = budgetLine.getToDate();
            if (fromDate != null
                && toDate != null
                && (fromDate.isBefore(orderDate) || fromDate.isEqual(orderDate))
                && (toDate.isAfter(orderDate) || toDate.isEqual(orderDate))) {
              if ((isPurchase
                      && (statusSelect == PurchaseOrderRepository.STATUS_VALIDATED
                          || statusSelect == PurchaseOrderRepository.STATUS_FINISHED)
                  || (!isPurchase
                      && (statusSelect == SaleOrderRepository.STATUS_FINALIZED_QUOTATION
                          || statusSelect == SaleOrderRepository.STATUS_ORDER_COMPLETED
                          || statusSelect == SaleOrderRepository.STATUS_ORDER_CONFIRMED)))) {
                budgetLine.setAmountPaid(
                    currencyScaleService.getCompanyScaledValue(
                        budget, budgetLine.getAmountPaid().add(amountInvoiced)));
              }
              if (amountInvoiced.compareTo(BigDecimal.ZERO) == 0) {
                budgetLine.setAmountCommitted(
                    currencyScaleService.getCompanyScaledValue(
                        budget,
                        budgetLine.getAmountCommitted().add(budgetDistribution.getAmount())));
              }
              budgetLine.setToBeCommittedAmount(
                  currencyScaleService.getCompanyScaledValue(
                      budget,
                      budgetLine.getAmountExpected().subtract(budgetLine.getAmountCommitted())));
              budgetLineRepository.save(budgetLine);
              break;
            }
          }
        }
      }
    }

    return budgetLineList;
  }

  @Override
  @Transactional(rollbackOn = {RuntimeException.class})
  public BigDecimal computeTotalAmountPaid(Budget budget) {

    List<BudgetLine> budgetLineList = budget.getBudgetLineList();
    BigDecimal totalAmountPaid = BigDecimal.ZERO;

    if (budgetLineList != null) {
      totalAmountPaid =
          budgetLineList.stream()
              .map(BudgetLine::getAmountPaid)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    budget.setTotalAmountPaid(currencyScaleService.getCompanyScaledValue(budget, totalAmountPaid));
    budgetRepository.save(budget);
    return totalAmountPaid;
  }

  @Override
  @Transactional
  public BigDecimal computeTotalAmountCommitted(Budget budget) {
    List<BudgetLine> budgetLineList = budget.getBudgetLineList();

    if (budgetLineList == null) {
      return BigDecimal.ZERO;
    }

    BigDecimal totalAmountCommitted =
        currencyScaleService.getCompanyScaledValue(
            budget,
            budgetLineList.stream()
                .map(BudgetLine::getAmountCommitted)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

    budget.setTotalAmountCommitted(totalAmountCommitted);

    return totalAmountCommitted;
  }

  @Override
  @Transactional
  public void updateBudgetDates(Budget budget, LocalDate fromDate, LocalDate toDate) {
    budget = budgetRepository.find(budget.getId());
    budget.setFromDate(fromDate);
    budget.setToDate(toDate);
    budgetRepository.save(budget);
  }

  @Override
  @Transactional
  public BigDecimal computeTotalAmountRealized(Budget budget) {

    BigDecimal totalAmountRealized = BigDecimal.ZERO;
    BigDecimal realizedWithPo = BigDecimal.ZERO;
    BigDecimal realizedWithNoPo = BigDecimal.ZERO;

    List<BudgetLine> budgetLineList = budget.getBudgetLineList();

    if (!CollectionUtils.isEmpty(budgetLineList)) {
      totalAmountRealized =
          currencyScaleService.getCompanyScaledValue(
              budget,
              budgetLineList.stream()
                  .map(BudgetLine::getAmountRealized)
                  .reduce(BigDecimal.ZERO, BigDecimal::add));
      realizedWithPo =
          currencyScaleService.getCompanyScaledValue(
              budget,
              budgetLineList.stream()
                  .map(BudgetLine::getRealizedWithPo)
                  .reduce(BigDecimal.ZERO, BigDecimal::add));

      realizedWithNoPo =
          currencyScaleService.getCompanyScaledValue(
              budget,
              budgetLineList.stream()
                  .map(BudgetLine::getRealizedWithNoPo)
                  .reduce(BigDecimal.ZERO, BigDecimal::add));

      budget.setTotalAmountRealized(totalAmountRealized);
      budget.setRealizedWithPo(realizedWithPo);
      budget.setRealizedWithNoPo(realizedWithNoPo);
    }

    return totalAmountRealized;
  }

  @Override
  public BigDecimal computeTotalSimulatedAmount(
      Move move, Budget budget, boolean excludeMoveInSimulated) {

    String query = "self.budget.id = ?1 AND self.moveLine.move.statusSelect = ?2";
    if (excludeMoveInSimulated) {
      query += " AND self.moveLine.move != " + move.getId();
    }
    List<BudgetDistribution> budgetDistributionList =
        budgetDistributionRepository
            .all()
            .filter(query, budget.getId(), MoveRepository.STATUS_SIMULATED)
            .fetch();
    BigDecimal simulatedAmount = BigDecimal.ZERO;
    for (BudgetDistribution budgetDistribution : budgetDistributionList) {
      simulatedAmount = simulatedAmount.add(budgetDistribution.getAmount());
    }
    budget.setSimulatedAmount(currencyScaleService.getCompanyScaledValue(budget, simulatedAmount));
    return simulatedAmount;
  }

  @Override
  public void computeTotalAvailableWithSimulatedAmount(Budget budget) {
    budget.setAvailableAmountWithSimulated(
        currencyScaleService.getCompanyScaledValue(
            budget,
            budget
                .getAvailableAmount()
                .subtract(budget.getSimulatedAmount())
                .max(BigDecimal.ZERO)));
  }

  @Override
  public void computeTotalFirmGap(Budget budget) {
    List<BudgetLine> budgetLineList = budget.getBudgetLineList();
    BigDecimal totalFirmGap = BigDecimal.ZERO;

    if (budgetLineList != null) {
      totalFirmGap =
          budgetLineList.stream()
              .map(BudgetLine::getFirmGap)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    budget.setTotalFirmGap(currencyScaleService.getCompanyScaledValue(budget, totalFirmGap));
  }

  @Override
  public List<BudgetLine> generatePeriods(Budget budget) throws AxelorException {
    if (budget.getBudgetLineList() != null && !budget.getBudgetLineList().isEmpty()) {
      List<BudgetLine> budgetLineList = budget.getBudgetLineList();
      budgetLineList.clear();
    }

    List<BudgetLine> budgetLineList = new ArrayList<BudgetLine>();
    Integer duration = budget.getPeriodDurationSelect();
    LocalDate fromDate = budget.getFromDate();
    LocalDate toDate = budget.getToDate();
    LocalDate budgetLineToDate = fromDate;
    Integer budgetLineNumber = 1;

    int c = 0;
    int loopLimit = 1000;
    while (budgetLineToDate.isBefore(toDate)) {
      if (budgetLineNumber != 1 && duration != 0) {
        fromDate = fromDate.plusMonths(duration);
      }
      if (c >= loopLimit) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get(BudgetExceptionMessage.BUDGET_1));
      }
      c += 1;
      budgetLineToDate = duration == 0 ? toDate : fromDate.plusMonths(duration).minusDays(1);
      if (budgetLineToDate.isAfter(toDate)) {
        budgetLineToDate = toDate;
      }
      if (fromDate.isAfter(toDate)) {
        continue;
      }
      BudgetLine budgetLine = new BudgetLine();
      budgetLine.setFromDate(fromDate);
      budgetLine.setToDate(budgetLineToDate);
      budgetLine.setBudget(budget);
      budgetLine.setAmountExpected(
          currencyScaleService.getCompanyScaledValue(budget, budget.getAmountForGeneration()));
      budgetLine.setAvailableAmount(
          currencyScaleService.getCompanyScaledValue(budget, budget.getAmountForGeneration()));
      budgetLine.setToBeCommittedAmount(
          currencyScaleService.getCompanyScaledValue(budget, budget.getAmountForGeneration()));

      budgetLineList.add(budgetLine);
      budget.addBudgetLineListItem(budgetLine);
      budgetLineNumber++;
      if (duration == 0) {
        break;
      }
    }
    return budgetLineList;
  }

  @Override
  public BigDecimal computeToBeCommittedAmount(Budget budget) {
    BigDecimal total = BigDecimal.ZERO;
    if (budget.getBudgetLineList() != null) {
      for (BudgetLine budgetLine : budget.getBudgetLineList()) {
        total =
            currencyScaleService.getCompanyScaledValue(
                budget, total.add(budgetLine.getToBeCommittedAmount()));
      }
    }

    return total;
  }

  @Override
  public BigDecimal computeFirmGap(Budget budget) {
    BigDecimal total = BigDecimal.ZERO;
    if (budget.getBudgetLineList() != null) {
      for (BudgetLine budgetLine : budget.getBudgetLineList()) {
        total =
            currencyScaleService.getCompanyScaledValue(budget, total.add(budgetLine.getFirmGap()));
      }
    }
    budget.setTotalFirmGap(total);

    return total;
  }

  @Transactional
  @Override
  public void validateBudget(Budget budget, boolean checkBudgetKey) throws AxelorException {
    if (budget != null) {
      GlobalBudget globalBudget = budgetToolsService.getGlobalBudgetUsingBudget(budget);
      if (checkBudgetKey && globalBudget != null) {
        String error = computeBudgetKey(budget, globalBudget.getCompany());
        if (!Strings.isNullOrEmpty(error)) {
          throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, error);
        }
        if (Strings.isNullOrEmpty(budget.getBudgetKey())) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_NO_VALUE,
              String.format(
                  I18n.get(BudgetExceptionMessage.BUDGET_MISSING_BUDGET_KEY), budget.getCode()));
        }
      }
      budget.setStatusSelect(BudgetRepository.STATUS_VALIDATED);
      budgetRepository.save(budget);

      budget.setGlobalBudget(globalBudget);
    }
  }

  @Transactional
  @Override
  public void draftBudget(Budget budget) {
    if (budget != null) {
      budget = budgetRepository.find(budget.getId());
      budget.setStatusSelect(BudgetRepository.STATUS_DRAFT);
      budget.setActiveVersionExpectedAmountsLine(null);
    }
  }

  @Override
  public String checkPreconditions(Budget budget, Company company) {
    if (budget.getAnalyticAxis() == null || budget.getAnalyticAccount() == null) {
      return String.format(
          I18n.get(BudgetExceptionMessage.BUDGET_ANALYTIC_EMPTY),
          company.getName(),
          budget.getCode());
    }
    if (CollectionUtils.isEmpty(budget.getAccountSet())) {
      return String.format(
          I18n.get(BudgetExceptionMessage.BUDGET_ACCOUNT_EMPTY),
          company.getName(),
          budget.getCode());
    }
    return null;
  }

  @Override
  public String computeBudgetKey(Budget budget, Company company) {
    String key = computeKey(budget, company);
    if (!Strings.isNullOrEmpty(key)) {
      if (!checkUniqueKey(budget, key)) {
        return String.format(
            I18n.get(BudgetExceptionMessage.BUDGET_SAME_BUDGET_KEY), budget.getCode());
      } else {
        budget.setBudgetKey(key);
      }
    }
    return null;
  }

  @Override
  public String computeKey(Budget budget, Company company) {
    if (company != null
        && !CollectionUtils.isEmpty(budget.getAccountSet())
        && budget.getAnalyticAxis() != null
        && budget.getAnalyticAccount() != null) {
      String analyticKey =
          computeAnalyticDistributionLineKey(budget.getAnalyticAxis(), budget.getAnalyticAccount());
      if (Strings.isNullOrEmpty(analyticKey)) {
        return null;
      }
      String key = "";
      for (Account account : budget.getAccountSet()) {
        key =
            key.concat(
                String.format(";%s-%s-%s", company.getCode(), account.getCode(), analyticKey));
      }
      key = key.substring(1);
      return key;
    }
    return null;
  }

  @Override
  public String computeKey(Account account, Company company, AnalyticMoveLine analyticMoveLine) {
    if (company != null && account != null && analyticMoveLine != null) {
      String analyticKey = computeAnalyticMoveLineKey(analyticMoveLine);
      if (Strings.isNullOrEmpty(analyticKey)) {
        return null;
      }
      return company.getCode().concat(String.format("-%s-%s", account.getCode(), analyticKey));
    }
    return null;
  }

  @Override
  public String computeAnalyticMoveLineKey(AnalyticMoveLine analyticMoveLine) {
    String analyticKey = "";
    if (analyticMoveLine != null
        && analyticMoveLine.getAnalyticAxis() != null
        && analyticMoveLine.getAnalyticAccount() != null) {
      analyticKey =
          String.format(
              "%s:%s",
              analyticMoveLine.getAnalyticAxis().getCode(),
              analyticMoveLine.getAnalyticAccount().getCode());
    }

    return analyticKey;
  }

  @Override
  public String computeAnalyticDistributionLineKey(
      AnalyticAxis analyticAxis, AnalyticAccount analyticAccount) {

    if (analyticAxis != null && analyticAccount != null) {
      String analyticKey =
          String.format("%s:%s", analyticAxis.getCode(), analyticAccount.getCode());
      return analyticKey;
    }
    return null;
  }

  @Override
  public boolean checkUniqueKey(Budget budget, String key) {
    if (key != null) {
      List<Budget> repoBudgetKeys =
          budgetRepository
              .all()
              .filter(
                  "self.budgetKey != null and self.id != ?1 AND self.globalBudget.statusSelect != ?2",
                  budget.getId() != null ? budget.getId() : new Long(0),
                  GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_ARCHIVED)
              .fetch();

      if (CollectionUtils.isEmpty(repoBudgetKeys)) {
        return true;
      }
      List<String> keys = Arrays.asList(key.split(";"));

      if (!CollectionUtils.isEmpty(repoBudgetKeys) && !CollectionUtils.isEmpty(keys)) {
        for (String keyStr : keys) {
          for (Budget budgetKey : repoBudgetKeys) {
            if (budgetKey.getBudgetKey().contains(keyStr) && isInSameDates(budget, budgetKey)) {
              return false;
            }
          }
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public Budget findBudgetWithKey(String key, LocalDate date) {
    List<Budget> budgetList =
        budgetRepository
            .all()
            .filter(
                "self.globalBudget.statusSelect = :statusSelect AND self.budgetKey LIKE '%"
                    + key
                    + "%'")
            .bind("statusSelect", GlobalBudgetRepository.GLOBAL_BUDGET_STATUS_SELECT_VALID)
            .fetch();
    if (!CollectionUtils.isEmpty(budgetList) && date != null) {
      for (Budget budget : budgetList) {
        if ((budget.getFromDate().isBefore(date) || budget.getFromDate().isEqual(date))
            && (budget.getToDate().isAfter(date) || budget.getToDate().isEqual(date))) {
          return budget;
        }
      }
    }
    return null;
  }

  @Override
  @Transactional
  public void updateLinesFromMove(
      List<BudgetDistribution> budgetDistributionList,
      Move move,
      MoveLine moveLine,
      boolean excludeMoveInSimulated) {
    boolean isBudgetImputed = false;
    if (budgetDistributionList != null) {
      for (BudgetDistribution budgetDistribution : budgetDistributionList) {
        if (updateLineFromMove(budgetDistribution, move, moveLine)) {
          isBudgetImputed = true;
        }
        Budget budget = budgetDistribution.getBudget();
        if (budget != null) {
          computeTotalAmountRealized(budget);
          computeTotalFirmGap(budget);
          computeTotalSimulatedAmount(move, budget, excludeMoveInSimulated);
          computeTotalAvailableWithSimulatedAmount(budget);
          budgetRepository.save(budget);
        }
      }
      if (isBudgetImputed) {
        moveLine.setIsBudgetImputed(true);
      }
    }
  }

  @Override
  @Transactional
  public void updateBudgetLinesFromMove(Move move, boolean excludeMoveInSimulated) {

    if (move.getStatusSelect() == MoveRepository.STATUS_NEW || move.getInvoice() != null) {
      return;
    }

    if (!CollectionUtils.isEmpty(move.getMoveLineList())) {
      move.getMoveLineList().stream()
          .filter(moveLine -> CollectionUtils.isNotEmpty(moveLine.getBudgetDistributionList()))
          .forEach(
              moveLine -> {
                updateLinesFromMove(
                    moveLine.getBudgetDistributionList(), move, moveLine, excludeMoveInSimulated);
              });
    }
  }

  @Override
  @Transactional
  public boolean updateLineFromMove(
      BudgetDistribution budgetDistribution, Move move, MoveLine moveLine) {
    if (budgetDistribution != null && budgetDistribution.getBudget() != null) {
      LocalDate date = move.getDate();
      budgetDistribution.setImputationDate(date);
      Budget budget = budgetDistribution.getBudget();
      Optional<BudgetLine> optBudgetLine =
          budgetLineService.findBudgetLineAtDate(budget.getBudgetLineList(), date);
      if (optBudgetLine.isPresent()) {
        BudgetLine budgetLine = optBudgetLine.get();
        if ((move.getStatusSelect() == MoveRepository.STATUS_ACCOUNTED
                || move.getStatusSelect() == MoveRepository.STATUS_DAYBOOK)
            && !moveLine.getIsBudgetImputed()) {
          updateBudgetLineAmounts(budgetLine, budget, budgetDistribution.getAmount());
          return true;
        } else if (move.getStatusSelect() == MoveRepository.STATUS_CANCELED) {
          updateBudgetLineAmounts(budgetLine, budget, budgetDistribution.getAmount().negate());
        }
      }
    }
    return false;
  }

  @Override
  public void updateBudgetLineAmounts(BudgetLine budgetLine, Budget budget, BigDecimal amount) {
    budgetLine.setRealizedWithNoPo(
        currencyScaleService.getCompanyScaledValue(
            budget, BigDecimal.ZERO.max(budgetLine.getRealizedWithNoPo().add(amount))));
    updateOtherAmounts(budgetLine, budget, amount);
  }

  @Override
  public void updateBudgetLineAmountWithPo(
      BudgetLine budgetLine, Budget budget, BigDecimal amount) {
    budgetLine.setRealizedWithPo(
        currencyScaleService.getCompanyScaledValue(
            budget, BigDecimal.ZERO.max(budgetLine.getRealizedWithPo().add(amount))));
    budgetLine.setAmountCommitted(
        currencyScaleService.getCompanyScaledValue(
            budget, BigDecimal.ZERO.max(budgetLine.getAmountCommitted().subtract(amount))));
    updateOtherAmounts(budgetLine, budget, amount);
  }

  protected void updateOtherAmounts(BudgetLine budgetLine, Budget budget, BigDecimal amount) {
    budgetLine.setAmountRealized(
        currencyScaleService.getCompanyScaledValue(
            budget, BigDecimal.ZERO.max(budgetLine.getAmountRealized().add(amount))));
    budgetLine.setToBeCommittedAmount(
        currencyScaleService.getCompanyScaledValue(
            budget, BigDecimal.ZERO.max(budgetLine.getToBeCommittedAmount().subtract(amount))));
    BigDecimal firmGap =
        currencyScaleService.getCompanyScaledValue(
            budget,
            budgetLine
                .getAmountExpected()
                .subtract(budgetLine.getRealizedWithPo().add(budgetLine.getRealizedWithNoPo())));
    budgetLine.setFirmGap(firmGap.signum() >= 0 ? BigDecimal.ZERO : firmGap.abs());
    budgetLine.setAvailableAmount(
        currencyScaleService.getCompanyScaledValue(
            budget, BigDecimal.ZERO.max(budgetLine.getAvailableAmount().subtract(amount))));
  }

  @Override
  public void createBudgetKey(Budget budget, Company company) throws AxelorException {
    if (budget == null || company == null) {
      return;
    }

    if (budgetToolsService.checkBudgetKeyInConfig(company)) {
      String errorMessage = this.checkPreconditions(budget, company);

      if (!Strings.isNullOrEmpty(errorMessage)) {
        throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, errorMessage);
      }
      errorMessage = this.computeBudgetKey(budget, company);
      if (!Strings.isNullOrEmpty(errorMessage)) {
        throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, errorMessage);
      }
    } else {
      budget.setBudgetKey("");
    }
  }

  @Override
  public String getAccountIdList(Long companyId, int budgetType) {
    if (companyId > 0) {
      List<String> accountTypeList = new ArrayList<>();

      switch (budgetType) {
        case GlobalBudgetRepository.GLOBAL_BUDGET_BUDGET_TYPE_SELECT_PURCHASE_AND_INVESTMENT:
          accountTypeList.add(AccountTypeRepository.TYPE_IMMOBILISATION);
          accountTypeList.add(AccountTypeRepository.TYPE_CHARGE);
        case GlobalBudgetRepository.GLOBAL_BUDGET_BUDGET_TYPE_SELECT_PURCHASE:
          accountTypeList.add(AccountTypeRepository.TYPE_CHARGE);
          break;
        case GlobalBudgetRepository.GLOBAL_BUDGET_BUDGET_TYPE_SELECT_SALE:
          accountTypeList.add(AccountTypeRepository.TYPE_INCOME);
          break;
        case GlobalBudgetRepository.GLOBAL_BUDGET_BUDGET_TYPE_SELECT_INVESTMENT:
          accountTypeList.add(AccountTypeRepository.TYPE_IMMOBILISATION);
          break;
      }

      List<Account> accountList =
          accountRepo
              .all()
              .filter(
                  "self.accountType.technicalTypeSelect IN (:accountTypeList) "
                      + "AND self.company.id = :companyId "
                      + "AND self.statusSelect = :statusNew")
              .bind("accountTypeList", accountTypeList)
              .bind("companyId", companyId.toString())
              .bind("statusNew", AccountRepository.STATUS_ACTIVE)
              .fetch();

      if (CollectionUtils.isNotEmpty(accountList)) {
        return accountList.stream()
            .map(Account::getId)
            .map(Object::toString)
            .collect(Collectors.joining(","));
      }
    }

    return "(0)";
  }

  @Override
  public boolean isInSameDates(Budget budget, Budget budgetKey) {
    if (budget != null
        && budget.getFromDate() != null
        && budget.getToDate() != null
        && budgetKey != null
        && budgetKey.getFromDate() != null
        && budgetKey.getToDate() != null) {
      return !((budget.getFromDate().isAfter(budgetKey.getFromDate())
              && budget.getFromDate().isAfter(budgetKey.getToDate()))
          || (budget.getToDate().isBefore(budgetKey.getFromDate())
              && budget.getToDate().isBefore(budgetKey.getToDate())));
    }
    return false;
  }

  @Override
  public void checkDatesOnBudget(Budget budget) throws AxelorException {
    if (budget == null) {
      return;
    }

    checkBudgetParentDates(budget);
    checkBudgetLinesDates(budget);
  }

  @Override
  public void checkBudgetParentDates(Budget budget) throws AxelorException {
    if (budget == null
        || (budget.getBudgetLevel() == null && budget.getGlobalBudget() == null)
        || budget.getFromDate() == null
        || budget.getToDate() == null) {
      return;
    }
    LocalDate parentFromDate = null;
    LocalDate parentToDate = null;
    if (budget.getBudgetLevel() != null) {
      parentFromDate = budget.getBudgetLevel().getFromDate();
      parentToDate = budget.getBudgetLevel().getToDate();
    } else if (budget.getGlobalBudget() != null) {
      parentFromDate = budget.getGlobalBudget().getFromDate();
      parentToDate = budget.getGlobalBudget().getToDate();
    }

    if (budget.getFromDate() == null
        || budget.getToDate() == null
        || (budget.getFromDate().isBefore(parentFromDate))
        || (budget.getToDate().isAfter(parentToDate))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          String.format(I18n.get(BudgetExceptionMessage.WRONG_DATES_ON_BUDGET), budget.getCode()));
    }
  }

  @Override
  public void checkBudgetLinesDates(Budget budget) throws AxelorException {
    if (budget != null && !CollectionUtils.isEmpty(budget.getBudgetLineList())) {
      List<BudgetLine> budgetLineList = new ArrayList(budget.getBudgetLineList());
      BudgetLine budgetLine = null;
      while (!CollectionUtils.isEmpty(budgetLineList)) {
        budgetLine = budgetLineList.get(0);
        if (budgetLine.getFromDate().isBefore(budget.getFromDate())
            || budgetLine.getToDate().isAfter(budget.getToDate())) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              String.format(
                  I18n.get(BudgetExceptionMessage.WRONG_DATES_ON_BUDGET_LINE), budget.getCode()));
        }
        budgetLineList.remove(budgetLine);
        if (!CollectionUtils.isEmpty(budgetLineList)) {
          for (BudgetLine bl : budgetLineList) {
            if (LocalDateHelper.isBetween(
                    budgetLine.getFromDate(), budgetLine.getToDate(), bl.getFromDate())
                || LocalDateHelper.isBetween(
                    budgetLine.getFromDate(), budgetLine.getToDate(), bl.getToDate())) {
              throw new AxelorException(
                  TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                  String.format(
                      I18n.get(BudgetExceptionMessage.BUDGET_LINES_ON_SAME_PERIOD),
                      budget.getCode()));
            }
          }
        }
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void getUpdatedBudgetLineList(Budget budget, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {

    if (!CollectionUtils.isEmpty(budget.getBudgetLineList())) {
      budget = budgetRepository.find(budget.getId());
      if (budget.getPeriodDurationSelect() == null) {
        budget.setPeriodDurationSelect(0);
      }
      budget.setAmountForGeneration(budget.getTotalAmountExpected());
      generatePeriods(budget);
    }
  }

  @Override
  public List<Long> getAnalyticAxisInConfig(Company company) throws AxelorException {
    List<Long> idList = new ArrayList<Long>();
    if (company != null) {
      List<AnalyticAxisByCompany> analyticAxisByCompanyList =
          accountConfigService.getAccountConfig(company).getAnalyticAxisByCompanyList();
      if (!CollectionUtils.isEmpty(analyticAxisByCompanyList)) {
        analyticAxisByCompanyList.stream()
            .filter(AnalyticAxisByCompany::getIncludeInBudgetKey)
            .forEach(axis -> idList.add(axis.getAnalyticAxis().getId()));
      }
    }
    return idList;
  }

  @Override
  public void validateBudgetDistributionAmounts(
      List<BudgetDistribution> budgetDistributionList, BigDecimal amount, String code)
      throws AxelorException {
    if (!CollectionUtils.isEmpty(budgetDistributionList)) {
      BigDecimal totalAmount =
          budgetDistributionList.stream()
              .map(BudgetDistribution::getAmount)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);
      if (this.isGreaterThan(
          totalAmount, amount, budgetDistributionList.stream().findFirst().get())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BudgetExceptionMessage.BUDGET_EXCEED_ORDER_LINE_AMOUNT),
            code);
      }
    }
  }

  protected boolean isGreaterThan(
      BigDecimal amount1, BigDecimal amount2, BudgetDistribution budgetDistribution) {
    amount1 = currencyScaleService.getCompanyScaledValue(budgetDistribution, amount1);
    amount2 = currencyScaleService.getCompanyScaledValue(budgetDistribution, amount2);

    return amount1.compareTo(amount2) > 0;
  }

  @Override
  public void computeAvailableFields(Budget budget) {
    budget.setAvailableAmount(
        (budget
                .getTotalAmountExpected()
                .subtract(budget.getRealizedWithPo())
                .subtract(budget.getRealizedWithNoPo()))
            .max(BigDecimal.ZERO));
    computeTotalAvailableWithSimulatedAmount(budget);
    computeTotalFirmGap(budget);
  }

  @Override
  @Transactional
  public void archiveBudget(Budget budget) {
    if (budget != null) {
      budget.setArchived(true);
      budgetRepository.save(budget);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void generateLineFromGenerator(
      Budget budget, BudgetLevel parent, GlobalBudget globalBudget) throws AxelorException {
    Budget optBudget = budgetRepository.copy(budget, true);
    optBudget.setFromDate(globalBudget.getFromDate());
    optBudget.setToDate(globalBudget.getToDate());
    optBudget.setTypeSelect(BudgetRepository.BUDGET_TYPE_SELECT_BUDGET);
    optBudget.setSourceSelect(BudgetRepository.BUDGET_SOURCE_AUTO);
    optBudget.setAmountForGeneration(
        currencyScaleService.getCompanyScaledValue(budget, budget.getTotalAmountExpected()));
    optBudget.setAvailableAmount(
        currencyScaleService.getCompanyScaledValue(budget, budget.getTotalAmountExpected()));
    optBudget.setAvailableAmountWithSimulated(
        currencyScaleService.getCompanyScaledValue(budget, budget.getTotalAmountExpected()));
    optBudget.setBudgetStructure(null);
    generatePeriods(optBudget);
    if (parent != null) {
      parent.addBudgetListItem(optBudget);
    }
    budgetRepository.save(optBudget);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void generateLineFromGenerator(
      BudgetScenarioVariable budgetScenarioVariable,
      BudgetLevel parent,
      Map<String, Object> variableAmountMap,
      GlobalBudget globalBudget)
      throws AxelorException {
    Budget optBudget = new Budget();
    optBudget.setCode(budgetScenarioVariable.getCode());
    optBudget.setName(budgetScenarioVariable.getName());
    optBudget.setFromDate(globalBudget.getFromDate());
    optBudget.setToDate(globalBudget.getToDate());
    optBudget.setStatusSelect(BudgetRepository.STATUS_DRAFT);
    optBudget.setTypeSelect(BudgetRepository.BUDGET_TYPE_SELECT_BUDGET);
    optBudget.setSourceSelect(BudgetRepository.BUDGET_SOURCE_AUTO);
    optBudget.setCategory(budgetScenarioVariable.getCategory());
    BigDecimal calculatedAmount =
        currencyScaleService.getCompanyScaledValue(
            parent,
            ((BigDecimal)
                variableAmountMap.getOrDefault(budgetScenarioVariable.getCode(), BigDecimal.ZERO)));
    optBudget.setAmountForGeneration(calculatedAmount);
    optBudget.setTotalAmountExpected(calculatedAmount);
    optBudget.setAvailableAmount(calculatedAmount);
    optBudget.setAvailableAmountWithSimulated(optBudget.getAmountForGeneration());
    optBudget.setPeriodDurationSelect(0);
    generatePeriods(optBudget);
    if (parent != null) {
      parent.addBudgetListItem(optBudget);
    }
    if (globalBudget != null) {
      globalBudget.addBudgetListItem(optBudget);
    }

    budgetRepository.save(optBudget);
  }
}
