package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.db.*;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.supplychain.db.ProductReservation;
import com.axelor.apps.supplychain.db.repo.AbstractProductReservationRepository;
import com.axelor.apps.supplychain.db.repo.ProductReservationRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import javax.persistence.Query;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductReservationServiceImpl implements ProductReservationService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final ProductReservationRepository productReservationRepository;
  private final ProductRepository productRepository;

  @Inject
  public ProductReservationServiceImpl(
      ProductReservationRepository productReservationRepository,
      ProductRepository productRepository) {
    this.productReservationRepository = productReservationRepository;
    this.productRepository = productRepository;
  }

  // UPDATE STATUS

  @Override
  @Transactional
  public void updateStatus(ProductReservation productReservation, boolean isHaveToSave)
      throws AxelorException {
    if (Boolean.TRUE.equals(productReservation.getIsReservation())) {
      updateStatusReservation(productReservation);
    } else if (Boolean.TRUE.equals(productReservation.getIsAllocation())) {
      updateStatusAllocation(productReservation);
    }
    if (isHaveToSave) {
      productReservationRepository.save(productReservation);
    }
  }

  protected void updateStatusReservation(ProductReservation productReservation) {
    productReservation.setStatus(
        AbstractProductReservationRepository.PRODUCT_RESERVATION_STATUS_IN_PROGRESS);
  }

  protected void updateStatusAllocation(ProductReservation productReservation)
      throws AxelorException {
    if (productReservation.getProduct().getTrackingNumberConfiguration() == null) {
      updateStatusAllocationByProduct(productReservation);
    } else {
      updateStatusAllocationByTrackingNumber(productReservation);
    }
  }

  protected void updateStatusAllocationByTrackingNumber(ProductReservation productReservation)
      throws AxelorException {
    updateStatusAllocation(
        computeAvailableQuantityForTrackingNumber(productReservation),
        productReservation,
        SupplychainExceptionMessage.ALLOCATION_QTY_BY_TRACKING_NUMBER_IS_NOT_AVAILABLE);
  }

  protected void updateStatusAllocationByProduct(ProductReservation productReservation)
      throws AxelorException {
    updateStatusAllocation(
        computeAvailableQuantityForProduct(productReservation),
        productReservation,
        SupplychainExceptionMessage.ALLOCATION_QTY_BY_PRODUCT_IS_NOT_AVAILABLE);
  }

  protected void updateStatusAllocation(
      BigDecimal availableQty,
      ProductReservation productReservation,
      String exceptionMessageOnNotAvailableQty)
      throws AxelorException {

    if (productReservation.getQty().compareTo(availableQty) <= 0) {
      productReservation.setStatus(
          AbstractProductReservationRepository.PRODUCT_RESERVATION_STATUS_IN_PROGRESS);
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(exceptionMessageOnNotAvailableQty),
          productReservation);
    }
  }

  // AVAILABLE QTY

  protected BigDecimal computeAvailableQuantityForTrackingNumber(
      ProductReservation productReservation) throws AxelorException {
    BigDecimal alreadyAllocatedQty =
        productReservationRepository
            .findByProductReservationTypeAndStatusAndStockLocationAndTrackingNumber(
                productReservation.getProductReservationType(),
                AbstractProductReservationRepository.PRODUCT_RESERVATION_STATUS_IN_PROGRESS,
                productReservation.getStockLocation(),
                productReservation.getTrackingNumber())
            .fetchStream()
            .map(ProductReservation::getQty)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    boolean canGetRealQty =
        productReservation.getProduct() != null
            && productReservation.getProduct().getId() != null
            && productReservation.getStockLocation() != null
            && productReservation.getStockLocation().getId() != null
            && productReservation.getStockLocation().getCompany() != null
            && productReservation.getStockLocation().getCompany().getId() != null;
    BigDecimal realQty = BigDecimal.ZERO;
    if (canGetRealQty) {
      realQty =
          Beans.get(StockLocationService.class)
              .getRealQty(
                  productReservation.getProduct().getId(),
                  productReservation.getStockLocation().getId(),
                  productReservation.getStockLocation().getCompany().getId());
    }
    return realQty.subtract(alreadyAllocatedQty).setScale(2, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeAvailableQuantityForProduct(ProductReservation productReservation) {
    BigDecimal realQty;
    BigDecimal reservedQty;
    reservedQty =
        productReservationRepository.getReservedQty(
            productReservation.getProduct(), productReservation.getStockLocation());
    realQty = getRealQty(productReservation.getProduct(), productReservation.getStockLocation());
    return realQty.subtract(reservedQty).setScale(2, RoundingMode.HALF_UP);
  }

  // TODO move to StockLocationLineRepository
  protected BigDecimal getRealQty(Product product, StockLocation stockLocation) {
    String query = "self.product = :product";
    if (stockLocation != null) {
      query += " AND self.stockLocation = :stockLocation";
    }
    return Beans.get(StockLocationLineRepository.class)
        .all()
        .filter(
            query + " AND self.stockLocation.typeSelect != " + StockLocationRepository.TYPE_VIRTUAL)
        .bind("stockLocation", stockLocation)
        .bind("product", product)
        .fetchStream()
        .map(StockLocationLine::getCurrentQty)
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);
  }

  // TODO supprimer ?
  @Override
  public Optional<ProductReservation> getReservedQty(StockMoveLine stockMoveLine) {
    Product product = stockMoveLine.getProduct();
    StockLocation fromStockLocation = stockMoveLine.getFromStockLocation();
    ProductReservation productReservation =
        productReservationRepository
            .findByProductReservationTypeAndStockLocationAndProduct(
                AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_ALLOCATION,
                fromStockLocation,
                product)
            .fetchOne();
    return Optional.ofNullable(productReservation);
  }

  /**
   * TODO : supprimer ? Création d'une réservation
   *
   * @param stockMoveLine
   * @param qty
   */
  @Override
  public void setRequestedReservedQty(StockMoveLine stockMoveLine, BigDecimal qty) {
    if (StockLocationRepository.TYPE_VIRTUAL
        == stockMoveLine.getFromStockLocation().getTypeSelect()) {
      logger.info("CMR ::: stockMoveLine.getFromStockLocation() est de type Virtuel");
    }
    getOrCreateRequestedReservedProductReservation(stockMoveLine, qty);
  }

  private void getOrCreateRequestedReservedProductReservation(
      StockMoveLine stockMoveLine, BigDecimal qty) {
    getOrCreateProductReservation(
        stockMoveLine, qty, ProductReservationRepository.TYPE_PRODUCT_RESERVATION_RESERVATION);
  }

  /**
   * TODO : a supprimer si on supprime setRequestedReservedQty avoir tous les productreservation
   * correspondant
   *
   * @param stockMoveLine
   * @param qty
   * @param typeProductReservationReservation
   */
  private void getOrCreateProductReservation(
      StockMoveLine stockMoveLine, BigDecimal qty, int typeProductReservationReservation) {
    Product product = stockMoveLine.getProduct();
    StockLocation fromStockLocation = stockMoveLine.getFromStockLocation();
    SaleOrderLine saleOrderLine = stockMoveLine.getSaleOrderLine();
    ProductReservation productReservation = new ProductReservation();
    productReservation.setProduct(product);
    // productReservation.setRequestedReservedType(typeProductReservationReservation);
  }

  /** TODO à supprimer */
  @Override
  public Optional<ProductReservation> getRequestedReservedQty(StockMoveLine stockMoveLine) {
    Product product = stockMoveLine.getProduct();
    StockLocation fromStockLocation = stockMoveLine.getFromStockLocation();
    ProductReservation productReservation =
        productReservationRepository
            .findByProductReservationTypeAndStockLocationAndProduct(
                ProductReservationRepository.TYPE_PRODUCT_RESERVATION_RESERVATION,
                fromStockLocation,
                product)
            .fetchOne();
    return Optional.ofNullable(productReservation);
  }

  // NEW
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void requestReservedQty(Long saleOrderLineId) throws AxelorException {

    SaleOrderLine saleOrderLine = findSaleOrderLineOrWarn(saleOrderLineId);
    if (saleOrderLine == null) {
      return;
    }

    checkSaleOrderLineQtyOrException(saleOrderLine);

    Product product = saleOrderLine.getProduct();
    checkProductStockManagedOrException(product);

    StockMoveLine stockMoveLine =
        Beans.get(StockMoveLineServiceSupplychain.class).getPlannedStockMoveLine(saleOrderLine);
    if (stockMoveLine != null) {
      stockMoveLine.setIsQtyRequested(true);
    }
    Beans.get(ReservedQtyService.class).requestQty(saleOrderLine);
    this.updateRequestedReservedQty(saleOrderLine, stockMoveLine);
  }

  /** @throws AxelorException if saleOrderLine.qty < 0 */
  private static void checkSaleOrderLineQtyOrException(SaleOrderLine saleOrderLine)
      throws AxelorException {
    if (saleOrderLine.getQty().signum() < 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.SALE_ORDER_LINE_REQUEST_QTY_NEGATIVE));
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void updateRequestedReservedQty(SaleOrderLine saleOrderLine, StockMoveLine stockMoveLine) {

    BigDecimal newReservedQty = saleOrderLine.getQty();
    StockLocation fromStockLocation = stockMoveLine.getStockMove().getFromStockLocation();

    findBySaleOrderLine(
        saleOrderLine,
        fromStockLocation,
        AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_RESERVATION);
  }

  private void findBySaleOrderLine(
      SaleOrderLine saleOrderLine, StockLocation fromStockLocation, int productReservationType) {
    List<ProductReservation> list =
        productReservationRepository
            .findByProductReservationTypeAndStockLocationAndProduct(
                productReservationType, fromStockLocation, saleOrderLine.getProduct())
            .fetchStream()
            .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(list)) {
      saveNewProductReservationFromSaleOrderLine(
          saleOrderLine, fromStockLocation, productReservationType);
    }
  }

  private void saveNewProductReservationFromSaleOrderLine(
      SaleOrderLine saleOrderLine, StockLocation fromStockLocation, int productReservationType) {
    ProductReservation productReservation = new ProductReservation();
    productReservation.setProductReservationType(productReservationType);
    productReservation.setQty(saleOrderLine.getQty());
    productReservation.setProduct(saleOrderLine.getProduct());
  }

  protected SaleOrderLine findSaleOrderLineOrWarn(Long saleOrderLineId) {
    // chack saleORderLineOrWarn
    if (saleOrderLineId == null) {
      logger.warn("Unable to requestReservedQty because of saleOrderLine id is null");
      return null;
    }
    // TODO CMR Passer par un optional renvoyé par un service type 'SaleOrderLineSupplyChainService'
    SaleOrderLine saleOrderLine = Beans.get(SaleOrderLineRepository.class).find(saleOrderLineId);
    if (saleOrderLine == null) { // traiter avec l'optional
      logger.warn("Unable to requestReservedQty because of saleOrderLine is null");
      return null;
    }
    return saleOrderLine;
  }

  /** @throws AxelorException if product is not stock manageable */
  protected void checkProductStockManagedOrException(Product product) throws AxelorException {
    if (product == null || !product.getStockManaged()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.SALE_ORDER_LINE_PRODUCT_NOT_STOCK_MANAGED));
    }
  }

  @Override
  public List<ProductReservation> findProductReservationRequestedReservedOfSaleOrderLine(
      SaleOrderLine saleOrderLineProxy) {
    return productReservationRepository
        .findByOriginSaleOrderLineAndProductReservationType(
            AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_RESERVATION,
            saleOrderLineProxy)
        .fetch();
  }

  @Override
  public List<ProductReservation> findProductReservationReservedOfSaleOrderLine(
      SaleOrderLine saleOrderLineProxy) {
    return productReservationRepository
        .findByOriginSaleOrderLineAndProductReservationType(
            AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_ALLOCATION,
            saleOrderLineProxy)
        .fetch();
  }

  @Override
  public BigDecimal getAvailableQty(ProductReservation productReservation) throws AxelorException {
    return computeAvailableQuantityForProduct(productReservation);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void saveSelectedProductReservationInMapList(
      List<Map<String, Object>> rawProductReservationMapList, int productReservationType) {

    rawProductReservationMapList.stream()
        .filter(this::isSelected)
        .findFirst()
        .ifPresent(
            productReservationMapSelected ->
                saveProductReservationMap(productReservationMapSelected, productReservationType));
  }

  protected boolean isSelected(Map<String, Object> producReservationMap) {
    return Boolean.TRUE.equals(Boolean.valueOf(producReservationMap.get("selected").toString()));
  }

  protected void saveProductReservationMap(
      Map<String, Object> productReseravationMap, int productReservationType) {

    ProductReservation productReservationToSave = new ProductReservation();

    // id reload productReservation
    if (productReseravationMap.get("id") != null) {
      Long id = Long.valueOf(productReseravationMap.get("id").toString());
      productReservationToSave = productReservationRepository.find(id);
    }

    // productReservationType
    productReservationToSave.setProductReservationType(productReservationType);

    // status
    Integer status = Integer.valueOf(productReseravationMap.get("status").toString());
    productReservationToSave.setStatus(status);

    // Qty
    BigDecimal qty = new BigDecimal(productReseravationMap.get("qty").toString());
    productReservationToSave.setQty(qty);

    // reload originSaleOrderLine
    @SuppressWarnings("unchecked")
    Map<String, Object> originSaleOrderLineMap =
        (Map<String, Object>) productReseravationMap.get("originSaleOrderLine");
    if (originSaleOrderLineMap != null) {
      Long saleOrderLineId = Long.valueOf(originSaleOrderLineMap.get("id").toString());
      SaleOrderLine saleOrderLine = JPA.find(SaleOrderLine.class, saleOrderLineId);
      productReservationToSave.setOriginSaleOrderLine(saleOrderLine);
    }

    // reload manufOrder
    @SuppressWarnings("unchecked")
    Map<String, Object> originManufOrderMap =
        (Map<String, Object>) productReseravationMap.get("originManufOrder");
    if (originManufOrderMap != null) {
      Long manufOrderId = Long.valueOf(originManufOrderMap.get("id").toString());
      Model originInstanceModel = getInstanceModel("com.axelor.apps.production.db.ManufOrder");
      assert originInstanceModel != null;
      originInstanceModel.setId(manufOrderId);
      originInstanceModel = JPA.find(originInstanceModel.getClass(), originInstanceModel.getId());
      setOrigin(productReservationToSave, originInstanceModel);
    }

    // reload Product
    @SuppressWarnings("unchecked")
    Map<String, Object> productMap = (Map<String, Object>) productReseravationMap.get("product");
    Long productId = Long.valueOf(productMap.get("id").toString());
    Product product = productRepository.find(productId);
    productReservationToSave.setProduct(product);

    // reload StockLocation
    @SuppressWarnings("unchecked")
    Map<String, Object> stockLocationMap =
        (Map<String, Object>) productReseravationMap.get("stockLocation");
    if (stockLocationMap != null) {
      Long stockLocationId = Long.valueOf(stockLocationMap.get("id").toString());
      StockLocation stockLocation = Beans.get(StockLocationRepository.class).find(stockLocationId);
      productReservationToSave.setStockLocation(stockLocation);
    }

    // reload TracingNumber
    @SuppressWarnings("unchecked")
    Map<String, Object> trackingNumberMap =
        (Map<String, Object>) productReseravationMap.get("trackingNumber");
    if (trackingNumberMap != null) {
      Long trackingNumberId = Long.valueOf(trackingNumberMap.get("id").toString());
      TrackingNumber trackingNumber = JPA.find(TrackingNumber.class, trackingNumberId);
      productReservationToSave.setTrackingNumber(trackingNumber);
    }

    productReservationRepository.save(productReservationToSave);
  }

  @Override
  public LinkedHashMap<Object, Object> setMapSaleOrderLine(
      SaleOrderLine proxySaleOrderLine,
      Map<String, Object> mapParent,
      ProductReservation newProductReservation) {
    LinkedHashMap<Object, Object> mapSaleOrderLine = new LinkedHashMap<>();
    mapSaleOrderLine.put("id", mapParent.get("id"));
    newProductReservation.setOriginSaleOrderLine(proxySaleOrderLine);
    return mapSaleOrderLine;
  }

  @Override
  public void createSaleOrderLineProductReservation(
      ProductReservation newProductReservation,
      Long productId,
      SaleOrderLine proxySaleOrderLine,
      int productReservationType)
      throws AxelorException {
    newProductReservation.setProductReservationType(productReservationType);
    Product product = productRepository.find(productId);
    newProductReservation.setProduct(product);
    newProductReservation.setOriginSaleOrderLine(proxySaleOrderLine);
    updateStatus(newProductReservation, false);
  }

  @Override
  public List<ProductReservation> findProductReservation(
      int productReservationType, Long productId, String originModelClassName, Long originId) {
    Model originInstanceModel = getInstanceModel(originModelClassName);
    assert originInstanceModel != null;
    originInstanceModel.setId(originId);
    return productReservationRepository
        .findByOriginAndProductReservationType(
            productReservationType, originInstanceModel, productId)
        .fetch();
  }

  @Override
  public void enrichProductReservationOnNew(
      ProductReservation newProductReservation,
      Long productId,
      String originModelClassName,
      Long originId,
      int typeProductReservationReservation)
      throws AxelorException {
    Model originInstanceModel = getInstanceModel(originModelClassName);
    assert originInstanceModel != null;
    originInstanceModel.setId(originId);

    newProductReservation.setProductReservationType(typeProductReservationReservation);
    Product product = productRepository.find(productId);
    newProductReservation.setProduct(product);
    originInstanceModel = JPA.find(originInstanceModel.getClass(), originInstanceModel.getId());
    setOrigin(newProductReservation, originInstanceModel);
    updateStatus(newProductReservation, false);
  }

  /**
   * Never called. Override concrete implementation called is
   * ProductReservationServiceProductionImpl#setOrigin thanks to bind in ProductionModule#configure
   */
  public void setOrigin(ProductReservation newProductReservation, Model originInstanceModel) {
    throw new RuntimeException("Should never be called"); // never happen
  }

  /**
   * Never called. Override concrete implementation called is
   * ProductReservationServiceProductionImpl#getOrigin thanks to bind in ProductionModule#configure
   */
  public Model getOrigin(ProductReservation productReservationToSave, Model originInstanceModel) {
    throw new RuntimeException("Should never be called"); // never happen
  }

  private Model getInstanceModel(String modelClassName) {
    try {
      @SuppressWarnings("unchecked")
      Class<? extends Model> modelClass =
          (Class<? extends Model>)
              Class.forName(
                  modelClassName); // maybe handle ManufOrder, unknown in supplychain module
      return modelClass.getDeclaredConstructor().newInstance();
    } catch (ClassNotFoundException
        | InvocationTargetException
        | InstantiationException
        | IllegalAccessException
        | NoSuchMethodException e) {
      TraceBackService.trace(e); // never happen
    }
    return null;
  }

  public Long getProductIdFromMap(Map<String, Object> map) {
    @SuppressWarnings("unchecked")
    Map<String, Object> mapProduct = (Map<String, Object>) map.get("product");
    if ((mapProduct != null) && (mapProduct.get("id") != null)) {
      Object oId = mapProduct.get("id");
      return Long.valueOf(oId.toString());
    }
    return null;
  }

  // REALIZE STOCKMOVE

  @Override
  public void onRealizeStockMove(StockMove stockMove) {
    StockLocation fromStockLocation = stockMove.getFromStockLocation();
    List<StockMoveLine> stockMoveLineList = stockMove.getStockMoveLineList();
    stockMoveLineList
        .parallelStream()
        .anyMatch(
            line -> {
              return productReservationRepository
                      .findByProductReservationTypeAndStockLocationAndProduct(
                          AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_ALLOCATION,
                          fromStockLocation,
                          line.getProduct())
                      .count()
                  > 0;
            });
    /*
        select spr.id, spr.product, spr.stock_location from supplychain_product_reservation spr
    inner join stock_stock_move_line ssml on spr.product = ssml.product and ssml.from_stock_location = spr.stock_location
    inner join stock_stock_move ssm on ssm.id=ssml.stock_move
    where
    ssm.id = :stockMoveId and
    spr.product_reservation_type = :productReservationTypeAllocation and
      */
    // javax.persistence.Query nativeQuery =
    Query nativeQuery =
        JPA.em()
            .createNativeQuery(
                "SELECT id, archived, import_id, import_origin, process_instance_id, \"version\","
                    + " created_on, updated_on, attrs, description, is_allocation, is_reservation,"
                    + " priority_reservation_date_time, product_reservation_type, qty,"
                    + " requested_reserved_type, status, created_by, updated_by,"
                    + " origin_sale_order_line, product, stock_location, tracking_number,"
                    + " origin_manuf_order_id,"
                    + " origin_manuf_order"
                    + " FROM supplychain_product_reservation as spr "
                    + " INNER JOIN stock_stock_move_line as ssml on spr.product = ssml.product and ssml.from_stock_location = spr.stock_location"
                    + " INNER JOIN stock_stock_move as ssm on ssm.id=ssml.stock_move"
                    + " WHERE"
                    + " ssm.id=:stockMoveId AND"
                    + " spr.product_reservation_type=:productReservationTypeAllocation AND"
                    + " spr.status=:status AND"
                    + " spr.qty > 0 AND"
                    + " (spr.archived is NULL OR spr.archived = false)",
                ProductReservation.class);
    nativeQuery.setParameter("stockMoveId", stockMove.getId());
    nativeQuery.setParameter(
        "productReservationTypeAllocation",
        AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_ALLOCATION);
    nativeQuery.setParameter(
        "status", AbstractProductReservationRepository.PRODUCT_RESERVATION_STATUS_IN_PROGRESS);

    List<?> resultList = nativeQuery.getResultList();

    resultList.forEach(
        pr -> {
          ProductReservation productReservation = (ProductReservation) pr;
          if (productReservation.getProduct() != null
              && productReservation.getProduct().getTrackingNumberConfiguration() == null) {
            StockLocation stockLocation = productReservation.getStockLocation();
            System.out.println(
                "CMR produit sans tracking :::: " + productReservation.getProduct().getCode());
          } else {
            System.out.println(
                "CMR produit avec tracking :::: " + productReservation.getProduct().getCode());
          }
        });
  }
}
