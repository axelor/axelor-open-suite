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
package com.axelor.apps.supplychain.service.batch;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.inject.Beans;

public abstract class BatchStrategy extends AbstractBatch {

  protected SaleOrderInvoiceService saleOrderInvoiceService;

  protected BatchStrategy() {
    super();
  }

  protected BatchStrategy(SaleOrderInvoiceService saleOrderInvoiceService) {
    super();
    this.saleOrderInvoiceService = saleOrderInvoiceService;
  }

  protected void updateSaleOrder(SaleOrder saleOrder) {

    saleOrder.addBatchSetItem(Beans.get(BatchRepository.class).find(batch.getId()));

    incrementDone();
  }

  protected void updateStockMove(StockMove stockMove) {

    stockMove.addBatchSetItem(Beans.get(BatchRepository.class).find(batch.getId()));

    incrementDone();
  }

  protected void updateAccountMove(Move move, boolean incrementDone) {

    move.addBatchSetItem(Beans.get(BatchRepository.class).find(batch.getId()));

    if (incrementDone) {
      incrementDone();
    } else {
      checkPoint();
    }
  }

  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_SUPPLYCHAIN_BATCH);
  }
}
