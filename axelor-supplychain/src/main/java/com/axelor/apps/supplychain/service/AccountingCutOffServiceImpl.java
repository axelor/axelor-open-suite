/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service;

import static com.axelor.apps.base.service.administration.AbstractBatch.FETCH_LIMIT;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.generator.line.InvoiceLineManagement;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveSimulateService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.db.repo.SupplychainBatchRepository;
import com.axelor.apps.supplychain.service.config.AccountConfigSupplychainService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class AccountingCutOffServiceImpl implements AccountingCutOffService {

  protected StockMoveRepository stockMoverepository;
  protected StockMoveLineRepository stockMoveLineRepository;
  protected MoveCreateService moveCreateService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected AccountConfigSupplychainService accountConfigSupplychainService;
  protected SaleOrderRepository saleOrderRepository;
  protected PurchaseOrderRepository purchaseOrderRepository;
  protected MoveToolService moveToolService;
  protected AccountManagementAccountService accountManagementAccountService;
  protected TaxAccountService taxAccountService;
  protected AppAccountService appAccountService;
  protected AnalyticMoveLineService analyticMoveLineService;
  protected MoveRepository moveRepository;
  protected MoveValidateService moveValidateService;
  protected UnitConversionService unitConversionService;
  protected AnalyticMoveLineRepository analyticMoveLineRepository;
  protected ReconcileService reconcileService;
  protected AccountConfigService accountConfigService;
  protected SaleOrderService saleOrderService;
  protected StockMoveLineServiceSupplychain stockMoveLineService;
  protected MoveSimulateService moveSimulateService;
  protected MoveLineService moveLineService;
  protected CurrencyService currencyService;
  protected int counter = 0;

  @Inject
  public AccountingCutOffServiceImpl(
      StockMoveRepository stockMoverepository,
      StockMoveLineRepository stockMoveLineRepository,
      MoveCreateService moveCreateService,
      AccountConfigSupplychainService accountConfigSupplychainService,
      SaleOrderRepository saleOrderRepository,
      PurchaseOrderRepository purchaseOrderRepository,
      MoveToolService moveToolService,
      AccountManagementAccountService accountManagementAccountService,
      TaxAccountService taxAccountService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      MoveRepository moveRepository,
      MoveValidateService moveValidateService,
      UnitConversionService unitConversionService,
      AnalyticMoveLineRepository analyticMoveLineRepository,
      ReconcileService reconcileService,
      AccountConfigService accountConfigService,
      MoveLineCreateService moveLineCreateService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      SaleOrderService saleOrderService,
      StockMoveLineServiceSupplychain stockMoveLineService,
      MoveSimulateService moveSimulateService,
      MoveLineService moveLineService,
      CurrencyService currencyService) {

    this.stockMoverepository = stockMoverepository;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.moveCreateService = moveCreateService;
    this.accountConfigSupplychainService = accountConfigSupplychainService;
    this.saleOrderRepository = saleOrderRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.moveToolService = moveToolService;
    this.accountManagementAccountService = accountManagementAccountService;
    this.taxAccountService = taxAccountService;
    this.appAccountService = appAccountService;
    this.analyticMoveLineService = analyticMoveLineService;
    this.moveRepository = moveRepository;
    this.moveValidateService = moveValidateService;
    this.unitConversionService = unitConversionService;
    this.analyticMoveLineRepository = analyticMoveLineRepository;
    this.reconcileService = reconcileService;
    this.accountConfigService = accountConfigService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.saleOrderService = saleOrderService;
    this.stockMoveLineService = stockMoveLineService;
    this.moveSimulateService = moveSimulateService;
    this.moveLineService = moveLineService;
    this.currencyService = currencyService;
  }

  @Override
  public Query<StockMove> getStockMoves(
      Company company, int accountingCutOffTypeSelect, LocalDate moveDate) {

    int stockMoveTypeSelect = 0;

    if (accountingCutOffTypeSelect
        == SupplychainBatchRepository.ACCOUNTING_CUT_OFF_TYPE_SUPPLIER_INVOICES) {
      stockMoveTypeSelect = StockMoveRepository.TYPE_INCOMING;
    } else if (accountingCutOffTypeSelect
        == SupplychainBatchRepository.ACCOUNTING_CUT_OFF_TYPE_CUSTOMER_INVOICES) {
      stockMoveTypeSelect = StockMoveRepository.TYPE_OUTGOING;
    }

    String queryStr =
        "self.invoicingStatusSelect != :stockMoveInvoiced "
            + "AND self.statusSelect = :stockMoveStatusRealized and self.realDate <= :moveDate "
            + "AND self.typeSelect = :stockMoveType ";

    if (company != null) {
      queryStr += "AND self.company.id = :companyId";
    }

    Query<StockMove> stockMoveQuery =
        stockMoverepository
            .all()
            .filter(queryStr)
            .bind("stockMoveInvoiced", StockMoveRepository.STATUS_INVOICED)
            .bind("stockMoveStatusRealized", StockMoveRepository.STATUS_REALIZED)
            .bind("stockMoveType", stockMoveTypeSelect)
            .bind("moveDate", moveDate);

    if (company != null) {
      stockMoveQuery.bind("companyId", company.getId());
    }

    return stockMoveQuery.order("id");
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<Move> generateCutOffMovesFromStockMove(
      StockMove stockMove,
      Journal miscOpeJournal,
      LocalDate moveDate,
      LocalDate reverseMoveDate,
      String moveDescription,
      int accountingCutOffTypeSelect,
      int cutOffMoveStatusSelect,
      boolean recoveredTax,
      boolean ati,
      boolean includeNotStockManagedProduct,
      boolean automaticReverse,
      boolean automaticReconcile)
      throws AxelorException {

    List<Move> moveList = new ArrayList<>();

    List<StockMoveLine> stockMoveLineSortedList = stockMove.getStockMoveLineList();
    stockMoveLineSortedList.sort(Comparator.comparing(StockMoveLine::getSequence));

    Move move =
        generateCutOffMoveFromStockMove(
            stockMove,
            stockMoveLineSortedList,
            miscOpeJournal,
            moveDate,
            moveDate,
            moveDescription,
            cutOffMoveStatusSelect,
            accountingCutOffTypeSelect
                == SupplychainBatchRepository.ACCOUNTING_CUT_OFF_TYPE_SUPPLIER_INVOICES,
            recoveredTax,
            ati,
            includeNotStockManagedProduct,
            false);

    if (move == null) {
      return null;
    }
    moveList.add(move);

    if (automaticReverse) {
      Move reverseMove =
          generateCutOffMoveFromStockMove(
              stockMove,
              stockMoveLineSortedList,
              miscOpeJournal,
              reverseMoveDate,
              moveDate,
              moveDescription,
              cutOffMoveStatusSelect,
              accountingCutOffTypeSelect
                  == SupplychainBatchRepository.ACCOUNTING_CUT_OFF_TYPE_SUPPLIER_INVOICES,
              recoveredTax,
              ati,
              includeNotStockManagedProduct,
              true);

      if (reverseMove == null) {
        return null;
      }
      moveList.add(reverseMove);

      if (automaticReconcile) {
        reconcile(move, reverseMove);
      }
    }

    if (!stockMove.getCutOffMoveGenerated()) {
      stockMove.setCutOffMoveGenerated(true);
    }

    return moveList;
  }

  @Override
  public Move generateCutOffMoveFromStockMove(
      StockMove stockMove,
      List<StockMoveLine> sortedStockMoveLine,
      Journal miscOpeJournal,
      LocalDate moveDate,
      LocalDate originDate,
      String moveDescription,
      int cutOffMoveStatusSelect,
      boolean isPurchase,
      boolean recoveredTax,
      boolean ati,
      boolean includeNotStockManagedProduct,
      boolean isReverse)
      throws AxelorException {

    if (moveDate == null
        || stockMove.getOriginTypeSelect() == null
        || stockMove.getOriginId() == null) {
      return null;
    }

    Company company = stockMove.getCompany();

    AccountConfig accountConfig = accountConfigSupplychainService.getAccountConfig(company);

    Partner partner = stockMove.getPartner();
    Account partnerAccount = null;

    Currency currency = null;
    if (StockMoveRepository.ORIGIN_SALE_ORDER.equals(stockMove.getOriginTypeSelect())
        && stockMove.getOriginId() != null) {
      SaleOrder saleOrder = saleOrderRepository.find(stockMove.getOriginId());
      currency = saleOrder.getCurrency();
      if (partner == null) {
        partner = saleOrder.getClientPartner();
      }
      partnerAccount = accountConfigSupplychainService.getForecastedInvCustAccount(accountConfig);
    }
    if (StockMoveRepository.ORIGIN_PURCHASE_ORDER.equals(stockMove.getOriginTypeSelect())
        && stockMove.getOriginId() != null) {
      PurchaseOrder purchaseOrder = purchaseOrderRepository.find(stockMove.getOriginId());
      currency = purchaseOrder.getCurrency();
      if (partner == null) {
        partner = purchaseOrder.getSupplierPartner();
      }
      partnerAccount = accountConfigSupplychainService.getForecastedInvSuppAccount(accountConfig);
    }

    String origin = stockMove.getStockMoveSeq();

    Move move =
        moveCreateService.createMove(
            miscOpeJournal,
            company,
            currency,
            partner,
            moveDate,
            originDate,
            null,
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_CUT_OFF,
            origin,
            moveDescription);

    counter = 0;

    this.generateMoveLinesFromStockMove(
        move,
        stockMove.getStockMoveLineList(),
        origin,
        isPurchase,
        recoveredTax,
        ati,
        moveDescription,
        isReverse,
        originDate,
        includeNotStockManagedProduct);

    this.generatePartnerMoveLine(move, origin, partnerAccount, moveDescription, originDate);

    // Status
    if (move.getMoveLineList() != null && !move.getMoveLineList().isEmpty()) {
      move.setStockMove(stockMove);
      this.updateStatus(move, cutOffMoveStatusSelect);
    } else {
      moveRepository.remove(move);
      return null;
    }

    return move;
  }

  protected List<MoveLine> generateMoveLinesFromStockMove(
      Move move,
      List<StockMoveLine> stockMoveLineList,
      String origin,
      boolean isPurchase,
      boolean recoveredTax,
      boolean ati,
      String moveDescription,
      boolean isReverse,
      LocalDate originDate,
      boolean includeNotStockManagedProduct)
      throws AxelorException {

    if (stockMoveLineList != null) {

      for (StockMoveLine stockMoveLine : stockMoveLineList) {

        Product product = stockMoveLine.getProduct();

        if (checkStockMoveLine(stockMoveLine, product, includeNotStockManagedProduct)) {
          continue;
        }

        generateProductMoveLine(
            move,
            stockMoveLine,
            origin,
            isPurchase,
            recoveredTax,
            ati,
            moveDescription,
            isReverse,
            originDate);
      }
    }

    return move.getMoveLineList();
  }

  protected void updateStatus(Move move, int cutOffMoveStatusSelect) throws AxelorException {
    if (cutOffMoveStatusSelect == MoveRepository.STATUS_SIMULATED) {
      moveSimulateService.simulate(move);
    } else {
      moveValidateService.updateValidateStatus(
          move, cutOffMoveStatusSelect == MoveRepository.STATUS_DAYBOOK);

      if (cutOffMoveStatusSelect == MoveRepository.STATUS_ACCOUNTED) {
        moveValidateService.accounting(move);
      } else if (cutOffMoveStatusSelect == MoveRepository.STATUS_DAYBOOK
          && move.getStatusSelect() != MoveRepository.STATUS_DAYBOOK) {
        move.setStatusSelect(MoveRepository.STATUS_DAYBOOK);
      }
    }
  }

  protected boolean checkStockMoveLine(
      StockMoveLine stockMoveLine, Product product, boolean includeNotStockManagedProduct) {
    return stockMoveLine.getRealQty().signum() == 0
        || product == null
        || (!includeNotStockManagedProduct && !product.getStockManaged())
        || stockMoveLine.getRealQty().compareTo(stockMoveLine.getQtyInvoiced()) == 0;
  }

  protected MoveLine generateProductMoveLine(
      Move move,
      StockMoveLine stockMoveLine,
      String origin,
      boolean isPurchase,
      boolean recoveredTax,
      boolean ati,
      String moveDescription,
      boolean isReverse,
      LocalDate originDate)
      throws AxelorException {

    SaleOrderLine saleOrderLine = stockMoveLine.getSaleOrderLine();
    PurchaseOrderLine purchaseOrderLine = stockMoveLine.getPurchaseOrderLine();
    Company company = move.getCompany();
    LocalDate moveDate = move.getDate();
    Partner partner = move.getPartner();

    boolean isFixedAssets =
        isPurchase && purchaseOrderLine != null && purchaseOrderLine.getFixedAssets();
    BigDecimal amountInCurrency =
        stockMoveLineService.getAmountNotInvoiced(
            stockMoveLine, purchaseOrderLine, saleOrderLine, isPurchase, ati, recoveredTax);

    if (amountInCurrency == null || amountInCurrency.signum() == 0) {
      return null;
    }

    Product product = stockMoveLine.getProduct();

    FiscalPosition fiscalPosition;
    if (saleOrderLine != null) {
      fiscalPosition = saleOrderLine.getSaleOrder().getFiscalPosition();
    } else {
      fiscalPosition = partner.getFiscalPosition();
    }

    Account account =
        accountManagementAccountService.getProductAccount(
            product, company, fiscalPosition, isPurchase, isFixedAssets);

    boolean isDebit =
        (isPurchase && amountInCurrency.signum() > 0)
            || !isPurchase && amountInCurrency.signum() < 0;
    if (isReverse) {
      isDebit = !isDebit;
    }

    MoveLine moveLine =
        moveLineCreateService.createMoveLine(
            move,
            partner,
            account,
            amountInCurrency,
            isDebit,
            originDate,
            ++counter,
            origin,
            moveDescription);
    moveLine.setDate(moveDate);
    moveLine.setDueDate(moveDate);

    getAndComputeAnalyticDistribution(product, move, moveLine);

    move.addMoveLineListItem(moveLine);

    if (recoveredTax) {
      TaxLine taxLine =
          accountManagementAccountService.getTaxLine(
              originDate, product, company, fiscalPosition, isPurchase);
      if (taxLine != null) {
        moveLine.setTaxLine(taxLine);
        moveLine.setTaxRate(taxLine.getValue());
        moveLine.setTaxCode(taxLine.getTax().getCode());

        if (taxLine.getValue().signum() != 0) {
          generateTaxMoveLine(move, moveLine, origin, isPurchase, moveDescription);
        }
      }
    }

    return moveLine;
  }

  @Override
  public Query<Move> getMoves(
      Company company,
      Journal researchJournal,
      LocalDate moveDate,
      int accountingCutOffTypeSelect) {
    String queryStr =
        "((:researchJournal > 0 AND self.journal.id = :researchJournal) "
            + "  OR (:researchJournal = 0 AND self.journal.journalType.technicalTypeSelect = :journalType))"
            + "AND YEAR(self.date) = :year "
            + "AND self.statusSelect IN (2, 3, 5) "
            + "AND EXISTS(SELECT 1 FROM MoveLine ml "
            + " WHERE ml.move = self "
            + " AND ml.account.manageCutOffPeriod IS TRUE "
            + " AND ml.cutOffStartDate != null AND ml.cutOffEndDate != null "
            + " AND YEAR(ml.cutOffEndDate) > :year)";

    if (company != null) {
      queryStr += " AND self.company = :company";
    }

    Query<Move> moveQuery =
        moveRepository
            .all()
            .filter(queryStr)
            .bind("researchJournal", researchJournal == null ? 0 : researchJournal.getId())
            .bind(
                "journalType",
                accountingCutOffTypeSelect
                        == SupplychainBatchRepository.ACCOUNTING_CUT_OFF_TYPE_PREPAID_EXPENSES
                    ? JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE
                    : JournalTypeRepository.TECHNICAL_TYPE_SELECT_SALE)
            .bind("year", moveDate.getYear());

    if (company != null) {
      moveQuery.bind("company", company.getId());
    }

    return moveQuery.order("id");
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<Move> generateCutOffMovesFromMove(
      Move move,
      Journal journal,
      LocalDate moveDate,
      LocalDate reverseMoveDate,
      String moveDescription,
      int accountingCutOffTypeSelect,
      int cutOffMoveStatusSelect,
      boolean automaticReverse,
      boolean automaticReconcile)
      throws AxelorException {
    List<Move> cutOffMoveList = new ArrayList<>();

    Move cutOffMove =
        this.generateCutOffMoveFromMove(
            move,
            journal,
            moveDate,
            moveDate,
            moveDescription,
            accountingCutOffTypeSelect,
            cutOffMoveStatusSelect,
            false);

    if (cutOffMove == null) {
      return null;
    }

    cutOffMoveList.add(cutOffMove);

    if (automaticReverse) {
      Move reverseCutOffMove =
          this.generateCutOffMoveFromMove(
              move,
              journal,
              reverseMoveDate,
              moveDate,
              moveDescription,
              accountingCutOffTypeSelect,
              cutOffMoveStatusSelect,
              true);

      if (reverseCutOffMove == null) {
        return null;
      }

      cutOffMoveList.add(reverseCutOffMove);

      if (automaticReconcile && cutOffMoveStatusSelect != MoveRepository.STATUS_SIMULATED) {
        reconcile(cutOffMove, reverseCutOffMove);
      }
    }

    if (!move.getCutOffMoveGenerated()) {
      move.setCutOffMoveGenerated(true);
    }

    return cutOffMoveList;
  }

  @Override
  public Move generateCutOffMoveFromMove(
      Move move,
      Journal journal,
      LocalDate moveDate,
      LocalDate originMoveDate,
      String moveDescription,
      int accountingCutOffTypeSelect,
      int cutOffMoveStatusSelect,
      boolean isReverse)
      throws AxelorException {
    Company company = move.getCompany();
    Partner partner = move.getPartner();
    LocalDate originDate = move.getOriginDate();
    String origin = move.getReference();

    Move cutOffMove =
        moveCreateService.createMove(
            journal,
            company,
            move.getCurrency(),
            partner,
            moveDate,
            originDate,
            null,
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_CUT_OFF,
            origin,
            moveDescription);

    if (CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      this.generateMoveLinesFromMove(
          move,
          cutOffMove,
          company,
          partner,
          moveDate,
          originMoveDate,
          originDate,
          origin,
          moveDescription,
          accountingCutOffTypeSelect,
          isReverse);
    }

    // Status
    if (CollectionUtils.isNotEmpty(cutOffMove.getMoveLineList())) {
      cutOffMove.setCutOffOriginMove(move);
      this.updateStatus(cutOffMove, cutOffMoveStatusSelect);
    } else {
      moveRepository.remove(cutOffMove);
      return null;
    }

    return cutOffMove;
  }

  protected void generateMoveLinesFromMove(
      Move move,
      Move cutOffMove,
      Company company,
      Partner partner,
      LocalDate moveDate,
      LocalDate originMoveDate,
      LocalDate originDate,
      String origin,
      String moveDescription,
      int accountingCutOffTypeSelect,
      boolean isReverse)
      throws AxelorException {
    Account moveLineAccount;
    BigDecimal amountInCurrency;
    MoveLine cutOffMoveLine;
    Map<Account, MoveLine> cutOffMoveLineMap = new HashMap<>();

    BigDecimal currencyRate =
        currencyService.getCurrencyConversionRate(
            move.getCurrency(), move.getCompanyCurrency(), moveDate);

    // Sorting so that move lines with analytic move lines are computed first
    List<MoveLine> sortedMoveLineList = new ArrayList<>(move.getMoveLineList());
    sortedMoveLineList.sort(
        (t1, t2) -> {
          if ((CollectionUtils.isNotEmpty(t1.getAnalyticMoveLineList())
                  && CollectionUtils.isNotEmpty(t2.getAnalyticMoveLineList()))
              || (CollectionUtils.isEmpty(t1.getAnalyticMoveLineList())
                  && CollectionUtils.isEmpty(t2.getAnalyticMoveLineList()))) {
            return 0;
          } else if (CollectionUtils.isNotEmpty(t1.getAnalyticMoveLineList())) {
            return -1;
          } else {
            return 1;
          }
        });

    for (MoveLine moveLine : sortedMoveLineList) {
      if (moveLine.getAccount().getManageCutOffPeriod()
          && moveLine.getCutOffStartDate() != null
          && moveLine.getCutOffEndDate() != null
          && (moveLine.getCutOffEndDate().getYear() > moveDate.getYear() || isReverse)) {
        moveLineAccount = moveLine.getAccount();
        amountInCurrency = moveLineService.getCutOffProrataAmount(moveLine, originMoveDate);
        BigDecimal convertedAmount =
            currencyService.getAmountCurrencyConvertedUsingExchangeRate(
                amountInCurrency, currencyRate);

        // Check if move line already exists with that account
        if (cutOffMoveLineMap.containsKey(moveLineAccount)) {
          cutOffMoveLine = cutOffMoveLineMap.get(moveLineAccount);
          cutOffMoveLine.setCurrencyAmount(
              cutOffMoveLine.getCurrencyAmount().add(amountInCurrency));
          if (isReverse
              != (accountingCutOffTypeSelect
                  == SupplychainBatchRepository.ACCOUNTING_CUT_OFF_TYPE_DEFERRED_INCOMES)) {
            cutOffMoveLine.setDebit(cutOffMoveLine.getDebit().add(convertedAmount));
          } else {
            cutOffMoveLine.setCredit(cutOffMoveLine.getCredit().add(convertedAmount));
          }

        } else {
          cutOffMoveLine =
              moveLineCreateService.createMoveLine(
                  cutOffMove,
                  partner,
                  moveLineAccount,
                  amountInCurrency,
                  isReverse
                      != (accountingCutOffTypeSelect
                          == SupplychainBatchRepository.ACCOUNTING_CUT_OFF_TYPE_DEFERRED_INCOMES),
                  originDate,
                  ++counter,
                  origin,
                  moveDescription);
          cutOffMoveLine.setTaxLine(moveLine.getTaxLine());

          cutOffMoveLineMap.put(moveLineAccount, cutOffMoveLine);
        }

        // Copy analytic move lines
        this.copyAnalyticMoveLines(moveLine, cutOffMoveLine, amountInCurrency);
      }
    }

    cutOffMoveLineMap.values().forEach(cutOffMove::addMoveLineListItem);

    // Partner move line
    Account account =
        accountConfigSupplychainService.getPartnerAccount(
            company.getAccountConfig(), accountingCutOffTypeSelect);

    this.generatePartnerMoveLine(cutOffMove, origin, account, moveDescription, originDate);
  }

  protected void copyAnalyticMoveLines(
      MoveLine moveLine, MoveLine cutOffMoveLine, BigDecimal newAmount) {
    if (CollectionUtils.isNotEmpty(moveLine.getAnalyticMoveLineList())) {
      if (CollectionUtils.isNotEmpty(cutOffMoveLine.getAnalyticMoveLineList())) {
        AnalyticMoveLine existingAnalyticMoveLine;
        List<AnalyticMoveLine> toComputeAnalyticMoveLineList =
            new ArrayList<>(cutOffMoveLine.getAnalyticMoveLineList());

        for (AnalyticMoveLine analyticMoveLine : moveLine.getAnalyticMoveLineList()) {
          existingAnalyticMoveLine =
              this.getExistingAnalyticMoveLine(cutOffMoveLine, analyticMoveLine);

          if (existingAnalyticMoveLine == null) {
            this.copyAnalyticMoveLine(cutOffMoveLine, analyticMoveLine, newAmount);
          } else {
            this.computeAnalyticMoveLine(
                cutOffMoveLine,
                existingAnalyticMoveLine,
                analyticMoveLine.getPercentage(),
                newAmount,
                false);

            toComputeAnalyticMoveLineList.remove(existingAnalyticMoveLine);
          }
        }

        for (AnalyticMoveLine toComputeAnalyticMoveLine : toComputeAnalyticMoveLineList) {
          this.computeAnalyticMoveLine(
              cutOffMoveLine, toComputeAnalyticMoveLine, BigDecimal.ZERO, newAmount, false);
        }
      } else {
        if (cutOffMoveLine.getAnalyticMoveLineList() == null) {
          cutOffMoveLine.setAnalyticMoveLineList(new ArrayList<>());
        }

        for (AnalyticMoveLine analyticMoveLine : moveLine.getAnalyticMoveLineList()) {
          this.copyAnalyticMoveLine(cutOffMoveLine, analyticMoveLine, newAmount);
        }
      }
    } else if (CollectionUtils.isNotEmpty(cutOffMoveLine.getAnalyticMoveLineList())) {
      for (AnalyticMoveLine analyticMoveLine : cutOffMoveLine.getAnalyticMoveLineList()) {
        this.computeAnalyticMoveLine(
            cutOffMoveLine, analyticMoveLine, analyticMoveLine.getPercentage(), newAmount, false);
      }
    }
  }

  protected AnalyticMoveLine getExistingAnalyticMoveLine(
      MoveLine moveLine, AnalyticMoveLine analyticMoveLine) {
    return moveLine.getAnalyticMoveLineList().stream()
        .filter(
            it ->
                it.getAnalyticAxis().equals(analyticMoveLine.getAnalyticAxis())
                    && it.getAnalyticAccount().equals(analyticMoveLine.getAnalyticAccount()))
        .findFirst()
        .orElse(null);
  }

  protected void copyAnalyticMoveLine(
      MoveLine moveLine, AnalyticMoveLine analyticMoveLine, BigDecimal newAmount) {
    AnalyticMoveLine analyticMoveLineCopy =
        analyticMoveLineRepository.copy(analyticMoveLine, false);

    this.computeAnalyticMoveLine(
        moveLine, analyticMoveLineCopy, analyticMoveLineCopy.getPercentage(), newAmount, true);

    moveLine.addAnalyticMoveLineListItem(analyticMoveLineCopy);
  }

  protected void computeAnalyticMoveLine(
      MoveLine moveLine,
      AnalyticMoveLine analyticMoveLine,
      BigDecimal newPercentage,
      BigDecimal newAmount,
      boolean newLine) {
    BigDecimal amount =
        newAmount.multiply(newPercentage.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));

    if (!newLine) {
      amount = analyticMoveLine.getAmount().add(amount);
    }

    BigDecimal percentage =
        amount
            .multiply(BigDecimal.valueOf(100))
            .divide(moveLine.getCurrencyAmount(), 2, RoundingMode.HALF_UP);

    analyticMoveLine.setPercentage(percentage);
    analyticMoveLine.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
  }

  protected void generateTaxMoveLine(
      Move move,
      MoveLine productMoveLine,
      String origin,
      boolean isPurchase,
      String moveDescription)
      throws AxelorException {

    TaxLine taxLine = productMoveLine.getTaxLine();

    Tax tax = taxLine.getTax();

    Account taxAccount =
        taxAccountService.getVatRegulationAccount(tax, move.getCompany(), isPurchase);

    BigDecimal currencyTaxAmount =
        InvoiceLineManagement.computeAmount(
            productMoveLine.getCurrencyAmount(), taxLine.getValue());

    MoveLine taxMoveLine =
        moveLineCreateService.createMoveLine(
            move,
            move.getPartner(),
            taxAccount,
            currencyTaxAmount,
            productMoveLine.getDebit().signum() > 0,
            productMoveLine.getOriginDate(),
            ++counter,
            origin,
            moveDescription);
    taxMoveLine.setDate(move.getDate());
    taxMoveLine.setDueDate(move.getDate());

    move.addMoveLineListItem(taxMoveLine);
  }

  protected MoveLine generatePartnerMoveLine(
      Move move, String origin, Account account, String moveDescription, LocalDate originDate)
      throws AxelorException {
    LocalDate moveDate = move.getDate();

    BigDecimal currencyBalance = moveToolService.getBalanceCurrencyAmount(move.getMoveLineList());
    BigDecimal balance = moveToolService.getBalanceAmount(move.getMoveLineList());

    if (balance.signum() == 0) {
      return null;
    }

    MoveLine moveLine =
        moveLineCreateService.createMoveLine(
            move,
            move.getPartner(),
            account,
            currencyBalance.abs(),
            balance.abs(),
            null,
            balance.signum() < 0,
            moveDate,
            moveDate,
            originDate,
            ++counter,
            origin,
            moveDescription);

    move.addMoveLineListItem(moveLine);

    return moveLine;
  }

  protected void getAndComputeAnalyticDistribution(Product product, Move move, MoveLine moveLine)
      throws AxelorException {

    if (accountConfigService.getAccountConfig(move.getCompany()).getAnalyticDistributionTypeSelect()
        == AccountConfigRepository.DISTRIBUTION_TYPE_FREE) {
      return;
    }

    AnalyticDistributionTemplate analyticDistributionTemplate =
        analyticMoveLineService.getAnalyticDistributionTemplate(
            move.getPartner(), product, move.getCompany());

    moveLine.setAnalyticDistributionTemplate(analyticDistributionTemplate);

    List<AnalyticMoveLine> analyticMoveLineList =
        moveLineComputeAnalyticService
            .createAnalyticDistributionWithTemplate(moveLine)
            .getAnalyticMoveLineList();
    for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
      analyticMoveLine.setMoveLine(moveLine);
    }
    analyticMoveLineList.stream().forEach(analyticMoveLineRepository::save);
  }

  protected void reconcile(Move move, Move reverseMove) throws AxelorException {

    List<MoveLine> moveLineSortedList = move.getMoveLineList();
    Collections.sort(moveLineSortedList, Comparator.comparing(MoveLine::getCounter));

    List<MoveLine> reverseMoveLineSortedList = reverseMove.getMoveLineList();
    Collections.sort(reverseMoveLineSortedList, Comparator.comparing(MoveLine::getCounter));

    Iterator<MoveLine> reverseMoveLinesIt = reverseMoveLineSortedList.iterator();

    for (MoveLine moveLine : moveLineSortedList) {

      MoveLine reverseMoveLine = reverseMoveLinesIt.next();

      reconcileService.reconcile(moveLine, reverseMoveLine, false, false);
    }
  }

  public List<Long> getStockMoveLines(Batch batch) {
    int offset = 0;
    Boolean includeNotStockManagedProduct =
        batch.getSupplychainBatch().getIncludeNotStockManagedProduct();

    List<StockMoveLine> stockMoveLineList;
    List<Long> stockMoveLineIdList = new ArrayList<>();

    Query<StockMove> stockMoveQuery =
        stockMoverepository.all().filter(":batch MEMBER OF self.batchSet").bind("batch", batch);
    List<Long> stockMoveIdList =
        stockMoveQuery.select("id").fetch(0, 0).stream()
            .map(m -> (Long) m.get("id"))
            .collect(Collectors.toList());

    if (stockMoveIdList.isEmpty()) {
      stockMoveLineIdList.add(0L);
    } else {
      Query<StockMoveLine> stockMoveLineQuery =
          stockMoveLineRepository
              .all()
              .filter("self.stockMove.id IN :stockMoveIdList")
              .bind("stockMoveIdList", stockMoveIdList)
              .order("id");

      while (!(stockMoveLineList = stockMoveLineQuery.fetch(FETCH_LIMIT, offset)).isEmpty()) {
        offset += stockMoveLineList.size();

        for (StockMoveLine stockMoveLine : stockMoveLineList) {
          Product product = stockMoveLine.getProduct();
          if (!checkStockMoveLine(stockMoveLine, product, includeNotStockManagedProduct)) {
            stockMoveLineIdList.add(stockMoveLine.getId());
          }
        }

        JPA.clear();
      }
    }
    return stockMoveLineIdList;
  }
}
