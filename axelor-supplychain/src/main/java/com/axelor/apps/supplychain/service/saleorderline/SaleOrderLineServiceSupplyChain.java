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
package com.axelor.apps.supplychain.service.saleorderline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SaleOrderLineServiceSupplyChain {

  BigDecimal getAvailableStock(SaleOrder saleOrder, SaleOrderLine saleOrderLine);

  BigDecimal getAllocatedStock(SaleOrder saleOrder, SaleOrderLine saleOrderLine);

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
   * Create a query to find sale order line of a product of a specific/all company and a
   * specific/all stock location
   *
   * @param productId
   * @param companyId
   * @param stockLocationId
   * @return the query.
   */
  String getSaleOrderLineListForAProduct(Long productId, Long companyId, Long stockLocationId);

  /**
   * check qty when modifying saleOrderLine which is invoiced or delivered
   *
   * @param saleOrderLine
   */
  BigDecimal checkInvoicedOrDeliveredOrderQty(SaleOrderLine saleOrderLine, SaleOrder saleOrder);

  /**
   * Update stock move lines linked to this sale order line by using estimated delivery date as date
   * used for reservation. Do nothing if the configuration is not set to use estimated delivery
   * date.
   *
   * @param saleOrderLine a sale order line managed by hibernate
   */
  void updateStockMoveReservationDateTime(SaleOrderLine saleOrderLine) throws AxelorException;

  Map<String, Object> updateRequestedReservedQty(SaleOrderLine saleOrderLine);
}
