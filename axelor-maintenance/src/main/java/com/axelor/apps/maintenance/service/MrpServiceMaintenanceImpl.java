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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ProductCategoryService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
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
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.stock.service.StockRulesService;
import com.axelor.apps.supplychain.db.Mrp;
import com.axelor.apps.supplychain.db.MrpLine;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    if (processedManufOrderIds != null && !processedManufOrderIds.add(maintenanceOrder.getId())) {
      return;
    }

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

    // Custom BOM explosion: walk DOWN the BOM tree of each planned SML product and accumulate
    // cascaded quantities for products that are in the MRP's initial filter (getProductList()).
    // Only filter products generate MrpLines from the maintenance order, so intermediate cascade
    // lines (e.g., for filter=C1 with an OM consuming PF, no PF/PSF lines, just C1 qty 4).
    Set<Long> filterProductIds =
        getProductList().stream().map(Product::getId).collect(Collectors.toSet());
    Company company = mrp.getStockLocation() != null ? mrp.getStockLocation().getCompany() : null;
    int qtyScale = appBaseService.getNbDecimalDigitForQty();
    Map<Long, BigDecimal> qtyByFilterProduct = new HashMap<>();

    for (StockMove stockMove : maintenanceOrder.getInStockMoveList()) {
      if (stockMove.getStatusSelect() == null
          || stockMove.getStatusSelect() != StockMoveRepository.STATUS_PLANNED) {
        continue;
      }
      for (StockMoveLine sml : stockMove.getStockMoveLineList()) {
        Product smlProduct = sml.getProduct();
        if (smlProduct == null) {
          continue;
        }
        BigDecimal smlQty = sml.getQty();
        if (sml.getUnit() != null
            && smlProduct.getUnit() != null
            && !sml.getUnit().equals(smlProduct.getUnit())) {
          smlQty =
              unitConversionService.convert(
                  sml.getUnit(), smlProduct.getUnit(), smlQty, qtyScale, smlProduct);
        }
        smlQty = smlQty.setScale(qtyScale, RoundingMode.HALF_UP);
        explodeBomForFilterProducts(
            smlProduct, smlQty, filterProductIds, company, qtyScale, qtyByFilterProduct, 0);
      }
    }

    for (Map.Entry<Long, BigDecimal> entry : qtyByFilterProduct.entrySet()) {
      Product product = productRepository.find(entry.getKey());
      BigDecimal qty = entry.getValue();
      MrpLine mrpLine =
          createMrpLine(
              mrp,
              product,
              maintenanceOrderNeedMrpLineType,
              qty,
              maturityDate,
              BigDecimal.ZERO,
              stockLocation,
              maintenanceOrder);
      if (mrpLine != null) {
        mrpLineRepository.save(mrpLine);
      }
    }
  }

  /**
   * Recursively walks DOWN the default BOM of {@code product}, multiplying quantities by each BOM
   * line's qty, and accumulates the cumulative qty in {@code result} for every product that is in
   * {@code filterProductIds}. {@code depth} guards against excessively deep or cyclic BOMs (same
   * 100-level limit as production's standard cascade).
   */
  protected void explodeBomForFilterProducts(
      Product product,
      BigDecimal qty,
      Set<Long> filterProductIds,
      Company company,
      int qtyScale,
      Map<Long, BigDecimal> result,
      int depth)
      throws AxelorException {

    if (product == null || depth > 100) {
      return;
    }

    if (filterProductIds.contains(product.getId())) {
      result.merge(product.getId(), qty, BigDecimal::add);
    }

    BillOfMaterial bom = billOfMaterialService.getDefaultBOM(product, company);
    if (bom == null || bom.getBillOfMaterialLineList() == null) {
      return;
    }

    for (BillOfMaterialLine line : bom.getBillOfMaterialLineList()) {
      Product subProduct = line.getProduct();
      if (subProduct == null || !isMrpProduct(subProduct) || line.getHasNoManageStock()) {
        continue;
      }
      BigDecimal lineQty = line.getQty() == null ? BigDecimal.ZERO : line.getQty();
      // BigDecimal.multiply sums the scales of the operands, which can quickly exceed
      // MrpLine.qty's validation precision (10 fraction digits). Cap the scale at the configured
      // qty precision so the persisted value stays within bounds.
      BigDecimal subQty = qty.multiply(lineQty).setScale(qtyScale, RoundingMode.HALF_UP);
      explodeBomForFilterProducts(
          subProduct, subQty, filterProductIds, company, qtyScale, result, depth + 1);
    }
  }
}
