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
package com.axelor.apps.production.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.production.db.repo.BillOfMaterialImportManagementRepository;
import com.axelor.apps.production.db.repo.BillOfMaterialImportRepository;
import com.axelor.apps.production.db.repo.BillOfMaterialManagementRepository;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.MachineManagementRepository;
import com.axelor.apps.production.db.repo.MachineRepository;
import com.axelor.apps.production.db.repo.ManufOrderManagementRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderManagementRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.db.repo.ProdProcessManagementRepository;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.axelor.apps.production.db.repo.ProdProductProductionRepository;
import com.axelor.apps.production.db.repo.ProdProductRepository;
import com.axelor.apps.production.db.repo.ProductProductionRepository;
import com.axelor.apps.production.db.repo.ProductionBatchManagementRepository;
import com.axelor.apps.production.db.repo.ProductionBatchRepository;
import com.axelor.apps.production.db.repo.RawMaterialRequirementProductionRepository;
import com.axelor.apps.production.db.repo.RawMaterialRequirementRepository;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsManagementRepository;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.production.db.repo.StockMoveLineProductionRepository;
import com.axelor.apps.production.db.repo.StockMoveProductionRepository;
import com.axelor.apps.production.db.repo.UnitCostCalculationManagementRepository;
import com.axelor.apps.production.db.repo.UnitCostCalculationRepository;
import com.axelor.apps.production.rest.ManufOrderProductRestService;
import com.axelor.apps.production.rest.ManufOrderProductRestServiceImpl;
import com.axelor.apps.production.rest.ManufOrderRestService;
import com.axelor.apps.production.rest.ManufOrderRestServiceImpl;
import com.axelor.apps.production.rest.OperationOrderRestService;
import com.axelor.apps.production.rest.OperationOrderRestServiceImpl;
import com.axelor.apps.production.service.BillOfMaterialCheckService;
import com.axelor.apps.production.service.BillOfMaterialCheckServiceImpl;
import com.axelor.apps.production.service.BillOfMaterialComputeNameService;
import com.axelor.apps.production.service.BillOfMaterialComputeNameServiceImpl;
import com.axelor.apps.production.service.BillOfMaterialDummyService;
import com.axelor.apps.production.service.BillOfMaterialDummyServiceImpl;
import com.axelor.apps.production.service.BillOfMaterialLineService;
import com.axelor.apps.production.service.BillOfMaterialLineServiceImpl;
import com.axelor.apps.production.service.BillOfMaterialMrpLineService;
import com.axelor.apps.production.service.BillOfMaterialMrpLineServiceImpl;
import com.axelor.apps.production.service.BillOfMaterialRemoveService;
import com.axelor.apps.production.service.BillOfMaterialRemoveServiceImpl;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.BillOfMaterialServiceImpl;
import com.axelor.apps.production.service.BomLineCreationService;
import com.axelor.apps.production.service.BomLineCreationServiceImpl;
import com.axelor.apps.production.service.MpsChargeService;
import com.axelor.apps.production.service.MpsChargeServiceImpl;
import com.axelor.apps.production.service.MpsWeeklyScheduleService;
import com.axelor.apps.production.service.MpsWeeklyScheduleServiceImpl;
import com.axelor.apps.production.service.MrpForecastProductionService;
import com.axelor.apps.production.service.MrpForecastProductionServiceImpl;
import com.axelor.apps.production.service.MrpLineServiceProductionImpl;
import com.axelor.apps.production.service.MrpServiceProductionImpl;
import com.axelor.apps.production.service.ProdProcessComputationService;
import com.axelor.apps.production.service.ProdProcessComputationServiceImpl;
import com.axelor.apps.production.service.ProdProcessLineComputationService;
import com.axelor.apps.production.service.ProdProcessLineComputationServiceImpl;
import com.axelor.apps.production.service.ProdProcessLineOutsourceService;
import com.axelor.apps.production.service.ProdProcessLineOutsourceServiceImpl;
import com.axelor.apps.production.service.ProdProcessLineService;
import com.axelor.apps.production.service.ProdProcessLineServiceImpl;
import com.axelor.apps.production.service.ProdProcessOutsourceService;
import com.axelor.apps.production.service.ProdProcessOutsourceServiceImpl;
import com.axelor.apps.production.service.ProdProcessWorkflowService;
import com.axelor.apps.production.service.ProdProcessWorkflowServiceImpl;
import com.axelor.apps.production.service.ProdProductAttrsService;
import com.axelor.apps.production.service.ProdProductAttrsServiceImpl;
import com.axelor.apps.production.service.ProdProductService;
import com.axelor.apps.production.service.ProdProductServiceImpl;
import com.axelor.apps.production.service.ProductionProductStockLocationServiceImpl;
import com.axelor.apps.production.service.PurchaseOrderMergingServiceProductionImpl;
import com.axelor.apps.production.service.PurchaseOrderTypeSelectProductionServiceImpl;
import com.axelor.apps.production.service.RawMaterialRequirementService;
import com.axelor.apps.production.service.RawMaterialRequirementServiceImpl;
import com.axelor.apps.production.service.SaleOrderComputeServiceProductionImpl;
import com.axelor.apps.production.service.SaleOrderConfirmProductionService;
import com.axelor.apps.production.service.SaleOrderConfirmProductionServiceImpl;
import com.axelor.apps.production.service.SaleOrderLineBomLineMappingService;
import com.axelor.apps.production.service.SaleOrderLineBomLineMappingServiceImpl;
import com.axelor.apps.production.service.SaleOrderLineBomService;
import com.axelor.apps.production.service.SaleOrderLineBomServiceImpl;
import com.axelor.apps.production.service.SaleOrderLineBomSyncService;
import com.axelor.apps.production.service.SaleOrderLineBomSyncServiceImpl;
import com.axelor.apps.production.service.SaleOrderLineCostPriceComputeProductionServiceImpl;
import com.axelor.apps.production.service.SaleOrderLineDetailsBomLineMappingService;
import com.axelor.apps.production.service.SaleOrderLineDetailsBomLineMappingServiceImpl;
import com.axelor.apps.production.service.SaleOrderLineDetailsBomService;
import com.axelor.apps.production.service.SaleOrderLineDetailsBomServiceImpl;
import com.axelor.apps.production.service.SaleOrderLineDetailsBomSyncService;
import com.axelor.apps.production.service.SaleOrderLineDetailsBomSyncServiceImpl;
import com.axelor.apps.production.service.SaleOrderLineDetailsPriceService;
import com.axelor.apps.production.service.SaleOrderLineDetailsPriceServiceImpl;
import com.axelor.apps.production.service.SaleOrderLineDetailsService;
import com.axelor.apps.production.service.SaleOrderLineDetailsServiceImpl;
import com.axelor.apps.production.service.SaleOrderLineDomainProductionService;
import com.axelor.apps.production.service.SaleOrderLineDomainProductionServiceImpl;
import com.axelor.apps.production.service.SaleOrderLineDummyProductionServiceImpl;
import com.axelor.apps.production.service.SaleOrderLineOnChangeProductionServiceImpl;
import com.axelor.apps.production.service.SaleOrderLineProductProductionService;
import com.axelor.apps.production.service.SaleOrderLineProductProductionServiceImpl;
import com.axelor.apps.production.service.SaleOrderLineProductionService;
import com.axelor.apps.production.service.SaleOrderLineProductionServiceImpl;
import com.axelor.apps.production.service.SaleOrderLineViewProductionService;
import com.axelor.apps.production.service.SaleOrderLineViewProductionServiceImpl;
import com.axelor.apps.production.service.SaleOrderProductionSyncService;
import com.axelor.apps.production.service.SaleOrderProductionSyncServiceImpl;
import com.axelor.apps.production.service.SolBomCustomizationService;
import com.axelor.apps.production.service.SolBomCustomizationServiceImpl;
import com.axelor.apps.production.service.SolBomUpdateService;
import com.axelor.apps.production.service.SolBomUpdateServiceImpl;
import com.axelor.apps.production.service.SolDetailsBomUpdateService;
import com.axelor.apps.production.service.SolDetailsBomUpdateServiceImpl;
import com.axelor.apps.production.service.SopService;
import com.axelor.apps.production.service.SopServiceImpl;
import com.axelor.apps.production.service.StockMoveLineProductionServiceImpl;
import com.axelor.apps.production.service.StockMoveMergingServiceProductionImpl;
import com.axelor.apps.production.service.StockMoveProductionService;
import com.axelor.apps.production.service.StockMoveServiceProductionImpl;
import com.axelor.apps.production.service.StockRulesSupplychainServiceProductionImpl;
import com.axelor.apps.production.service.SubSaleOrderLineComputeServiceProductionImpl;
import com.axelor.apps.production.service.TrackingNumberCompanyProductionServiceImpl;
import com.axelor.apps.production.service.WorkCenterService;
import com.axelor.apps.production.service.WorkCenterServiceImpl;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.app.AppProductionServiceImpl;
import com.axelor.apps.production.service.app.ConfiguratorServiceProductionImpl;
import com.axelor.apps.production.service.bomimport.BillOfMaterialImportLineService;
import com.axelor.apps.production.service.bomimport.BillOfMaterialImportLineServiceImpl;
import com.axelor.apps.production.service.bomimport.BillOfMaterialImportService;
import com.axelor.apps.production.service.bomimport.BillOfMaterialImportServiceImpl;
import com.axelor.apps.production.service.config.StockConfigProductionService;
import com.axelor.apps.production.service.configurator.ConfiguratorBomService;
import com.axelor.apps.production.service.configurator.ConfiguratorBomServiceImpl;
import com.axelor.apps.production.service.configurator.ConfiguratorCheckServiceProductionImpl;
import com.axelor.apps.production.service.configurator.ConfiguratorCreatorImportServiceProductionImpl;
import com.axelor.apps.production.service.configurator.ConfiguratorProdProcessLineService;
import com.axelor.apps.production.service.configurator.ConfiguratorProdProcessLineServiceImpl;
import com.axelor.apps.production.service.configurator.ConfiguratorProdProcessService;
import com.axelor.apps.production.service.configurator.ConfiguratorProdProcessServiceImpl;
import com.axelor.apps.production.service.configurator.ConfiguratorProdProductService;
import com.axelor.apps.production.service.configurator.ConfiguratorProdProductServiceImpl;
import com.axelor.apps.production.service.costsheet.CostSheetLineService;
import com.axelor.apps.production.service.costsheet.CostSheetLineServiceImpl;
import com.axelor.apps.production.service.costsheet.CostSheetService;
import com.axelor.apps.production.service.costsheet.CostSheetServiceImpl;
import com.axelor.apps.production.service.costsheet.UnitCostCalcLineService;
import com.axelor.apps.production.service.costsheet.UnitCostCalcLineServiceImpl;
import com.axelor.apps.production.service.costsheet.UnitCostCalculationService;
import com.axelor.apps.production.service.costsheet.UnitCostCalculationServiceImpl;
import com.axelor.apps.production.service.machine.MachineService;
import com.axelor.apps.production.service.machine.MachineServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderCheckStockMoveLineService;
import com.axelor.apps.production.service.manuforder.ManufOrderCheckStockMoveLineServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderCreateBarcodeService;
import com.axelor.apps.production.service.manuforder.ManufOrderCreateBarcodeServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderCreatePurchaseOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderCreatePurchaseOrderServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderCreateStockMoveLineService;
import com.axelor.apps.production.service.manuforder.ManufOrderCreateStockMoveLineServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderCreateStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderCreateStockMoveServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderGetStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderGetStockMoveServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderOutgoingStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderOutgoingStockMoveServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderOutsourceService;
import com.axelor.apps.production.service.manuforder.ManufOrderOutsourceServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderPlanService;
import com.axelor.apps.production.service.manuforder.ManufOrderPlanServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderPlanStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderPlanStockMoveServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderReservedQtyService;
import com.axelor.apps.production.service.manuforder.ManufOrderReservedQtyServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderResidualProductService;
import com.axelor.apps.production.service.manuforder.ManufOrderResidualProductServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderSetStockMoveLineService;
import com.axelor.apps.production.service.manuforder.ManufOrderSetStockMoveLineServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderTrackingNumberService;
import com.axelor.apps.production.service.manuforder.ManufOrderTrackingNumberServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderUpdateStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderUpdateStockMoveServiceImpl;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowService;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowServiceImpl;
import com.axelor.apps.production.service.observer.SaleOrderLineProductionObserver;
import com.axelor.apps.production.service.observer.SaleOrderProductionObserver;
import com.axelor.apps.production.service.operationorder.OperationOrderChartService;
import com.axelor.apps.production.service.operationorder.OperationOrderChartServiceImpl;
import com.axelor.apps.production.service.operationorder.OperationOrderCreateBarcodeService;
import com.axelor.apps.production.service.operationorder.OperationOrderCreateBarcodeServiceImpl;
import com.axelor.apps.production.service.operationorder.OperationOrderOutsourceService;
import com.axelor.apps.production.service.operationorder.OperationOrderOutsourceServiceImpl;
import com.axelor.apps.production.service.operationorder.OperationOrderPlanningService;
import com.axelor.apps.production.service.operationorder.OperationOrderPlanningServiceImpl;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderServiceImpl;
import com.axelor.apps.production.service.operationorder.OperationOrderStockMoveService;
import com.axelor.apps.production.service.operationorder.OperationOrderStockMoveServiceImpl;
import com.axelor.apps.production.service.operationorder.OperationOrderWorkflowService;
import com.axelor.apps.production.service.operationorder.OperationOrderWorkflowServiceImpl;
import com.axelor.apps.production.service.operationorder.planning.OperationOrderPlanningInfiniteCapacityService;
import com.axelor.apps.production.service.operationorder.planning.OperationOrderPlanningInfiniteCapacityServiceImpl;
import com.axelor.apps.production.service.productionorder.ProductionOrderSaleOrderMOGenerationService;
import com.axelor.apps.production.service.productionorder.ProductionOrderSaleOrderMOGenerationServiceImpl;
import com.axelor.apps.production.service.productionorder.ProductionOrderSaleOrderService;
import com.axelor.apps.production.service.productionorder.ProductionOrderSaleOrderServiceImpl;
import com.axelor.apps.production.service.productionorder.ProductionOrderService;
import com.axelor.apps.production.service.productionorder.ProductionOrderServiceImpl;
import com.axelor.apps.production.service.productionorder.ProductionOrderUpdateService;
import com.axelor.apps.production.service.productionorder.ProductionOrderUpdateServiceImpl;
import com.axelor.apps.production.service.productionorder.ProductionOrderWizardService;
import com.axelor.apps.production.service.productionorder.ProductionOrderWizardServiceImpl;
import com.axelor.apps.production.service.saleorder.onchange.SaleOrderOnLineChangeProductionServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderTypeSelectServiceImpl;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorImportServiceImpl;
import com.axelor.apps.sale.service.configurator.ConfiguratorServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineCostPriceComputeServiceImpl;
import com.axelor.apps.sale.service.saleorderline.subline.SubSaleOrderLineComputeServiceImpl;
import com.axelor.apps.stock.db.repo.ProductStockRepository;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.db.repo.StockMoveLineSupplychainRepository;
import com.axelor.apps.supplychain.db.repo.StockMoveSupplychainRepository;
import com.axelor.apps.supplychain.service.ConfiguratorCheckServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.MrpLineServiceImpl;
import com.axelor.apps.supplychain.service.MrpServiceImpl;
import com.axelor.apps.supplychain.service.ProductStockLocationServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderMergingServiceSupplyChainImpl;
import com.axelor.apps.supplychain.service.StockMoveLineServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.StockMoveMergingServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.StockMoveServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.StockRulesSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.TrackingNumberCompanySupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderComputeServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.saleorder.onchange.SaleOrderOnLineChangeSupplyChainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineDummySupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineOnChangeSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineProductSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineViewSupplychainServiceImpl;

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
    bind(StockRulesSupplychainServiceImpl.class)
        .to(StockRulesSupplychainServiceProductionImpl.class);
    bind(BillOfMaterialRepository.class).to(BillOfMaterialManagementRepository.class);
    bind(StockConfigService.class).to(StockConfigProductionService.class);
    bind(ConfiguratorBomService.class).to(ConfiguratorBomServiceImpl.class);
    bind(ConfiguratorProdProcessService.class).to(ConfiguratorProdProcessServiceImpl.class);
    bind(ConfiguratorProdProcessLineService.class).to(ConfiguratorProdProcessLineServiceImpl.class);
    bind(ConfiguratorServiceImpl.class).to(ConfiguratorServiceProductionImpl.class);
    bind(AppProductionService.class).to(AppProductionServiceImpl.class);
    bind(ProdProcessRepository.class).to(ProdProcessManagementRepository.class);
    bind(StockMoveLineSupplychainRepository.class).to(StockMoveLineProductionRepository.class);
    bind(ProdProcessLineService.class).to(ProdProcessLineServiceImpl.class);
    bind(ProdProductRepository.class).to(ProdProductProductionRepository.class);
    bind(RawMaterialRequirementService.class).to(RawMaterialRequirementServiceImpl.class);
    bind(RawMaterialRequirementRepository.class)
        .to(RawMaterialRequirementProductionRepository.class);
    bind(ProductionBatchRepository.class).to(ProductionBatchManagementRepository.class);
    bind(UnitCostCalculationRepository.class).to(UnitCostCalculationManagementRepository.class);
    bind(UnitCostCalculationService.class).to(UnitCostCalculationServiceImpl.class);
    bind(UnitCostCalcLineService.class).to(UnitCostCalcLineServiceImpl.class);
    bind(ProductStockRepository.class).to(ProductProductionRepository.class);
    bind(ConfiguratorCreatorImportServiceImpl.class)
        .to(ConfiguratorCreatorImportServiceProductionImpl.class);
    bind(ProductStockLocationServiceImpl.class).to(ProductionProductStockLocationServiceImpl.class);
    bind(StockMoveSupplychainRepository.class).to(StockMoveProductionRepository.class);
    bind(MrpForecastProductionService.class).to(MrpForecastProductionServiceImpl.class);
    bind(MpsWeeklyScheduleService.class).to(MpsWeeklyScheduleServiceImpl.class);
    bind(MpsChargeService.class).to(MpsChargeServiceImpl.class);
    bind(MachineRepository.class).to(MachineManagementRepository.class);
    bind(SopService.class).to(SopServiceImpl.class);
    bind(ManufOrderReservedQtyService.class).to(ManufOrderReservedQtyServiceImpl.class);
    bind(WorkCenterService.class).to(WorkCenterServiceImpl.class);
    bind(ConfiguratorProdProductService.class).to(ConfiguratorProdProductServiceImpl.class);
    bind(ManufOrderProductRestService.class).to(ManufOrderProductRestServiceImpl.class);
    bind(ManufOrderRestService.class).to(ManufOrderRestServiceImpl.class);
    bind(OperationOrderRestService.class).to(OperationOrderRestServiceImpl.class);
    bind(ManufOrderWorkflowService.class).to(ManufOrderWorkflowServiceImpl.class);
    bind(StockMoveServiceSupplychainImpl.class).to(StockMoveServiceProductionImpl.class);
    bind(StockMoveProductionService.class).to(StockMoveServiceProductionImpl.class);
    bind(MachineService.class).to(MachineServiceImpl.class);
    bind(OperationOrderWorkflowService.class).to(OperationOrderWorkflowServiceImpl.class);
    bind(OperationOrderPlanningService.class).to(OperationOrderPlanningServiceImpl.class);
    bind(BillOfMaterialLineService.class).to(BillOfMaterialLineServiceImpl.class);
    bind(BillOfMaterialImportService.class).to(BillOfMaterialImportServiceImpl.class);
    bind(BillOfMaterialImportRepository.class).to(BillOfMaterialImportManagementRepository.class);
    bind(BillOfMaterialImportLineService.class).to(BillOfMaterialImportLineServiceImpl.class);
    bind(StockMoveMergingServiceSupplychainImpl.class)
        .to(StockMoveMergingServiceProductionImpl.class);
    bind(OperationOrderChartService.class).to(OperationOrderChartServiceImpl.class);
    bind(BillOfMaterialComputeNameService.class).to(BillOfMaterialComputeNameServiceImpl.class);
    bind(ManufOrderOutgoingStockMoveService.class).to(ManufOrderOutgoingStockMoveServiceImpl.class);
    bind(ManufOrderStockMoveService.class).to(ManufOrderStockMoveServiceImpl.class);
    bind(ManufOrderOutsourceService.class).to(ManufOrderOutsourceServiceImpl.class);
    bind(ProdProcessLineOutsourceService.class).to(ProdProcessLineOutsourceServiceImpl.class);
    bind(OperationOrderOutsourceService.class).to(OperationOrderOutsourceServiceImpl.class);
    bind(ProdProcessOutsourceService.class).to(ProdProcessOutsourceServiceImpl.class);
    bind(ManufOrderCreatePurchaseOrderService.class)
        .to(ManufOrderCreatePurchaseOrderServiceImpl.class);
    bind(ManufOrderPlanService.class).to(ManufOrderPlanServiceImpl.class);
    bind(ProductionOrderSaleOrderMOGenerationService.class)
        .to(ProductionOrderSaleOrderMOGenerationServiceImpl.class);
    bind(ProductionOrderUpdateService.class).to(ProductionOrderUpdateServiceImpl.class);
    bind(ManufOrderCheckStockMoveLineService.class)
        .to(ManufOrderCheckStockMoveLineServiceImpl.class);
    bind(ManufOrderSetStockMoveLineService.class).to(ManufOrderSetStockMoveLineServiceImpl.class);
    bind(ManufOrderGetStockMoveService.class).to(ManufOrderGetStockMoveServiceImpl.class);
    bind(ManufOrderPlanStockMoveService.class).to(ManufOrderPlanStockMoveServiceImpl.class);
    bind(ManufOrderResidualProductService.class).to(ManufOrderResidualProductServiceImpl.class);
    bind(ManufOrderCreateStockMoveService.class).to(ManufOrderCreateStockMoveServiceImpl.class);
    bind(ManufOrderCreateStockMoveLineService.class)
        .to(ManufOrderCreateStockMoveLineServiceImpl.class);
    bind(ManufOrderUpdateStockMoveService.class).to(ManufOrderUpdateStockMoveServiceImpl.class);
    bind(OperationOrderStockMoveService.class).to(OperationOrderStockMoveServiceImpl.class);
    bind(OperationOrderPlanningInfiniteCapacityService.class)
        .to(OperationOrderPlanningInfiniteCapacityServiceImpl.class);
    bind(BillOfMaterialMrpLineService.class).to(BillOfMaterialMrpLineServiceImpl.class);
    bind(StockMoveLineServiceSupplychainImpl.class).to(StockMoveLineProductionServiceImpl.class);
    bind(ManufOrderTrackingNumberService.class).to(ManufOrderTrackingNumberServiceImpl.class);
    bind(PurchaseOrderMergingServiceSupplyChainImpl.class)
        .to(PurchaseOrderMergingServiceProductionImpl.class);
    bind(SaleOrderLineProductProductionService.class)
        .to(SaleOrderLineProductProductionServiceImpl.class);
    bind(SaleOrderLineProductSupplychainServiceImpl.class)
        .to(SaleOrderLineProductProductionServiceImpl.class);
    bind(SaleOrderLineViewSupplychainServiceImpl.class)
        .to(SaleOrderLineViewProductionServiceImpl.class);
    bind(ManufOrderCreateBarcodeService.class).to(ManufOrderCreateBarcodeServiceImpl.class);
    bind(OperationOrderCreateBarcodeService.class).to(OperationOrderCreateBarcodeServiceImpl.class);
    bind(SaleOrderLineDomainProductionService.class)
        .to(SaleOrderLineDomainProductionServiceImpl.class);
    bind(SaleOrderLineViewProductionService.class).to(SaleOrderLineViewProductionServiceImpl.class);
    bind(SaleOrderLineProductionObserver.class);
    bind(SaleOrderProductionObserver.class);
    bind(SaleOrderConfirmProductionService.class).to(SaleOrderConfirmProductionServiceImpl.class);
    bind(ProdProcessWorkflowService.class).to(ProdProcessWorkflowServiceImpl.class);
    bind(SaleOrderLineBomService.class).to(SaleOrderLineBomServiceImpl.class);
    bind(SaleOrderLineBomLineMappingService.class).to(SaleOrderLineBomLineMappingServiceImpl.class);
    bind(SaleOrderOnLineChangeSupplyChainServiceImpl.class)
        .to(SaleOrderOnLineChangeProductionServiceImpl.class);
    bind(ProdProductAttrsService.class).to(ProdProductAttrsServiceImpl.class);
    bind(ProdProductService.class).to(ProdProductServiceImpl.class);
    bind(SaleOrderLineBomSyncService.class).to(SaleOrderLineBomSyncServiceImpl.class);
    bind(SaleOrderLineDetailsService.class).to(SaleOrderLineDetailsServiceImpl.class);
    bind(SaleOrderLineDetailsRepository.class).to(SaleOrderLineDetailsManagementRepository.class);
    bind(SaleOrderLineDetailsService.class).to(SaleOrderLineDetailsServiceImpl.class);
    bind(SaleOrderLineDetailsRepository.class).to(SaleOrderLineDetailsManagementRepository.class);
    bind(SaleOrderLineDetailsBomService.class).to(SaleOrderLineDetailsBomServiceImpl.class);
    bind(SaleOrderLineDetailsBomLineMappingService.class)
        .to(SaleOrderLineDetailsBomLineMappingServiceImpl.class);
    bind(SaleOrderComputeServiceSupplychainImpl.class)
        .to(SaleOrderComputeServiceProductionImpl.class);
    bind(SubSaleOrderLineComputeServiceImpl.class)
        .to(SubSaleOrderLineComputeServiceProductionImpl.class);
    bind(BomLineCreationService.class).to(BomLineCreationServiceImpl.class);
    bind(SolBomCustomizationService.class).to(SolBomCustomizationServiceImpl.class);
    bind(SolBomUpdateService.class).to(SolBomUpdateServiceImpl.class);
    bind(SolDetailsBomUpdateService.class).to(SolDetailsBomUpdateServiceImpl.class);
    bind(PurchaseOrderTypeSelectServiceImpl.class)
        .to(PurchaseOrderTypeSelectProductionServiceImpl.class);
    bind(SaleOrderLineDetailsBomSyncService.class).to(SaleOrderLineDetailsBomSyncServiceImpl.class);
    bind(SaleOrderProductionSyncService.class).to(SaleOrderProductionSyncServiceImpl.class);
    bind(TrackingNumberCompanySupplychainServiceImpl.class)
        .to(TrackingNumberCompanyProductionServiceImpl.class);
    bind(SaleOrderLineCostPriceComputeServiceImpl.class)
        .to(SaleOrderLineCostPriceComputeProductionServiceImpl.class);
    bind(ConfiguratorCheckServiceSupplychainImpl.class)
        .to(ConfiguratorCheckServiceProductionImpl.class);
    bind(BillOfMaterialRemoveService.class).to(BillOfMaterialRemoveServiceImpl.class);
    bind(BillOfMaterialCheckService.class).to(BillOfMaterialCheckServiceImpl.class);
    bind(SaleOrderLineDetailsPriceService.class).to(SaleOrderLineDetailsPriceServiceImpl.class);
    bind(ProdProcessLineComputationService.class).to(ProdProcessLineComputationServiceImpl.class);
    bind(SaleOrderLineOnChangeSupplychainServiceImpl.class)
        .to(SaleOrderLineOnChangeProductionServiceImpl.class);
    bind(SaleOrderLineProductionService.class).to(SaleOrderLineProductionServiceImpl.class);
    bind(SaleOrderLineDummySupplychainServiceImpl.class)
        .to(SaleOrderLineDummyProductionServiceImpl.class);
    bind(ProdProcessComputationService.class).to(ProdProcessComputationServiceImpl.class);
    bind(BillOfMaterialDummyService.class).to(BillOfMaterialDummyServiceImpl.class);
  }
}
