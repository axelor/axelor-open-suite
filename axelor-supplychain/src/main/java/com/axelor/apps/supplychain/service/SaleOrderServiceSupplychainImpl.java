/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderServiceImpl;
import com.axelor.apps.stock.db.ShipmentMode;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.PartnerStockSettingsService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.db.CustomerShippingCarriagePaid;
import com.axelor.apps.supplychain.db.PartnerSupplychainLink;
import com.axelor.apps.supplychain.db.Timetable;
import com.axelor.apps.supplychain.db.repo.PartnerSupplychainLinkTypeRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppSupplychain;
import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderServiceSupplychainImpl extends SaleOrderServiceImpl
    implements SaleOrderSupplychainService {

  protected AppSupplychainService appSupplychainService;
  protected SaleOrderStockService saleOrderStockService;
  protected PartnerStockSettingsService partnerStockSettingsService;
  protected StockConfigService stockConfigService;

  @Inject
  public SaleOrderServiceSupplychainImpl(
      SaleOrderLineService saleOrderLineService,
      AppBaseService appBaseService,
      SaleOrderLineRepository saleOrderLineRepo,
      SaleOrderRepository saleOrderRepo,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderMarginService saleOrderMarginService,
      AppSupplychainService appSupplychainService,
      SaleOrderStockService saleOrderStockService,
      PartnerStockSettingsService partnerStockSettingsService,
      StockConfigService stockConfigService) {
    super(
        saleOrderLineService,
        appBaseService,
        saleOrderLineRepo,
        saleOrderRepo,
        saleOrderComputeService,
        saleOrderMarginService);
    this.appSupplychainService = appSupplychainService;
    this.saleOrderStockService = saleOrderStockService;
    this.partnerStockSettingsService = partnerStockSettingsService;
    this.stockConfigService = stockConfigService;
  }

  public SaleOrder getClientInformations(SaleOrder saleOrder) {
    Partner client = saleOrder.getClientPartner();
    PartnerService partnerService = Beans.get(PartnerService.class);
    if (client != null) {
      saleOrder.setPaymentCondition(client.getPaymentCondition());
      saleOrder.setPaymentMode(client.getInPaymentMode());
      saleOrder.setMainInvoicingAddress(partnerService.getInvoicingAddress(client));
      this.computeAddressStr(saleOrder);
      saleOrder.setDeliveryAddress(partnerService.getDeliveryAddress(client));
      saleOrder.setPriceList(
          Beans.get(PartnerPriceListService.class)
              .getDefaultPriceList(client, PriceListRepository.TYPE_SALE));
    }
    return saleOrder;
  }

  @Override
  public void updateAmountToBeSpreadOverTheTimetable(SaleOrder saleOrder) {
    List<Timetable> timetableList = saleOrder.getTimetableList();
    BigDecimal totalHT = saleOrder.getExTaxTotal();
    BigDecimal sumTimetableAmount = BigDecimal.ZERO;
    if (timetableList != null) {
      for (Timetable timetable : timetableList) {
        sumTimetableAmount = sumTimetableAmount.add(timetable.getAmount());
      }
    }
    saleOrder.setAmountToBeSpreadOverTheTimetable(totalHT.subtract(sumTimetableAmount));
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public boolean enableEditOrder(SaleOrder saleOrder) throws AxelorException {
    boolean checkAvailabiltyRequest = super.enableEditOrder(saleOrder);
    AppSupplychain appSupplychain = appSupplychainService.getAppSupplychain();

    if (!appSupplychainService.isApp("supplychain")) {
      return checkAvailabiltyRequest;
    }

    List<StockMove> allStockMoves =
        Beans.get(StockMoveRepository.class)
            .findAllBySaleOrderAndStatus(
                StockMoveRepository.ORIGIN_SALE_ORDER,
                saleOrder.getId(),
                StockMoveRepository.STATUS_PLANNED)
            .fetch();
    List<StockMove> stockMoves =
        !allStockMoves.isEmpty()
            ? allStockMoves.stream()
                .filter(stockMove -> !stockMove.getAvailabilityRequest())
                .collect(Collectors.toList())
            : allStockMoves;
    checkAvailabiltyRequest =
        stockMoves.size() != allStockMoves.size() ? true : checkAvailabiltyRequest;
    if (!stockMoves.isEmpty()) {
      StockMoveService stockMoveService = Beans.get(StockMoveService.class);
      CancelReason cancelReason = appSupplychain.getCancelReasonOnChangingSaleOrder();
      if (cancelReason == null) {
        throw new AxelorException(
            appSupplychain,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            SupplychainExceptionMessage.SUPPLYCHAIN_MISSING_CANCEL_REASON_ON_CHANGING_SALE_ORDER);
      }
      for (StockMove stockMove : stockMoves) {
        stockMoveService.cancel(stockMove, cancelReason);
        stockMove.setArchived(true);
        for (StockMoveLine stockMoveline : stockMove.getStockMoveLineList()) {
          stockMoveline.setSaleOrderLine(null);
          stockMoveline.setArchived(true);
        }
      }
    }
    return checkAvailabiltyRequest;
  }

  /**
   * In the supplychain implementation, we check if the user has deleted already delivered qty.
   *
   * @param saleOrder
   * @param saleOrderView
   * @throws AxelorException if the user tried to remove already delivered qty.
   */
  @Override
  public void checkModifiedConfirmedOrder(SaleOrder saleOrder, SaleOrder saleOrderView)
      throws AxelorException {

    if (!appSupplychainService.isApp("supplychain")) {
      super.checkModifiedConfirmedOrder(saleOrder, saleOrderView);
      return;
    }

    List<SaleOrderLine> saleOrderLineList =
        MoreObjects.firstNonNull(saleOrder.getSaleOrderLineList(), Collections.emptyList());
    List<SaleOrderLine> saleOrderViewLineList =
        MoreObjects.firstNonNull(saleOrderView.getSaleOrderLineList(), Collections.emptyList());

    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      if (saleOrderLine.getDeliveryState()
          <= SaleOrderLineRepository.DELIVERY_STATE_NOT_DELIVERED) {
        continue;
      }

      Optional<SaleOrderLine> optionalNewSaleOrderLine =
          saleOrderViewLineList.stream().filter(saleOrderLine::equals).findFirst();

      if (optionalNewSaleOrderLine.isPresent()) {
        SaleOrderLine newSaleOrderLine = optionalNewSaleOrderLine.get();

        if (newSaleOrderLine.getQty().compareTo(saleOrderLine.getDeliveredQty()) < 0) {
          throw new AxelorException(
              saleOrder,
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(SupplychainExceptionMessage.SO_CANT_DECREASE_QTY_ON_DELIVERED_LINE),
              saleOrderLine.getFullName());
        }
      } else {
        throw new AxelorException(
            saleOrder,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(SupplychainExceptionMessage.SO_CANT_REMOVED_DELIVERED_LINE),
            saleOrderLine.getFullName());
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validateChanges(SaleOrder saleOrder) throws AxelorException {
    super.validateChanges(saleOrder);

    if (!appSupplychainService.isApp("supplychain")) {
      return;
    }

    saleOrderStockService.fullyUpdateDeliveryState(saleOrder);
    saleOrder.setOrderBeingEdited(false);

    if (appSupplychainService.getAppSupplychain().getCustomerStockMoveGenerationAuto()) {
      saleOrderStockService.createStocksMovesFromSaleOrder(saleOrder);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateToConfirmedStatus(SaleOrder saleOrder) throws AxelorException {
    if (saleOrder.getStatusSelect() == null
        || saleOrder.getStatusSelect() != SaleOrderRepository.STATUS_ORDER_COMPLETED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.SALE_ORDER_BACK_TO_CONFIRMED_WRONG_STATUS));
    }
    saleOrder.setStatusSelect(SaleOrderRepository.STATUS_ORDER_CONFIRMED);
    saleOrderRepo.save(saleOrder);
  }

  @Override
  public String createShipmentCostLine(SaleOrder saleOrder) throws AxelorException {
    List<SaleOrderLine> saleOrderLines = saleOrder.getSaleOrderLineList();
    Partner client = saleOrder.getClientPartner();
    ShipmentMode shipmentMode = saleOrder.getShipmentMode();

    if (shipmentMode == null) {
      return null;
    }
    Product shippingCostProduct = shipmentMode.getShippingCostsProduct();
    if (shippingCostProduct == null) {
      return null;
    }
    BigDecimal carriagePaidThreshold = shipmentMode.getCarriagePaidThreshold();
    if (client != null) {
      List<CustomerShippingCarriagePaid> carriagePaids =
          client.getCustomerShippingCarriagePaidList();
      for (CustomerShippingCarriagePaid customerShippingCarriagePaid : carriagePaids) {
        if (shipmentMode.getId() == customerShippingCarriagePaid.getShipmentMode().getId()) {
          if (customerShippingCarriagePaid.getShippingCostsProduct() != null) {
            shippingCostProduct = customerShippingCarriagePaid.getShippingCostsProduct();
          }
          carriagePaidThreshold = customerShippingCarriagePaid.getCarriagePaidThreshold();
          break;
        }
      }
    }
    if (carriagePaidThreshold != null && shipmentMode.getHasCarriagePaidPossibility()) {
      if (computeExTaxTotalWithoutShippingLines(saleOrder).compareTo(carriagePaidThreshold) >= 0) {
        String message = removeShipmentCostLine(saleOrder);
        saleOrderComputeService.computeSaleOrder(saleOrder);
        saleOrderMarginService.computeMarginSaleOrder(saleOrder);
        return message;
      }
    }
    if (alreadyHasShippingCostLine(saleOrder, shippingCostProduct)) {
      return null;
    }
    SaleOrderLine shippingCostLine = createShippingCostLine(saleOrder, shippingCostProduct);
    saleOrderLines.add(shippingCostLine);
    saleOrderComputeService.computeSaleOrder(saleOrder);
    saleOrderMarginService.computeMarginSaleOrder(saleOrder);
    return null;
  }

  @Override
  public boolean alreadyHasShippingCostLine(SaleOrder saleOrder, Product shippingCostProduct) {
    List<SaleOrderLine> saleOrderLines = saleOrder.getSaleOrderLineList();
    if (saleOrderLines == null) {
      return false;
    }
    for (SaleOrderLine saleOrderLine : saleOrderLines) {
      if (shippingCostProduct.equals(saleOrderLine.getProduct())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public SaleOrderLine createShippingCostLine(SaleOrder saleOrder, Product shippingCostProduct)
      throws AxelorException {
    SaleOrderLine shippingCostLine = new SaleOrderLine();
    shippingCostLine.setSaleOrder(saleOrder);
    shippingCostLine.setProduct(shippingCostProduct);
    saleOrderLineService.computeProductInformation(shippingCostLine, saleOrder);
    saleOrderLineService.computeValues(saleOrder, shippingCostLine);
    return shippingCostLine;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public String removeShipmentCostLine(SaleOrder saleOrder) {
    List<SaleOrderLine> saleOrderLines = saleOrder.getSaleOrderLineList();
    if (saleOrderLines == null) {
      return null;
    }
    List<SaleOrderLine> linesToRemove = new ArrayList<>();
    for (SaleOrderLine saleOrderLine : saleOrderLines) {
      if (saleOrderLine.getProduct().getIsShippingCostsProduct()) {
        linesToRemove.add(saleOrderLine);
      }
    }
    if (linesToRemove.isEmpty()) {
      return null;
    }
    for (SaleOrderLine lineToRemove : linesToRemove) {
      saleOrderLines.remove(lineToRemove);
      if (lineToRemove.getId() != null) {
        saleOrderLineRepo.remove(lineToRemove);
      }
    }
    saleOrder.setSaleOrderLineList(saleOrderLines);
    return I18n.get("Carriage paid threshold is exceeded, all shipment cost lines are removed");
  }

  @Override
  public BigDecimal computeExTaxTotalWithoutShippingLines(SaleOrder saleOrder) {
    List<SaleOrderLine> saleOrderLines = saleOrder.getSaleOrderLineList();
    if (saleOrderLines == null) {
      return BigDecimal.ZERO;
    }
    BigDecimal exTaxTotal = BigDecimal.ZERO;
    for (SaleOrderLine saleOrderLine : saleOrderLines) {
      if (!saleOrderLine.getProduct().getIsShippingCostsProduct()) {
        exTaxTotal = exTaxTotal.add(saleOrderLine.getExTaxTotal());
      }
    }
    return exTaxTotal;
  }

  public void setDefaultInvoicedAndDeliveredPartnersAndAddresses(SaleOrder saleOrder) {
    if (saleOrder != null
        && saleOrder.getClientPartner() != null
        && saleOrder.getClientPartner().getId() != null) {
      Partner clientPartner =
          Beans.get(PartnerRepository.class).find(saleOrder.getClientPartner().getId());
      if (clientPartner != null) {
        setDefaultInvoicedAndDeliveredPartners(saleOrder, clientPartner);
        setInvoicedAndDeliveredAddresses(saleOrder);
      }
    }
  }

  protected void setInvoicedAndDeliveredAddresses(SaleOrder saleOrder) {
    if (saleOrder.getInvoicedPartner() != null) {
      saleOrder.setMainInvoicingAddress(
          Beans.get(PartnerService.class).getInvoicingAddress(saleOrder.getInvoicedPartner()));
      saleOrder.setMainInvoicingAddressStr(
          Beans.get(AddressService.class).computeAddressStr(saleOrder.getMainInvoicingAddress()));
    }
    if (saleOrder.getDeliveredPartner() != null) {
      saleOrder.setDeliveryAddress(
          Beans.get(PartnerService.class).getDeliveryAddress(saleOrder.getDeliveredPartner()));
      saleOrder.setDeliveryAddressStr(
          Beans.get(AddressService.class).computeAddressStr(saleOrder.getDeliveryAddress()));
    }
  }

  protected void setDefaultInvoicedAndDeliveredPartners(
      SaleOrder saleOrder, Partner clientPartner) {
    if (!CollectionUtils.isEmpty(clientPartner.getPartner1SupplychainLinkList())) {
      List<PartnerSupplychainLink> partnerSupplychainLinkList =
          clientPartner.getPartner1SupplychainLinkList();
      // Retrieve all Invoiced by Type
      List<PartnerSupplychainLink> partnerSupplychainLinkInvoicedByList =
          partnerSupplychainLinkList.stream()
              .filter(
                  partnerSupplychainLink ->
                      PartnerSupplychainLinkTypeRepository.TYPE_SELECT_INVOICED_BY.equals(
                          partnerSupplychainLink.getPartnerSupplychainLinkType().getTypeSelect()))
              .collect(Collectors.toList());
      // Retrieve all Delivered by Type
      List<PartnerSupplychainLink> partnerSupplychainLinkDeliveredByList =
          partnerSupplychainLinkList.stream()
              .filter(
                  partnerSupplychainLink ->
                      PartnerSupplychainLinkTypeRepository.TYPE_SELECT_DELIVERED_BY.equals(
                          partnerSupplychainLink.getPartnerSupplychainLinkType().getTypeSelect()))
              .collect(Collectors.toList());

      // If there is only one, then it is the default one
      if (partnerSupplychainLinkInvoicedByList.size() == 1) {
        PartnerSupplychainLink partnerSupplychainLinkInvoicedBy =
            partnerSupplychainLinkInvoicedByList.get(0);
        saleOrder.setInvoicedPartner(partnerSupplychainLinkInvoicedBy.getPartner2());
      } else if (partnerSupplychainLinkInvoicedByList.isEmpty()) {
        saleOrder.setInvoicedPartner(clientPartner);
      } else {
        saleOrder.setInvoicedPartner(null);
      }
      if (partnerSupplychainLinkDeliveredByList.size() == 1) {
        PartnerSupplychainLink partnerSupplychainLinkDeliveredBy =
            partnerSupplychainLinkDeliveredByList.get(0);
        saleOrder.setDeliveredPartner(partnerSupplychainLinkDeliveredBy.getPartner2());
      } else if (partnerSupplychainLinkDeliveredByList.isEmpty()) {
        saleOrder.setDeliveredPartner(clientPartner);
      } else {
        saleOrder.setDeliveredPartner(null);
      }

    } else {
      saleOrder.setInvoicedPartner(clientPartner);
      saleOrder.setDeliveredPartner(clientPartner);
    }
  }

  @Override
  public StockLocation getStockLocation(Partner clientPartner, Company company)
      throws AxelorException {
    if (company == null) {
      return null;
    }
    StockLocation stockLocation =
        partnerStockSettingsService.getDefaultStockLocation(
            clientPartner, company, StockLocation::getUsableOnSaleOrder);
    if (stockLocation == null) {
      StockConfig stockConfig = stockConfigService.getStockConfig(company);
      stockLocation = stockConfigService.getPickupDefaultStockLocation(stockConfig);
    }
    return stockLocation;
  }

  @Override
  public StockLocation getToStockLocation(Partner clientPartner, Company company)
      throws AxelorException {
    if (company == null) {
      return null;
    }
    StockLocation toStockLocation =
        partnerStockSettingsService.getDefaultExternalStockLocation(
            clientPartner, company, StockLocation::getUsableOnSaleOrder);
    if (toStockLocation == null) {
      StockConfig stockConfig = stockConfigService.getStockConfig(company);
      toStockLocation = stockConfigService.getCustomerVirtualStockLocation(stockConfig);
    }
    return toStockLocation;
  }
}
