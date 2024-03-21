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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.production.service.config.StockConfigProductionService;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderStockMoveService;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.supplychain.service.ProductStockLocationService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ValidationException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManufOrderServiceImpl implements ManufOrderService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected SequenceService sequenceService;
  protected OperationOrderService operationOrderService;
  protected ManufOrderPlanService manufOrderPlanService;
  protected ManufOrderCreatePurchaseOrderService manufOrderCreatePurchaseOrderService;
  protected AppBaseService appBaseService;
  protected AppProductionService appProductionService;
  protected ManufOrderRepository manufOrderRepo;
  protected ProductCompanyService productCompanyService;
  protected BarcodeGeneratorService barcodeGeneratorService;
  protected ProductStockLocationService productStockLocationService;
  protected UnitConversionService unitConversionService;
  protected MetaFiles metaFiles;
  protected BillOfMaterialService billOfMaterialService;
  protected StockMoveService stockMoveService;
  protected ManufOrderOutgoingStockMoveService manufOrderOutgoingStockMoveService;
  protected ManufOrderStockMoveService manufOrderStockMoveService;
  protected ManufOrderGetStockMoveService manufOrderGetStockMoveService;
  protected ManufOrderCreateStockMoveLineService manufOrderCreateStockMoveLineService;
  protected ManufOrderProdProductService manufOrderProdProductService;
  protected ManufOrderBillOfMaterialService manufOrderBillOfMaterialService;

  @Inject
  public ManufOrderServiceImpl(
      SequenceService sequenceService,
      OperationOrderService operationOrderService,
      ManufOrderPlanService manufOrderPlanService,
      ManufOrderCreatePurchaseOrderService manufOrderCreatePurchaseOrderService,
      AppBaseService appBaseService,
      AppProductionService appProductionService,
      ManufOrderRepository manufOrderRepo,
      ProductCompanyService productCompanyService,
      BarcodeGeneratorService barcodeGeneratorService,
      ProductStockLocationService productStockLocationService,
      UnitConversionService unitConversionService,
      MetaFiles metaFiles,
      BillOfMaterialService billOfMaterialService,
      StockMoveService stockMoveService,
      ManufOrderOutgoingStockMoveService manufOrderOutgoingStockMoveService,
      ManufOrderStockMoveService manufOrderStockMoveService,
      ManufOrderGetStockMoveService manufOrderGetStockMoveService,
      ManufOrderCreateStockMoveLineService manufOrderCreateStockMoveLineService,
      ManufOrderProdProductService manufOrderProdProductService,
      ManufOrderBillOfMaterialService manufOrderBillOfMaterialService) {
    this.sequenceService = sequenceService;
    this.operationOrderService = operationOrderService;
    this.manufOrderPlanService = manufOrderPlanService;
    this.manufOrderCreatePurchaseOrderService = manufOrderCreatePurchaseOrderService;
    this.appBaseService = appBaseService;
    this.appProductionService = appProductionService;
    this.manufOrderRepo = manufOrderRepo;
    this.productCompanyService = productCompanyService;
    this.barcodeGeneratorService = barcodeGeneratorService;
    this.productStockLocationService = productStockLocationService;
    this.unitConversionService = unitConversionService;
    this.metaFiles = metaFiles;
    this.billOfMaterialService = billOfMaterialService;
    this.stockMoveService = stockMoveService;
    this.manufOrderOutgoingStockMoveService = manufOrderOutgoingStockMoveService;
    this.manufOrderStockMoveService = manufOrderStockMoveService;
    this.manufOrderGetStockMoveService = manufOrderGetStockMoveService;
    this.manufOrderCreateStockMoveLineService = manufOrderCreateStockMoveLineService;
    this.manufOrderProdProductService = manufOrderProdProductService;
    this.manufOrderBillOfMaterialService = manufOrderBillOfMaterialService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public ManufOrder generateManufOrder(
      Product product,
      BigDecimal qtyRequested,
      int priority,
      boolean isToInvoice,
      BillOfMaterial billOfMaterial,
      LocalDateTime plannedStartDateT,
      LocalDateTime plannedEndDateT,
      ManufOrderOriginType manufOrderOrigin)
      throws AxelorException {

    if (billOfMaterial == null) {
      billOfMaterial = this.getBillOfMaterial(product);
    }

    Company company = billOfMaterial.getCompany();
    if (company == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(ProductionExceptionMessage.NO_COMPANY_IN_BILL_OF_MATERIALS),
          billOfMaterial.getProduct().getName());
    }

    if (billOfMaterial.getQty().signum() == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.GENERATE_MANUF_ORDER_BOM_DIVIDE_ZERO),
          billOfMaterial.getName());
    }
    Unit unit = billOfMaterial.getUnit();
    if (unit == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(ProductionExceptionMessage.GENERATE_MANUF_ORDER_BOM_MISSING_UNIT),
          billOfMaterial.getName());
    }
    if (!unit.equals(product.getUnit())) {
      qtyRequested =
          unitConversionService.convert(
              product.getUnit(), unit, qtyRequested, qtyRequested.scale(), product);
    }
    BigDecimal qty =
        qtyRequested.divide(
            billOfMaterial.getQty(),
            appBaseService.getNbDecimalDigitForQty(),
            RoundingMode.HALF_UP);

    ManufOrder manufOrder =
        this.createManufOrder(
            product,
            qty,
            unit,
            priority,
            IS_TO_INVOICE,
            company,
            billOfMaterial,
            plannedStartDateT,
            plannedEndDateT);

    if (manufOrderOrigin.equals(ManufOrderOriginTypeProduction.ORIGIN_TYPE_SALE_ORDER)
            && appProductionService.getAppProduction().getAutoPlanManufOrderFromSO()
        || manufOrderOrigin.equals(ManufOrderOriginTypeProduction.ORIGIN_TYPE_MRP)
        || manufOrderOrigin.equals(ManufOrderOriginTypeProduction.ORIGIN_TYPE_OTHER)) {
      manufOrder = manufOrderPlanService.plan(manufOrder);
      manufOrderCreatePurchaseOrderService.createPurchaseOrders(manufOrder);
    }

    return manufOrderRepo.save(manufOrder);
  }

  @Override
  public ManufOrder createManufOrder(
      Product product,
      BigDecimal qty,
      Unit unit,
      int priority,
      boolean isToInvoice,
      Company company,
      BillOfMaterial billOfMaterial,
      LocalDateTime plannedStartDateT,
      LocalDateTime plannedEndDateT)
      throws AxelorException {

    logger.debug("Creation of a manufacturing order {}", priority);

    ProdProcess prodProcess = billOfMaterial.getProdProcess();

    ManufOrder manufOrder =
        new ManufOrder(
            qty,
            company,
            null,
            priority,
            billOfMaterialService.isManagedConsumedProduct(billOfMaterial),
            unit,
            billOfMaterial,
            product,
            prodProcess,
            plannedStartDateT,
            plannedEndDateT,
            ManufOrderRepository.STATUS_DRAFT,
            prodProcess.getOutsourcing());
    manufOrder = manufOrderRepo.save(manufOrder);

    if (appProductionService.getAppProduction().getManageWorkshop()) {
      manufOrder.setWorkshopStockLocation(billOfMaterial.getWorkshopStockLocation());
    }

    if (prodProcess != null && prodProcess.getProdProcessLineList() != null) {
      List<ProdProcessLine> sortedProdProcessLineList =
          prodProcess.getProdProcessLineList().stream()
              .sorted(Comparator.comparing(ProdProcessLine::getPriority))
              .collect(Collectors.toList());
      for (ProdProcessLine prodProcessLine : sortedProdProcessLineList) {

        manufOrder.addOperationOrderListItem(
            operationOrderService.createOperationOrder(manufOrder, prodProcessLine));
      }
    }

    return manufOrder;
  }

  @Override
  public String getManufOrderSeq(ManufOrder manufOrder) throws AxelorException {

    ProductionConfigService productionConfigService = Beans.get(ProductionConfigService.class);
    ProductionConfig productionConfig =
        productionConfigService.getProductionConfig(manufOrder.getCompany());
    Sequence sequence =
        productionConfigService.getManufOrderSequence(
            productionConfig, manufOrder.getWorkshopStockLocation());

    String seq =
        sequenceService.getSequenceNumber(sequence, ManufOrder.class, "manufOrderSeq", manufOrder);

    if (seq == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.MANUF_ORDER_SEQ));
    }

    return seq;
  }

  public BillOfMaterial getBillOfMaterial(Product product) throws AxelorException {

    BillOfMaterial billOfMaterial = product.getDefaultBillOfMaterial();

    if (billOfMaterial == null && product.getParentProduct() != null) {
      billOfMaterial = product.getParentProduct().getDefaultBillOfMaterial();
    }

    if (billOfMaterial == null) {
      throw new AxelorException(
          product,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_SALES_ORDER_NO_BOM),
          product.getName(),
          product.getCode());
    }

    return billOfMaterial;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public StockMove generateWasteStockMove(ManufOrder manufOrder) throws AxelorException {
    StockMove wasteStockMove = null;
    Company company = manufOrder.getCompany();

    if (manufOrder.getWasteProdProductList() == null
        || company == null
        || manufOrder.getWasteProdProductList().isEmpty()) {
      return wasteStockMove;
    }

    StockConfigProductionService stockConfigService = Beans.get(StockConfigProductionService.class);
    StockMoveService stockMoveService = Beans.get(StockMoveService.class);
    StockMoveLineService stockMoveLineService = Beans.get(StockMoveLineService.class);

    StockConfig stockConfig = stockConfigService.getStockConfig(company);
    StockLocation virtualStockLocation =
        stockConfigService.getProductionVirtualStockLocation(stockConfig, false);
    StockLocation wasteStockLocation = stockConfigService.getWasteStockLocation(stockConfig);

    wasteStockMove =
        stockMoveService.createStockMove(
            virtualStockLocation.getAddress(),
            wasteStockLocation.getAddress(),
            company,
            virtualStockLocation,
            wasteStockLocation,
            null,
            appBaseService.getTodayDate(company),
            manufOrder.getWasteProdDescription(),
            StockMoveRepository.TYPE_INTERNAL);

    for (ProdProduct prodProduct : manufOrder.getWasteProdProductList()) {
      stockMoveLineService.createStockMoveLine(
          prodProduct.getProduct(),
          (String) productCompanyService.get(prodProduct.getProduct(), "name", company),
          (String) productCompanyService.get(prodProduct.getProduct(), "description", company),
          prodProduct.getQty(),
          (BigDecimal) productCompanyService.get(prodProduct.getProduct(), "costPrice", company),
          (BigDecimal) productCompanyService.get(prodProduct.getProduct(), "costPrice", company),
          prodProduct.getUnit(),
          wasteStockMove,
          StockMoveLineService.TYPE_WASTE_PRODUCTIONS,
          false,
          BigDecimal.ZERO,
          virtualStockLocation,
          wasteStockLocation);
    }

    stockMoveService.validate(wasteStockMove);

    manufOrder.setWasteStockMove(wasteStockMove);
    return wasteStockMove;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updatePlannedQty(ManufOrder manufOrder) throws AxelorException {
    manufOrder.clearToConsumeProdProductList();
    manufOrder.clearToProduceProdProductList();
    manufOrderProdProductService.createToConsumeProdProductList(manufOrder);
    manufOrderProdProductService.createToProduceProdProductList(manufOrder);
    updateRealQty(manufOrder, manufOrder.getQty());
    LocalDateTime plannedStartDateT = manufOrder.getPlannedStartDateT();
    manufOrderPlanService.updatePlannedDates(
        manufOrder,
        plannedStartDateT != null
            ? plannedStartDateT
            : appProductionService.getTodayDateTime().toLocalDateTime());

    manufOrderRepo.save(manufOrder);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateRealQty(ManufOrder manufOrder, BigDecimal qtyToUpdate) throws AxelorException {
    ManufOrderStockMoveService manufOrderStockMoveService =
        Beans.get(ManufOrderStockMoveService.class);
    if (!manufOrder.getIsConsProOnOperation()) {
      manufOrderCreateStockMoveLineService.createNewConsumedStockMoveLineList(
          manufOrder, qtyToUpdate);
      manufOrderProdProductService.updateDiffProdProductList(manufOrder);
    } else {
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
        Beans.get(OperationOrderStockMoveService.class)
            .createNewConsumedStockMoveLineList(operationOrder, qtyToUpdate);
        operationOrderService.updateDiffProdProductList(operationOrder);
      }
    }

    manufOrderCreateStockMoveLineService.createNewProducedStockMoveLineList(
        manufOrder, qtyToUpdate);
  }

  /**
   * Called by generateMultiLevelManufOrder controller to generate all manuf order for a given bill
   * of material list from a given manuf order.
   *
   * @param manufOrder
   * @throws AxelorException
   * @return
   */
  public List<ManufOrder> generateAllSubManufOrder(List<Product> productList, ManufOrder manufOrder)
      throws AxelorException {
    Integer depth = 0;
    List<ManufOrder> moList = new ArrayList<>();
    List<Pair<BillOfMaterial, BigDecimal>> childBomList =
        manufOrderBillOfMaterialService.getToConsumeSubBomList(
            manufOrder.getBillOfMaterial(), manufOrder, productList);
    moList.addAll(this.generateChildMOs(manufOrder, childBomList, depth));
    return moList;
  }

  protected ManufOrder createDraftManufOrder(
      Product product,
      BigDecimal qtyRequested,
      int priority,
      BillOfMaterial billOfMaterial,
      LocalDateTime plannedStartDateT,
      LocalDateTime plannedEndDateT)
      throws AxelorException {

    ProdProcess prodProcess = billOfMaterial.getProdProcess();
    Company company = billOfMaterial.getCompany();

    Unit unit = billOfMaterial.getUnit();
    if (unit != null && !unit.equals(product.getUnit())) {
      qtyRequested =
          unitConversionService.convert(
              product.getUnit(), unit, qtyRequested, qtyRequested.scale(), product);
    }
    return new ManufOrder(
        qtyRequested,
        company,
        null,
        priority,
        false,
        unit,
        billOfMaterial,
        product,
        prodProcess,
        plannedStartDateT,
        plannedEndDateT,
        ManufOrderRepository.STATUS_DRAFT,
        prodProcess.getOutsourcing());
  }

  @Override
  public void createBarcode(ManufOrder manufOrder) {
    String manufOrderSeq = manufOrder.getManufOrderSeq();

    try (InputStream inStream =
        barcodeGeneratorService.createBarCode(
            manufOrderSeq, appProductionService.getAppProduction().getBarcodeTypeConfig(), true)) {

      if (inStream != null) {
        MetaFile barcodeFile =
            metaFiles.upload(
                inStream, String.format("ManufOrderBarcode%d.png", manufOrder.getId()));
        manufOrder.setBarCode(barcodeFile);
      }
    } catch (IOException e) {
      TraceBackService.trace(e);
    } catch (AxelorException e) {
      throw new ValidationException(e.getMessage());
    }
  }

  @Override
  public List<ManufOrder> getChildrenManufOrder(ManufOrder manufOrder) {
    return manufOrderRepo
        .all()
        .filter("self.parentMO = :manufOrder")
        .bind("manufOrder", manufOrder)
        .fetch();
  }

  protected List<ManufOrder> generateChildMOs(
      ManufOrder parentMO, List<Pair<BillOfMaterial, BigDecimal>> childBomList, Integer depth)
      throws AxelorException {
    List<ManufOrder> manufOrderList = new ArrayList<>();

    // prevent infinite loop
    if (depth >= 25) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.CHILD_BOM_TOO_MANY_ITERATION));
    }
    depth++;

    for (Pair<BillOfMaterial, BigDecimal> childBomPair : childBomList) {
      BillOfMaterial childBom = childBomPair.getLeft();
      BigDecimal qtyRequested = childBomPair.getRight();

      ManufOrder childMO =
          createDraftManufOrder(
              childBom.getProduct(),
              qtyRequested,
              billOfMaterialService.getPriority(childBom),
              childBom,
              null,
              parentMO.getPlannedStartDateT());

      childMO.setManualMOSeq(this.getManualSequence());
      childMO.setParentMO(parentMO);
      childMO.setClientPartner(parentMO.getClientPartner());
      manufOrderList.add(childMO);

      manufOrderList.addAll(
          this.generateChildMOs(
              childMO,
              manufOrderBillOfMaterialService.getToConsumeSubBomList(
                  childMO.getBillOfMaterial(), childMO, null),
              depth));
    }
    return manufOrderList;
  }

  protected String getManualSequence() {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
  }

  @Override
  public BigDecimal computeProducibleQty(ManufOrder manufOrder) throws AxelorException {
    Company company = manufOrder.getCompany();
    BillOfMaterial billOfMaterial = manufOrder.getBillOfMaterial();

    if (company == null
        || billOfMaterial == null
        || billOfMaterial.getQty().compareTo(BigDecimal.ZERO) <= 0
        || CollectionUtils.isEmpty(billOfMaterial.getBillOfMaterialLineList())) {
      return BigDecimal.ZERO;
    }

    BigDecimal producibleQty = null;
    BigDecimal bomQty = billOfMaterial.getQty();

    for (BillOfMaterialLine billOfMaterialLine : billOfMaterial.getBillOfMaterialLineList()) {
      Product product = billOfMaterialLine.getProduct();
      BigDecimal availableQty = productStockLocationService.getAvailableQty(product, company, null);
      BigDecimal qtyNeeded = billOfMaterialLine.getQty();
      if (availableQty.compareTo(BigDecimal.ZERO) >= 0
          && qtyNeeded.compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal qtyToUse = availableQty.divideToIntegralValue(qtyNeeded);
        producibleQty = producibleQty == null ? qtyToUse : producibleQty.min(qtyToUse);
      }
    }

    producibleQty =
        producibleQty == null
            ? BigDecimal.ZERO
            : producibleQty
                .multiply(bomQty)
                .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP);
    return producibleQty;
  }

  /**
   * Method that will update planned dates of manuf order. Unlike the other methods, this will not
   * reset planned dates of the operation orders of the manuf order. This method must be called when
   * changement has occured in operation orders.
   *
   * @param manufOrder
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updatePlannedDates(ManufOrder manufOrder) {

    manufOrder.setPlannedStartDateT(manufOrderPlanService.computePlannedStartDateT(manufOrder));
    manufOrder.setPlannedEndDateT(manufOrderPlanService.computePlannedEndDateT(manufOrder));
  }

  @Override
  public void checkApplicableManufOrder(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder.getBillOfMaterial().getStatusSelect()
            != BillOfMaterialRepository.STATUS_APPLICABLE
        || manufOrder.getProdProcess().getStatusSelect()
            != ProdProcessRepository.STATUS_APPLICABLE) {
      throw new AxelorException(
          manufOrder,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.CHECK_BOM_AND_PROD_PROCESS));
    }
  }
}
