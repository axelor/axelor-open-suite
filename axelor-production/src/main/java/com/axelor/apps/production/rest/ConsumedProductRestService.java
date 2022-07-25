package com.axelor.apps.production.rest;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.rest.dto.ConsumedProductResponse;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface ConsumedProductRestService {

  StockMoveLine getStockMoveLine(ManufOrder manufOrder, Product product) throws AxelorException;

  List<ConsumedProductResponse> getConsumedProductList(ManufOrder manufOrder)
      throws AxelorException;
}
