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
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Map;
import javax.validation.ValidationException;

public class TrackingNumberManagementRepository extends TrackingNumberRepository {

  @Inject private StockLocationRepository stockLocationRepo;

  @Inject private StockLocationLineService stockLocationLineService;

  @Inject private AppStockService appStockService;

  @Inject private BarcodeGeneratorService barcodeGeneratorService;

  @Inject private MetaFiles metaFiles;

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
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return super.populate(json, context);
  }

  @Override
  public TrackingNumber save(TrackingNumber trackingNumber) {

    AppStock appStock = appStockService.getAppStock();

    if (appStock != null
        && appStock.getActivateBarCodeGeneration()
        && trackingNumber.getBarCode() == null
        && trackingNumber.getBarcodeTypeConfig() != null
        && trackingNumber.getSerialNumber() != null) {
      try {
        boolean addPadding = false;
        InputStream inStream =
            barcodeGeneratorService.createBarCode(
                trackingNumber.getSerialNumber(),
                trackingNumber.getBarcodeTypeConfig(),
                addPadding);
        if (inStream != null) {
          MetaFile barcodeFile =
              metaFiles.upload(
                  inStream, String.format("TrackingNumberBarCode%d.png", trackingNumber.getId()));
          trackingNumber.setBarCode(barcodeFile);
        }
      } catch (IOException e) {
        TraceBackService.trace(e);
        throw new ValidationException(e);
      } catch (AxelorException e) {
        TraceBackService.trace(e);
        throw new ValidationException(e);
      }
    }

    return super.save(trackingNumber);
  }
}
