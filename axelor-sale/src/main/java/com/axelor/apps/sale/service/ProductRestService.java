package com.axelor.apps.sale.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.rest.dto.PriceResponse;
import java.util.List;
import wslite.json.JSONException;

public interface ProductRestService {
  public List<PriceResponse> fetchProductPrice(Product product, Partner partner, Company company)
          throws JSONException, AxelorException;
}
