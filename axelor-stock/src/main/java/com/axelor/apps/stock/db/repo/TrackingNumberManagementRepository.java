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
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.meta.db.MetaFile;
import com.axelor.studio.db.AppStock;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;

public class TrackingNumberManagementRepository extends TrackingNumberRepository {

  @Inject private StockLocationRepository stockLocationRepo;

  @Inject private StockLocationLineService stockLocationLineService;

  @Inject private AppStockService appStockService;

  @Inject private BarcodeGeneratorService barcodeGeneratorService;

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    try {
      Long trackingNumberId = (Long) json.get("id");
      TrackingNumber trackingNumber = find(trackingNumberId);

      if (trackingNumber.getProduct() != null && context.get("_parent") != null) {
        Map<String, Object> _parent = (Map<String, Object>) context.get("_parent");

        if (_parent.get("fromStockLocation") != null) {
          StockLocation stockLocation =
              stockLocationRepo.find(
                  Long.parseLong(((Map) _parent.get("fromStockLocation")).get("id").toString()));

          if (stockLocation != null) {
            BigDecimal availableQty =
                stockLocationLineService.getTrackingNumberAvailableQty(
                    stockLocation, trackingNumber);

            json.put("$availableQty", availableQty);
          }
        }
      } else if (trackingNumber.getProduct() != null) {
        json.put(
            "$availableQty",
            stockLocationLineService.getTrackingNumberAvailableQty(trackingNumber));
      } else {
        json.put("$availableQty", BigDecimal.ZERO);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return super.populate(json, context);
  }

  @Override
  public TrackingNumber save(TrackingNumber trackingNumber) {

    // Barcode generation
    AppStock appStock = appStockService.getAppStock();
    if (appStock != null
        && appStock.getActivateTrackingNumberBarCodeGeneration()
        && trackingNumber.getBarCode() == null) {
      boolean addPadding = false;
      /*
       * Barcode type config defaulting rule :
       * Check if edit barcode type config is enabled and tracking number was generated from configuration
       *    If true
       *        we take the type from configuration as default
       *    Else
       *        we take the barcode type config from App Stock as default
       */
      BarcodeTypeConfig barcodeTypeConfig;
      if (appStock.getEditTrackingNumberBarcodeType()
          && trackingNumber.getProduct() != null
          && trackingNumber.getProduct().getTrackingNumberConfiguration() != null) {
        TrackingNumberConfiguration trackingNumberConfiguration =
            trackingNumber.getProduct().getTrackingNumberConfiguration();
        barcodeTypeConfig = trackingNumberConfiguration.getBarcodeTypeConfig();
      } else {
        barcodeTypeConfig = appStock.getTrackingNumberBarcodeTypeConfig();
      }
      MetaFile barcodeFile =
          barcodeGeneratorService.createBarCode(
              trackingNumber.getId(),
              "TrackingNumberBarCode%d.png",
              trackingNumber.getSerialNumber(),
              barcodeTypeConfig,
              addPadding);
      if (barcodeFile != null) {
        trackingNumber.setBarCode(barcodeFile);
      }
    }

    return super.save(trackingNumber);
  }
}
