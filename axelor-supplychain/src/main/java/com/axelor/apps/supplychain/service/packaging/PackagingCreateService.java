package com.axelor.apps.supplychain.service.packaging;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.supplychain.db.Packaging;

public interface PackagingCreateService {
  Packaging createPackaging(
      LogisticalForm logisticalForm, Packaging parentPackaging, Product packageUsed)
      throws AxelorException;

  void updatePackageUsed(Product packageUsed, Packaging packaging) throws AxelorException;
}
