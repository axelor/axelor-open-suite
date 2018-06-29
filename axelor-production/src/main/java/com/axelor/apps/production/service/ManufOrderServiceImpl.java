/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.ProductVariantService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.ProdResidualProduct;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.config.StockConfigProductionService;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.tool.date.DurationTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.examples.projectjobscheduling.domain.Allocation;
import org.optaplanner.examples.projectjobscheduling.domain.ExecutionMode;
import org.optaplanner.examples.projectjobscheduling.domain.Job;
import org.optaplanner.examples.projectjobscheduling.domain.JobType;
import org.optaplanner.examples.projectjobscheduling.domain.Project;
import org.optaplanner.examples.projectjobscheduling.domain.ResourceRequirement;
import org.optaplanner.examples.projectjobscheduling.domain.Schedule;
import org.optaplanner.examples.projectjobscheduling.domain.resource.GlobalResource;
import org.optaplanner.examples.projectjobscheduling.domain.resource.LocalResource;
import org.optaplanner.examples.projectjobscheduling.domain.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManufOrderServiceImpl implements ManufOrderService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected SequenceService sequenceService;
  protected OperationOrderService operationOrderService;
  protected ManufOrderWorkflowService manufOrderWorkflowService;
  protected ProductVariantService productVariantService;
  protected AppProductionService appProductionService;
  protected ManufOrderRepository manufOrderRepo;

  @Inject
  public ManufOrderServiceImpl(
      SequenceService sequenceService,
      OperationOrderService operationOrderService,
      ManufOrderWorkflowService manufOrderWorkflowService,
      ProductVariantService productVariantService,
      AppProductionService appProductionService,
      ManufOrderRepository manufOrderRepo) {
    this.sequenceService = sequenceService;
    this.operationOrderService = operationOrderService;
    this.manufOrderWorkflowService = manufOrderWorkflowService;
    this.productVariantService = productVariantService;
    this.appProductionService = appProductionService;
    this.manufOrderRepo = manufOrderRepo;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public ManufOrder generateManufOrder(
      Product product,
      BigDecimal qtyRequested,
      int priority,
      boolean isToInvoice,
      BillOfMaterial billOfMaterial,
      LocalDateTime plannedStartDateT)
      throws AxelorException {

    if (billOfMaterial == null) {
      billOfMaterial = this.getBillOfMaterial(product);
    }

    Company company = billOfMaterial.getCompany();

    BigDecimal qty = qtyRequested.divide(billOfMaterial.getQty(), 2, RoundingMode.HALF_EVEN);

    ManufOrder manufOrder =
        this.createManufOrder(
            product, qty, priority, IS_TO_INVOICE, company, billOfMaterial, plannedStartDateT);

    manufOrder = manufOrderWorkflowService.plan(manufOrder);

    return manufOrderRepo.save(manufOrder);
  }

  @Override
  public void createToConsumeProdProductList(ManufOrder manufOrder) {

    BigDecimal manufOrderQty = manufOrder.getQty();

    BillOfMaterial billOfMaterial = manufOrder.getBillOfMaterial();

    if (billOfMaterial.getBillOfMaterialSet() != null) {

      for (BillOfMaterial billOfMaterialLine : billOfMaterial.getBillOfMaterialSet()) {

        if (!billOfMaterialLine.getHasNoManageStock()) {

          Product product =
              productVariantService.getProductVariant(
                  manufOrder.getProduct(), billOfMaterialLine.getProduct());

          BigDecimal qty =
              billOfMaterialLine
                  .getQty()
                  .multiply(manufOrderQty)
                  .setScale(2, RoundingMode.HALF_EVEN);

          manufOrder.addToConsumeProdProductListItem(
              new ProdProduct(product, qty, billOfMaterialLine.getUnit()));
        }
      }
    }
  }

  @Override
  public void createToProduceProdProductList(ManufOrder manufOrder) {

    BigDecimal manufOrderQty = manufOrder.getQty();

    BillOfMaterial billOfMaterial = manufOrder.getBillOfMaterial();

    BigDecimal qty =
        billOfMaterial.getQty().multiply(manufOrderQty).setScale(2, RoundingMode.HALF_EVEN);

    // add the produced product
    manufOrder.addToProduceProdProductListItem(
        new ProdProduct(manufOrder.getProduct(), qty, billOfMaterial.getUnit()));

    // Add the residual products
    if (appProductionService.getAppProduction().getManageResidualProductOnBom()
        && billOfMaterial.getProdResidualProductList() != null) {

      for (ProdResidualProduct prodResidualProduct : billOfMaterial.getProdResidualProductList()) {

        Product product =
            productVariantService.getProductVariant(
                manufOrder.getProduct(), prodResidualProduct.getProduct());

        qty =
            prodResidualProduct
                .getQty()
                .multiply(manufOrderQty)
                .setScale(
                    appProductionService.getNbDecimalDigitForBomQty(), RoundingMode.HALF_EVEN);

        manufOrder.addToProduceProdProductListItem(
            new ProdProduct(product, qty, prodResidualProduct.getUnit()));
      }
    }
  }

  @Override
  public ManufOrder createManufOrder(
      Product product,
      BigDecimal qty,
      int priority,
      boolean isToInvoice,
      Company company,
      BillOfMaterial billOfMaterial,
      LocalDateTime plannedStartDateT)
      throws AxelorException {

    logger.debug("Création d'un OF {}", priority);

    ProdProcess prodProcess = billOfMaterial.getProdProcess();

    ManufOrder manufOrder =
        new ManufOrder(
            qty,
            company,
            null,
            priority,
            this.isManagedConsumedProduct(billOfMaterial),
            billOfMaterial,
            product,
            prodProcess,
            plannedStartDateT,
            ManufOrderRepository.STATUS_DRAFT);

    if (prodProcess != null && prodProcess.getProdProcessLineList() != null) {
      for (ProdProcessLine prodProcessLine :
          this._sortProdProcessLineByPriority(prodProcess.getProdProcessLineList())) {

        manufOrder.addOperationOrderListItem(
            operationOrderService.createOperationOrder(manufOrder, prodProcessLine));
      }
    }

    if (!manufOrder.getIsConsProOnOperation()) {
      this.createToConsumeProdProductList(manufOrder);
    }

    this.createToProduceProdProductList(manufOrder);

    return manufOrder;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void preFillOperations(ManufOrder manufOrder) throws AxelorException {

    BillOfMaterial billOfMaterial = manufOrder.getBillOfMaterial();

    if (manufOrder.getProdProcess() == null) {
      manufOrder.setProdProcess(billOfMaterial.getProdProcess());
    }
    ProdProcess prodProcess = manufOrder.getProdProcess();

    if (manufOrder.getPlannedStartDateT() == null) {
      manufOrder.setPlannedStartDateT(appProductionService.getTodayDateTime().toLocalDateTime());
    }

    if (prodProcess != null && prodProcess.getProdProcessLineList() != null) {

      for (ProdProcessLine prodProcessLine :
          this._sortProdProcessLineByPriority(prodProcess.getProdProcessLineList())) {
        manufOrder.addOperationOrderListItem(
            operationOrderService.createOperationOrder(manufOrder, prodProcessLine));
      }
    }

    manufOrderRepo.save(manufOrder);

    manufOrder.setPlannedEndDateT(manufOrderWorkflowService.computePlannedEndDateT(manufOrder));

    manufOrderRepo.save(manufOrder);
  }

  /**
   * Trier une liste de ligne de règle de template
   *
   * @param templateRuleLine
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
  public String getManufOrderSeq() throws AxelorException {

    String seq = sequenceService.getSequenceNumber(SequenceRepository.MANUF_ORDER);

    if (seq == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.MANUF_ORDER_SEQ));
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
          I18n.get(IExceptionMessage.PRODUCTION_ORDER_SALES_ORDER_NO_BOM),
          product.getName(),
          product.getCode());
    }

    return billOfMaterial;
  }

  @Override
  public BigDecimal getProducedQuantity(ManufOrder manufOrder) {
    for (StockMoveLine stockMoveLine : manufOrder.getProducedStockMoveLineList()) {
      if (stockMoveLine.getProduct().equals(manufOrder.getProduct())) {
        return stockMoveLine.getRealQty();
      }
    }
    return BigDecimal.ZERO;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
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
    AppBaseService appBaseService = Beans.get(AppBaseService.class);

    StockConfig stockConfig = stockConfigService.getStockConfig(company);
    StockLocation virtualStockLocation =
        stockConfigService.getProductionVirtualStockLocation(stockConfig);
    StockLocation wasteStockLocation = stockConfigService.getWasteStockLocation(stockConfig);

    wasteStockMove =
        stockMoveService.createStockMove(
            virtualStockLocation.getAddress(),
            wasteStockLocation.getAddress(),
            company,
            virtualStockLocation,
            wasteStockLocation,
            null,
            appBaseService.getTodayDate(),
            manufOrder.getWasteProdDescription(),
            StockMoveRepository.TYPE_INTERNAL);

    for (ProdProduct prodProduct : manufOrder.getWasteProdProductList()) {
      stockMoveLineService.createStockMoveLine(
          prodProduct.getProduct(),
          prodProduct.getProduct().getName(),
          prodProduct.getProduct().getDescription(),
          prodProduct.getQty(),
          prodProduct.getProduct().getCostPrice(),
          prodProduct.getUnit(),
          wasteStockMove,
          StockMoveLineService.TYPE_WASTE_PRODUCTIONS,
          false,
          BigDecimal.ZERO);
    }

    stockMoveService.validate(wasteStockMove);

    manufOrder.setWasteStockMove(wasteStockMove);
    return wasteStockMove;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void updatePlannedQty(ManufOrder manufOrder) {
    manufOrder.clearToConsumeProdProductList();
    manufOrder.clearToProduceProdProductList();
    this.createToConsumeProdProductList(manufOrder);
    this.createToProduceProdProductList(manufOrder);

    manufOrderRepo.save(manufOrder);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void updateRealQty(ManufOrder manufOrder, BigDecimal qtyToUpdate) throws AxelorException {
    ManufOrderStockMoveService manufOrderStockMoveService =
        Beans.get(ManufOrderStockMoveService.class);
    if (!manufOrder.getIsConsProOnOperation()) {
      manufOrderStockMoveService.createNewConsumedStockMoveLineList(manufOrder, qtyToUpdate);
      updateDiffProdProductList(manufOrder);
    } else {
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
        Beans.get(OperationOrderStockMoveService.class)
            .createNewConsumedStockMoveLineList(operationOrder, qtyToUpdate);
        Beans.get(OperationOrderService.class).updateDiffProdProductList(operationOrder);
      }
    }

    manufOrderStockMoveService.createNewProducedStockMoveLineList(manufOrder, qtyToUpdate);
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
          stockMoveLineList
              .stream()
              .filter(stockMoveLine1 -> stockMoveLine1.getProduct() != null)
              .filter(stockMoveLine1 -> stockMoveLine1.getProduct().equals(product))
              .collect(Collectors.toList());
      if (stockMoveLineProductList.isEmpty()) {
        continue;
      }
      BigDecimal diffQty = computeDiffQty(prodProduct, stockMoveLineProductList, product);
      if (diffQty.compareTo(BigDecimal.ZERO) != 0) {
        ProdProduct diffProdProduct = new ProdProduct();
        diffProdProduct.setQty(diffQty);
        diffProdProduct.setProduct(product);
        diffProdProduct.setUnit(newUnit);
        diffConsumeList.add(diffProdProduct);
      }
    }
    // There are stock move lines with products that are not available in
    // prod product list. It needs to appear in the prod product list
    List<StockMoveLine> stockMoveLineMissingProductList =
        stockMoveLineList
            .stream()
            .filter(stockMoveLine1 -> stockMoveLine1.getProduct() != null)
            .filter(
                stockMoveLine1 ->
                    !prodProductList
                        .stream()
                        .map(ProdProduct::getProduct)
                        .collect(Collectors.toList())
                        .contains(stockMoveLine1.getProduct()))
            .collect(Collectors.toList());
    for (StockMoveLine stockMoveLine : stockMoveLineMissingProductList) {
      if (stockMoveLine.getQty().compareTo(BigDecimal.ZERO) != 0) {
        ProdProduct diffProdProduct = new ProdProduct();
        diffProdProduct.setQty(stockMoveLine.getQty());
        diffProdProduct.setProduct(stockMoveLine.getProduct());
        diffProdProduct.setUnit(stockMoveLine.getUnit());
        diffConsumeList.add(diffProdProduct);
      }
    }
    return diffConsumeList;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void updateConsumedStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException {
    this.updateDiffProdProductList(manufOrder);
    List<StockMoveLine> consumedStockMoveLineList = manufOrder.getConsumedStockMoveLineList();
    if (consumedStockMoveLineList == null) {
      return;
    }
    ManufOrderStockMoveService manufOrderStockMoveService =
        Beans.get(ManufOrderStockMoveService.class);
    Optional<StockMove> stockMoveOpt =
        manufOrderStockMoveService.getPlannedStockMove(manufOrder.getInStockMoveList());
    if (!stockMoveOpt.isPresent()) {
      return;
    }
    StockMove stockMove = stockMoveOpt.get();

    updateStockMoveFromManufOrder(consumedStockMoveLineList, stockMove);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void updateProducedStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException {
    List<StockMoveLine> producedStockMoveLineList = manufOrder.getProducedStockMoveLineList();
    ManufOrderStockMoveService manufOrderStockMoveService =
        Beans.get(ManufOrderStockMoveService.class);
    Optional<StockMove> stockMoveOpt =
        manufOrderStockMoveService.getPlannedStockMove(manufOrder.getOutStockMoveList());
    if (!stockMoveOpt.isPresent()) {
      return;
    }
    StockMove stockMove = stockMoveOpt.get();

    updateStockMoveFromManufOrder(producedStockMoveLineList, stockMove);
  }

  @Override
  public void updateStockMoveFromManufOrder(
      List<StockMoveLine> stockMoveLineList, StockMove stockMove) throws AxelorException {
    if (stockMoveLineList == null) {
      return;
    }

    StockMoveService stockMoveService = Beans.get(StockMoveService.class);
    stockMoveService.cancel(stockMove);

    try {
      // add missing lines in stock move
      stockMoveLineList
          .stream()
          .filter(stockMoveLine -> stockMoveLine.getStockMove() == null)
          .forEach(stockMove::addStockMoveLineListItem);

      // remove lines in stock move removed in manuf order
      if (stockMove.getStockMoveLineList() != null) {
        stockMove
            .getStockMoveLineList()
            .removeIf(stockMoveLine -> !stockMoveLineList.contains(stockMoveLine));
      }
    } finally {
      stockMoveService.plan(stockMove);
    }
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
                    .convertWithProduct(
                        stockMoveLine.getUnit(),
                        prodProduct.getUnit(),
                        stockMoveLine.getQty(),
                        product));
      } else {
        consumedQty = consumedQty.add(stockMoveLine.getQty());
      }
    }
    return consumedQty.subtract(prodProduct.getQty());
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void optaPlan(ManufOrder manufOrder) throws AxelorException {
    optaPlan(Lists.newArrayList(manufOrder));
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void optaPlan(List<ManufOrder> manufOrderList) throws AxelorException {
    // Build the Solver
    SolverFactory<Schedule> solverFactory =
        SolverFactory.createFromXmlResource(
            "projectjobscheduling/solver/projectJobSchedulingSolverConfig.xml");
    Solver<Schedule> solver = solverFactory.buildSolver();

    // Custom Unsolved Job Scheduling
    Schedule unsolvedJobScheduling = new Schedule();
    unsolvedJobScheduling.setJobList(new ArrayList<Job>());
    unsolvedJobScheduling.setProjectList(new ArrayList<Project>());
    unsolvedJobScheduling.setResourceList(new ArrayList<Resource>());
    unsolvedJobScheduling.setResourceRequirementList(new ArrayList<ResourceRequirement>());
    unsolvedJobScheduling.setExecutionModeList(new ArrayList<ExecutionMode>());
    unsolvedJobScheduling.setAllocationList(new ArrayList<Allocation>());

    // Create Resources
    List<WorkCenter> workCenterList = Beans.get(WorkCenterRepository.class).all().fetch();
    Map<String, Resource> machineCodeToResourceMap = new HashMap<>();
    for (WorkCenter workCenter : workCenterList) {
      Resource curResource = new GlobalResource();

      curResource.setCapacity(1);
      long resourceId =
          unsolvedJobScheduling.getResourceList().size() > 0
              ? unsolvedJobScheduling
                      .getResourceList()
                      .get(unsolvedJobScheduling.getResourceList().size() - 1)
                      .getId()
                  + 1
              : 0;
      curResource.setId(resourceId);

      machineCodeToResourceMap.put(workCenter.getCode(), curResource);

      unsolvedJobScheduling.getResourceList().add(curResource);
    }

    Map<Long, ManufOrder> projectIdToManufOrderMap = new HashMap<>();
    Map<Long, ProdProcessLine> allocationIdToProdProcessLineMap = new HashMap<>();
    for (ManufOrder manufOrder : manufOrderList) {
      // Create project
      Project project =
          createProject(
              unsolvedJobScheduling,
              manufOrder,
              machineCodeToResourceMap,
              allocationIdToProdProcessLineMap);
      projectIdToManufOrderMap.put(project.getId(), manufOrder);
    }

    // Solve the problem
    Schedule solvedJobScheduling = solver.solve(unsolvedJobScheduling);

    for (ManufOrder manufOrder : manufOrderList) {
      manufOrder.getOperationOrderList().clear();
      manufOrder.setStatusSelect(ManufOrderRepository.STATUS_PLANNED);
      if (manufOrder.getManufOrderSeq() == null)
        manufOrder.setManufOrderSeq(Beans.get(ManufOrderService.class).getManufOrderSeq());
      manufOrder.setPlannedStartDateT(null);
      manufOrder.setPlannedEndDateT(null);
    }
    for (Allocation allocation : solvedJobScheduling.getAllocationList()) {
      OperationOrder operationOrder = new OperationOrder();
      ProdProcessLine prodProcessLine = allocationIdToProdProcessLineMap.get(allocation.getId());

      if (prodProcessLine != null) {
        ManufOrder manufOrder = projectIdToManufOrderMap.get(allocation.getProject().getId());

        operationOrder.setOperationName(prodProcessLine.getName());
        LocalDateTime now =
            Beans.get(AppProductionService.class).getTodayDateTime().toLocalDateTime();

        LocalDateTime operationOrderPlannedStartDate = now.plusMinutes(allocation.getStartDate());
        operationOrder.setPlannedStartDateT(operationOrderPlannedStartDate);
        if (manufOrder.getPlannedStartDateT() == null
            || manufOrder.getPlannedStartDateT().isAfter(operationOrderPlannedStartDate)) {
          manufOrder.setPlannedStartDateT(operationOrderPlannedStartDate);
        }

        LocalDateTime operationOrderPlannedEndDate = now.plusMinutes(allocation.getEndDate());
        operationOrder.setPlannedEndDateT(operationOrderPlannedEndDate);
        if (manufOrder.getPlannedEndDateT() == null
            || manufOrder.getPlannedEndDateT().isBefore(operationOrderPlannedEndDate)) {
          manufOrder.setPlannedEndDateT(operationOrderPlannedEndDate);
        }

        operationOrder.setPlannedDuration(
            DurationTool.getSecondsDuration(
                Duration.between(
                    operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT())));

        operationOrder.setPriority(prodProcessLine.getPriority());
        operationOrder.setManufOrder(manufOrder);
        operationOrder.setWorkCenter(prodProcessLine.getWorkCenter());
        operationOrder.setMachineWorkCenter(prodProcessLine.getWorkCenter());
        operationOrder.setStatusSelect(OperationOrderRepository.STATUS_PLANNED);
        operationOrder.setProdProcessLine(prodProcessLine);

        manufOrder.addOperationOrderListItem(operationOrder);
      }
    }
  }

  private int getCriticalPathDuration(Project project) {
    Job sourceJob = null;
    for (Job curJob : project.getJobList()) {
      if (curJob.getJobType() == JobType.SOURCE) {
        sourceJob = curJob;
        break;
      }
    }
    if (sourceJob != null) {
      return getCriticalPathDuration(sourceJob);
    }
    return 0;
  }

  private int getCriticalPathDuration(Job job) {
    if (job.getJobType() == JobType.SINK) {
      return 0;
    } else {
      int maximumCriticalPathDuration = 0;
      for (Job successorJob : job.getSuccessorJobList()) {
        int curCriticalPathDuration = getCriticalPathDuration(successorJob);
        if (curCriticalPathDuration > maximumCriticalPathDuration) {
          maximumCriticalPathDuration = curCriticalPathDuration;
        }
      }
      return maximumCriticalPathDuration + maximumExecutionModeDuration(job);
    }
  }

  private int maximumExecutionModeDuration(Job job) {
    int maximumExecutionModeDuration = 0;
    if (job.getExecutionModeList() != null) {
      for (ExecutionMode executionMode : job.getExecutionModeList()) {
        if (maximumExecutionModeDuration < executionMode.getDuration()) {
          maximumExecutionModeDuration = executionMode.getDuration();
        }
      }
      return maximumExecutionModeDuration;
    }
    return 0;
  }

  private Project createProject(
      Schedule unsolvedJobScheduling,
      ManufOrder manufOrder,
      Map<String, Resource> machineCodeToResourceMap,
      Map<Long, ProdProcessLine> allocationIdToProdProcessLineMap) {

    List<ProdProcessLine> prodProcessLineList =
        manufOrder.getProdProcess().getProdProcessLineList();
    Map<Integer, List<ProdProcessLine>> priorityToProdProcessLineMap = new HashMap<>();
    for (ProdProcessLine curProdProcessLine : prodProcessLineList) {
      int priority = curProdProcessLine.getPriority();
      if (!priorityToProdProcessLineMap.containsKey(priority)) {
        priorityToProdProcessLineMap.put(priority, new ArrayList<ProdProcessLine>());
      }
      priorityToProdProcessLineMap.get(priority).add(curProdProcessLine);
    }
    List<Integer> sortedPriorityList =
        new ArrayList<Integer>(new TreeSet<Integer>(priorityToProdProcessLineMap.keySet()));
    Map<Integer, ArrayList<Job>> priorityToJobMap = new HashMap<>();
    Map<Integer, ArrayList<Allocation>> priorityToAllocationMap = new HashMap<>();
    for (Integer priority : sortedPriorityList) {
      priorityToJobMap.put(priority, new ArrayList<Job>());
      priorityToAllocationMap.put(priority, new ArrayList<Allocation>());
    }

    Project project = new Project();
    long projectId =
        unsolvedJobScheduling.getProjectList().size() > 0
            ? unsolvedJobScheduling
                    .getProjectList()
                    .get(unsolvedJobScheduling.getProjectList().size() - 1)
                    .getId()
                + 1
            : 0;
    project.setId(projectId);
    project.setJobList(new ArrayList<Job>());
    project.setLocalResourceList(new ArrayList<LocalResource>());
    project.setReleaseDate(0);
    project.setCriticalPathDuration(getCriticalPathDuration(project));
    unsolvedJobScheduling.getProjectList().add(project);

    Job sourceJob = new Job();
    long sourceJobId =
        unsolvedJobScheduling.getJobList().size() > 0
            ? unsolvedJobScheduling
                    .getJobList()
                    .get(unsolvedJobScheduling.getJobList().size() - 1)
                    .getId()
                + 1
            : 0;
    sourceJob.setId(sourceJobId);
    sourceJob.setProject(project);
    sourceJob.setJobType(JobType.SOURCE);
    sourceJob.setSuccessorJobList(priorityToJobMap.get(sortedPriorityList.get(0)));
    project.getJobList().add(sourceJob);
    unsolvedJobScheduling.getJobList().add(sourceJob);

    Allocation sourceAllocation = new Allocation();
    sourceAllocation.setPredecessorsDoneDate(0);
    sourceAllocation.setId((long) (projectId * 100));
    sourceAllocation.setSuccessorAllocationList(
        priorityToAllocationMap.get(sortedPriorityList.get(0)));
    sourceAllocation.setJob(sourceJob);
    unsolvedJobScheduling.getAllocationList().add(sourceAllocation);
    List<Allocation> sourceAllocationList = new ArrayList<>();
    sourceAllocationList.add(sourceAllocation);

    Job sinkJob = new Job();
    long sinkJobId =
        unsolvedJobScheduling.getJobList().size() > 0
            ? unsolvedJobScheduling
                    .getJobList()
                    .get(unsolvedJobScheduling.getJobList().size() - 1)
                    .getId()
                + 1
            : 0;
    sinkJob.setId(sinkJobId);
    sinkJob.setProject(project);
    sinkJob.setJobType(JobType.SINK);
    project.getJobList().add(sinkJob);
    unsolvedJobScheduling.getJobList().add(sinkJob);
    List<Job> sinkJobList = new ArrayList<>();
    sinkJobList.add(sinkJob);

    Allocation sinkAllocation = new Allocation();
    sinkAllocation.setPredecessorsDoneDate(0);
    sinkAllocation.setId((long) (projectId * 100 + prodProcessLineList.size() + 1));
    sinkAllocation.setPredecessorAllocationList(
        priorityToAllocationMap.get(sortedPriorityList.get(sortedPriorityList.size() - 1)));
    sinkAllocation.setSuccessorAllocationList(new ArrayList<Allocation>());
    sinkAllocation.setJob(sinkJob);
    List<Allocation> sinkAllocationList = new ArrayList<>();
    sinkAllocationList.add(sinkAllocation);

    int allocationIdx = 0;
    for (int priorityIdx = 0; priorityIdx < sortedPriorityList.size(); priorityIdx++) {
      int priority = sortedPriorityList.get(priorityIdx);
      for (ProdProcessLine prodProcessLine : priorityToProdProcessLineMap.get(priority)) {
        // Job
        Job job = new Job();
        long jobId =
            unsolvedJobScheduling.getJobList().size() > 0
                ? unsolvedJobScheduling
                        .getJobList()
                        .get(unsolvedJobScheduling.getJobList().size() - 1)
                        .getId()
                    + 1
                : 0;
        job.setId(jobId);
        job.setExecutionModeList(new ArrayList<ExecutionMode>());
        job.setJobType(JobType.STANDARD);
        job.setProject(project);
        if (priorityIdx < sortedPriorityList.size() - 1) {
          job.setSuccessorJobList(priorityToJobMap.get(sortedPriorityList.get(priorityIdx + 1)));
        } else {
          job.setSuccessorJobList(sinkJobList);
        }

        unsolvedJobScheduling.getJobList().add(job);

        priorityToJobMap.get(priority).add(job);

        project.getJobList().add(job);

        // Execution Mode
        ExecutionMode executionMode = new ExecutionMode();
        long executionModeId =
            unsolvedJobScheduling.getExecutionModeList().size() > 0
                ? unsolvedJobScheduling
                        .getExecutionModeList()
                        .get(unsolvedJobScheduling.getExecutionModeList().size() - 1)
                        .getId()
                    + 1
                : 0;
        executionMode.setId(executionModeId);
        executionMode.setJob(job);
        executionMode.setResourceRequirementList(new ArrayList<ResourceRequirement>());
        long duration = 0;
        if (prodProcessLine.getWorkCenter().getWorkCenterTypeSelect() != 1) {
          duration =
              (long)
                  (prodProcessLine.getWorkCenter().getDurationPerCycle()
                      * Math.ceil(
                          (float) manufOrder.getQty().intValue()
                              / prodProcessLine
                                  .getWorkCenter()
                                  .getMaxCapacityPerCycle()
                                  .intValue()));
        } else if (prodProcessLine.getWorkCenter().getWorkCenterTypeSelect() == 1) {
          duration =
              prodProcessLine.getWorkCenter().getProdHumanResourceList().get(0).getDuration()
                  * manufOrder.getQty().intValue();
        }
        executionMode.setDuration((int) duration / 60);

        unsolvedJobScheduling.getExecutionModeList().add(executionMode);

        job.getExecutionModeList().add(executionMode);

        // Resource Requirement
        ResourceRequirement resourceRequirement = new ResourceRequirement();
        long resourceRequirementId =
            unsolvedJobScheduling.getResourceRequirementList().size() > 0
                ? unsolvedJobScheduling
                        .getResourceRequirementList()
                        .get(unsolvedJobScheduling.getResourceRequirementList().size() - 1)
                        .getId()
                    + 1
                : 0;
        resourceRequirement.setId(resourceRequirementId);
        resourceRequirement.setExecutionMode(executionMode);
        Resource resource = machineCodeToResourceMap.get(prodProcessLine.getWorkCenter().getCode());
        resourceRequirement.setResource(resource);
        resourceRequirement.setRequirement(1);
        executionMode.getResourceRequirementList().add(resourceRequirement);

        unsolvedJobScheduling.getResourceRequirementList().add(resourceRequirement);

        // Allocation
        Allocation allocation = new Allocation();
        Long allocationId = (long) (projectId * 100 + (allocationIdx + 1));
        allocation.setId(allocationId);
        allocationIdx++;
        allocation.setJob(job);
        List<Allocation> predecessorAllocationList =
            priorityIdx > 0
                ? priorityToAllocationMap.get(sortedPriorityList.get(priorityIdx - 1))
                : sourceAllocationList;
        allocation.setPredecessorAllocationList(predecessorAllocationList);
        List<Allocation> successorAllocationList =
            priorityIdx < sortedPriorityList.size() - 1
                ? priorityToAllocationMap.get(sortedPriorityList.get(priorityIdx + 1))
                : sinkAllocationList;
        allocation.setSuccessorAllocationList(successorAllocationList);
        allocationIdToProdProcessLineMap.put(allocationId, prodProcessLine);
        allocation.setPredecessorsDoneDate(0);
        allocation.setSourceAllocation(sourceAllocation);
        allocation.setSinkAllocation(sinkAllocation);
        allocation.setPredecessorsDoneDate(0);

        unsolvedJobScheduling.getAllocationList().add(allocation);

        priorityToAllocationMap.get(priority).add(allocation);
      }
    }

    unsolvedJobScheduling.getAllocationList().add(sinkAllocation);

    return project;
  }
}
