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
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.IPurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrder;
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
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
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

  @Inject private AppSupplychainService appSupplyChainService;

  @Inject protected PurchaseOrderRepository purchaseOrderRepo;
  @Inject protected SaleOrderRepository saleOrderRepo;

  @Inject private PurchaseOrderServiceSupplychainImpl purchaseOrderServiceSupplychain;

  @Inject
  public StockMoveServiceSupplychainImpl(
      StockMoveLineService stockMoveLineService,
      SequenceService sequenceService,
      StockMoveLineRepository stockMoveLineRepository,
      AppBaseService appBaseService,
      StockMoveRepository stockMoveRepository,
      PartnerProductQualityRatingService partnerProductQualityRatingService) {
    super(
        stockMoveLineService,
        sequenceService,
        stockMoveLineRepository,
        appBaseService,
        stockMoveRepository,
        partnerProductQualityRatingService);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public String realize(StockMove stockMove, boolean check) throws AxelorException {
    LOG.debug(
        "Réalisation du mouvement de stock : {} ", new Object[] {stockMove.getStockMoveSeq()});
    String newStockSeq = super.realize(stockMove, check);
    AppSupplychain appSupplychain = appSupplyChainService.getAppSupplychain();

    if (StockMoveRepository.ORIGIN_SALE_ORDER.equals(stockMove.getOriginTypeSelect())) {
      updateSaleOrderLines(stockMove, true);
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
      // Update linked purchaseOrder receipt state depending on BackOrder's existence
      PurchaseOrder purchaseOrder = purchaseOrderRepo.find(stockMove.getOriginId());
      if (newStockSeq != null) {
        purchaseOrder.setReceiptState(IPurchaseOrder.STATE_PARTIALLY_RECEIVED);
      } else {
        purchaseOrder.setReceiptState(IPurchaseOrder.STATE_RECEIVED);
        if (appSupplychain.getTerminatePurchaseOrderOnReceipt()) {
          Beans.get(PurchaseOrderServiceImpl.class).finishPurchaseOrder(purchaseOrder);
        }
      }

      Beans.get(PurchaseOrderRepository.class).save(purchaseOrder);
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
        PurchaseOrder purchaseOrder = purchaseOrderRepo.find(stockMove.getOriginId());
        purchaseOrderServiceSupplychain.updatePurchaseOrderOnCancel(stockMove, purchaseOrder);
      }
    }
    super.cancel(stockMove);
  }

  @Transactional
  public void updateSaleOrderOnCancel(StockMove stockMove) {
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
    updateSaleOrderLines(stockMove, false);
  }

  private void updateSaleOrderLines(StockMove stockMove, boolean realize) {
    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
      if (stockMoveLine.getSaleOrderLine() != null) {
        SaleOrderLine saleOrderLine = stockMoveLine.getSaleOrderLine();
        if (realize) {
          saleOrderLine.setDeliveredQty(
              saleOrderLine.getDeliveredQty().add(stockMoveLine.getRealQty()));
        } else {
          saleOrderLine.setDeliveredQty(
              saleOrderLine.getDeliveredQty().subtract(stockMoveLine.getRealQty()));
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
