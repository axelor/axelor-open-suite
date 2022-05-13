package com.axelor.apps.stock.rest;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.rest.dto.StockProductResponse;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.tool.api.ResponseConstructor;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;

import javax.ws.rs.core.Response;
import java.util.Map;

public class StockProductRestServiceImpl implements StockProductRestService{
    @Override
    public Response getProductIndicators(
            Product product, Company company, StockLocation stockLocation) throws AxelorException {
        Map<String, Object> stockIndicators;
        if (company == null) {
            stockIndicators =
                    Beans.get(StockLocationService.class).getStockIndicators(product.getId(), 0L, 0L);
        } else if (stockLocation == null) {
            stockIndicators =
                    Beans.get(StockLocationService.class)
                            .getStockIndicators(product.getId(), company.getId(), 0L);
        } else {
            stockIndicators =
                    Beans.get(StockLocationService.class)
                            .getStockIndicators(product.getId(), company.getId(), stockLocation.getId());
        }

        return ResponseConstructor.build(
                Response.Status.OK,
                "Request completed",
                new StockProductResponse(product, stockIndicators));
    }
}
