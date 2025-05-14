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
package com.axelor.apps.stock.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BarcodeTypeConfig;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import com.axelor.apps.stock.service.StockLocationLineFetchService;
import com.axelor.apps.stock.service.TrackingNumberCompanyService;
import com.axelor.apps.stock.service.TrackingNumberService;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.db.JPA;
import com.axelor.meta.db.MetaFile;
import com.axelor.studio.db.AppStock;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;
import javax.persistence.PersistenceException;

public class TrackingNumberManagementRepository extends TrackingNumberRepository {

  protected AppStockService appStockService;
  protected BarcodeGeneratorService barcodeGeneratorService;
  protected ProductCompanyService productCompanyService;
  protected StockLocationLineFetchService stockLocationLineFetchService;
  protected TrackingNumberService trackingNumberService;
  protected final TrackingNumberCompanyService trackingNumberCompanyService;

  @Inject
  public TrackingNumberManagementRepository(
      AppStockService appStockService,
      BarcodeGeneratorService barcodeGeneratorService,
      ProductCompanyService productCompanyService,
      StockLocationLineFetchService stockLocationLineFetchService,
      TrackingNumberService trackingNumberService,
      TrackingNumberCompanyService trackingNumberCompanyService) {
    this.appStockService = appStockService;
    this.barcodeGeneratorService = barcodeGeneratorService;
    this.productCompanyService = productCompanyService;
    this.stockLocationLineFetchService = stockLocationLineFetchService;
    this.trackingNumberService = trackingNumberService;
    this.trackingNumberCompanyService = trackingNumberCompanyService;
  }

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
              JPA.find(
                  StockLocation.class,
                  Long.parseLong(((Map) _parent.get("fromStockLocation")).get("id").toString()));

          if (stockLocation != null) {
            BigDecimal availableQty =
                stockLocationLineFetchService.getTrackingNumberAvailableQty(
                    stockLocation, trackingNumber);

            json.put("$availableQty", availableQty);
          }
        }
      } else if (trackingNumber.getProduct() != null) {
        json.put(
            "$availableQty",
            stockLocationLineFetchService.getTrackingNumberAvailableQty(trackingNumber));
        Company company = trackingNumberCompanyService.getCompany(trackingNumber).orElse(null);
        json.put(
            "productTrackingNumberConfiguration",
            productCompanyService.get(
                trackingNumber.getProduct(), "trackingNumberConfiguration", company));

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
    try {
      // This method calls is to check circular parent dependencies.
      trackingNumberService.getOriginParents(trackingNumber);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }

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
      TrackingNumberConfiguration trackingNumberConfiguration;

      if (trackingNumber.getProduct() != null) {
        try {
          trackingNumberConfiguration =
              (TrackingNumberConfiguration)
                  productCompanyService.get(
                      trackingNumber.getProduct(), "trackingNumberConfiguration", null);

        } catch (AxelorException e) {
          TraceBackService.traceExceptionFromSaveMethod(e);
          throw new PersistenceException(e.getMessage(), e);
        }
      } else {
        trackingNumberConfiguration = null;
      }

      if (appStock.getEditTrackingNumberBarcodeType()
          && trackingNumber.getProduct() != null
          && trackingNumberConfiguration != null) {

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
