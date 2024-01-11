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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StockMoveCheckWapServiceImpl implements StockMoveCheckWapService {

  protected StockConfigService stockConfigService;
  protected StockLocationLineService stockLocationLineService;
  protected ProductCompanyService productCompanyService;
  protected StockMoveLineService stockMoveLineService;

  @Inject
  public StockMoveCheckWapServiceImpl(
      StockConfigService stockConfigService,
      StockLocationLineService stockLocationLineService,
      ProductCompanyService productCompanyService,
      StockMoveLineService stockMoveLineService) {
    this.stockConfigService = stockConfigService;
    this.stockLocationLineService = stockLocationLineService;
    this.productCompanyService = productCompanyService;
    this.stockMoveLineService = stockMoveLineService;
  }

  @Override
  public String checkWap(StockMove stockMove) throws AxelorException {
    List<String> productsWithErrors = new ArrayList<>();

    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {

      Product product = stockMoveLine.getProduct();

      if (product != null
          && product.getProductTypeSelect().equals(ProductRepository.PRODUCT_TYPE_STORABLE)) {

        StockLocation toStockLocation = stockMove.getToStockLocation();
        StockLocationLine stockLocationLine =
            stockLocationLineService.getStockLocationLine(stockMove.getToStockLocation(), product);
        if (!product.getStockManaged()
            || toStockLocation.getTypeSelect() == StockLocationRepository.TYPE_VIRTUAL
            || stockLocationLine == null
            || stockLocationLine.getAvgPrice().compareTo(BigDecimal.ZERO) == 0) {
          continue;
        }

        if (checkPercentToleranceForWapChange(stockLocationLine, stockMoveLine)) {
          productsWithErrors.add(
              (String) productCompanyService.get(product, "name", stockMove.getCompany()));
        }
      }
    }
    return productsWithErrors.stream().collect(Collectors.joining(", "));
  }

  protected boolean checkPercentToleranceForWapChange(
      StockLocationLine stockLocationLine, StockMoveLine stockMoveLine) throws AxelorException {

    Integer percentToleranceForWapChange =
        stockConfigService
            .getStockConfig(stockLocationLine.getStockLocation().getCompany())
            .getPercentToleranceForWapChange();

    BigDecimal newAvgPrice =
        stockMoveLineService.computeNewAveragePriceLocationLine(stockLocationLine, stockMoveLine);
    BigDecimal oldAvgPrice = stockLocationLine.getAvgPrice();

    BigDecimal percentWapChange =
        (newAvgPrice.subtract(oldAvgPrice))
            .divide(oldAvgPrice, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP)
            .multiply(new BigDecimal(100))
            .abs();

    return percentToleranceForWapChange != null
        && percentWapChange.compareTo(new BigDecimal(percentToleranceForWapChange)) > 0;
  }
}
