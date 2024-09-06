package com.axelor.apps.production.service;

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;

public interface SaleOrderLineBomService {

  List<SaleOrderLine> createSaleOrderLinesFromBom(BillOfMaterial billOfMaterial);
}
