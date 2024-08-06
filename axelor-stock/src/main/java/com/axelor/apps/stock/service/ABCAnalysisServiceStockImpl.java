/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.service;

import static com.axelor.apps.base.service.administration.AbstractBatch.FETCH_LIMIT;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ABCAnalysis;
import com.axelor.apps.base.db.ABCAnalysisLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ABCAnalysisClassRepository;
import com.axelor.apps.base.db.repo.ABCAnalysisLineRepository;
import com.axelor.apps.base.db.repo.ABCAnalysisRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ABCAnalysisServiceImpl;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ABCAnalysisServiceStockImpl extends ABCAnalysisServiceImpl {

  protected StockLocationService stockLocationService;
  protected StockLocationLineRepository stockLocationLineRepository;

  private static final String STOCK_MANAGED_TRUE = " AND self.stockManaged = TRUE";

  @Inject
  public ABCAnalysisServiceStockImpl(
      ABCAnalysisLineRepository abcAnalysisLineRepository,
      UnitConversionService unitConversionService,
      ABCAnalysisRepository abcAnalysisRepository,
      ProductRepository productRepository,
      StockLocationService stockLocationService,
      StockLocationLineRepository stockLocationLineRepository,
      ABCAnalysisClassRepository abcAnalysisClassRepository,
      SequenceService sequenceService) {
    super(
        abcAnalysisLineRepository,
        unitConversionService,
        abcAnalysisRepository,
        productRepository,
        abcAnalysisClassRepository,
        sequenceService);
    this.stockLocationService = stockLocationService;
    this.stockLocationLineRepository = stockLocationLineRepository;
  }

  @Override
  protected Optional<ABCAnalysisLine> createABCAnalysisLine(
      ABCAnalysis abcAnalysis, Product product) throws AxelorException {
    ABCAnalysisLine abcAnalysisLine = null;
    List<StockLocation> stockLocationList =
        stockLocationService.getAllLocationAndSubLocation(abcAnalysis.getStockLocation(), false);
    BigDecimal productQty = BigDecimal.ZERO;
    BigDecimal productWorth = BigDecimal.ZERO;
    List<StockLocationLine> stockLocationLineList;
    int offset = 0;

    Query<StockLocationLine> stockLocationLineQuery =
        stockLocationLineRepository
            .all()
            .filter(
                "self.stockLocation IN :stockLocationList AND self.product.id = :productId AND self.currentQty != 0 ")
            .bind("stockLocationList", stockLocationList)
            .bind("productId", product.getId());

    while (!(stockLocationLineList = stockLocationLineQuery.fetch(FETCH_LIMIT, offset)).isEmpty()) {
      offset += stockLocationLineList.size();
      abcAnalysis = abcAnalysisRepository.find(abcAnalysis.getId());

      if (abcAnalysisLine == null) {
        abcAnalysisLine = super.createABCAnalysisLine(abcAnalysis, product).get();
      }

      for (StockLocationLine stockLocationLine : stockLocationLineList) {
        BigDecimal convertedQty =
            unitConversionService.convert(
                stockLocationLine.getUnit(),
                product.getUnit(),
                stockLocationLine.getCurrentQty(),
                5,
                product);
        productQty = productQty.add(convertedQty);
        productWorth = productWorth.add(stockLocationLine.getAvgPrice());
      }

      super.incTotalQty(productQty);
      super.incTotalWorth(productWorth);

      JPA.clear();
    }

    if (abcAnalysisLine != null) {
      setQtyWorth(
          abcAnalysisLineRepository.find(abcAnalysisLine.getId()), productQty, productWorth);
    }

    return Optional.ofNullable(abcAnalysisLine);
  }

  @Override
  protected String getProductCategoryQuery() {
    return super.getProductCategoryQuery() + STOCK_MANAGED_TRUE;
  }

  @Override
  protected String getProductFamilyQuery() {
    return super.getProductFamilyQuery() + STOCK_MANAGED_TRUE;
  }
}
