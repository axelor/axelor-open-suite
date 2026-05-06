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
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ProductCategoryService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.BillOfMaterialMrpLineService;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.MrpServiceProductionImpl;
import com.axelor.apps.production.service.ProdProcessComputationService;
import com.axelor.apps.production.service.ProdProcessLineComputationService;
import com.axelor.apps.production.service.ProdProcessLineService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockHistoryLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.stock.service.StockRulesService;
import com.axelor.apps.supplychain.db.Mrp;
import com.axelor.apps.supplychain.db.MrpLineType;
import com.axelor.apps.supplychain.db.repo.MrpForecastRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineTypeRepository;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.apps.supplychain.service.MrpLineSaleOrderService;
import com.axelor.apps.supplychain.service.MrpLineService;
import com.axelor.apps.supplychain.service.MrpLineTypeService;
import com.axelor.apps.supplychain.service.MrpSaleOrderCheckLateSaleService;
import com.axelor.db.JPA;
import com.axelor.message.service.MailMessageService;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MrpServiceMaintenanceImpl extends MrpServiceProductionImpl {

  @Inject
  public MrpServiceMaintenanceImpl(
      MrpRepository mrpRepository,
      StockLocationRepository stockLocationRepository,
      ProductRepository productRepository,
      StockLocationLineRepository stockLocationLineRepository,
      MrpLineTypeRepository mrpLineTypeRepository,
      PurchaseOrderLineRepository purchaseOrderLineRepository,
      SaleOrderLineRepository saleOrderLineRepository,
      MrpLineRepository mrpLineRepository,
      StockRulesService stockRulesService,
      MrpLineService mrpLineService,
      MrpForecastRepository mrpForecastRepository,
      ProductCategoryService productCategoryService,
      StockLocationService stockLocationService,
      MailMessageService mailMessageService,
      UnitConversionService unitConversionService,
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      AppPurchaseService appPurchaseService,
      StockHistoryLineRepository stockHistoryLineRepository,
      MrpSaleOrderCheckLateSaleService mrpSaleOrderCheckLateSaleService,
      MrpLineTypeService mrpLineTypeService,
      MrpLineSaleOrderService mrpLineSaleOrderService,
      ManufOrderRepository manufOrderRepository,
      ProductCompanyService productCompanyService,
      BillOfMaterialService billOfMaterialService,
      AppProductionService appProductionService,
      ProdProcessLineService prodProcessLineService,
      ProdProcessLineComputationService prodProcessLineComputationService,
      ProdProcessComputationService prodProcessComputationService,
      BillOfMaterialMrpLineService billOfMaterialMrpLineService) {
    super(
        mrpRepository,
        stockLocationRepository,
        productRepository,
        stockLocationLineRepository,
        mrpLineTypeRepository,
        purchaseOrderLineRepository,
        saleOrderLineRepository,
        mrpLineRepository,
        stockRulesService,
        mrpLineService,
        mrpForecastRepository,
        productCategoryService,
        stockLocationService,
        mailMessageService,
        unitConversionService,
        appBaseService,
        appSaleService,
        appPurchaseService,
        stockHistoryLineRepository,
        mrpSaleOrderCheckLateSaleService,
        mrpLineTypeService,
        mrpLineSaleOrderService,
        manufOrderRepository,
        productCompanyService,
        billOfMaterialService,
        appProductionService,
        prodProcessLineService,
        prodProcessLineComputationService,
        prodProcessComputationService,
        billOfMaterialMrpLineService);
  }

  @Override
  protected void completeMrp(Mrp mrp) throws AxelorException {
    super.completeMrp(mrp);
    this.createMaintenanceOrderMrpLines();
  }

  @Override
  protected void fillMrpLinesForProductMap(Map<Long, Integer> productMap) throws AxelorException {
    super.fillMrpLinesForProductMap(productMap);
    this.createMaintenanceOrderMrpLines();
  }

  protected void createMaintenanceOrderMrpLines() throws AxelorException {

    MrpLineType maintenanceOrderNeedMrpLineType =
        mrpLineTypeService.getMrpLineType(
            MrpLineTypeRepository.ELEMENT_MAINTENANCE_ORDER_NEED, mrp.getMrpTypeSelect());

    if (maintenanceOrderNeedMrpLineType == null) {
      return;
    }

    String statusSelect = maintenanceOrderNeedMrpLineType.getStatusSelect();
    List<Integer> statusList = StringHelper.getIntegerList(statusSelect);

    if (statusList.isEmpty()) {
      statusList.add(ManufOrderRepository.STATUS_PLANNED);
    }

    List<ManufOrder> maintenanceOrderList =
        manufOrderRepository
            .all()
            .filter(
                "self.typeSelect = :typeSelect "
                    + "AND self.prodProcess IS NOT NULL "
                    + "AND self.prodProcess.stockLocation IN (:stockLocations) "
                    + "AND self.statusSelect IN (:statusList)")
            .bind("typeSelect", ManufOrderRepository.TYPE_MAINTENANCE)
            .bind("stockLocations", this.stockLocationList)
            .bind("statusList", statusList)
            .fetch();

    for (ManufOrder maintenanceOrder : maintenanceOrderList) {
      this.createMaintenanceOrderMrpLines(
          mrpRepository.find(mrp.getId()),
          manufOrderRepository.find(maintenanceOrder.getId()),
          mrpLineTypeRepository.find(maintenanceOrderNeedMrpLineType.getId()));
      JPA.clear();
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createMaintenanceOrderMrpLines(
      Mrp mrp, ManufOrder maintenanceOrder, MrpLineType maintenanceOrderNeedMrpLineType)
      throws AxelorException {

    StockLocation stockLocation = maintenanceOrder.getProdProcess().getStockLocation();

    LocalDate maturityDate = null;
    if (maintenanceOrder.getPlannedStartDateT() != null) {
      maturityDate = maintenanceOrder.getPlannedStartDateT().toLocalDate();
    } else if (maintenanceOrder.getPlannedEndDateT() != null) {
      maturityDate = maintenanceOrder.getPlannedEndDateT().toLocalDate();
    }

    maturityDate = this.computeMaturityDate(maturityDate, maintenanceOrderNeedMrpLineType);

    if (!this.isBeforeEndDate(maturityDate)
        && !maintenanceOrderNeedMrpLineType.getIgnoreEndDate()) {
      return;
    }

    List<StockMoveLine> stockMoveLineList = new ArrayList<>();
    for (StockMove stockMove : maintenanceOrder.getInStockMoveList()) {
      stockMoveLineList.addAll(stockMove.getStockMoveLineList());
    }

    // Pass null for autoInjectLevelReference: maintenance orders have no parent product to anchor
    // BOM level assignment, and consumed components are tracked directly via planned
    // StockMoveLines.
    createConsumedNeedMrpLines(
        mrp,
        stockMoveLineList,
        maintenanceOrderNeedMrpLineType,
        maturityDate,
        stockLocation,
        maintenanceOrder,
        null);
  }
}
