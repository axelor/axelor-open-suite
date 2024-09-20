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
package com.axelor.apps.stock.db.repo;

import com.axelor.apps.base.db.BarcodeTypeConfig;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.StockLocationSaveService;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.studio.db.AppStock;
import com.google.inject.Inject;
import java.util.Map;

public class StockLocationStockRepository extends StockLocationRepository {

  protected AppStockService appStockService;
  protected BarcodeGeneratorService barcodeGeneratorService;

  @Inject
  public StockLocationStockRepository(
      AppStockService appStockService, BarcodeGeneratorService barcodeGeneratorService) {
    this.appStockService = appStockService;
    this.barcodeGeneratorService = barcodeGeneratorService;
  }

  /**
   * Override to remove incompatible stock locations in partners
   *
   * @param entity
   * @return
   */
  @Override
  public StockLocation save(StockLocation stockLocation) {
    Beans.get(StockLocationSaveService.class).removeForbiddenDefaultStockLocation(stockLocation);

    // Barcode generation
    AppStock appStock = appStockService.getAppStock();
    if (appStock != null
        && appStock.getActivateStockLocationBarCodeGeneration()
        && stockLocation.getBarCode() == null) {
      boolean addPadding = false;
      BarcodeTypeConfig barcodeTypeConfig = stockLocation.getBarcodeTypeConfig();
      if (!appStock.getEditStockLocationBarcodeType()) {
        barcodeTypeConfig = appStock.getStockLocationBarcodeTypeConfig();
      }
      MetaFile barcodeFile =
          barcodeGeneratorService.createBarCode(
              stockLocation.getId(),
              "StockLocationBarCode%d.png",
              stockLocation.getSerialNumber(),
              barcodeTypeConfig,
              addPadding);
      if (barcodeFile != null) {
        stockLocation.setBarCode(barcodeFile);
      }
    }

    return super.save(stockLocation);
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    Long stocklocationId = (Long) json.get("id");
    StockLocation stockLocation = find(stocklocationId);

    if (!stockLocation.getIsValued()) {
      return super.populate(json, context);
    }

    json.put(
        "stockLocationValue",
        Beans.get(StockLocationService.class).getStockLocationValue(stockLocation));

    return super.populate(json, context);
  }

  @Override
  public StockLocation copy(StockLocation entity, boolean deep) {

    StockLocation copy = super.copy(entity, deep);

    copy.clearDetailsStockLocationLineList();
    copy.clearStockLocationLineList();
    copy.setBarCode(null);
    copy.setSerialNumber(null);
    return copy;
  }
}
