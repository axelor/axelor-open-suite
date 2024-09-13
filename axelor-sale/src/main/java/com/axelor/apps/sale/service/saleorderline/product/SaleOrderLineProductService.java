package com.axelor.apps.sale.service.saleorderline.product;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineProductService {

  /**
   * Update all fields of the sale order line from the product.
   *
   * @param saleOrderLine
   * @param saleOrder
   */
  Map<String, Object> computeProductInformation(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  Map<String, Object> resetProductInformation(SaleOrderLine line);

  /**
   * Fill price for standard line.
   *
   * @param saleOrderLine
   * @param saleOrder
   * @throws AxelorException
   */
  Map<String, Object> fillPrice(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  Map<String, Object> fillTaxInformation(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  Unit getSaleUnit(Product product);
}
