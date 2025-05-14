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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ShippingCoefService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.PartnerStockSettingsService;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.db.repo.SupplyChainConfigRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.helpers.StringHelper;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseOrderStockServiceImpl implements PurchaseOrderStockService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final int MAX_ITERATION = 100;

  protected UnitConversionService unitConversionService;
  protected StockMoveLineRepository stockMoveLineRepository;
  protected PurchaseOrderLineServiceSupplyChainImpl purchaseOrderLineServiceSupplychainImpl;
  protected AppBaseService appBaseService;
  protected ShippingCoefService shippingCoefService;
  protected StockMoveLineServiceSupplychain stockMoveLineServiceSupplychain;
  protected StockMoveService stockMoveService;
  protected PartnerStockSettingsService partnerStockSettingsService;
  protected StockConfigService stockConfigService;
  protected ProductCompanyService productCompanyService;
  protected TaxService taxService;
  protected AppStockService appStockService;

  @Inject
  public PurchaseOrderStockServiceImpl(
      UnitConversionService unitConversionService,
      StockMoveLineRepository stockMoveLineRepository,
      PurchaseOrderLineServiceSupplyChainImpl purchaseOrderLineServiceSupplychainImpl,
      AppBaseService appBaseService,
      ShippingCoefService shippingCoefService,
      StockMoveLineServiceSupplychain stockMoveLineServiceSupplychain,
      StockMoveService stockMoveService,
      PartnerStockSettingsService partnerStockSettingsService,
      StockConfigService stockConfigService,
      ProductCompanyService productCompanyService,
      TaxService taxService,
      AppStockService appStockService) {

    this.unitConversionService = unitConversionService;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.purchaseOrderLineServiceSupplychainImpl = purchaseOrderLineServiceSupplychainImpl;
    this.appBaseService = appBaseService;
    this.shippingCoefService = shippingCoefService;
    this.stockMoveLineServiceSupplychain = stockMoveLineServiceSupplychain;
    this.stockMoveService = stockMoveService;
    this.partnerStockSettingsService = partnerStockSettingsService;
    this.stockConfigService = stockConfigService;
    this.productCompanyService = productCompanyService;
    this.taxService = taxService;
    this.appStockService = appStockService;
  }

  /**
   * Méthode permettant de créer un StockMove à partir d'un PurchaseOrder.
   *
   * @param purchaseOrder une commande
   * @throws AxelorException Aucune séquence de StockMove n'a été configurée
   */
  public List<Long> createStockMoveFromPurchaseOrder(PurchaseOrder purchaseOrder)
      throws AxelorException {

    List<Long> stockMoveIdList = new ArrayList<>();

    if (purchaseOrder.getPurchaseOrderLineList() == null || purchaseOrder.getCompany() == null) {
      return stockMoveIdList;
    }

    if (purchaseOrder.getStockLocation() == null) {
      throw new AxelorException(
          purchaseOrder,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(SupplychainExceptionMessage.PO_MISSING_STOCK_LOCATION),
          purchaseOrder.getPurchaseOrderSeq());
    }

    Map<Pair<StockLocation, LocalDate>, List<PurchaseOrderLine>> purchaseOrderLineMap =
        getPurchaseOrderLineMap(purchaseOrder);

    for (Entry<Pair<StockLocation, LocalDate>, List<PurchaseOrderLine>> entry :
        purchaseOrderLineMap.entrySet()) {

      List<PurchaseOrderLine> purchaseOrderLineList = entry.getValue();
      Pair<StockLocation, LocalDate> pair = entry.getKey();
      StockLocation stockLocation = pair.getLeft();
      LocalDate estimatedDeliveryDate = pair.getRight();

      List<Long> stockMoveId =
          createStockMove(
              purchaseOrder, stockLocation, estimatedDeliveryDate, purchaseOrderLineList);

      if (stockMoveId != null && !stockMoveId.isEmpty()) {

        stockMoveIdList.addAll(stockMoveId);
      }
    }
    return stockMoveIdList;
  }

  protected List<Long> createStockMove(
      PurchaseOrder purchaseOrder,
      StockLocation stockLocation,
      LocalDate estimatedDeliveryDate,
      List<PurchaseOrderLine> purchaseOrderLineList)
      throws AxelorException {

    List<Long> stockMoveIdList = new ArrayList<>();

    Partner supplierPartner = purchaseOrder.getSupplierPartner();
    Company company = purchaseOrder.getCompany();

    Address address = Beans.get(PartnerService.class).getDeliveryAddress(supplierPartner);

    StockLocation startLocation = getStartStockLocation(purchaseOrder);

    StockLocation endLocation = null;

    StockMove stockMove =
        stockMoveService.createStockMove(
            address,
            null,
            company,
            supplierPartner,
            startLocation,
            Optional.ofNullable(stockLocation).orElse(purchaseOrder.getStockLocation()),
            null,
            estimatedDeliveryDate,
            purchaseOrder.getNotes(),
            purchaseOrder.getShipmentMode(),
            purchaseOrder.getFreightCarrierMode(),
            null,
            null,
            null,
            StockMoveRepository.TYPE_INCOMING);

    if (appBaseService.getAppBase().getEnableTradingNamesManagement()
        && !CollectionUtils.isEmpty(company.getTradingNameList())) {
      if (purchaseOrder.getTradingName() != null) {
        endLocation = purchaseOrder.getTradingName().getQualityControlDefaultStockLocation();
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(SupplychainExceptionMessage.PURCHASE_ORDER_TRADING_NAME_MISSING));
      }

    } else {
      StockConfig stockConfig = stockConfigService.getStockConfig(company);
      endLocation = stockConfigService.getQualityControlDefaultStockLocation(stockConfig);
    }

    StockMove qualityStockMove =
        stockMoveService.createStockMove(
            address,
            null,
            company,
            supplierPartner,
            startLocation,
            endLocation,
            null,
            estimatedDeliveryDate,
            purchaseOrder.getNotes(),
            purchaseOrder.getShipmentMode(),
            purchaseOrder.getFreightCarrierMode(),
            null,
            null,
            null,
            StockMoveRepository.TYPE_INCOMING);

    stockMove.setPurchaseOrderSet(Sets.newHashSet(purchaseOrder));
    stockMove.setOrigin(purchaseOrder.getPurchaseOrderSeq());
    stockMove.setTradingName(purchaseOrder.getTradingName());
    stockMove.setGroupProductsOnPrintings(purchaseOrder.getGroupProductsOnPrintings());

    qualityStockMove.setPurchaseOrderSet(Sets.newHashSet(purchaseOrder));
    qualityStockMove.setOrigin(purchaseOrder.getPurchaseOrderSeq());
    qualityStockMove.setTradingName(purchaseOrder.getTradingName());
    qualityStockMove.setGroupProductsOnPrintings(purchaseOrder.getGroupProductsOnPrintings());

    SupplyChainConfig supplychainConfig =
        Beans.get(SupplyChainConfigService.class).getSupplyChainConfig(purchaseOrder.getCompany());
    if (supplychainConfig.getDefaultEstimatedDateForPurchaseOrder()
            == SupplyChainConfigRepository.CURRENT_DATE
        && stockMove.getEstimatedDate() == null) {
      stockMove.setEstimatedDate(appBaseService.getTodayDate(company));
    } else if (supplychainConfig.getDefaultEstimatedDateForPurchaseOrder()
            == SupplyChainConfigRepository.CURRENT_DATE_PLUS_DAYS
        && stockMove.getEstimatedDate() == null) {
      stockMove.setEstimatedDate(
          appBaseService
              .getTodayDate(company)
              .plusDays(supplychainConfig.getNumberOfDaysForPurchaseOrder().longValue()));
    }

    for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {
      BigDecimal qty =
          purchaseOrderLineServiceSupplychainImpl.computeUndeliveredQty(purchaseOrderLine);

      if (qty.signum() > 0 && !existActiveStockMoveForPurchaseOrderLine(purchaseOrderLine)) {
        this.createStockMoveLine(
            stockMove, qualityStockMove, purchaseOrderLine, qty, startLocation, endLocation);
      }
    }
    if (stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()) {
      stockMoveService.plan(stockMove);
      stockMoveIdList.add(stockMove.getId());
    }
    if (qualityStockMove.getStockMoveLineList() != null
        && !qualityStockMove.getStockMoveLineList().isEmpty()) {
      stockMoveService.plan(qualityStockMove);
      stockMoveIdList.add(qualityStockMove.getId());
    }

    return stockMoveIdList;
  }

  protected StockLocation getStartStockLocation(PurchaseOrder purchaseOrder)
      throws AxelorException {

    Company company = purchaseOrder.getCompany();

    StockLocation startLocation = purchaseOrder.getFromStockLocation();

    if (startLocation == null) {
      startLocation =
          partnerStockSettingsService.getDefaultExternalStockLocation(
              purchaseOrder.getSupplierPartner(), company, null);
    }

    if (startLocation == null) {
      StockConfig stockConfig = stockConfigService.getStockConfig(company);
      startLocation = stockConfigService.getSupplierVirtualStockLocation(stockConfig);
    }
    if (startLocation == null) {
      throw new AxelorException(
          purchaseOrder,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(SupplychainExceptionMessage.PURCHASE_ORDER_1),
          company.getName());
    }

    return startLocation;
  }

  protected Map<Pair<StockLocation, LocalDate>, List<PurchaseOrderLine>> getPurchaseOrderLineMap(
      PurchaseOrder purchaseOrder) {

    Map<Pair<StockLocation, LocalDate>, List<PurchaseOrderLine>> purchaseOrderLineMap =
        new HashMap<>();

    for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {

      if (purchaseOrderLineServiceSupplychainImpl.computeUndeliveredQty(purchaseOrderLine).signum()
          <= 0) {
        continue;
      }
      StockLocation stockLocation = purchaseOrderLine.getStockLocation();
      StockLocation purchaseOrderStockLocation = purchaseOrder.getStockLocation();

      LocalDate dateKey =
          Optional.ofNullable(purchaseOrderLine.getEstimatedReceiptDate())
              .orElse(purchaseOrder.getEstimatedReceiptDate());

      StockLocation stockLocationKey =
          appStockService.getAppStock().getIsManageStockLocationOnStockMoveLine()
                  && isStockLocationRelatedToPurchaseOrder(
                      stockLocation, purchaseOrderStockLocation)
              ? purchaseOrderStockLocation
              : stockLocation;

      purchaseOrderLineMap
          .computeIfAbsent(Pair.of(stockLocationKey, dateKey), k -> new ArrayList<>())
          .add(purchaseOrderLine);
    }

    return purchaseOrderLineMap;
  }

  protected boolean isStockLocationRelatedToPurchaseOrder(
      StockLocation stockLocation, StockLocation purchaseOrderStockLocation) {
    StockLocation currentLocation = stockLocation;
    int i = 0;
    while (currentLocation != null && i < MAX_ITERATION) {
      if (currentLocation.equals(purchaseOrderStockLocation)) {
        return true;
      }
      currentLocation = currentLocation.getParentStockLocation();
      i++;
    }
    return false;
  }

  public StockMoveLine createStockMoveLine(
      StockMove stockMove,
      StockMove qualityStockMove,
      PurchaseOrderLine purchaseOrderLine,
      BigDecimal qty,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {

    StockMoveLine stockMoveLine = null;

    if (this.isStockMoveProduct(purchaseOrderLine)) {

      stockMoveLine =
          createProductStockMoveLine(
              purchaseOrderLine,
              qty,
              needControlOnReceipt(purchaseOrderLine) ? qualityStockMove : stockMove,
              fromStockLocation,
              needControlOnReceipt(purchaseOrderLine)
                  ? toStockLocation
                  : Optional.ofNullable(purchaseOrderLine.getStockLocation())
                      .orElse(purchaseOrderLine.getPurchaseOrder().getStockLocation()));

    } else if (purchaseOrderLine.getIsTitleLine()) {
      stockMoveLine = createTitleStockMoveLine(purchaseOrderLine, stockMove);
    }
    return stockMoveLine;
  }

  /**
   * @param product
   * @param purchaseOrder
   * @return true if product needs a control on receipt and if the purchase order is not a direct
   *     order
   * @throws AxelorException
   */
  protected boolean needControlOnReceipt(PurchaseOrderLine purchaseOrderLine)
      throws AxelorException {

    Product product = purchaseOrderLine.getProduct();
    PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();
    Boolean controlOnReceipt =
        (Boolean)
            productCompanyService.get(product, "controlOnReceipt", purchaseOrder.getCompany());

    return controlOnReceipt && !purchaseOrder.getStockLocation().getDirectOrderLocation();
  }

  protected StockMoveLine createProductStockMoveLine(
      PurchaseOrderLine purchaseOrderLine,
      BigDecimal qty,
      StockMove stockMove,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {

    PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();
    Product product = purchaseOrderLine.getProduct();
    Unit unit = product.getUnit();
    BigDecimal priceDiscounted = purchaseOrderLine.getPriceDiscounted();
    BigDecimal companyUnitPriceUntaxed = purchaseOrderLine.getCompanyExTaxTotal();

    if (purchaseOrderLine.getQty().compareTo(BigDecimal.ZERO) != 0) {
      companyUnitPriceUntaxed =
          purchaseOrderLine
              .getCompanyExTaxTotal()
              .divide(
                  purchaseOrderLine.getQty(),
                  appBaseService.getNbDecimalDigitForUnitPrice(),
                  RoundingMode.HALF_UP);
    }

    if (unit != null && !unit.equals(purchaseOrderLine.getUnit())) {
      qty =
          unitConversionService.convert(
              purchaseOrderLine.getUnit(), unit, qty, qty.scale(), product);

      priceDiscounted =
          unitConversionService.convert(
              unit,
              purchaseOrderLine.getUnit(),
              priceDiscounted,
              appBaseService.getNbDecimalDigitForUnitPrice(),
              product);

      companyUnitPriceUntaxed =
          unitConversionService.convert(
              unit,
              purchaseOrderLine.getUnit(),
              companyUnitPriceUntaxed,
              appBaseService.getNbDecimalDigitForUnitPrice(),
              product);
    }

    BigDecimal shippingCoef =
        shippingCoefService.getShippingCoef(
            product, purchaseOrder.getSupplierPartner(), purchaseOrder.getCompany(), qty);
    BigDecimal companyPurchasePrice =
        priceDiscounted.setScale(
            appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
    priceDiscounted = priceDiscounted.multiply(shippingCoef);
    companyUnitPriceUntaxed = companyUnitPriceUntaxed.multiply(shippingCoef);

    BigDecimal taxRate = BigDecimal.ZERO;
    Set<TaxLine> taxLineSet = purchaseOrderLine.getTaxLineSet();
    if (CollectionUtils.isNotEmpty(taxLineSet)) {
      taxRate = taxService.getTotalTaxRateInPercentage(taxLineSet);
    }
    if (purchaseOrderLine.getReceiptState() == 0) {
      purchaseOrderLine.setReceiptState(PurchaseOrderLineRepository.RECEIPT_STATE_NOT_RECEIVED);
    }

    return stockMoveLineServiceSupplychain.createStockMoveLine(
        product,
        purchaseOrderLine.getProductName(),
        purchaseOrderLine.getDescription(),
        qty,
        BigDecimal.ZERO,
        priceDiscounted,
        companyUnitPriceUntaxed,
        companyPurchasePrice,
        unit,
        stockMove,
        StockMoveLineService.TYPE_PURCHASES,
        purchaseOrder.getInAti(),
        taxRate,
        null,
        purchaseOrderLine,
        fromStockLocation,
        toStockLocation);
  }

  protected StockMoveLine createTitleStockMoveLine(
      PurchaseOrderLine purchaseOrderLine, StockMove stockMove) throws AxelorException {

    return stockMoveLineServiceSupplychain.createStockMoveLine(
        purchaseOrderLine.getProduct(),
        purchaseOrderLine.getProductName(),
        purchaseOrderLine.getDescription(),
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        BigDecimal.ZERO,
        null,
        stockMove,
        2,
        purchaseOrderLine.getPurchaseOrder().getInAti(),
        null,
        null,
        purchaseOrderLine,
        null,
        null);
  }

  public void cancelReceipt(PurchaseOrder purchaseOrder) throws AxelorException {

    List<StockMove> stockMoveList =
        Beans.get(StockMoveRepository.class)
            .all()
            .filter(
                "? MEMBER OF self.purchaseOrderSet AND self.statusSelect = 2",
                purchaseOrder.getId())
            .fetch();

    for (StockMove stockMove : stockMoveList) {

      stockMoveService.cancel(stockMove);
    }
  }

  public boolean isStockMoveProduct(PurchaseOrderLine purchaseOrderLine) throws AxelorException {
    return isStockMoveProduct(purchaseOrderLine, purchaseOrderLine.getPurchaseOrder());
  }

  public boolean isStockMoveProduct(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder) throws AxelorException {

    Company company = purchaseOrder.getCompany();

    SupplyChainConfig supplyChainConfig =
        Beans.get(SupplyChainConfigService.class).getSupplyChainConfig(company);

    Product product = purchaseOrderLine.getProduct();

    return (product != null
        && ((ProductRepository.PRODUCT_TYPE_SERVICE.equals(product.getProductTypeSelect())
                && supplyChainConfig.getHasInSmForNonStorableProduct()
                && !product.getIsShippingCostsProduct())
            || (ProductRepository.PRODUCT_TYPE_STORABLE.equals(product.getProductTypeSelect())
                && supplyChainConfig.getHasInSmForStorableProduct())));
  }

  protected boolean existActiveStockMoveForPurchaseOrderLine(PurchaseOrderLine purchaseOrderLine) {

    long stockMoveLineCount =
        stockMoveLineRepository
            .all()
            .filter(
                "self.purchaseOrderLine.id = ?1 AND self.stockMove.statusSelect in (?2,?3)",
                purchaseOrderLine.getId(),
                StockMoveRepository.STATUS_DRAFT,
                StockMoveRepository.STATUS_PLANNED)
            .count();

    return stockMoveLineCount > 0;
  }

  // Check if existing at least one stockMove not canceled for the purchaseOrder
  public boolean existActiveStockMoveForPurchaseOrder(Long purchaseOrderId) {
    long nbStockMove =
        Beans.get(StockMoveRepository.class)
            .all()
            .filter(
                "? MEMBER OF self.purchaseOrderSet AND self.statusSelect <> ?",
                purchaseOrderId,
                StockMoveRepository.STATUS_CANCELED)
            .count();
    return nbStockMove > 0;
  }

  public void updateReceiptState(PurchaseOrder purchaseOrder) throws AxelorException {
    purchaseOrder.setReceiptState(computeReceiptState(purchaseOrder));
  }

  protected int computeReceiptState(PurchaseOrder purchaseOrder) throws AxelorException {

    if (purchaseOrder.getPurchaseOrderLineList() == null
        || purchaseOrder.getPurchaseOrderLineList().isEmpty()) {
      return PurchaseOrderRepository.STATE_NOT_RECEIVED;
    }

    int receiptState = -1;

    for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {

      if (this.isStockMoveProduct(purchaseOrderLine, purchaseOrder)) {

        if (purchaseOrderLine.getReceiptState() == PurchaseOrderRepository.STATE_RECEIVED) {
          if (receiptState == PurchaseOrderRepository.STATE_NOT_RECEIVED
              || receiptState == PurchaseOrderRepository.STATE_PARTIALLY_RECEIVED) {
            return PurchaseOrderRepository.STATE_PARTIALLY_RECEIVED;
          } else {
            receiptState = PurchaseOrderRepository.STATE_RECEIVED;
          }
        } else if (purchaseOrderLine.getReceiptState()
            == PurchaseOrderRepository.STATE_NOT_RECEIVED) {
          if (receiptState == PurchaseOrderRepository.STATE_RECEIVED
              || receiptState == PurchaseOrderRepository.STATE_PARTIALLY_RECEIVED) {
            return PurchaseOrderRepository.STATE_PARTIALLY_RECEIVED;
          } else {
            receiptState = PurchaseOrderRepository.STATE_NOT_RECEIVED;
          }
        } else if (purchaseOrderLine.getReceiptState()
            == PurchaseOrderRepository.STATE_PARTIALLY_RECEIVED) {
          return PurchaseOrderRepository.STATE_PARTIALLY_RECEIVED;
        }
      }
    }
    return receiptState;
  }

  @Override
  public String getPurchaseOrderLineListForAProduct(
      Long productId, Long companyId, Long stockLocationId) {
    List<Integer> statusList = new ArrayList<>();
    statusList.add(PurchaseOrderRepository.STATUS_VALIDATED);
    String status =
        Beans.get(AppSupplychainService.class)
            .getAppSupplychain()
            .getpOFilterOnStockDetailStatusSelect();
    if (!StringUtils.isBlank(status)) {
      statusList = StringHelper.getIntegerList(status);
    }
    String statusListQuery =
        statusList.stream().map(String::valueOf).collect(Collectors.joining(","));
    String query =
        "self.product.id = "
            + productId
            + " AND self.receiptState != "
            + PurchaseOrderLineRepository.RECEIPT_STATE_RECEIVED
            + " AND self.purchaseOrder.statusSelect IN ("
            + statusListQuery
            + ")";
    if (companyId != 0L) {
      query += " AND self.purchaseOrder.company.id = " + companyId;
      if (stockLocationId != 0L) {
        StockLocation stockLocation =
            Beans.get(StockLocationRepository.class).find(stockLocationId);
        List<StockLocation> stockLocationList =
            Beans.get(StockLocationService.class)
                .getAllLocationAndSubLocation(stockLocation, false);
        if (!stockLocationList.isEmpty() && stockLocation.getCompany().getId().equals(companyId)) {
          query +=
              " AND self.purchaseOrder.stockLocation.id IN ("
                  + StringHelper.getIdListString(stockLocationList)
                  + ") ";
        }
      }
    }
    return query;
  }

  @Override
  public List<PurchaseOrderLine> updatePurchaseOrderLinesStockLocation(
      PurchaseOrder purchaseOrder) {
    List<PurchaseOrderLine> purchaseOrderLineList = purchaseOrder.getPurchaseOrderLineList();
    if (CollectionUtils.isEmpty(purchaseOrderLineList)) {
      return purchaseOrderLineList;
    }
    for (PurchaseOrderLine purchaseOrderLine : purchaseOrderLineList) {
      purchaseOrderLine.setStockLocation(purchaseOrder.getStockLocation());
    }
    return purchaseOrderLineList;
  }
}
