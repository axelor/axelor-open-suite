/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

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

  public void createToConsumeProdProductList(ManufOrder manufOrder);

  /**
   * Compute the quantity on generated prod product line. If the quantity of the bill of materials
   * is equal to the quantity of manuf order then the prod product line will have the same quantity
   * as configured line.
   *
   * @param bomQty quantity of the bill of materials.
   * @param manufOrderQty quantity configured of the manuf order.
   * @param lineQty quantity of the line.
   * @return the quantity for the prod product line.
   */
  BigDecimal computeToConsumeProdProductLineQuantity(
      BigDecimal bomQty, BigDecimal manufOrderQty, BigDecimal lineQty);

  public void createToProduceProdProductList(ManufOrder manufOrder);

  public ManufOrder createManufOrder(
      Product product,
      BigDecimal qty,
      int priority,
      boolean isToInvoice,
      Company company,
      BillOfMaterial billOfMaterial,
      LocalDateTime plannedStartDateT,
      LocalDateTime plannedEndDateT)
      throws AxelorException;

  public void preFillOperations(ManufOrder manufOrder) throws AxelorException;

  public void updateOperationsName(ManufOrder manufOrder);

  public String getManufOrderSeq(ManufOrder manufOrder) throws AxelorException;

  public boolean isManagedConsumedProduct(BillOfMaterial billOfMaterial);

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

  /**
   * Updates the diff prod product list.
   *
   * @param manufOrder
   * @return the updated manufOrder
   * @throws AxelorException
   */
  ManufOrder updateDiffProdProductList(ManufOrder manufOrder) throws AxelorException;

  /**
   * Compute the difference between the two lists for the given manuf order.
   *
   * @param manufOrder
   * @param prodProductList
   * @param stockMoveLineList
   * @return a list of ProdProduct
   * @throws AxelorException
   */
  List<ProdProduct> createDiffProdProductList(
      ManufOrder manufOrder,
      List<ProdProduct> prodProductList,
      List<StockMoveLine> stockMoveLineList)
      throws AxelorException;

  /**
   * Compute the difference between the two lists.
   *
   * @param prodProductList
   * @param stockMoveLineList
   * @return a list of ProdProduct
   * @throws AxelorException
   */
  List<ProdProduct> createDiffProdProductList(
      List<ProdProduct> prodProductList, List<StockMoveLine> stockMoveLineList)
      throws AxelorException;

  /**
   * On changing {@link ManufOrder#consumedStockMoveLineList}, we also update the stock move.
   *
   * @param manufOrder
   */
  void updateConsumedStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException;

  StockMove getConsumedStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException;

  /**
   * On changing {@link ManufOrder#producedStockMoveLineList}, we also update the stock move.
   *
   * @param manufOrder
   * @throws AxelorException
   */
  void updateProducedStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException;

  StockMove getProducedStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException;

  /**
   * Check the realized consumed stock move lines in manuf order has not changed.
   *
   * @param manufOrder a manuf order from context.
   * @param oldManufOrder a manuf order from database.
   * @throws AxelorException if the check fails.
   */
  void checkConsumedStockMoveLineList(ManufOrder manufOrder, ManufOrder oldManufOrder)
      throws AxelorException;

  /**
   * Check the realized produced stock move lines in manuf order has not changed.
   *
   * @param manufOrder a manuf order from context.
   * @param oldManufOrder a manuf order from database.
   * @throws AxelorException if the check fails.
   */
  void checkProducedStockMoveLineList(ManufOrder manufOrder, ManufOrder oldManufOrder)
      throws AxelorException;

  /**
   * Check between a new and an old stock move line list whether a realized stock move line has been
   * deleted.
   *
   * @param stockMoveLineList a stock move line list from view context.
   * @param oldStockMoveLineList a stock move line list from database.
   * @throws AxelorException if the check fails.
   */
  void checkRealizedStockMoveLineList(
      List<StockMoveLine> stockMoveLineList, List<StockMoveLine> oldStockMoveLineList)
      throws AxelorException;

  /**
   * Compute {@link ManufOrder#diffConsumeProdProductList}, then add and remove lines to the stock
   * move to match the stock move line list. The list can be from manuf order or operation order.
   *
   * @param stockMoveLineList
   * @param stockMove
   * @throws AxelorException
   */
  void updateStockMoveFromManufOrder(List<StockMoveLine> stockMoveLineList, StockMove stockMove)
      throws AxelorException;

  /**
   * Create a query to find product's consume and missing qty of a specific/all company and a
   * specific/all stock location in a Manuf Order
   *
   * @param productId, companyId and stockLocationId
   * @return the query.
   */
  public String getConsumeAndMissingQtyForAProduct(
      Long productId, Long companyId, Long stockLocationId);

  /**
   * Create a query to find product's building qty of a specific/all company and a specific/all
   * stock location in a Manuf Order
   *
   * @param productId, companyId and stockLocationId
   * @return the query.
   */
  public String getBuildingQtyForAProduct(Long productId, Long companyId, Long stockLocationId);

  public List<ManufOrder> generateAllSubManufOrder(List<Product> productList, ManufOrder manufOrder)
      throws AxelorException;

  public List<Long> planSelectedOrdersAndDiscardOthers(List<Map<String, Object>> manufOrders)
      throws AxelorException;

  public List<Pair<BillOfMaterial, BigDecimal>> getToConsumeSubBomList(
      BillOfMaterial bom, ManufOrder mo, List<Product> productList) throws AxelorException;

  /**
   * Merge different manufacturing orders into a single one.
   *
   * @param ids List of ids of manufacturing orders to merge
   * @throws AxelorException
   */
  public void merge(List<Long> ids) throws AxelorException;

  /**
   * Check if the manufacturing orders can be merged.
   *
   * @param ids List of ids of manufacturing orders to merge
   */
  public boolean canMerge(List<Long> ids);

  /**
   * Create a barcode from {@link ManufOrder}'s sequence and it will get displayed in the report of
   * {@link ManufOrder} on the header of every page.
   *
   * @return
   */
  public void createBarcode(ManufOrder manufOrder);

  List<ManufOrder> getChildrenManufOrder(ManufOrder manufOrder);

  public BigDecimal computeProducibleQty(ManufOrder manufOrder) throws AxelorException;
}
