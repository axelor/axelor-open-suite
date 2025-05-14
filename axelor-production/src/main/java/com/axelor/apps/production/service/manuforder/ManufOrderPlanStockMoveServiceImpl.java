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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.service.ReservedQtyService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.google.inject.Inject;
import java.util.Optional;

public class ManufOrderPlanStockMoveServiceImpl implements ManufOrderPlanStockMoveService {

  protected StockMoveService stockMoveService;
  protected ManufOrderStockMoveService manufOrderStockMoveService;
  protected SupplyChainConfigService supplyChainConfigService;
  protected ReservedQtyService reservedQtyService;
  protected ManufOrderCreateStockMoveService manufOrderCreateStockMoveService;
  protected ManufOrderCreateStockMoveLineService manufOrderCreateStockMoveLineService;

  @Inject
  public ManufOrderPlanStockMoveServiceImpl(
      StockMoveService stockMoveService,
      ManufOrderStockMoveService manufOrderStockMoveService,
      SupplyChainConfigService supplyChainConfigService,
      ReservedQtyService reservedQtyService,
      ManufOrderCreateStockMoveService manufOrderCreateStockMoveService,
      ManufOrderCreateStockMoveLineService manufOrderCreateStockMoveLineService) {
    this.stockMoveService = stockMoveService;
    this.manufOrderStockMoveService = manufOrderStockMoveService;
    this.supplyChainConfigService = supplyChainConfigService;
    this.reservedQtyService = reservedQtyService;
    this.manufOrderCreateStockMoveService = manufOrderCreateStockMoveService;
    this.manufOrderCreateStockMoveLineService = manufOrderCreateStockMoveLineService;
  }

  @Override
  public Optional<StockMove> createAndPlanToProduceStockMoveWithLines(ManufOrder manufOrder)
      throws AxelorException {

    Company company = manufOrder.getCompany();
    StockLocation virtualStockLocation =
        manufOrderStockMoveService.getVirtualStockLocationForProducedStockMove(manufOrder, company);
    StockLocation producedProductStockLocation =
        manufOrderStockMoveService.getProducedProductStockLocation(manufOrder, company);

    if (manufOrder.getToProduceProdProductList() != null && company != null) {

      StockMove stockMove =
          manufOrderCreateStockMoveService._createToProduceStockMove(
              manufOrder, company, virtualStockLocation, producedProductStockLocation);

      manufOrderCreateStockMoveLineService.createToProduceStockMoveLines(
          manufOrder, stockMove, virtualStockLocation, producedProductStockLocation);
      planProducedStockMove(stockMove);

      return Optional.of(stockMove);
    }
    return Optional.empty();
  }

  @Override
  public Optional<StockMove> createAndPlanResidualStockMoveWithLines(ManufOrder manufOrder)
      throws AxelorException {

    Company company = manufOrder.getCompany();
    StockLocation virtualStockLocation =
        manufOrderStockMoveService.getVirtualStockLocationForProducedStockMove(manufOrder, company);
    StockLocation residualProductStockLocation =
        manufOrderStockMoveService.getResidualProductStockLocation(manufOrder);

    if (manufOrder.getToProduceProdProductList() != null && company != null) {

      StockMove stockMove =
          manufOrderCreateStockMoveService._createToProduceStockMove(
              manufOrder, company, virtualStockLocation, residualProductStockLocation);

      manufOrderCreateStockMoveLineService.createResidualStockMoveLines(
          manufOrder, stockMove, virtualStockLocation, residualProductStockLocation);
      planProducedStockMove(stockMove);

      return Optional.of(stockMove);
    }
    return Optional.empty();
  }

  @Override
  public Optional<StockMove> createAndPlanToConsumeStockMoveWithLines(ManufOrder manufOrder)
      throws AxelorException {
    Company company = manufOrder.getCompany();

    // Get stock locations dest and source
    StockLocation fromStockLocation =
        manufOrderStockMoveService.getFromStockLocationForConsumedStockMove(manufOrder, company);
    StockLocation virtualStockLocation =
        manufOrderStockMoveService.getVirtualStockLocationForConsumedStockMove(manufOrder, company);

    if (manufOrder.getToConsumeProdProductList() != null && company != null) {

      // Create stock move
      StockMove stockMove =
          manufOrderCreateStockMoveService._createToConsumeStockMove(
              manufOrder, company, fromStockLocation, virtualStockLocation);

      // Create stock move lines of Stock move
      manufOrderCreateStockMoveLineService.createToConsumeStockMoveLines(
          manufOrder.getToConsumeProdProductList(),
          stockMove,
          fromStockLocation,
          virtualStockLocation);

      // Plan stock move
      planConsumedStockMove(stockMove);

      return Optional.of(stockMove);
    }
    return Optional.empty();
  }

  @Override
  public Optional<StockMove> createAndPlanToConsumeStockMove(ManufOrder manufOrder)
      throws AxelorException {
    Company company = manufOrder.getCompany();

    // Get stock locations dest and source
    StockLocation fromStockLocation =
        manufOrderStockMoveService.getFromStockLocationForConsumedStockMove(manufOrder, company);
    StockLocation virtualStockLocation =
        manufOrderStockMoveService.getVirtualStockLocationForConsumedStockMove(manufOrder, company);

    if (manufOrder.getToConsumeProdProductList() != null && company != null) {

      // Create stock move
      StockMove stockMove =
          manufOrderCreateStockMoveService._createToConsumeStockMove(
              manufOrder, company, fromStockLocation, virtualStockLocation);

      // Plan stock move
      planConsumedStockMove(stockMove);
      return Optional.of(stockMove);
    }
    return Optional.empty();
  }

  protected void planConsumedStockMove(StockMove stockMove) throws AxelorException {
    SupplyChainConfig supplyChainConfig =
        supplyChainConfigService.getSupplyChainConfig(stockMove.getCompany());
    if (stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()) {
      stockMoveService.plan(stockMove);
      if (supplyChainConfig.getAutoRequestReservedQtyOnManufOrder()) {
        requestStockReservation(stockMove);
      }
    }
  }

  protected void planProducedStockMove(StockMove stockMove) throws AxelorException {
    if (stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()) {
      stockMoveService.plan(stockMove);
    }
  }

  /** Request reservation (and allocate if possible) all stock for this stock move. */
  protected void requestStockReservation(StockMove stockMove) throws AxelorException {
    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
      reservedQtyService.allocateAll(stockMoveLine);
    }
  }
}
