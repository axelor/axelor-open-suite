package com.axelor.apps.supplierportal.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.supplierportal.db.ProductSupplier;
import com.axelor.exception.AxelorException;

public interface ProductSupplierService {
  public Product addOnCatalog(ProductSupplier productSupplier) throws AxelorException;
}
