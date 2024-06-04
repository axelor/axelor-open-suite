package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.stock.db.StockMoveLine;
import java.util.Objects;

public class ManufOrderTrackingNumberServiceImpl implements ManufOrderTrackingNumberService {
  @Override
  public void setParentTrackingNumbers(ManufOrder manufOrder) {
    Objects.requireNonNull(manufOrder);

    if (manufOrder.getProducedStockMoveLineList() != null
        && manufOrder.getConsumedStockMoveLineList() != null) {
      manufOrder.getProducedStockMoveLineList().stream()
          .map(StockMoveLine::getTrackingNumber)
          .filter(Objects::nonNull)
          .forEach(
              trackingNumber -> {
                manufOrder.getConsumedStockMoveLineList().stream()
                    .map(StockMoveLine::getTrackingNumber)
                    .filter(Objects::nonNull)
                    .forEach(trackingNumber::addParentTrackingNumberSetItem);
              });
    }
  }
}
