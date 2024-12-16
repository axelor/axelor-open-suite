package com.axelor.apps.production.service;

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import java.util.List;

public interface SaleOrderBomRemoveLineService {
  void removeBomLines(
      List<BillOfMaterialLine> bomLineList, BillOfMaterial bom, int productTypeSelect);
}
