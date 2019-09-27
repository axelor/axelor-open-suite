/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import com.axelor.apps.production.db.ProdProduct;
import java.math.BigDecimal;
import java.util.List;

public class ProdProductService {

  // TODO add conversion unit
  public BigDecimal computeQuantity(List<ProdProduct> prodProductList) {

    BigDecimal qty = BigDecimal.ZERO;

    if (prodProductList != null) {

      for (ProdProduct prodProduct : prodProductList) {

        qty = qty.add(prodProduct.getQty());
      }
    }

    return qty;
  }
}
