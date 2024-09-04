package com.axelor.apps.sale.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.sale.rest.dto.ProductResponse;
import com.axelor.apps.sale.rest.dto.ProductResquest;
import java.util.List;
import wslite.json.JSONException;

public interface ProductRestService {
  List<ProductResponse> computeProductResponse(
      Company company, List<ProductResquest> unitProducts, Partner partner, Currency currency)
      throws AxelorException, JSONException;
}
