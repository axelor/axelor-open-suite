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
package com.axelor.apps.production.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.production.db.repo.BillOfMaterialManagementRepository;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.ManufOrderManagementRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderManagementRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.db.repo.ProdProcessManagementRepository;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.axelor.apps.production.db.repo.StockMoveLineProductionRepository;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.BillOfMaterialServiceImpl;
import com.axelor.apps.production.service.ConfiguratorBomService;
import com.axelor.apps.production.service.ConfiguratorBomServiceImpl;
import com.axelor.apps.production.service.ConfiguratorProdProcessLineService;
import com.axelor.apps.production.service.ConfiguratorProdProcessLineServiceImpl;
import com.axelor.apps.production.service.ConfiguratorProdProcessService;
import com.axelor.apps.production.service.ConfiguratorProdProcessServiceImpl;
import com.axelor.apps.production.service.CostSheetLineService;
import com.axelor.apps.production.service.CostSheetLineServiceImpl;
import com.axelor.apps.production.service.CostSheetService;
import com.axelor.apps.production.service.CostSheetServiceImpl;
import com.axelor.apps.production.service.ManufOrderService;
import com.axelor.apps.production.service.ManufOrderServiceImpl;
import com.axelor.apps.production.service.MrpLineServiceProductionImpl;
import com.axelor.apps.production.service.MrpServiceProductionImpl;
import com.axelor.apps.production.service.OperationOrderService;
import com.axelor.apps.production.service.OperationOrderServiceImpl;
import com.axelor.apps.production.service.ProdProcessLineService;
import com.axelor.apps.production.service.ProdProcessLineServiceImpl;
import com.axelor.apps.production.service.ProductionOrderSaleOrderService;
import com.axelor.apps.production.service.ProductionOrderSaleOrderServiceImpl;
import com.axelor.apps.production.service.ProductionOrderService;
import com.axelor.apps.production.service.ProductionOrderServiceImpl;
import com.axelor.apps.production.service.ProductionOrderWizardService;
import com.axelor.apps.production.service.ProductionOrderWizardServiceImpl;
import com.axelor.apps.production.service.SaleOrderWorkflowServiceProductionImpl;
import com.axelor.apps.production.service.StockMoveProductionServiceImpl;
import com.axelor.apps.production.service.StockRulesServiceProductionImpl;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.app.AppProductionServiceImpl;
import com.axelor.apps.production.service.app.ConfiguratorServiceProductionImpl;
import com.axelor.apps.production.service.config.StockConfigProductionService;
import com.axelor.apps.sale.service.configurator.ConfiguratorServiceImpl;
import com.axelor.apps.stock.db.repo.StockMoveLineStockRepository;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.service.MrpLineServiceImpl;
import com.axelor.apps.supplychain.service.MrpServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderWorkflowServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.StockMoveServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.StockRulesServiceSupplychainImpl;

public class ProductionModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(ManufOrderRepository.class).to(ManufOrderManagementRepository.class);
    bind(OperationOrderRepository.class).to(OperationOrderManagementRepository.class);
    bind(ProductionOrderService.class).to(ProductionOrderServiceImpl.class);
    bind(BillOfMaterialService.class).to(BillOfMaterialServiceImpl.class);
    bind(ManufOrderService.class).to(ManufOrderServiceImpl.class);
    bind(OperationOrderService.class).to(OperationOrderServiceImpl.class);
    bind(ProductionOrderService.class).to(ProductionOrderServiceImpl.class);
    bind(ProductionOrderWizardService.class).to(ProductionOrderWizardServiceImpl.class);
    bind(ProductionOrderSaleOrderService.class).to(ProductionOrderSaleOrderServiceImpl.class);
    bind(MrpLineServiceImpl.class).to(MrpLineServiceProductionImpl.class);
    bind(MrpServiceImpl.class).to(MrpServiceProductionImpl.class);
    bind(CostSheetService.class).to(CostSheetServiceImpl.class);
    bind(CostSheetLineService.class).to(CostSheetLineServiceImpl.class);
    bind(SaleOrderWorkflowServiceSupplychainImpl.class)
        .to(SaleOrderWorkflowServiceProductionImpl.class);
    bind(StockRulesServiceSupplychainImpl.class).to(StockRulesServiceProductionImpl.class);
    bind(BillOfMaterialRepository.class).to(BillOfMaterialManagementRepository.class);
    bind(StockConfigService.class).to(StockConfigProductionService.class);
    bind(ConfiguratorBomService.class).to(ConfiguratorBomServiceImpl.class);
    bind(ConfiguratorProdProcessService.class).to(ConfiguratorProdProcessServiceImpl.class);
    bind(ConfiguratorProdProcessLineService.class).to(ConfiguratorProdProcessLineServiceImpl.class);
    bind(ConfiguratorServiceImpl.class).to(ConfiguratorServiceProductionImpl.class);
    bind(AppProductionService.class).to(AppProductionServiceImpl.class);
    bind(ProdProcessRepository.class).to(ProdProcessManagementRepository.class);
    bind(StockMoveLineStockRepository.class).to(StockMoveLineProductionRepository.class);
    bind(ProdProcessLineService.class).to(ProdProcessLineServiceImpl.class);
    bind(StockMoveServiceSupplychainImpl.class).to(StockMoveProductionServiceImpl.class);
  }
}
