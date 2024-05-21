package com.axelor.apps.supplychain.service.saleOrderLine;

import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.helper.SaleOrderLineHelper;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;

import java.math.BigDecimal;
import java.util.Map;

public class SaleOrderLineRecordUpdateSupplyChainServiceImpl
    implements SaleOrderLineRecordUpdateSupplyChainService {

  protected final StockMoveLineRepository stockMoveLineRepository;

  SaleOrderLineRecordUpdateSupplyChainServiceImpl(StockMoveLineRepository stockMoveLineRepository){
    this.stockMoveLineRepository = stockMoveLineRepository;
  }

  @Override
  public void updateRequestedReservedQty(
      SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {
    SaleOrderLineHelper.addAttr(
            "requestedReservedQty", "value", updateRequestedReservedQty(saleOrderLine), attrsMap);
  }

  @Override
  public BigDecimal updateRequestedReservedQty(
          SaleOrderLine saleOrderLine) {
    BigDecimal requestedReservedQty = saleOrderLine.getRequestedReservedQty();
    if (saleOrderLine.getRequestedReservedQty().compareTo(saleOrderLine.getQty()) > 0
            || saleOrderLine.getIsQtyRequested()) {
      requestedReservedQty = BigDecimal.ZERO.max(saleOrderLine.getQty());
    }
    return requestedReservedQty;
  }

  @Override
  public void setAvailabilityRequestValue(
          SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap) {

    long count = stockMoveLineRepository.all().filter("self.saleOrderLine.id = ? AND self.stockMove.availabilityRequest = TRUE AND self.stockMove.statusSelect = 2", saleOrderLine.getId()).count();
    SaleOrderLineHelper.addAttr(
            "$availabiltyRequest",
            "value",
            saleOrderLine.getId() != null && (count > 0),
            attrsMap);
  }
}
