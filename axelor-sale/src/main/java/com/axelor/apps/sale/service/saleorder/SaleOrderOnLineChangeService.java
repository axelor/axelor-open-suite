package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;

public interface SaleOrderOnLineChangeService {

  /**
   * Handle the creation / updating of complementary products. Called onChange of saleOrderLineList.
   *
   * @param saleOrder
   * @return
   */
  public List<SaleOrderLine> handleComplementaryProducts(SaleOrder saleOrder)
      throws AxelorException;

  /**
   * To update product quantity with pack header quantity.
   *
   * @param saleOrder
   * @return {@link SaleOrder}
   * @throws AxelorException
   */
  public SaleOrder updateProductQtyWithPackHeaderQty(SaleOrder saleOrder) throws AxelorException;

  public void onLineChange(SaleOrder saleOrder) throws AxelorException;
}
