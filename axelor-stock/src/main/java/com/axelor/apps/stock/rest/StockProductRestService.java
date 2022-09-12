package com.axelor.apps.stock.rest;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.rest.dto.StockProductVariantAttributeResponse;
import com.axelor.exception.AxelorException;
import java.util.List;
import javax.ws.rs.core.Response;

public interface StockProductRestService {

  Response getProductIndicators(Product product, Company company, StockLocation stockLocation)
      throws AxelorException;

  List<StockProductVariantAttributeResponse> fetchAttributes(Product product)
      throws AxelorException;
}
