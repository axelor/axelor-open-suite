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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineProductService {

  /**
   * Update all fields in the sale module of the sale order line from the product. <br>
   * <br>
   * Warning: you probably want to call {@link
   * SaleOrderLineOnProductChangeService#computeLineFromProduct(SaleOrderLine)} instead, as this
   * will compute all fields from all modules and compute the line.
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
