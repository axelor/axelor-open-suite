/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceGeneratorSupplyChain;
import com.axelor.apps.supplychain.service.invoice.generator.InvoiceLineGeneratorSupplyChain;
import com.axelor.apps.tool.StringTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StockMoveInvoiceServiceImpl implements StockMoveInvoiceService {

  private SaleOrderInvoiceService saleOrderInvoiceService;
  private PurchaseOrderInvoiceService purchaseOrderInvoiceService;
  private StockMoveLineServiceSupplychain stockMoveLineServiceSupplychain;
  private InvoiceRepository invoiceRepository;
  private StockMoveRepository stockMoveRepo;
  private SaleOrderRepository saleOrderRepo;
  private PurchaseOrderRepository purchaseOrderRepo;

  @Inject
  public StockMoveInvoiceServiceImpl(
      SaleOrderInvoiceService saleOrderInvoiceService,
      PurchaseOrderInvoiceService purchaseOrderInvoiceService,
      StockMoveLineServiceSupplychain stockMoveLineServiceSupplychain,
      InvoiceRepository invoiceRepository,
      StockMoveRepository stockMoveRepo,
      SaleOrderRepository saleOrderRepo,
      PurchaseOrderRepository purchaseOrderRepo) {
    this.saleOrderInvoiceService = saleOrderInvoiceService;
    this.purchaseOrderInvoiceService = purchaseOrderInvoiceService;
    this.stockMoveLineServiceSupplychain = stockMoveLineServiceSupplychain;
    this.invoiceRepository = invoiceRepository;
    this.stockMoveRepo = stockMoveRepo;
    this.saleOrderRepo = saleOrderRepo;
    this.purchaseOrderRepo = purchaseOrderRepo;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public Invoice createInvoice(StockMove stockMove) throws AxelorException {

    Long origin = stockMove.getOriginId();

    if (StockMoveRepository.ORIGIN_SALE_ORDER.equals(stockMove.getOriginTypeSelect())) {
      return createInvoiceFromSaleOrder(stockMove, saleOrderRepo.find(origin));
    } else if (StockMoveRepository.ORIGIN_PURCHASE_ORDER.equals(stockMove.getOriginTypeSelect())) {
      return createInvoiceFromPurchaseOrder(stockMove, purchaseOrderRepo.find(origin));
    } else {
      return createInvoiceFromOrderlessStockMove(stockMove);
    }
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Invoice createInvoiceFromSaleOrder(StockMove stockMove, SaleOrder saleOrder)
      throws AxelorException {

    if (stockMove.getInvoice() != null
        && stockMove.getInvoice().getStatusSelect() != InvoiceRepository.STATUS_CANCELED) {
      throw new AxelorException(
          stockMove,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.OUTGOING_STOCK_MOVE_INVOICE_EXISTS),
          stockMove.getName());
    }
    InvoiceGenerator invoiceGenerator =
        saleOrderInvoiceService.createInvoiceGenerator(saleOrder, stockMove.getIsReversion());

    Invoice invoice = invoiceGenerator.generate();

    invoiceGenerator.populate(
        invoice, this.createInvoiceLines(invoice, stockMove.getStockMoveLineList()));

    if (invoice != null) {
      invoice.setSaleOrder(saleOrder);
      saleOrderInvoiceService.fillInLines(invoice);
      this.extendInternalReference(stockMove, invoice);
      invoice.setAddressStr(saleOrder.getMainInvoicingAddressStr());

      // fill default advance payment invoice
      if (invoice.getOperationSubTypeSelect() != InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {
        invoice.setAdvancePaymentInvoiceSet(
            Beans.get(InvoiceService.class).getDefaultAdvancePaymentInvoice(invoice));
      }

      invoice.setPartnerTaxNbr(saleOrder.getClientPartner().getTaxNbr());

      invoiceRepository.save(invoice);

      stockMove.setInvoice(invoice);
      stockMoveRepo.save(stockMove);
    }

    return invoice;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Invoice createInvoiceFromPurchaseOrder(StockMove stockMove, PurchaseOrder purchaseOrder)
      throws AxelorException {

    if (stockMove.getInvoice() != null
        && stockMove.getInvoice().getStatusSelect() != InvoiceRepository.STATUS_CANCELED) {
      throw new AxelorException(
          stockMove,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.INCOMING_STOCK_MOVE_INVOICE_EXISTS),
          stockMove.getName());
    }
    InvoiceGenerator invoiceGenerator =
        purchaseOrderInvoiceService.createInvoiceGenerator(
            purchaseOrder, stockMove.getIsReversion());

    Invoice invoice = invoiceGenerator.generate();

    invoiceGenerator.populate(
        invoice, this.createInvoiceLines(invoice, stockMove.getStockMoveLineList()));

    if (invoice != null) {

      this.extendInternalReference(stockMove, invoice);
      invoice.setAddressStr(
          Beans.get(AddressService.class).computeAddressStr(invoice.getAddress()));
      invoiceRepository.save(invoice);

      stockMove.setInvoice(invoice);
      stockMoveRepo.save(stockMove);
    }
    return invoice;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Invoice createInvoiceFromOrderlessStockMove(StockMove stockMove) throws AxelorException {

    int stockMoveType = stockMove.getTypeSelect();
    Invoice invoice = stockMove.getInvoice();
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

    if (invoice != null && invoice.getStatusSelect() != InvoiceRepository.STATUS_CANCELED) {
      if (stockMoveType == StockMoveRepository.TYPE_INCOMING) {
        throw new AxelorException(
            stockMove,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.INCOMING_STOCK_MOVE_INVOICE_EXISTS),
            stockMove.getName());
      } else {
        throw new AxelorException(
            stockMove,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.OUTGOING_STOCK_MOVE_INVOICE_EXISTS),
            stockMove.getName());
      }
    }

    InvoiceGenerator invoiceGenerator =
        new InvoiceGeneratorSupplyChain(stockMove, invoiceOperationType) {

          @Override
          public Invoice generate() throws AxelorException {

            return super.createInvoiceHeader();
          }
        };

    invoice = invoiceGenerator.generate();

    invoiceGenerator.populate(
        invoice, this.createInvoiceLines(invoice, stockMove.getStockMoveLineList()));

    if (invoice != null) {
      saleOrderInvoiceService.fillInLines(invoice);
      this.extendInternalReference(stockMove, invoice);
      invoice.setAddressStr(
          Beans.get(AddressService.class).computeAddressStr(invoice.getAddress()));
      if(stockMoveType == StockMoveRepository.TYPE_OUTGOING) {
        invoice.setHeadOfficeAddress(stockMove.getPartner().getHeadOfficeAddress());
      }
      invoiceRepository.save(invoice);

      stockMove.setInvoice(invoice);
      stockMoveRepo.save(stockMove);
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
      Invoice invoice, List<StockMoveLine> stockMoveLineList) throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

    Map<StockMoveLine, InvoiceLine> packLineMap = Maps.newHashMap();
    boolean setPack = Beans.get(AppSaleService.class).getAppSale().getProductPackMgt();
    for (StockMoveLine stockMoveLine : getConsolidatedStockMoveLineList(stockMoveLineList)) {
      List<InvoiceLine> invoiceLineListCreated = this.createInvoiceLine(invoice, stockMoveLine);
      if (invoiceLineListCreated != null) {
        invoiceLineList.addAll(invoiceLineListCreated);
        if (setPack
            && !invoiceLineListCreated.isEmpty()
            && (stockMoveLine.getLineTypeSelect() == StockMoveLineRepository.TYPE_PACK
                || stockMoveLine.getIsSubLine())) {
          packLineMap.put(stockMoveLine, invoiceLineListCreated.get(0));
        }
      }
      // Depending on stockMove type
      if (stockMoveLine.getSaleOrderLine() != null) {
        stockMoveLine.getSaleOrderLine().setInvoiced(true);
      } else if (stockMoveLine.getPurchaseOrderLine() != null) {
        stockMoveLine.getPurchaseOrderLine().setInvoiced(true);
      }
    }

    if (setPack) {
      for (StockMoveLine stockMoveLine : packLineMap.keySet()) {
        if (stockMoveLine.getLineTypeSelect() == StockMoveLineRepository.TYPE_PACK) {
          InvoiceLine invoiceLine = packLineMap.get(stockMoveLine);
          if (invoiceLine == null) {
            continue;
          }
          BigDecimal totalPack = BigDecimal.ZERO;
          for (StockMoveLine subLine : stockMoveLine.getSubLineList()) {
            InvoiceLine subInvoiceLine = packLineMap.get(subLine);
            if (subInvoiceLine != null) {
              totalPack = totalPack.add(subInvoiceLine.getExTaxTotal());
              subInvoiceLine.setParentLine(invoiceLine);
            }
          }
          invoiceLine.setTotalPack(totalPack);
        }
      }
    }

    return invoiceLineList;
  }

  @Override
  public List<InvoiceLine> createInvoiceLine(Invoice invoice, StockMoveLine stockMoveLine)
      throws AxelorException {

    Product product = stockMoveLine.getProduct();
    boolean isTitleLine = false;

    int sequence = InvoiceLineGenerator.DEFAULT_SEQUENCE;
    SaleOrderLine saleOrderLine = stockMoveLine.getSaleOrderLine();
    PurchaseOrderLine purchaseOrderLine = stockMoveLine.getPurchaseOrderLine();

    if (saleOrderLine != null) {
      if (saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_PACK) {
        isTitleLine = true;
      }
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
            stockMoveLine.getRealQty(),
            stockMoveLine.getUnit(),
            sequence,
            false,
            stockMoveLine.getSaleOrderLine(),
            stockMoveLine.getPurchaseOrderLine(),
            stockMoveLine,
            stockMoveLine.getIsSubLine(),
            stockMoveLine.getPackPriceSelect()) {
          @Override
          public List<InvoiceLine> creates() throws AxelorException {

            InvoiceLine invoiceLine = this.createInvoiceLine();

            List<InvoiceLine> invoiceLines = new ArrayList<InvoiceLine>();
            invoiceLines.add(invoiceLine);

            return invoiceLines;
          }
        };

    return invoiceLineGenerator.creates();
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
}
