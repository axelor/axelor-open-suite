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
package com.axelor.apps.sale.rest.dto;

import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseStructure;
import java.util.ArrayList;
import java.util.List;

public class ProductResponse extends ResponseStructure {

  protected Long productId;
  protected List<PriceResponse> prices;
  protected CurrencyResponse currency;

  public ProductResponse(Long productId, List<PriceResponse> prices, CurrencyResponse currency) {
    super(ObjectFinder.NO_VERSION);
    this.productId = productId;
    this.prices = new ArrayList<>(prices);
    this.currency = currency;
  }

  public Long getProductId() {
    return productId;
  }

  public List<PriceResponse> getPrices() {
    return prices;
  }

  public CurrencyResponse getCurrency() {
    return currency;
  }
}
