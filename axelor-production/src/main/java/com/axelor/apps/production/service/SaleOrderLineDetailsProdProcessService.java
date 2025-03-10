package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;

public interface SaleOrderLineDetailsProdProcessService {

  List<SaleOrderLineDetails> createSaleOrderLineDetailsFromProdProcess(
      ProdProcess prodProcess, SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException;
}
