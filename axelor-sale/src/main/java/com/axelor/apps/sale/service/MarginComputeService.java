package com.axelor.apps.sale.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.interfaces.MarginLine;
import java.math.BigDecimal;
import java.util.Map;

public interface MarginComputeService {

  Map<String, BigDecimal> getComputedMarginInfo(
      SaleOrder saleOrder, MarginLine marginLine, BigDecimal totalPrice) throws AxelorException;

  void computeSubMargin(SaleOrder saleOrder, MarginLine marginLine, BigDecimal totalPrice)
      throws AxelorException;
}
