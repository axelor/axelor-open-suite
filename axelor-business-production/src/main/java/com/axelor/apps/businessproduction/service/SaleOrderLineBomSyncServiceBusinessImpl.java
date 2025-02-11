package com.axelor.apps.businessproduction.service;

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.service.SaleOrderLineBomSyncServiceImpl;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;

public class SaleOrderLineBomSyncServiceBusinessImpl extends SaleOrderLineBomSyncServiceImpl {

  @Override
  protected void removeBomLines(
      SaleOrderLine saleOrderLine, List<SaleOrderLine> subSaleOrderLineList) {
    BillOfMaterial billOfMaterial = saleOrderLine.getBillOfMaterial();
    if (billOfMaterial != null && billOfMaterial.getPersonalized()) {
      removeSolBomLine(subSaleOrderLineList, billOfMaterial);
      removeSolDetailsBomLine(saleOrderLine.getProjectSaleOrderLineDetailsList(), billOfMaterial);
    }
  }
}
