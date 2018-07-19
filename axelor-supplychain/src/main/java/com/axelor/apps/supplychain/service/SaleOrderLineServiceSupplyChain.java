/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.util.List;

public interface SaleOrderLineServiceSupplyChain extends SaleOrderLineService {
  /**
   * Compute undelivered quantity.
   *
   * @param saleOrderLine
   * @return
   */
  BigDecimal computeUndeliveredQty(SaleOrderLine saleOrderLine);

  /**
   * Get a list of supplier partner ids available for the product in the sale order line.
   *
   * @param saleOrderLine
   * @return the list of ids
   */
  List<Long> getSupplierPartnerList(SaleOrderLine saleOrderLine);

  /**
   * Update delivery state.
   *
   * @param saleOrderLine
   */
  void updateDeliveryState(SaleOrderLine saleOrderLine);

  /**
   * Update delivery states.
   *
   * @param saleOrderLineList
   */
  void updateDeliveryStates(List<SaleOrderLine> saleOrderLineList);

  /**
   * Generates a list of sub sale order lines from a pack product.
   *
   * @param product a product of type 'pack'
   * @param saleOrder the sale order containing a sale order line with the pack product
   * @return a list of sub sale order lines
   */
  public List<SaleOrderLine> createPackLines(Product product, SaleOrder saleOrder)
      throws AxelorException;
}
