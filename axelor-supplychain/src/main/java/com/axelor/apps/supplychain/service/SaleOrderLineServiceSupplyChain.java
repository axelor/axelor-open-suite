/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
  BigDecimal checkInvoicedOrDeliveredOrderQty(SaleOrderLine saleOrderLine);

  /**
   * Method used for the invoicing wizard. The computation uses the invoicing quantity, but also
   * quantity from invoices that are not still ventilated. Also manage the case of refund invoice.
   *
   * @param saleOrderLine a sale order line with a sale order.
   * @return the quantity remaining to invoice
   */
  BigDecimal computeInvoicedQty(SaleOrderLine saleOrderLine) throws AxelorException;
}
