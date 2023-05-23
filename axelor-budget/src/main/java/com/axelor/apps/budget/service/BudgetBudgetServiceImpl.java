package com.axelor.apps.budget.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.AnalyticDistributionLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.budget.db.Budget;
import com.axelor.apps.budget.db.BudgetDistribution;
import com.axelor.apps.budget.db.BudgetLevel;
import com.axelor.apps.budget.db.BudgetLine;
import com.axelor.apps.budget.db.repo.BudgetDistributionRepository;
import com.axelor.apps.budget.db.repo.BudgetLevelRepository;
import com.axelor.apps.budget.db.repo.BudgetLineRepository;
import com.axelor.apps.budget.db.repo.BudgetRepository;
import com.axelor.apps.budget.exception.IExceptionMessage;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.i18n.I18n;
import com.axelor.utils.date.DateTool;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class BudgetBudgetServiceImpl implements BudgetBudgetService {

  protected BudgetLineRepository budgetLineRepository;
  protected BudgetRepository budgetRepository;
  protected BudgetLevelRepository budgetLevelRepository;
  protected PurchaseOrderLineRepository purchaseOrderLineRepo;
  protected BudgetDistributionRepository budgetDistributionRepo;
  protected BudgetLineService budgetLineService;
  protected AppAccountService appAccountService;
  protected AccountConfigService accountConfigService;
  protected AnalyticMoveLineService analyticMoveLineService;
  protected BudgetDistributionRepository budgetDistributionRepository;
  protected BudgetLevelService budgetLevelService;
  protected AccountRepository accountRepo;
  protected AnalyticDistributionLineRepository analyticDistributionLineRepo;
  protected BudgetInvoiceService budgetInvoiceService;

  @Inject
  public BudgetBudgetServiceImpl(
      BudgetLineRepository budgetLineRepository,
      BudgetRepository budgetRepository,
      BudgetLevelRepository budgetLevelRepository,
      PurchaseOrderLineRepository purchaseOrderLineRepo,
      BudgetDistributionRepository budgetDistributionRepo,
      BudgetLineService budgetLineService,
      AppAccountService appAccountService,
      AccountConfigService accountConfigService,
      AnalyticMoveLineService analyticMoveLineService,
      BudgetDistributionRepository budgetDistributionRepository,
      BudgetLevelService budgetLevelService,
      AccountRepository accountRepo,
      AnalyticDistributionLineRepository analyticDistributionLineRepo,
      BudgetInvoiceService budgetInvoiceService) {
    this.budgetLineRepository = budgetLineRepository;
    this.budgetRepository = budgetRepository;
    this.budgetLevelRepository = budgetLevelRepository;
    this.purchaseOrderLineRepo = purchaseOrderLineRepo;
    this.budgetDistributionRepo = budgetDistributionRepo;
    this.budgetLineService = budgetLineService;
    this.appAccountService = appAccountService;
    this.accountConfigService = accountConfigService;
    this.analyticMoveLineService = analyticMoveLineService;
    this.budgetDistributionRepository = budgetDistributionRepository;
    this.budgetLevelService = budgetLevelService;
    this.accountRepo = accountRepo;
    this.analyticDistributionLineRepo = analyticDistributionLineRepo;
    this.budgetInvoiceService = budgetInvoiceService;
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
              budgetDistribution.getPurchaseOrderLine().getPurchaseOrder().getAmountInvoiced();
        } else if (budgetDistribution.getSaleOrderLine() != null
            && budgetDistribution.getSaleOrderLine().getSaleOrder() != null) {
          orderDate =
              budgetDistribution.getSaleOrderLine().getSaleOrder().getOrderDate() != null
                  ? budgetDistribution.getSaleOrderLine().getSaleOrder().getOrderDate()
                  : budgetDistribution.getSaleOrderLine().getSaleOrder().getCreationDate();
          statusSelect = budgetDistribution.getSaleOrderLine().getSaleOrder().getStatusSelect();
          amountInvoiced = budgetDistribution.getSaleOrderLine().getSaleOrder().getAmountInvoiced();
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
                    budgetLine
                        .getAmountPaid()
                        .add(amountInvoiced)
                        .setScale(2, RoundingMode.HALF_UP));
              }
              if (amountInvoiced.compareTo(BigDecimal.ZERO) == 0) {
                budgetLine.setAmountCommitted(
                    budgetLine.getAmountCommitted().add(budgetDistribution.getAmount()));
              }
              budgetLine.setToBeCommittedAmount(
                  budgetLine.getAmountExpected().subtract(budgetLine.getAmountCommitted()));
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

    budget.setTotalAmountPaid(totalAmountPaid);
    budgetRepository.save(budget);
    return totalAmountPaid;
  }

  @Override
  @Transactional(rollbackOn = {RuntimeException.class})
  public void computeBudgetLevelTotals(Budget budget) {

    BudgetLevel sectionBudgetLevel = budget.getBudgetLevel();

    if (sectionBudgetLevel != null) {

      budgetLevelService.computeBudgetTotals(sectionBudgetLevel);
      budgetLevelRepository.save(sectionBudgetLevel);

      BudgetLevel groupBudgetLevel = sectionBudgetLevel.getParentBudgetLevel();
      if (groupBudgetLevel != null) {
        budgetLevelService.computeTotals(groupBudgetLevel);
        budgetLevelRepository.save(groupBudgetLevel);

        BudgetLevel globalBudgetLevel = groupBudgetLevel.getParentBudgetLevel();
        if (globalBudgetLevel != null) {
          budgetLevelService.computeTotals(globalBudgetLevel);
          budgetLevelRepository.save(groupBudgetLevel);
        }
      }
    }
  }

  @Override
  @Transactional
  public BigDecimal computeTotalAmountCommitted(Budget budget) {
    List<BudgetLine> budgetLineList = budget.getBudgetLineList();

    if (budgetLineList == null) {
      return BigDecimal.ZERO;
    }

    BigDecimal totalAmountCommitted =
        budgetLineList.stream()
            .map(BudgetLine::getAmountCommitted)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    budget.setTotalAmountCommitted(totalAmountCommitted);

    return totalAmountCommitted;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateBudgetDates(Budget budget, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    budget = budgetRepository.find(budget.getId());
    budget.setFromDate(fromDate);
    budget.setToDate(toDate);
    budgetRepository.save(budget);
  }

  @Override
  @Transactional
  public void updateBudgetLinesFromInvoice(Invoice invoice) {

    if (invoice.getStatusSelect() == InvoiceRepository.STATUS_VENTILATED) {
      if (invoice.getInvoiceLineList() != null) {
        invoice.getInvoiceLineList().stream()
            .filter(
                invoiceLine -> CollectionUtils.isNotEmpty(invoiceLine.getBudgetDistributionList()))
            .forEach(
                invoiceLine -> {
                  updateLinesFromInvoice(
                      invoiceLine.getBudgetDistributionList(), invoice, invoiceLine);
                });
      }
    }
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
          budgetLineList.stream()
              .map(BudgetLine::getAmountRealized)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
      realizedWithPo =
          budgetLineList.stream()
              .map(BudgetLine::getRealizedWithPo)
              .reduce(BigDecimal.ZERO, BigDecimal::add);

      realizedWithNoPo =
          budgetLineList.stream()
              .map(BudgetLine::getRealizedWithNoPo)
              .reduce(BigDecimal.ZERO, BigDecimal::add);

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
    budget.setSimulatedAmount(simulatedAmount);
    return simulatedAmount;
  }

  @Override
  public void computeTotalAvailableWithSimulatedAmount(Move move, Budget budget) {
    if (budget.getAvailableAmount().subtract(budget.getSimulatedAmount()).compareTo(BigDecimal.ZERO)
        > 0) {
      budget.setAvailableAmountWithSimulated(
          budget.getAvailableAmount().subtract(budget.getSimulatedAmount()));

    } else {
      budget.setAvailableAmountWithSimulated(BigDecimal.ZERO);
    }
  }

  @Transactional
  protected void updateLinesFromInvoice(
      List<BudgetDistribution> budgetDistributionList, Invoice invoice, InvoiceLine invoiceLine) {
    if (budgetDistributionList != null) {
      for (BudgetDistribution budgetDistribution : budgetDistributionList) {
        if (invoiceLine.getInvoice().getPurchaseOrder() != null
            || invoiceLine.getInvoice().getSaleOrder() != null) {
          budgetInvoiceService.updateLineWithPO(budgetDistribution, invoice, invoiceLine);
        } else {
          budgetInvoiceService.updateLineWithNoPO(budgetDistribution, invoice);
        }
        Budget budget = budgetDistribution.getBudget();
        if (budget != null) {
          computeTotalAmountRealized(budget);
          computeTotalFirmGap(budget);
          computeTotalAmountCommitted(budget);
          budgetRepository.save(budget);
        }
      }
    }
  }

  @Override
  @Transactional
  public void computeTotalFirmGap(Budget budget) {
    List<BudgetLine> budgetLineList = budget.getBudgetLineList();
    BigDecimal totalFirmGap = BigDecimal.ZERO;

    if (budgetLineList != null) {
      totalFirmGap =
          budgetLineList.stream()
              .map(BudgetLine::getFirmGap)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    budget.setTotalFirmGap(totalFirmGap);
    budgetRepository.save(budget);
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
            TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get(AccountExceptionMessage.BUDGET_1));
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
      budgetLine.setAmountExpected(budget.getAmountForGeneration());
      budgetLine.setAvailableAmount(budget.getAmountForGeneration());
      budgetLine.setToBeCommittedAmount(budget.getAmountForGeneration());
      budgetLineList.add(budgetLine);
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
        total = total.add(budgetLine.getToBeCommittedAmount());
      }
    }

    return total;
  }

  @Override
  public BigDecimal computeFirmGap(Budget budget) {
    BigDecimal total = BigDecimal.ZERO;
    if (budget.getBudgetLineList() != null) {
      for (BudgetLine budgetLine : budget.getBudgetLineList()) {
        total = total.add(budgetLine.getFirmGap());
      }
    }
    budget.setTotalFirmGap(total);

    return total;
  }

  @Override
  public boolean checkBudgetKeyInConfig(Company company) throws AxelorException {
    return (company != null
        && accountConfigService.getAccountConfig(company) != null
        && accountConfigService.getAccountConfig(company).getEnableBudgetKey());
  }

  @Transactional
  @Override
  public void validateBudget(Budget budget, boolean checkBudgetKey) throws AxelorException {
    if (budget != null) {
      if (checkBudgetKey && Strings.isNullOrEmpty(budget.getBudgetKey())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            String.format(I18n.get(IExceptionMessage.BUDGET_MISSING_BUDGET_KEY), budget.getCode()));
      }

      budget.setStatusSelect(BudgetRepository.STATUS_VALIDATED);
      budgetRepository.save(budget);
    }
  }

  @Transactional
  @Override
  public void draftBudget(Budget budget) {
    if (budget != null) {
      budget.setStatusSelect(BudgetRepository.STATUS_DRAFT);
      budgetRepository.save(budget);
    }
  }

  @Override
  public String checkPreconditions(Budget budget, Company company) {
    if (budget.getAnalyticAxis() == null || budget.getAnalyticAccount() == null) {
      return String.format(
          I18n.get(IExceptionMessage.BUDGET_ANALYTIC_EMPTY), company.getName(), budget.getCode());
    }
    if (CollectionUtils.isEmpty(budget.getAccountSet())) {
      return String.format(
          I18n.get(IExceptionMessage.BUDGET_ACCOUNT_EMPTY), company.getName(), budget.getCode());
    }
    return null;
  }

  @Override
  public String computeBudgetKey(Budget budget, Company company) {
    String key = computeKey(budget, company);
    if (!Strings.isNullOrEmpty(key)) {
      if (!checkUniqueKey(budget, key)) {
        return String.format(I18n.get(IExceptionMessage.BUDGET_SAME_BUDGET_KEY), budget.getCode());
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
                  "self.budgetKey != null and self.id != ?1 AND self.budgetLevel.parentBudgetLevel.parentBudgetLevel.statusSelect != ?2",
                  budget.getId() != null ? budget.getId() : new Long(0),
                  BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_ARCHIVED)
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
                "self.budgetLevel.parentBudgetLevel.parentBudgetLevel.statusSelect = :statusSelect AND self.budgetKey LIKE '%"
                    + key
                    + "%'")
            .bind("statusSelect", BudgetLevelRepository.BUDGET_LEVEL_STATUS_SELECT_VALID)
            .fetch();
    if (!CollectionUtils.isEmpty(budgetList)) {
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
          computeTotalAvailableWithSimulatedAmount(move, budget);
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
      Budget budget = budgetDistribution.getBudget();
      Optional<BudgetLine> optBudgetLine =
          budgetLineService.findBudgetLineAtDate(budget.getBudgetLineList(), date);
      if (optBudgetLine.isPresent()) {
        BudgetLine budgetLine = optBudgetLine.get();
        if ((move.getStatusSelect() == MoveRepository.STATUS_ACCOUNTED
                || move.getStatusSelect() == MoveRepository.STATUS_DAYBOOK)
            && !moveLine.getIsBudgetImputed()) {
          budgetLine.setRealizedWithNoPo(
              budgetLine.getRealizedWithNoPo().add(budgetDistribution.getAmount()));
          budgetLine.setAmountRealized(
              budgetLine.getAmountRealized().add(budgetDistribution.getAmount()));
          budgetLine.setToBeCommittedAmount(
              budgetLine.getToBeCommittedAmount().subtract(budgetDistribution.getAmount()));
          BigDecimal firmGap =
              budgetLine
                  .getAmountExpected()
                  .subtract(budgetLine.getRealizedWithPo().add(budgetLine.getRealizedWithNoPo()));
          budgetLine.setFirmGap(firmGap.signum() >= 0 ? BigDecimal.ZERO : firmGap.abs());
          budgetLine.setAvailableAmount(
              (budgetLine.getAvailableAmount().subtract(budgetDistribution.getAmount()))
                          .compareTo(BigDecimal.ZERO)
                      > 0
                  ? budgetLine.getAvailableAmount().subtract(budgetDistribution.getAmount())
                  : BigDecimal.ZERO);
          return true;
        } else if (move.getStatusSelect() == MoveRepository.STATUS_CANCELED) {
          budgetLine.setRealizedWithNoPo(
              budgetLine.getRealizedWithNoPo().subtract(budgetDistribution.getAmount()));
          budgetLine.setAmountRealized(
              budgetLine.getAmountRealized().subtract(budgetDistribution.getAmount()));
          budgetLine.setToBeCommittedAmount(
              budgetLine.getToBeCommittedAmount().add(budgetDistribution.getAmount()));
          BigDecimal firmGap =
              budgetLine
                  .getAmountExpected()
                  .subtract(budgetLine.getRealizedWithPo().add(budgetLine.getRealizedWithNoPo()));
          budgetLine.setFirmGap(firmGap.signum() >= 0 ? BigDecimal.ZERO : firmGap.abs());
          budgetLine.setAvailableAmount(
              (budgetLine.getAvailableAmount().add(budgetDistribution.getAmount()))
                          .compareTo(BigDecimal.ZERO)
                      > 0
                  ? budgetLine.getAvailableAmount().add(budgetDistribution.getAmount())
                  : BigDecimal.ZERO);
        }
      }
    }
    return false;
  }

  @Override
  public void computeBudgetDistributionSumAmount(
      BudgetDistribution budgetDistribution, LocalDate computeDate) {

    if (budgetDistribution.getBudget() != null) {
      Budget budget = budgetDistribution.getBudget();
      if (budget.getBudgetLevel() != null
          && budget.getBudgetLevel().getParentBudgetLevel() != null
          && budget.getBudgetLevel().getParentBudgetLevel().getParentBudgetLevel() != null
          && budget
                  .getBudgetLevel()
                  .getParentBudgetLevel()
                  .getParentBudgetLevel()
                  .getBudgetTypeSelect()
              == BudgetLevelRepository.BUDGET_LEVEL_BUDGET_TYPE_SELECT_SALE) {
        budgetDistribution.setBudgetAmountAvailable(budget.getAvailableAmount());
        return;
      }
      BigDecimal budgetAmountAvailable = BigDecimal.ZERO;
      Integer budgetControlLevel = budgetLevelService.getBudgetControlLevel(budget);
      if (budgetControlLevel == null) {
        budgetControlLevel = 0;
      }

      switch (budgetControlLevel) {
        case BudgetLevelRepository.BUDGET_LEVEL_AVAILABLE_AMOUNT_BUDGET_LINE:
          if (budgetDistribution.getBudget().getBudgetLineList() != null && computeDate != null) {
            List<BudgetLine> budgetLineList = budgetDistribution.getBudget().getBudgetLineList();

            for (BudgetLine budgetLine : budgetLineList) {
              LocalDate fromDate = budgetLine.getFromDate();
              LocalDate toDate = budgetLine.getToDate();

              if (fromDate != null && DateTool.isBetween(fromDate, toDate, computeDate)) {
                BigDecimal amount =
                    budgetLine
                                .getAmountExpected()
                                .subtract(budgetLine.getRealizedWithNoPo())
                                .subtract(budgetLine.getRealizedWithPo())
                                .compareTo(BigDecimal.ZERO)
                            > 0
                        ? budgetLine
                            .getAmountExpected()
                            .subtract(budgetLine.getRealizedWithNoPo())
                            .subtract(budgetLine.getRealizedWithPo())
                        : BigDecimal.ZERO;
                budgetAmountAvailable = budgetAmountAvailable.add(amount);
              }
            }
          }
          break;
        case BudgetLevelRepository.BUDGET_LEVEL_AVAILABLE_AMOUNT_BUDGET:
          budgetAmountAvailable = budget.getAvailableAmount();
          break;
        case BudgetLevelRepository.BUDGET_LEVEL_AVAILABLE_AMOUNT_BUDGET_SECTION:
          budgetAmountAvailable = budget.getBudgetLevel().getTotalAmountAvailable();
          break;
        case BudgetLevelRepository.BUDGET_LEVEL_AVAILABLE_AMOUNT_BUDGET_GROUP:
          budgetAmountAvailable =
              budget.getBudgetLevel().getParentBudgetLevel().getTotalAmountAvailable();
          break;
        default:
          budgetAmountAvailable =
              budget
                  .getBudgetLevel()
                  .getParentBudgetLevel()
                  .getParentBudgetLevel()
                  .getTotalAmountAvailable();
          break;
      }
      budgetDistribution.setBudgetAmountAvailable(budgetAmountAvailable);
    }
  }

  @Override
  public void createBudgetKey(Budget budget) throws AxelorException {
    if (budget.getBudgetLevel() != null
        && budget.getBudgetLevel().getParentBudgetLevel() != null
        && budget.getBudgetLevel().getParentBudgetLevel().getParentBudgetLevel() != null) {
      Company company =
          budget.getBudgetLevel().getParentBudgetLevel().getParentBudgetLevel().getCompany();

      if (this.checkBudgetKeyInConfig(company)) {
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
  }

  @Override
  public String getAccountIdList(Long companyId, int budgetType) {
    if (companyId > 0) {
      List<String> accountTypeList = new ArrayList<>();

      switch (budgetType) {
        case BudgetLevelRepository.BUDGET_LEVEL_BUDGET_TYPE_SELECT_PURCHASE_AND_INVESTMENT:
          accountTypeList.add(AccountTypeRepository.TYPE_IMMOBILISATION);
          accountTypeList.add(AccountTypeRepository.TYPE_CHARGE);
        case BudgetLevelRepository.BUDGET_LEVEL_BUDGET_TYPE_SELECT_PURCHASE:
          accountTypeList.add(AccountTypeRepository.TYPE_CHARGE);
          break;
        case BudgetLevelRepository.BUDGET_LEVEL_BUDGET_TYPE_SELECT_SALE:
          accountTypeList.add(AccountTypeRepository.TYPE_INCOME);
          break;
        case BudgetLevelRepository.BUDGET_LEVEL_BUDGET_TYPE_SELECT_INVESTMENT:
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
    if (budget != null && budget.getBudgetLevel() != null) {
      checkBudgetParentDates(budget);
    }
    if (budget != null && !CollectionUtils.isEmpty(budget.getBudgetLineList())) {
      checkBudgetLinesDates(budget);
    }
  }

  @Override
  public void checkBudgetParentDates(Budget budget) throws AxelorException {
    if (budget != null && budget.getBudgetLevel() != null) {
      BudgetLevel section = budget.getBudgetLevel();

      if ((budget.getFromDate().isBefore(section.getFromDate()))
          || (budget.getToDate().isAfter(section.getToDate()))) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(I18n.get(IExceptionMessage.WRONG_DATES_ON_BUDGET), budget.getCode()));
      }
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
                  I18n.get(IExceptionMessage.WRONG_DATES_ON_BUDGET_LINE), budget.getCode()));
        }
        budgetLineList.remove(budgetLine);
        if (!CollectionUtils.isEmpty(budgetLineList)) {
          for (BudgetLine bl : budgetLineList) {
            if (DateTool.isBetween(
                    budgetLine.getFromDate(), budgetLine.getToDate(), bl.getFromDate())
                || DateTool.isBetween(
                    budgetLine.getFromDate(), budgetLine.getToDate(), bl.getToDate())) {
              throw new AxelorException(
                  TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                  String.format(
                      I18n.get(IExceptionMessage.BUDGET_LINES_ON_SAME_PERIOD), budget.getCode()));
            }
          }
        }
      }
    }
  }

  @Override
  public Budget resetBudget(Budget entity) {

    entity.setCode(entity.getCode() + " (" + I18n.get("copy") + ")");

    entity.setStatusSelect(BudgetRepository.STATUS_DRAFT);
    entity.setArchived(false);

    entity.setTotalAmountExpected(entity.getTotalAmountExpected());
    entity.setTotalAmountCommitted(BigDecimal.ZERO);
    entity.setRealizedWithNoPo(BigDecimal.ZERO);
    entity.setRealizedWithPo(BigDecimal.ZERO);
    entity.setSimulatedAmount(BigDecimal.ZERO);
    entity.setAvailableAmount(entity.getTotalAmountExpected());
    entity.setAvailableAmountWithSimulated(entity.getTotalAmountExpected());
    entity.setTotalAmountRealized(BigDecimal.ZERO);
    entity.setTotalFirmGap(BigDecimal.ZERO);
    entity.setTotalAmountPaid(BigDecimal.ZERO);

    if (!CollectionUtils.isEmpty(entity.getBudgetLineList())) {
      for (BudgetLine child : entity.getBudgetLineList()) {
        child = budgetLineService.resetBudgetLine(child);
      }
    }

    return entity;
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
      List<BudgetLine> budgetLineList = generatePeriods(budget);
      budget.clearBudgetLineList();
      if (!CollectionUtils.isEmpty(budgetLineList)) {
        for (BudgetLine bl : budgetLineList) {
          budget.addBudgetLineListItem(bl);
        }
      }
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
      for (BudgetDistribution budgetDistribution : budgetDistributionList) {
        if (budgetDistribution.getAmount().compareTo(amount) > 0) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(IExceptionMessage.BUDGET_EXCEED_ORDER_LINE_AMOUNT),
              code);
        }
      }
    }
  }
}
