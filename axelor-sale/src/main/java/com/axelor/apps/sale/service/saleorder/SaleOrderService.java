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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;

public interface SaleOrderService {

  public String getFileName(SaleOrder saleOrder);

  public SaleOrder computeEndOfValidityDate(SaleOrder saleOrder);

  @Deprecated
  public String getReportLink(
      SaleOrder saleOrder, String name, String language, boolean proforma, String format)
      throws AxelorException;

  /**
   * Fill {@link SaleOrder#mainInvoicingAddressStr} and {@link SaleOrder#deliveryAddressStr}
   *
   * @param saleOrder
   */
  public void computeAddressStr(SaleOrder saleOrder);

  /**
   * Enable edit order.
   *
   * @param saleOrder
   * @throws AxelorException
   */
  boolean enableEditOrder(SaleOrder saleOrder) throws AxelorException;

  /**
   * Check modified confirmed order before saving it.
   *
   * @param saleOrder
   * @param saleOrderView
   * @throws AxelorException
   */
  void checkModifiedConfirmedOrder(SaleOrder saleOrder, SaleOrder saleOrderView)
      throws AxelorException;

  /**
   * Validate changes.
   *
   * @param saleOrder
   * @throws AxelorException
   */
  void validateChanges(SaleOrder saleOrder) throws AxelorException;

  /**
   * Sort detail lines by sequence.
   *
   * @param saleOrder
   */
  void sortSaleOrderLineList(SaleOrder saleOrder);
}
