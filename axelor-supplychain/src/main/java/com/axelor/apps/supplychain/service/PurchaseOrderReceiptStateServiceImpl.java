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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class PurchaseOrderReceiptStateServiceImpl implements PurchaseOrderReceiptStateService {

  protected final UnitConversionService unitConversionService;
  protected final StockMoveRepository stockMoveRepository;
  protected final StockMoveLineRepository stockMoveLineRepository;
  protected final PurchaseOrderStockService purchaseOrderStockService;

  @Inject
  public PurchaseOrderReceiptStateServiceImpl(
      UnitConversionService unitConversionService,
      StockMoveRepository stockMoveRepository,
      StockMoveLineRepository stockMoveLineRepository,
      PurchaseOrderStockService purchaseOrderStockService) {
    this.unitConversionService = unitConversionService;
    this.stockMoveRepository = stockMoveRepository;
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.purchaseOrderStockService = purchaseOrderStockService;
  }

  @Override
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

      if (purchaseOrderStockService.isStockMoveProduct(purchaseOrderLine, purchaseOrder)) {

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
  public void updatePurchaseOrderLineReceiptState(PurchaseOrder purchaseOrder)
      throws AxelorException {
    stockMoveRepository.findAllByPurchaseOrderAndStatus(
        purchaseOrder, StockMoveRepository.STATUS_PLANNED);

    for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
      List<StockMoveLine> stockMoveLineList =
          stockMoveLineRepository
              .all()
              .filter(
                  "self.stockMove.statusSelect = :realizedStatus AND self.purchaseOrderLine = :purchaseOrderLine")
              .bind("realizedStatus", StockMoveRepository.STATUS_REALIZED)
              .bind("purchaseOrderLine", purchaseOrderLine)
              .fetch();
      BigDecimal receivedQty = BigDecimal.ZERO;
      for (StockMoveLine stockMoveLine : stockMoveLineList) {
        StockMove stockMove = stockMoveLine.getStockMove();
        BigDecimal realQty =
            unitConversionService.convert(
                stockMoveLine.getUnit(),
                purchaseOrderLine.getUnit(),
                stockMoveLine.getRealQty(),
                stockMoveLine.getRealQty().scale(),
                purchaseOrderLine.getProduct());
        if (!stockMove.getIsReversion()) {
          receivedQty = receivedQty.add(realQty);
        } else {
          receivedQty = receivedQty.subtract(realQty);
        }
      }
      purchaseOrderLine.setReceivedQty(receivedQty);
      computePurchaseOrderLineReceiptState(purchaseOrderLine);
    }
  }

  protected void computePurchaseOrderLineReceiptState(PurchaseOrderLine purchaseOrderLine) {
    if (purchaseOrderLine.getReceivedQty().signum() == 0) {
      purchaseOrderLine.setReceiptState(PurchaseOrderRepository.STATE_NOT_RECEIVED);
    } else if (purchaseOrderLine.getReceivedQty().compareTo(purchaseOrderLine.getQty()) < 0) {
      purchaseOrderLine.setReceiptState(PurchaseOrderRepository.STATE_PARTIALLY_RECEIVED);
    } else {
      purchaseOrderLine.setReceiptState(PurchaseOrderRepository.STATE_RECEIVED);
    }
  }
}
