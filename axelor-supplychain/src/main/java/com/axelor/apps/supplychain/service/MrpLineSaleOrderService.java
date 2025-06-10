package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.db.MrpLineType;
import java.math.BigDecimal;

public interface MrpLineSaleOrderService {

  BigDecimal getSoMrpLineQty(SaleOrderLine saleOrderLine, Unit unit, MrpLineType mrpLineType)
      throws AxelorException;
}
