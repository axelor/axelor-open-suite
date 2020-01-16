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
import com.axelor.apps.supplychain.exception.IExceptionMessage;
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
import java.util.Set;

public class StockMoveInvoiceServiceImpl implements StockMoveInvoiceService {

  private SaleOrderInvoiceService saleOrderInvoiceService;
  private PurchaseOrderInvoiceService purchaseOrderInvoiceService;
  private StockMoveLineServiceSupplychain stockMoveLineServiceSupplychain;
  private InvoiceRepository invoiceRepository;
  private SaleOrderRepository saleOrderRepo;
  private PurchaseOrderRepository purchaseOrderRepo;
  private StockMoveLineRepository stockMoveLineRepository;
  private InvoiceLineRepository invoiceLineRepository;

  @Inject
  public StockMoveInvoiceServiceImpl(
      SaleOrderInvoiceService saleOrderInvoiceService,
      PurchaseOrderInvoiceService purchaseOrderInvoiceService,
      StockMoveLineServiceSupplychain stockMoveLineServiceSupplychain,
      InvoiceRepository invoiceRepository,
      SaleOrderRepository saleOrderRepo,
      PurchaseOrderRepository purchaseOrderRepo,
      StockMoveLineRepository stockMoveLineRepository,
      InvoiceLineRepository invoiceLineRepository) {
    this.saleOrderInvoiceService = saleOrderInvoiceService;
    this.purchaseOrderInvoiceService = purchaseOrderInvoiceService;
    this.stockMoveLineServiceSupplychain = stockMoveLineServiceSupplychain;
    this.invoiceRepository = invoiceRepository;
    this.saleOrderRepo = saleOrderRepo;
    this.purchaseOrderRepo = purchaseOrderRepo;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.invoiceLineRepository = invoiceLineRepository;
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
      for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
        qtyToInvoiceMap.put(
            stockMoveLine.getId(),
            stockMoveLine.getRealQty().subtract(getNonCanceledInvoiceQty(stockMoveLine)));
      }
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

    InvoiceGenerator invoiceGenerator =
        saleOrderInvoiceService.createInvoiceGenerator(saleOrder, stockMove.getIsReversion());

    Invoice invoice = invoiceGenerator.generate();

    invoiceGenerator.populate(
        invoice,
        this.createInvoiceLines(invoice, stockMove.getStockMoveLineList(), qtyToInvoiceMap));

