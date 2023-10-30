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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.ProductCompanyService;
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
  protected ProductCompanyService productCompanyService;
  protected ManufOrderStockMoveService manufOrderStockMoveService;

  @Inject
  public OperationOrderStockMoveService(
      StockMoveService stockMoveService,
      StockMoveLineService stockMoveLineService,
      StockLocationRepository stockLocationRepo,
      ProductCompanyService productCompanyService,
      ManufOrderStockMoveService manufOrderStockMoveService) {
    this.stockMoveService = stockMoveService;
    this.stockMoveLineService = stockMoveLineService;
    this.stockLocationRepo = stockLocationRepo;
    this.productCompanyService = productCompanyService;
    this.manufOrderStockMoveService = manufOrderStockMoveService;
  }

  public void createToConsumeStockMove(OperationOrder operationOrder) throws AxelorException {

    Company company = operationOrder.getManufOrder().getCompany();

    if (operationOrder.getToConsumeProdProductList() != null && company != null) {

      StockMove stockMove = this._createToConsumeStockMove(operationOrder, company);
      stockMove.setOperationOrder(operationOrder);
      stockMove.setOrigin(operationOrder.getOperationName());

      StockConfigProductionService stockConfigService =
          Beans.get(StockConfigProductionService.class);
      StockConfig stockConfig = stockConfigService.getStockConfig(company);
      ManufOrder manufOrder = operationOrder.getManufOrder();
      StockLocation virtualStockLocation =
          stockConfigService.getProductionVirtualStockLocation(
              stockConfig, manufOrder.getProdProcess().getOutsourcing());

      StockLocation fromStockLocation;

      ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();
      if (operationOrder.getManufOrder().getIsConsProOnOperation()
          && prodProcessLine != null
          && prodProcessLine.getStockLocation() != null) {
        fromStockLocation = prodProcessLine.getStockLocation();
      } else if (!manufOrder.getIsConsProOnOperation()
          && prodProcessLine != null
          && prodProcessLine.getProdProcess() != null
          && prodProcessLine.getProdProcess().getStockLocation() != null) {
        fromStockLocation = prodProcessLine.getProdProcess().getStockLocation();
      } else {
        fromStockLocation =
            stockConfigService.getComponentDefaultStockLocation(
                manufOrder.getWorkshopStockLocation(), stockConfig);
      }

      for (ProdProduct prodProduct : operationOrder.getToConsumeProdProductList()) {

        StockMoveLine stockMoveLine =
            this._createStockMoveLine(
                prodProduct, stockMove, fromStockLocation, virtualStockLocation);
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
    ManufOrder manufOrder = operationOrder.getManufOrder();
    StockLocation virtualStockLocation =
        stockConfigService.getProductionVirtualStockLocation(
            stockConfig, manufOrder.getProdProcess().getOutsourcing());

    StockLocation fromStockLocation;

    ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();
    if (operationOrder.getManufOrder().getIsConsProOnOperation()
        && prodProcessLine != null
        && prodProcessLine.getStockLocation() != null) {
      fromStockLocation = prodProcessLine.getStockLocation();
    } else if (!manufOrder.getIsConsProOnOperation()
        && prodProcessLine != null
        && prodProcessLine.getProdProcess() != null
        && prodProcessLine.getProdProcess().getStockLocation() != null) {
      fromStockLocation = prodProcessLine.getProdProcess().getStockLocation();
    } else {
      fromStockLocation =
          stockConfigService.getComponentDefaultStockLocation(
              manufOrder.getWorkshopStockLocation(), stockConfig);
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

  protected StockMoveLine _createStockMoveLine(
      ProdProduct prodProduct,
      StockMove stockMove,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {

    return stockMoveLineService.createStockMoveLine(
        prodProduct.getProduct(),
        (String)
            productCompanyService.get(prodProduct.getProduct(), "name", stockMove.getCompany()),
        (String)
            productCompanyService.get(prodProduct.getProduct(), "name", stockMove.getCompany()),
        prodProduct.getQty(),
        (BigDecimal)
            productCompanyService.get(
                prodProduct.getProduct(), "costPrice", stockMove.getCompany()),
        (BigDecimal)
            productCompanyService.get(
                prodProduct.getProduct(), "costPrice", stockMove.getCompany()),
        prodProduct.getUnit(),
        stockMove,
        StockMoveLineService.TYPE_IN_PRODUCTIONS,
        false,
        BigDecimal.ZERO,
        fromStockLocation,
        toStockLocation);
  }

  public void finish(OperationOrder operationOrder) throws AxelorException {

    List<StockMove> stockMoveList = operationOrder.getInStockMoveList();

    if (stockMoveList != null) {
      // clear empty stock move
      stockMoveList.removeIf(
          stockMove -> CollectionUtils.isEmpty(stockMove.getStockMoveLineList()));

      for (StockMove stockMove : stockMoveList) {
        manufOrderStockMoveService.finishStockMove(stockMove);
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
  @Transactional(rollbackOn = {Exception.class})
  public void partialFinish(OperationOrder operationOrder) throws AxelorException {
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
    toStockLocation =
        stockConfigService.getProductionVirtualStockLocation(
            stockConfig, operationOrder.getManufOrder().getProdProcess().getOutsourcing());

    // realize current stock move
    Optional<StockMove> stockMoveToRealize =
        stockMoveList.stream()
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
    newStockMove.setOperationOrder(operationOrder);

    newStockMove.setStockMoveLineList(new ArrayList<>());
    createNewStockMoveLines(operationOrder, newStockMove, fromStockLocation, toStockLocation);

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
  public void createNewStockMoveLines(
      OperationOrder operationOrder,
      StockMove stockMove,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {
    List<ProdProduct> diffProdProductList;
    Beans.get(OperationOrderService.class).updateDiffProdProductList(operationOrder);
    diffProdProductList = new ArrayList<>(operationOrder.getDiffConsumeProdProductList());
    manufOrderStockMoveService.createNewStockMoveLines(
        diffProdProductList,
        stockMove,
        StockMoveLineService.TYPE_IN_PRODUCTIONS,
        fromStockLocation,
        toStockLocation);
  }

  public void cancel(OperationOrder operationOrder) throws AxelorException {

    List<StockMove> stockMoveList = operationOrder.getInStockMoveList();

    if (stockMoveList != null) {

      for (StockMove stockMove : stockMoveList) {
        stockMoveService.cancel(stockMove);
      }

      stockMoveList.stream()
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
          prodProduct, stockMove, StockMoveLineService.TYPE_IN_PRODUCTIONS, qty, null, null);
      // Update consumed StockMoveLineList with created stock move lines
      stockMove.getStockMoveLineList().stream()
          .filter(
              stockMoveLine1 ->
                  !operationOrder.getConsumedStockMoveLineList().contains(stockMoveLine1))
          .forEach(operationOrder::addConsumedStockMoveLineListItem);
    }
    stockMoveService.goBackToDraft(stockMove);
    stockMoveService.plan(stockMove);
  }
}
