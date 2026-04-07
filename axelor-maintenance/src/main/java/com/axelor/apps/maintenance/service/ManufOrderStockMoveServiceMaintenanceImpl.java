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
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.maintenance.exception.MaintenanceExceptionMessage;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.StockMoveProductionService;
import com.axelor.apps.production.service.config.StockConfigProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderCreateStockMoveLineService;
import com.axelor.apps.production.service.manuforder.ManufOrderGetStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderOutgoingStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderOutsourceService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveServiceImpl;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveToolService;
import com.axelor.apps.stock.utils.JpaModelHelper;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.i18n.I18n;
import jakarta.inject.Inject;
import org.apache.commons.collections.CollectionUtils;

public class ManufOrderStockMoveServiceMaintenanceImpl extends ManufOrderStockMoveServiceImpl {

  @Inject
  public ManufOrderStockMoveServiceMaintenanceImpl(
      StockMoveProductionService stockMoveProductionService,
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
      StockMoveToolService stockMoveToolService,
      StockMoveRepository stockMoveRepository) {
    super(
        stockMoveProductionService,
        stockMoveLineService,
        appBaseService,
        supplyChainConfigService,
        productCompanyService,
        stockConfigProductionService,
        partnerService,
        manufOrderOutsourceService,
        stockMoveLineRepository,
        manufOrderOutgoingStockMoveService,
        manufOrderGetStockMoveService,
        manufOrderCreateStockMoveLineService,
        stockMoveToolService,
        stockMoveRepository);
  }

  /**
   * For maintenance orders, use maintenance components stock location from StockConfig instead of
   * the production component default stock location.
   */
  @Override
  protected StockLocation _getDefaultInStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException {
    if (manufOrder.getTypeSelect() != ManufOrderRepository.TYPE_MAINTENANCE) {
      return super._getDefaultInStockLocation(manufOrder, company);
    }

    StockLocation stockLocation =
        getDefaultStockLocation(manufOrder.getProdProcess(), STOCK_LOCATION_IN);
    if (stockLocation != null) {
      return stockLocation;
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

  /**
   * For maintenance orders, use maintenance virtual stock location from StockConfig instead of the
   * production virtual stock location.
   */
  @Override
  public StockLocation _getVirtualProductionStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException {
    if (manufOrder.getTypeSelect() != ManufOrderRepository.TYPE_MAINTENANCE) {
      return super._getVirtualProductionStockLocation(manufOrder, company);
    }

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

  /** For maintenance orders, only realize IN stock moves (no OUT stock moves). */
  @Override
  public ManufOrder finish(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder.getTypeSelect() != ManufOrderRepository.TYPE_MAINTENANCE) {
      return super.finish(manufOrder);
    }

    manufOrder
        .getInStockMoveList()
        .removeIf(stockMove -> CollectionUtils.isEmpty(stockMove.getStockMoveLineList()));

    for (StockMove stockMove : manufOrder.getInStockMoveList()) {
      this.finishStockMove(stockMove);
    }

    return JpaModelHelper.ensureManaged(manufOrder);
  }
}
