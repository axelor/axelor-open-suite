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
package com.axelor.apps.stock.rest.dto;

import java.math.BigDecimal;

public class StockProductVariantAttributeResponse {

  private final String attrName;
  private final String attrValue;
  private final BigDecimal priceExtra;
  private final int applicationPriceSelect;

  public StockProductVariantAttributeResponse(
      String attrName, String attrValue, BigDecimal priceExtra, int applicationPriceSelect) {
    this.attrName = attrName;
    this.attrValue = attrValue;
    this.priceExtra = priceExtra;
    this.applicationPriceSelect = applicationPriceSelect;
  }

  public String getAttrName() {
    return attrName;
  }

  public String getAttrValue() {
    return attrValue;
  }

  public BigDecimal getPriceExtra() {
    return priceExtra;
  }

  public int getApplicationPriceSelect() {
    return applicationPriceSelect;
  }
}
