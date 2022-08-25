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
package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockCorrection;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockCorrectionRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class StockCorrectionServiceImpl implements StockCorrectionService {

  protected StockConfigService stockConfigService;
  protected ProductCompanyService productCompanyService;
  protected StockLocationLineService stockLocationLineService;
  protected AppBaseService baseService;
  protected StockMoveService stockMoveService;
  protected StockMoveLineService stockMoveLineService;

  @Inject
  public StockCorrectionServiceImpl(
      StockConfigService stockConfigService,
      ProductCompanyService productCompanyService,
      StockLocationLineService stockLocationLineService,
      AppBaseService baseService,
      StockMoveService stockMoveService,
      StockMoveLineService stockMoveLineService) {
    this.stockConfigService = stockConfigService;
    this.productCompanyService = productCompanyService;
    this.stockLocationLineService = stockLocationLineService;
    this.baseService = baseService;
    this.stockMoveService = stockMoveService;
    this.stockMoveLineService = stockMoveLineService;
  }

  @Override
  public Map<String, Object> fillDefaultValues(StockLocationLine stockLocationLine) {

    Map<String, Object> stockCorrectionInformation = new HashMap<>();

    stockCorrectionInformation.put(
        "stockLocation",
        stockLocationLine.getStockLocation() != null
            ? stockLocationLine.getStockLocation()
            : stockLocationLine.getDetailsStockLocation());
    stockCorrectionInformation.put("product", stockLocationLine.getProduct());
    stockCorrectionInformation.put("trackingNumber", stockLocationLine.getTrackingNumber());
    getDefaultQtys(stockLocationLine, stockCorrectionInformation);

    return stockCorrectionInformation;
  }

  @Override
  public Map<String, Object> fillDeafultQtys(StockCorrection stockCorrection) {

    Map<String, Object> stockCorrectionQtys = new HashMap<>();

    StockLocationLine stockLocationLine;

    if (stockCorrection.getTrackingNumber() == null) {
      stockLocationLine =
          stockLocationLineService.getStockLocationLine(
              stockCorrection.getStockLocation(), stockCorrection.getProduct());
    } else {
      stockLocationLine =
          stockLocationLineService.getDetailLocationLine(
              stockCorrection.getStockLocation(),
              stockCorrection.getProduct(),
              stockCorrection.getTrackingNumber());
    }

    if (stockLocationLine != null) {
      getDefaultQtys(stockLocationLine, stockCorrectionQtys);
    } else {
      stockCorrectionQtys.put("realQty", BigDecimal.ZERO);
    }
    return stockCorrectionQtys;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public boolean validate(StockCorrection stockCorrection) throws AxelorException {
    if (stockCorrection.getStatusSelect() == null
        || stockCorrection.getStatusSelect() != StockCorrectionRepository.STATUS_DRAFT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.STOCK_CORRECTION_VALIDATE_WRONG_STATUS));
    }

    StockMove stockMove = generateStockMove(stockCorrection);
    if (stockMove != null) {
      stockCorrection.setStatusSelect(StockCorrectionRepository.STATUS_VALIDATED);
      stockCorrection.setValidationDateT(baseService.getTodayDateTime().toLocalDateTime());
      return true;
    }
    return false;
  }

  public StockMove generateStockMove(StockCorrection stockCorrection) throws AxelorException {
    StockLocation toStockLocation = stockCorrection.getStockLocation();

    Company company = toStockLocation.getCompany();
    StockLocation fromStockLocation =
        stockConfigService.getInventoryVirtualStockLocation(
            stockConfigService.getStockConfig(company));

    StockLocationLine stockLocationLine = null;
    BigDecimal realQty = stockCorrection.getRealQty();
    Product product = stockCorrection.getProduct();
    TrackingNumber trackingNumber = stockCorrection.getTrackingNumber();

    if (stockCorrection.getTrackingNumber() == null) {
      stockLocationLine =
          stockLocationLineService.getOrCreateStockLocationLine(
              stockCorrection.getStockLocation(), stockCorrection.getProduct());
    } else {
      stockLocationLine =
          stockLocationLineService.getOrCreateDetailLocationLine(
              stockCorrection.getStockLocation(),
              stockCorrection.getProduct(),
              stockCorrection.getTrackingNumber());
    }

    if (stockLocationLine == null) {
      stockLocationLine =
          stockLocationLineService.getOrCreateStockLocationLine(toStockLocation, product);
    }

    BigDecimal diff = realQty.subtract(stockLocationLine.getCurrentQty());

    StockMove stockMove = null;

    if (diff.compareTo(BigDecimal.ZERO) == 0) {
      return null;
    } else if (diff.compareTo(BigDecimal.ZERO) > 0) {
      stockMove =
          stockMoveService.createStockMove(
              null,
              null,
              company,
              fromStockLocation,
              toStockLocation,
              null,
              null,
              null,
              StockMoveRepository.TYPE_INTERNAL);
    } else {
      stockMove =
          stockMoveService.createStockMove(
              null,
              null,
              company,
              toStockLocation,
              fromStockLocation,
              null,
              null,
              null,
              StockMoveRepository.TYPE_INTERNAL);
    }

    stockMove.setOriginTypeSelect(StockMoveRepository.ORIGIN_STOCK_CORRECTION);
    stockMove.setOriginId(stockCorrection.getId());
    stockMove.setStockCorrectionReason(stockCorrection.getStockCorrectionReason());

    BigDecimal productCostPrice =
        (BigDecimal) productCompanyService.get(product, "costPrice", company);

    StockMoveLine stockMoveLine =
        stockMoveLineService.createStockMoveLine(
            product,
            product.getName(),
            product.getDescription(),
            diff.abs(),
            productCostPrice,
            stockLocationLine.getAvgPrice(),
            product.getUnit(),
            stockMove,
            StockMoveLineService.TYPE_NULL,
            false,
            BigDecimal.ZERO);

    if (stockMoveLine == null) {
      throw new AxelorException(
          stockCorrection,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.STOCK_CORRECTION_1));
    }
    if (trackingNumber != null && stockMoveLine.getTrackingNumber() == null) {
      stockMoveLine.setTrackingNumber(trackingNumber);
    }

    stockMoveService.plan(stockMove);
    stockMoveService.copyQtyToRealQty(stockMove);
    stockMoveService.realize(stockMove, false);

    return stockMove;
  }

  @Override
  public void getDefaultQtys(
      StockLocationLine stockLocationLine, Map<String, Object> stockCorrectionQtys) {
    stockCorrectionQtys.put("realQty", stockLocationLine.getCurrentQty());
    stockCorrectionQtys.put("futureQty", stockLocationLine.getFutureQty());
  }
}
