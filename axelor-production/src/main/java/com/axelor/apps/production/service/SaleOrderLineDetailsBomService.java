package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrder;
import java.util.List;

public interface SaleOrderLineDetailsBomService {
  List<SaleOrderLineDetails> createSaleOrderLineDetailsFromBom(
      BillOfMaterial billOfMaterial, SaleOrder saleOrder) throws AxelorException;
}
