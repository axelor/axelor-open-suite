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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCategoryService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.StockRules;
import com.axelor.apps.stock.db.repo.StockHistoryLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.db.repo.StockRulesRepository;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.stock.service.StockRulesService;
import com.axelor.apps.supplychain.db.Mrp;
import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.db.MrpLineOrigin;
import com.axelor.apps.supplychain.db.MrpLineType;
import com.axelor.apps.supplychain.db.repo.MrpForecastRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineTypeRepository;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.apps.supplychain.service.MrpLineService;
import com.axelor.apps.supplychain.service.MrpLineTypeService;
import com.axelor.apps.supplychain.service.MrpSaleOrderCheckLateSaleService;
import com.axelor.apps.supplychain.service.MrpServiceImpl;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.message.service.MailMessageService;
import com.axelor.utils.StringTool;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MrpServiceProductionImpl extends MrpServiceImpl {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected ManufOrderRepository manufOrderRepository;

  protected ProductCompanyService productCompanyService;

  protected BillOfMaterialService billOfMaterialService;

  protected AppProductionService appProductionService;

  protected ProdProcessLineService prodProcessLineService;

  @Inject
  public MrpServiceProductionImpl(
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
      ManufOrderRepository manufOrderRepository,
      ProductCompanyService productCompanyService,
      BillOfMaterialService billOfMaterialService,
      AppProductionService appProductionService,
      ProdProcessLineService prodProcessLineService) {
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
        mrpLineTypeService);
    this.manufOrderRepository = manufOrderRepository;
    this.productCompanyService = productCompanyService;
    this.billOfMaterialService = billOfMaterialService;
    this.appProductionService = appProductionService;
    this.prodProcessLineService = prodProcessLineService;
  }

  @Override
  protected void completeMrp(Mrp mrp) throws AxelorException {

    super.completeMrp(mrp);

    if (appProductionService.isApp("production")) {
      this.createManufOrderMrpLines();
      this.createMPSLines();
    }
  }

  // Manufacturing order AND manufacturing order need
  protected void createManufOrderMrpLines() throws AxelorException {

    MrpLineType manufOrderMrpLineType =
        mrpLineTypeService.getMrpLineType(
            MrpLineTypeRepository.ELEMENT_MANUFACTURING_ORDER, mrp.getMrpTypeSelect());

    if (manufOrderMrpLineType == null) {
      return;
    }

    MrpLineType manufOrderNeedMrpLineType =
        mrpLineTypeService.getMrpLineType(
            MrpLineTypeRepository.ELEMENT_MANUFACTURING_ORDER_NEED, mrp.getMrpTypeSelect());

    String statusSelect = manufOrderMrpLineType.getStatusSelect();
    List<Integer> statusList = StringTool.getIntegerList(statusSelect);

    if (statusList.isEmpty()) {
      statusList.add(ManufOrderRepository.STATUS_FINISHED);
    }

    List<ManufOrder> manufOrderList =
        manufOrderRepository
            .all()
            .filter(
                "self.product.id in (?1) AND (self.prodProcess.stockLocation in (?2) OR "
                    + "self.prodProcess.producedProductStockLocation in (?2)) "
                    + "AND self.statusSelect IN (?3)",
                this.productMap.keySet(),
                this.stockLocationList,
                statusList)
            .fetch();

    for (ManufOrder manufOrder : manufOrderList) {

      this.createManufOrderMrpLines(
          mrpRepository.find(mrp.getId()),
          manufOrderRepository.find(manufOrder.getId()),
          mrpLineTypeRepository.find(manufOrderMrpLineType.getId()),
          mrpLineTypeRepository.find(manufOrderNeedMrpLineType.getId()));
      JPA.clear();
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createManufOrderMrpLines(
      Mrp mrp,
      ManufOrder manufOrder,
      MrpLineType manufOrderMrpLineType,
      MrpLineType manufOrderNeedMrpLineType)
      throws AxelorException {

    StockLocation stockLocation = manufOrder.getProdProcess().getStockLocation();

    LocalDate maturityDate = null;

    if (manufOrder.getPlannedEndDateT() != null) {
      maturityDate = manufOrder.getPlannedEndDateT().toLocalDate();
    } else if (manufOrder.getPlannedStartDateT() != null) {
      maturityDate = manufOrder.getPlannedStartDateT().toLocalDate();
    }

    maturityDate = this.computeMaturityDate(maturityDate, manufOrderMrpLineType);

    for (ProdProduct prodProduct : manufOrder.getToProduceProdProductList()) {

      Product product = prodProduct.getProduct();

      if ((this.isBeforeEndDate(maturityDate) || manufOrderMrpLineType.getIgnoreEndDate())
          && this.isMrpProduct(product)) {
        Unit unit = prodProduct.getUnit();
        BigDecimal qty = computeQtyLeftToProduce(manufOrder, prodProduct);
        if (!unit.equals(product.getUnit())) {
          qty =
              unitConversionService.convert(prodProduct.getUnit(), unit, qty, qty.scale(), product);
        }
        MrpLine mrpLine =
            this.createMrpLine(
                mrp,
                product,
                manufOrderMrpLineType,
                qty,
                maturityDate,
                BigDecimal.ZERO,
                stockLocation,
                manufOrder);
        if (mrpLine != null) {
          mrpLineRepository.save(mrpLine);
        }
      }
    }

    if (manufOrderNeedMrpLineType == null) {
      return;
    }

    if (manufOrder.getIsConsProOnOperation()) {
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
        for (ProdProduct prodProduct : operationOrder.getToConsumeProdProductList()) {

          Product product = prodProduct.getProduct();

          if (this.isMrpProduct(product)) {

            if (operationOrder.getPlannedEndDateT() != null) {
              maturityDate = operationOrder.getPlannedEndDateT().toLocalDate();
            } else if (operationOrder.getPlannedStartDateT() != null) {
              maturityDate = operationOrder.getPlannedStartDateT().toLocalDate();
            }

            maturityDate = this.computeMaturityDate(maturityDate, manufOrderNeedMrpLineType);

            Unit unit = product.getUnit();
            BigDecimal qty = computeQtyLeftToConsume(operationOrder, prodProduct);
            if (!unit.equals(prodProduct.getUnit())) {
              qty =
                  unitConversionService.convert(
                      prodProduct.getUnit(), unit, qty, qty.scale(), product);
            }

            MrpLine mrpLine =
                this.createMrpLine(
                    mrp,
                    prodProduct.getProduct(),
                    manufOrderNeedMrpLineType,
                    qty,
                    maturityDate,
                    BigDecimal.ZERO,
                    stockLocation,
                    operationOrder);
            if (mrpLine != null) {
              mrpLineRepository.save(mrpLine);
            }
          }
        }
      }
    } else {
      for (ProdProduct prodProduct : manufOrder.getToConsumeProdProductList()) {

        Product product = prodProduct.getProduct();

        if (this.isMrpProduct(product)) {

          // A component of a manuf order that is not loaded on MRP because there is no default
          // BOM or
          // because the component of manuf order is not a component of the bill of materials, we
          // add it with the level of manuf order product + 1.
          if (!this.productMap.containsKey(product.getId())) {
            this.assignProductAndLevel(product, manufOrder.getProduct());
            this.createAvailableStockMrpLine(
                product, manufOrder.getProdProcess().getStockLocation());
          }

          Unit unit = product.getUnit();
          BigDecimal qty = computeQtyLeftToConsume(manufOrder, prodProduct);
          if (!unit.equals(prodProduct.getUnit())) {
            qty =
                unitConversionService.convert(
                    prodProduct.getUnit(), unit, qty, qty.scale(), product);
          }

          MrpLine mrpLine =
              this.createMrpLine(
                  mrp,
                  product,
                  manufOrderNeedMrpLineType,
                  qty,
                  maturityDate,
                  BigDecimal.ZERO,
                  stockLocation,
                  manufOrder);
          if (mrpLine != null) {
            mrpLineRepository.save(mrpLine);
          }
        }
      }
    }
  }

  protected BigDecimal computeQtyLeftToConsume(ManufOrder manufOrder, ProdProduct prodProduct) {
    return computeQtyLeftToProcess(manufOrder.getConsumedStockMoveLineList(), prodProduct);
  }

  protected BigDecimal computeQtyLeftToConsume(
      OperationOrder operationOrder, ProdProduct prodProduct) {
    return computeQtyLeftToProcess(operationOrder.getConsumedStockMoveLineList(), prodProduct);
  }

  protected BigDecimal computeQtyLeftToProduce(ManufOrder manufOrder, ProdProduct prodProduct) {
    return computeQtyLeftToProcess(manufOrder.getProducedStockMoveLineList(), prodProduct);
  }

  protected BigDecimal computeQtyLeftToProcess(
      List<StockMoveLine> stockMoveLineList, ProdProduct prodProduct) {
    BigDecimal qtyToProcess = prodProduct.getQty();
    BigDecimal processedQty =
        stockMoveLineList.stream()
            .filter(
                stockMoveLine ->
                    stockMoveLine.getStockMove().getStatusSelect()
                            == StockMoveRepository.STATUS_REALIZED
                        && stockMoveLine.getProduct().equals(prodProduct.getProduct()))
            .map(StockMoveLine::getQty)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    return qtyToProcess.subtract(processedQty);
  }

  protected void createMPSLines() throws AxelorException {

    MrpLineType mpsNeedMrpLineType =
        mrpLineTypeService.getMrpLineType(
            MrpLineTypeRepository.ELEMENT_MASTER_PRODUCTION_SCHEDULING, mrp.getMrpTypeSelect());

    if (mpsNeedMrpLineType == null || mrp.getMrpTypeSelect() != MrpRepository.MRP_TYPE_MRP) {
      return;
    }

    List<MrpLine> mpsMrpLineList =
        mrpLineRepository
            .all()
            .filter(
                "self.product.id in (?1) AND self.stockLocation in (?2) AND self.mrp.mrpTypeSelect = ?3 "
                    + "AND self.mrp.statusSelect = ?4 AND self.mrpLineType.elementSelect = ?5 AND self.maturityDate >= ?6 AND (?7 is true OR self.maturityDate <= ?8)",
                this.productMap.keySet(),
                this.stockLocationList,
                MrpRepository.MRP_TYPE_MPS,
                MrpRepository.STATUS_CALCULATION_ENDED,
                MrpLineTypeRepository.ELEMENT_MASTER_PRODUCTION_SCHEDULING,
                today.atStartOfDay(),
                mrp.getEndDate() == null,
                mrp.getEndDate())
            .fetch();

    for (MrpLine mpsMrpLine : mpsMrpLineList) {

      this.createMpsMrpLines(
          mrpRepository.find(mrp.getId()),
          mrpLineRepository.find(mpsMrpLine.getId()),
          mrpLineTypeRepository.find(mpsNeedMrpLineType.getId()));
      JPA.clear();
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void createMpsMrpLines(Mrp mrp, MrpLine mpsMrpLine, MrpLineType mpsMrpLineType)
      throws AxelorException {

    Product product = mpsMrpLine.getProduct();

    if (this.isMrpProduct(product)) {
      MrpLine mrpLine =
          this.createMrpLine(
              mrp,
              product,
              mpsMrpLineType,
              mpsMrpLine.getQty(),
              mpsMrpLine.getMaturityDate(),
              BigDecimal.ZERO,
              mpsMrpLine.getStockLocation(),
              mrp);
      if (mrpLine != null) {
        mrpLineRepository.save(mrpLine);
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  protected void createProposalMrpLine(
      Mrp mrp,
      Product product,
      MrpLineType mrpLineType,
      BigDecimal reorderQty,
      StockLocation stockLocation,
      LocalDate maturityDate,
      List<MrpLineOrigin> mrpLineOriginList,
      String relatedToSelectName)
      throws AxelorException {

    Company company = mrp.getStockLocation().getCompany();
    BillOfMaterial defaultBillOfMaterial = billOfMaterialService.getDefaultBOM(product, company);

    if (appProductionService.isApp("production")
        && mrpLineType.getElementSelect() == MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL
        && defaultBillOfMaterial != null) {
      maturityDate = updateMaturityDate(maturityDate, defaultBillOfMaterial, reorderQty);
    }

    super.createProposalMrpLine(
        mrp,
        product,
        mrpLineType,
        reorderQty,
        stockLocation,
        maturityDate,
        mrpLineOriginList,
        relatedToSelectName);

    if (!appProductionService.isApp("production")) {
      return;
    }

    if (mrpLineType.getElementSelect() == MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL
        && defaultBillOfMaterial != null) {

      MrpLineType manufProposalNeedMrpLineType =
          mrpLineTypeService.getMrpLineType(
              MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL_NEED, mrp.getMrpTypeSelect());

      if (manufProposalNeedMrpLineType == null) {
        return;
      }

      for (BillOfMaterial billOfMaterial : defaultBillOfMaterial.getBillOfMaterialSet()) {

        Product subProduct = billOfMaterial.getProduct();

        if (this.isMrpProduct(subProduct)) {
          super.createProposalMrpLine(
              mrp,
              subProduct,
              manufProposalNeedMrpLineType,
              reorderQty
                  .multiply(billOfMaterial.getQty())
                  .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP),
              stockLocation,
              maturityDate,
              mrpLineOriginList,
              relatedToSelectName);
        }
      }
    }
  }

  /**
   * Update maturiy date because current currenty date do not take into account the duration of the
   * entrire process of manuf order
   *
   * @param maturityDate
   * @param defaultBillOfMaterial
   * @param reorderQty
   * @return
   * @throws AxelorException
   */
  protected LocalDate updateMaturityDate(
      LocalDate maturityDate, BillOfMaterial defaultBillOfMaterial, BigDecimal reorderQty)
      throws AxelorException {

    long totalDuration = 0;
    if (defaultBillOfMaterial.getProdProcess() != null) {
      for (ProdProcessLine prodProcessLine :
          defaultBillOfMaterial.getProdProcess().getProdProcessLineList()) {
        totalDuration +=
            prodProcessLineService.computeEntireCycleDuration(prodProcessLine, reorderQty);
      }
    }
    // If days should be rounded to a upper value
    if (totalDuration != 0 && totalDuration % TimeUnit.DAYS.toSeconds(1) != 0) {
      return maturityDate.minusDays(TimeUnit.SECONDS.toDays(totalDuration) + 1);
    }

    return maturityDate.minusDays(TimeUnit.SECONDS.toDays(totalDuration));
  }

  /**
   * Returns the type of an mrp proposal.
   *
   * <p>First checks for a stock rule, then if there isn't one, checks for the
   * procurementMethodSelect field of the product. If the product can be both bought and produced, a
   * manufacturing order is generated.
   */
  @Override
  protected MrpLineType getMrpLineTypeForProposal(
      StockRules stockRules, Product product, Company company) throws AxelorException {

    if (!appProductionService.isApp("production")) {
      return super.getMrpLineTypeForProposal(stockRules, product, company);
    }

    if (mrp.getMrpTypeSelect() == MrpRepository.MRP_TYPE_MPS) {
      return mrpLineTypeService.getMrpLineType(
          MrpLineTypeRepository.ELEMENT_MASTER_PRODUCTION_SCHEDULING, mrp.getMrpTypeSelect());
    } else {
      if (stockRules != null) {
        if (stockRules.getOrderAlertSelect() == StockRulesRepository.ORDER_ALERT_PRODUCTION_ORDER) {
          return mrpLineTypeService.getMrpLineType(
              MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL, mrp.getMrpTypeSelect());
        } else {
          return mrpLineTypeService.getMrpLineType(
              MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL, mrp.getMrpTypeSelect());
        }
      }

      if (ProductRepository.PROCUREMENT_METHOD_BUY.equals(
          ((String) productCompanyService.get(product, "procurementMethodSelect", company)))) {
        return mrpLineTypeService.getMrpLineType(
            MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL, mrp.getMrpTypeSelect());
      } else {
        return mrpLineTypeService.getMrpLineType(
            MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL, mrp.getMrpTypeSelect());
      }
    }
  }

  @Override
  protected boolean isProposalElement(MrpLineType mrpLineType) {

    if (!appProductionService.isApp("production")) {
      return super.isProposalElement(mrpLineType);
    }

    if (mrpLineType.getElementSelect() == MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL
        || mrpLineType.getElementSelect() == MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL
        || mrpLineType.getElementSelect()
            == MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL_NEED
        || (mrpLineType.getElementSelect()
                == MrpLineTypeRepository.ELEMENT_MASTER_PRODUCTION_SCHEDULING
            && mrp.getMrpTypeSelect() == MrpRepository.MRP_TYPE_MPS)) {

      return true;
    }

    return false;
  }

  @Override
  protected void assignProductAndLevel(Product product) throws AxelorException {

    if (!appProductionService.isApp("production")) {
      super.assignProductAndLevel(product);
      return;
    }
    Company company = mrp.getStockLocation().getCompany();
    BillOfMaterial billOfMaterial = billOfMaterialService.getDefaultBOM(product, company);

    if (billOfMaterial != null && mrp.getMrpTypeSelect() == MrpRepository.MRP_TYPE_MRP) {
      this.assignProductLevel(billOfMaterial, 0);
    } else {
      log.debug("Add product: {}", product.getFullName());
      this.productMap.put(product.getId(), this.getMaxLevel(product, 0));
    }
  }

  public int getMaxLevel(Product product, int level) {

    if (this.productMap.containsKey(product.getId())) {
      return Math.max(level, this.productMap.get(product.getId()));
    }

    return level;
  }

  /**
   * Update the level of Bill of materials. The highest for each product (0: product with parent, 1:
   * product with a parent, 2: product with a parent that have a parent, ...)
   *
   * @param billOfMaterial
   * @param level
   */
  protected void assignProductLevel(BillOfMaterial billOfMaterial, int level)
      throws AxelorException {

    if (level > 100) {
      if (billOfMaterial == null || billOfMaterial.getProduct() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(ProductionExceptionMessage.MRP_BOM_LEVEL_TOO_HIGH));
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(ProductionExceptionMessage.MRP_BOM_LEVEL_TOO_HIGH_PRODUCT),
            billOfMaterial.getProduct().getFullName());
      }
    }

    Product product = billOfMaterial.getProduct();

    log.debug("Add product: {} for the level : {} ", product.getFullName(), level);
    this.productMap.put(product.getId(), this.getMaxLevel(product, level));

    level = level + 1;
    if (billOfMaterial.getBillOfMaterialSet() != null
        && !billOfMaterial.getBillOfMaterialSet().isEmpty()) {

      for (BillOfMaterial subBillOfMaterial : billOfMaterial.getBillOfMaterialSet()) {

        Product subProduct = subBillOfMaterial.getProduct();

        if (this.isMrpProduct(subProduct)) {
          this.assignProductLevel(subBillOfMaterial, level);

          Company company = mrp.getStockLocation().getCompany();
          BillOfMaterial defaultBOM = billOfMaterialService.getDefaultBOM(subProduct, company);
          if (defaultBOM != null) {
            this.assignProductLevel(defaultBOM, level);
          }
          List<BillOfMaterial> alternativesBOM =
              billOfMaterialService.getAlternativesBOM(subProduct, company);
          for (BillOfMaterial alternativeBom : alternativesBOM) {
            this.assignProductLevel(alternativeBom, level);
          }
        }
      }
    }
  }

  /**
   * Add a component product of a manuf order where the component product is not contained on the
   * default bill of materials of the produced product.
   *
   * @param manufOrderComponentProduct
   * @param manufOrderProducedProduct
   */
  protected void assignProductAndLevel(
      Product manufOrderComponentProduct, Product manufOrderProducedProduct) {

    log.debug("Add of the product : {}", manufOrderComponentProduct.getFullName());
    this.productMap.put(
        manufOrderComponentProduct.getId(), this.getMaxLevel(manufOrderProducedProduct, 0) + 1);
  }

  protected void createAvailableStockMrpLine(Product product, StockLocation stockLocation)
      throws AxelorException {

    MrpLineType availableStockMrpLineType =
        mrpLineTypeService.getMrpLineType(
            MrpLineTypeRepository.ELEMENT_AVAILABLE_STOCK, mrp.getMrpTypeSelect());

    if (availableStockMrpLineType == null) {
      return;
    }

    mrpLineRepository.save(
        this.createAvailableStockMrpLine(
            mrpRepository.find(mrp.getId()),
            productRepository.find(product.getId()),
            stockLocation,
            availableStockMrpLineType));
  }

  @Override
  protected Mrp completeProjectedStock(
      Mrp mrp, Product product, Company company, StockLocation stockLocation)
      throws AxelorException {
    super.completeProjectedStock(mrp, product, company, stockLocation);
    this.createManufOrderMrpLines();
    return mrp;
  }
}
