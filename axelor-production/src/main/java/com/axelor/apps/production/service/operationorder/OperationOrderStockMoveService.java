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
package com.axelor.apps.production.service.operationorder;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.service.config.StockConfigProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class OperationOrderStockMoveService {

  protected StockMoveService stockMoveService;
  protected StockMoveLineService stockMoveLineService;
  protected StockLocationRepository stockLocationRepo;

  @Inject
  public OperationOrderStockMoveService(
      StockMoveService stockMoveService,
      StockMoveLineService stockMoveLineService,
      StockLocationRepository stockLocationRepo) {
    this.stockMoveService = stockMoveService;
    this.stockMoveLineService = stockMoveLineService;
    this.stockLocationRepo = stockLocationRepo;
  }

  public void createToConsumeStockMove(OperationOrder operationOrder) throws AxelorException {

    Company company = operationOrder.getManufOrder().getCompany();

    if (operationOrder.getToConsumeProdProductList() != null && company != null) {

      StockMove stockMove = this._createToConsumeStockMove(operationOrder, company);
      stockMove.setOriginId(operationOrder.getId());
      stockMove.setOriginTypeSelect(StockMoveRepository.ORIGIN_OPERATION_ORDER);
      stockMove.setOrigin(operationOrder.getOperationName());

      for (ProdProduct prodProduct : operationOrder.getToConsumeProdProductList()) {

        StockMoveLine stockMoveLine = this._createStockMoveLine(prodProduct, stockMove);
      }

      if (stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()) {
        stockMoveService.plan(stockMove);
        operationOrder.addInStockMoveListItem(stockMove);
      }

      // fill here the consumed stock move line list item to manage the
      // case where we had to split tracked stock move lines
      if (stockMove.getStockMoveLineList() != null) {
        for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
          operationOrder.addConsumedStockMoveLineListItem(stockMoveLine);
        }
      }
    }
  }

  protected StockMove _createToConsumeStockMove(OperationOrder operationOrder, Company company)
      throws AxelorException {

    StockConfigProductionService stockConfigService = Beans.get(StockConfigProductionService.class);
    StockConfig stockConfig = stockConfigService.getStockConfig(company);
    StockLocation virtualStockLocation =
        stockConfigService.getProductionVirtualStockLocation(stockConfig);

    StockLocation fromStockLocation;

    ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();
    if (operationOrder.getManufOrder().getIsConsProOnOperation()
        && prodProcessLine != null
        && prodProcessLine.getStockLocation() != null) {
      fromStockLocation = prodProcessLine.getStockLocation();
    } else if (!operationOrder.getManufOrder().getIsConsProOnOperation()
        && prodProcessLine != null
        && prodProcessLine.getProdProcess() != null
        && prodProcessLine.getProdProcess().getStockLocation() != null) {
      fromStockLocation = prodProcessLine.getProdProcess().getStockLocation();
    } else {
      fromStockLocation = stockConfigService.getComponentDefaultStockLocation(stockConfig);
    }

    return stockMoveService.createStockMove(
        null,
        null,
        company,
        fromStockLocation,
        virtualStockLocation,
        null,
        operationOrder.getPlannedStartDateT().toLocalDate(),
        null,
        StockMoveRepository.TYPE_INTERNAL);
  }

  protected StockMoveLine _createStockMoveLine(ProdProduct prodProduct, StockMove stockMove)
      throws AxelorException {

    return stockMoveLineService.createStockMoveLine(
        prodProduct.getProduct(),
        prodProduct.getProduct().getName(),
        prodProduct.getProduct().getDescription(),
        prodProduct.getQty(),
        prodProduct.getProduct().getCostPrice(),
        prodProduct.getProduct().getCostPrice(),
        prodProduct.getUnit(),
        stockMove,
        StockMoveLineService.TYPE_IN_PRODUCTIONS,
        false,
        BigDecimal.ZERO);
  }

  public void finish(OperationOrder operationOrder) throws AxelorException {

    List<StockMove> stockMoveList = operationOrder.getInStockMoveList();

    if (stockMoveList != null) {
      // clear empty stock move
      stockMoveList.removeIf(
          stockMove -> CollectionUtils.isEmpty(stockMove.getStockMoveLineList()));

      for (StockMove stockMove : stockMoveList) {
        Beans.get(ManufOrderStockMoveService.class).finishStockMove(stockMove);
      }
    }
  }

  /**
   * Allows to create and realize in stock moves for the given operation order. This method is used
   * during a partial finish.
   *
   * @param operationOrder
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void partialFinish(OperationOrder operationOrder) throws AxelorException {
    ManufOrderStockMoveService manufOrderStockMoveService =
        Beans.get(ManufOrderStockMoveService.class);
    ManufOrder manufOrder = operationOrder.getManufOrder();
    Company company = manufOrder.getCompany();
    StockConfigProductionService stockConfigService = Beans.get(StockConfigProductionService.class);
    StockConfig stockConfig = stockConfigService.getStockConfig(company);

    StockLocation fromStockLocation;
    StockLocation toStockLocation;
    List<StockMove> stockMoveList;

    stockMoveList = operationOrder.getInStockMoveList();
    fromStockLocation =
        manufOrderStockMoveService.getDefaultStockLocation(
            manufOrder, company, ManufOrderStockMoveService.STOCK_LOCATION_IN);
    toStockLocation = stockConfigService.getProductionVirtualStockLocation(stockConfig);

    // realize current stock move
    Optional<StockMove> stockMoveToRealize =
        stockMoveList
            .stream()
            .filter(
                stockMove ->
                    stockMove.getStatusSelect() == StockMoveRepository.STATUS_PLANNED
                        && !CollectionUtils.isEmpty(stockMove.getStockMoveLineList()))
            .findFirst();
    if (stockMoveToRealize.isPresent()) {
      manufOrderStockMoveService.finishStockMove(stockMoveToRealize.get());
    }

    // generate new stock move

    StockMove newStockMove =
        stockMoveService.createStockMove(
            null,
            null,
            company,
            fromStockLocation,
            toStockLocation,
            null,
            operationOrder.getPlannedStartDateT().toLocalDate(),
            null,
            StockMoveRepository.TYPE_INTERNAL);
    newStockMove.setOrigin(operationOrder.getOperationName());
    newStockMove.setOriginId(operationOrder.getId());
    newStockMove.setOriginTypeSelect(StockMoveRepository.ORIGIN_OPERATION_ORDER);

    newStockMove.setStockMoveLineList(new ArrayList<>());
    createNewStockMoveLines(operationOrder, newStockMove);

    if (!newStockMove.getStockMoveLineList().isEmpty()) {
      // plan the stockmove
      stockMoveService.plan(newStockMove);

      operationOrder.addInStockMoveListItem(newStockMove);
      newStockMove.getStockMoveLineList().forEach(operationOrder::addConsumedStockMoveLineListItem);
      operationOrder.clearDiffConsumeProdProductList();
    }
  }

  /**
   * Generate stock move lines after a partial finish
   *
   * @param operationOrder
   * @param stockMove
   */
  public void createNewStockMoveLines(OperationOrder operationOrder, StockMove stockMove)
      throws AxelorException {
    List<ProdProduct> diffProdProductList;
    Beans.get(OperationOrderService.class).updateDiffProdProductList(operationOrder);
    diffProdProductList = new ArrayList<>(operationOrder.getDiffConsumeProdProductList());
    Beans.get(ManufOrderStockMoveService.class)
        .createNewStockMoveLines(
            diffProdProductList, stockMove, StockMoveLineService.TYPE_IN_PRODUCTIONS);
  }

  public void cancel(OperationOrder operationOrder) throws AxelorException {

    List<StockMove> stockMoveList = operationOrder.getInStockMoveList();

    if (stockMoveList != null) {

      for (StockMove stockMove : stockMoveList) {
        stockMoveService.cancel(stockMove);
      }

      stockMoveList
          .stream()
          .filter(stockMove -> stockMove.getStockMoveLineList() != null)
          .flatMap(stockMove -> stockMove.getStockMoveLineList().stream())
          .forEach(stockMoveLine -> stockMoveLine.setConsumedOperationOrder(null));
    }
  }

  /**
   * Clear the consumed list and create a new one with the right quantity.
   *
   * @param operationOrder
   * @param qtyToUpdate
   */
  public void createNewConsumedStockMoveLineList(
      OperationOrder operationOrder, BigDecimal qtyToUpdate) throws AxelorException {
    ManufOrderStockMoveService manufOrderStockMoveService =
        Beans.get(ManufOrderStockMoveService.class);

    // find planned stock move
    Optional<StockMove> stockMoveOpt =
        manufOrderStockMoveService.getPlannedStockMove(operationOrder.getInStockMoveList());
    if (!stockMoveOpt.isPresent()) {
      return;
    }

    StockMove stockMove = stockMoveOpt.get();

    stockMoveService.cancel(stockMove);

    // clear all lists from planned lines
    operationOrder
        .getConsumedStockMoveLineList()
        .removeIf(
            stockMoveLine ->
                stockMoveLine.getStockMove().getStatusSelect()
                    == StockMoveRepository.STATUS_CANCELED);
    stockMove.clearStockMoveLineList();

    // create a new list
    for (ProdProduct prodProduct : operationOrder.getToConsumeProdProductList()) {
      BigDecimal qty =
          manufOrderStockMoveService.getFractionQty(
              operationOrder.getManufOrder(), prodProduct, qtyToUpdate);
      manufOrderStockMoveService._createStockMoveLine(
          prodProduct, stockMove, StockMoveLineService.TYPE_IN_PRODUCTIONS, qty);
      // Update consumed StockMoveLineList with created stock move lines
      stockMove
          .getStockMoveLineList()
          .stream()
          .filter(
              stockMoveLine1 ->
                  !operationOrder.getConsumedStockMoveLineList().contains(stockMoveLine1))
          .forEach(operationOrder::addConsumedStockMoveLineListItem);
    }
    stockMoveService.plan(stockMove);
  }
}
