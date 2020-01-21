/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.AppSupplychain;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderServiceImpl;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.supplychain.db.Timetable;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleOrderServiceSupplychainImpl extends SaleOrderServiceImpl {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppSupplychain appSupplychain;
  protected SaleOrderStockService saleOrderStockService;

  @Inject
  public SaleOrderServiceSupplychainImpl(
      AppSupplychainService appSupplychainService, SaleOrderStockService saleOrderStockService) {

    this.appSupplychain = appSupplychainService.getAppSupplychain();
    this.saleOrderStockService = saleOrderStockService;
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

  public void updateAmountToBeSpreadOverTheTimetable(SaleOrder saleOrder) {
    List<Timetable> timetableList = saleOrder.getTimetableList();
    BigDecimal totalHT = saleOrder.getExTaxTotal();
    BigDecimal sumTimetableAmount = BigDecimal.ZERO;
    if (timetableList != null) {
      for (Timetable timetable : timetableList) {
        sumTimetableAmount =
            sumTimetableAmount.add(timetable.getAmount().multiply(timetable.getQty()));
      }
    }
    saleOrder.setAmountToBeSpreadOverTheTimetable(totalHT.subtract(sumTimetableAmount));
  }

  @Override
  @Transactional(rollbackOn = {Exception.class, AxelorException.class})
  public boolean enableEditOrder(SaleOrder saleOrder) throws AxelorException {
    boolean checkAvailabiltyRequest = super.enableEditOrder(saleOrder);

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
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
            ? allStockMoves
                .stream()
                .filter(stockMove -> !stockMove.getAvailabilityRequest())
                .collect(Collectors.toList())
            : allStockMoves;
    checkAvailabiltyRequest =
        stockMoves.size() != allStockMoves.size() ? true : checkAvailabiltyRequest;
    if (!stockMoves.isEmpty()) {
      StockMoveService stockMoveService = Beans.get(StockMoveService.class);
      StockMoveRepository stockMoveRepository = Beans.get(StockMoveRepository.class);
      CancelReason cancelReason = appSupplychain.getCancelReasonOnChangingSaleOrder();
      if (cancelReason == null) {
        throw new AxelorException(
            appSupplychain,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            IExceptionMessage.SUPPLYCHAIN_MISSING_CANCEL_REASON_ON_CHANGING_SALE_ORDER);
      }
      for (StockMove stockMove : stockMoves) {
        stockMoveService.cancel(stockMove, cancelReason);
        stockMoveRepository.remove(stockMove);
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

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
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
              I18n.get(IExceptionMessage.SO_CANT_DECREASE_QTY_ON_DELIVERED_LINE),
              saleOrderLine.getFullName());
        }
      } else {
        throw new AxelorException(
            saleOrder,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.SO_CANT_REMOVED_DELIVERED_LINE),
            saleOrderLine.getFullName());
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void validateChanges(SaleOrder saleOrder) throws AxelorException {
    super.validateChanges(saleOrder);

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return;
    }

    saleOrderStockService.fullyUpdateDeliveryState(saleOrder);
    saleOrder.setOrderBeingEdited(false);

    if (appSupplychain.getCustomerStockMoveGenerationAuto()) {
      saleOrderStockService.createStocksMovesFromSaleOrder(saleOrder);
    }
  }
}
