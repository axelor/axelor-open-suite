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
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.config.StockConfigProductionService;
import com.axelor.apps.production.service.operationorder.OperationOrderStockMoveService;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManufOrderStockMoveServiceImpl implements ManufOrderStockMoveService {

  public static final int PART_FINISH_IN = 1;
  public static final int PART_FINISH_OUT = 2;
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected StockMoveService stockMoveService;
  protected StockMoveLineService stockMoveLineService;
  protected AppBaseService appBaseService;
  protected SupplyChainConfigService supplyChainConfigService;

  protected ProductCompanyService productCompanyService;
  protected StockConfigProductionService stockConfigProductionService;
  protected PartnerService partnerService;
  protected ManufOrderOutsourceService manufOrderOutsourceService;
  protected StockMoveLineRepository stockMoveLineRepository;
  protected ManufOrderOutgoingStockMoveService manufOrderOutgoingStockMoveService;
  protected ManufOrderGetStockMoveService manufOrderGetStockMoveService;
  protected ManufOrderCreateStockMoveLineService manufOrderCreateStockMoveLineService;

  protected ManufOrderStockMoveStockLocationService manufOrderStockMoveStockLocationService;

  @Inject
  public ManufOrderStockMoveServiceImpl(
      StockMoveService stockMoveService,
      StockMoveLineService stockMoveLineService,
      AppBaseService appBaseService,
      SupplyChainConfigService supplyChainConfigService,
      ProductCompanyService productCompanyService,
      StockConfigProductionService stockConfigProductionService,
      PartnerService partnerService,
      ManufOrderOutsourceService manufOrderOutsourceService,
      StockMoveLineRepository stockMoveLineRepository,
      ManufOrderOutgoingStockMoveService manufOrderOutgoingStockMoveService,
      ManufOrderGetStockMoveService manufOrderGetStockMoveService,
      ManufOrderCreateStockMoveLineService manufOrderCreateStockMoveLineService,
      ManufOrderStockMoveStockLocationService manufOrderStockMoveStockLocationService) {
    this.supplyChainConfigService = supplyChainConfigService;
    this.stockMoveService = stockMoveService;
    this.stockMoveLineService = stockMoveLineService;
    this.appBaseService = appBaseService;
    this.productCompanyService = productCompanyService;
    this.stockConfigProductionService = stockConfigProductionService;
    this.partnerService = partnerService;
    this.manufOrderOutsourceService = manufOrderOutsourceService;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.manufOrderOutgoingStockMoveService = manufOrderOutgoingStockMoveService;
    this.manufOrderGetStockMoveService = manufOrderGetStockMoveService;
    this.manufOrderCreateStockMoveLineService = manufOrderCreateStockMoveLineService;
    this.manufOrderStockMoveStockLocationService = manufOrderStockMoveStockLocationService;
  }

  /**
   * Consume in stock moves in manuf order.
   *
   * @param manufOrder
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public void consumeInStockMoves(ManufOrder manufOrder) throws AxelorException {
    for (StockMove stockMove : manufOrder.getInStockMoveList()) {

      finishStockMove(stockMove);
    }
  }

  public void finish(ManufOrder manufOrder) throws AxelorException {
    // clear empty stock move
    manufOrder
        .getInStockMoveList()
        .removeIf(stockMove -> CollectionUtils.isEmpty(stockMove.getStockMoveLineList()));
    manufOrder
        .getOutStockMoveList()
        .removeIf(stockMove -> CollectionUtils.isEmpty(stockMove.getStockMoveLineList()));

    // finish remaining stock move
    for (StockMove stockMove : manufOrder.getInStockMoveList()) {
      this.finishStockMove(stockMove);
    }
    for (StockMove stockMove : manufOrder.getOutStockMoveList()) {
      updateRealPrice(manufOrder, stockMove);
      this.finishStockMove(stockMove);
    }
  }

  /**
   * Update price in stock move line: if the product price is configured to be real, then we use the
   * cost price from costsheet. Else, we do nothing as the planned price is already filled.
   *
   * @param manufOrder
   * @param stockMove
   */
  protected void updateRealPrice(ManufOrder manufOrder, StockMove stockMove) {
    stockMove.getStockMoveLineList().stream()
        .filter(
            stockMoveLine ->
                stockMoveLine.getProduct() != null
                    && stockMoveLine.getProduct().getRealOrEstimatedPriceSelect()
                        == ProductRepository.PRICE_METHOD_REAL)
        .forEach(stockMoveLine -> stockMoveLine.setUnitPriceUntaxed(manufOrder.getCostPrice()));
  }

  public void finishStockMove(StockMove stockMove) throws AxelorException {

    if (stockMove != null && stockMove.getStatusSelect() == StockMoveRepository.STATUS_PLANNED) {
      stockMove.setIsWithBackorder(false);
      stockMoveService.copyQtyToRealQty(stockMove);
      stockMoveService.realize(stockMove);
    }
  }

  /**
   * Call the method to realize in stock move, then the method to realize out stock move for the
   * given manufacturing order.
   *
   * @param manufOrder
   */
  @Transactional(rollbackOn = {Exception.class})
  public void partialFinish(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder.getIsConsProOnOperation()) {
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
        if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_IN_PROGRESS) {
          Beans.get(OperationOrderStockMoveService.class).partialFinish(operationOrder);
        }
      }
    } else {
      partialFinish(manufOrder, PART_FINISH_IN);
    }
    partialFinish(manufOrder, PART_FINISH_OUT);
    Beans.get(ManufOrderRepository.class).save(manufOrder);
  }

  /**
   * Allows to create and realize in or out stock moves for the given manufacturing order.
   *
   * @param manufOrder
   * @param inOrOut can be {@link ManufOrderStockMoveServiceImpl#PART_FINISH_IN} or {@link
   *     ManufOrderStockMoveServiceImpl#PART_FINISH_OUT}
   * @throws AxelorException
   */
  protected void partialFinish(ManufOrder manufOrder, int inOrOut) throws AxelorException {

    if (inOrOut != PART_FINISH_IN && inOrOut != PART_FINISH_OUT) {
      throw new IllegalArgumentException(
          I18n.get(ProductionExceptionMessage.IN_OR_OUT_INVALID_ARG));
    }

    Company company = manufOrder.getCompany();
    StockConfig stockConfig = stockConfigProductionService.getStockConfig(company);

    StockLocation fromStockLocation;
    StockLocation toStockLocation;
    List<StockMove> stockMoveList;

    if (inOrOut == PART_FINISH_IN) {
      stockMoveList = manufOrder.getInStockMoveList();
      fromStockLocation =
          manufOrderStockMoveStockLocationService.getDefaultInStockLocation(manufOrder, company);
      toStockLocation =
          stockConfigProductionService.getProductionVirtualStockLocation(
              stockConfig, manufOrderOutsourceService.isOutsource(manufOrder));

    } else {
      stockMoveList = manufOrder.getOutStockMoveList();
      fromStockLocation =
          stockConfigProductionService.getProductionVirtualStockLocation(
              stockConfig, manufOrderOutsourceService.isOutsource(manufOrder));
      toStockLocation =
          manufOrderStockMoveStockLocationService.getDefaultOutStockLocation(manufOrder, company);
    }

    // realize current stock move and update the price
    Optional<StockMove> stockMoveToRealize =
        manufOrderGetStockMoveService.getPlannedStockMove(stockMoveList);
    if (stockMoveToRealize.isPresent()) {
      updateRealPrice(manufOrder, stockMoveToRealize.get());
      finishStockMove(stockMoveToRealize.get());
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
            manufOrder.getPlannedStartDateT().toLocalDate(),
            null,
            StockMoveRepository.TYPE_INTERNAL);

    newStockMove.setStockMoveLineList(new ArrayList<>());
    newStockMove.setOrigin(manufOrder.getManufOrderSeq());
    newStockMove.setManufOrder(manufOrder);
    manufOrderCreateStockMoveLineService.createNewStockMoveLines(
        manufOrder, newStockMove, inOrOut, fromStockLocation, toStockLocation);

    if (!newStockMove.getStockMoveLineList().isEmpty()) {
      // plan the stockmove
      stockMoveService.plan(newStockMove);

      if (inOrOut == PART_FINISH_IN) {
        manufOrder.addInStockMoveListItem(newStockMove);
        newStockMove.getStockMoveLineList().forEach(manufOrder::addConsumedStockMoveLineListItem);
        manufOrder.clearDiffConsumeProdProductList();
      } else {
        manufOrder.addOutStockMoveListItem(newStockMove);
        newStockMove.getStockMoveLineList().forEach(manufOrder::addProducedStockMoveLineListItem);
      }
    }
  }

  public void cancel(ManufOrder manufOrder) throws AxelorException {

    for (StockMove stockMove : manufOrder.getInStockMoveList()) {
      this.cancel(stockMove);
    }
    for (StockMove stockMove : manufOrder.getOutStockMoveList()) {
      this.cancel(stockMove);
    }
  }

  public void cancel(StockMove stockMove) throws AxelorException {

    if (stockMove != null) {

      stockMoveService.cancel(stockMove);

      for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {

        stockMoveLine.setProducedManufOrder(null);
      }
    }
  }

  /**
   * Compute the right qty when modifying real quantity in a manuf order
   *
   * @param manufOrder
   * @param prodProduct
   * @param qtyToUpdate
   * @return
   */
  public BigDecimal getFractionQty(
      ManufOrder manufOrder, ProdProduct prodProduct, BigDecimal qtyToUpdate) {
    BigDecimal manufOrderQty = manufOrder.getQty();
    BigDecimal prodProductQty = prodProduct.getQty();

    int scale = appBaseService.getNbDecimalDigitForQty();

    return qtyToUpdate
        .multiply(prodProductQty)
        .setScale(scale, RoundingMode.HALF_UP)
        .divide(manufOrderQty, scale, RoundingMode.HALF_UP);
  }

  @Override
  public List<Long> getOutgoingStockMoves(ManufOrder manufOrder) {
    if (CollectionUtils.isEmpty(manufOrder.getSaleOrderSet())) {
      return Lists.newArrayList(0l);
    }

    return stockMoveLineRepository
        .all()
        .filter("self.saleOrderLine = :saleOrderLine AND self.stockMove != null")
        .bind("saleOrderLine", manufOrder.getSaleOrderLine())
        .fetchStream()
        .map(l -> l.getStockMove().getId())
        .collect(Collectors.toList());
  }
}
