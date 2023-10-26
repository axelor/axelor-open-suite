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
package com.axelor.apps.production.service.operationorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.MachineTool;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.OperationOrderDuration;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.stock.db.StockMoveLine;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OperationOrderService {

  public OperationOrder createOperationOrder(ManufOrder manufOrder, ProdProcessLine prodProcessLine)
      throws AxelorException;

  public OperationOrder createOperationOrder(
      ManufOrder manufOrder,
      int priority,
      WorkCenter workCenter,
      Machine machine,
      MachineTool machineTool,
      ProdProcessLine prodProcessLine)
      throws AxelorException;

  public String computeName(ManufOrder manufOrder, int priority, String operationName);

  /**
   * Generate {@link OperationOrder#toConsumeProdProductList} from the prod process line in param.
   *
   * @param operationOrder
   */
  void createToConsumeProdProductList(OperationOrder operationOrder) throws AxelorException;

  /**
   * Updates the diff prod product list.
   *
   * @param operationOrder
   * @return the updated operation order
   * @throws AxelorException
   */
  OperationOrder updateDiffProdProductList(OperationOrder operationOrder) throws AxelorException;

  List<Map<String, Object>> chargeByMachineHours(
      LocalDateTime fromDateTime, LocalDateTime toDateTime) throws AxelorException;

  List<Map<String, Object>> chargeByMachineDays(
      LocalDateTime fromDateTime, LocalDateTime toDateTime) throws AxelorException;

  /**
   * Compute the difference between the two lists for the given operation order.
   *
   * @param operationOrder
   * @param prodProductList
   * @param stockMoveLineList
   * @return
   * @throws AxelorException
   */
  List<ProdProduct> createDiffProdProductList(
      OperationOrder operationOrder,
      List<ProdProduct> prodProductList,
      List<StockMoveLine> stockMoveLineList)
      throws AxelorException;
  /**
   * Check the realized consumed stock move lines in operation order has not changed.
   *
   * @param operationOrder an operation order from context.
   * @param oldOperationOrder an operation order from database.
   * @throws AxelorException if the check fails.
   */
  void checkConsumedStockMoveLineList(
      OperationOrder operationOrder, OperationOrder oldOperationOrder) throws AxelorException;

  /**
   * On changing {@link OperationOrder#consumedStockMoveLineList}, we update {@link
   * OperationOrder#diffConsumeProdProductList}, and also the stock move.
   *
   * @param operationOrder
   */
  void updateConsumedStockMoveFromOperationOrder(OperationOrder operationOrder)
      throws AxelorException;

  void createBarcode(OperationOrder operationOrder);

  long computeEntireCycleDuration(OperationOrder operationOrder, BigDecimal qty)
      throws AxelorException;

  /**
   * Computes the duration of all the {@link OperationOrderDuration} of {@code operationOrder}
   *
   * @param operationOrder An operation order
   * @return Real duration of {@code operationOrder}
   */
  Duration computeRealDuration(OperationOrder operationOrder);

  LocalDateTime getNextOperationDate(OperationOrder operationOrder);

  LocalDateTime getLastOperationDate(OperationOrder operationOrder);

  long getDuration(OperationOrder operationOrder) throws AxelorException;

  List<OperationOrder> getSortedOperationOrderList(List<OperationOrder> operationOrders);

  List<OperationOrder> getReversedSortedOperationOrderList(List<OperationOrder> operationOrders);
}
