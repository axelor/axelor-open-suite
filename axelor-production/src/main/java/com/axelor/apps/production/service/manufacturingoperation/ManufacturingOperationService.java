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
package com.axelor.apps.production.service.manufacturingoperation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.MachineTool;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ManufacturingOperation;
import com.axelor.apps.production.db.ManufacturingOperationDuration;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.stock.db.StockMoveLine;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public interface ManufacturingOperationService {

  public ManufacturingOperation createManufacturingOperation(
      ManufOrder manufOrder, ProdProcessLine prodProcessLine) throws AxelorException;

  public ManufacturingOperation createManufacturingOperation(
      ManufOrder manufOrder,
      int priority,
      WorkCenter workCenter,
      Machine machine,
      MachineTool machineTool,
      ProdProcessLine prodProcessLine)
      throws AxelorException;

  public String computeName(ManufOrder manufOrder, int priority, String operationName);

  /**
   * Generate {@link ManufacturingOperation#toConsumeProdProductList} from the prod process line in
   * param.
   *
   * @param manufacturingOperation
   */
  void createToConsumeProdProductList(ManufacturingOperation manufacturingOperation)
      throws AxelorException;

  /**
   * Updates the diff prod product list.
   *
   * @param manufacturingOperation
   * @return the updated operation order
   * @throws AxelorException
   */
  ManufacturingOperation updateDiffProdProductList(ManufacturingOperation manufacturingOperation)
      throws AxelorException;

  /**
   * Compute the difference between the two lists for the given operation order.
   *
   * @param manufacturingOperation
   * @param prodProductList
   * @param stockMoveLineList
   * @return
   * @throws AxelorException
   */
  List<ProdProduct> createDiffProdProductList(
      ManufacturingOperation manufacturingOperation,
      List<ProdProduct> prodProductList,
      List<StockMoveLine> stockMoveLineList)
      throws AxelorException;
  /**
   * Check the realized consumed stock move lines in operation order has not changed.
   *
   * @param manufacturingOperation an operation order from context.
   * @param oldManufacturingOperation an operation order from database.
   * @throws AxelorException if the check fails.
   */
  void checkConsumedStockMoveLineList(
      ManufacturingOperation manufacturingOperation,
      ManufacturingOperation oldManufacturingOperation)
      throws AxelorException;

  /**
   * On changing {@link ManufacturingOperation#consumedStockMoveLineList}, we update {@link
   * ManufacturingOperation#diffConsumeProdProductList}, and also the stock move.
   *
   * @param manufacturingOperation
   */
  void updateConsumedStockMoveFromManufacturingOperation(
      ManufacturingOperation manufacturingOperation) throws AxelorException;

  void createBarcode(ManufacturingOperation manufacturingOperation);

  long computeEntireCycleDuration(ManufacturingOperation manufacturingOperation, BigDecimal qty)
      throws AxelorException;

  /**
   * Computes the duration of all the {@link ManufacturingOperationDuration} of {@code
   * manufacturingOperation}
   *
   * @param manufacturingOperation An operation order
   * @return Real duration of {@code manufacturingOperation}
   */
  Duration computeRealDuration(ManufacturingOperation manufacturingOperation);

  LocalDateTime getNextOperationDate(ManufacturingOperation manufacturingOperation);

  LocalDateTime getLastOperationDate(ManufacturingOperation manufacturingOperation);

  long getDuration(ManufacturingOperation manufacturingOperation) throws AxelorException;

  List<ManufacturingOperation> getSortedManufacturingOperationList(
      List<ManufacturingOperation> manufacturingOperations);

  List<ManufacturingOperation> getReversedSortedManufacturingOperationList(
      List<ManufacturingOperation> manufacturingOperations);
}
