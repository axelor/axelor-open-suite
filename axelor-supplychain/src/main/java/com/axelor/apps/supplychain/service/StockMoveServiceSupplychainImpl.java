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
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.IPurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.PartnerProductQualityRatingService;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveServiceImpl;
import com.axelor.apps.stock.service.StockMoveToolService;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javax.persistence.Query;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockMoveServiceSupplychainImpl extends StockMoveServiceImpl
    implements StockMoveServiceSupplychain {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppSupplychainService appSupplyChainService;
  protected PurchaseOrderRepository purchaseOrderRepo;
  protected SaleOrderRepository saleOrderRepo;
  protected UnitConversionService unitConversionService;
  protected ReservedQtyService reservedQtyService;

  @Inject private StockMoveLineServiceSupplychain stockMoveLineServiceSupplychain;

  @Inject
  public StockMoveServiceSupplychainImpl(
      StockMoveLineService stockMoveLineService,
      StockMoveToolService stockMoveToolService,
      StockMoveLineRepository stockMoveLineRepository,
      AppBaseService appBaseService,
      StockMoveRepository stockMoveRepository,
      PartnerProductQualityRatingService partnerProductQualityRatingService,
      AppSupplychainService appSupplyChainService,
      PurchaseOrderRepository purchaseOrderRepo,
      SaleOrderRepository saleOrderRepo,
      UnitConversionService unitConversionService,
      ReservedQtyService reservedQtyService,
      ProductRepository productRepository) {
    super(
        stockMoveLineService,
        stockMoveToolService,
        stockMoveLineRepository,
        appBaseService,
        stockMoveRepository,
        partnerProductQualityRatingService,
        productRepository);
    this.appSupplyChainService = appSupplyChainService;
    this.purchaseOrderRepo = purchaseOrderRepo;
    this.saleOrderRepo = saleOrderRepo;
    this.unitConversionService = unitConversionService;
    this.reservedQtyService = reservedQtyService;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public String realize(StockMove stockMove, boolean check) throws AxelorException {
    LOG.debug("Réalisation du mouvement de stock : {} ", stockMove.getStockMoveSeq());
    String newStockSeq = super.realize(stockMove, check);
    AppSupplychain appSupplychain = appSupplyChainService.getAppSupplychain();

    if (StockMoveRepository.ORIGIN_SALE_ORDER.equals(stockMove.getOriginTypeSelect())) {
      updateSaleOrderLinesDeliveryState(stockMove, !stockMove.getIsReversion());
      // Update linked saleOrder delivery state depending on BackOrder's existence
      SaleOrder saleOrder = saleOrderRepo.find(stockMove.getOriginId());
      if (newStockSeq != null) {
        saleOrder.setDeliveryState(SaleOrderRepository.DELIVERY_STATE_PARTIALLY_DELIVERED);
      } else {
        Beans.get(SaleOrderStockService.class).updateDeliveryState(saleOrder);

        if (appSupplychain.getTerminateSaleOrderOnDelivery()) {
          terminateOrConfirmSaleOrderStatus(saleOrder);
        }
      }

      Beans.get(SaleOrderRepository.class).save(saleOrder);
    } else if (StockMoveRepository.ORIGIN_PURCHASE_ORDER.equals(stockMove.getOriginTypeSelect())) {
      updatePurchaseOrderLines(stockMove, !stockMove.getIsReversion());
      // Update linked purchaseOrder receipt state depending on BackOrder's existence
      PurchaseOrder purchaseOrder = purchaseOrderRepo.find(stockMove.getOriginId());
      if (newStockSeq != null) {
        purchaseOrder.setReceiptState(IPurchaseOrder.STATE_PARTIALLY_RECEIVED);
      } else {
        Beans.get(PurchaseOrderStockService.class).updateReceiptState(purchaseOrder);

        if (appSupplychain.getTerminatePurchaseOrderOnReceipt()) {
          finishOrValidatePurchaseOrderStatus(purchaseOrder);
        }
      }

      Beans.get(PurchaseOrderRepository.class).save(purchaseOrder);
    }
    if (appSupplyChainService.getAppSupplychain().getManageStockReservation()) {
      Beans.get(ReservedQtyService.class)
          .updateReservedQuantity(stockMove, StockMoveRepository.STATUS_REALIZED);
    }

    detachNonDeliveredStockMoveLines(stockMove);

    List<Long> trackingNumberIds =
        stockMove
            .getStockMoveLineList()
            .stream()
            .map(StockMoveLine::getTrackingNumber)
            .filter(Objects::nonNull)
            .map(TrackingNumber::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    if (CollectionUtils.isNotEmpty(trackingNumberIds)) {
      Query update =
          JPA.em()
              .createQuery(
                  "UPDATE FixedAsset self SET self.stockLocation = :stockLocation WHERE self.trackingNumber.id IN (:trackingNumber)");
      update.setParameter("stockLocation", stockMove.getToStockLocation());
      update.setParameter("trackingNumber", trackingNumberIds);
      update.executeUpdate();
    }

    return newStockSeq;
  }

  @Override
  public void detachNonDeliveredStockMoveLines(StockMove stockMove) {
    if (stockMove.getStockMoveLineList() == null) {
      return;
    }
    stockMove
        .getStockMoveLineList()
        .stream()
        .filter(line -> line.getRealQty().signum() == 0)
        .forEach(line -> line.setSaleOrderLine(null));
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void cancel(StockMove stockMove) throws AxelorException {
    if (stockMove.getStatusSelect() == StockMoveRepository.STATUS_REALIZED) {
      if (StockMoveRepository.ORIGIN_SALE_ORDER.equals(stockMove.getOriginTypeSelect())) {
        updateSaleOrderOnCancel(stockMove);
      }
      if (StockMoveRepository.ORIGIN_PURCHASE_ORDER.equals(stockMove.getOriginTypeSelect())) {
        updatePurchaseOrderOnCancel(stockMove);
      }
    }
    super.cancel(stockMove);
    if (appSupplyChainService.getAppSupplychain().getManageStockReservation()) {
      Beans.get(ReservedQtyService.class)
          .updateReservedQuantity(stockMove, StockMoveRepository.STATUS_CANCELED);
    }
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void plan(StockMove stockMove) throws AxelorException {
    super.plan(stockMove);
    if (appSupplyChainService.getAppSupplychain().getManageStockReservation()) {
      Beans.get(ReservedQtyService.class)
          .updateReservedQuantity(stockMove, StockMoveRepository.STATUS_PLANNED);
    }
  }

  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void updateSaleOrderOnCancel(StockMove stockMove) throws AxelorException {
    SaleOrder so = saleOrderRepo.find(stockMove.getOriginId());

    updateSaleOrderLinesDeliveryState(stockMove, stockMove.getIsReversion());
    Beans.get(SaleOrderStockService.class).updateDeliveryState(so);

    if (Beans.get(AppSupplychainService.class)
        .getAppSupplychain()
        .getTerminateSaleOrderOnDelivery()) {
      terminateOrConfirmSaleOrderStatus(so);
    }
  }

  /**
   * Update saleOrder status from or to terminated status, from or to confirm status, depending on
   * its delivery state. Should be called only if we terminate sale order on receipt.
   *
   * @param saleOrder
   */
  protected void terminateOrConfirmSaleOrderStatus(SaleOrder saleOrder) {
    if (saleOrder.getDeliveryState() == SaleOrderRepository.DELIVERY_STATE_DELIVERED) {
      saleOrder.setStatusSelect(SaleOrderRepository.STATUS_ORDER_COMPLETED);
    } else {
      saleOrder.setStatusSelect(SaleOrderRepository.STATUS_ORDER_CONFIRMED);
    }
  }

  protected void updateSaleOrderLinesDeliveryState(StockMove stockMove, boolean qtyWasDelivered)
      throws AxelorException {
    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
      if (stockMoveLine.getSaleOrderLine() != null) {
        SaleOrderLine saleOrderLine = stockMoveLine.getSaleOrderLine();

        BigDecimal realQty =
            unitConversionService.convert(
                stockMoveLine.getUnit(),
                saleOrderLine.getUnit(),
                stockMoveLine.getRealQty(),
                stockMoveLine.getRealQty().scale(),
                saleOrderLine.getProduct());

        if (stockMove.getTypeSelect() != StockMoveRepository.TYPE_INTERNAL) {
          if (qtyWasDelivered) {
            saleOrderLine.setDeliveredQty(saleOrderLine.getDeliveredQty().add(realQty));
          } else {
            saleOrderLine.setDeliveredQty(saleOrderLine.getDeliveredQty().subtract(realQty));
          }
        }
        if (saleOrderLine.getDeliveredQty().signum() == 0) {
          saleOrderLine.setDeliveryState(SaleOrderLineRepository.DELIVERY_STATE_NOT_DELIVERED);
        } else if (saleOrderLine.getDeliveredQty().compareTo(saleOrderLine.getQty()) < 0) {
          saleOrderLine.setDeliveryState(
              SaleOrderLineRepository.DELIVERY_STATE_PARTIALLY_DELIVERED);
        } else {
          saleOrderLine.setDeliveryState(SaleOrderLineRepository.DELIVERY_STATE_DELIVERED);
        }
      }
    }
  }

  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void updatePurchaseOrderOnCancel(StockMove stockMove) throws AxelorException {
    PurchaseOrder po = purchaseOrderRepo.find(stockMove.getOriginId());

    updatePurchaseOrderLines(stockMove, stockMove.getIsReversion());
    Beans.get(PurchaseOrderStockService.class).updateReceiptState(po);
    if (Beans.get(AppSupplychainService.class)
        .getAppSupplychain()
        .getTerminatePurchaseOrderOnReceipt()) {
      finishOrValidatePurchaseOrderStatus(po);
    }
  }

  /**
   * Update purchaseOrder status from or to finished status, from or to validated status, depending
   * on its state. Should be called only if we terminate purchase order on receipt.
   *
   * @param purchaseOrder a purchase order.
   */
  protected void finishOrValidatePurchaseOrderStatus(PurchaseOrder purchaseOrder) {

    if (purchaseOrder.getReceiptState() == IPurchaseOrder.STATE_RECEIVED) {
      purchaseOrder.setStatusSelect(IPurchaseOrder.STATUS_FINISHED);
    } else {
      purchaseOrder.setStatusSelect(IPurchaseOrder.STATUS_VALIDATED);
    }
  }

  protected void updatePurchaseOrderLines(StockMove stockMove, boolean qtyWasReceived)
      throws AxelorException {
    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
      if (stockMoveLine.getPurchaseOrderLine() != null) {
        PurchaseOrderLine purchaseOrderLine = stockMoveLine.getPurchaseOrderLine();

        BigDecimal realQty =
            unitConversionService.convert(
                stockMoveLine.getUnit(),
                purchaseOrderLine.getUnit(),
                stockMoveLine.getRealQty(),
                stockMoveLine.getRealQty().scale(),
                purchaseOrderLine.getProduct());

        if (qtyWasReceived) {
          purchaseOrderLine.setReceivedQty(purchaseOrderLine.getReceivedQty().add(realQty));
        } else {
          purchaseOrderLine.setReceivedQty(purchaseOrderLine.getReceivedQty().subtract(realQty));
        }
        if (purchaseOrderLine.getReceivedQty().signum() == 0) {
          purchaseOrderLine.setReceiptState(IPurchaseOrder.STATE_NOT_RECEIVED);
        } else if (purchaseOrderLine.getReceivedQty().compareTo(purchaseOrderLine.getQty()) < 0) {
          purchaseOrderLine.setReceiptState(IPurchaseOrder.STATE_PARTIALLY_RECEIVED);
        } else {
          purchaseOrderLine.setReceiptState(IPurchaseOrder.STATE_RECEIVED);
        }
      }
    }
  }

  @Override
  public List<StockMoveLine> addSubLines(List<StockMoveLine> moveLines) {

    if (moveLines == null) {
      return moveLines;
    }

    List<StockMoveLine> lines = new ArrayList<StockMoveLine>();
    lines.addAll(moveLines);
    for (StockMoveLine line : lines) {
      if (line.getSubLineList() == null) {
        continue;
      }
      for (StockMoveLine subLine : line.getSubLineList()) {
        if (subLine.getStockMove() == null) {
          moveLines.add(subLine);
        }
      }
    }
    return moveLines;
  }

  @Override
  public List<StockMoveLine> removeSubLines(List<StockMoveLine> moveLines) {

    if (moveLines == null) {
      return moveLines;
    }

    List<StockMoveLine> subLines = new ArrayList<StockMoveLine>();
    for (StockMoveLine packLine : moveLines) {
      if (packLine != null
          && packLine.getLineTypeSelect() != null
          && packLine.getLineTypeSelect() == 2
          && packLine.getSubLineList() != null) {
        packLine.getSubLineList().removeIf(it -> it.getId() != null && !moveLines.contains(it));
        subLines.addAll(packLine.getSubLineList());
      }
    }
    Iterator<StockMoveLine> lines = moveLines.iterator();

    while (lines.hasNext()) {
      StockMoveLine subLine = lines.next();
      if (subLine.getId() != null
          && subLine.getParentLine() != null
          && !subLines.contains(subLine)) {
        lines.remove();
      }
    }

    return moveLines;
  }

  /**
   * The splitted stock move line needs an allocation and will be planned before the previous stock
   * move line is realized. To solve this issue, we deallocate here in the previous stock move line
   * the quantity that will be allocated in the generated stock move line. The quantity will be
   * reallocated when the generated stock move is planned.
   *
   * @param stockMoveLine the previous stock move line
   * @return the generated stock move line
   * @throws AxelorException
   */
  @Override
  protected StockMoveLine copySplittedStockMoveLine(StockMoveLine stockMoveLine)
      throws AxelorException {
    StockMoveLine newStockMoveLine = super.copySplittedStockMoveLine(stockMoveLine);

    if (appSupplyChainService.getAppSupplychain().getManageStockReservation()) {
      BigDecimal requestedReservedQty =
          stockMoveLine
              .getRequestedReservedQty()
              .subtract(stockMoveLine.getRealQty())
              .max(BigDecimal.ZERO);

      newStockMoveLine.setRequestedReservedQty(requestedReservedQty);
      newStockMoveLine.setReservedQty(BigDecimal.ZERO);

      reservedQtyService.deallocateStockMoveLineAfterSplit(
          stockMoveLine, stockMoveLine.getReservedQty());
      stockMoveLine.setReservedQty(BigDecimal.ZERO);
    }
    return newStockMoveLine;
  }

  @Override
  public void verifyProductStock(StockMove stockMove) throws AxelorException {
    AppSupplychain appSupplychain = appSupplyChainService.getAppSupplychain();
    if (stockMove.getAvailabilityRequest()
        && stockMove.getStockMoveLineList() != null
        && appSupplychain.getIsVerifyProductStock()
        && stockMove.getFromStockLocation() != null) {
      StringJoiner notAvailableProducts = new StringJoiner(",");
      int counter = 1;
      for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
        boolean isAvailableProduct =
            stockMoveLineServiceSupplychain.isAvailableProduct(stockMove, stockMoveLine);
        if (!isAvailableProduct && counter <= 10) {
          notAvailableProducts.add(stockMoveLine.getProduct().getFullName());
          counter++;
        }
      }
      if (!Strings.isNullOrEmpty(notAvailableProducts.toString())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            String.format(
                I18n.get(IExceptionMessage.STOCK_MOVE_VERIFY_PRODUCT_STOCK_ERROR),
                notAvailableProducts.toString()));
      }
    }
  }
}
