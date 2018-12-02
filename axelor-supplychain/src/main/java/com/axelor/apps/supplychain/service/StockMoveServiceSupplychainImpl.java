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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.AppSupplychain;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.IPurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderServiceImpl;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderWorkflowServiceImpl;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.PartnerProductQualityRatingService;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveServiceImpl;
import com.axelor.apps.stock.service.StockMoveToolService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockMoveServiceSupplychainImpl extends StockMoveServiceImpl
    implements StockMoveServiceSupplychain {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppSupplychainService appSupplyChainService;
  protected PurchaseOrderRepository purchaseOrderRepo;
  protected SaleOrderRepository saleOrderRepo;
  protected PurchaseOrderServiceSupplychainImpl purchaseOrderServiceSupplychain;
  protected UnitConversionService unitConversionService;
  protected ReservedQtyService reservedQtyService;

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
      PurchaseOrderServiceSupplychainImpl purchaseOrderServiceSupplychain,
      UnitConversionService unitConversionService,
      ReservedQtyService reservedQtyService) {
    super(
        stockMoveLineService,
        stockMoveToolService,
        stockMoveLineRepository,
        appBaseService,
        stockMoveRepository,
        partnerProductQualityRatingService);
    this.appSupplyChainService = appSupplyChainService;
    this.purchaseOrderRepo = purchaseOrderRepo;
    this.saleOrderRepo = saleOrderRepo;
    this.purchaseOrderServiceSupplychain = purchaseOrderServiceSupplychain;
    this.unitConversionService = unitConversionService;
    this.reservedQtyService = reservedQtyService;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public String realize(StockMove stockMove, boolean check) throws AxelorException {
    LOG.debug(
        "Réalisation du mouvement de stock : {} ", new Object[] {stockMove.getStockMoveSeq()});
    String newStockSeq = super.realize(stockMove, check);
    AppSupplychain appSupplychain = appSupplyChainService.getAppSupplychain();

    if (StockMoveRepository.ORIGIN_SALE_ORDER.equals(stockMove.getOriginTypeSelect())) {
      updateSaleOrderLinesDeliveryState(stockMove, true);
      // Update linked saleOrder delivery state depending on BackOrder's existence
      SaleOrder saleOrder = saleOrderRepo.find(stockMove.getOriginId());
      if (newStockSeq != null) {
        saleOrder.setDeliveryState(SaleOrderRepository.DELIVERY_STATE_PARTIALLY_DELIVERED);
      } else {
        Beans.get(SaleOrderStockService.class).updateDeliveryState(saleOrder);

        if (saleOrder.getDeliveryState() == SaleOrderRepository.DELIVERY_STATE_DELIVERED
            && appSupplychain.getTerminateSaleOrderOnDelivery()) {
          Beans.get(SaleOrderWorkflowServiceImpl.class).completeSaleOrder(saleOrder);
        }
      }

      Beans.get(SaleOrderRepository.class).save(saleOrder);
    } else if (StockMoveRepository.ORIGIN_PURCHASE_ORDER.equals(stockMove.getOriginTypeSelect())) {
      updatePurchaseOrderLines(stockMove, true);
      // Update linked purchaseOrder receipt state depending on BackOrder's existence
      PurchaseOrder purchaseOrder = purchaseOrderRepo.find(stockMove.getOriginId());
      if (newStockSeq != null) {
        purchaseOrder.setReceiptState(IPurchaseOrder.STATE_PARTIALLY_RECEIVED);
      } else {
        purchaseOrderServiceSupplychain.updateReceiptState(purchaseOrder);

        if (purchaseOrder.getReceiptState() == IPurchaseOrder.STATE_RECEIVED
            && appSupplychain.getTerminatePurchaseOrderOnReceipt()) {
          Beans.get(PurchaseOrderServiceImpl.class).finishPurchaseOrder(purchaseOrder);
        }
      }

      Beans.get(PurchaseOrderRepository.class).save(purchaseOrder);
    }
    if (appSupplyChainService.getAppSupplychain().getManageStockReservation()) {
      Beans.get(ReservedQtyService.class)
          .updateReservedQuantity(stockMove, StockMoveRepository.STATUS_REALIZED);
    }

    return newStockSeq;
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

    List<StockMove> stockMoveList =
        stockMoveRepo
            .all()
            .filter(
                "self.originId = ?1 AND self.originTypeSelect = ?2",
                so.getId(),
                StockMoveRepository.ORIGIN_SALE_ORDER)
            .fetch();
    so.setDeliveryState(SaleOrderRepository.DELIVERY_STATE_NOT_DELIVERED);
    for (StockMove stock : stockMoveList) {
      if (stock.getStatusSelect() != StockMoveRepository.STATUS_CANCELED
          && !stock.getId().equals(stockMove.getId())) {
        so.setDeliveryState(SaleOrderRepository.DELIVERY_STATE_PARTIALLY_DELIVERED);
        break;
      }
    }

    if (so.getStatusSelect() == SaleOrderRepository.STATUS_ORDER_COMPLETED
        && Beans.get(AppSupplychainService.class)
            .getAppSupplychain()
            .getTerminateSaleOrderOnDelivery()) {
      so.setStatusSelect(SaleOrderRepository.STATUS_ORDER_CONFIRMED);
    }
    updateSaleOrderLinesDeliveryState(stockMove, false);
  }

  protected void updateSaleOrderLinesDeliveryState(StockMove stockMove, boolean realize)
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

        if (realize) {
          saleOrderLine.setDeliveredQty(saleOrderLine.getDeliveredQty().add(realQty));
        } else {
          saleOrderLine.setDeliveredQty(saleOrderLine.getDeliveredQty().subtract(realQty));
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

    List<StockMove> stockMoveList =
        stockMoveRepo
            .all()
            .filter(
                "self.originId = ?1 AND self.originTypeSelect = ?2",
                po.getId(),
                StockMoveRepository.ORIGIN_PURCHASE_ORDER)
            .fetch();
    po.setReceiptState(IPurchaseOrder.STATE_NOT_RECEIVED);
    for (StockMove stock : stockMoveList) {
      if (stock.getStatusSelect() != StockMoveRepository.STATUS_CANCELED
          && !stock.getId().equals(stockMove.getId())) {
        po.setReceiptState(IPurchaseOrder.STATE_PARTIALLY_RECEIVED);
        break;
      }
    }

    if (po.getStatusSelect() == IPurchaseOrder.STATUS_FINISHED
        && Beans.get(AppSupplychainService.class)
            .getAppSupplychain()
            .getTerminatePurchaseOrderOnReceipt()) {
      po.setStatusSelect(IPurchaseOrder.STATUS_VALIDATED);
    }
    updatePurchaseOrderLines(stockMove, false);
  }

  protected void updatePurchaseOrderLines(StockMove stockMove, boolean realize)
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

        if (realize) {
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

      reservedQtyService.desallocateStockMoveLineAfterSplit(
          stockMoveLine, stockMoveLine.getReservedQty());
      stockMoveLine.setReservedQty(BigDecimal.ZERO);
    }
    return newStockMoveLine;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void updateReservedQty(StockMove stockMove) throws AxelorException {
    cancel(stockMove);
    plan(stockMove);
  }

  @Override
  public boolean hasReservedQtyChanged(StockMove oldStockMove, StockMove newStockMove)
      throws AxelorException {
    List<StockMoveLine> oldStockMoveLineList =
        MoreObjects.firstNonNull(oldStockMove.getStockMoveLineList(), new ArrayList<>());
    List<StockMoveLine> newStockMoveLineList =
        MoreObjects.firstNonNull(newStockMove.getStockMoveLineList(), new ArrayList<>());

    for (StockMoveLine oldStockMoveLine : oldStockMoveLineList) {
      Optional<StockMoveLine> newStockMoveLine =
          newStockMoveLineList
              .stream()
              .filter(stockMoveLine -> stockMoveLine.getId().equals(oldStockMoveLine.getId()))
              .findAny();
      if (newStockMoveLine.isPresent()) {
        if (newStockMoveLine.get().getReservedQty().compareTo(oldStockMoveLine.getReservedQty())
            != 0) {
          return true;
        }
      } else if (oldStockMoveLine.getReservedQty().signum() != 0) {
        return true;
      }
    }
    // get added lines
    List<StockMoveLine> newStockMoveLineListFiltered =
        newStockMoveLineList
            .stream()
            .filter(stockMoveLine -> !oldStockMoveLineList.contains(stockMoveLine))
            .collect(Collectors.toList());
    for (StockMoveLine filteredNewStockMoveLine : newStockMoveLineListFiltered) {
      if (filteredNewStockMoveLine.getReservedQty().signum() != 0) {
        return true;
      }
    }

    return false;
  }
}
