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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.Pack;
import com.axelor.apps.sale.db.SaleOrder;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import wslite.json.JSONException;

public interface SaleOrderService {

  public String getFileName(SaleOrder saleOrder);

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

  /**
   * Convert PackLines of pack into SaleOrderLines.
   *
   * @param saleOrder
   * @throws AxelorException
   * @throws JSONException
   * @throws MalformedURLException
   */
  SaleOrder addPack(SaleOrder saleOrder, Pack pack, BigDecimal packQty)
      throws AxelorException, MalformedURLException, JSONException;

  /**
   * Blocks if the given sale order has line with a discount superior to the max authorized
   * discount.
   *
   * @param saleOrder a sale order
   * @throws AxelorException if the sale order is in anomaly
   */
  void checkUnauthorizedDiscounts(SaleOrder saleOrder) throws AxelorException;

  /**
   * To manage Complementary Product sale order lines.
   *
   * @param saleOrder
   * @throws AxelorException
   */
  public void manageComplementaryProductSOLines(SaleOrder saleOrder) throws AxelorException;

  SaleOrder separateInNewQuotation(
      SaleOrder saleOrder, ArrayList<LinkedHashMap<String, Object>> saleOrderLines)
      throws AxelorException;

  void checkPrintingSettings(SaleOrder saleOrder) throws AxelorException;

  boolean isIncotermRequired(SaleOrder saleOrder);
}
