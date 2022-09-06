package com.axelor.apps.supplychain.service;

import com.axelor.apps.supplychain.db.Mrp;
import java.util.List;

public interface MrpFilterSaleOrderLineService {

  /**
   * A method that returns all the saleOrderLines that should be selectable in the MRP. That is, all
   * the sale order lines that comply with the existing mrp line types.
   *
   * @return a list of ids of the sale order lines
   */
  List<Long> getSaleOrderLinesComplyingToMrpLineTypes(Mrp mrp);
}
