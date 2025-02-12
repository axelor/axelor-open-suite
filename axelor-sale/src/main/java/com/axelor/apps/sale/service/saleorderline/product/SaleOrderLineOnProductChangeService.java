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
