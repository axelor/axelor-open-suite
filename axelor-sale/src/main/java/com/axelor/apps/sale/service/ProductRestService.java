package com.axelor.apps.sale.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.rest.dto.ProductResponse;
import wslite.json.JSONException;

public interface ProductRestService {
  ProductResponse computeProductResponse(Company company, Product product, Partner partner)
      throws AxelorException, JSONException;
}