    if (invoice != null) {
      invoice.setSaleOrder(saleOrder);
      this.extendInternalReference(stockMove, invoice);
      invoice.setAddressStr(saleOrder.getMainInvoicingAddressStr());

      // fill default advance payment invoice
      if (invoice.getOperationSubTypeSelect() != InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {
        invoice.setAdvancePaymentInvoiceSet(
            Beans.get(InvoiceService.class).getDefaultAdvancePaymentInvoice(invoice));
      }

      invoice.setPartnerTaxNbr(saleOrder.getClientPartner().getTaxNbr());
      if (ObjectUtils.isEmpty(invoice.getNote())) {
        if (!Strings.isNullOrEmpty(saleOrder.getInvoiceComments())
            && invoice.getCompanyBankDetails() != null
            && !Strings.isNullOrEmpty(invoice.getCompanyBankDetails().getSpecificNoteOnInvoice())) {
          invoice.setNote(
              saleOrder.getInvoiceComments()
                  + "\n"
                  + invoice.getCompanyBankDetails().getSpecificNoteOnInvoice());
        } else if (Strings.isNullOrEmpty(saleOrder.getInvoiceComments())
            && invoice.getCompanyBankDetails() != null
            && !Strings.isNullOrEmpty(invoice.getCompanyBankDetails().getSpecificNoteOnInvoice())) {
          invoice.setNote(saleOrder.getInvoiceComments());
        } else if (!Strings.isNullOrEmpty(saleOrder.getInvoiceComments())
            && invoice.getCompanyBankDetails() != null
            && Strings.isNullOrEmpty(invoice.getCompanyBankDetails().getSpecificNoteOnInvoice())) {
          invoice.setNote(invoice.getCompanyBankDetails().getSpecificNoteOnInvoice());
        }
      }

      if (ObjectUtils.isEmpty(invoice.getProformaComments())) {
        if (!Strings.isNullOrEmpty(saleOrder.getProformaComments())
            && invoice.getCompanyBankDetails() != null
            && !Strings.isNullOrEmpty(invoice.getCompanyBankDetails().getSpecificNoteOnInvoice())) {
          invoice.setProformaComments(
              saleOrder.getProformaComments()
                  + "\n"
                  + invoice.getCompanyBankDetails().getSpecificNoteOnInvoice());
        } else if (Strings.isNullOrEmpty(saleOrder.getProformaComments())
            && invoice.getCompanyBankDetails() != null
            && !Strings.isNullOrEmpty(invoice.getCompanyBankDetails().getSpecificNoteOnInvoice())) {
          invoice.setProformaComments(saleOrder.getProformaComments());
        } else if (!Strings.isNullOrEmpty(saleOrder.getProformaComments())
            && invoice.getCompanyBankDetails() != null
            && Strings.isNullOrEmpty(invoice.getCompanyBankDetails().getSpecificNoteOnInvoice())) {
          invoice.setProformaComments(invoice.getCompanyBankDetails().getSpecificNoteOnInvoice());
        }
      }

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
  public Invoice createInvoiceFromPurchaseOrder(
      StockMove stockMove, PurchaseOrder purchaseOrder, Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException {

    InvoiceGenerator invoiceGenerator =
        purchaseOrderInvoiceService.createInvoiceGenerator(
            purchaseOrder, stockMove.getIsReversion());

    Invoice invoice = invoiceGenerator.generate();

    invoiceGenerator.populate(
        invoice,
        this.createInvoiceLines(invoice, stockMove.getStockMoveLineList(), qtyToInvoiceMap));

    if (invoice != null) {

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

    List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

    for (StockMoveLine stockMoveLine : getConsolidatedStockMoveLineList(stockMoveLineList)) {

      List<InvoiceLine> invoiceLineListCreated = null;
      Long id = stockMoveLine.getId();
      if (qtyToInvoiceMap != null) {
        if (qtyToInvoiceMap.containsKey(id)) {
          invoiceLineListCreated =
              this.createInvoiceLine(invoice, stockMoveLine, qtyToInvoiceMap.get(id));
        }
      } else {
        invoiceLineListCreated =
            this.createInvoiceLine(
                invoice,
                stockMoveLine,
                stockMoveLine.getRealQty().subtract(getNonCanceledInvoiceQty(stockMoveLine)));
      }

      if (invoiceLineListCreated != null) {
        invoiceLineList.addAll(invoiceLineListCreated);
      }
      // Depending on stockMove type
      if (stockMoveLine.getSaleOrderLine() != null) {
        stockMoveLine.getSaleOrderLine().setInvoiced(true);
      } else if (stockMoveLine.getPurchaseOrderLine() != null) {
        stockMoveLine.getPurchaseOrderLine().setInvoiced(true);
      }
    }
    return invoiceLineList;
  }

  @Override
  public List<InvoiceLine> createInvoiceLine(
      Invoice invoice, StockMoveLine stockMoveLine, BigDecimal qty) throws AxelorException {

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

    if (stockMoveLine.getRealQty().compareTo(BigDecimal.ZERO) == 0 && !isTitleLine) {
      return new ArrayList<InvoiceLine>();
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

            List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };

    List<InvoiceLine> invoiceLines = invoiceLineGenerator.creates();
    for (InvoiceLine invoiceLine : invoiceLines) {
      invoiceLine.setStockMoveLine(stockMoveLine);
    }
    return invoiceLines;
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
  public List<Map<String, Object>> getStockMoveLinesToInvoice(StockMove stockMove) {
    List<Map<String, Object>> stockMoveLines = new ArrayList<>();

    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {

      BigDecimal qty = stockMoveLine.getRealQty().subtract(getNonCanceledInvoiceQty(stockMoveLine));
      if (qty.compareTo(BigDecimal.ZERO) != 0) {
        Map<String, Object> stockMoveLineMap = new HashMap<>();
        stockMoveLineMap.put("productCode", stockMoveLine.getProduct().getCode());
        stockMoveLineMap.put("productName", stockMoveLine.getProductName());
        stockMoveLineMap.put("remainingQty", qty);
        stockMoveLineMap.put("realQty", stockMoveLine.getRealQty());
        stockMoveLineMap.put("qtyInvoiced", getNonCanceledInvoiceQty(stockMoveLine));
        stockMoveLineMap.put("qtyToInvoice", BigDecimal.ZERO);
        stockMoveLineMap.put("invoiceAll", false);
        stockMoveLineMap.put("stockMoveLineId", stockMoveLine.getId());
        stockMoveLines.add(stockMoveLineMap);
      }
    }
    return stockMoveLines;
  }

  @Override
  public BigDecimal getNonCanceledInvoiceQty(StockMoveLine stockMoveLine) {
    return invoiceLineRepository
        .all()
        .filter(
            "self.invoice.statusSelect != :invoiceCanceled "
                + "AND self.stockMoveLine.id = :stockMoveLineId")
        .bind("invoiceCanceled", InvoiceRepository.STATUS_CANCELED)
        .bind("stockMoveLineId", stockMoveLine.getId())
        .fetchStream()
        .map(InvoiceLine::getQty)
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);
  }

  @Override
  public void computeStockMoveInvoicingStatus(StockMove stockMove) {
    int invoicingStatus = StockMoveRepository.STATUS_NOT_INVOICED;
    if (stockMove.getStockMoveLineList() != null
        && stockMove.getInvoiceSet() != null
        && !stockMove.getInvoiceSet().isEmpty()) {
      BigDecimal totalInvoicedQty =
          stockMove
              .getStockMoveLineList()
              .stream()
              .map(StockMoveLine::getQtyInvoiced)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);
      BigDecimal totalRealQty =
          stockMove
              .getStockMoveLineList()
              .stream()
              .map(StockMoveLine::getRealQty)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);
      if (totalRealQty.compareTo(totalInvoicedQty) > 0) {
        invoicingStatus = StockMoveRepository.STATUS_PARTIALLY_INVOICED;
      } else if (totalRealQty.compareTo(totalInvoicedQty) == 0) {
        invoicingStatus = StockMoveRepository.STATUS_INVOICED;
      }
    }
    stockMove.setInvoicingStatusSelect(invoicingStatus);
  }
}
