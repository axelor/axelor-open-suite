/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service.batch;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.stock.db.StockHistoryLine;
import com.axelor.apps.stock.service.StockHistoryService;
import com.axelor.apps.supplychain.db.SupplychainBatch;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.db.repo.ExceptionOriginRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class BatchUpdateStockHistory extends BatchStrategy {

  protected StockHistoryService stockHistoryService;

  @Inject
  public BatchUpdateStockHistory(StockHistoryService stockHistoryService) {
    this.stockHistoryService = stockHistoryService;
  }

  @Override
  protected void process() {
    SupplychainBatch supplychainBatch = batch.getSupplychainBatch();

    List<Product> productList;
    List<StockHistoryLine> stockHistoryLineList = new ArrayList<>();
    Query<Product> productQuery =
        Beans.get(ProductRepository.class)
            .all()
            .filter("self.productTypeSelect = :productTypeSelect")
            .bind("productTypeSelect", ProductRepository.PRODUCT_TYPE_STORABLE);

    int offset = 0;

    while (!(productList = productQuery.order("id").fetch(FETCH_LIMIT, offset)).isEmpty()) {

      for (Product product : productList) {
        ++offset;
        try {
          stockHistoryLineList.addAll(
              stockHistoryService.computeAndSaveStockHistoryLineList(
                  product.getId(),
                  supplychainBatch.getCompany().getId(),
                  null,
                  supplychainBatch.getPeriod().getFromDate(),
                  supplychainBatch.getPeriod().getToDate()));
          incrementDone();
        } catch (Exception e) {
          incrementAnomaly();
          TraceBackService.trace(e, ExceptionOriginRepository.UPDATE_STOCK_HISTORY, batch.getId());
        }
      }
      JPA.clear();
    }
  }

  @Override
  protected void stop() {
    String comment = I18n.get(IExceptionMessage.BATCH_UPDATE_STOCK_HISTORY_1) + " ";
    comment +=
        String.format(
            "\t* %s " + I18n.get(IExceptionMessage.BATCH_UPDATE_STOCK_HISTORY_2) + "\n",
            batch.getDone());
    comment +=
        String.format(
            "\t" + I18n.get(com.axelor.apps.base.exceptions.IExceptionMessage.ALARM_ENGINE_BATCH_4),
            batch.getAnomaly());

    super.stop();
    addComment(comment);
  }
}
