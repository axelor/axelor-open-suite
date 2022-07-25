package com.axelor.apps.production.rest;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.rest.dto.ConsumedProductResponse;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.supplychain.service.ProductStockLocationService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConsumedProductRestServiceImpl implements ConsumedProductRestService {

  protected ProductStockLocationService productStockLocationService;

  @Inject
  public ConsumedProductRestServiceImpl(ProductStockLocationService productStockLocationService) {
    this.productStockLocationService = productStockLocationService;
  }

  public StockMoveLine getStockMoveLine(ManufOrder manufOrder, Product product)
      throws AxelorException {

    for (StockMoveLine stockMoveLine : manufOrder.getConsumedStockMoveLineList()) {
      if (Objects.equals(stockMoveLine.getProduct().getId(), product.getId())) {
        return stockMoveLine;
      }
    }

    return null;
  }

  public List<ConsumedProductResponse> getConsumedProductList(ManufOrder manufOrder)
      throws AxelorException {
    List<ConsumedProductResponse> result = new ArrayList<>();

    List<ProdProduct> prodProducts = manufOrder.getToConsumeProdProductList();

    for (ProdProduct prodProduct : prodProducts) {
      Product product = prodProduct.getProduct();
      StockMoveLine stockMoveLine = getStockMoveLine(manufOrder, product);
      Map<String, Object> mapIndicators =
          productStockLocationService.computeIndicators(
              product.getId(), manufOrder.getCompany().getId(), 0L);

      BigDecimal availableQty = (BigDecimal) mapIndicators.get("$availableQty");

      ConsumedProductResponse consumedProduct =
          new ConsumedProductResponse(
              product,
              prodProduct.getQty(),
              stockMoveLine.getQty(),
              BigDecimal.ZERO.max(prodProduct.getQty().subtract(availableQty)),
              availableQty);
      result.add(consumedProduct);
    }

    return result;
  }
}
