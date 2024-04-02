package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;

public interface ManufOrderResidualProductService {
  boolean hasResidualProduct(ManufOrder manufOrder);

  boolean isResidualProduct(ProdProduct prodProduct, ManufOrder manufOrder);
}
