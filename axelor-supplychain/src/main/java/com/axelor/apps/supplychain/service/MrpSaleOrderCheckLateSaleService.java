package com.axelor.apps.supplychain.service;

import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.db.MrpLineType;

public interface MrpSaleOrderCheckLateSaleService {

  /**
   * Determine if the saleOrderLine should generate a SaleOrderMrpLine with regard to the late sales
   * parameter of the mrpLineType
   *
   * @param saleOrderLine
   * @param mrpLineType
   * @return a boolean indicating if a line should be created
   */
  boolean checkLateSalesParameter(SaleOrderLine saleOrderLine, MrpLineType mrpLineType);
}
