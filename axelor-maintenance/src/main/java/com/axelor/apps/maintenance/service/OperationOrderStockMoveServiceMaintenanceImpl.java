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
package com.axelor.apps.maintenance.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.maintenance.exception.MaintenanceExceptionMessage;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.StockMoveProductionService;
import com.axelor.apps.production.service.config.StockConfigProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderCreateStockMoveLineService;
import com.axelor.apps.production.service.manuforder.ManufOrderGetStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderOutsourceService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderStockMoveServiceImpl;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class OperationOrderStockMoveServiceMaintenanceImpl
    extends OperationOrderStockMoveServiceImpl {

  protected StockConfigProductionService stockConfigProductionService;

  @Inject
  public OperationOrderStockMoveServiceMaintenanceImpl(
      StockMoveService stockMoveService,
      StockMoveLineService stockMoveLineService,
      StockLocationRepository stockLocationRepo,
      ProductCompanyService productCompanyService,
      ManufOrderStockMoveService manufOrderStockMoveService,
      ManufOrderOutsourceService manufOrderOutsourceService,
      ManufOrderGetStockMoveService manufOrderGetStockMoveService,
      ManufOrderCreateStockMoveLineService manufOrderCreateStockMoveLineService,
      StockMoveProductionService stockMoveProductionService,
      OperationOrderService operationOrderService,
      StockConfigProductionService stockConfigProductionService) {
    super(
        stockMoveService,
        stockMoveLineService,
        stockLocationRepo,
        productCompanyService,
        manufOrderStockMoveService,
        manufOrderOutsourceService,
        manufOrderGetStockMoveService,
        manufOrderCreateStockMoveLineService,
        stockMoveProductionService,
        operationOrderService);
    this.stockConfigProductionService = stockConfigProductionService;
  }

  @Override
  public void createToConsumeStockMove(OperationOrder operationOrder) throws AxelorException {
    ManufOrder manufOrder = operationOrder.getManufOrder();
    if (manufOrder.getTypeSelect() != ManufOrderRepository.TYPE_MAINTENANCE) {
      super.createToConsumeStockMove(operationOrder);
      return;
    }

    Company company = manufOrder.getCompany();
    if (operationOrder.getToConsumeProdProductList() == null || company == null) {
      return;
    }

    StockLocation virtualStockLocation = getMaintenanceVirtualStockLocation(company);
    StockLocation fromStockLocation = getMaintenanceFromStockLocation(operationOrder, company);

    StockMove stockMove =
        stockMoveService.createStockMove(
            null,
            null,
            company,
            fromStockLocation,
            virtualStockLocation,
            null,
            operationOrder.getPlannedStartDateT().toLocalDate(),
            null,
            StockMoveRepository.TYPE_INTERNAL);
    stockMove.setOperationOrder(operationOrder);
    stockMove.setOrigin(operationOrder.getOperationName());

    for (ProdProduct prodProduct : operationOrder.getToConsumeProdProductList()) {
      this._createStockMoveLine(prodProduct, stockMove, fromStockLocation, virtualStockLocation);
    }

    if (stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()) {
      stockMoveService.plan(stockMove);
      operationOrder.addInStockMoveListItem(stockMove);
    }

    if (stockMove.getStockMoveLineList() != null) {
      for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
        operationOrder.addConsumedStockMoveLineListItem(stockMoveLine);
      }
    }
  }

  @Override
  public StockMove _createToConsumeStockMove(OperationOrder operationOrder, Company company)
      throws AxelorException {
    ManufOrder manufOrder = operationOrder.getManufOrder();
    if (manufOrder.getTypeSelect() != ManufOrderRepository.TYPE_MAINTENANCE) {
      return super._createToConsumeStockMove(operationOrder, company);
    }

    StockLocation virtualStockLocation = getMaintenanceVirtualStockLocation(company);
    StockLocation fromStockLocation = getMaintenanceFromStockLocation(operationOrder, company);

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

  protected StockLocation getMaintenanceVirtualStockLocation(Company company)
      throws AxelorException {
    StockConfig stockConfig = stockConfigProductionService.getStockConfig(company);
    StockLocation maintenanceVirtualLocation = stockConfig.getMaintenanceVirtualStockLocation();
    if (maintenanceVirtualLocation == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(MaintenanceExceptionMessage.MAINTENANCE_VIRTUAL_STOCK_LOCATION_MISSING),
          company.getName());
    }
    return maintenanceVirtualLocation;
  }

  protected StockLocation getMaintenanceFromStockLocation(
      OperationOrder operationOrder, Company company) throws AxelorException {
    ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();
    if (prodProcessLine != null && prodProcessLine.getStockLocation() != null) {
      return prodProcessLine.getStockLocation();
    }

    StockConfig stockConfig = stockConfigProductionService.getStockConfig(company);
    StockLocation maintenanceLocation = stockConfig.getMaintenanceComponentsStockLocation();
    if (maintenanceLocation == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(MaintenanceExceptionMessage.MAINTENANCE_COMPONENTS_STOCK_LOCATION_MISSING),
          company.getName());
    }
    return maintenanceLocation;
  }
}
