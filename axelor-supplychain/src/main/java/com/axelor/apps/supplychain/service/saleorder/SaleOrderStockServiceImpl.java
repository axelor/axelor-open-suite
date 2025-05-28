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
package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderDeliveryAddressService;
import com.axelor.apps.stock.db.PartnerStockSettings;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.PartnerStockSettingsService;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.db.repo.SupplyChainConfigRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.StockMoveLineServiceSupplychain;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineBlockingSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineServiceSupplyChain;
import com.axelor.i18n.I18n;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

public class SaleOrderStockServiceImpl implements SaleOrderStockService {

  protected StockMoveService stockMoveService;
  protected StockMoveLineService stockMoveLineService;
  protected StockConfigService stockConfigService;
  protected UnitConversionService unitConversionService;
  protected SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain;
  protected StockMoveLineServiceSupplychain stockMoveLineSupplychainService;
  protected StockMoveLineRepository stockMoveLineRepository;
  protected AppBaseService appBaseService;
  protected SaleOrderRepository saleOrderRepository;
  protected AppSupplychainService appSupplychainService;
  protected SupplyChainConfigService supplyChainConfigService;
  protected ProductCompanyService productCompanyService;
  protected PartnerStockSettingsService partnerStockSettingsService;
  protected TaxService taxService;
  protected SaleOrderDeliveryAddressService saleOrderDeliveryAddressService;
  protected final SaleOrderLineBlockingSupplychainService saleOrderLineBlockingSupplychainService;

