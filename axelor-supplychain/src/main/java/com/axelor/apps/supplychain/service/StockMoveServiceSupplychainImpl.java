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
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockMoveServiceSupplychainImpl extends StockMoveServiceImpl {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private AppSupplychainService appSupplyChainService;

  @Inject protected PurchaseOrderRepository purchaseOrderRepo;

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
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public String realize(StockMove stockMove, boolean check) throws AxelorException {
    LOG.debug(
        "Réalisation du mouvement de stock : {} ", new Object[] {stockMove.getStockMoveSeq()});
    String newStockSeq = super.realize(stockMove, check);
    AppSupplychain appSupplychain = appSupplyChainService.getAppSupplychain();
    if (stockMove.getSaleOrder() != null) {
      updateSaleOrderLines(stockMove, true);
      // Update linked saleOrder delivery state depending on BackOrder's existence
      SaleOrder saleOrder = stockMove.getSaleOrder();
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
    } else if (stockMove.getPurchaseOrder() != null) {
      // Update linked purchaseOrder receipt state depending on BackOrder's existence
      PurchaseOrder purchaseOrder = stockMove.getPurchaseOrder();
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
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void cancel(StockMove stockMove) throws AxelorException {
    if (stockMove.getStatusSelect() == StockMoveRepository.STATUS_REALIZED) {
      if (stockMove.getSaleOrder() != null) {
        updateSaleOrderOnCancel(stockMove);
      }
      if (stockMove.getPurchaseOrder() != null) {
        PurchaseOrder purchaseOrder = purchaseOrderRepo.find(stockMove.getPurchaseOrder().getId());
        purchaseOrderServiceSupplychain.updatePurchaseOrderOnCancel(stockMove, purchaseOrder);
      }
    }
    super.cancel(stockMove);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void updateSaleOrderOnCancel(StockMove stockMove) {
    SaleOrder so = Beans.get(SaleOrderRepository.class).find(stockMove.getSaleOrder().getId());

    List<StockMove> stockMoveList = stockMoveRepo.all().filter("self.saleOrder = ?1", so).fetch();
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
}
