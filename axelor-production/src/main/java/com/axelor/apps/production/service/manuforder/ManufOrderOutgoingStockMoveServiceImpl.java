package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class ManufOrderOutgoingStockMoveServiceImpl implements ManufOrderOutgoingStockMoveService {

  protected AppSupplychainService appSupplychainService;
  protected StockMoveLineRepository stockMoveLineRepository;

  @Inject
  public ManufOrderOutgoingStockMoveServiceImpl(
      AppSupplychainService appSupplychainService,
      StockMoveLineRepository stockMoveLineRepository) {
    this.appSupplychainService = appSupplychainService;
    this.stockMoveLineRepository = stockMoveLineRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void setManufOrderOnOutgoingMove(ManufOrder manufOrder) {
    Objects.requireNonNull(manufOrder);
    if (!appSupplychainService.getAppSupplychain().getAutoFillDeliveryRealQty()
        && appSupplychainService.getAppSupplychain().getDeliveriesCopyFromManufOrder()
        && !ObjectUtils.isEmpty(manufOrder.getProducedStockMoveLineList())
        && !ObjectUtils.isEmpty(manufOrder.getSaleOrderSet())) {

      List<StockMoveLine> producedStockMoveLinesToDeliver =
          manufOrder.getProducedStockMoveLineList();

      String filter =
          "self.saleOrderLine != NULL"
              + " AND self.saleOrderLine.billOfMaterial = :manufOrderBOM"
              + " AND self.saleOrderLine = :saleOrderLine"
              + " AND self.stockMove != null"
              + " AND self.stockMove.statusSelect < :stockMoveStatusRealized"
              + " AND self.copiedManufOrder = null";

      List<StockMoveLine> deliveryStockMoveLinesToComplete =
          stockMoveLineRepository
              .all()
              .filter(filter)
              .bind("manufOrderBOM", manufOrder.getBillOfMaterial())
              .bind("saleOrderLine", manufOrder.getSaleOrderLine())
              .bind("stockMoveStatusRealized", StockMoveRepository.STATUS_REALIZED)
              .fetch();

      // If there is nothing to complete then we stop here
      if (ObjectUtils.isEmpty(deliveryStockMoveLinesToComplete)) {
        return;
      }

      StockMove stockMove = deliveryStockMoveLinesToComplete.get(0).getStockMove();

      for (StockMoveLine producedStockMoveLine : producedStockMoveLinesToDeliver) {
        StockMoveLine stockMoveLineToComplete =
            deliveryStockMoveLinesToComplete.stream()
                .filter(it -> it.getCopiedManufOrder() == null)
                .findAny()
                .orElse(null);

        if (stockMoveLineToComplete != null) {
          completeStockMoveLine(manufOrder, producedStockMoveLine, stockMoveLineToComplete);
        } else {
          StockMoveLine copyStockMoveLine =
              copyProducedStockMoveLine(stockMove, producedStockMoveLine);
          completeStockMoveLine(manufOrder, producedStockMoveLine, copyStockMoveLine);
        }
      }
    }
  }

  protected void completeStockMoveLine(
      ManufOrder manufOrder,
      StockMoveLine producedStockMoveLine,
      StockMoveLine stockMoveLineToComplete) {
    stockMoveLineToComplete.setRealQty(producedStockMoveLine.getRealQty());
    stockMoveLineToComplete.setTrackingNumber(producedStockMoveLine.getTrackingNumber());
    stockMoveLineToComplete.setNetMass(producedStockMoveLine.getNetMass());
    stockMoveLineToComplete.setTotalNetMass(producedStockMoveLine.getTotalNetMass());
    stockMoveLineToComplete.setCopiedManufOrder(manufOrder);
    stockMoveLineRepository.save(stockMoveLineToComplete);
  }

  protected StockMoveLine copyProducedStockMoveLine(
      StockMove stockMove, StockMoveLine producedStockMoveLine) {
    StockMoveLine copy = stockMoveLineRepository.copy(producedStockMoveLine, false);

    copy.setStockMove(stockMove);
    copy.setQty(BigDecimal.ZERO);
    copy.setSaleOrderLine(producedStockMoveLine.getSaleOrderLine());
    return copy;
  }
}
