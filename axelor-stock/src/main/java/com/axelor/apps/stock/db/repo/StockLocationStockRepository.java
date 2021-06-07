/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.db.repo;

import com.axelor.apps.base.db.AppStock;
import com.axelor.apps.base.db.BarcodeTypeConfig;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.StockLocationSaveService;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.validation.ValidationException;

public class StockLocationStockRepository extends StockLocationRepository {

  protected AppStockService appStockService;
  protected BarcodeGeneratorService barcodeGeneratorService;
  protected MetaFiles metaFiles;

  @Inject
  public StockLocationStockRepository(
      AppStockService appStockService,
      BarcodeGeneratorService barcodeGeneratorService,
      MetaFiles metaFiles) {
    this.appStockService = appStockService;
    this.barcodeGeneratorService = barcodeGeneratorService;
    this.metaFiles = metaFiles;
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

    if (appStock != null && appStock.getActivateStockLocationBarCodeGeneration()) {

      BarcodeTypeConfig barcodeTypeConfig = stockLocation.getBarcodeTypeConfig();
      // Apply defaulting if needed
      if (appStock.getEditStockLocationBarcodeType() == false) {
        barcodeTypeConfig = appStock.getStockLocationBarcodeTypeConfig();
      }

      if (stockLocation.getBarCode() == null
          && barcodeTypeConfig != null
          && stockLocation.getSerialNumber() != null) {
        try {
          boolean addPadding = false;
          InputStream inStream =
              barcodeGeneratorService.createBarCode(
                  stockLocation.getSerialNumber(), barcodeTypeConfig, addPadding);
          if (inStream != null) {
            MetaFile barcodeFile =
                metaFiles.upload(
                    inStream, String.format("StockLocationBarCode%d.png", stockLocation.getId()));
            stockLocation.setBarCode(barcodeFile);
          }
        } catch (IOException e) {
          TraceBackService.trace(e);
          throw new ValidationException(e);
        } catch (AxelorException e) {
          TraceBackService.trace(e);
          throw new ValidationException(e);
        }
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
    return copy;
  }
}
