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

import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.StockMoveMergingServiceImpl;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.StockMoveToolService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

public class StockMoveMergingServiceSupplychainImpl extends StockMoveMergingServiceImpl {

  @Inject
  public StockMoveMergingServiceSupplychainImpl(
      StockMoveRepository stockMoveRepository,
      AppBaseService appBaseService,
      StockMoveService stockMoveService,
      StockMoveToolService stockMoveToolService,
      PartnerService partnerService,
      StockMoveLineRepository stockMoveLineRepository) {
    super(
        stockMoveRepository,
        appBaseService,
        stockMoveService,
        stockMoveToolService,
        partnerService,
        stockMoveLineRepository);
  }

  @Override
  protected void checkErrors(List<StockMove> stockMoveList, StringJoiner errors) {
    super.checkErrors(stockMoveList, errors);
    if (!checkAllSame(stockMoveList, StockMove::getSaleOrder)) {
      errors.add(I18n.get(StockExceptionMessage.STOCK_MOVE_MERGE_ERROR_SALE_ORDER));
    }
    if (!checkAllSame(stockMoveList, StockMove::getPurchaseOrder)) {
      errors.add(I18n.get(StockExceptionMessage.STOCK_MOVE_MERGE_ERROR_PURCHASE_ORDER));
    }
  }

  @Override
  protected List<Function<StockMove, Object>> getShipmentFieldsToCheck() {
    List<Function<StockMove, Object>> shipmentFieldsToCheck = super.getShipmentFieldsToCheck();
    shipmentFieldsToCheck.add(StockMove::getDeliveryCondition);
    return shipmentFieldsToCheck;
  }

  @Override
  protected void fillStockMoveFields(
      List<StockMove> stockMoveList, StockMove stockMove, StockMove mergedStockMove) {
    super.fillStockMoveFields(stockMoveList, stockMove, mergedStockMove);
    mergedStockMove.setDeliveryCondition(stockMove.getDeliveryCondition());
    mergedStockMove.setSaleOrder(stockMove.getSaleOrder());
    mergedStockMove.setPurchaseOrder(stockMove.getPurchaseOrder());
  }
}
