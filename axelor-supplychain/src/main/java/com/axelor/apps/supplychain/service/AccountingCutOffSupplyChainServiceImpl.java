/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import static com.axelor.apps.base.service.administration.AbstractBatch.FETCH_LIMIT;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AccountingCutOffServiceImpl;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveSimulateService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineComputeAnalyticService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineService;
import com.axelor.apps.account.util.TaxAccountToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.db.SupplychainBatch;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class AccountingCutOffSupplyChainServiceImpl extends AccountingCutOffServiceImpl
    implements AccountingCutOffSupplyChainService {

  protected StockMoveRepository stockMoverepository;
  protected StockMoveLineRepository stockMoveLineRepository;
  protected SaleOrderRepository saleOrderRepository;
  protected PurchaseOrderRepository purchaseOrderRepository;
  protected StockMoveLineServiceSupplychain stockMoveLineService;
  protected BankDetailsService bankDetailsService;
  protected int counter = 0;

  @Inject
  public AccountingCutOffSupplyChainServiceImpl(
      StockMoveRepository stockMoverepository,
      StockMoveLineRepository stockMoveLineRepository,
      MoveCreateService moveCreateService,
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
      StockMoveLineServiceSupplychain stockMoveLineService,
      MoveSimulateService moveSimulateService,
      MoveLineService moveLineService,
      CurrencyService currencyService,
      TaxAccountToolService taxAccountToolService,
      BankDetailsService bankDetailsService) {

    super(
        moveCreateService,
        moveToolService,
        accountManagementAccountService,
        taxAccountService,
        appAccountService,
        analyticMoveLineService,
        moveRepository,
        moveValidateService,
        unitConversionService,
        analyticMoveLineRepository,
        reconcileService,
        accountConfigService,
        moveLineCreateService,
        moveLineComputeAnalyticService,
        moveSimulateService,
        moveLineService,
        currencyService,
        taxAccountToolService);
    this.stockMoverepository = stockMoverepository;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.saleOrderRepository = saleOrderRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.stockMoveLineService = stockMoveLineService;
    this.bankDetailsService = bankDetailsService;
  }

  @Override
  public Query<StockMove> getStockMoves(
      Company company, int accountingCutOffTypeSelect, LocalDate moveDate) {

    int stockMoveTypeSelect = 0;

    if (accountingCutOffTypeSelect
        == AccountingBatchRepository.ACCOUNTING_CUT_OFF_TYPE_SUPPLIER_INVOICES) {
      stockMoveTypeSelect = StockMoveRepository.TYPE_INCOMING;
    } else if (accountingCutOffTypeSelect
        == AccountingBatchRepository.ACCOUNTING_CUT_OFF_TYPE_CUSTOMER_INVOICES) {
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
      String reverseMoveDescription,
      int accountingCutOffTypeSelect,
      int cutOffMoveStatusSelect,
      boolean recoveredTax,
      boolean ati,
      boolean includeNotStockManagedProduct,
      boolean automaticReverse,
      boolean automaticReconcile,
      Account forecastedInvCustAccount,
      Account forecastedInvSuppAccount,
      String prefixOrigin)
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
                == AccountingBatchRepository.ACCOUNTING_CUT_OFF_TYPE_SUPPLIER_INVOICES,
            recoveredTax,
            ati,
            includeNotStockManagedProduct,
            false,
            forecastedInvCustAccount,
            forecastedInvSuppAccount,
            prefixOrigin);

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
              reverseMoveDescription,
              cutOffMoveStatusSelect,
              accountingCutOffTypeSelect
                  == AccountingBatchRepository.ACCOUNTING_CUT_OFF_TYPE_SUPPLIER_INVOICES,
              recoveredTax,
              ati,
              includeNotStockManagedProduct,
              true,
              forecastedInvCustAccount,
              forecastedInvSuppAccount,
              prefixOrigin);

      if (reverseMove == null) {
        return null;
      }
      moveList.add(reverseMove);

      if (automaticReconcile
          && (cutOffMoveStatusSelect == MoveRepository.STATUS_DAYBOOK
              || cutOffMoveStatusSelect == MoveRepository.STATUS_ACCOUNTED)) {

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
      boolean isReverse,
      Account forecastedInvCustAccount,
      Account forecastedInvSuppAccount,
      String prefixOrigin)
      throws AxelorException {

    if (moveDate == null
        || stockMove.getOriginTypeSelect() == null
        || stockMove.getOriginId() == null) {
      return null;
    }

    Company company = stockMove.getCompany();

    Partner partner = stockMove.getPartner();
    FiscalPosition fiscalPosition = partner != null ? partner.getFiscalPosition() : null;
    Account partnerAccount = null;

    Currency currency = null;
    if (StockMoveRepository.ORIGIN_SALE_ORDER.equals(stockMove.getOriginTypeSelect())
        && stockMove.getOriginId() != null) {
      SaleOrder saleOrder = saleOrderRepository.find(stockMove.getOriginId());
      currency = saleOrder.getCurrency();
      if (partner == null) {
        partner = saleOrder.getClientPartner();
      }
      fiscalPosition = saleOrder.getFiscalPosition();
      partnerAccount = forecastedInvCustAccount;
      if (partnerAccount == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(SupplychainExceptionMessage.MISSING_FORECASTED_INV_CUST_ACCOUNT));
      }
    }
    if (StockMoveRepository.ORIGIN_PURCHASE_ORDER.equals(stockMove.getOriginTypeSelect())
        && stockMove.getOriginId() != null) {
      PurchaseOrder purchaseOrder = purchaseOrderRepository.find(stockMove.getOriginId());
      currency = purchaseOrder.getCurrency();
      if (partner == null) {
        partner = purchaseOrder.getSupplierPartner();
      }
      fiscalPosition = purchaseOrder.getFiscalPosition();
      partnerAccount = forecastedInvSuppAccount;
      if (partnerAccount == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(SupplychainExceptionMessage.MISSING_FORECASTED_INV_SUPP_ACCOUNT));
      }
    }

    String origin = prefixOrigin + stockMove.getStockMoveSeq();

    BankDetails companyBankDetails = null;
    if (company != null) {
      companyBankDetails =
          bankDetailsService.getDefaultCompanyBankDetails(company, null, partner, null);
    }

    Move move =
        moveCreateService.createMove(
            miscOpeJournal,
            company,
            currency,
            partner,
            moveDate,
            originDate,
            null,
            fiscalPosition,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_CUT_OFF,
            origin,
            moveDescription,
            companyBankDetails);

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

    counter = 0;
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
            || (!isPurchase && amountInCurrency.signum() < 0);
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

    List<AnalyticMoveLine> analyticMoveLineList =
        CollectionUtils.isEmpty(moveLine.getAnalyticMoveLineList())
            ? new ArrayList<>()
            : new ArrayList<>(moveLine.getAnalyticMoveLineList());
    moveLine.clearAnalyticMoveLineList();
    getAndComputeAnalyticDistribution(product, move, moveLine, isPurchase);

    if (CollectionUtils.isEmpty(moveLine.getAnalyticMoveLineList())) {
      moveLine.setAnalyticMoveLineList(analyticMoveLineList);
    }

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

  public List<Long> getStockMoveLines(Batch batch) {
    int offset = 0;

    Query<StockMove> stockMoveQuery =
        stockMoverepository.all().filter(":batch MEMBER OF self.batchSet").bind("batch", batch);
    List<Long> stockMoveIdList =
        stockMoveQuery.select("id").fetch(0, 0).stream()
            .map(m -> (Long) m.get("id"))
            .collect(Collectors.toList());

    AccountingBatch accountingBatch = batch.getAccountingBatch();
    Boolean includeNotStockManagedProduct =
        accountingBatch != null && accountingBatch.getIncludeNotStockManagedProduct();
    SupplychainBatch supplychainBatch = batch.getSupplychainBatch();

    if (stockMoveIdList.isEmpty()) {
      return List.of(0L);
    }

    List<Long> stockMoveLineIdList = new ArrayList<>();
    Query<StockMoveLine> stockMoveLineQuery =
        stockMoveLineRepository
            .all()
            .filter("self.stockMove.id IN :stockMoveIdList")
            .bind("stockMoveIdList", stockMoveIdList)
            .order("id");

    List<StockMoveLine> stockMoveLineList;
    while (!(stockMoveLineList = stockMoveLineQuery.fetch(FETCH_LIMIT, offset)).isEmpty()) {
      offset += stockMoveLineList.size();

      for (StockMoveLine stockMoveLine : stockMoveLineList) {
        Product product = stockMoveLine.getProduct();
        if (supplychainBatch != null
            || !checkStockMoveLine(stockMoveLine, product, includeNotStockManagedProduct)) {
          stockMoveLineIdList.add(stockMoveLine.getId());
        }
      }
      JPA.clear();
    }
    return stockMoveLineIdList;
  }
}
