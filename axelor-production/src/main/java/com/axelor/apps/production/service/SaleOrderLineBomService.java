package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;

public interface SaleOrderLineBomService {

  List<SaleOrderLine> createSaleOrderLinesFromBom(
      BillOfMaterial billOfMaterial, SaleOrder saleOrder) throws AxelorException;

  BillOfMaterial customizeBomOf(SaleOrderLine saleOrderLine) throws AxelorException;

  void updateWithBillOfMaterial(SaleOrderLine saleOrderLine) throws AxelorException;

  boolean isUpdated(SaleOrderLine saleOrderLine);
}
