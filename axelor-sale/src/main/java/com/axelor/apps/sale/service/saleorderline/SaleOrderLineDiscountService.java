/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;
import java.util.Map;

public interface SaleOrderLineDiscountService {

  Map<String, Object> getDiscount(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  Map<String, Object> getDiscountsFromPriceLists(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, BigDecimal price);

  /**
   * Finds max discount from product category and his parents, and returns it.
   *
   * @param saleOrder a sale order (from context or sale order line)
   * @param saleOrderLine a sale order line
   * @return The maximal discount or null if the value is not needed
   */
  BigDecimal computeMaxDiscount(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException;

  /**
   * Compares sale order line discount with given max discount. Manages the two cases of amount
   * percent and amount fixed.
   *
   * @param saleOrderLine a sale order line
   * @param maxDiscount a max discount
   * @return whether the discount is greather than the one authorized
   */
  boolean isSaleOrderLineDiscountGreaterThanMaxDiscount(
      SaleOrderLine saleOrderLine, BigDecimal maxDiscount);

  Map<String, Object> fillDiscount(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, BigDecimal price);

  BigDecimal getDiscountedPrice(SaleOrderLine saleOrderLine, SaleOrder saleOrder, BigDecimal price);
}
