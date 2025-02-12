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
package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.PickedProduct;
import java.util.Objects;

public class PickedProductAttrsServiceImpl implements PickedProductAttrsService {
  @Override
  public String getPickedProductDomain(PickedProduct pickedProduct) {
    Objects.requireNonNull(pickedProduct);

    if (pickedProduct.getFromStockLocation() != null) {
      String domain =
          "(self IN (SELECT stockLocationLine.product FROM StockLocationLine stockLocationLine WHERE stockLocationLine.currentQty > 0 AND stockLocationLine.stockLocation = %d))";

      return String.format(domain, pickedProduct.getFromStockLocation().getId());
    }
    return "self.id IN (0)";
  }

  @Override
  public String getTrackingNumberDomain(PickedProduct pickedProduct) {

    Objects.requireNonNull(pickedProduct);

    if (pickedProduct.getPickedProduct() != null && pickedProduct.getFromStockLocation() != null) {
      String domain =
          "self.product.id = %d AND"
              + " (self IN (SELECT stockLocationLine.trackingNumber FROM StockLocationLine stockLocationLine WHERE stockLocationLine.detailsStockLocation = %d AND stockLocationLine.currentQty > 0))";

      return String.format(
          domain,
          pickedProduct.getPickedProduct().getId(),
          pickedProduct.getFromStockLocation().getId());
    }

    // Must not be able to select
    return "self.id IN (0)";
  }
}
