package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.supplychain.db.ProductReservation;
import com.axelor.apps.supplychain.db.repo.AbstractProductReservationRepository;
import com.axelor.apps.supplychain.db.repo.ProductReservationRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductReservationServiceImpl implements ProductReservationService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected ProductReservationRepository productReservationRepository;

  @Inject
  public ProductReservationServiceImpl(ProductReservationRepository productReservationRepository) {
    this.productReservationRepository = productReservationRepository;
  }

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
    BigDecimal realQty =
        Beans.get(StockLocationService.class)
            .getRealQty(
                productReservation.getProduct().getId(),
                productReservation.getStockLocation().getId(),
                productReservation.getStockLocation().getCompany().getId());
    return realQty.subtract(alreadyAllocatedQty);
  }

  protected BigDecimal computeAvailableQuantityForProduct(ProductReservation productReservation) {
    Product product = productReservation.getProduct();
    StockLocation stockLocation = productReservation.getStockLocation();
    if (Boolean.TRUE.equals(productReservation.getIsAllocation())) {
      return computeAvailableQuantityForProductReservationReserved(product, stockLocation);
    }
    return computeAvailableQuantityForProductReservationRequestedReserved(product);
  }

  private BigDecimal computeAvailableQuantityForProductReservationRequestedReserved(
      Product product) {
    BigDecimal realQty;
    BigDecimal reservedQty;
    reservedQty = getReservedQty(product, null);
    realQty = getRealQty(product, null);
    return realQty.subtract(reservedQty).setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal computeAvailableQuantityForProductReservationReserved(
      Product product, StockLocation stockLocation) {
    BigDecimal reservedQty = getReservedQty(product, stockLocation);
    BigDecimal realQty = getRealQty(product, stockLocation);
    return realQty.subtract(reservedQty).setScale(2, RoundingMode.HALF_UP);
  }

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

  public BigDecimal getReservedQty(Product product, StockLocation stockLocation) {
    if (stockLocation == null) {
      return sumQtyOrZero(
          productReservationRepository
              .findByProductReservationTypeAndStatusAndProduct(
                  AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_ALLOCATION,
                  AbstractProductReservationRepository.PRODUCT_RESERVATION_STATUS_IN_PROGRESS,
                  product)
              .fetchStream());
    }
    return sumQtyOrZero(
        productReservationRepository
            .findByProductReservationTypeAndStatusAndStockLocationAndProduct(
                AbstractProductReservationRepository.TYPE_PRODUCT_RESERVATION_ALLOCATION,
                AbstractProductReservationRepository.PRODUCT_RESERVATION_STATUS_IN_PROGRESS,
                stockLocation,
                product)
            .fetchStream());
  }

  protected BigDecimal sumQtyOrZero(Stream<ProductReservation> productReservationStream) {
    return productReservationStream
        .filter(
            productReservation ->
                productReservation.getQty() != null
                    && !Objects.equals(productReservation.getQty(), BigDecimal.ZERO))
        .map(ProductReservation::getQty)
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);
  }

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
   * Création d'une réservation : TODO CMR i18n commentaire
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
   * avoir tous les productreservation correspondant TODO CMR traduire javadoc
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

  // NEW

  /**
   * required
   *
   * @param saleOrderLine
   * @throws AxelorException
   */
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
    // productReservation.setRequestedReservedType(null);// on ne sait pas au moment de la création
    productReservation.setProduct(saleOrderLine.getProduct());
    // productReservation.setStockLocation(null);// une réservation n'a pas de stocklocation
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
            ProductReservationRepository.TYPE_PRODUCT_RESERVATION_RESERVATION, saleOrderLineProxy)
        .fetch();
  }

  @Override
  public List<ProductReservation> findProductReservationReservedOfSaleOrderLine(
      SaleOrderLine saleOrderLineProxy) {
    return productReservationRepository
        .findByOriginSaleOrderLineAndProductReservationType(
            ProductReservationRepository.TYPE_PRODUCT_RESERVATION_ALLOCATION, saleOrderLineProxy)
        .fetch();
  }

  @Override
  public BigDecimal getAvailableQty(ProductReservation productReservation) throws AxelorException {
    return computeAvailableQuantityForProduct(productReservation);
  }

  @Override
  public void saveSelectedProductReservation(List<Map<String,Object>> rawProductReservationRequestedReservedList) {
    try {
      @SuppressWarnings("unchecked")//always ok
      Class<? extends Model> productReservationClass = (Class<? extends Model>) Class.forName(ProductReservation.class.getName());
      rawProductReservationRequestedReservedList.stream()
              .filter(rawMap -> Boolean.TRUE.equals(Boolean.valueOf(rawMap.get("selected").toString())))
              .forEach(
                      rawMap -> {
                        ProductReservation productReservation =  (ProductReservation) Mapper.toBean(productReservationClass, rawMap);
                        JPA.em().merge(productReservation.getProduct());//avoid detach persistenceException
                        Beans.get(ProductReservationRepository.class).save(productReservation);
                      });
    } catch (ClassNotFoundException e) {
      TraceBackService.trace(e);// never happen
    }


  }

  @Override
  public LinkedHashMap<Object, Object> setMapSaleOrderLine(SaleOrderLine proxySaleOrderLine, Map<String, Object> mapParent, ProductReservation newProductReservation) {
      LinkedHashMap<Object, Object> mapSaleOrderLine = new LinkedHashMap<>();
      mapSaleOrderLine.put("id", mapParent.get("id"));
      newProductReservation.setOriginSaleOrderLine(proxySaleOrderLine);
      return mapSaleOrderLine;
  }
}
