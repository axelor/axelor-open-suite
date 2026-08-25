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
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

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
    createNewOutStockMoveLineList(manufOrder, qtyToUpdate, false);
  }

  @Override
  public void createNewResidualStockMoveLineList(ManufOrder manufOrder, BigDecimal qtyToUpdate)
      throws AxelorException {
    createNewOutStockMoveLineList(manufOrder, qtyToUpdate, true);
  }

  /**
   * Clear the produced or the residual list and create a new one with the right quantity. Residual
   * products have their own outgoing stock move, going to their own stock location, so the two are
   * always rebuilt separately.
   *
   * @param manufOrder
   * @param qtyToUpdate
   * @param residual whether residual products or finished products are rebuilt
   */
  protected void createNewOutStockMoveLineList(
      ManufOrder manufOrder, BigDecimal qtyToUpdate, boolean residual) throws AxelorException {
    manufOrder = JpaModelHelper.ensureManaged(manufOrder);
    Optional<StockMove> stockMoveOpt =
        manufOrderGetStockMoveService.getPlannedStockMove(
            residual
                ? manufOrderGetStockMoveService.getResidualOutStockMoveLineList(manufOrder)
                : manufOrderGetStockMoveService.getFinishedProductOutStockMoveList(manufOrder));
    if (!stockMoveOpt.isPresent()) {
      if (!manufOrderStockMoveService.hasRemainingQty(
          manufOrder,
          getOutProdProductList(manufOrder, residual),
          qtyToUpdate,
          getOutStockMoveLineList(manufOrder, residual))) {
        return;
      }

      // After a partial finish, the outgoing stock move is REALIZED.
      // Create a new stock move for the remaining quantity. It is created without lines: they are
      // built below with the remaining quantity, as for an already existing stock move.
      ManufOrderPlanStockMoveService manufOrderPlanStockMoveService =
          Beans.get(ManufOrderPlanStockMoveService.class);
      Optional<StockMove> newStockMoveOpt =
          residual
              ? manufOrderPlanStockMoveService.createAndPlanResidualStockMove(manufOrder)
              : manufOrderPlanStockMoveService.createAndPlanToProduceStockMove(manufOrder);
      if (newStockMoveOpt.isEmpty()) {
        return;
      }
      manufOrder.addOutStockMoveListItem(newStockMoveOpt.get());
      manufOrder = JpaModelHelper.ensureManaged(manufOrder);
      stockMoveOpt = newStockMoveOpt;
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

    // Also reclaim tracking numbers orphaned by a manually deleted produced line
    preservedTrackingNumbersByProduct =
        productionTrackingPreservationService.reclaimOrphanedTrackingNumbers(
            preservedTrackingNumbersByProduct,
            getOutProdProductList(manufOrder, residual),
            manufOrder);

    manufOrder = JpaModelHelper.ensureManaged(manufOrder);

    // clear all lists
    List<StockMoveLine> outStockMoveLineList = getOutStockMoveLineList(manufOrder, residual);
    if (outStockMoveLineList != null) {
      outStockMoveLineList.removeIf(
          stockMoveLine ->
              stockMoveLine.getStockMove().getStatusSelect()
                  == StockMoveRepository.STATUS_CANCELED);
    }

    stockMove = JpaModelHelper.ensureManaged(stockMove);
    clearTrackingNumberOriginStockMoveLine(stockMove);
    stockMove.clearStockMoveLineList();

    // create a new list, reusing preserved tracking numbers
    for (ProdProduct prodProduct : getOutProdProductList(manufOrder, residual)) {
      BigDecimal qty =
          manufOrderStockMoveService.getRemainingQty(
              manufOrder, prodProduct, qtyToUpdate, getOutStockMoveLineList(manufOrder, residual));
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
        getOutProdProductList(manufOrder, residual),
        stockMove,
        StockMoveLineService.TYPE_OUT_PRODUCTIONS,
        stockMove.getFromStockLocation(),
        stockMove.getToStockLocation(),
        preservedTrackingNumbersByProduct);

    List<StockMoveLine> stockMoveLineList = stockMove.getStockMoveLineList();
    if (CollectionUtils.isEmpty(stockMoveLineList)) {
      return;
    }
    stockMoveService.goBackToDraft(stockMove);
    stockMoveService.plan(stockMove);

    stockMove = JpaModelHelper.ensureManaged(stockMove);
    manufOrder = JpaModelHelper.ensureManaged(manufOrder);
    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
      // Only add production lines to the outgoing list, NOT reserve lines
      List<StockMoveLine> currentList = getOutStockMoveLineList(manufOrder, residual);
      if (productionLines.contains(stockMoveLine)
          && (currentList == null || !currentList.contains(stockMoveLine))) {
        if (residual) {
          manufOrder.addResidualStockMoveLineListItem(stockMoveLine);
        } else {
          manufOrder.addProducedStockMoveLineListItem(stockMoveLine);
        }
      }
    }
  }

  /**
   * Get the products to produce of the given kind: residual products are discriminated from
   * finished products by looking them up in the bill of material.
   */
  protected List<ProdProduct> getOutProdProductList(ManufOrder manufOrder, boolean residual) {
    List<ProdProduct> toProduceProdProductList = manufOrder.getToProduceProdProductList();
    if (toProduceProdProductList == null) {
      return new ArrayList<>();
    }
    List<ProdProduct> prodProductList = new ArrayList<>();
    for (ProdProduct prodProduct : toProduceProdProductList) {
      if (manufOrderResidualProductService.isResidualProduct(prodProduct, manufOrder) == residual) {
        prodProductList.add(prodProduct);
      }
    }
    return prodProductList;
  }

  protected List<StockMoveLine> getOutStockMoveLineList(ManufOrder manufOrder, boolean residual) {
    return residual
        ? manufOrder.getResidualStockMoveLineList()
        : manufOrder.getProducedStockMoveLineList();
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

      // must compute remaining quantities in produced product, keeping residual products apart:
      // they go to their own stock move, with their own destination stock location
      boolean residual = inOrOut == ManufOrderStockMoveServiceImpl.PART_FINISH_RESIDUAL;
      List<ProdProduct> outProdProductList = getOutProdProductList(manufOrder, residual);
      List<StockMoveLine> stockMoveLineList = getOutStockMoveLineList(manufOrder, residual);

      if (stockMoveLineList == null) {
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
      List<ProdProduct> toConsumeProdProductList = manufOrder.getToConsumeProdProductList();
      List<StockMoveLine> consumedStockMoveLineList = manufOrder.getConsumedStockMoveLineList();
      if (!manufOrderStockMoveService.hasRemainingQty(
          manufOrder, toConsumeProdProductList, qtyToUpdate, consumedStockMoveLineList)) {
        return;
      }

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
    List<StockMoveLine> consumedStockMoveLineList = manufOrder.getConsumedStockMoveLineList();
    // clear all lists from planned lines
    consumedStockMoveLineList.removeIf(
        stockMoveLine ->
            stockMoveLine.getStockMove().getStatusSelect() == StockMoveRepository.STATUS_CANCELED);

    stockMove = JpaModelHelper.ensureManaged(stockMove);
    stockMove.clearStockMoveLineList();

    // create a new list, reusing preserved tracking numbers
    List<ProdProduct> toConsumeProdProductList = manufOrder.getToConsumeProdProductList();
    for (ProdProduct prodProduct : toConsumeProdProductList) {
      BigDecimal qty =
          manufOrderStockMoveService.getRemainingQty(
              manufOrder, prodProduct, qtyToUpdate, consumedStockMoveLineList);
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
        if (!consumedStockMoveLineList.contains(stockMoveLine)) {
          manufOrder.addConsumedStockMoveLineListItem(stockMoveLine);
        }
      }
    }
    List<StockMoveLine> stockMoveLineList = stockMove.getStockMoveLineList();
    if (CollectionUtils.isEmpty(stockMoveLineList)) {
      return;
    }
    stockMoveService.goBackToDraft(stockMove);
    stockMoveService.plan(stockMove);
  }
}
