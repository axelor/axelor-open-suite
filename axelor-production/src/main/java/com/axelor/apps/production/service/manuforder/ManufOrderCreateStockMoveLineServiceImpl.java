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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.service.StockMoveProductionService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ManufOrderCreateStockMoveLineServiceImpl
    implements ManufOrderCreateStockMoveLineService {

  protected ManufOrderResidualProductService manufOrderResidualProductService;
  protected ManufOrderGetStockMoveService manufOrderGetStockMoveService;

  protected StockMoveService stockMoveService;
  protected ProductCompanyService productCompanyService;
  protected StockMoveLineService stockMoveLineService;
  protected ManufOrderStockMoveService manufOrderStockMoveService;
  protected final StockMoveProductionService stockMoveProductionService;

  @Inject
  public ManufOrderCreateStockMoveLineServiceImpl(
      ManufOrderResidualProductService manufOrderResidualProductService,
      ManufOrderGetStockMoveService manufOrderGetStockMoveService,
      StockMoveService stockMoveService,
      ProductCompanyService productCompanyService,
      StockMoveLineService stockMoveLineService,
      ManufOrderStockMoveService manufOrderStockMoveService,
      StockMoveProductionService stockMoveProductionService) {
    this.manufOrderResidualProductService = manufOrderResidualProductService;
    this.manufOrderGetStockMoveService = manufOrderGetStockMoveService;
    this.stockMoveService = stockMoveService;
    this.productCompanyService = productCompanyService;
    this.stockMoveLineService = stockMoveLineService;
    this.manufOrderStockMoveService = manufOrderStockMoveService;
    this.stockMoveProductionService = stockMoveProductionService;
  }

  @Override
  public void createToProduceStockMoveLines(
      ManufOrder manufOrder,
      StockMove stockMove,
      StockLocation virtualStockLocation,
      StockLocation producedProductStockLocation)
      throws AxelorException {
    for (ProdProduct prodProduct : manufOrder.getToProduceProdProductList()) {

      // Only manages non residual products.
      if (!manufOrderResidualProductService.isResidualProduct(prodProduct, manufOrder)) {
        BigDecimal productCostPrice =
            prodProduct.getProduct() != null
                ? (BigDecimal)
                    productCompanyService.get(
                        prodProduct.getProduct(), "costPrice", manufOrder.getCompany())
                : BigDecimal.ZERO;
        this._createStockMoveLine(
            prodProduct,
            stockMove,
            StockMoveLineService.TYPE_OUT_PRODUCTIONS,
            prodProduct.getQty(),
            productCostPrice,
            virtualStockLocation,
            producedProductStockLocation);
      }
    }
  }

  @Override
  public void createResidualStockMoveLines(
      ManufOrder manufOrder,
      StockMove stockMove,
      StockLocation virtualStockLocation,
      StockLocation residualProductStockLocation)
      throws AxelorException {
    for (ProdProduct prodProduct : manufOrder.getToProduceProdProductList()) {

      // Only manages residual products.
      if (manufOrderResidualProductService.isResidualProduct(prodProduct, manufOrder)) {
        BigDecimal productCostPrice =
            prodProduct.getProduct() != null
                ? (BigDecimal)
                    productCompanyService.get(
                        prodProduct.getProduct(), "costPrice", manufOrder.getCompany())
                : BigDecimal.ZERO;
        this._createStockMoveLine(
            prodProduct,
            stockMove,
            StockMoveLineService.TYPE_OUT_PRODUCTIONS,
            prodProduct.getQty(),
            productCostPrice,
            virtualStockLocation,
            residualProductStockLocation);
      }
    }
  }

  @Override
  public void createToConsumeStockMoveLines(
      List<ProdProduct> prodProductList,
      StockMove stockMove,
      StockLocation fromStockLocation,
      StockLocation virtualStockLocation)
      throws AxelorException {
    for (ProdProduct prodProduct : prodProductList) {

      this._createStockMoveLine(
          prodProduct,
          stockMove,
          StockMoveLineService.TYPE_IN_PRODUCTIONS,
          fromStockLocation,
          virtualStockLocation);
    }
  }

  @Override
  public StockMoveLine _createStockMoveLine(
      ProdProduct prodProduct,
      StockMove stockMove,
      int inOrOutType,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {

    return _createStockMoveLine(
        prodProduct,
        stockMove,
        inOrOutType,
        prodProduct.getQty(),
        fromStockLocation,
        toStockLocation);
  }

  @Override
  public StockMoveLine _createStockMoveLine(
      ProdProduct prodProduct,
      StockMove stockMove,
      int inOrOutType,
      BigDecimal qty,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {
    BigDecimal productCostPrice =
        prodProduct.getProduct() != null
            ? (BigDecimal)
                productCompanyService.get(
                    prodProduct.getProduct(), "costPrice", stockMove.getCompany())
            : BigDecimal.ZERO;
    return _createStockMoveLine(
        prodProduct,
        stockMove,
        inOrOutType,
        qty,
        productCostPrice,
        fromStockLocation,
        toStockLocation);
  }

  protected StockMoveLine _createStockMoveLine(
      ProdProduct prodProduct,
      StockMove stockMove,
      int inOrOutType,
      BigDecimal qty,
      BigDecimal costPrice,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {

    return stockMoveLineService.createStockMoveLine(
        prodProduct.getProduct(),
        (String)
            productCompanyService.get(prodProduct.getProduct(), "name", stockMove.getCompany()),
        (String)
            productCompanyService.get(
                prodProduct.getProduct(), "description", stockMove.getCompany()),
        qty,
        costPrice,
        costPrice,
        prodProduct.getUnit(),
        stockMove,
        inOrOutType,
        false,
        BigDecimal.ZERO,
        fromStockLocation,
        toStockLocation);
  }

  /**
   * Clear the produced list and create a new one with the right quantity.
   *
   * @param manufOrder
   * @param qtyToUpdate
   */
  @Override
  public void createNewProducedStockMoveLineList(ManufOrder manufOrder, BigDecimal qtyToUpdate)
      throws AxelorException {
    Optional<StockMove> stockMoveOpt =
        manufOrderGetStockMoveService.getPlannedStockMove(
            manufOrderGetStockMoveService.getFinishedProductOutStockMoveList(manufOrder));
    if (!stockMoveOpt.isPresent()) {
      return;
    }
    StockMove stockMove = stockMoveOpt.get();

    stockMoveProductionService.cancelFromManufOrder(stockMove);

    // clear all lists
    manufOrder
        .getProducedStockMoveLineList()
        .removeIf(
            stockMoveLine ->
                stockMoveLine.getStockMove().getStatusSelect()
                    == StockMoveRepository.STATUS_CANCELED);
    clearTrackingNumberOriginStockMoveLine(stockMove);
    stockMove.clearStockMoveLineList();

    // create a new list
    for (ProdProduct prodProduct : manufOrder.getToProduceProdProductList()) {
      BigDecimal qty =
          manufOrderStockMoveService.getFractionQty(manufOrder, prodProduct, qtyToUpdate);
      BigDecimal productCostPrice =
          prodProduct.getProduct() != null
              ? (BigDecimal)
                  productCompanyService.get(
                      prodProduct.getProduct(), "costPrice", manufOrder.getCompany())
              : BigDecimal.ZERO;
      _createStockMoveLine(
          prodProduct,
          stockMove,
          StockMoveLineService.TYPE_OUT_PRODUCTIONS,
          qty,
          productCostPrice,
          stockMove.getFromStockLocation(),
          stockMove.getToStockLocation());

      // Update produced StockMoveLineList with created stock move lines
      stockMove.getStockMoveLineList().stream()
          .filter(
              stockMoveLine1 -> !manufOrder.getProducedStockMoveLineList().contains(stockMoveLine1))
          .forEach(manufOrder::addProducedStockMoveLineListItem);
    }
    stockMoveService.goBackToDraft(stockMove);
    stockMoveService.plan(stockMove);
  }

  protected void clearTrackingNumberOriginStockMoveLine(StockMove stockMove) {
    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
      if (stockMoveLine.getTrackingNumber() != null) {
        stockMoveLine.getTrackingNumber().setOriginStockMoveLine(null);
      }
    }
  }

  /**
   * Generate stock move lines after a partial finish
   *
   * @param manufOrder
   * @param stockMove
   * @param inOrOut can be {@link ManufOrderStockMoveServiceImpl#PART_FINISH_IN} or {@link
   *     ManufOrderStockMoveServiceImpl#PART_FINISH_OUT}
   */
  @Override
  public void createNewStockMoveLines(
      ManufOrder manufOrder,
      StockMove stockMove,
      int inOrOut,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {
    int stockMoveLineType;
    List<ProdProduct> diffProdProductList;
    if (inOrOut == ManufOrderStockMoveServiceImpl.PART_FINISH_IN) {
      stockMoveLineType = StockMoveLineService.TYPE_IN_PRODUCTIONS;

      diffProdProductList = new ArrayList<>(manufOrder.getDiffConsumeProdProductList());
    } else {
      stockMoveLineType = StockMoveLineService.TYPE_OUT_PRODUCTIONS;

      // must compute remaining quantities in produced product
      List<ProdProduct> outProdProductList = manufOrder.getToProduceProdProductList();
      List<StockMoveLine> stockMoveLineList = manufOrder.getProducedStockMoveLineList();

      if (outProdProductList == null || stockMoveLineList == null) {
        return;
      }
      diffProdProductList =
          Beans.get(ManufOrderService.class)
              .createDiffProdProductList(manufOrder, outProdProductList, stockMoveLineList);
    }
    createNewStockMoveLines(
        diffProdProductList, stockMove, stockMoveLineType, fromStockLocation, toStockLocation);
  }

  /**
   * Generate stock move lines after a partial finish
   *
   * @param diffProdProductList
   * @param stockMove
   * @param stockMoveLineType
   * @throws AxelorException
   */
  @Override
  public void createNewStockMoveLines(
      List<ProdProduct> diffProdProductList,
      StockMove stockMove,
      int stockMoveLineType,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {
    diffProdProductList.forEach(prodProduct -> prodProduct.setQty(prodProduct.getQty().negate()));
    for (ProdProduct prodProduct : diffProdProductList) {
      if (prodProduct.getQty().signum() >= 0) {
        _createStockMoveLine(
            prodProduct, stockMove, stockMoveLineType, fromStockLocation, toStockLocation);
      }
    }
  }

  /**
   * Clear the consumed list and create a new one with the right quantity.
   *
   * @param manufOrder
   * @param qtyToUpdate
   */
  @Override
  public void createNewConsumedStockMoveLineList(ManufOrder manufOrder, BigDecimal qtyToUpdate)
      throws AxelorException {
    // find planned stock move
    Optional<StockMove> stockMoveOpt =
        manufOrderGetStockMoveService.getPlannedStockMove(manufOrder.getInStockMoveList());
    if (!stockMoveOpt.isPresent()) {
      return;
    }

    StockMove stockMove = stockMoveOpt.get();

    stockMoveProductionService.cancelFromManufOrder(stockMove);

    // clear all lists from planned lines
    manufOrder
        .getConsumedStockMoveLineList()
        .removeIf(
            stockMoveLine ->
                stockMoveLine.getStockMove().getStatusSelect()
                    == StockMoveRepository.STATUS_CANCELED);
    stockMove.clearStockMoveLineList();

    // create a new list
    for (ProdProduct prodProduct : manufOrder.getToConsumeProdProductList()) {
      BigDecimal qty =
          manufOrderStockMoveService.getFractionQty(manufOrder, prodProduct, qtyToUpdate);
      _createStockMoveLine(
          prodProduct,
          stockMove,
          StockMoveLineService.TYPE_IN_PRODUCTIONS,
          qty,
          stockMove.getFromStockLocation(),
          stockMove.getToStockLocation());

      // Update consumed StockMoveLineList with created stock move lines
      stockMove.getStockMoveLineList().stream()
          .filter(
              stockMoveLine1 -> !manufOrder.getConsumedStockMoveLineList().contains(stockMoveLine1))
          .forEach(manufOrder::addConsumedStockMoveLineListItem);
    }
    stockMoveService.goBackToDraft(stockMove);
    stockMoveService.plan(stockMove);
  }
}
