package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.saleorder.model.SaleOrderMergeControlResponse;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface SaleOrderMergeControlService {

  /**
   * This method controls fields values that need to be the same in order to merge saleOrder. For
   * example tax number or company. This method does not merge sale orders.
   *
   * @param saleOrderList
   * @return
   * @throws AxelorException
   */
  public SaleOrderMergeControlResponse controlFieldsBeforeMerge(List<SaleOrder> saleOrderList)
      throws AxelorException;
}
