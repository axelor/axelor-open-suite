package com.axelor.apps.production.rest;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.rest.dto.ConsumedProductResponse;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.util.List;

public interface ManufOrderProductRestService {

  List<ConsumedProductResponse> getConsumedProductList(ManufOrder manufOrder)
      throws AxelorException;

  StockMoveLine updateStockMoveLineQty(StockMoveLine stockMoveLine, BigDecimal qty)
      throws AxelorException;
}
