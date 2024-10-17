package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;

public interface SaleOrderComplementaryProductService {

  /**
   * Handle the creation / updating of complementary products. Called onChange of saleOrderLineList.
   *
   * @param saleOrder
   * @return
   */
  List<SaleOrderLine> handleComplementaryProducts(SaleOrder saleOrder) throws AxelorException;
}
