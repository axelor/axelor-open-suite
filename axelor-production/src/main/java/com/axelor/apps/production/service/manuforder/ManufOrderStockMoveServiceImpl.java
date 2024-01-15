package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.config.StockConfigProductionService;
import com.axelor.apps.production.service.operationorder.OperationOrderStockMoveService;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.service.ReservedQtyService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManufOrderStockMoveServiceImpl implements ManufOrderStockMoveService {

  public static final int PART_FINISH_IN = 1;
  public static final int PART_FINISH_OUT = 2;
  public static final int STOCK_LOCATION_IN = 1;
  public static final int STOCK_LOCATION_OUT = 2;
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected StockMoveService stockMoveService;
  protected StockMoveLineService stockMoveLineService;
  protected AppBaseService appBaseService;
  protected SupplyChainConfigService supplyChainConfigService;
  protected ReservedQtyService reservedQtyService;
  protected ProductCompanyService productCompanyService;
  protected StockConfigProductionService stockConfigProductionService;
  protected PartnerService partnerService;
  protected ManufOrderOutsourceService manufOrderOutsourceService;
  protected StockMoveLineRepository stockMoveLineRepository;

  @Inject
  public ManufOrderStockMoveServiceImpl(
      StockMoveService stockMoveService,
      StockMoveLineService stockMoveLineService,
      AppBaseService appBaseService,
      SupplyChainConfigService supplyChainConfigService,
      ReservedQtyService reservedQtyService,
      ProductCompanyService productCompanyService,
      StockConfigProductionService stockConfigProductionService,
      PartnerService partnerService,
      ManufOrderOutsourceService manufOrderOutsourceService,
      StockMoveLineRepository stockMoveLineRepository) {
    this.supplyChainConfigService = supplyChainConfigService;
    this.reservedQtyService = reservedQtyService;
    this.stockMoveService = stockMoveService;
    this.stockMoveLineService = stockMoveLineService;
    this.appBaseService = appBaseService;
    this.productCompanyService = productCompanyService;
    this.stockConfigProductionService = stockConfigProductionService;
    this.partnerService = partnerService;
    this.manufOrderOutsourceService = manufOrderOutsourceService;
    this.stockMoveLineRepository = stockMoveLineRepository;
  }

  @Override
  public Optional<StockMove> createAndPlanToConsumeStockMoveWithLines(ManufOrder manufOrder)
      throws AxelorException {
    Company company = manufOrder.getCompany();

    // Get stock locations dest and source
    StockLocation fromStockLocation = getFromStockLocationForConsumedStockMove(manufOrder, company);
    StockLocation virtualStockLocation =
        getVirtualStockLocationForConsumedStockMove(manufOrder, company);

    if (manufOrder.getToConsumeProdProductList() != null && company != null) {

      // Create stock move
      StockMove stockMove =
          this._createToConsumeStockMove(
              manufOrder, company, fromStockLocation, virtualStockLocation);

      // Create stock move lines of Stock move
      createToConsumeStockMoveLines(
          manufOrder.getToConsumeProdProductList(),
          stockMove,
          fromStockLocation,
          virtualStockLocation);

      // Plan stock move
      planConsumedStockMove(stockMove);

      return Optional.of(stockMove);
    }
    return Optional.empty();
  }

  @Override
  public Optional<StockMove> createAndPlanToConsumeStockMove(ManufOrder manufOrder)
      throws AxelorException {
    Company company = manufOrder.getCompany();

    // Get stock locations dest and source
    StockLocation fromStockLocation = getFromStockLocationForConsumedStockMove(manufOrder, company);
    StockLocation virtualStockLocation =
        getVirtualStockLocationForConsumedStockMove(manufOrder, company);

    if (manufOrder.getToConsumeProdProductList() != null && company != null) {

      // Create stock move
      StockMove stockMove =
          this._createToConsumeStockMove(
              manufOrder, company, fromStockLocation, virtualStockLocation);

      // Plan stock move
      planConsumedStockMove(stockMove);
      return Optional.of(stockMove);
    }
    return Optional.empty();
  }

  protected void createToConsumeStockMoveLines(
      List<ProdProduct> prodProductList,
      StockMove stockMove,
      StockLocation fromStockLocation,
      StockLocation virtualStockLocation)
      throws AxelorException {
    for (ProdProduct prodProduct : prodProductList) {

      this._createStockMoveLine(
          prodProduct,
          stockMove,
          StockMoveLineService.TYPE_IN_PRODUCTIONS,
          fromStockLocation,
          virtualStockLocation);
    }
  }

  protected void planConsumedStockMove(StockMove stockMove) throws AxelorException {
    SupplyChainConfig supplyChainConfig =
        supplyChainConfigService.getSupplyChainConfig(stockMove.getCompany());
    if (stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()) {
      stockMoveService.plan(stockMove);
      if (supplyChainConfig.getAutoRequestReservedQtyOnManufOrder()) {
        requestStockReservation(stockMove);
      }
    }
  }

  /** Request reservation (and allocate if possible) all stock for this stock move. */
  protected void requestStockReservation(StockMove stockMove) throws AxelorException {
    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
      reservedQtyService.allocateAll(stockMoveLine);
    }
  }

  protected StockMove _createToConsumeStockMove(
      ManufOrder manufOrder,
      Company company,
      StockLocation fromStockLocation,
      StockLocation virtualStockLocation)
      throws AxelorException {

    StockMove stockMove;
    if (manufOrder.getOutsourcing()) {
      stockMove =
          _createToConsumeOutgoingOutsourceStockMove(
              manufOrder, company, fromStockLocation, virtualStockLocation);
    } else {
      stockMove =
          _createToConsumeProductionStockMove(
              manufOrder, company, fromStockLocation, virtualStockLocation);
    }

    stockMove.setManufOrder(manufOrder);
    stockMove.setOrigin(manufOrder.getManufOrderSeq());

    return stockMove;
  }

  protected StockMove _createToConsumeOutgoingOutsourceStockMove(
      ManufOrder manufOrder,
      Company company,
      StockLocation fromStockLocation,
      StockLocation virtualStockLocation)
      throws AxelorException {

    Address fromAddress = fromStockLocation.getAddress();
    Address toAddress =
        partnerService.getDefaultAddress(
            manufOrderOutsourceService.getOutsourcePartner(manufOrder).orElse(null));

    return stockMoveService.createStockMove(
        fromAddress,
        toAddress,
        company,
        fromStockLocation,
        virtualStockLocation,
        null,
        manufOrder.getPlannedStartDateT().toLocalDate(),
        null,
        StockMoveRepository.TYPE_OUTGOING);
  }

  protected StockMove _createToConsumeProductionStockMove(
      ManufOrder manufOrder,
      Company company,
      StockLocation fromStockLocation,
      StockLocation virtualStockLocation)
      throws AxelorException {

    return stockMoveService.createStockMove(
        null,
        null,
        company,
        fromStockLocation,
        virtualStockLocation,
        null,
        manufOrder.getPlannedStartDateT().toLocalDate(),
        null,
        StockMoveRepository.TYPE_INTERNAL);
  }

  @Override
  public StockLocation getDefaultInStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException {

    if (manufOrder.getOutsourcing()) {
      return _getDefaultInOutsourcingStockLocation(manufOrder, company);
    } else {
      return _getDefaultInStockLocation(manufOrder, company);
    }
  }

  protected StockLocation _getDefaultInStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException {
    StockConfig stockConfig = stockConfigProductionService.getStockConfig(company);
    StockLocation stockLocation =
        getDefaultStockLocation(manufOrder.getProdProcess(), STOCK_LOCATION_IN);
    if (stockLocation == null) {
      return stockConfigProductionService.getComponentDefaultStockLocation(
          manufOrder.getWorkshopStockLocation(), stockConfig);
    }
    return stockLocation;
  }

  protected StockLocation _getDefaultInOutsourcingStockLocation(
      ManufOrder manufOrder, Company company) throws AxelorException {
    StockConfig stockConfig = stockConfigProductionService.getStockConfig(company);
    // Because it will be send to outsource.
    return stockConfigProductionService.getPickupDefaultStockLocation(stockConfig);
  }

  @Override
  public StockLocation getDefaultOutStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException {
    StockConfig stockConfig = stockConfigProductionService.getStockConfig(company);
    StockLocation stockLocation =
        getDefaultStockLocation(manufOrder.getProdProcess(), STOCK_LOCATION_OUT);
    if (stockLocation == null) {
      return stockConfigProductionService.getFinishedProductsDefaultStockLocation(
          manufOrder.getWorkshopStockLocation(), stockConfig);
    }
    return stockLocation;
  }

  /**
   * Given a prodprocess and whether we want to create a in or out stock move, determine the stock
   * location and return it.
   *
   * @param prodProcess a production process.
   * @param inOrOut can be {@link ManufOrderStockMoveServiceImpl#STOCK_LOCATION_IN} or {@link
   *     ManufOrderStockMoveServiceImpl#STOCK_LOCATION_OUT}.
   * @return the found stock location, or null if the prod process is null.
   */
  protected StockLocation getDefaultStockLocation(ProdProcess prodProcess, int inOrOut) {
    if (inOrOut != STOCK_LOCATION_IN && inOrOut != STOCK_LOCATION_OUT) {
      throw new IllegalArgumentException(
          I18n.get(ProductionExceptionMessage.IN_OR_OUT_INVALID_ARG));
    }
    if (prodProcess == null) {
      return null;
    }
    if (inOrOut == STOCK_LOCATION_IN) {
      return prodProcess.getStockLocation();
    } else {
      return prodProcess.getProducedProductStockLocation();
    }
  }

  @Override
  public Optional<StockMove> createAndPlanToProduceStockMoveWithLines(ManufOrder manufOrder)
      throws AxelorException {

    Company company = manufOrder.getCompany();
    StockLocation virtualStockLocation =
        getVirtualStockLocationForProducedStockMove(manufOrder, company);
    StockLocation producedProductStockLocation =
        getProducedProductStockLocation(manufOrder, company);

    if (manufOrder.getToProduceProdProductList() != null && company != null) {

      StockMove stockMove =
          this._createToProduceStockMove(
              manufOrder, company, virtualStockLocation, producedProductStockLocation);

      createToProduceStockMoveLines(
          manufOrder, stockMove, virtualStockLocation, producedProductStockLocation);
      planProducedStockMove(stockMove);

      return Optional.of(stockMove);
    }
    return Optional.empty();
  }

  @Override
  public Optional<StockMove> createAndPlanToProduceStockMove(ManufOrder manufOrder)
      throws AxelorException {

    Company company = manufOrder.getCompany();
    StockLocation virtualStockLocation =
        getVirtualStockLocationForProducedStockMove(manufOrder, company);
    StockLocation producedProductStockLocation =
        getProducedProductStockLocation(manufOrder, company);

    if (manufOrder.getToProduceProdProductList() != null && company != null) {

      StockMove stockMove =
          this._createToProduceStockMove(
              manufOrder, company, virtualStockLocation, producedProductStockLocation);

      createToProduceStockMoveLines(
          manufOrder, stockMove, virtualStockLocation, producedProductStockLocation);
      planProducedStockMove(stockMove);

      return Optional.of(stockMove);
    }
    return Optional.empty();
  }

  protected void createToProduceStockMoveLines(
      ManufOrder manufOrder,
      StockMove stockMove,
      StockLocation virtualStockLocation,
      StockLocation producedProductStockLocation)
      throws AxelorException {
    for (ProdProduct prodProduct : manufOrder.getToProduceProdProductList()) {

      BigDecimal productCostPrice =
          prodProduct.getProduct() != null
              ? (BigDecimal)
                  productCompanyService.get(
                      prodProduct.getProduct(), "costPrice", manufOrder.getCompany())
              : BigDecimal.ZERO;
      this._createStockMoveLine(
          prodProduct,
          stockMove,
          StockMoveLineService.TYPE_OUT_PRODUCTIONS,
          prodProduct.getQty(),
          productCostPrice,
          virtualStockLocation,
          producedProductStockLocation);
    }
  }

  protected void planProducedStockMove(StockMove stockMove) throws AxelorException {
    if (stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()) {
      stockMoveService.plan(stockMove);
    }
  }

  /**
   * Consume in stock moves in manuf order.
   *
   * @param manufOrder
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public void consumeInStockMoves(ManufOrder manufOrder) throws AxelorException {
    for (StockMove stockMove : manufOrder.getInStockMoveList()) {

      finishStockMove(stockMove);
    }
  }

  protected StockMove _createToProduceStockMove(
      ManufOrder manufOrder,
      Company company,
      StockLocation virtualStockLocation,
      StockLocation producedProductStockLocation)
      throws AxelorException {

    StockMove stockMove;
    if (manufOrder.getOutsourcing()) {
      stockMove =
          _createToProduceIncomingOutsourceStockMove(
              manufOrder, company, virtualStockLocation, producedProductStockLocation);
    } else {
      stockMove =
          _createToProduceProductionStockMove(
              manufOrder, company, virtualStockLocation, producedProductStockLocation);
    }

    stockMove.setManufOrder(manufOrder);
    stockMove.setOrigin(manufOrder.getManufOrderSeq());
    return stockMove;
  }

  protected StockMove _createToProduceIncomingOutsourceStockMove(
      ManufOrder manufOrder,
      Company company,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {
    LocalDateTime plannedEndDateT = manufOrder.getPlannedEndDateT();
    LocalDate plannedEndDate = plannedEndDateT != null ? plannedEndDateT.toLocalDate() : null;

    Address toAddress = toStockLocation.getAddress();
    Address fromAddress =
        partnerService.getDefaultAddress(
            manufOrderOutsourceService.getOutsourcePartner(manufOrder).orElse(null));
    return stockMoveService.createStockMove(
        fromAddress,
        toAddress,
        company,
        fromStockLocation,
        toStockLocation,
        null,
        plannedEndDate,
        null,
        StockMoveRepository.TYPE_INCOMING);
  }

  protected StockMove _createToProduceProductionStockMove(
      ManufOrder manufOrder,
      Company company,
      StockLocation virtualStockLocation,
      StockLocation producedProductStockLocation)
      throws AxelorException {
    LocalDateTime plannedEndDateT = manufOrder.getPlannedEndDateT();
    LocalDate plannedEndDate = plannedEndDateT != null ? plannedEndDateT.toLocalDate() : null;
    return stockMoveService.createStockMove(
        null,
        null,
        company,
        virtualStockLocation,
        producedProductStockLocation,
        null,
        plannedEndDate,
        null,
        StockMoveRepository.TYPE_INTERNAL);
  }

  protected StockMoveLine _createStockMoveLine(
      ProdProduct prodProduct,
      StockMove stockMove,
      int inOrOutType,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {

    return _createStockMoveLine(
        prodProduct,
        stockMove,
        inOrOutType,
        prodProduct.getQty(),
        fromStockLocation,
        toStockLocation);
  }

  public StockMoveLine _createStockMoveLine(
      ProdProduct prodProduct,
      StockMove stockMove,
      int inOrOutType,
      BigDecimal qty,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {
    BigDecimal productCostPrice =
        prodProduct.getProduct() != null
            ? (BigDecimal)
                productCompanyService.get(
                    prodProduct.getProduct(), "costPrice", stockMove.getCompany())
            : BigDecimal.ZERO;
    return _createStockMoveLine(
        prodProduct,
        stockMove,
        inOrOutType,
        qty,
        productCostPrice,
        fromStockLocation,
        toStockLocation);
  }

  protected StockMoveLine _createStockMoveLine(
      ProdProduct prodProduct,
      StockMove stockMove,
      int inOrOutType,
      BigDecimal qty,
      BigDecimal costPrice,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {

    return stockMoveLineService.createStockMoveLine(
        prodProduct.getProduct(),
        (String)
            productCompanyService.get(prodProduct.getProduct(), "name", stockMove.getCompany()),
        (String)
            productCompanyService.get(
                prodProduct.getProduct(), "description", stockMove.getCompany()),
        qty,
        costPrice,
        costPrice,
        prodProduct.getUnit(),
        stockMove,
        inOrOutType,
        false,
        BigDecimal.ZERO,
        fromStockLocation,
        toStockLocation);
  }

  public void finish(ManufOrder manufOrder) throws AxelorException {
    // clear empty stock move
    manufOrder
        .getInStockMoveList()
        .removeIf(stockMove -> CollectionUtils.isEmpty(stockMove.getStockMoveLineList()));
    manufOrder
        .getOutStockMoveList()
        .removeIf(stockMove -> CollectionUtils.isEmpty(stockMove.getStockMoveLineList()));

    // finish remaining stock move
    for (StockMove stockMove : manufOrder.getInStockMoveList()) {
      this.finishStockMove(stockMove);
    }
    for (StockMove stockMove : manufOrder.getOutStockMoveList()) {
      updateRealPrice(manufOrder, stockMove);
      this.finishStockMove(stockMove);
    }
  }

  /**
   * Update price in stock move line: if the product price is configured to be real, then we use the
   * cost price from costsheet. Else, we do nothing as the planned price is already filled.
   *
   * @param manufOrder
   * @param stockMove
   */
  protected void updateRealPrice(ManufOrder manufOrder, StockMove stockMove) {
    stockMove.getStockMoveLineList().stream()
        .filter(
            stockMoveLine ->
                stockMoveLine.getProduct() != null
                    && stockMoveLine.getProduct().getRealOrEstimatedPriceSelect()
                        == ProductRepository.PRICE_METHOD_REAL)
        .forEach(stockMoveLine -> stockMoveLine.setUnitPriceUntaxed(manufOrder.getCostPrice()));
  }

  public void finishStockMove(StockMove stockMove) throws AxelorException {

    if (stockMove != null && stockMove.getStatusSelect() == StockMoveRepository.STATUS_PLANNED) {
      stockMove.setIsWithBackorder(false);
      stockMoveService.copyQtyToRealQty(stockMove);
      stockMoveService.realize(stockMove);
    }
  }

  /**
   * Call the method to realize in stock move, then the method to realize out stock move for the
   * given manufacturing order.
   *
   * @param manufOrder
   */
  @Transactional(rollbackOn = {Exception.class})
  public void partialFinish(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder.getIsConsProOnOperation()) {
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
        if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_IN_PROGRESS) {
          Beans.get(OperationOrderStockMoveService.class).partialFinish(operationOrder);
        }
      }
    } else {
      partialFinish(manufOrder, PART_FINISH_IN);
    }
    partialFinish(manufOrder, PART_FINISH_OUT);
    Beans.get(ManufOrderRepository.class).save(manufOrder);
  }

  /**
   * Allows to create and realize in or out stock moves for the given manufacturing order.
   *
   * @param manufOrder
   * @param inOrOut can be {@link ManufOrderStockMoveServiceImpl#PART_FINISH_IN} or {@link
   *     ManufOrderStockMoveServiceImpl#PART_FINISH_OUT}
   * @throws AxelorException
   */
  protected void partialFinish(ManufOrder manufOrder, int inOrOut) throws AxelorException {

    if (inOrOut != PART_FINISH_IN && inOrOut != PART_FINISH_OUT) {
      throw new IllegalArgumentException(
          I18n.get(ProductionExceptionMessage.IN_OR_OUT_INVALID_ARG));
    }

    Company company = manufOrder.getCompany();
    StockConfig stockConfig = stockConfigProductionService.getStockConfig(company);

    StockLocation fromStockLocation;
    StockLocation toStockLocation;
    List<StockMove> stockMoveList;

    if (inOrOut == PART_FINISH_IN) {
      stockMoveList = manufOrder.getInStockMoveList();
      fromStockLocation = getDefaultInStockLocation(manufOrder, company);
      toStockLocation =
          stockConfigProductionService.getProductionVirtualStockLocation(
              stockConfig, manufOrderOutsourceService.isOutsource(manufOrder));

    } else {
      stockMoveList = manufOrder.getOutStockMoveList();
      fromStockLocation =
          stockConfigProductionService.getProductionVirtualStockLocation(
              stockConfig, manufOrderOutsourceService.isOutsource(manufOrder));
      toStockLocation = getDefaultOutStockLocation(manufOrder, company);
    }

    // realize current stock move and update the price
    Optional<StockMove> stockMoveToRealize = getPlannedStockMove(stockMoveList);
    if (stockMoveToRealize.isPresent()) {
      updateRealPrice(manufOrder, stockMoveToRealize.get());
      finishStockMove(stockMoveToRealize.get());
    }

    // generate new stock move

    StockMove newStockMove =
        stockMoveService.createStockMove(
            null,
            null,
            company,
            fromStockLocation,
            toStockLocation,
            null,
            manufOrder.getPlannedStartDateT().toLocalDate(),
            null,
            StockMoveRepository.TYPE_INTERNAL);

    newStockMove.setStockMoveLineList(new ArrayList<>());
    newStockMove.setOrigin(manufOrder.getManufOrderSeq());
    newStockMove.setManufOrder(manufOrder);
    createNewStockMoveLines(manufOrder, newStockMove, inOrOut, fromStockLocation, toStockLocation);

    if (!newStockMove.getStockMoveLineList().isEmpty()) {
      // plan the stockmove
      stockMoveService.plan(newStockMove);

      if (inOrOut == PART_FINISH_IN) {
        manufOrder.addInStockMoveListItem(newStockMove);
        newStockMove.getStockMoveLineList().forEach(manufOrder::addConsumedStockMoveLineListItem);
        manufOrder.clearDiffConsumeProdProductList();
      } else {
        manufOrder.addOutStockMoveListItem(newStockMove);
        newStockMove.getStockMoveLineList().forEach(manufOrder::addProducedStockMoveLineListItem);
      }
    }
  }

  /**
   * Get the planned stock move in a stock move list
   *
   * @param stockMoveList
   * @return an optional stock move
   */
  public Optional<StockMove> getPlannedStockMove(List<StockMove> stockMoveList) {
    return stockMoveList.stream()
        .filter(stockMove -> stockMove.getStatusSelect() == StockMoveRepository.STATUS_PLANNED)
        .findFirst();
  }

  /**
   * Generate stock move lines after a partial finish
   *
   * @param manufOrder
   * @param stockMove
   * @param inOrOut can be {@link ManufOrderStockMoveServiceImpl#PART_FINISH_IN} or {@link
   *     ManufOrderStockMoveServiceImpl#PART_FINISH_OUT}
   */
  public void createNewStockMoveLines(
      ManufOrder manufOrder,
      StockMove stockMove,
      int inOrOut,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {
    int stockMoveLineType;
    List<ProdProduct> diffProdProductList;
    if (inOrOut == PART_FINISH_IN) {
      stockMoveLineType = StockMoveLineService.TYPE_IN_PRODUCTIONS;

      diffProdProductList = new ArrayList<>(manufOrder.getDiffConsumeProdProductList());
    } else {
      stockMoveLineType = StockMoveLineService.TYPE_OUT_PRODUCTIONS;

      // must compute remaining quantities in produced product
      List<ProdProduct> outProdProductList = manufOrder.getToProduceProdProductList();
      List<StockMoveLine> stockMoveLineList = manufOrder.getProducedStockMoveLineList();

      if (outProdProductList == null || stockMoveLineList == null) {
        return;
      }
      diffProdProductList =
          Beans.get(ManufOrderService.class)
              .createDiffProdProductList(manufOrder, outProdProductList, stockMoveLineList);
    }
    createNewStockMoveLines(
        diffProdProductList, stockMove, stockMoveLineType, fromStockLocation, toStockLocation);
  }

  /**
   * Generate stock move lines after a partial finish
   *
   * @param diffProdProductList
   * @param stockMove
   * @param stockMoveLineType
   * @throws AxelorException
   */
  public void createNewStockMoveLines(
      List<ProdProduct> diffProdProductList,
      StockMove stockMove,
      int stockMoveLineType,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {
    diffProdProductList.forEach(prodProduct -> prodProduct.setQty(prodProduct.getQty().negate()));
    for (ProdProduct prodProduct : diffProdProductList) {
      if (prodProduct.getQty().signum() >= 0) {
        _createStockMoveLine(
            prodProduct, stockMove, stockMoveLineType, fromStockLocation, toStockLocation);
      }
    }
  }

  public void cancel(ManufOrder manufOrder) throws AxelorException {

    for (StockMove stockMove : manufOrder.getInStockMoveList()) {
      this.cancel(stockMove);
    }
    for (StockMove stockMove : manufOrder.getOutStockMoveList()) {
      this.cancel(stockMove);
    }
  }

  public void cancel(StockMove stockMove) throws AxelorException {

    if (stockMove != null) {

      stockMoveService.cancel(stockMove);

      for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {

        stockMoveLine.setProducedManufOrder(null);
      }
    }
  }

  /**
   * Clear the consumed list and create a new one with the right quantity.
   *
   * @param manufOrder
   * @param qtyToUpdate
   */
  public void createNewConsumedStockMoveLineList(ManufOrder manufOrder, BigDecimal qtyToUpdate)
      throws AxelorException {
    // find planned stock move
    Optional<StockMove> stockMoveOpt = getPlannedStockMove(manufOrder.getInStockMoveList());
    if (!stockMoveOpt.isPresent()) {
      return;
    }

    StockMove stockMove = stockMoveOpt.get();

    stockMoveService.cancel(stockMove);

    // clear all lists from planned lines
    manufOrder
        .getConsumedStockMoveLineList()
        .removeIf(
            stockMoveLine ->
                stockMoveLine.getStockMove().getStatusSelect()
                    == StockMoveRepository.STATUS_CANCELED);
    stockMove.clearStockMoveLineList();

    // create a new list
    for (ProdProduct prodProduct : manufOrder.getToConsumeProdProductList()) {
      BigDecimal qty = getFractionQty(manufOrder, prodProduct, qtyToUpdate);
      _createStockMoveLine(
          prodProduct,
          stockMove,
          StockMoveLineService.TYPE_IN_PRODUCTIONS,
          qty,
          stockMove.getFromStockLocation(),
          stockMove.getToStockLocation());

      // Update consumed StockMoveLineList with created stock move lines
      stockMove.getStockMoveLineList().stream()
          .filter(
              stockMoveLine1 -> !manufOrder.getConsumedStockMoveLineList().contains(stockMoveLine1))
          .forEach(manufOrder::addConsumedStockMoveLineListItem);
    }
    stockMoveService.goBackToDraft(stockMove);
    stockMoveService.plan(stockMove);
  }

  /**
   * Clear the produced list and create a new one with the right quantity.
   *
   * @param manufOrder
   * @param qtyToUpdate
   */
  public void createNewProducedStockMoveLineList(ManufOrder manufOrder, BigDecimal qtyToUpdate)
      throws AxelorException {
    Optional<StockMove> stockMoveOpt = getPlannedStockMove(manufOrder.getOutStockMoveList());
    if (!stockMoveOpt.isPresent()) {
      return;
    }
    StockMove stockMove = stockMoveOpt.get();

    stockMoveService.cancel(stockMove);

    // clear all lists
    manufOrder
        .getProducedStockMoveLineList()
        .removeIf(
            stockMoveLine ->
                stockMoveLine.getStockMove().getStatusSelect()
                    == StockMoveRepository.STATUS_CANCELED);
    stockMove.clearStockMoveLineList();

    // create a new list
    for (ProdProduct prodProduct : manufOrder.getToProduceProdProductList()) {
      BigDecimal qty = getFractionQty(manufOrder, prodProduct, qtyToUpdate);
      BigDecimal productCostPrice =
          prodProduct.getProduct() != null
              ? (BigDecimal)
                  productCompanyService.get(
                      prodProduct.getProduct(), "costPrice", manufOrder.getCompany())
              : BigDecimal.ZERO;
      _createStockMoveLine(
          prodProduct,
          stockMove,
          StockMoveLineService.TYPE_OUT_PRODUCTIONS,
          qty,
          productCostPrice,
          stockMove.getFromStockLocation(),
          stockMove.getToStockLocation());

      // Update produced StockMoveLineList with created stock move lines
      stockMove.getStockMoveLineList().stream()
          .filter(
              stockMoveLine1 -> !manufOrder.getProducedStockMoveLineList().contains(stockMoveLine1))
          .forEach(manufOrder::addProducedStockMoveLineListItem);
    }
    stockMoveService.goBackToDraft(stockMove);
    stockMoveService.plan(stockMove);
  }

  /**
   * Compute the right qty when modifying real quantity in a manuf order
   *
   * @param manufOrder
   * @param prodProduct
   * @param qtyToUpdate
   * @return
   */
  public BigDecimal getFractionQty(
      ManufOrder manufOrder, ProdProduct prodProduct, BigDecimal qtyToUpdate) {
    BigDecimal manufOrderQty = manufOrder.getQty();
    BigDecimal prodProductQty = prodProduct.getQty();

    int scale = appBaseService.getNbDecimalDigitForQty();

    return qtyToUpdate
        .multiply(prodProductQty)
        .setScale(scale, RoundingMode.HALF_UP)
        .divide(manufOrderQty, scale, RoundingMode.HALF_UP);
  }

  public StockLocation getFromStockLocationForConsumedStockMove(
      ManufOrder manufOrder, Company company) throws AxelorException {
    StockLocation fromStockLocation = getDefaultInStockLocation(manufOrder, company);

    if (fromStockLocation == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(
              ProductionExceptionMessage.MANUF_ORDER_STOCK_MOVE_MISSING_SOURCE_STOCK_LOCATION));
    }
    return fromStockLocation;
  }

  public StockLocation getVirtualStockLocationForConsumedStockMove(
      ManufOrder manufOrder, Company company) throws AxelorException {

    if (manufOrder.getOutsourcing()) {
      return _getVirtualOutsourcingStockLocation(manufOrder, company);
    } else {
      return _getVirtualProductionStockLocation(manufOrder, company);
    }
  }

  protected StockLocation _getVirtualProductionStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException {
    StockConfig stockConfig = stockConfigProductionService.getStockConfig(company);

    return stockConfigProductionService.getProductionVirtualStockLocation(
        stockConfig, manufOrderOutsourceService.isOutsource(manufOrder));
  }

  protected StockLocation _getVirtualOutsourcingStockLocation(
      ManufOrder manufOrder, Company company) throws AxelorException {
    StockConfig stockConfig = stockConfigProductionService.getStockConfig(company);

    return stockConfigProductionService.getVirtualOutsourcingStockLocation(stockConfig);
  }

  public StockLocation getProducedProductStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException {

    if (manufOrder.getOutsourcing()) {
      return _getReceiptOutsourcingStockLocation(manufOrder, company);
    } else {
      return _getProducedProductStockLocation(manufOrder, company);
    }
  }

  protected StockLocation _getReceiptOutsourcingStockLocation(
      ManufOrder manufOrder, Company company) throws AxelorException {
    StockConfig stockConfig = stockConfigProductionService.getStockConfig(company);
    return stockConfigProductionService.getReceiptDefaultStockLocation(stockConfig);
  }

  protected StockLocation _getProducedProductStockLocation(ManufOrder manufOrder, Company company)
      throws AxelorException {
    StockConfig stockConfig = stockConfigProductionService.getStockConfig(company);

    StockLocation producedProductStockLocation =
        manufOrder.getProdProcess().getProducedProductStockLocation();
    if (producedProductStockLocation == null) {
      producedProductStockLocation =
          stockConfigProductionService.getFinishedProductsDefaultStockLocation(
              manufOrder.getWorkshopStockLocation(), stockConfig);
    }
    return producedProductStockLocation;
  }

  public StockLocation getVirtualStockLocationForProducedStockMove(
      ManufOrder manufOrder, Company company) throws AxelorException {

    if (manufOrder.getOutsourcing()) {
      return _getVirtualOutsourcingStockLocation(manufOrder, company);
    } else {
      return _getVirtualProductionStockLocation(manufOrder, company);
    }
  }

  @Override
  public List<Long> getOutgoingStockMoves(ManufOrder manufOrder) {
    if (CollectionUtils.isEmpty(manufOrder.getSaleOrderSet())) {
      return Lists.newArrayList(0l);
    }

    return stockMoveLineRepository
        .all()
        .filter("self.saleOrderLine = :saleOrderLine AND self.stockMove != null")
        .bind("saleOrderLine", manufOrder.getSaleOrderLine())
        .fetchStream()
        .map(l -> l.getStockMove().getId())
        .collect(Collectors.toList());
  }
}
