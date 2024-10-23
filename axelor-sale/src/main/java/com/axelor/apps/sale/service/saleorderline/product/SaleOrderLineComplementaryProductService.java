/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
