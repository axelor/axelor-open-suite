package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import java.math.BigDecimal;

public interface BillOfMaterialLineService {

  BillOfMaterialLine newBillOfMaterial(
      Product product,
      BillOfMaterial billOfMaterial,
      BigDecimal qty,
      Integer priority,
      boolean hasNoManageStock);

  BillOfMaterialLine createFromRawMaterial(long productId, int priority, Company company) throws AxelorException;
}
