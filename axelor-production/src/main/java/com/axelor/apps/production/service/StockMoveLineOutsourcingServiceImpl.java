/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class StockMoveLineOutsourcingServiceImpl implements StockMoveLineOutsourcingService {

  protected final StockMoveLineRepository stockMoveLineRepository;

  @Inject
  public StockMoveLineOutsourcingServiceImpl(StockMoveLineRepository stockMoveLineRepository) {
    this.stockMoveLineRepository = stockMoveLineRepository;
  }

  @Override
  public void checkServiceOutsourcingRealQty(StockMoveLine stockMoveLine) throws AxelorException {
    if (stockMoveLine.getId() == null) {
      return;
    }

    StockMoveLine dbLine = stockMoveLineRepository.find(stockMoveLine.getId());
    if (dbLine == null || dbLine.getPurchaseOrderLine() == null) {
      return;
    }

    PurchaseOrder purchaseOrder = dbLine.getPurchaseOrderLine().getPurchaseOrder();
    if (purchaseOrder == null || !Boolean.TRUE.equals(purchaseOrder.getOutsourcingOrder())) {
      return;
    }

    Product product = dbLine.getProduct();
    if (product == null
        || !ProductRepository.PRODUCT_TYPE_SERVICE.equals(product.getProductTypeSelect())) {
      return;
    }

    BigDecimal realQty =
        stockMoveLine.getRealQty() != null ? stockMoveLine.getRealQty() : BigDecimal.ZERO;
    if (realQty.compareTo(dbLine.getQty()) != 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(
              ProductionExceptionMessage.OUTSOURCING_SERVICE_PRODUCT_REAL_QTY_CHANGE_NOT_ALLOWED),
          product.getName());
    }
  }
}
