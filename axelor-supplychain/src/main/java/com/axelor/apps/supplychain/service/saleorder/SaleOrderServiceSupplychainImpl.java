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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.address.AddressService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.config.SaleConfigService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMarginService;
import com.axelor.apps.sale.service.saleorder.SaleOrderServiceImpl;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineDiscountService;
import com.axelor.apps.sale.service.saleorderline.creation.SaleOrderLineCreateService;
import com.axelor.apps.sale.service.saleorderline.pack.SaleOrderLinePackService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineComplementaryProductService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.supplychain.db.Timetable;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainService;
import com.axelor.apps.supplychain.service.PartnerLinkSupplychainService;
import com.axelor.apps.supplychain.service.SaleInvoicingStateService;
import com.axelor.apps.supplychain.service.TrackingNumberSupplychainService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.saleorderline.SaleOrderLineAnalyticService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppSupplychain;
import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderServiceSupplychainImpl extends SaleOrderServiceImpl
    implements SaleOrderSupplychainService {

  protected AppSupplychainService appSupplychainService;
  protected SaleOrderStockService saleOrderStockService;
  protected AccountingSituationSupplychainService accountingSituationSupplychainService;
  protected TrackingNumberSupplychainService trackingNumberSupplychainService;

  protected PartnerLinkSupplychainService partnerLinkSupplychainService;
  protected SaleInvoicingStateService saleInvoicingStateService;
  protected SaleOrderLineAnalyticService saleOrderLineAnalyticService;

  @Inject
  public SaleOrderServiceSupplychainImpl(
      AppBaseService appBaseService,
      SaleOrderLineRepository saleOrderLineRepo,
      SaleOrderRepository saleOrderRepo,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderMarginService saleOrderMarginService,
      SaleConfigService saleConfigService,
      SaleOrderLineCreateService saleOrderLineCreateService,
      SaleOrderLineComplementaryProductService saleOrderLineComplementaryProductService,
      SaleOrderLinePackService saleOrderLinePackService,
      SaleOrderLineDiscountService saleOrderLineDiscountService,
      SaleOrderLineComputeService saleOrderLineComputeService,
      AppSupplychainService appSupplychainService,
      SaleOrderStockService saleOrderStockService,
      AccountingSituationSupplychainService accountingSituationSupplychainService,
      TrackingNumberSupplychainService trackingNumberSupplychainService,
      PartnerLinkSupplychainService partnerLinkSupplychainService,
      SaleInvoicingStateService saleInvoicingStateService,
      SaleOrderLineAnalyticService saleOrderLineAnalyticService) {
    super(
        appBaseService,
        saleOrderLineRepo,
        saleOrderRepo,
        saleOrderComputeService,
        saleOrderMarginService,
        saleConfigService,
        saleOrderLineCreateService,
        saleOrderLineComplementaryProductService,
        saleOrderLinePackService,
        saleOrderLineDiscountService,
        saleOrderLineComputeService);
    this.appSupplychainService = appSupplychainService;
    this.saleOrderStockService = saleOrderStockService;
    this.accountingSituationSupplychainService = accountingSituationSupplychainService;
    this.trackingNumberSupplychainService = trackingNumberSupplychainService;
    this.partnerLinkSupplychainService = partnerLinkSupplychainService;
    this.saleInvoicingStateService = saleInvoicingStateService;
    this.saleOrderLineAnalyticService = saleOrderLineAnalyticService;
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
    BigDecimal totalHT =
        saleOrder.getInAti() ? saleOrder.getInTaxTotal() : saleOrder.getExTaxTotal();
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
            .findAllBySaleOrderAndStatus(saleOrder, StockMoveRepository.STATUS_PLANNED)
            .fetch();
    List<StockMove> stockMoves =
        !allStockMoves.isEmpty()
            ? allStockMoves.stream()
                .filter(stockMove -> !stockMove.getAvailabilityRequest())
                .collect(Collectors.toList())
            : allStockMoves;
    checkAvailabiltyRequest =
        stockMoves.size() != allStockMoves.size() ? true : checkAvailabiltyRequest;
    saleOrder
        .getSaleOrderLineList()
        .forEach(trackingNumberSupplychainService::freeOriginSaleOrderLine);
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
          TrackingNumber trackingNumber = stockMoveline.getTrackingNumber();
          if (trackingNumber != null) {
            trackingNumber.setOriginSaleOrderLine(null);
          }
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

    checkTimetable(saleOrderView);
    saleOrderLineAnalyticService.checkAnalyticAxisByCompany(saleOrder);

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

  protected void checkTimetable(SaleOrder saleOrderView) throws AxelorException {
    List<BigDecimal> percentageList =
        saleOrderView.getTimetableList().stream()
            .map(Timetable::getPercentage)
            .collect(Collectors.toList());
    if (percentageList.stream()
        .anyMatch(percentage -> percentage.compareTo(BigDecimal.valueOf(100)) > 0)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.SALE_ORDER_TIMETABLE_PERCENTAGE_ERROR));
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
    saleInvoicingStateService.updateInvoicingState(saleOrder);
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
    accountingSituationSupplychainService.updateUsedCredit(saleOrder.getClientPartner());
  }

  public void setDefaultInvoicedAndDeliveredPartnersAndAddresses(SaleOrder saleOrder) {
    if (saleOrder != null
        && saleOrder.getClientPartner() != null
        && saleOrder.getClientPartner().getId() != null) {
      Partner clientPartner =
          Beans.get(PartnerRepository.class).find(saleOrder.getClientPartner().getId());
      if (clientPartner != null) {
        saleOrder.setInvoicedPartner(
            partnerLinkSupplychainService.getDefaultInvoicedPartner(clientPartner));
        saleOrder.setDeliveredPartner(
            partnerLinkSupplychainService.getDefaultDeliveredPartner(clientPartner));
        setInvoicedAndDeliveredAddresses(saleOrder);
      }
    } else if (saleOrder != null && saleOrder.getClientPartner() == null) {
      saleOrder.setInvoicedPartner(null);
      saleOrder.setDeliveredPartner(null);
      saleOrder.setMainInvoicingAddress(null);
      saleOrder.setMainInvoicingAddressStr(null);
      saleOrder.setDeliveryAddress(null);
      saleOrder.setDeliveryAddressStr(null);
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

  @Override
  public void setAdvancePayment(SaleOrder saleOrder) {
    if (appSupplychainService.getAppSupplychain().getManageAdvancePaymentsFromPaymentConditions()
        && saleOrder.getPaymentCondition() != null
        && saleOrder.getPaymentCondition().getAdvancePaymentNeeded() != null) {
      saleOrder.setAdvancePaymentNeeded(
          saleOrder.getPaymentCondition().getAdvancePaymentNeeded().compareTo(BigDecimal.ZERO) > 0);
      saleOrder.setAdvancePaymentAmountNeeded(
          saleOrder
              .getInTaxTotal()
              .multiply(
                  saleOrder
                      .getPaymentCondition()
                      .getAdvancePaymentNeeded()
                      .divide(BigDecimal.valueOf(100)))
              .setScale(2, RoundingMode.HALF_UP));
    }
  }

  @Override
  public String updateTimetableAmounts(SaleOrder saleOrder) throws AxelorException {
    int invoicingState = saleOrder.getInvoicingState();
    List<Timetable> timetableList = saleOrder.getTimetableList();
    if (CollectionUtils.isNotEmpty(timetableList)) {
      if (invoicingState == SaleOrderRepository.INVOICING_STATE_PARTIALLY_INVOICED
          || invoicingState == SaleOrderRepository.INVOICING_STATE_INVOICED) {
        timetableList.forEach(
            timetable ->
                timetable.setPercentage(
                    timetable
                        .getAmount()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                            saleOrder.getExTaxTotal(),
                            appBaseService.getNbDecimalDigitForUnitPrice(),
                            RoundingMode.HALF_UP)));
        return String.format(
            I18n.get(SupplychainExceptionMessage.SALE_ORDER_TIMETABLE_CAN_NOT_BE_UPDATED),
            saleOrder.getAmountToBeSpreadOverTheTimetable());
      }
      BigDecimal amount =
          saleOrder.getInAti() ? saleOrder.getInTaxTotal() : saleOrder.getExTaxTotal();
      timetableList.forEach(
          timetable ->
              timetable.setAmount(
                  amount
                      .multiply(
                          timetable
                              .getPercentage()
                              .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP))
                      .setScale(
                          appBaseService.getAppBase().getNbDecimalDigitForUnitPrice(),
                          RoundingMode.HALF_UP)));
    }
    return "";
  }

  @Override
  public boolean isIncotermRequired(SaleOrder saleOrder) {
    Partner clientPartner = saleOrder.getClientPartner();
    return saleOrder.getSaleOrderLineList() != null
        && saleOrder.getSaleOrderLineList().stream()
            .anyMatch(
                saleOrderLine ->
                    saleOrderLine.getProduct() != null
                        && saleOrderLine
                            .getProduct()
                            .getProductTypeSelect()
                            .equals(ProductRepository.PRODUCT_TYPE_STORABLE))
        && isSameAlpha2Code(saleOrder)
        && saleOrder.getStatusSelect() == SaleOrderRepository.STATUS_FINALIZED_QUOTATION
            & clientPartner.getPartnerTypeSelect() == PartnerRepository.PARTNER_TYPE_COMPANY;
  }

  protected boolean isSameAlpha2Code(SaleOrder saleOrder) {
    String saleOrderA2C = null;
    String stockLocationA2C = null;
    String companyA2C = null;
    if (saleOrder.getDeliveryAddress() != null
        && saleOrder.getDeliveryAddress().getCountry() != null) {
      saleOrderA2C = saleOrder.getDeliveryAddress().getCountry().getAlpha2Code();
    }
    StockLocation stockLocation = saleOrder.getStockLocation();
    if (stockLocation != null
        && stockLocation.getAddress() != null
        && stockLocation.getAddress().getCountry() != null) {
      stockLocationA2C = stockLocation.getAddress().getCountry().getAlpha2Code();
    }
    if (saleOrder.getCompany() != null
        && saleOrder.getCompany().getAddress() != null
        && saleOrder.getCompany().getAddress().getCountry() != null) {
      companyA2C = saleOrder.getCompany().getAddress().getCountry().getAlpha2Code();
    }
    return stockLocation != null && saleOrderA2C != null && !saleOrderA2C.equals(stockLocationA2C)
        || stockLocation == null && saleOrderA2C != null && !saleOrderA2C.equals(companyA2C);
  }
}