  @Inject
  public SaleOrderStockServiceImpl(
      StockMoveService stockMoveService,
      StockMoveLineService stockMoveLineService,
      StockConfigService stockConfigService,
      UnitConversionService unitConversionService,
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain,
      StockMoveLineServiceSupplychain stockMoveLineSupplychainService,
      StockMoveLineRepository stockMoveLineRepository,
      AppBaseService appBaseService,
      SaleOrderRepository saleOrderRepository,
      AppSupplychainService appSupplychainService,
      SupplyChainConfigService supplyChainConfigService,
      ProductCompanyService productCompanyService,
      PartnerStockSettingsService partnerStockSettingsService,
      TaxService taxService,
      SaleOrderDeliveryAddressService saleOrderDeliveryAddressService,
      SaleOrderLineBlockingSupplychainService saleOrderLineBlockingSupplychainService) {
    this.stockMoveService = stockMoveService;
    this.stockMoveLineService = stockMoveLineService;
    this.stockConfigService = stockConfigService;
    this.unitConversionService = unitConversionService;
    this.saleOrderLineServiceSupplyChain = saleOrderLineServiceSupplyChain;
    this.stockMoveLineSupplychainService = stockMoveLineSupplychainService;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.appBaseService = appBaseService;
    this.saleOrderRepository = saleOrderRepository;
    this.appSupplychainService = appSupplychainService;
    this.supplyChainConfigService = supplyChainConfigService;
    this.productCompanyService = productCompanyService;
    this.partnerStockSettingsService = partnerStockSettingsService;
    this.taxService = taxService;
    this.saleOrderDeliveryAddressService = saleOrderDeliveryAddressService;
    this.saleOrderLineBlockingSupplychainService = saleOrderLineBlockingSupplychainService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<Long> createStocksMovesFromSaleOrder(SaleOrder saleOrder) throws AxelorException {

    if (!this.isSaleOrderWithProductsToDeliver(saleOrder)) {
      return null;
    }

    if (saleOrder.getStockLocation() == null) {
      throw new AxelorException(
          saleOrder,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(SupplychainExceptionMessage.SO_MISSING_STOCK_LOCATION),
          saleOrder.getSaleOrderSeq());
    }

    List<Long> stockMoveList = new ArrayList<>();

    Map<Pair<String, LocalDate>, List<SaleOrderLine>> saleOrderLineMap =
        getSaleOrderLineMap(saleOrder);

    for (Map.Entry<Pair<String, LocalDate>, List<SaleOrderLine>> entry :
        saleOrderLineMap.entrySet()) {

      Pair<String, LocalDate> key = entry.getKey();
      String deliveryAddressStr = key.getLeft();
      LocalDate estimatedDeliveryDate = key.getRight();

      List<SaleOrderLine> saleOrderLineList = entry.getValue();

      Optional<StockMove> stockMove =
          createStockMove(saleOrder, deliveryAddressStr, estimatedDeliveryDate, saleOrderLineList);

      stockMove.map(StockMove::getId).ifPresent(stockMoveList::add);
    }
    return stockMoveList;
  }

  protected Optional<StockMove> createStockMove(
      SaleOrder saleOrder,
      String deliveryAddressStr,
      LocalDate estimatedDeliveryDate,
      List<SaleOrderLine> saleOrderLineList)
      throws AxelorException {
    Company company = saleOrder.getCompany();
    StockMove stockMove =
        this.createStockMove(
            saleOrder, company, saleOrderLineList, deliveryAddressStr, estimatedDeliveryDate);
    stockMove.setDeliveryCondition(saleOrder.getDeliveryCondition());

    StockLocation toStockLocation = saleOrder.getToStockLocation();
    if (toStockLocation == null) {
      toStockLocation =
          partnerStockSettingsService.getDefaultExternalStockLocation(
              saleOrder.getClientPartner(), company, null);
    }
    if (toStockLocation == null) {
      toStockLocation =
          stockConfigService.getCustomerVirtualStockLocation(
              stockConfigService.getStockConfig(company));
    }

    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      if (saleOrderLine.getProduct() != null) {
        if (saleOrderLineBlockingSupplychainService.isDeliveryBlocked(saleOrderLine)) {
          continue;
        }
        BigDecimal qty = saleOrderLineServiceSupplyChain.computeUndeliveredQty(saleOrderLine);
        if (qty.signum() > 0 && !existActiveStockMoveForSaleOrderLine(saleOrderLine)) {
          createStockMoveLine(
              stockMove, saleOrderLine, qty, saleOrder.getStockLocation(), toStockLocation);
        }
      }
    }

    if (stockMove.getStockMoveLineList() == null || stockMove.getStockMoveLineList().isEmpty()) {
      return Optional.empty();
    }

    if (stockMove.getStockMoveLineList().stream()
        .noneMatch(
            stockMoveLine ->
                stockMoveLine.getSaleOrderLine() != null
                    && stockMoveLine.getSaleOrderLine().getTypeSelect()
                        == SaleOrderLineRepository.TYPE_NORMAL)) {
      stockMove.setFullySpreadOverLogisticalFormsFlag(true);
    }

    boolean isNeedingConformityCertificate = saleOrder.getIsNeedingConformityCertificate();
    stockMove.setIsNeedingConformityCertificate(isNeedingConformityCertificate);

    if (isNeedingConformityCertificate) {
      stockMove.setSignatoryUser(
          stockConfigService.getStockConfig(stockMove.getCompany()).getSignatoryUser());
    }

    SupplyChainConfig supplychainConfig =
        supplyChainConfigService.getSupplyChainConfig(saleOrder.getCompany());

    if (supplychainConfig.getDefaultEstimatedDate() != null
        && supplychainConfig.getDefaultEstimatedDate() == SupplyChainConfigRepository.CURRENT_DATE
        && stockMove.getEstimatedDate() == null) {
      stockMove.setEstimatedDate(appBaseService.getTodayDate(saleOrder.getCompany()));
    } else if (supplychainConfig.getDefaultEstimatedDate()
            == SupplyChainConfigRepository.CURRENT_DATE_PLUS_DAYS
        && stockMove.getEstimatedDate() == null) {
      stockMove.setEstimatedDate(
          appBaseService
              .getTodayDate(saleOrder.getCompany())
              .plusDays(supplychainConfig.getNumberOfDays().longValue()));
    }

    setReservationDateTime(stockMove, saleOrder);
    stockMoveService.planWithNoSplit(stockMove);

    return Optional.of(stockMove);
  }

  protected Map<Pair<String, LocalDate>, List<SaleOrderLine>> getSaleOrderLineMap(
      SaleOrder saleOrder) {

    Map<Pair<String, LocalDate>, List<SaleOrderLine>> saleOrderLineMap = new HashMap<>();

    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {

      if (saleOrderLineServiceSupplyChain.computeUndeliveredQty(saleOrderLine).signum() <= 0) {
        continue;
      }

      Pair<String, LocalDate> key = getDeliveryInformation(saleOrderLine);

      saleOrderLineMap.computeIfAbsent(key, k -> new ArrayList<>()).add(saleOrderLine);
    }

    return saleOrderLineMap;
  }

