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
import java.util.List;
import java.util.Map;

public interface SaleOrderLineComputeService {

  /**
   * Compute totals from a sale order line
   *
   * @param saleOrder
   * @param saleOrderLine
   * @return
   * @throws AxelorException
   */
  Map<String, Object> computeValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException;

  /**
   * Compute and return the discounted price of a sale order line.
   *
   * @param saleOrderLine the sale order line.
   * @param inAti whether or not the sale order line (and thus the discounted price) includes taxes.
   * @return the discounted price of the line, including taxes if inAti is true.
   */
  BigDecimal computeDiscount(SaleOrderLine saleOrderLine, Boolean inAti);

  BigDecimal getAmountInCompanyCurrency(BigDecimal exTaxTotal, SaleOrder saleOrder)
      throws AxelorException;

  /**
   * Update product qty.
   *
   * @param saleOrderLine
   * @param saleOrder
   * @param oldQty
   * @param newQty
   * @return {@link SaleOrderLine}}
   * @throws AxelorException
   */
  Map<String, Object> updateProductQty(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder, BigDecimal oldQty, BigDecimal newQty)
      throws AxelorException;

  void computeLevels(List<SaleOrderLine> saleOrderLineList, String parentLevel);
}
