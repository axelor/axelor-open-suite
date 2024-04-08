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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.stock.db.StockMove;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ManufOrderService {

  public static int DEFAULT_PRIORITY = 2;
  public static int DEFAULT_PRIORITY_INTERVAL = 10;
  public static boolean IS_TO_INVOICE = false;

  public interface ManufOrderOriginType {}

  public enum ManufOrderOriginTypeProduction implements ManufOrderOriginType {
    ORIGIN_TYPE_MRP,
    ORIGIN_TYPE_SALE_ORDER,
    ORIGIN_TYPE_OTHER;
  }

  public ManufOrder generateManufOrder(
      Product product,
      BigDecimal qtyRequested,
      int priority,
      boolean isToInvoice,
      BillOfMaterial billOfMaterial,
      LocalDateTime plannedStartDateT,
      LocalDateTime plannedEndDateT,
      ManufOrderOriginType manufOrderOriginType)
      throws AxelorException;

  public ManufOrder createManufOrder(
      Product product,
      BigDecimal qty,
      Unit unit,
      int priority,
      boolean isToInvoice,
      Company company,
      BillOfMaterial billOfMaterial,
      LocalDateTime plannedStartDateT,
      LocalDateTime plannedEndDateT)
      throws AxelorException;

  public String getManufOrderSeq(ManufOrder manufOrder) throws AxelorException;

  /**
   * Generate waste stock move.
   *
   * @param manufOrder
   * @return wasteStockMove
   */
  public StockMove generateWasteStockMove(ManufOrder manufOrder) throws AxelorException;

  /**
   * Update planned qty in {@link ManufOrder#toConsumeProdProductList} and {@link
   * ManufOrder#toProduceProdProductList} then update quantity in stock move lines to match the new
   * planned qty.
   *
   * @param manufOrder
   */
  void updatePlannedQty(ManufOrder manufOrder) throws AxelorException;

  /**
   * Update real qty in {@link ManufOrder#consumedStockMoveLineList} and {@link
   * ManufOrder#producedStockMoveLineList}
   *
   * @param manufOrder
   * @param qtyToUpdate
   */
  void updateRealQty(ManufOrder manufOrder, BigDecimal qtyToUpdate) throws AxelorException;

  public List<ManufOrder> generateAllSubManufOrder(List<Product> productList, ManufOrder manufOrder)
      throws AxelorException;

  /**
   * Create a barcode from {@link ManufOrder}'s sequence and it will get displayed in the report of
   * {@link ManufOrder} on the header of every page.
   *
   * @return
   */
  public void createBarcode(ManufOrder manufOrder);

  List<ManufOrder> getChildrenManufOrder(ManufOrder manufOrder);

  public BigDecimal computeProducibleQty(ManufOrder manufOrder) throws AxelorException;

  /**
   * Method that will update planned dates of manuf order. Unlike the other methods, this will not
   * reset planned dates of the operation orders of the manuf order. This method must be called when
   * changement has occured in operation orders.
   *
   * @param manufOrder
   */
  public void updatePlannedDates(ManufOrder manufOrder);

  void checkApplicableManufOrder(ManufOrder manufOrder) throws AxelorException;
}
