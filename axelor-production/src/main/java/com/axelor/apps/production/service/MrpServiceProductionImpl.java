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
package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockRules;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
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
import com.axelor.apps.supplychain.service.MrpServiceImpl;
import com.axelor.apps.tool.StringTool;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MrpServiceProductionImpl extends MrpServiceImpl {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppBaseService appBaseService;

  protected ManufOrderRepository manufOrderRepository;

  @Inject
  public MrpServiceProductionImpl(
      AppBaseService appBaseService,
      AppProductionService appProductionService,
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
      ManufOrderRepository manufOrderRepository,
      StockLocationService stockLocationService) {

    super(
        appProductionService,
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
        stockLocationService);

    this.appBaseService = appBaseService;
    this.manufOrderRepository = manufOrderRepository;
  }

  @Override
  protected void completeMrp(Mrp mrp) throws AxelorException {

    super.completeMrp(mrp);

    if (Beans.get(AppProductionService.class).isApp("production")) {
      this.createManufOrderMrpLines();
    }
  }

  // Manufacturing order AND manufacturing order need
  protected void createManufOrderMrpLines() throws AxelorException {

    MrpLineType manufOrderMrpLineType =
        this.getMrpLineType(MrpLineTypeRepository.ELEMENT_MANUFACTURING_ORDER);
    MrpLineType manufOrderNeedMrpLineType =
        this.getMrpLineType(MrpLineTypeRepository.ELEMENT_MANUFACTURING_ORDER_NEED);

    String statusSelect = manufOrderMrpLineType.getStatusSelect();
    List<Integer> statusList = StringTool.getIntegerList(statusSelect);

    if (statusList.isEmpty()) {
      statusList.add(ManufOrderRepository.STATUS_FINISHED);
    }

    List<ManufOrder> manufOrderList =
        manufOrderRepository
            .all()
            .filter(
                "self.product.id in (?1) AND self.prodProcess.stockLocation in (?2) "
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
    } else {
      maturityDate = manufOrder.getPlannedStartDateT().toLocalDate();
    }

    maturityDate = this.computeMaturityDate(maturityDate, manufOrderMrpLineType);

    for (ProdProduct prodProduct : manufOrder.getToProduceProdProductList()) {

      Product product = prodProduct.getProduct();

      if ((this.isBeforeEndDate(maturityDate) || manufOrderMrpLineType.getIgnoreEndDate())
          && this.isMrpProduct(product)) {
        MrpLine mrpLine =
            this.createMrpLine(
                mrp,
                product,
                manufOrderMrpLineType,
                prodProduct.getQty(),
                maturityDate,
                BigDecimal.ZERO,
                stockLocation,
                manufOrder);
        if (mrpLine != null) {
          mrpLineRepository.save(mrpLine);
        }
      }
    }

    if (manufOrder.getIsConsProOnOperation()) {
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
        for (ProdProduct prodProduct : operationOrder.getToConsumeProdProductList()) {

          Product product = prodProduct.getProduct();

          if (this.isMrpProduct(product)) {

            maturityDate = null;

            if (operationOrder.getPlannedEndDateT() != null) {
              maturityDate = operationOrder.getPlannedEndDateT().toLocalDate();
            } else {
              maturityDate = operationOrder.getPlannedStartDateT().toLocalDate();
            }

            maturityDate = this.computeMaturityDate(maturityDate, manufOrderNeedMrpLineType);

            MrpLine mrpLine =
                this.createMrpLine(
                    mrp,
                    prodProduct.getProduct(),
                    manufOrderNeedMrpLineType,
                    prodProduct.getQty(),
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

          MrpLine mrpLine =
              this.createMrpLine(
                  mrp,
                  product,
                  manufOrderNeedMrpLineType,
                  prodProduct.getQty(),
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

  @Override
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

    super.createProposalMrpLine(
        mrp,
        product,
        mrpLineType,
        reorderQty,
        stockLocation,
        maturityDate,
        mrpLineOriginList,
        relatedToSelectName);

    if (!Beans.get(AppProductionService.class).isApp("production")) {
      return;
    }
    BillOfMaterial defaultBillOfMaterial = product.getDefaultBillOfMaterial();

    if (mrpLineType.getElementSelect() == MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL
        && defaultBillOfMaterial != null) {

      MrpLineType manufProposalNeedMrpLineType =
          this.getMrpLineType(MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL_NEED);

      for (BillOfMaterial billOfMaterial : defaultBillOfMaterial.getBillOfMaterialSet()) {

        Product subProduct = billOfMaterial.getProduct();

        if (this.isMrpProduct(subProduct)) {
          // TODO take the time to do the Manuf order (use machine planning)
          super.createProposalMrpLine(
              mrp,
              subProduct,
              manufProposalNeedMrpLineType,
              reorderQty
                  .multiply(billOfMaterial.getQty())
                  .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_EVEN),
              stockLocation,
              maturityDate,
              mrpLineOriginList,
              relatedToSelectName);
        }
      }
    }
  }

  /**
   * Returns the type of an mrp proposal.
   *
   * <p>First checks for a stock rule, then if there isn't one, checks for the
   * procurementMethodSelect field of the product. If the product can be both bought and produced, a
   * manufacturing order is generated.
   */
  @Override
  protected MrpLineType getMrpLineTypeForProposal(StockRules stockRules, Product product)
      throws AxelorException {

    if (!Beans.get(AppProductionService.class).isApp("production")) {
      return super.getMrpLineTypeForProposal(stockRules, product);
    }

    if (stockRules != null) {
      if (stockRules.getOrderAlertSelect() == StockRulesRepository.ORDER_ALERT_PRODUCTION_ORDER) {
        return this.getMrpLineType(MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL);
      } else {
        return this.getMrpLineType(MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL);
      }
    }

    if (product.getProcurementMethodSelect().equals(ProductRepository.PROCUREMENT_METHOD_BUY)) {
      return this.getMrpLineType(MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL);
    } else {
      return this.getMrpLineType(MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL);
    }
  }

  @Override
  protected boolean isProposalElement(MrpLineType mrpLineType) {

    if (!Beans.get(AppProductionService.class).isApp("production")) {
      return super.isProposalElement(mrpLineType);
    }

    if (mrpLineType.getElementSelect() == MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL
        || mrpLineType.getElementSelect() == MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL
        || mrpLineType.getElementSelect()
            == MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL_NEED) {

      return true;
    }

    return false;
  }

  @Override
  protected void assignProductAndLevel(Product product) {

    if (!Beans.get(AppProductionService.class).isApp("production")) {
      super.assignProductAndLevel(product);
      return;
    }

    log.debug("Add of the product : {}", product.getFullName());
    this.productMap.put(product.getId(), this.getMaxLevel(product, 0));

    if (product.getDefaultBillOfMaterial() != null) {
      this.assignProductLevel(product.getDefaultBillOfMaterial(), 0);
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
  protected void assignProductLevel(BillOfMaterial billOfMaterial, int level) {

    if (billOfMaterial.getBillOfMaterialSet() == null
        || billOfMaterial.getBillOfMaterialSet().isEmpty()
        || level > 100) {

      Product subProduct = billOfMaterial.getProduct();

      log.debug("Add of the sub product : {} for the level : {} ", subProduct.getFullName(), level);
      this.productMap.put(subProduct.getId(), this.getMaxLevel(subProduct, level));

    } else {

      level = level + 1;

      for (BillOfMaterial subBillOfMaterial : billOfMaterial.getBillOfMaterialSet()) {

        Product subProduct = subBillOfMaterial.getProduct();

        if (this.isMrpProduct(subProduct)) {
          this.assignProductLevel(subBillOfMaterial, level);

          if (subProduct.getDefaultBillOfMaterial() != null) {
            this.assignProductLevel(subProduct.getDefaultBillOfMaterial(), level);
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
        this.getMrpLineType(MrpLineTypeRepository.ELEMENT_AVAILABLE_STOCK);

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
