package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.stock.db.StockHistoryLine;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.service.StockHistoryServiceImpl;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class StockHistoryServiceSupplyChainImpl extends StockHistoryServiceImpl {

  @Inject
  public StockHistoryServiceSupplyChainImpl(
      StockMoveLineRepository stockMoveLineRepository,
      UnitConversionService unitConversionService) {
    super(stockMoveLineRepository, unitConversionService);
  }

  @Override
  protected void fillOutgoingStockHistoryLineFields(
      StockHistoryLine stockHistoryLine, List<StockMoveLine> stockMoveLineList)
      throws AxelorException {

    super.fillOutgoingStockHistoryLineFields(stockHistoryLine, stockMoveLineList);

    BigDecimal sumOutQtyPeriod = BigDecimal.ZERO;
    BigDecimal sumOneoffSaleOutQtyPeriod = BigDecimal.ZERO;
    BigDecimal qtyConverted = BigDecimal.ZERO;
    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      qtyConverted =
          unitConversionService.convert(
              stockMoveLine.getUnit(),
              stockMoveLine.getProduct().getUnit(),
              stockMoveLine.getRealQty(),
              stockMoveLine.getRealQty().scale(),
              stockMoveLine.getProduct());
      if (stockMoveLine.getSaleOrderLine().getSaleOrder().getOneoffSale()) {
        sumOneoffSaleOutQtyPeriod = sumOneoffSaleOutQtyPeriod.add(qtyConverted);
      } else {
        sumOutQtyPeriod = sumOutQtyPeriod.add(qtyConverted);
      }
    }
    stockHistoryLine.setSumOutQtyPeriod(sumOutQtyPeriod);
    stockHistoryLine.setSumOneoffSaleOutQtyPeriod(sumOneoffSaleOutQtyPeriod);
  }
}
