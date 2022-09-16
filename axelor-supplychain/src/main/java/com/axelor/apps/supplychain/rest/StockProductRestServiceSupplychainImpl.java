package com.axelor.apps.supplychain.rest;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.rest.StockProductRestServiceImpl;
import com.axelor.apps.supplychain.rest.dto.SupplychainStockProductResponse;
import com.axelor.apps.supplychain.service.ProductStockLocationService;
import com.axelor.apps.tool.api.ResponseConstructor;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import java.util.Map;
import javax.ws.rs.core.Response;

public class StockProductRestServiceSupplychainImpl extends StockProductRestServiceImpl {

  /*
   * OVERRIDING
   * Origin :  StockProductRestController in axelor-stock module
   * Add : stock indicators of supplychain (sale / purchase / production)
   */
  @Override
  public Response getProductIndicators(
      Product product, Company company, StockLocation stockLocation) throws AxelorException {
    Map<String, Object> stockIndicators;
    if (company == null) {
      stockIndicators =
          Beans.get(ProductStockLocationService.class).computeIndicators(product.getId(), 0L, 0L);
    } else if (stockLocation == null) {
      stockIndicators =
          Beans.get(ProductStockLocationService.class)
              .computeIndicators(product.getId(), company.getId(), 0L);
    } else {
      stockIndicators =
          Beans.get(ProductStockLocationService.class)
              .computeIndicators(product.getId(), company.getId(), stockLocation.getId());
    }

    return ResponseConstructor.build(
        Response.Status.OK,
        "Request completed",
        new SupplychainStockProductResponse(product, stockIndicators));
  }
}
