/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.tool.api.ResponseStructure;
import java.math.BigDecimal;
import java.util.Map;

public class StockProductResponse extends ResponseStructure {

  private final long id;
  private final BigDecimal realQty;
  private final BigDecimal futureQty;

  public StockProductResponse(Product product, Map<String, Object> qtys) {
    super(product.getVersion());
    this.id = product.getId();
    this.realQty = (BigDecimal) qtys.get("$realQty");
    this.futureQty = (BigDecimal) qtys.get("$futureQty");
  }

  public long getId() {
    return id;
  }

  public BigDecimal getRealQty() {
    return realQty;
  }

  public BigDecimal getFutureQty() {
    return futureQty;
  }
}
