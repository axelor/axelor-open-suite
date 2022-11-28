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
