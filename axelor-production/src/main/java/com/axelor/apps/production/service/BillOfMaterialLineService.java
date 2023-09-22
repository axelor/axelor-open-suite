package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import java.math.BigDecimal;

public interface BillOfMaterialLineService {

  BillOfMaterialLine createBillOfMaterialLine(
      Product product,
      BillOfMaterial billOfMaterial,
      BigDecimal qty,
      Unit unit,
      Integer priority,
      boolean hasNoManageStock);

  BillOfMaterialLine createFromRawMaterial(
      long productId, int priority, BillOfMaterial billOfMaterial) throws AxelorException;

  BillOfMaterialLine createFromBillOfMaterial(BillOfMaterial billOfMaterial);

  void fillBom(BillOfMaterialLine billOfMaterialLine, Company company) throws AxelorException;

  void fillHasNoManageStock(BillOfMaterialLine billOfMaterialLine);

  void fillUnit(BillOfMaterialLine billOfMaterialLine);
}
