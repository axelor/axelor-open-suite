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
package com.axelor.apps.quality.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.quality.service.app.AppQualityService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.studio.db.AppQuality;
import jakarta.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NonCompliantReceptionServiceImpl implements NonCompliantReceptionService {

  protected final AppQualityService appQualityService;
  protected final QuarantineStockLocationService quarantineStockLocationService;
  protected final StockMoveLineService stockMoveLineService;
  protected final StockMoveLineRepository stockMoveLineRepository;

  @Inject
  public NonCompliantReceptionServiceImpl(
      AppQualityService appQualityService,
      QuarantineStockLocationService quarantineStockLocationService,
      StockMoveLineService stockMoveLineService,
      StockMoveLineRepository stockMoveLineRepository) {
    this.appQualityService = appQualityService;
    this.quarantineStockLocationService = quarantineStockLocationService;
    this.stockMoveLineService = stockMoveLineService;
    this.stockMoveLineRepository = stockMoveLineRepository;
  }

  @Override
  public boolean isSupplierReception(StockMove stockMove) {
    return stockMove != null
        && stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING
        && !stockMove.getIsReversion()
        && stockMove.getPartner() != null;
  }

  @Override
  public boolean isNonCompliantLine(StockMoveLine stockMoveLine) {
    Product product = stockMoveLine.getProduct();
    return stockMoveLine.getConformitySelect() == StockMoveLineRepository.CONFORMITY_NON_COMPLIANT
        && product != null
        && ProductRepository.PRODUCT_TYPE_STORABLE.equals(product.getProductTypeSelect())
        && stockMoveLine.getRealQty() != null
        && stockMoveLine.getRealQty().signum() > 0;
  }

  @Override
  public List<StockMoveLine> getNonCompliantLines(StockMove stockMove) {
    if (!isSupplierReception(stockMove) || stockMove.getStockMoveLineList() == null) {
      return List.of();
    }
    return stockMove.getStockMoveLineList().stream()
        .filter(this::isNonCompliantLine)
        .collect(Collectors.toList());
  }

  @Override
  public boolean isRedirectedToQuarantine(StockMoveLine stockMoveLine) {
    return isRedirectEnabled()
        && isSupplierReception(stockMoveLine.getStockMove())
        && isNonCompliantLine(stockMoveLine);
  }

  @Override
  public List<StockMoveLine> redirectToQuarantine(StockMove stockMove) throws AxelorException {
    if (!isRedirectEnabled()) {
      return List.of();
    }
    List<StockMoveLine> nonCompliantLines = getNonCompliantLines(stockMove);
    Map<StockMoveLine, StockLocation> quarantineByLine = new LinkedHashMap<>();
    for (StockMoveLine stockMoveLine : nonCompliantLines) {
      quarantineByLine.put(
          stockMoveLine,
          quarantineStockLocationService.getQuarantineStockLocation(
              stockMoveLine.getToStockLocation(), stockMove.getCompany()));
    }
    for (Map.Entry<StockMoveLine, StockLocation> entry : quarantineByLine.entrySet()) {
      StockMoveLine stockMoveLine = entry.getKey();
      if (stockMoveLine.getRealQty().compareTo(stockMoveLine.getQty()) < 0) {
        stockMoveLineService.splitIntoFulfilledMoveLineAndUnfulfilledOne(stockMoveLine);
      }
      stockMoveLine.setToStockLocation(entry.getValue());
      stockMoveLineRepository.save(stockMoveLine);
    }
    return nonCompliantLines;
  }

  protected boolean isRedirectEnabled() {
    AppQuality appQuality = appQualityService.getAppQuality();
    return appQuality != null && appQuality.getRedirectNonCompliantReceptionToQuarantine();
  }
}
