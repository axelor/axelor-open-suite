package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;
import java.util.Set;

public interface SaleOrderLinePriceService {

  /**
   * Reset price and inTaxPrice (only if the line.enableFreezeField is disabled) of the
   * saleOrderLine
   *
   * @param line
   */
  void resetPrice(SaleOrderLine line);

  BigDecimal getExTaxUnitPrice(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Set<TaxLine> taxLineSet)
      throws AxelorException;

  BigDecimal getInTaxUnitPrice(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Set<TaxLine> taxLineSet)
      throws AxelorException;

  BigDecimal getCompanyCostPrice(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException;
}