  protected Pair<String, LocalDate> getDeliveryInformation(SaleOrderLine saleOrderLine) {
    String addressKey = saleOrderLine.getDeliveryAddressStr();
    if (addressKey == null) {
      addressKey = saleOrderLine.getSaleOrder().getDeliveryAddressStr();
    }

    LocalDate dateKey = saleOrderLine.getEstimatedShippingDate();
    if (dateKey == null) {
      dateKey = saleOrderLine.getSaleOrder().getEstimatedShippingDate();
    }
    if (dateKey == null) {
      dateKey = saleOrderLine.getDesiredDeliveryDate();
    }

    return Pair.of(addressKey, dateKey);
  }

  protected boolean isSaleOrderWithProductsToDeliver(SaleOrder saleOrder) throws AxelorException {

    if (saleOrder.getSaleOrderLineList() == null) {
      return false;
    }

    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {

      if (this.isStockMoveProduct(saleOrderLine)) {

        return true;
      }
    }
    return false;
  }

  @Override
  public StockMove createStockMove(
      SaleOrder saleOrder,
      Company company,
      List<SaleOrderLine> saleOrderLineList,
      String deliveryAddressStr,
      LocalDate estimatedDeliveryDate)
      throws AxelorException {
    StockLocation toStockLocation = saleOrder.getToStockLocation();
    if (toStockLocation == null) {
      toStockLocation =
          partnerStockSettingsService.getDefaultExternalStockLocation(
              saleOrder.getClientPartner(), company, null);
    }
    if (toStockLocation == null) {
      toStockLocation =
          stockConfigService.getCustomerVirtualStockLocation(
              stockConfigService.getStockConfig(company));
    }

    Partner partner = computePartnerToUseForStockMove(saleOrder);
    Address deliveryAddress =
        saleOrderDeliveryAddressService.getDeliveryAddress(saleOrder, saleOrderLineList);

    StockMove stockMove =
        stockMoveService.createStockMove(
            null,
            deliveryAddress,
            company,
            partner,
            saleOrder.getStockLocation(),
            toStockLocation,
            null,
            estimatedDeliveryDate,
            saleOrder.getDescription(),
            saleOrder.getShipmentMode(),
            saleOrder.getFreightCarrierMode(),
            saleOrder.getCarrierPartner(),
            saleOrder.getForwarderPartner(),
            saleOrder.getIncoterm(),
            StockMoveRepository.TYPE_OUTGOING);

    stockMove.setToAddressStr(deliveryAddressStr);
    stockMove.setSaleOrderSet(Sets.newHashSet(saleOrder));
    stockMove.setOrigin(saleOrder.getSaleOrderSeq());
    stockMove.setStockMoveLineList(new ArrayList<>());
    stockMove.setTradingName(saleOrder.getTradingName());
    stockMove.setSpecificPackage(saleOrder.getSpecificPackage());
    stockMove.setNote(saleOrder.getDeliveryComments());
    stockMove.setPickingOrderComments(saleOrder.getPickingOrderComments());
    stockMove.setGroupProductsOnPrintings(partner.getGroupProductsOnPrintings());
    stockMove.setInvoicedPartner(saleOrder.getInvoicedPartner());
    if (stockMove.getPartner() != null) {
      setDefaultAutoMailSettings(stockMove);
    }
    if (saleOrder.getPrintingSettings() != null) {
      stockMove.setPrintingSettings(saleOrder.getPrintingSettings());
    }
    return stockMove;
  }

  /**
   * Fill reservation date time in stock move lines with sale order following supplychain
   * configuration.
   *
   * @param stockMove
   * @param saleOrder
   */
  protected void setReservationDateTime(StockMove stockMove, SaleOrder saleOrder)
      throws AxelorException {
    SupplyChainConfig supplyChainConfig =
        supplyChainConfigService.getSupplyChainConfig(saleOrder.getCompany());

    List<StockMoveLine> stockMoveLineList = stockMove.getStockMoveLineList();
    if (stockMoveLineList == null) {
      stockMoveLineList = new ArrayList<>();
    }
    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      LocalDateTime reservationDateTime;

      switch (supplyChainConfig.getSaleOrderReservationDateSelect()) {
        case SupplyChainConfigRepository.SALE_ORDER_CONFIRMATION_DATE:
          reservationDateTime = saleOrder.getConfirmationDateTime();
          break;
        case SupplyChainConfigRepository.SALE_ORDER_SHIPPING_DATE:
          SaleOrderLine saleOrderLine = stockMoveLine.getSaleOrderLine();
          if (saleOrderLine == null || saleOrderLine.getEstimatedShippingDate() == null) {
            reservationDateTime = null;
          } else {
            reservationDateTime = saleOrderLine.getEstimatedShippingDate().atStartOfDay();
          }
          break;
        default:
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(
                  SupplychainExceptionMessage.RESERVATION_SALE_ORDER_DATE_CONFIG_INCORRECT_VALUE));
      }

