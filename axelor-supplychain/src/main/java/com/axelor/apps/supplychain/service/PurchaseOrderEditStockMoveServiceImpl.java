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
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.studio.db.AppSupplychain;
import jakarta.inject.Inject;
import java.util.List;

public class PurchaseOrderEditStockMoveServiceImpl implements PurchaseOrderEditStockMoveService {

  protected final StockMoveRepository stockMoveRepository;
  protected final TrackingNumberSupplychainService trackingNumberSupplychainService;
  protected final AppSupplychainService appSupplychainService;
  protected final StockMoveService stockMoveService;

  @Inject
  public PurchaseOrderEditStockMoveServiceImpl(
      StockMoveRepository stockMoveRepository,
      TrackingNumberSupplychainService trackingNumberSupplychainService,
      AppSupplychainService appSupplychainService,
      StockMoveService stockMoveService) {
    this.stockMoveRepository = stockMoveRepository;
    this.trackingNumberSupplychainService = trackingNumberSupplychainService;
    this.appSupplychainService = appSupplychainService;
    this.stockMoveService = stockMoveService;
  }

  @Override
  public void cancelStockMoves(PurchaseOrder purchaseOrder) throws AxelorException {
    AppSupplychain appSupplychain = appSupplychainService.getAppSupplychain();

    List<StockMove> allStockMoves =
        stockMoveRepository
            .findAllByPurchaseOrderAndStatus(purchaseOrder, StockMoveRepository.STATUS_PLANNED)
            .fetch();

    purchaseOrder
        .getPurchaseOrderLineList()
        .forEach(trackingNumberSupplychainService::freeOriginPurchaseOrderLine);

    if (!allStockMoves.isEmpty()) {
      CancelReason cancelReason = appSupplychain.getCancelReasonOnChangingPurchaseOrder();
      if (cancelReason == null) {
        throw new AxelorException(
            appSupplychain,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            SupplychainExceptionMessage
                .SUPPLYCHAIN_MISSING_CANCEL_REASON_ON_CHANGING_PURCHASE_ORDER);
      }
      for (StockMove stockMove : allStockMoves) {
        stockMoveService.cancel(stockMove, cancelReason);
        stockMove.setArchived(true);
        for (StockMoveLine stockMoveline : stockMove.getStockMoveLineList()) {
          TrackingNumber trackingNumber = stockMoveline.getTrackingNumber();
          if (trackingNumber != null) {
            trackingNumber.setOriginSaleOrderLine(null);
          }
          stockMoveline.setPurchaseOrderLine(null);
          stockMoveline.setArchived(true);
        }
      }
    }
  }
}
