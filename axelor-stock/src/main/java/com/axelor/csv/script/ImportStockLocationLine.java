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
package com.axelor.csv.script;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockLocationLineHistory;
import com.axelor.apps.stock.db.repo.StockLocationLineHistoryRepository;
import com.axelor.common.StringUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class ImportStockLocationLine {

  protected final ProductCompanyService productCompanyService;
  protected final StockLocationLineHistoryRepository stockLocationLineHistoryRepository;

  public static final String STOCK_LOCATION_HEADER = "stockLocation_importId";
  public static final String ORIGIN_STOCK_DATA = "Stock import";

  @Inject
  public ImportStockLocationLine(
      ProductCompanyService productCompanyService,
      StockLocationLineHistoryRepository stockLocationLineHistoryRepository) {
    this.productCompanyService = productCompanyService;
    this.stockLocationLineHistoryRepository = stockLocationLineHistoryRepository;
  }

  public Object importStockLocationLine(Object bean, Map<String, Object> values)
      throws AxelorException {

    assert bean instanceof StockLocationLine;

    StockLocationLine stockLocationLine = (StockLocationLine) bean;
    if (!StringUtils.isEmpty((String) values.get(STOCK_LOCATION_HEADER))) {
      createStockLocationLineHistory(stockLocationLine);
    }

    productCompanyService.set(
        stockLocationLine.getProduct(),
        "avgPrice",
        (BigDecimal) stockLocationLine.getAvgPrice(),
        null);

    return stockLocationLine;
  }

  @Transactional
  protected void createStockLocationLineHistory(StockLocationLine stockLocationLine) {
    StockLocationLineHistory stockLocationLineHistory = new StockLocationLineHistory();
    stockLocationLineHistory.setTypeSelect(
        StockLocationLineHistoryRepository.TYPE_SELECT_STOCK_MOVE);
    stockLocationLineHistory.setOrigin(ORIGIN_STOCK_DATA);
    stockLocationLineHistory.setStockLocationLine(stockLocationLine);
    stockLocationLineHistory.setWap(stockLocationLine.getAvgPrice());
    stockLocationLineHistory.setQty(stockLocationLine.getCurrentQty());
    stockLocationLineHistory.setDateT(LocalDateTime.now());
    stockLocationLineHistory.setUnit(stockLocationLine.getUnit());
    stockLocationLineHistoryRepository.save(stockLocationLineHistory);
  }
}
