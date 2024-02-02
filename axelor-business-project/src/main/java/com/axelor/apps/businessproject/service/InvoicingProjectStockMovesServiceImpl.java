package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.SaleOrderLineSaleRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.service.StockMoveInvoiceService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InvoicingProjectStockMovesServiceImpl implements InvoicingProjectStockMovesService {

  protected final StockMoveLineRepository stockMoveLineRepository;

  protected final StockMoveInvoiceService stockMoveInvoiceService;

  @Inject
  public InvoicingProjectStockMovesServiceImpl(
      StockMoveLineRepository stockMoveLineRepository,
      StockMoveInvoiceService stockMoveInvoiceService) {
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.stockMoveInvoiceService = stockMoveInvoiceService;
  }

  @Override
  public void processDeliveredSaleOrderLines(
      Set<StockMoveLine> deliverySet, List<SaleOrderLine> saleOrderLineList) {
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      if (shouldProcessSaleOrderLine(saleOrderLine)) {
        addStockMoveLinesToDeliverySet(deliverySet, saleOrderLine);
      }
    }
  }

  protected boolean shouldProcessSaleOrderLine(SaleOrderLine saleOrderLine) {
    return saleOrderLine.getInvoicingModeSelect()
            == SaleOrderLineSaleRepository.INVOICING_MODE_ON_DELIVERY
        && saleOrderLine.getDeliveryState() == SaleOrderLineRepository.DELIVERY_STATE_DELIVERED;
  }

  protected void addStockMoveLinesToDeliverySet(
      Set<StockMoveLine> deliverySet, SaleOrderLine saleOrderLine) {
    List<StockMoveLine> stockMoveLineList = findRelevantStockMoveLines(saleOrderLine);

    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      if (isStockMoveLineEligible(stockMoveLine)) {
        deliverySet.add(stockMoveLine);
      }
    }
  }

  protected List<StockMoveLine> findRelevantStockMoveLines(SaleOrderLine saleOrderLine) {
    return stockMoveLineRepository
        .all()
        .filter(
            "self.saleOrderLine = :saleOrderLine AND self.stockMove IS NOT NULL "
                + "AND self.stockMove.statusSelect = :statusSelect")
        .bind("saleOrderLine", saleOrderLine)
        .bind("statusSelect", StockMoveRepository.STATUS_REALIZED)
        .fetch();
  }

  protected boolean isStockMoveLineEligible(StockMoveLine stockMoveLine) {
    return stockMoveLine
            .getRealQty()
            .subtract(stockMoveLine.getQtyInvoiced())
            .compareTo(BigDecimal.ZERO)
        > 0;
  }

  @Override
  public List<InvoiceLine> createStockMovesInvoiceLines(
      Invoice invoice, Set<StockMoveLine> deliverySet) throws AxelorException {

    Map<StockMove, List<StockMoveLine>> stockMoveMap = groupStockMoveLines(deliverySet);

    List<InvoiceLine> invoiceLineList = new ArrayList<>();
    for (Map.Entry<StockMove, List<StockMoveLine>> entry : stockMoveMap.entrySet()) {
      StockMove stockMove = entry.getKey();
      List<StockMoveLine> stockMoveLines = entry.getValue();

      Map<Long, BigDecimal> qtyToInvoiceMap = calculateQtyToInvoice(stockMoveLines);

      invoiceLineList.addAll(
          createInvoiceLines(invoice, stockMove, stockMoveLines, qtyToInvoiceMap));

      updateInvoiceStockMoves(invoice, stockMove);
    }

    return invoiceLineList;
  }

  protected Map<StockMove, List<StockMoveLine>> groupStockMoveLines(
      Set<StockMoveLine> deliverySet) {
    Map<StockMove, List<StockMoveLine>> stockMoveMap = new HashMap<>();
    for (StockMoveLine stockMoveLine : deliverySet) {
      StockMove stockMove = stockMoveLine.getStockMove();
      stockMoveMap.computeIfAbsent(stockMove, k -> new ArrayList<>()).add(stockMoveLine);
    }
    return stockMoveMap;
  }

  protected Map<Long, BigDecimal> calculateQtyToInvoice(List<StockMoveLine> stockMoveLines) {
    Map<Long, BigDecimal> qtyToInvoiceMap = new HashMap<>();
    for (StockMoveLine stockMoveLine : stockMoveLines) {
      BigDecimal qtyToInvoice = stockMoveLine.getRealQty().subtract(stockMoveLine.getQtyInvoiced());
      qtyToInvoiceMap.put(stockMoveLine.getId(), qtyToInvoice);
    }
    return qtyToInvoiceMap;
  }

  protected List<InvoiceLine> createInvoiceLines(
      Invoice invoice,
      StockMove stockMove,
      List<StockMoveLine> stockMoveLines,
      Map<Long, BigDecimal> qtyToInvoiceMap)
      throws AxelorException {
    return stockMoveInvoiceService.createInvoiceLines(
        invoice, stockMove, stockMoveLines, qtyToInvoiceMap);
  }

  protected void updateInvoiceStockMoves(Invoice invoice, StockMove stockMove) {
    Set<StockMove> stockMoveSet = invoice.getStockMoveSet();
    if (stockMoveSet == null) {
      stockMoveSet = new HashSet<>();
      invoice.setStockMoveSet(stockMoveSet);
    }
    stockMoveSet.add(stockMove);
  }
}
