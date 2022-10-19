package com.axelor.apps.production.rest;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.rest.dto.ManufOrderProductResponse;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.util.List;

public interface ManufOrderProductRestService {

  String PRODUCT_TYPE_CONSUMED = "consumed";
  String PRODUCT_TYPE_PRODUCED = "produced";

  List<ManufOrderProductResponse> getConsumedProductList(ManufOrder manufOrder)
      throws AxelorException;

  List<ManufOrderProductResponse> getProducedProductList(ManufOrder manufOrder)
      throws AxelorException;

  StockMoveLine updateStockMoveLineQty(StockMoveLine stockMoveLine, BigDecimal qty)
      throws AxelorException;

  void addWasteProduct(ManufOrder manufOrder, ProdProduct wasteProduct);

  void updateProdProductQty(ProdProduct prodProduct, BigDecimal qty);

  StockMoveLine addManufOrderProduct(
      Product product,
      BigDecimal qty,
      TrackingNumber trackingNumber,
      ManufOrder manufOrder,
      String productType)
      throws AxelorException;
}
