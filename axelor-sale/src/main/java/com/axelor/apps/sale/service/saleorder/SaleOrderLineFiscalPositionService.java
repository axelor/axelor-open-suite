package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;

public interface SaleOrderLineFiscalPositionService {
  List<SaleOrderLine> updateLinesAfterFiscalPositionChange(SaleOrder saleOrder)
      throws AxelorException;
}
