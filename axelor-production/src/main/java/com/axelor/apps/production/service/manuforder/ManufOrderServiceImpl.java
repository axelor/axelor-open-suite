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
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Sequence;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductVariantService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.ProdResidualProduct;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.axelor.apps.production.db.repo.ProdProductRepository;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.production.service.config.StockConfigProductionService;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderStockMoveService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.supplychain.service.ProductStockLocationService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.helpers.StringHelper;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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
  protected ProductVariantService productVariantService;
  protected AppBaseService appBaseService;
  protected AppProductionService appProductionService;
  protected ManufOrderRepository manufOrderRepo;
  protected ProdProductRepository prodProductRepo;
  protected ProductCompanyService productCompanyService;
  protected ProductStockLocationService productStockLocationService;
  protected UnitConversionService unitConversionService;
  protected PartnerRepository partnerRepository;
  protected BillOfMaterialService billOfMaterialService;
  protected StockMoveService stockMoveService;
  protected ManufOrderOutgoingStockMoveService manufOrderOutgoingStockMoveService;
  protected ManufOrderStockMoveService manufOrderStockMoveService;
  protected ManufOrderGetStockMoveService manufOrderGetStockMoveService;
  protected ManufOrderCreateStockMoveLineService manufOrderCreateStockMoveLineService;

  @Inject
  public ManufOrderServiceImpl(
      SequenceService sequenceService,
      OperationOrderService operationOrderService,
      ManufOrderPlanService manufOrderPlanService,
      ManufOrderCreatePurchaseOrderService manufOrderCreatePurchaseOrderService,
      ProductVariantService productVariantService,
      AppBaseService appBaseService,
      AppProductionService appProductionService,
      ManufOrderRepository manufOrderRepo,
      ProdProductRepository prodProductRepo,
      ProductCompanyService productCompanyService,
      ProductStockLocationService productStockLocationService,
      UnitConversionService unitConversionService,
      PartnerRepository partnerRepository,
      BillOfMaterialService billOfMaterialService,
      StockMoveService stockMoveService,
      ManufOrderOutgoingStockMoveService manufOrderOutgoingStockMoveService,
      ManufOrderStockMoveService manufOrderStockMoveService,
      ManufOrderGetStockMoveService manufOrderGetStockMoveService,
      ManufOrderCreateStockMoveLineService manufOrderCreateStockMoveLineService) {
    this.sequenceService = sequenceService;
    this.operationOrderService = operationOrderService;
    this.manufOrderPlanService = manufOrderPlanService;
    this.manufOrderCreatePurchaseOrderService = manufOrderCreatePurchaseOrderService;
    this.productVariantService = productVariantService;
    this.appBaseService = appBaseService;
    this.appProductionService = appProductionService;
    this.manufOrderRepo = manufOrderRepo;
    this.prodProductRepo = prodProductRepo;
    this.productCompanyService = productCompanyService;
    this.productStockLocationService = productStockLocationService;
    this.unitConversionService = unitConversionService;
    this.partnerRepository = partnerRepository;
    this.billOfMaterialService = billOfMaterialService;
    this.stockMoveService = stockMoveService;
    this.manufOrderOutgoingStockMoveService = manufOrderOutgoingStockMoveService;
    this.manufOrderStockMoveService = manufOrderStockMoveService;
    this.manufOrderGetStockMoveService = manufOrderGetStockMoveService;
    this.manufOrderCreateStockMoveLineService = manufOrderCreateStockMoveLineService;
  }

  @Override
  public boolean areLinesOutsourced(ManufOrder manufOrder) {

    List<OperationOrder> operationOrderList = manufOrder.getOperationOrderList();
    if (manufOrder.getOutsourcing() || CollectionUtils.isEmpty(operationOrderList)) {
      return false;
    }
    return operationOrderList.stream().anyMatch(OperationOrder::getOutsourcing);
  }

  @Override
  public void setOperationOrdersOutsourcing(ManufOrder manufOrder) {
    if (manufOrder != null && manufOrder.getOperationOrderList() != null) {
      manufOrder
          .getOperationOrderList()
          .forEach(oo -> oo.setOutsourcing(manufOrder.getOutsourcing()));
    }
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
  public void createToConsumeProdProductList(ManufOrder manufOrder) {

    BigDecimal manufOrderQty = manufOrder.getQty();

    BillOfMaterial billOfMaterial = manufOrder.getBillOfMaterial();

    BigDecimal bomQty = billOfMaterial.getQty();

    if (billOfMaterial.getBillOfMaterialLineList() != null) {

      for (BillOfMaterialLine billOfMaterialLine :
          getSortedBillsOfMaterialsLine(billOfMaterial.getBillOfMaterialLineList())) {

        if (!billOfMaterialLine.getHasNoManageStock()) {

          Product product =
              productVariantService.getProductVariant(
                  manufOrder.getProduct(), billOfMaterialLine.getProduct());

          BigDecimal qty =
              computeToConsumeProdProductLineQuantity(
                  bomQty, manufOrderQty, billOfMaterialLine.getQty());
          ProdProduct prodProduct = new ProdProduct(product, qty, billOfMaterialLine.getUnit());
          manufOrder.addToConsumeProdProductListItem(prodProduct);
          prodProductRepo.persist(prodProduct); // id by order of creation
        }
      }
    }
  }

  protected List<BillOfMaterialLine> getSortedBillsOfMaterialsLine(
      Collection<BillOfMaterialLine> billsOfMaterialLines) {

    billsOfMaterialLines = MoreObjects.firstNonNull(billsOfMaterialLines, Collections.emptyList());
    return billsOfMaterialLines.stream()
        .sorted(
            Comparator.comparing(BillOfMaterialLine::getPriority)
                .thenComparing(Comparator.comparing(BillOfMaterialLine::getId)))
        .collect(Collectors.toList());
  }

  @Override
  public BigDecimal computeToConsumeProdProductLineQuantity(
      BigDecimal bomQty, BigDecimal manufOrderQty, BigDecimal lineQty) {

    BigDecimal qty = BigDecimal.ZERO;

    if (bomQty.signum() != 0) {
      qty =
          manufOrderQty
              .multiply(lineQty)
              .divide(bomQty, appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP);
    }
    return qty;
  }

  @Override
  public void createToProduceProdProductList(ManufOrder manufOrder) {

    BigDecimal manufOrderQty = manufOrder.getQty();

    BillOfMaterial billOfMaterial = manufOrder.getBillOfMaterial();

    BigDecimal bomQty = billOfMaterial.getQty();

    // add the produced product
    manufOrder.addToProduceProdProductListItem(
        new ProdProduct(manufOrder.getProduct(), manufOrderQty, billOfMaterial.getUnit()));

    // Add the residual products
    if (appProductionService.getAppProduction().getManageResidualProductOnBom()
        && billOfMaterial.getProdResidualProductList() != null) {

      for (ProdResidualProduct prodResidualProduct : billOfMaterial.getProdResidualProductList()) {

        Product product =
            productVariantService.getProductVariant(
                manufOrder.getProduct(), prodResidualProduct.getProduct());

        BigDecimal qty =
            bomQty.signum() != 0
                ? prodResidualProduct
                    .getQty()
                    .multiply(manufOrderQty)
                    .divide(bomQty, appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        manufOrder.addToProduceProdProductListItem(
            new ProdProduct(product, qty, prodResidualProduct.getUnit()));
      }
    }
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
            this.isManagedConsumedProduct(billOfMaterial),
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
      for (ProdProcessLine prodProcessLine :
          this._sortProdProcessLineByPriority(prodProcess.getProdProcessLineList())) {

        manufOrder.addOperationOrderListItem(
            operationOrderService.createOperationOrder(manufOrder, prodProcessLine));
      }
    }

    return manufOrder;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void preFillOperations(ManufOrder manufOrder) throws AxelorException {

    BillOfMaterial billOfMaterial = manufOrder.getBillOfMaterial();

    if (manufOrder.getProdProcess() == null) {
      manufOrder.setProdProcess(billOfMaterial.getProdProcess());
    }
    ProdProcess prodProcess = manufOrder.getProdProcess();

    if (prodProcess != null && prodProcess.getProdProcessLineList() != null) {

      for (ProdProcessLine prodProcessLine :
          this._sortProdProcessLineByPriority(prodProcess.getProdProcessLineList())) {
        manufOrder.addOperationOrderListItem(
            operationOrderService.createOperationOrder(manufOrder, prodProcessLine));
      }
    }

    manufOrderRepo.save(manufOrder);
  }

  @Override
  @Transactional
  public void updateOperationsName(ManufOrder manufOrder) {
    for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
      operationOrder.setName(
          operationOrderService.computeName(
              manufOrder, operationOrder.getPriority(), operationOrder.getOperationName()));
    }
  }

  /**
   * Trier une liste de ligne de règle de template
   *
   * @param prodProcessLineList
   */
  public List<ProdProcessLine> _sortProdProcessLineByPriority(
      List<ProdProcessLine> prodProcessLineList) {

    Collections.sort(
        prodProcessLineList,
        new Comparator<ProdProcessLine>() {

          @Override
          public int compare(ProdProcessLine ppl1, ProdProcessLine ppl2) {
            return ppl1.getPriority().compareTo(ppl2.getPriority());
          }
        });

    return prodProcessLineList;
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

  @Override
  public boolean isManagedConsumedProduct(BillOfMaterial billOfMaterial) {

    if (billOfMaterial != null
        && billOfMaterial.getProdProcess() != null
        && billOfMaterial.getProdProcess().getProdProcessLineList() != null) {
      for (ProdProcessLine prodProcessLine :
          billOfMaterial.getProdProcess().getProdProcessLineList()) {

        if ((prodProcessLine.getToConsumeProdProductList() != null
            && !prodProcessLine.getToConsumeProdProductList().isEmpty())) {

          return true;
        }
      }
    }

    return false;
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
          wasteStockLocation,
          prodProduct.getWasteProductTrackingNumber());
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
    this.createToConsumeProdProductList(manufOrder);
    this.createToProduceProdProductList(manufOrder);
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
      updateDiffProdProductList(manufOrder);
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

  @Override
  public ManufOrder updateDiffProdProductList(ManufOrder manufOrder) throws AxelorException {
    List<ProdProduct> toConsumeList = manufOrder.getToConsumeProdProductList();
    List<StockMoveLine> consumedList = manufOrder.getConsumedStockMoveLineList();
    if (toConsumeList == null || consumedList == null) {
      return manufOrder;
    }
    List<ProdProduct> diffConsumeList =
        createDiffProdProductList(manufOrder, toConsumeList, consumedList);

    manufOrder.clearDiffConsumeProdProductList();
    diffConsumeList.forEach(manufOrder::addDiffConsumeProdProductListItem);
    return manufOrder;
  }

  @Override
  public List<ProdProduct> createDiffProdProductList(
      ManufOrder manufOrder,
      List<ProdProduct> prodProductList,
      List<StockMoveLine> stockMoveLineList)
      throws AxelorException {
    List<ProdProduct> diffConsumeList =
        createDiffProdProductList(prodProductList, stockMoveLineList);
    diffConsumeList.forEach(prodProduct -> prodProduct.setDiffConsumeManufOrder(manufOrder));
    return diffConsumeList;
  }

  @Override
  public List<ProdProduct> createDiffProdProductList(
      List<ProdProduct> prodProductList, List<StockMoveLine> stockMoveLineList)
      throws AxelorException {
    List<ProdProduct> diffConsumeList = new ArrayList<>();
    for (ProdProduct prodProduct : prodProductList) {
      Product product = prodProduct.getProduct();
      Unit newUnit = prodProduct.getUnit();
      List<StockMoveLine> stockMoveLineProductList =
          stockMoveLineList.stream()
              .filter(stockMoveLine1 -> stockMoveLine1.getProduct() != null)
              .filter(stockMoveLine1 -> stockMoveLine1.getProduct().equals(product))
              .collect(Collectors.toList());
      if (stockMoveLineProductList.isEmpty()) {
        StockMoveLine stockMoveLine = new StockMoveLine();
        stockMoveLineProductList.add(stockMoveLine);
      }
      BigDecimal diffQty = computeDiffQty(prodProduct, stockMoveLineProductList, product);
      BigDecimal plannedQty = prodProduct.getQty();
      BigDecimal realQty = diffQty.add(plannedQty);
      if (diffQty.compareTo(BigDecimal.ZERO) != 0) {
        ProdProduct diffProdProduct = new ProdProduct();
        diffProdProduct.setQty(diffQty);
        diffProdProduct.setPlannedQty(plannedQty);
        diffProdProduct.setRealQty(realQty);
        diffProdProduct.setProduct(product);
        diffProdProduct.setUnit(newUnit);
        diffConsumeList.add(diffProdProduct);
      }
    }
    // There are stock move lines with products that are not available in
    // prod product list. It needs to appear in the prod product list
    List<StockMoveLine> stockMoveLineMissingProductList =
        stockMoveLineList.stream()
            .filter(stockMoveLine1 -> stockMoveLine1.getProduct() != null)
            .filter(
                stockMoveLine1 ->
                    !prodProductList.stream()
                        .map(ProdProduct::getProduct)
                        .collect(Collectors.toList())
                        .contains(stockMoveLine1.getProduct()))
            .collect(Collectors.toList());
    for (StockMoveLine stockMoveLine : stockMoveLineMissingProductList) {
      if (stockMoveLine.getQty().compareTo(BigDecimal.ZERO) != 0) {
        ProdProduct diffProdProduct = new ProdProduct();
        diffProdProduct.setQty(stockMoveLine.getQty());
        diffProdProduct.setPlannedQty(BigDecimal.ZERO);
        diffProdProduct.setRealQty(stockMoveLine.getQty());
        diffProdProduct.setProduct(stockMoveLine.getProduct());
        diffProdProduct.setUnit(stockMoveLine.getUnit());
        diffConsumeList.add(diffProdProduct);
      }
    }
    return diffConsumeList;
  }

  /**
   * Compute the difference in qty between a prodProduct and the qty in a list of stock move lines.
   *
   * @param prodProduct
   * @param stockMoveLineList
   * @param product
   * @return
   * @throws AxelorException
   */
  protected BigDecimal computeDiffQty(
      ProdProduct prodProduct, List<StockMoveLine> stockMoveLineList, Product product)
      throws AxelorException {
    BigDecimal consumedQty = BigDecimal.ZERO;
    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      if (stockMoveLine.getUnit() != null && prodProduct.getUnit() != null) {
        consumedQty =
            consumedQty.add(
                Beans.get(UnitConversionService.class)
                    .convert(
                        stockMoveLine.getUnit(),
                        prodProduct.getUnit(),
                        stockMoveLine.getQty(),
                        stockMoveLine.getQty().scale(),
                        product));
      } else {
        consumedQty = consumedQty.add(stockMoveLine.getQty());
      }
    }
    return consumedQty.subtract(prodProduct.getQty());
  }

  @Override
  public String getConsumeAndMissingQtyForAProduct(
      Long productId, Long companyId, Long stockLocationId) {
    List<Integer> statusList = getMOFiltersOnProductionConfig();
    String statusListQuery =
        statusList.stream().map(String::valueOf).collect(Collectors.joining(","));
    String query =
        "self.product.id = "
            + productId
            + " AND self.stockMove.statusSelect = "
            + StockMoveRepository.STATUS_PLANNED
            + " AND self.fromStockLocation.typeSelect != "
            + StockLocationRepository.TYPE_VIRTUAL
            + " AND ( (self.consumedManufOrder IS NOT NULL AND self.consumedManufOrder.statusSelect IN ("
            + statusListQuery
            + "))"
            + " OR (self.consumedOperationOrder IS NOT NULL AND self.consumedOperationOrder.statusSelect IN ( "
            + statusListQuery
            + ") ) ) ";
    if (companyId != 0L) {
      query += " AND self.stockMove.company.id = " + companyId;
      if (stockLocationId != 0L) {
        if (stockLocationId != 0L) {
          StockLocation stockLocation =
              Beans.get(StockLocationRepository.class).find(stockLocationId);
          List<StockLocation> stockLocationList =
              Beans.get(StockLocationService.class)
                  .getAllLocationAndSubLocation(stockLocation, false);
          if (!stockLocationList.isEmpty()
              && stockLocation.getCompany().getId().equals(companyId)) {
            query +=
                " AND self.fromStockLocation.id IN ("
                    + StringHelper.getIdListString(stockLocationList)
                    + ") ";
          }
        }
      }
    }

    return query;
  }

  @Override
  public String getBuildingQtyForAProduct(Long productId, Long companyId, Long stockLocationId) {
    List<Integer> statusList = getMOFiltersOnProductionConfig();
    String statusListQuery =
        statusList.stream().map(String::valueOf).collect(Collectors.joining(","));
    String query =
        "self.product.id = "
            + productId
            + " AND self.stockMove.statusSelect = "
            + StockMoveRepository.STATUS_PLANNED
            + " AND self.stockMove.toStockLocation.typeSelect != "
            + StockLocationRepository.TYPE_VIRTUAL
            + " AND self.producedManufOrder IS NOT NULL "
            + " AND self.producedManufOrder.statusSelect IN ( "
            + statusListQuery
            + " )";
    if (companyId != 0L) {
      query += "AND self.stockMove.company.id = " + companyId;
      if (stockLocationId != 0L) {
        StockLocation stockLocation =
            Beans.get(StockLocationRepository.class).find(stockLocationId);
        List<StockLocation> stockLocationList =
            Beans.get(StockLocationService.class)
                .getAllLocationAndSubLocation(stockLocation, false);
        if (!stockLocationList.isEmpty() && stockLocation.getCompany().getId().equals(companyId)) {
          query +=
              " AND self.stockMove.toStockLocation.id IN ("
                  + StringHelper.getIdListString(stockLocationList)
                  + ") ";
        }
      }
    }

    return query;
  }

  private List<Integer> getMOFiltersOnProductionConfig() {
    List<Integer> statusList = new ArrayList<>();
    statusList.add(ManufOrderRepository.STATUS_IN_PROGRESS);
    statusList.add(ManufOrderRepository.STATUS_STANDBY);
    String status = appProductionService.getAppProduction().getmOFilterOnStockDetailStatusSelect();
    if (!StringUtils.isBlank(status)) {
      statusList = StringHelper.getIntegerList(status);
    }
    return statusList;
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
        getToConsumeSubBomList(manufOrder.getBillOfMaterial(), manufOrder, productList);
    moList.addAll(this.generateChildMOs(manufOrder, childBomList, depth));
    return moList;
  }

  public List<Pair<BillOfMaterial, BigDecimal>> getToConsumeSubBomList(
      BillOfMaterial billOfMaterial, ManufOrder mo, List<Product> productList)
      throws AxelorException {
    List<Pair<BillOfMaterial, BigDecimal>> bomList = new ArrayList<>();

    for (BillOfMaterialLine boml : billOfMaterial.getBillOfMaterialLineList()) {
      Product product = boml.getProduct();
      if (productList != null && !productList.contains(product)) {
        continue;
      }

      BigDecimal qtyReq =
          computeToConsumeProdProductLineQuantity(
              mo.getBillOfMaterial().getQty(), mo.getQty(), boml.getQty());

      BillOfMaterial bom = boml.getBillOfMaterial();
      if (bom != null) {
        if (bom.getProdProcess() != null) {
          bomList.add(Pair.of(bom, qtyReq));
        }
      } else {
        BillOfMaterial defaultBOM =
            Beans.get(BillOfMaterialService.class).getDefaultBOM(product, null);

        if ((product.getProductSubTypeSelect()
                    == ProductRepository.PRODUCT_SUB_TYPE_FINISHED_PRODUCT
                || product.getProductSubTypeSelect()
                    == ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT)
            && defaultBOM != null
            && defaultBOM.getProdProcess() != null) {
          bomList.add(Pair.of(defaultBOM, qtyReq));
        }
      }
    }
    return bomList;
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

  @Transactional(rollbackOn = {Exception.class})
  public void merge(List<Long> ids) throws AxelorException {
    if (!canMerge(ids)) {
      throw new AxelorException(
          ManufOrder.class,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.MANUF_ORDER_NO_GENERATION));
    }
    List<ManufOrder> manufOrderList =
        manufOrderRepo.all().filter("self.id in (" + Joiner.on(",").join(ids) + ")").fetch();

    /* Init all the necessary values to create the new Manuf Order */
    Product product = manufOrderList.get(0).getProduct();
    StockLocation stockLocation = manufOrderList.get(0).getWorkshopStockLocation();
    Company company = manufOrderList.get(0).getCompany();
    BillOfMaterial billOfMaterial =
        manufOrderList.stream()
            .filter(x -> x.getBillOfMaterial().getVersionNumber() == 1)
            .findFirst()
            .get()
            .getBillOfMaterial();
    int priority = manufOrderList.stream().mapToInt(ManufOrder::getPrioritySelect).max().orElse(2);
    Unit unit = billOfMaterial.getUnit();
    BigDecimal qty = BigDecimal.ZERO;
    String note = "";

    ManufOrder mergedManufOrder = new ManufOrder();

    mergedManufOrder.setMoCommentFromSaleOrder("");
    mergedManufOrder.setMoCommentFromSaleOrderLine("");

    for (ManufOrder manufOrder : manufOrderList) {
      manufOrder.setStatusSelect(ManufOrderRepository.STATUS_MERGED);

      manufOrder.setManufOrderMergeResult(mergedManufOrder);
      for (ProductionOrder productionOrder : manufOrder.getProductionOrderSet()) {
        mergedManufOrder.addProductionOrderSetItem(productionOrder);
      }
      for (SaleOrder saleOrder : manufOrder.getSaleOrderSet()) {
        mergedManufOrder.addSaleOrderSetItem(saleOrder);
      }
      /*
       * If unit are the same, then add the qty If not, convert the unit and get the
       * converted qty
       */
      if (manufOrder.getUnit() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(ProductionExceptionMessage.MANUF_ORDER_MERGE_MISSING_UNIT));
      }
      if (manufOrder.getUnit().equals(unit)) {
        qty = qty.add(manufOrder.getQty());
      } else {
        BigDecimal qtyConverted =
            Beans.get(UnitConversionService.class)
                .convert(
                    manufOrder.getUnit(),
                    unit,
                    manufOrder.getQty(),
                    appBaseService.getNbDecimalDigitForQty(),
                    null);
        qty = qty.add(qtyConverted);
      }
      if (manufOrder.getNote() != null && !manufOrder.getNote().equals("")) {
        note += manufOrder.getManufOrderSeq() + " : " + manufOrder.getNote() + "\n";
      }

      if (!Strings.isNullOrEmpty(manufOrder.getMoCommentFromSaleOrder())) {
        mergedManufOrder.setMoCommentFromSaleOrder(
            mergedManufOrder
                .getMoCommentFromSaleOrder()
                .concat(System.lineSeparator())
                .concat(manufOrder.getMoCommentFromSaleOrder()));
      }

      if (!Strings.isNullOrEmpty(manufOrder.getMoCommentFromSaleOrderLine())) {
        mergedManufOrder.setMoCommentFromSaleOrderLine(
            mergedManufOrder
                .getMoCommentFromSaleOrderLine()
                .concat(System.lineSeparator())
                .concat(manufOrder.getMoCommentFromSaleOrderLine()));
      }
    }

    ProductionConfigService productionConfigService = Beans.get(ProductionConfigService.class);
    ProductionConfig productionConfig = productionConfigService.getProductionConfig(company);
    int scheduling = productionConfig.getScheduling();

    if (scheduling == ProductionConfigRepository.AS_SOON_AS_POSSIBLE_SCHEDULING) {
      Optional<LocalDateTime> minStartDate =
          manufOrderList.stream()
              .map(ManufOrder::getPlannedStartDateT)
              .filter(Objects::nonNull)
              .min(LocalDateTime::compareTo);

      minStartDate.ifPresent(mergedManufOrder::setPlannedStartDateT);
    } else {
      Optional<LocalDateTime> minEndDate =
          manufOrderList.stream()
              .map(ManufOrder::getPlannedEndDateT)
              .filter(Objects::nonNull)
              .min(LocalDateTime::compareTo);

      minEndDate.ifPresent(mergedManufOrder::setPlannedEndDateT);
    }

    /* Update the created manuf order */
    mergedManufOrder.setStatusSelect(ManufOrderRepository.STATUS_DRAFT);
    mergedManufOrder.setProduct(product);
    mergedManufOrder.setUnit(unit);
    mergedManufOrder.setWorkshopStockLocation(stockLocation);
    mergedManufOrder.setQty(qty);
    mergedManufOrder.setBillOfMaterial(billOfMaterial);
    mergedManufOrder.setCompany(company);
    mergedManufOrder.setPrioritySelect(priority);
    mergedManufOrder.setProdProcess(billOfMaterial.getProdProcess());
    mergedManufOrder.setNote(note);

    /*
     * Check the config to see if you directly plan the created manuf order or just
     * prefill the operations
     */
    if (appProductionService.isApp("production")
        && appProductionService.getAppProduction().getIsManufOrderPlannedAfterMerge()) {
      manufOrderPlanService.plan(mergedManufOrder);
    } else {
      preFillOperations(mergedManufOrder);
    }

    manufOrderRepo.save(mergedManufOrder);
  }

  public boolean canMerge(List<Long> ids) {
    List<ManufOrder> manufOrderList =
        manufOrderRepo.all().filter("self.id in (" + Joiner.on(",").join(ids) + ")").fetch();

    // I check if all the status of the manuf order in the list are Draft or
    // Planned. If not i can return false
    boolean allStatusDraftOrPlanned =
        manufOrderList.stream()
            .allMatch(
                x ->
                    x.getStatusSelect().equals(ManufOrderRepository.STATUS_DRAFT)
                        || x.getStatusSelect().equals(ManufOrderRepository.STATUS_PLANNED));
    if (!allStatusDraftOrPlanned) {
      return false;
    }
    // I check if all the products are the same. If not i return false
    Product product = manufOrderList.get(0).getProduct();
    boolean allSameProducts = manufOrderList.stream().allMatch(x -> x.getProduct().equals(product));
    if (!allSameProducts) {
      return false;
    }

    // Workshop management must be enabled to do the checking
    if (appProductionService.getAppProduction().getManageWorkshop()) {
      // Check if one of the workShopStockLocation is null
      boolean oneWorkShopIsNull =
          manufOrderList.stream().anyMatch(x -> x.getWorkshopStockLocation() == null);
      if (oneWorkShopIsNull) {
        return false;
      }

      // I check if all the stockLocation are the same. If not i return false
      StockLocation stockLocation = manufOrderList.get(0).getWorkshopStockLocation();
      boolean allSameLocation =
          manufOrderList.stream()
              .allMatch(
                  x ->
                      x.getWorkshopStockLocation() != null
                          && x.getWorkshopStockLocation().equals(stockLocation));
      if (!allSameLocation) {
        return false;
      }
    }

    // Check if one of the billOfMaterial is null
    boolean oneBillOfMaterialIsNull =
        manufOrderList.stream().anyMatch(x -> x.getBillOfMaterial() == null);
    if (oneBillOfMaterialIsNull) {
      return false;
    }

    // Check if one of the billOfMaterial has his version equal to 1
    boolean oneBillOfMaterialWithFirstVersion =
        manufOrderList.stream().anyMatch(x -> x.getBillOfMaterial().getVersionNumber() == 1);
    if (!oneBillOfMaterialWithFirstVersion) {
      return false;
    }

    // I check if all the billOfMaterial are the same. If not i will check
    // if all version are compatible, and if not i can return false
    BillOfMaterial billOfMaterial =
        manufOrderList.stream()
            .filter(x -> x.getBillOfMaterial().getVersionNumber() == 1)
            .findFirst()
            .get()
            .getBillOfMaterial();
    boolean allSameOrCompatibleBillOfMaterial =
        manufOrderList.stream()
            .allMatch(
                x ->
                    x.getBillOfMaterial().equals(billOfMaterial)
                        || billOfMaterial.equals(
                            x.getBillOfMaterial().getOriginalBillOfMaterial()));
    if (!allSameOrCompatibleBillOfMaterial) {
      return false;
    }

    return true;
  }

  @Override
  public List<ManufOrder> getChildrenManufOrder(ManufOrder manufOrder) {
    return manufOrderRepo
        .all()
        .filter("self.parentMO = :manufOrder")
        .bind("manufOrder", manufOrder)
        .fetch();
  }

  @Override
  public List<Long> planSelectedOrdersAndDiscardOthers(List<Map<String, Object>> manufOrders)
      throws AxelorException {
    List<Long> ids = new ArrayList<>();
    Map<String, String> sequenceParentSeqMap = new HashMap<>();
    Map<String, ManufOrder> seqMOMap = new HashMap<>();
    List<ManufOrder> generatedMOList = new ArrayList<>();

    for (Map<String, Object> manufOrderMap : manufOrders) {
      ManufOrder manufOrder = Mapper.toBean(ManufOrder.class, manufOrderMap);
      Product product = Beans.get(ProductRepository.class).find(manufOrder.getProduct().getId());

      String backupSeq = manufOrder.getManualMOSeq();

      ManufOrder parentMO = manufOrder.getParentMO();
      Long parentMOId = parentMO.getId();
      if (parentMOId != null) {
        if (!seqMOMap.containsKey(parentMOId.toString())) {
          seqMOMap.put(parentMOId.toString(), parentMO);
        }
        sequenceParentSeqMap.put(backupSeq, parentMOId.toString());
      } else {
        sequenceParentSeqMap.put(backupSeq, parentMO.getManualMOSeq());
      }

      if ((boolean) manufOrderMap.get("selected")) {
        BillOfMaterial billOfMaterial = manufOrder.getBillOfMaterial();
        billOfMaterial = Beans.get(BillOfMaterialRepository.class).find(billOfMaterial.getId());
        Partner clientPartner = manufOrder.getClientPartner();
        if (ObjectUtils.notEmpty(clientPartner)) {
          clientPartner = partnerRepository.find(clientPartner.getId());
        }
        manufOrder =
            generateManufOrder(
                product,
                manufOrder.getQty().multiply(billOfMaterial.getQty()),
                manufOrder.getPrioritySelect(),
                IS_TO_INVOICE,
                billOfMaterial,
                manufOrder.getPlannedStartDateT(),
                manufOrder.getPlannedEndDateT(),
                ManufOrderOriginTypeProduction.ORIGIN_TYPE_OTHER);

        manufOrder.setClientPartner(clientPartner);
        manufOrder.setManualMOSeq(backupSeq);
        seqMOMap.put(backupSeq, manufOrder);
        ids.add(manufOrder.getId());
        generatedMOList.add(manufOrder);
      }
    }
    this.setParentMos(sequenceParentSeqMap, seqMOMap, generatedMOList);
    return ids;
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
              parentMO.getPrioritySelect(),
              childBom,
              null,
              parentMO.getPlannedStartDateT());

      childMO.setManualMOSeq(this.getManualSequence());
      childMO.setParentMO(parentMO);
      childMO.setClientPartner(parentMO.getClientPartner());
      manufOrderList.add(childMO);

      manufOrderList.addAll(
          this.generateChildMOs(
              childMO, getToConsumeSubBomList(childMO.getBillOfMaterial(), childMO, null), depth));
    }
    return manufOrderList;
  }

  protected String getManualSequence() {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
  }

  @Transactional
  protected void setParentMos(
      Map<String, String> sequenceParentSeqMap,
      Map<String, ManufOrder> seqMOMap,
      List<ManufOrder> generatedMOList) {
    for (ManufOrder mo : generatedMOList) {
      String seq = mo.getManualMOSeq();
      ManufOrder parentMO = this.getParentMO(sequenceParentSeqMap, seqMOMap, seq);
      mo.setParentMO(parentMO);
    }
  }

  protected ManufOrder getParentMO(
      Map<String, String> sequenceParentSeqMap, Map<String, ManufOrder> seqMOMap, String seq) {
    ManufOrder parentMO = null;
    String parentSeq = sequenceParentSeqMap.get(seq);

    if (seqMOMap.containsKey(parentSeq)) {
      parentMO = seqMOMap.get(parentSeq);
      parentMO = manufOrderRepo.find(parentMO.getId());
    } else {
      parentMO = this.getParentMO(sequenceParentSeqMap, seqMOMap, parentSeq);
    }
    return parentMO;
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
      if (billOfMaterialLine.getHasNoManageStock()) {
        continue;
      }
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

  @Override
  public Map<Product, BigDecimal> getMissingComponents(ManufOrder manufOrder)
      throws AxelorException {
    Map<Product, BigDecimal> missingProductsMap = new HashMap<>();
    Company company = manufOrder.getCompany();
    BillOfMaterial billOfMaterial = manufOrder.getBillOfMaterial();

    if (company == null
        || billOfMaterial == null
        || billOfMaterial.getQty().compareTo(BigDecimal.ZERO) <= 0
        || CollectionUtils.isEmpty(billOfMaterial.getBillOfMaterialLineList())) {
      return missingProductsMap;
    }

    BigDecimal bomQty = billOfMaterial.getQty();
    BigDecimal qty = manufOrder.getQty();

    Map<Product, Pair<BigDecimal, Unit>> bomLineMap =
        billOfMaterial.getBillOfMaterialLineList().stream()
            .filter(bomLine -> !bomLine.getHasNoManageStock())
            .collect(
                Collectors.toMap(
                    BillOfMaterialLine::getProduct,
                    line -> Pair.of(line.getQty(), line.getUnit()),
                    (x, y) -> Pair.of(x.getLeft().add(y.getLeft()), x.getRight())));

    for (Entry<Product, Pair<BigDecimal, Unit>> billOfMaterialLine : bomLineMap.entrySet()) {
      Product product = billOfMaterialLine.getKey();
      BigDecimal bomLineQty = billOfMaterialLine.getValue().getLeft();
      Unit bomLineUnit = billOfMaterialLine.getValue().getRight();
      BigDecimal availableQty = productStockLocationService.getAvailableQty(product, company, null);
      availableQty =
          unitConversionService.convert(
              product.getUnit(), bomLineUnit, availableQty, availableQty.scale(), product);
      BigDecimal qtyNeeded =
          qty.multiply(bomLineQty)
              .divide(bomQty, appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP);

      if (availableQty.compareTo(BigDecimal.ZERO) >= 0
          && qtyNeeded.compareTo(BigDecimal.ZERO) > 0
          && qtyNeeded.compareTo(availableQty) > 0) {
        BigDecimal missingQty =
            qtyNeeded
                .subtract(availableQty)
                .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP);
        missingProductsMap.put(product, missingQty);
      }
    }
    return missingProductsMap;
  }
}
