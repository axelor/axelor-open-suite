package com.axelor.apps.sale.service.saleorderline.product;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineOnProductChangeService {

  /**
   * Fill all sale order line fields with default values computed from the product. <br>
   * <br>
   * This method fires the event {@link
   * com.axelor.apps.sale.service.event.SaleOrderLineProductOnChange} which allows all observer
   * classes to update the given sale order line.
   *
   * @param saleOrderLine a sale order line with a product
   * @return a map containing all updated fields with their new values.
   */
  Map<String, Object> computeLineFromProduct(SaleOrderLine saleOrderLine) throws AxelorException;

  /**
   * Fill all sale order line fields with default values computed from the product. <br>
   * <br>
   * This method fires the event {@link
   * com.axelor.apps.sale.service.event.SaleOrderLineProductOnChange} which allows all observer
   * classes to update the given sale order line.
   *
   * @param saleOrder the parent sale order
   * @param saleOrderLine a sale order line with a product
   * @return a map containing all updated fields with their new values.
   */
  Map<String, Object> computeLineFromProduct(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException;
}
