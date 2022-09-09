/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.ProductCategoryRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.ProductCategoryService;
import com.axelor.apps.stock.db.StockHistoryLine;
import com.axelor.apps.stock.service.StockHistoryService;
import com.axelor.apps.supplychain.db.SupplychainBatch;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.ExceptionOriginRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class BatchUpdateStockHistory extends BatchStrategy {

  private static final Integer ITERATIONS = 100;

  protected StockHistoryService stockHistoryService;
  protected ProductCategoryRepository productCategoryRepository;

  @Inject
  public BatchUpdateStockHistory(
      StockHistoryService stockHistoryService,
      ProductCategoryRepository productCategoryRepository) {
    this.stockHistoryService = stockHistoryService;
    this.productCategoryRepository = productCategoryRepository;
  }

  @Override
  protected void process() {
    SupplychainBatch supplychainBatch = batch.getSupplychainBatch();

    try {
      List<Product> productList;
      List<ProductCategory> productCategoryList = getProductCategoryList(supplychainBatch);
      List<StockHistoryLine> stockHistoryLineList = new ArrayList<>();
      Query<Product> productQuery;

      if (supplychainBatch.getProductCategorySet() != null
          && !supplychainBatch.getProductCategorySet().isEmpty()) {
        productQuery =
            Beans.get(ProductRepository.class)
                .all()
                .filter(
                    "self.productCategory in (?1) AND self.productTypeSelect = ?2",
                    productCategoryList,
                    ProductRepository.PRODUCT_TYPE_STORABLE);
      } else {
        productQuery =
            Beans.get(ProductRepository.class)
                .all()
                .filter("self.productTypeSelect = ?1", ProductRepository.PRODUCT_TYPE_STORABLE);
      }

      int offset = 0;

      while (!(productList = productQuery.order("id").fetch(FETCH_LIMIT, offset)).isEmpty()) {

        for (Product product : productList) {
          ++offset;
          try {
            stockHistoryLineList.addAll(
                stockHistoryService.computeStockHistoryLineList(
                    product.getId(),
                    supplychainBatch.getCompany().getId(),
                    null,
                    supplychainBatch.getPeriod().getFromDate(),
                    supplychainBatch.getPeriod().getToDate()));
            incrementDone();
          } catch (Exception e) {
            incrementAnomaly();
            TraceBackService.trace(
                e, ExceptionOriginRepository.UPDATE_STOCK_HISTORY, batch.getId());
          }
        }
        JPA.clear();
      }
    } catch (AxelorException e) {
      TraceBackService.trace(
          new AxelorException(
              e,
              e.getCategory(),
              I18n.get(SupplychainExceptionMessage.BATCH_UPDATE_STOCK_HISTORY_1),
              batch.getId()),
          ExceptionOriginRepository.REIMBURSEMENT,
          batch.getId());
      incrementAnomaly();
      stop();
    }
  }

  @Override
  protected void stop() {
    String comment = I18n.get(SupplychainExceptionMessage.BATCH_UPDATE_STOCK_HISTORY_1) + " ";
    comment +=
        String.format(
            "\t* %s " + I18n.get(SupplychainExceptionMessage.BATCH_UPDATE_STOCK_HISTORY_2) + "\n",
            batch.getDone());
    comment +=
        String.format(
            "\t" + I18n.get(BaseExceptionMessage.ALARM_ENGINE_BATCH_4), batch.getAnomaly());

    super.stop();
    addComment(comment);
  }

  protected List<ProductCategory> getProductCategoryList(SupplychainBatch supplychainBatch)
      throws AxelorException {

    List<ProductCategory> productCategoryList = new ArrayList<>();

    if (supplychainBatch.getProductCategorySet() != null
        && !supplychainBatch.getProductCategorySet().isEmpty()) {

      productCategoryList = new ArrayList<>(supplychainBatch.getProductCategorySet());

      List<ProductCategory> childProductCategoryList = new ArrayList<>();
      ProductCategoryService productCategoryService = Beans.get(ProductCategoryService.class);
      for (ProductCategory productCategory : productCategoryList) {
        childProductCategoryList.addAll(
            productCategoryService.fetchChildrenCategoryList(productCategory));
      }
      productCategoryList.addAll(childProductCategoryList);
    }

    return productCategoryList;
  }
}
