package com.axelor.apps.sale.service.saleorderline.product;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.ComplementaryProduct;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;
import java.util.Map;

public interface SaleOrderLineComplementaryProductService {
  /**
   * To manage Complementary Product sale order line.
   *
   * @param complementaryProduct
   * @param saleOrder
   * @param saleOrderLine
   * @return New complementary sales order lines
   * @throws AxelorException
   */
  List<SaleOrderLine> manageComplementaryProductSaleOrderLine(
      ComplementaryProduct complementaryProduct, SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException;

  /**
   * Fill the complementaryProductList of the saleOrderLine from the possible complementary products
   * of the product of the line
   *
   * @param saleOrderLine
   */
  Map<String, Object> fillComplementaryProductList(SaleOrderLine saleOrderLine);

  Map<String, Object> setIsComplementaryProductsUnhandledYet(SaleOrderLine saleOrderLine);
}
