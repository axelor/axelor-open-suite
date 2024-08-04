package com.axelor.apps.sale.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import wslite.json.JSONException;

public interface ProductRestService {
    public Product fetchProductPrice(Product product, Partner partner, Company company) throws JSONException;
}
