package com.axelor.apps.production.rest;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.rest.dto.ManufOrderProductResponse;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.util.List;

public interface ManufOrderProductRestService {

  List<ManufOrderProductResponse> getConsumedProductList(ManufOrder manufOrder)
      throws AxelorException;

  List<ManufOrderProductResponse> getProducedProductList(ManufOrder manufOrder)
      throws AxelorException;

  StockMoveLine updateStockMoveLineQty(StockMoveLine stockMoveLine, BigDecimal qty)
      throws AxelorException;

  void addWasteProduct(ManufOrder manufOrder, ProdProduct wasteProduct);

  void updateProdProductQty(ProdProduct prodProduct, BigDecimal qty);
}
