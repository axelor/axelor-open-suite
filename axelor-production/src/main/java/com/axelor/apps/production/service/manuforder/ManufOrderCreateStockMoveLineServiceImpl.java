/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.production.service.ProductionTrackingPreservationService;
import com.axelor.apps.production.service.ProductionTrackingPreservationService.PreservedTrackingNumbersByProduct;
import com.axelor.apps.production.service.StockMoveProductionService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.utils.JpaModelHelper;
import com.axelor.inject.Beans;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ManufOrderCreateStockMoveLineServiceImpl
    implements ManufOrderCreateStockMoveLineService {

  protected ManufOrderResidualProductService manufOrderResidualProductService;
  protected ManufOrderGetStockMoveService manufOrderGetStockMoveService;

  protected StockMoveService stockMoveService;
  protected ProductCompanyService productCompanyService;
  protected StockMoveLineService stockMoveLineService;
  protected ManufOrderStockMoveService manufOrderStockMoveService;
  protected final StockMoveProductionService stockMoveProductionService;
  protected final ProductionTrackingPreservationService productionTrackingPreservationService;

  @Inject
  public ManufOrderCreateStockMoveLineServiceImpl(
      ManufOrderResidualProductService manufOrderResidualProductService,
      ManufOrderGetStockMoveService manufOrderGetStockMoveService,
      StockMoveService stockMoveService,
      ProductCompanyService productCompanyService,
      StockMoveLineService stockMoveLineService,
      ManufOrderStockMoveService manufOrderStockMoveService,
      StockMoveProductionService stockMoveProductionService,
      ProductionTrackingPreservationService productionTrackingPreservationService) {
    this.manufOrderResidualProductService = manufOrderResidualProductService;
    this.manufOrderGetStockMoveService = manufOrderGetStockMoveService;
    this.stockMoveService = stockMoveService;
    this.productCompanyService = productCompanyService;
    this.stockMoveLineService = stockMoveLineService;
    this.manufOrderStockMoveService = manufOrderStockMoveService;
    this.stockMoveProductionService = stockMoveProductionService;
    this.productionTrackingPreservationService = productionTrackingPreservationService;
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

    StockMoveLine stockMoveLine =
        stockMoveLineService.createStockMoveLine(
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
    stockMoveLine.getStockMove().addStockMoveLineListItem(stockMoveLine);
    return stockMoveLine;
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
    manufOrder = JpaModelHelper.ensureManaged(manufOrder);
    Optional<StockMove> stockMoveOpt =
        manufOrderGetStockMoveService.getPlannedStockMove(
            manufOrderGetStockMoveService.getFinishedProductOutStockMoveList(manufOrder));
    if (!stockMoveOpt.isPresent()) {
      return;
    }
    StockMove stockMove = stockMoveOpt.get();

    // Snapshot tracking numbers before cancellation mutates the lines
    List<StockMoveLine> originalLines =
        stockMove.getStockMoveLineList() != null
            ? new ArrayList<>(stockMove.getStockMoveLineList())
            : new ArrayList<>();

    stockMoveProductionService.cancelFromManufOrder(stockMove);

    PreservedTrackingNumbersByProduct preservedTrackingNumbersByProduct =
        productionTrackingPreservationService.getPreservedTrackingNumbersByProduct(originalLines);

    manufOrder = JpaModelHelper.ensureManaged(manufOrder);

    // clear all lists
    manufOrder
        .getProducedStockMoveLineList()
        .removeIf(
            stockMoveLine ->
                stockMoveLine.getStockMove().getStatusSelect()
                    == StockMoveRepository.STATUS_CANCELED);

    stockMove = JpaModelHelper.ensureManaged(stockMove);
    clearTrackingNumberOriginStockMoveLine(stockMove);
    stockMove.clearStockMoveLineList();

    // create a new list, reusing preserved tracking numbers
    for (ProdProduct prodProduct : manufOrder.getToProduceProdProductList()) {
      BigDecimal qty =
          manufOrderStockMoveService.getFractionQty(manufOrder, prodProduct, qtyToUpdate);
      BigDecimal realizedQty =
          manufOrder.getProducedStockMoveLineList().stream()
              .filter(
                  sml ->
                      sml.getProduct() != null
                          && sml.getProduct().equals(prodProduct.getProduct())
                          && sml.getStockMove() != null
                          && sml.getStockMove().getStatusSelect()
                              == StockMoveRepository.STATUS_REALIZED)
              .map(StockMoveLine::getRealQty)
              .filter(Objects::nonNull)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
      qty = qty.subtract(realizedQty).max(BigDecimal.ZERO);
      productionTrackingPreservationService.createStockMoveLinesWithPreservedTracking(
          prodProduct,
          stockMove,
          StockMoveLineService.TYPE_OUT_PRODUCTIONS,
          qty,
          stockMove.getFromStockLocation(),
          stockMove.getToStockLocation(),
          preservedTrackingNumbersByProduct);
    }

    // Record production lines before adding reserves
    Set<StockMoveLine> productionLines = new HashSet<>(stockMove.getStockMoveLineList());

    // Create reserve lines for remaining preserved tracking (carries forward unused tracking)
    productionTrackingPreservationService.drainRemainingPreservedTracking(
        manufOrder.getToProduceProdProductList(),
        stockMove,
        StockMoveLineService.TYPE_OUT_PRODUCTIONS,
        stockMove.getFromStockLocation(),
        stockMove.getToStockLocation(),
        preservedTrackingNumbersByProduct);

    stockMoveService.goBackToDraft(stockMove);
    stockMoveService.plan(stockMove);

    stockMove = JpaModelHelper.ensureManaged(stockMove);
    manufOrder = JpaModelHelper.ensureManaged(manufOrder);
    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
      // Only add production lines to producedStockMoveLineList, NOT reserve lines
      if (productionLines.contains(stockMoveLine)
          && !manufOrder.getProducedStockMoveLineList().contains(stockMoveLine)) {
        manufOrder.addProducedStockMoveLineListItem(stockMoveLine);
      }
    }
  }

  protected void clearTrackingNumberOriginStockMoveLine(StockMove stockMove) {
    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
      if (stockMoveLine.getTrackingNumber() != null) {
        stockMoveLine.getTrackingNumber().setOriginStockMoveLine(null);
      }
    }
  }

  @Override
  public void createNewStockMoveLines(
      ManufOrder manufOrder,
      StockMove stockMove,
      int inOrOut,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {
    createNewStockMoveLines(
        manufOrder, stockMove, inOrOut, fromStockLocation, toStockLocation, null);
  }

  @Override
  public void createNewStockMoveLines(
      ManufOrder manufOrder,
      StockMove stockMove,
      int inOrOut,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      PreservedTrackingNumbersByProduct preservedTrackingNumbersByProduct)
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
        diffProdProductList,
        stockMove,
        stockMoveLineType,
        fromStockLocation,
        toStockLocation,
        preservedTrackingNumbersByProduct);
  }

  @Override
  public void createNewStockMoveLines(
      List<ProdProduct> diffProdProductList,
      StockMove stockMove,
      int stockMoveLineType,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {
    createNewStockMoveLines(
        diffProdProductList,
        stockMove,
        stockMoveLineType,
        fromStockLocation,
        toStockLocation,
        null);
  }

  @Override
  public void createNewStockMoveLines(
      List<ProdProduct> diffProdProductList,
      StockMove stockMove,
      int stockMoveLineType,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      PreservedTrackingNumbersByProduct preservedTrackingNumbersByProduct)
      throws AxelorException {
    diffProdProductList.forEach(prodProduct -> prodProduct.setQty(prodProduct.getQty().negate()));
    for (ProdProduct prodProduct : diffProdProductList) {
      if (prodProduct.getQty().signum() >= 0) {
        if (preservedTrackingNumbersByProduct != null) {
          productionTrackingPreservationService.createStockMoveLinesWithPreservedTracking(
              prodProduct,
              stockMove,
              stockMoveLineType,
              prodProduct.getQty(),
              fromStockLocation,
              toStockLocation,
              preservedTrackingNumbersByProduct);
        } else {
          _createStockMoveLine(
              prodProduct, stockMove, stockMoveLineType, fromStockLocation, toStockLocation);
        }
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
      // After a partial finish, the consumed stock move is REALIZED.
      // Create a new planned stock move for the remaining quantity.
      StockMove newStockMove =
          manufOrderGetStockMoveService.getConsumedStockMoveFromManufOrder(manufOrder);
      if (newStockMove == null) {
        return;
      }
      manufOrder = JpaModelHelper.ensureManaged(manufOrder);
      stockMoveOpt = Optional.of(newStockMove);
    }

    StockMove stockMove = stockMoveOpt.get();

    // Snapshot tracking numbers before cancellation mutates the lines
    List<StockMoveLine> originalLines =
        stockMove.getStockMoveLineList() != null
            ? new ArrayList<>(stockMove.getStockMoveLineList())
            : new ArrayList<>();

    stockMoveProductionService.cancelFromManufOrder(stockMove);

    PreservedTrackingNumbersByProduct preservedTrackingNumbersByProduct =
        productionTrackingPreservationService.getPreservedTrackingNumbersByProduct(originalLines);

    manufOrder = JpaModelHelper.ensureManaged(manufOrder);
    // clear all lists from planned lines
    manufOrder
        .getConsumedStockMoveLineList()
        .removeIf(
            stockMoveLine ->
                stockMoveLine.getStockMove().getStatusSelect()
                    == StockMoveRepository.STATUS_CANCELED);

    stockMove = JpaModelHelper.ensureManaged(stockMove);
    stockMove.clearStockMoveLineList();

    // create a new list, reusing preserved tracking numbers
    for (ProdProduct prodProduct : manufOrder.getToConsumeProdProductList()) {
      BigDecimal qty =
          manufOrderStockMoveService.getFractionQty(manufOrder, prodProduct, qtyToUpdate);
      BigDecimal realizedQty =
          manufOrder.getConsumedStockMoveLineList().stream()
              .filter(
                  sml ->
                      sml.getProduct() != null
                          && sml.getProduct().equals(prodProduct.getProduct())
                          && sml.getStockMove() != null
                          && sml.getStockMove().getStatusSelect()
                              == StockMoveRepository.STATUS_REALIZED)
              .map(StockMoveLine::getRealQty)
              .filter(Objects::nonNull)
              .reduce(BigDecimal.ZERO, BigDecimal::add);
      qty = qty.subtract(realizedQty).max(BigDecimal.ZERO);
      productionTrackingPreservationService.createStockMoveLinesWithPreservedTracking(
          prodProduct,
          stockMove,
          StockMoveLineService.TYPE_IN_PRODUCTIONS,
          qty,
          stockMove.getFromStockLocation(),
          stockMove.getToStockLocation(),
          preservedTrackingNumbersByProduct);

      // Update consumed StockMoveLineList with created stock move lines
      List<StockMoveLine> stockMoveLineList = stockMove.getStockMoveLineList();
      for (StockMoveLine stockMoveLine : stockMoveLineList) {
        if (!manufOrder.getConsumedStockMoveLineList().contains(stockMoveLine)) {
          manufOrder.addConsumedStockMoveLineListItem(stockMoveLine);
        }
      }
    }
    stockMoveService.goBackToDraft(stockMove);
    stockMoveService.plan(stockMove);
  }
}
