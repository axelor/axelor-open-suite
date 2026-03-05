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
package com.axelor.apps.stock.service.inventory.dto;

import com.axelor.i18n.I18n;
import org.apache.commons.csv.CSVRecord;

/** Represents a CSV inventory row along with the localized headers used to access its data. */
public final class InventoryCsvRowDto {

  private final String productName;
  private final String productCode;
  private final String productCategory;
  private final String rack;
  private final String trackingNumber;
  private final String currentQuantity;
  private final String realQuantity;
  private final String description;
  private final String lastInventoryDate;
  private final String stockLocation;
  private final String price;

  private InventoryCsvRowDto(CSVRecord record) {
    Headers headers = Headers.getInstance();
    this.productName = value(record, headers.productName);
    this.productCode = removeQuotes(value(record, headers.productCode));
    this.productCategory = value(record, headers.productCategory);
    this.rack = removeQuotes(value(record, headers.rack));
    this.trackingNumber = removeQuotes(value(record, headers.trackingNumber));
    this.currentQuantity = removeQuotes(value(record, headers.currentQuantity));
    this.realQuantity = value(record, headers.realQuantity);
    this.description = removeQuotes(value(record, headers.description));
    this.lastInventoryDate = value(record, headers.lastInventoryDate);
    this.stockLocation = removeQuotes(value(record, headers.stockLocation));
    this.price = value(record, headers.price);
  }

  public static InventoryCsvRowDto from(CSVRecord record) {
    return new InventoryCsvRowDto(record);
  }

  public static String[] headers() {
    return Headers.getInstance().asArray();
  }

  public String getProductName() {
    return productName;
  }

  public String getProductCode() {
    return productCode;
  }

  public String getProductCategory() {
    return productCategory;
  }

  public String getRack() {
    return rack;
  }

  public String getTrackingNumber() {
    return trackingNumber;
  }

  public String getCurrentQuantity() {
    return currentQuantity;
  }

  public String getRealQuantity() {
    return realQuantity;
  }

  public String getDescription() {
    return description;
  }

  public String getLastInventoryDate() {
    return lastInventoryDate;
  }

  public String getStockLocation() {
    return stockLocation;
  }

  public String getPrice() {
    return price;
  }

  private static String value(CSVRecord record, String header) {
    if (record == null || header == null) {
      return "";
    }
    if (!record.isMapped(header)) {
      return "";
    }
    String raw = record.get(header);
    return raw == null ? "" : raw;
  }

  private static String removeQuotes(String value) {
    return value == null ? "" : value.replace("\"", "");
  }

  private static final class Headers {

    private static final String PRODUCT_NAME_KEY = "Product Name";
    private static final String PRODUCT_CODE_KEY = "Product Code";
    private static final String PRODUCT_CATEGORY_KEY = "Product category";
    private static final String RACK_KEY = "Rack";
    private static final String TRACKING_NUMBER_KEY = "Tracking Number";
    private static final String CURRENT_QUANTITY_KEY = "Current Quantity";
    private static final String REAL_QUANTITY_KEY = "Real Quantity";
    private static final String DESCRIPTION_KEY = "Description";
    private static final String LAST_INVENTORY_DATE_KEY = "Last Inventory date";
    private static final String STOCK_LOCATION_KEY = "Stock Location";
    private static final String PRICE_KEY = "Price";

    private final String productName;
    private final String productCode;
    private final String productCategory;
    private final String rack;
    private final String trackingNumber;
    private final String currentQuantity;
    private final String realQuantity;
    private final String description;
    private final String lastInventoryDate;
    private final String stockLocation;
    private final String price;

    private Headers() {
      this.productName = I18n.get(PRODUCT_NAME_KEY);
      this.productCode = I18n.get(PRODUCT_CODE_KEY);
      this.productCategory = I18n.get(PRODUCT_CATEGORY_KEY);
      this.rack = I18n.get(RACK_KEY);
      this.trackingNumber = I18n.get(TRACKING_NUMBER_KEY);
      this.currentQuantity = I18n.get(CURRENT_QUANTITY_KEY);
      this.realQuantity = I18n.get(REAL_QUANTITY_KEY);
      this.description = I18n.get(DESCRIPTION_KEY);
      this.lastInventoryDate = I18n.get(LAST_INVENTORY_DATE_KEY);
      this.stockLocation = I18n.get(STOCK_LOCATION_KEY);
      this.price = I18n.get(PRICE_KEY);
    }

    private static Headers getInstance() {
      return Holder.INSTANCE;
    }

    private String[] asArray() {
      return new String[] {
        productName,
        productCode,
        productCategory,
        rack,
        trackingNumber,
        currentQuantity,
        realQuantity,
        description,
        lastInventoryDate,
        stockLocation,
        price
      };
    }

    private static final class Holder {
      private static final Headers INSTANCE = new Headers();
    }
  }
}
