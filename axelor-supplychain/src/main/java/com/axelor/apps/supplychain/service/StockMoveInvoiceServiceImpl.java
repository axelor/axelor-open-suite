/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.account.service.invoice.generator.InvoiceLineGenerator;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.AddressService;
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
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceGeneratorSupplyChain;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineGeneratorSupplyChain;
import com.axelor.apps.tool.StringTool;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class StockMoveInvoiceServiceImpl implements StockMoveInvoiceService {

  private SaleOrderInvoiceService saleOrderInvoiceService;
  private PurchaseOrderInvoiceService purchaseOrderInvoiceService;
  private StockMoveLineServiceSupplychain stockMoveLineServiceSupplychain;
  private InvoiceRepository invoiceRepository;
  private SaleOrderRepository saleOrderRepo;
  private PurchaseOrderRepository purchaseOrderRepo;
  private StockMoveLineRepository stockMoveLineRepository;
  private InvoiceLineRepository invoiceLineRepository;
  private SupplyChainConfigService supplyChainConfigService;
  private AppSupplychainService appSupplychainService;

  @Inject
  public StockMoveInvoiceServiceImpl(
      SaleOrderInvoiceService saleOrderInvoiceService,
      PurchaseOrderInvoiceService purchaseOrderInvoiceService,
      StockMoveLineServiceSupplychain stockMoveLineServiceSupplychain,
      InvoiceRepository invoiceRepository,
      SaleOrderRepository saleOrderRepo,
      PurchaseOrderRepository purchaseOrderRepo,
      StockMoveLineRepository stockMoveLineRepository,
      InvoiceLineRepository invoiceLineRepository,
      SupplyChainConfigService supplyChainConfigService,
      AppSupplychainService appSupplychainService) {
    this.saleOrderInvoiceService = saleOrderInvoiceService;
    this.purchaseOrderInvoiceService = purchaseOrderInvoiceService;
    this.stockMoveLineServiceSupplychain = stockMoveLineServiceSupplychain;
    this.invoiceRepository = invoiceRepository;
    this.saleOrderRepo = saleOrderRepo;
    this.purchaseOrderRepo = purchaseOrderRepo;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.invoiceLineRepository = invoiceLineRepository;
    this.supplyChainConfigService = supplyChainConfigService;
    this.appSupplychainService = appSupplychainService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Invoice createInvoice(
      StockMove stockMove,
      Integer operationSelect,
      List<Map<String, Object>> stockMoveLineListContext)
      throws AxelorException {
    Invoice invoice;
    Map<Long, BigDecimal> qtyToInvoiceMap = new HashMap<>();
    if (operationSelect == StockMoveRepository.INVOICE_PARTIALLY) {
      for (Map<String, Object> map : stockMoveLineListContext) {
        if (map.get("qtyToInvoice") != null) {
          BigDecimal qtyToInvoiceItem = new BigDecimal(map.get("qtyToInvoice").toString());
          BigDecimal remainingQty = new BigDecimal(map.get("remainingQty").toString());
          if (qtyToInvoiceItem.compareTo(BigDecimal.ZERO) != 0) {
            if (qtyToInvoiceItem.compareTo(remainingQty) > 0) {
              qtyToInvoiceItem = remainingQty;
            }
            Long stockMoveLineId = Long.parseLong(map.get("stockMoveLineId").toString());
            StockMoveLine stockMoveLine = stockMoveLineRepository.find(stockMoveLineId);
            qtyToInvoiceMap.put(stockMoveLine.getId(), qtyToInvoiceItem);
          }
        }
      }
    } else {
      qtyToInvoiceMap = null;
    }

    Long origin = stockMove.getOriginId();

    if (StockMoveRepository.ORIGIN_SALE_ORDER.equals(stockMove.getOriginTypeSelect())) {
      invoice = createInvoiceFromSaleOrder(stockMove, saleOrderRepo.find(origin), qtyToInvoiceMap);
    } else if (StockMoveRepository.ORIGIN_PURCHASE_ORDER.equals(stockMove.getOriginTypeSelect())) {
      invoice =
          createInvoiceFromPurchaseOrder(
              stockMove, purchaseOrderRepo.find(origin), qtyToInvoiceMap);
    } else {
      invoice = createInvoiceFromOrderlessStockMove(stockMove, qtyToInvoiceMap);
    }
    return invoice;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Invoice createInvoiceFromSaleOrder(
      StockMove stockMove, SaleOrder saleOrder, Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException {

    // we block if we are trying to invoice partially if config is deactivated
    if (!supplyChainConfigService
            .getSupplyChainConfig(stockMove.getCompany())
            .getActivateOutStockMovePartialInvoicing()
        && computeNonCanceledInvoiceQty(stockMove).signum() > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.STOCK_MOVE_PARTIAL_INVOICE_ERROR));
    }

    InvoiceGenerator invoiceGenerator =
        saleOrderInvoiceService.createInvoiceGenerator(saleOrder, stockMove.getIsReversion());

    Invoice invoice = invoiceGenerator.generate();

    checkSplitSalePartiallyInvoicedStockMoveLines(stockMove, stockMove.getStockMoveLineList());

    invoiceGenerator.populate(
        invoice,
        this.createInvoiceLines(invoice, stockMove.getStockMoveLineList(), qtyToInvoiceMap));

    if (invoice != null) {
      // do not create empty invoices
      if (invoice.getInvoiceLineList() == null || invoice.getInvoiceLineList().isEmpty()) {
        return null;
      }
      invoice.setSaleOrder(saleOrder);
      this.extendInternalReference(stockMove, invoice);
      invoice.setAddressStr(saleOrder.getMainInvoicingAddressStr());

      // fill default advance payment invoice
      if (invoice.getOperationSubTypeSelect() != InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {
        invoice.setAdvancePaymentInvoiceSet(
            Beans.get(InvoiceService.class).getDefaultAdvancePaymentInvoice(invoice));
      }

      invoice.setPartnerTaxNbr(saleOrder.getClientPartner().getTaxNbr());
      if (!Strings.isNullOrEmpty(saleOrder.getInvoiceComments())) {
        invoice.setNote(saleOrder.getInvoiceComments());
      }

      if (ObjectUtils.isEmpty(invoice.getProformaComments())
          && !Strings.isNullOrEmpty(saleOrder.getProformaComments())) {
        invoice.setProformaComments(saleOrder.getProformaComments());
      }

      Set<StockMove> stockMoveSet = invoice.getStockMoveSet();
      if (stockMoveSet == null) {
        stockMoveSet = new HashSet<>();
        invoice.setStockMoveSet(stockMoveSet);
      }
      stockMoveSet.add(stockMove);

      invoiceRepository.save(invoice);
    }

    return invoice;
  }

  @Override
  public void checkSplitSalePartiallyInvoicedStockMoveLines(
      StockMove stockMove, List<StockMoveLine> stockMoveLineList) throws AxelorException {
    SupplyChainConfig supplyChainConfig =
        supplyChainConfigService.getSupplyChainConfig(stockMove.getCompany());
    if (stockMoveLineList != null && supplyChainConfig.getActivateOutStockMovePartialInvoicing()) {
      for (SaleOrderLine saleOrderLine :
          stockMoveLineList.stream()
              .map(StockMoveLine::getSaleOrderLine)
              .filter(Objects::nonNull)
              .collect(Collectors.toList())) {
        if (stockMoveLineList.stream()
                .filter(stockMoveLine -> saleOrderLine.equals(stockMoveLine.getSaleOrderLine()))
                .count()
            > 1) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(IExceptionMessage.BLOCK_SPLIT_OUTGOING_STOCK_MOVE_LINES));
        }
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Invoice createInvoiceFromPurchaseOrder(
      StockMove stockMove, PurchaseOrder purchaseOrder, Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException {

    if (!supplyChainConfigService
            .getSupplyChainConfig(stockMove.getCompany())
            .getActivateIncStockMovePartialInvoicing()
        && computeNonCanceledInvoiceQty(stockMove).signum() > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.STOCK_MOVE_PARTIAL_INVOICE_ERROR));
    }

    InvoiceGenerator invoiceGenerator =
        purchaseOrderInvoiceService.createInvoiceGenerator(
            purchaseOrder, stockMove.getIsReversion());

    Invoice invoice = invoiceGenerator.generate();

    invoiceGenerator.populate(
        invoice,
        this.createInvoiceLines(invoice, stockMove.getStockMoveLineList(), qtyToInvoiceMap));

    if (invoice != null) {

      // do not create empty invoices
      if (invoice.getInvoiceLineList() == null || invoice.getInvoiceLineList().isEmpty()) {
        return null;
      }
      this.extendInternalReference(stockMove, invoice);
      invoice.setAddressStr(
          Beans.get(AddressService.class).computeAddressStr(invoice.getAddress()));

      if (invoice != null) {
        Set<StockMove> stockMoveSet = invoice.getStockMoveSet();
        if (stockMoveSet == null) {
          stockMoveSet = new HashSet<>();
          invoice.setStockMoveSet(stockMoveSet);
        }
        stockMoveSet.add(stockMove);
      }

      invoiceRepository.save(invoice);
    }
    return invoice;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Invoice createInvoiceFromOrderlessStockMove(
      StockMove stockMove, Map<Long, BigDecimal> qtyToInvoiceMap) throws AxelorException {

    int stockMoveType = stockMove.getTypeSelect();
    int invoiceOperationType;

    if (stockMove.getIsReversion()) {
      if (stockMoveType == StockMoveRepository.TYPE_INCOMING) {
        invoiceOperationType = InvoiceRepository.OPERATION_TYPE_CLIENT_REFUND;
      } else if (stockMoveType == StockMoveRepository.TYPE_OUTGOING) {
        invoiceOperationType = InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND;
      } else {
        return null;
      }
    } else {
      if (stockMoveType == StockMoveRepository.TYPE_INCOMING) {
        invoiceOperationType = InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE;
      } else if (stockMoveType == StockMoveRepository.TYPE_OUTGOING) {
        invoiceOperationType = InvoiceRepository.OPERATION_TYPE_CLIENT_SALE;
      } else {
        return null;
      }
    }
    // do not use invoiced partner if the option is disabled
    if (!appSupplychainService.getAppSupplychain().getActivatePartnerRelations()) {
      stockMove.setInvoicedPartner(null);
    }

    InvoiceGenerator invoiceGenerator =
        new InvoiceGeneratorSupplyChain(stockMove, invoiceOperationType) {

          @Override
          public Invoice generate() throws AxelorException {

            return super.createInvoiceHeader();
          }
        };

    Invoice invoice = invoiceGenerator.generate();

    invoiceGenerator.populate(
        invoice,
        this.createInvoiceLines(invoice, stockMove.getStockMoveLineList(), qtyToInvoiceMap));

    if (invoice != null) {

      this.extendInternalReference(stockMove, invoice);
      invoice.setAddressStr(
          Beans.get(AddressService.class).computeAddressStr(invoice.getAddress()));
      if (stockMoveType == StockMoveRepository.TYPE_OUTGOING) {
        invoice.setHeadOfficeAddress(stockMove.getPartner().getHeadOfficeAddress());
      }
      invoiceRepository.save(invoice);

      if (invoice != null) {
        Set<StockMove> stockMoveSet = invoice.getStockMoveSet();
        if (stockMoveSet == null) {
          stockMoveSet = new HashSet<>();
          invoice.setStockMoveSet(stockMoveSet);
        }
        stockMoveSet.add(stockMove);
      }

      invoiceRepository.save(invoice);
    }

    return invoice;
  }

  @Override
  public Invoice extendInternalReference(StockMove stockMove, Invoice invoice) {

    invoice.setInternalReference(
        StringTool.cutTooLongString(
            stockMove.getStockMoveSeq() + ":" + invoice.getInternalReference()));

    return invoice;
  }

  @Override
  public List<InvoiceLine> createInvoiceLines(
      Invoice invoice, List<StockMoveLine> stockMoveLineList, Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    StockMove stockMove = stockMoveLineList.get(0).getStockMove();

    List<StockMoveLine> stockMoveLineToInvoiceList;
    if ((StockMoveRepository.ORIGIN_PURCHASE_ORDER.equals(stockMove.getOriginTypeSelect())
            && supplyChainConfigService
                .getSupplyChainConfig(invoice.getCompany())
                .getActivateIncStockMovePartialInvoicing())
        || (StockMoveRepository.ORIGIN_SALE_ORDER.equals(stockMove.getOriginTypeSelect())
            && supplyChainConfigService
                .getSupplyChainConfig(invoice.getCompany())
                .getActivateOutStockMovePartialInvoicing())) {
      // we do not consolidate because the invoicing is partial
      stockMoveLineToInvoiceList = stockMoveLineList;
    } else {
      stockMoveLineToInvoiceList = getConsolidatedStockMoveLineList(stockMoveLineList);
    }
    for (StockMoveLine stockMoveLine : stockMoveLineToInvoiceList) {

      InvoiceLine invoiceLineCreated;
      Long id = stockMoveLine.getId();
      if (qtyToInvoiceMap != null) {
        invoiceLineCreated =
            this.createInvoiceLine(invoice, stockMoveLine, qtyToInvoiceMap.get(id));
      } else {
        invoiceLineCreated =
            this.createInvoiceLine(
                invoice,
                stockMoveLine,
                stockMoveLine.getRealQty().subtract(computeNonCanceledInvoiceQty(stockMoveLine)));
      }

      if (invoiceLineCreated != null) {
        invoiceLineList.add(invoiceLineCreated);
      }
    }
    return invoiceLineList;
  }

  @Override
  public InvoiceLine createInvoiceLine(Invoice invoice, StockMoveLine stockMoveLine, BigDecimal qty)
      throws AxelorException {

    Product product = stockMoveLine.getProduct();
    boolean isTitleLine = false;

    int sequence = InvoiceLineGenerator.DEFAULT_SEQUENCE;
    SaleOrderLine saleOrderLine = stockMoveLine.getSaleOrderLine();
    PurchaseOrderLine purchaseOrderLine = stockMoveLine.getPurchaseOrderLine();

    if (saleOrderLine != null) {
      sequence = saleOrderLine.getSequence();
    } else if (purchaseOrderLine != null) {
      if (purchaseOrderLine.getIsTitleLine()) {
        isTitleLine = true;
      }
      sequence = purchaseOrderLine.getSequence();
    }

    // do not create lines with no qties
    if ((qty == null || qty.signum() == 0 || stockMoveLine.getRealQty().signum() == 0)
        && !isTitleLine) {
      return null;
    }
    if (product == null && !isTitleLine) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.STOCK_MOVE_INVOICE_1),
          stockMoveLine.getStockMove().getStockMoveSeq());
    }

    InvoiceLineGenerator invoiceLineGenerator =
        new InvoiceLineGeneratorSupplyChain(
            invoice,
            product,
            stockMoveLine.getProductName(),
            stockMoveLine.getDescription(),
            qty,
            stockMoveLine.getUnit(),
            sequence,
            false,
            stockMoveLine.getSaleOrderLine(),
            stockMoveLine.getPurchaseOrderLine(),
            stockMoveLine) {
          @Override
          public List<InvoiceLine> creates() throws AxelorException {

            InvoiceLine invoiceLine = this.createInvoiceLine();

            List<InvoiceLine> invoiceLines = new ArrayList<>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };

    List<InvoiceLine> invoiceLines = invoiceLineGenerator.creates();
    InvoiceLine invoiceLine = null;
    if (invoiceLines != null && !invoiceLines.isEmpty()) {
      invoiceLine = invoiceLines.get(0);
      if (!stockMoveLine.getIsMergedStockMoveLine()) {
        // not a consolidated line so we can set the reference.
        invoiceLine.setStockMoveLine(stockMoveLine);
      } else {
        // set the reference to a correct stock move line by following either the sale order line or
        // purchase order line. We cannot have a consolidated line without purchase order line or
        // sale order line reference
        StockMoveLine nonConsolidatedStockMoveLine = null;
        StockMove stockMove = stockMoveLine.getStockMove();
        if (saleOrderLine != null) {
          nonConsolidatedStockMoveLine =
              stockMoveLineRepository
                  .all()
                  .filter(
                      "self.saleOrderLine.id = :saleOrderLineId "
                          + "AND self.stockMove.id = :stockMoveId "
                          + "AND self.id != :stockMoveLineId")
                  .bind("saleOrderLineId", saleOrderLine.getId())
                  .bind("stockMoveId", stockMove.getId())
                  .bind("stockMoveLineId", stockMoveLine.getId())
                  .order("id")
                  .fetchOne();
        } else if (purchaseOrderLine != null) {
          nonConsolidatedStockMoveLine =
              stockMoveLineRepository
                  .all()
                  .filter(
                      "self.purchaseOrderLine.id = :purchaseOrderLineId "
                          + "AND self.stockMove.id = :stockMoveId "
                          + "AND self.id != :stockMoveLineId")
                  .bind("purchaseOrderLineId", purchaseOrderLine.getId())
                  .bind("stockMoveId", stockMove.getId())
                  .bind("stockMoveLineId", stockMoveLine.getId())
                  .order("id")
                  .fetchOne();
        }
        invoiceLine.setStockMoveLine(nonConsolidatedStockMoveLine);
        deleteConsolidatedStockMoveLine(stockMoveLine);
      }
    }
    return invoiceLine;
  }

  protected void deleteConsolidatedStockMoveLine(StockMoveLine stockMoveLine) {
    if (stockMoveLine.getStockMove() != null
        && stockMoveLine.getStockMove().getStockMoveLineList() != null) {
      stockMoveLine.getStockMove().getStockMoveLineList().remove(stockMoveLine);
    }
    stockMoveLineRepository.remove(stockMoveLine);
  }

  /**
   * Get a list of stock move lines consolidated by parent line (sale or purchase order).
   *
   * @param stockMoveLineList
   * @return
   * @throws AxelorException
   */
  private List<StockMoveLine> getConsolidatedStockMoveLineList(
      List<StockMoveLine> stockMoveLineList) throws AxelorException {

    Map<SaleOrderLine, List<StockMoveLine>> stockMoveLineSaleMap = new LinkedHashMap<>();
    Map<PurchaseOrderLine, List<StockMoveLine>> stockMoveLinePurchaseMap = new LinkedHashMap<>();
    List<StockMoveLine> resultList = new ArrayList<>();

    List<StockMoveLine> list;
    for (StockMoveLine stockMoveLine : stockMoveLineList) {

      if (stockMoveLine.getSaleOrderLine() != null) {
        list = stockMoveLineSaleMap.get(stockMoveLine.getSaleOrderLine());
        if (list == null) {
          list = new ArrayList<>();
          stockMoveLineSaleMap.put(stockMoveLine.getSaleOrderLine(), list);
        }
        list.add(stockMoveLine);
      } else if (stockMoveLine.getPurchaseOrderLine() != null) {
        list = stockMoveLinePurchaseMap.get(stockMoveLine.getPurchaseOrderLine());
        if (list == null) {
          list = new ArrayList<>();
          stockMoveLinePurchaseMap.put(stockMoveLine.getPurchaseOrderLine(), list);
        }
        list.add(stockMoveLine);
      } else { // if the stock move line does not have a parent line (sale or purchase order line)
        resultList.add(stockMoveLine);
      }
    }

    for (List<StockMoveLine> stockMoveLines : stockMoveLineSaleMap.values()) {
      resultList.add(stockMoveLineServiceSupplychain.getMergedStockMoveLine(stockMoveLines));
    }
    for (List<StockMoveLine> stockMoveLines : stockMoveLinePurchaseMap.values()) {
      resultList.add(stockMoveLineServiceSupplychain.getMergedStockMoveLine(stockMoveLines));
    }
    return resultList;
  }

  @Override
  public List<Map<String, Object>> getStockMoveLinesToInvoice(StockMove stockMove)
      throws AxelorException {
    List<Map<String, Object>> stockMoveLines = new ArrayList<>();

    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {

      BigDecimal qty =
          stockMoveLine.getRealQty().subtract(computeNonCanceledInvoiceQty(stockMoveLine));
      if (qty.compareTo(BigDecimal.ZERO) != 0) {
        Map<String, Object> stockMoveLineMap = new HashMap<>();
        stockMoveLineMap.put(
            "productCode",
            stockMoveLine.getProduct() != null ? stockMoveLine.getProduct().getCode() : null);
        stockMoveLineMap.put("productName", stockMoveLine.getProductName());
        stockMoveLineMap.put("remainingQty", qty);
        stockMoveLineMap.put("realQty", stockMoveLine.getRealQty());
        stockMoveLineMap.put("qtyInvoiced", computeNonCanceledInvoiceQty(stockMoveLine));
        stockMoveLineMap.put("qtyToInvoice", BigDecimal.ZERO);
        stockMoveLineMap.put("invoiceAll", false);
        stockMoveLineMap.put("stockMoveLineId", stockMoveLine.getId());
        stockMoveLines.add(stockMoveLineMap);
      }
    }
    return stockMoveLines;
  }

  @Override
  public BigDecimal computeNonCanceledInvoiceQty(StockMove stockMove) throws AxelorException {
    if (stockMove.getStockMoveLineList() == null) {
      return BigDecimal.ZERO;
    }
    BigDecimal nonCanceledInvoiceQtySum = BigDecimal.ZERO;
    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
      nonCanceledInvoiceQtySum =
          nonCanceledInvoiceQtySum.add(computeNonCanceledInvoiceQty(stockMoveLine));
    }
    return nonCanceledInvoiceQtySum;
  }

  @Override
  public BigDecimal computeNonCanceledInvoiceQty(StockMoveLine stockMoveLine)
      throws AxelorException {
    List<InvoiceLine> nonCanceledInvoiceLineList =
        invoiceLineRepository
            .all()
            .filter(
                "self.invoice.statusSelect != :invoiceCanceled "
                    + "AND self.stockMoveLine.id = :stockMoveLineId")
            .bind("invoiceCanceled", InvoiceRepository.STATUS_CANCELED)
            .bind("stockMoveLineId", stockMoveLine.getId())
            .fetch();
    BigDecimal nonCanceledInvoiceQty = BigDecimal.ZERO;
    for (InvoiceLine invoiceLine : nonCanceledInvoiceLineList) {
      if (isInvoiceRefundingStockMove(stockMoveLine.getStockMove(), invoiceLine.getInvoice())) {
        nonCanceledInvoiceQty = nonCanceledInvoiceQty.subtract(invoiceLine.getQty());
      } else {
        nonCanceledInvoiceQty = nonCanceledInvoiceQty.add(invoiceLine.getQty());
      }
    }
    return nonCanceledInvoiceQty;
  }

  @Override
  public void computeStockMoveInvoicingStatus(StockMove stockMove) {
    int invoicingStatus = StockMoveRepository.STATUS_NOT_INVOICED;
    if (stockMove.getStockMoveLineList() != null
        && stockMove.getInvoiceSet() != null
        && !stockMove.getInvoiceSet().isEmpty()) {
      BigDecimal totalInvoicedQty =
          stockMove.getStockMoveLineList().stream()
              .map(StockMoveLine::getQtyInvoiced)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);
      BigDecimal totalRealQty =
          stockMove.getStockMoveLineList().stream()
              .map(StockMoveLine::getRealQty)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);

      // the status will stay at not invoiced if totalInvoicedQty is at 0 (or negative) while
      // realQty is > 0.
      if (totalInvoicedQty.signum() == 0 && totalRealQty.signum() == 0) {
        // special case where we invoice a stock move line with no quantities.
        invoicingStatus = StockMoveRepository.STATUS_INVOICED;
      } else if (totalInvoicedQty.signum() > 0 && totalRealQty.compareTo(totalInvoicedQty) > 0) {
        invoicingStatus = StockMoveRepository.STATUS_PARTIALLY_INVOICED;
      } else if (totalRealQty.compareTo(totalInvoicedQty) == 0) {
        invoicingStatus = StockMoveRepository.STATUS_INVOICED;
      }
    }
    stockMove.setInvoicingStatusSelect(invoicingStatus);
  }

  @Override
  public boolean isInvoiceRefundingStockMove(StockMove stockMove, Invoice invoice)
      throws AxelorException {
    boolean isRefundInvoice = InvoiceToolService.isRefund(invoice);
    boolean isReversionStockMove = stockMove.getIsReversion();
    return isRefundInvoice != isReversionStockMove;
  }
}