      if (reservationDateTime == null) {
        reservationDateTime = appBaseService.getTodayDateTime().toLocalDateTime();
      }
      stockMoveLine.setReservationDateTime(reservationDateTime);
    }
  }

  /**
   * Set automatic mail configuration from the partner.
   *
   * @param stockMove
   */
  protected void setDefaultAutoMailSettings(StockMove stockMove) throws AxelorException {
    Partner partner = stockMove.getPartner();
    Company company = stockMove.getCompany();

    PartnerStockSettings mailSettings =
        partnerStockSettingsService.getOrCreateMailSettings(partner, company);

    stockMove.setRealStockMoveAutomaticMail(mailSettings.getRealStockMoveAutomaticMail());
    stockMove.setRealStockMoveMessageTemplate(mailSettings.getRealStockMoveMessageTemplate());
    stockMove.setPlannedStockMoveAutomaticMail(mailSettings.getPlannedStockMoveAutomaticMail());
    stockMove.setPlannedStockMoveMessageTemplate(mailSettings.getPlannedStockMoveMessageTemplate());
  }

  @Override
  public StockMoveLine createStockMoveLine(
      StockMove stockMove,
      SaleOrderLine saleOrderLine,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {
    return createStockMoveLine(
        stockMove,
        saleOrderLine,
        saleOrderLineServiceSupplyChain.computeUndeliveredQty(saleOrderLine),
        fromStockLocation,
        toStockLocation);
  }

  @Override
  public StockMoveLine createStockMoveLine(
      StockMove stockMove,
      SaleOrderLine saleOrderLine,
      BigDecimal qty,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {

    if (this.isStockMoveProduct(saleOrderLine)) {

      Unit unit = saleOrderLine.getProduct().getUnit();
      BigDecimal priceDiscounted = saleOrderLine.getPriceDiscounted();
      BigDecimal requestedReservedQty =
          saleOrderLine.getRequestedReservedQty().subtract(saleOrderLine.getDeliveredQty());

      BigDecimal companyUnitPriceUntaxed =
          (BigDecimal)
              productCompanyService.get(
                  saleOrderLine.getProduct(),
                  "costPrice",
                  saleOrderLine.getSaleOrder() != null
                      ? saleOrderLine.getSaleOrder().getCompany()
                      : null);
      if (unit != null && !unit.equals(saleOrderLine.getUnit())) {
        qty =
            unitConversionService.convert(
                saleOrderLine.getUnit(), unit, qty, qty.scale(), saleOrderLine.getProduct());
        priceDiscounted =
            unitConversionService.convert(
                unit,
                saleOrderLine.getUnit(),
                priceDiscounted,
                appBaseService.getNbDecimalDigitForUnitPrice(),
                saleOrderLine.getProduct());
        requestedReservedQty =
            unitConversionService.convert(
                saleOrderLine.getUnit(),
                unit,
                requestedReservedQty,
                requestedReservedQty.scale(),
                saleOrderLine.getProduct());
      }

      BigDecimal taxRate = BigDecimal.ZERO;
      Set<TaxLine> taxLineSet = saleOrderLine.getTaxLineSet();
      if (CollectionUtils.isNotEmpty(taxLineSet)) {
        taxRate = taxService.getTotalTaxRateInPercentage(taxLineSet);
      }
      if (saleOrderLine.getQty().signum() != 0) {
        companyUnitPriceUntaxed =
            saleOrderLine
                .getCompanyExTaxTotal()
                .divide(
                    saleOrderLine.getQty(),
                    appBaseService.getNbDecimalDigitForUnitPrice(),
                    RoundingMode.HALF_UP);
      }

      StockMoveLine stockMoveLine =
          stockMoveLineSupplychainService.createStockMoveLine(
              saleOrderLine.getProduct(),
              saleOrderLine.getProductName(),
              saleOrderLine.getDescription(),
              qty,
              requestedReservedQty,
              priceDiscounted,
              companyUnitPriceUntaxed,
              null,
              unit,
              stockMove,
              StockMoveLineService.TYPE_SALES,
              saleOrderLine.getSaleOrder().getInAti(),
              taxRate,
              saleOrderLine,
              null,
              fromStockLocation,
              toStockLocation);

      if (saleOrderLine.getDeliveryState() == 0) {
        saleOrderLine.setDeliveryState(SaleOrderLineRepository.DELIVERY_STATE_NOT_DELIVERED);
      }

      return stockMoveLine;
    }
    return null;
  }

  @Override
  public boolean isStockMoveProduct(SaleOrderLine saleOrderLine) throws AxelorException {
    return isStockMoveProduct(saleOrderLine, saleOrderLine.getSaleOrder());
  }

  @Override
  public boolean isStockMoveProduct(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {

    Company company = saleOrder.getCompany();

    SupplyChainConfig supplyChainConfig = supplyChainConfigService.getSupplyChainConfig(company);

    Product product = saleOrderLine.getProduct();

    return (product != null
        && ((ProductRepository.PRODUCT_TYPE_SERVICE.equals(product.getProductTypeSelect())
                && supplyChainConfig.getHasOutSmForNonStorableProduct()
                && !product.getIsShippingCostsProduct())
            || (ProductRepository.PRODUCT_TYPE_STORABLE.equals(product.getProductTypeSelect())
                && supplyChainConfig.getHasOutSmForStorableProduct())));
  }

  protected boolean existActiveStockMoveForSaleOrderLine(SaleOrderLine saleOrderLine) {

    long stockMoveLineCount =
        stockMoveLineRepository
            .all()
            .filter(
                "self.saleOrderLine.id = ?1 AND self.stockMove.statusSelect in (?2,?3)",
                saleOrderLine.getId(),
                StockMoveRepository.STATUS_DRAFT,
                StockMoveRepository.STATUS_PLANNED)
            .count();

    return stockMoveLineCount > 0;
  }

  @Override
  public void updateDeliveryState(SaleOrder saleOrder) throws AxelorException {
    saleOrder.setDeliveryState(computeDeliveryState(saleOrder));
  }

  @Override
  public void fullyUpdateDeliveryState(SaleOrder saleOrder) throws AxelorException {
    saleOrderLineServiceSupplyChain.updateDeliveryStates(saleOrder.getSaleOrderLineList());
    updateDeliveryState(saleOrder);
  }

  protected int computeDeliveryState(SaleOrder saleOrder) throws AxelorException {

    if (saleOrder.getSaleOrderLineList() == null || saleOrder.getSaleOrderLineList().isEmpty()) {
      return SaleOrderRepository.DELIVERY_STATE_NOT_DELIVERED;
    }

    int deliveryState = -1;

    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {

      if (this.isStockMoveProduct(saleOrderLine, saleOrder)) {

        if (saleOrderLine.getDeliveryState() == SaleOrderLineRepository.DELIVERY_STATE_DELIVERED) {
          if (deliveryState == SaleOrderRepository.DELIVERY_STATE_NOT_DELIVERED) {
            return SaleOrderRepository.DELIVERY_STATE_PARTIALLY_DELIVERED;
          } else {
            deliveryState = SaleOrderRepository.DELIVERY_STATE_DELIVERED;
          }
        } else if (saleOrderLine.getDeliveryState()
            == SaleOrderLineRepository.DELIVERY_STATE_NOT_DELIVERED) {
          if (deliveryState == SaleOrderRepository.DELIVERY_STATE_DELIVERED) {
            return SaleOrderRepository.DELIVERY_STATE_PARTIALLY_DELIVERED;
          } else {
            deliveryState = SaleOrderRepository.DELIVERY_STATE_NOT_DELIVERED;
          }
        } else if (saleOrderLine.getDeliveryState()
            == SaleOrderLineRepository.DELIVERY_STATE_PARTIALLY_DELIVERED) {
          return SaleOrderRepository.DELIVERY_STATE_PARTIALLY_DELIVERED;
        }
      }
    }
    return deliveryState;
  }

  /**
   * Use delivered partner if the configuration is set in generated stock move, else the default is
   * client partner.
   */
  protected Partner computePartnerToUseForStockMove(SaleOrder saleOrder) {
    if (appBaseService.getAppBase().getActivatePartnerRelations()
        && saleOrder.getDeliveredPartner() != null) {
      return saleOrder.getDeliveredPartner();
    } else {
      return saleOrder.getClientPartner();
    }
  }
}
