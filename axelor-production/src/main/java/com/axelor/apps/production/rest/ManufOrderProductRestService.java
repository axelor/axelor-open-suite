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
package com.axelor.apps.production.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.rest.dto.ManufOrderProductResponse;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import java.math.BigDecimal;
import java.util.List;

public interface ManufOrderProductRestService {

  String PRODUCT_TYPE_CONSUMED = "consumed";
  String PRODUCT_TYPE_PRODUCED = "produced";

  List<ManufOrderProductResponse> getConsumedProductList(ManufOrder manufOrder)
      throws AxelorException;

  List<ManufOrderProductResponse> getProducedProductList(ManufOrder manufOrder)
      throws AxelorException;

  StockMoveLine updateStockMoveLineQty(StockMoveLine stockMoveLine, BigDecimal qty)
      throws AxelorException;

  void addWasteProduct(ManufOrder manufOrder, ProdProduct wasteProduct);

  void updateProdProductQty(ProdProduct prodProduct, BigDecimal qty);

  StockMoveLine addManufOrderProduct(
      Product product,
      BigDecimal qty,
      TrackingNumber trackingNumber,
      ManufOrder manufOrder,
      String productType)
      throws AxelorException;
}
